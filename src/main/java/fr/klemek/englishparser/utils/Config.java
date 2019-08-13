package fr.klemek.englishparser.utils;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility class that store useful configuration functions.
 */
public final class Config {

    private static final ResourceBundle CONFIGURATION_BUNDLE = ResourceBundle.getBundle("configuration");

    private Config() {
    }

    /**
     * Get a configuration string by its key.
     *
     * @param key the key in the config file
     * @return the string or null if not found
     */
    static String getString(String key) {
        try {
            return CONFIGURATION_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            throw new RuntimeException("Missing configuration string : " + key);
        }
    }

    /**
     * Get a string from version.
     *
     * @param key the key in the config file
     * @return the string or null if not found
     */
    public static String getConnectionString(String key) {
        String connectionString = Config.getString(key);
        String localIP = Utils.getLocalIP();
        if (localIP != null)
            return connectionString.replace(localIP, "localhost");
        else
            return connectionString;
    }

    /**
     * Get a configuration string by its key.
     *
     * @param key the key in the config file
     * @return the integer or 0 if not found
     */
    static int getInt(String key) {
        try {
            return Integer.parseInt(Config.getString(key));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid Integer at key : " + key);
        }
    }

}
