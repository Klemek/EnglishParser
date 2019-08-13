package fr.klemek.englishparser.utils;

import fr.klemek.englishparser.model.dict.*;
import fr.klemek.logger.Logger;
import org.hibernate.Transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public final class DictionaryManager {

    private static boolean initialized;

    private static HashMap<String, String> irregularPlurals;
    private static HashMap<String, String> irregularAdverbs;
    private static HashMap<String, String> irregularThirdPersons;
    private static HashMap<String, String[]> irregularVerbs;
    private static HashMap<String, String> genderNouns;

    private static HashMap<Integer, Integer> synSetMapping;
    private static int autoInc;

    /*
    private static List<String> modals = Arrays.asList(
            "can", "could", "may", "might", "will", "would", "shall", " should", "must");
    */
    private DictionaryManager() {

    }

    public static boolean init() {
        return init(false);
    }

    public static boolean init(boolean skipPreComputed) {
        if (!DatabaseManager.isInitialized()) {
            Logger.log(Level.SEVERE, "Database not initialized, aborting");
            return false;
        }
        Logger.log("Dictionary {0}initialized", DictionaryManager.isInitialized() ? "" : "not ");
        if (!initialized) {
            Logger.log("Initializing dictionary...");
            long t0 = System.currentTimeMillis();
            long t1;
            try (Connection conn = DatabaseManager.openConnection(true)) {
                t1 = System.currentTimeMillis();
                emptyDictionary(conn);
                Logger.log("\tEmptied dictionary in {0}", Utils.getTimeSpent(t1));

                if (!skipPreComputed && FileUtils.resourceFileExists("dict/sql/ep_fill.sql")) {
                    Logger.log("\tImporting pre-computed data...");
                    t1 = System.currentTimeMillis();
                    DatabaseManager.importSQL(conn, "dict/sql/ep_fill");

                    Logger.log("\tImported pre-computed data in {0}", Utils.getTimeSpent(t1));

                    Logger.log("\t\t+{0} synonyms' set", Definition.getAll().size());
                    Logger.log("\t\t+{0} words", Word.getAll().size());
                    Logger.log("\t\t+{0} definitions", Definition.getAll().size());
                    Logger.log("\t\t+{0} nouns", Noun.getAll().size());
                    Logger.log("\t\t+{0} verbs", Verb.getAll().size());
                    Logger.log("\t\t+{0} adjectives", Adjective.getAll().size());

                } else {
                    t1 = System.currentTimeMillis();
                    DatabaseManager.importSQL(conn, "dict/sql/wordnet_init");
                    Logger.log("\tImported wordnet structure in {0}", Utils.getTimeSpent(t1));

                    Logger.log("\tImporting wordnet data...");
                    t1 = System.currentTimeMillis();
                    DatabaseManager.importSQL(conn, "dict/sql/wordnet_fill");
                    Logger.log("\tImported wordnet data in {0}", Utils.getTimeSpent(t1));

                    Transaction tx = DatabaseManager.getSessionFactory().getCurrentSession().beginTransaction();

                    t1 = System.currentTimeMillis();
                    loadIrregularPlurals();
                    Logger.log("\tLoaded {1} irregular nouns in {0}", Utils.getTimeSpent(t1), irregularPlurals.size());

                    t1 = System.currentTimeMillis();
                    loadIrregularThirdPersons();
                    Logger.log("\tLoaded {1} irregular third persons in {0}", Utils.getTimeSpent(t1), irregularThirdPersons.size());

                    t1 = System.currentTimeMillis();
                    loadIrregularVerbs();
                    Logger.log("\tLoaded {1} irregular verbs in {0}", Utils.getTimeSpent(t1), irregularVerbs.size());

                    t1 = System.currentTimeMillis();
                    loadIrregularAdverbs();
                    Logger.log("\tLoaded {1} irregular adverbs in {0}", Utils.getTimeSpent(t1), irregularAdverbs.size());

                    t1 = System.currentTimeMillis();
                    loadGenderNouns();
                    Logger.log("\tLoaded {1} gender nouns in {0}", Utils.getTimeSpent(t1), genderNouns.size());

                    Logger.log("\tComputing words... (this will take a while) ");
                    t1 = System.currentTimeMillis();
                    computeWords(conn);
                    Logger.log("\tComputed words in {0}", Utils.getTimeSpent(t1));

                    synSetMapping.clear();

                    tx.commit();

                    t1 = System.currentTimeMillis();
                    DatabaseManager.importSQL(conn, "dict/sql/wordnet_drop");
                    Logger.log("\tDropped wordnet structure in {0}", Utils.getTimeSpent(t1));
                }

                DatabaseManager.setDictionaryInitialized(conn, true);
            } catch (Exception e) {
                Logger.log(e, "Exception in dictionary initialization");
                try (Connection conn = DatabaseManager.openConnection(true)) {
                    DatabaseManager.importSQL(conn, "dict/sql/wordnet_drop");
                } catch (IOException | SQLException e2) {
                    Logger.log(e2, "Exception while trying to drop wordnet tables");
                    return false;
                }
                return false;
            }
            initialized = true;
            Logger.log("Dictionary initialized in {0}", Utils.getTimeSpent(t0));
        }
        return true;
    }

    private static void emptyDictionary(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.addBatch("DELETE FROM dict_def WHERE 1");
            st.addBatch("DELETE FROM dict_noun WHERE 1");
            st.addBatch("DELETE FROM dict_verb WHERE 1");
            st.addBatch("DELETE FROM dict_adj WHERE 1");
            st.addBatch("DELETE FROM dict_word WHERE 1");
            st.executeBatch();
        }
    }

    private static void loadIrregularVerbs() throws IOException {
        irregularVerbs = new HashMap<>();
        try (BufferedReader reader = FileUtils.readResourceFile("dict/irregular_verbs.csv")) {

            String line;
            String[][] table;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (StringUtils.countChar(line, ';') == 2) {
                    table = Utils.getTable(line, ";", "/");

                    if (table.length == 1)
                        table = new String[][]{table[0], {""}, {""}};
                    if (table.length == 2)
                        table = new String[][]{table[0], table[1], {""}};

                    irregularVerbs.put(table[0][0], new String[]{table[1][0], table[2][0]});

                    String root;
                    String pt;
                    String pp;
                    for (int i = 0; i < Utils.maxDepth(table); i++) {
                        root = i < table[0].length ? table[0][i] : table[0][0];
                        pt = i < table[1].length ? table[1][i] : table[0][0];
                        pp = i < table[2].length ? table[2][i] : table[0][0];
                        new Verb(root, pt, pp,
                                pp.length() == 0 ? "" : getPresentParticiple(root),
                                getThirdPerson(root)).save();
                    }
                } else {
                    Logger.log(line);
                }
            }
        }
    }

    private static void loadIrregularPlurals() throws IOException {
        irregularPlurals = new HashMap<>();
        try (BufferedReader reader = FileUtils.readResourceFile("dict/irregular_plurals.csv")) {
            String line;
            String[] spl;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                spl = line.split(";");
                if (line.length() > 0 && spl.length >= 2) {
                    new Noun(spl[0], spl[1]).save();
                    if (!irregularPlurals.containsKey(spl[0]))
                        irregularPlurals.put(spl[0], spl[1]);
                }
            }
        }
    }

    private static void loadIrregularAdverbs() throws IOException {
        irregularAdverbs = new HashMap<>();
        try (BufferedReader reader = FileUtils.readResourceFile("dict/irregular_adverbs.csv")) {

            String line;
            String[] spl;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                spl = line.split(";");
                if (line.length() > 0 && spl.length >= 2) {
                    new Adjective(spl[0], spl[1]).save();
                    if (!irregularAdverbs.containsKey(spl[0]))
                        irregularAdverbs.put(spl[0], spl[1]);
                }
            }
        }
    }

    private static void loadIrregularThirdPersons() throws IOException {
        irregularThirdPersons = new HashMap<>();
        try (BufferedReader reader = FileUtils.readResourceFile("dict/irregular_third_persons.csv")) {

            String line;
            String[] spl;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                spl = line.split(";");
                if (line.length() > 0 && spl.length >= 2)
                    irregularThirdPersons.put(spl[0], spl[1]);
            }
        }
    }

    private static void loadGenderNouns() throws IOException {
        genderNouns = new HashMap<>();
        try (BufferedReader reader = FileUtils.readResourceFile("dict/gender_nouns.csv")) {

            String line;
            String[] spl;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                spl = line.split(";");
                if (line.length() > 0 && spl.length >= 2) {
                    genderNouns.put(spl[0], spl[1]);
                    Noun nm = Noun.getByWord(spl[0]);
                    if (nm == null) {
                        nm = new Noun(spl[0], getNounPlural(spl[0]));
                        nm.save();
                    }
                    Noun nf = Noun.getByWord(spl[1]);
                    if (nf != null)
                        nf.delete();
                    nm.setFemale(spl[1], getNounPlural(spl[1]));
                    nm.update();
                }
            }
        }
    }

    private static void computeWords(Connection conn) throws SQLException {
        int nounCount = Noun.getAll().size();
        int verbCount = Verb.getAll().size();
        int adjCount = Adjective.getAll().size();

        long t0 = System.currentTimeMillis();

        try (Statement st = conn.createStatement()) {
            try (ResultSet rs = st.executeQuery("SELECT * FROM wn_synset")) {

                int rowcount = DatabaseManager.getRowCount(rs);
                int row = 0;
                int rowstep = rowcount / 10;

                Word w;
                String word;
                String type;
                int synsetId;
                int wNum;

                while (rs.next()) {
                    word = rs.getString("word");
                    type = rs.getString("ss_type");
                    synsetId = rs.getInt("synset_id");
                    wNum = rs.getInt("w_num");
                    w = null;
                    if (!word.contains("_")) {
                        word = word.split("\\(")[0];
                        switch (type) {
                            case "n":
                                w = new Word(word, Word.Type.NOUN, getSynSetId(synsetId), wNum);
                                if (Noun.getByWord(word) == null)
                                    computeNewNoun(word);
                                break;
                            case "v":
                                w = new Word(word, Word.Type.VERB, getSynSetId(synsetId), wNum);
                                if (Verb.getByWord(word) == null)
                                    computeNewVerb(word);
                                break;
                            case "a":
                            case "s":
                                w = new Word(word, Word.Type.ADJ, getSynSetId(synsetId), wNum);
                                if (Adjective.getByWord(word) == null)
                                    computeNewAdjective(word);
                                break;
                            case "r":
                                w = new Word(word, Word.Type.ADV, getSynSetId(synsetId), wNum);
                                break;
                            default:
                                break;
                        }
                    }
                    if (w != null)
                        w.save();
                    if (row > 0 && row % rowstep == 0) {
                        long dt = System.currentTimeMillis() - t0;
                        Logger.log("\tComputed {0} words ({1}%) (ETA {2})", row, 100 * row / rowcount, Utils.getTimeSpan((long) (rowcount * dt / (float) row)));
                    }
                    row++;
                }
            }
            try (ResultSet rs = st.executeQuery("SELECT * FROM wn_gloss")) {
                String gloss;
                int synsetId;
                Definition d;

                int rowcount = DatabaseManager.getRowCount(rs);
                int row = 0;
                int rowstep = rowcount / 10;

                while (rs.next()) {
                    gloss = rs.getString("gloss");
                    synsetId = rs.getInt("synset_id");
                    if (synSetMapping.containsKey(synsetId)) {
                        d = new Definition(getSynSetId(synsetId), gloss);
                        d.save();
                    }

                    if (row > 0 && row % rowstep == 0) {
                        long dt = System.currentTimeMillis() - t0;
                        Logger.log("\tComputed {0} definitions ({1}%) (ETA {2})", row, 100 * row / rowcount, Utils.getTimeSpan((long) (rowcount * dt / (float) row)));
                    }
                    row++;
                }
            }
        }

        Logger.log("\t\t+{0} synonyms' set", autoInc);
        Logger.log("\t\t+{0} words", Word.getAll().size());
        Logger.log("\t\t+{0} definitions", Definition.getAll().size());
        Logger.log("\t\t+{0} nouns", Noun.getAll().size() - nounCount);
        Logger.log("\t\t+{0} verbs", Verb.getAll().size() - verbCount);
        Logger.log("\t\t+{0} adjectives", Adjective.getAll().size() - adjCount);
    }

    private static void computeNewNoun(String word) {
        Noun nf = Noun.getByFemale(word);
        if (nf != null)
            return;
        for (Map.Entry<String, String> gn : genderNouns.entrySet()) {
            if (word.endsWith(gn.getKey())) {
                String prefix = word.substring(0, word.lastIndexOf(gn.getKey()));
                Noun nm = new Noun(word, getNounPlural(word));
                nm.setFemale(prefix + gn.getValue(), prefix + getNounPlural(gn.getValue()));
                nm.save();
                return;
            } else if (word.endsWith(gn.getValue())) {
                String prefix = word.substring(0, word.lastIndexOf(gn.getValue()));
                if (Noun.getByWord(prefix + gn.getKey()) != null)
                    return;
                Noun nm = new Noun(prefix + gn.getKey(), prefix + getNounPlural(gn.getKey()));
                nm.setFemale(word, getNounPlural(word));
                nm.save();
                return;
            }
        }
        new Noun(word, getNounPlural(word)).save();
    }

    private static void computeNewAdjective(String word) {
        new Adjective(word, getAdverb(word)).save();
    }

    private static void computeNewVerb(String word) {
        String pastTense = null;
        String pastPart = null;
        String presPart = getPresentParticiple(word);
        String thirdPers = getThirdPerson(word);
        for (Map.Entry<String, String[]> irregular : irregularVerbs.entrySet())
            if (StringUtils.partOf(word, irregular.getKey(), '-')) {
                String[] pasts = irregular.getValue();
                if (pasts[1].length() > 0) {
                    String prefix = word.substring(0, word.lastIndexOf(irregular.getKey()));
                    pastTense = prefix + pasts[0];
                    pastPart = prefix + pasts[1];
                    break;
                }
            }
        if (pastTense == null) {
            pastTense = getRegularPast(word);
            pastPart = getRegularPast(word);
        }
        new Verb(word, pastTense, pastPart, presPart, thirdPers).save();
    }

    private static int getSynSetId(int synsetId) {
        if (synSetMapping == null)
            synSetMapping = new HashMap<>();
        if (!synSetMapping.containsKey(synsetId))
            synSetMapping.put(synsetId, autoInc++);
        return synSetMapping.get(synsetId);
    }

    static String getAdverb(String adj) {
        if (irregularAdverbs != null)
            for (Map.Entry<String, String> irregular : irregularAdverbs.entrySet())
                if (StringUtils.partOf(adj, irregular.getKey(), 4)) {
                    String prefix = adj.substring(0, adj.lastIndexOf(irregular.getKey()));
                    return prefix + irregular.getValue();
                }
        int lp = adj.length() - 1;
        if (adj.charAt(lp) == 'y')
            return adj.substring(0, lp) + "ily";
        if (adj.endsWith("le"))
            return adj.substring(0, lp) + "y";
        if (adj.endsWith("ll"))
            return adj + "y";
        if (adj.endsWith("ic"))
            return adj + "ally";
        return adj + "ly";
    }

    static String getNounPlural(String noun) {
        if (irregularPlurals != null)
            for (Map.Entry<String, String> irregular : irregularPlurals.entrySet())
                if (StringUtils.partOf(noun, irregular.getKey(), 4, "man")) {
                    String prefix = noun.substring(0, noun.lastIndexOf(irregular.getKey()));
                    return prefix + irregular.getValue();
                }
        int lp = noun.length() - 1;
        if (noun.endsWith("ch") || //sibilants and regular o
                noun.endsWith("sh") ||
                noun.endsWith("zh") ||
                noun.charAt(lp) == 'x' ||
                noun.charAt(lp) == 's' ||
                noun.charAt(lp) == 'z')
            return noun + "es";
        if (noun.charAt(lp) == 'y' && StringUtils.isConsonant(noun.charAt(lp - 1)))
            return noun.substring(0, lp) + "ies";
        return noun + "s";
    }

    static String getPresentParticiple(String verb) {
        int lp = verb.length() - 1;
        if (verb.charAt(lp) == 'x' || verb.charAt(lp) == 's') //one consonent sibilants
            return verb + "ing";
        if (shortVowelConsonant(verb)) //short vowel
            return verb + verb.charAt(lp) + "ing";
        if (verb.charAt(lp) == 'e' && StringUtils.isConsonant(verb.charAt(lp - 1)))
            return verb.substring(0, lp) + "ing";
        return verb + "ing";
    }

    static String getRegularPast(String verb) {
        int lp = verb.length() - 1;
        if (verb.charAt(lp) == 'y' && StringUtils.isConsonant(verb.charAt(lp - 1)))
            return verb.substring(0, lp) + "ied";
        if (verb.charAt(lp) == 'x' || verb.charAt(lp) == 's') //one consonent sibilants
            return verb + "ed";
        if (shortVowelConsonant(verb)) //short vowel
            return verb + verb.charAt(lp) + "ed";
        if (verb.charAt(lp) == 'e' && StringUtils.isConsonant(verb.charAt(lp - 1)))
            return verb + "d";
        return verb + "ed";
    }

    private static boolean shortVowelConsonant(String word) {
        word = word
                .replace("qui", "qi")
                .replace("qua", "qa");
        int lp = word.length() - 1;
        return StringUtils.isConsonant(word.charAt(lp)) &&
                StringUtils.isVowel(word.charAt(lp - 1)) &&
                (lp < 2 || StringUtils.isConsonant(word.charAt(lp - 2)));
    }

    static String getThirdPerson(String verb) {
        if (irregularThirdPersons != null && irregularThirdPersons.containsKey(verb))
            return irregularThirdPersons.get(verb);
        int lp = verb.length() - 1;
        if (verb.charAt(lp) == 'y' && StringUtils.isConsonant(verb.charAt(lp - 1)))
            return verb.substring(0, lp) + "ies";
        if (verb.endsWith("ch") || //sibilants
                verb.endsWith("zh") ||
                verb.endsWith("sh") ||
                verb.charAt(lp) == 'x' ||
                verb.charAt(lp) == 's' ||
                verb.charAt(lp) == 'z')
            return verb + "es";
        return verb + "s";
    }

    public static boolean isInitialized() {
        return initialized;
    }

    static void setInitialized(boolean initialized) {
        DictionaryManager.initialized = initialized;
    }
}
