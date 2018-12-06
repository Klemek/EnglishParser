package fr.klemek.englishparser.utils;

import fr.klemek.logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Utility class that store useful misc functions.
 *
 * @author Clement Gouin
 */
public final class Utils {

    private static final ResourceBundle CONFIGURATION_BUNDLE = ResourceBundle.getBundle("configuration");
    private static final List<Character> vowels = Arrays.asList('a', 'e', 'i', 'o', 'u', 'y');
    private static String localIP = null;

    private Utils() {
    }

    /*
     * Configuration utils
     */

    /**
     * Get a configuration string by its key.
     *
     * @param key the key in the config file
     * @return the string or null if not found
     */
    public static String getString(String key) {
        try {
            return CONFIGURATION_BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            Logger.log(Level.SEVERE, "Missing configuration string {0}", key);
            return null;
        }
    }

    /**
     * Get a string from version.
     *
     * @param key the key in the config file
     * @return the string or null if not found
     */
    public static String getConnectionString(String key) {
        String connectionString = Utils.getString(key);
        if (connectionString == null)
            return null;
        String localIP = getLocalIP();
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
    public static int getInt(String key) {
        try {
            String string = Utils.getString(key);
            if (string == null)
                return 0;
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            Logger.log(Level.SEVERE, "Not integer string at key {0}", key);
            return 0;
        }
    }

    /**
     * Transform a JSONArray into a List of jsonObject.
     *
     * @param src the source JSONArray
     * @return a list of jsonObject
     */
    public static List<JSONObject> jArrayToJObjectList(JSONArray src) {
        ArrayList<JSONObject> lst = new ArrayList<>(src.length());
        for (int i = 0; i < src.length(); i++)
            lst.add(src.getJSONObject(i));
        return lst;
    }

    /**
     * Convert a string to an Integer.
     *
     * @param text a text to convert to Integer
     * @return Integer or null if the string couldn't be converted
     */
    public static Integer stringToInteger(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Convert a string into a Long.
     *
     * @param text a text to convert to Long
     * @return Long or null if the string couldn't be converted
     */
    public static Long stringToLong(String text) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Return the class name from the calling class in th stack trace.
     *
     * @param stackLevel the level in the stack trace
     * @return the classname of th calling class
     */
    public static String getCallingClassName(int stackLevel) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackLevel >= stackTrace.length)
            return null;
        String[] source = stackTrace[stackLevel].getClassName().split("\\.");
        return source[source.length - 1];
    }

    public static boolean containsIgnoreCase(String s1, String s2) {
        return s1.toLowerCase().contains(s2.toLowerCase());
    }

    /**
     * Return the extension of a file.
     *
     * @param file the file
     * @return the extension of the file
     */
    public static String getExtension(File file) {
        if (file == null) {
            return null;
        }
        return getExtension(file.getName());
    }

