package fr.klemek.englishparser;

import fr.klemek.englishparser.model.dict.Word;
import fr.klemek.englishparser.utils.DatabaseManager;
import fr.klemek.englishparser.utils.DictionaryManager;
import fr.klemek.englishparser.utils.Utils;
import fr.klemek.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;

public class Launch {

    public static void main(String... args) {
        Logger.init("logging.properties");
        Logger.log("Initializing...");
        if (!DatabaseManager.init(Utils.getConnectionString("db_connection_string")))
            throw new IllegalStateException("Database cannot be initialized");
        if (!DictionaryManager.init())
            throw new IllegalStateException("Dictionary cannot be initialized");
        Logger.log("Initialized");

        testRead();
    }

    private static void testRead() {
        StringBuilder data = new StringBuilder();
        try (BufferedReader reader = Utils.readFile("test_res/a_very_short_story.txt")) {
            reader.readLine(); //skip first line
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.endsWith(" "))
                    line += " ";
                data.append(line);
            }
        } catch (IOException e) {
            Logger.log(e);
        }
        String[] sentences = data.toString().trim().split("\\. ");
        analyseSentence(sentences[0]);
    }

    private static void analyseSentence(String s) {
        Logger.log(s);
        for (String word : s.split(" "))
            analyseWord(word);
    }

    private static void analyseWord(String w) {
        Logger.log(w);

        EnumMap<Word.Type, Integer> p = new EnumMap<>(Word.Type.class);
        List<Word> found = Word.getByWord(w.toLowerCase());
        for (Word word : found)
            p.put(word.getType(), p.getOrDefault(word.getType(), 0) + 1);

        Word.Type[] types = Word.Type.values();
        Arrays.sort(types, Comparator.comparingInt(o -> -p.getOrDefault(o, 0)));
        for (Word.Type t : types) {
            if (p.containsKey(t))
                Logger.log("\t{0} {1}%", t, 100 * p.get(t) / (float) found.size());
        }
    }
}
