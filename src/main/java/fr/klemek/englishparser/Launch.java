package fr.klemek.englishparser;

import fr.klemek.englishparser.utils.DatabaseManager;
import fr.klemek.englishparser.utils.DictionaryManager;
import fr.klemek.englishparser.utils.Utils;
import fr.klemek.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;

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

    private static void testRead(){
        StringBuilder data = new StringBuilder();
        try (BufferedReader reader = Utils.readFile("test_res/a_very_short_story.txt")) {
            reader.readLine(); //skip first line
            String line;
            while((line = reader.readLine()) != null){
                line = line.trim();
                if(!line.endsWith(" "))
                    line += " ";
                data.append(line);
            }
        } catch (IOException e) {
            Logger.log(e);
        }
        Logger.log(data.toString());
    }
}