    /**
     * Return the extension of a file.
     *
     * @param fileName the file
     * @return the extension of the file
     */
    public static String getExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i + 1);
        }
        return null;
    }

    /**
     * Compare 2 lists
     *
     * @param src     source list
     * @param toCheck list to check
     * @param <T>     type of lists
     * @return a list containing elements of src not found in the toCheck list
     */
    public static <T> List<T> compareLists(List<T> src, List<T> toCheck) {
        List<T> out = new ArrayList<>();
        for (T obj : src)
            if (!toCheck.contains(obj))
                out.add(obj);
        return out;
    }


    @SuppressWarnings("unchecked")
    public static <T> T coalesce(T... items) {
        for (T i : items) if (i != null) return i;
        return null;
    }

    /**
     * Check if a String is alphanumeric including some chars
     *
     * @param source   the String to test
     * @param included included chars other than alphanumerics
     * @return true if it passes
     */
    public static boolean isAlphaNumeric(String source, Character... included) {
        if (source == null)
            return true;
        List<Character> includedList = Arrays.asList(included);
        for (char c : source.toCharArray())
            if (!Character.isAlphabetic(c) && !Character.isDigit(c) && !includedList.contains(c))
                return false;
        return true;
    }

    /**
     * Navigate through a JSONObject by keys
     *
     * @param source the original JSONObject
     * @param keys   the keys to find successively
     * @return the found JSONObject or null if the key was not found
     */
    public static JSONObject navigateJSON(JSONObject source, String... keys) {
        JSONObject obj = source;
        for (String key : keys) {
            if (!obj.has(key))
                return null;
            obj = obj.getJSONObject(key);
        }
        return obj;
    }

    /**
     * @return the current ip on the first local network
     */
    private static String getLocalIP() {
        if (localIP != null)
            return localIP;
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            String localIp = localhost.getHostAddress();
            if (localIp.startsWith("192.168")) {
                localIP = localIp;
                return localIP;
            }
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface itf = networkInterfaces.nextElement();
                if (itf.isUp() && !itf.isLoopback() && !itf.isVirtual() && !itf.isPointToPoint()) {
                    Enumeration<InetAddress> adds = itf.getInetAddresses();
                    while (adds.hasMoreElements()) {
                        InetAddress add = adds.nextElement();
                        if (add.isSiteLocalAddress() && add.getHostAddress().startsWith("192.168")) {
                            localIP = add.getHostAddress();
                            return localIP;
                        }
                    }
                }
            }
        } catch (UnknownHostException | SocketException e) {
            Logger.log(e);
        }
        return localIP;
    }

    static String getDefaultPackage() {
        String pkg = Utils.class.getName();
        pkg = pkg.substring(0, pkg.lastIndexOf('.'));
        pkg = pkg.substring(0, pkg.lastIndexOf('.'));
        return pkg;
    }

    static boolean isVowel(char c) {
        return vowels.contains(c);
    }

    static boolean isConsonant(char c) {
        return Character.isAlphabetic(c) && !vowels.contains(c);
    }

    public static String getTimeSpent(long t0) {
        long ms = System.currentTimeMillis() - t0;
        long sec = ms / 1000;
        ms %= 1000;
        long min = sec / 60;
        sec %= 60;
        long hour = min / 60;
        min %= 60;

        List<String> times = new ArrayList<>();
        if (hour > 0)
            times.add(hour + " h");
        if (min > 0)
            times.add(min + " m");
        if (sec > 0 && times.size() < 2)
            times.add(sec + " s");
        if (ms > 0 && times.size() < 2)
            times.add(ms + " ms");

        if (times.isEmpty())
            return "no time";

        return String.join(" ", times);
    }

    static boolean partOf(String word1, String word2, int minLen, String... excluded) {
        if (word1.equals(word2))
            return true;
        if (word2.length() < minLen && !Arrays.asList(excluded).contains(word2))
            return false;
        return word1.endsWith(word2);
    }

    static boolean partOf(String word1, String word2, char separator) {
        int i = word1.lastIndexOf(separator);
        if (i < 0)
            return false;
        return word1.substring(i + 1).equals(word2);
    }

    static String[][] getTable(String src, String reg1, String reg2) {
        String[] spl1 = src.split(reg1);
        String[][] out = new String[spl1.length][];
        for (int i = 0; i < spl1.length; i++)
            out[i] = spl1[i].split(reg2);
        return out;
    }

    static int countChar(String src, char c) {
        int count = 0;
        for (char c2 : src.toCharArray())
            if (c2 == c)
                count++;
        return count;
    }

    static <T> int maxDepth(T[][] table) {
        int max = 0;
        for (T[] row : table)
            if (row.length > max)
                max = row.length;
        return max;
    }

    static BufferedReader readResourceFile(String name) {
        return new BufferedReader(new InputStreamReader(Utils.class.getClassLoader().getResourceAsStream(name)));
    }

    public static BufferedReader readFile(String path) throws FileNotFoundException {
        return new BufferedReader(new FileReader(new File(path)));
    }
}
