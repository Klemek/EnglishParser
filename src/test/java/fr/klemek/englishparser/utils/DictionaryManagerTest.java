package fr.klemek.englishparser.utils;

import fr.klemek.betterlists.BetterArrayList;
import fr.klemek.betterlists.BetterList;
import fr.klemek.englishparser.TestUtils;
import fr.klemek.englishparser.model.dict.Adjective;
import fr.klemek.englishparser.model.dict.Noun;
import fr.klemek.englishparser.model.dict.Verb;
import fr.klemek.englishparser.model.dict.Word;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DictionaryManagerTest {

    @Test
    public void testInit() throws Exception {
        TestUtils.initTest(false, true);
        DictionaryManager.setInitialized(false);

        assertFalse(DictionaryManager.isInitialized());
        assertTrue(DictionaryManager.init());
        assertTrue(DictionaryManager.isInitialized());

        try (Connection conn = DatabaseManager.openConnection()) {
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery("SELECT * FROM db_info")) {
                    assertTrue(rs.first());
                    assertEquals("1", rs.getString("dict_init"));
                }
            }
        }

        testWords();
        testDefinitions();
        testSynonyms();
        testNouns();
        testAdjectives();
        testVerbs();
    }

    @Test
    @Ignore
    public void testWords() throws Exception {
        TestUtils.initTest();
        BetterList<Word> lst = BetterArrayList.fromList(Word.getAll());

        for (String word : new String[]{"entity", "frog", "frogs", "toad", "Frenchman", "Frenchwoman", "spadefoot"})
            assertTrue(lst.any(w -> w.getWord().equals(word) && w.getType() == Word.Type.NOUN));

        for (String word : new String[]{"easy", "economic", "dying", "nonpublic"})
            assertTrue(lst.any(w -> w.getWord().equals(word) && w.getType() == Word.Type.ADJ));

        for (String word : new String[]{"easily", "economically"})
            assertTrue(lst.any(w -> w.getWord().equals(word) && w.getType() == Word.Type.ADV));

        for (String word : new String[]{"hack", "do", "can"})
            assertTrue(lst.any(w -> w.getWord().equals(word) && w.getType() == Word.Type.VERB));

        assertFalse(lst.any(w -> w.getWord().equals("French_person") && w.getType() == Word.Type.NOUN));

        assertEquals(2, lst.count(w -> w.getWord().equals("frog") && w.getType() == Word.Type.NOUN));
    }

    @Test
    @Ignore
    public void testDefinitions() throws Exception {
        TestUtils.initTest();
        for (String word : new String[]{"entity", "frog", "Frenchman"})
            for (Word w : Word.getByWord(word))
                assertNotNull(w.getDefinition());
    }

    @Test
    @Ignore
    public void testSynonyms() throws Exception {
        TestUtils.initTest();
        Word w = Word.getByWord("toad").get(0);
        List<Word> syns = w.getSynonyms();
        assertEquals(2, syns.size());
        assertEquals("frog", syns.get(0).getWord());
        assertEquals("toad", syns.get(1).getWord());
    }

    @Test
    @Ignore
    public void testNouns() throws Exception {
        TestUtils.initTest();
        assertEquals(new Noun("entity", "entities"), Noun.getByWord("entity"));
        assertEquals(new Noun("frog", "frogs"), Noun.getByWord("frog"));
        assertEquals(new Noun("toad", "toads"), Noun.getByWord("toad"));
        assertEquals(new Noun("Frenchman", "Frenchmen", "Frenchwoman", "Frenchwomen"), Noun.getByWord("Frenchman"));
        assertNull(Noun.getByWord("Frenchwoman"));
        assertEquals(new Noun("spadefoot", "spadefeet"), Noun.getByWord("spadefoot"));
        assertNull(Noun.getByWord("French_person"));
    }

    @Test
    @Ignore
    public void testAdjectives() throws Exception {
        TestUtils.initTest();
        assertEquals(new Adjective("easy", "easily"), Adjective.getByWord("easy"));
        assertEquals(new Adjective("economic", "economically"), Adjective.getByWord("economic"));
        assertEquals(new Adjective("dying", "dyingly"), Adjective.getByWord("dying"));
        assertEquals(new Adjective("nonpublic", "nonpublicly"), Adjective.getByWord("nonpublic"));
    }

    @Test
    @Ignore
    public void testVerbs() throws Exception {
        TestUtils.initTest();
        assertEquals(new Verb("hack", "hacked", "hacked", "hacking", "hacks"), Verb.getByWord("hack"));
        assertEquals(new Verb("do", "did", "done", "doing", "does"), Verb.getByWord("do"));
        assertEquals(new Verb("can", "could", "", "", "can"), Verb.getByWord("can"));
        assertEquals(new Verb("babysit", "babysat", "babysat", "babysitting", "babysits"), Verb.getByWord("babysit"));
    }

    @Test
    public void testGetPossibleAdverb() {
        for (String root : new String[]{"cheap", "quick", "slow"})
            assertEquals(root + "ly", DictionaryManager.getAdverb(root));
        for (String root : new String[]{"easy", "angry", "happy", "lucky"})
            assertEquals(root.substring(0, root.length() - 1) + "ily", DictionaryManager.getAdverb(root));
        for (String root : new String[]{"probable", "terrible", "gentle"})
            assertEquals(root.substring(0, root.length() - 1) + "y", DictionaryManager.getAdverb(root));
        for (String root : new String[]{"basic", "tragic", "economic"})
            assertEquals(root + "ally", DictionaryManager.getAdverb(root));
    }

    @Test
    public void testGetNounPlural() {
        for (String root : new String[]{"file", "centre", "girl", "book", "computer", "ambition",
                "chief", "spoof", "cliff", "journey", "boy", "radio", "stereo", "video"})
            assertEquals(root + "s", DictionaryManager.getNounPlural(root));
        for (String root : new String[]{"wash", "box", "match", "glass", "bus", "business", "coach", "peach",})
            assertEquals(root + "es", DictionaryManager.getNounPlural(root));
        for (String root : new String[]{"country", "baby", "body", "memory"})
            assertEquals(root.substring(0, root.length() - 1) + "ies", DictionaryManager.getNounPlural(root));
    }

    @Test
    public void testGetVerbForms() {
        //long vowel or diphthong followed by a consonant or ending in a consonant cluster
        for (String root : new String[]{"paint", "claim", "devour", "play", "delight", "clamp", "lacquer"}) {
            assertEquals(root + "ed", DictionaryManager.getRegularPast(root));
            assertEquals(root + "ing", DictionaryManager.getPresentParticiple(root));
            assertEquals(root + "s", DictionaryManager.getThirdPerson(root));
        }
        //short vowel
        for (String root : new String[]{"chat", "chop", "compel", "quiz", "squat", "quit", "equal", "whiz"}) {
            char lastChar = root.charAt(root.length() - 1);
            assertEquals(root + lastChar + "ed", DictionaryManager.getRegularPast(root));
            assertEquals(root + lastChar + "ing", DictionaryManager.getPresentParticiple(root));
            if (!root.equals("quiz") && !root.equals("whiz")) //irregular third person
                assertEquals(root + "s", DictionaryManager.getThirdPerson(root));
        }
        //consonant followed by e
        for (String root : new String[]{"dance", "save", "devote", "evolve", "quote"}) {
            assertEquals(root + "d", DictionaryManager.getRegularPast(root));
            assertEquals(root.substring(0, root.length() - 1) + "ing", DictionaryManager.getPresentParticiple(root));
            assertEquals(root + "s", DictionaryManager.getThirdPerson(root));
        }
        //sibilants
        for (String root : new String[]{"kiss", "bless", "box", "polish", "preach", "bias", "box"}) {
            assertEquals(root + "ed", DictionaryManager.getRegularPast(root));
            assertEquals(root + "ing", DictionaryManager.getPresentParticiple(root));
            assertEquals(root + "es", DictionaryManager.getThirdPerson(root));
        }
        //consonant followed by y
        for (String root : new String[]{"comply", "copy", "magnify"}) {
            assertEquals(root.substring(0, root.length() - 1) + "ied", DictionaryManager.getRegularPast(root));
            assertEquals(root + "ing", DictionaryManager.getPresentParticiple(root));
            assertEquals(root.substring(0, root.length() - 1) + "ies", DictionaryManager.getThirdPerson(root));
        }
    }
}