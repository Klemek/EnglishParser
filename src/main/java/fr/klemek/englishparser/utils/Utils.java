package fr.klemek.englishparser.utils;

import fr.klemek.logger.Logger;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Utility class that store useful misc functions.
 */
public final class Utils {

    private static String localIP = null;

    private Utils() {
    }

    /*
     * Transform a JSONArray into a List of jsonObject.
     *
     * @param src the source JSONArray
     * @return a list of jsonObject
     */
    /*public static List<JSONObject> jArrayToJObjectList(JSONArray src) {
        ArrayList<JSONObject> lst = new ArrayList<>(src.length());
        for (int i = 0; i < src.length(); i++)
            lst.add(src.getJSONObject(i));
        return lst;
    }*/

    /**
     * Return the class name from the calling class in th stack trace.
     *
     * @param stackLevel the level in the stack trace
     * @return the classname of th calling class
     */
    static String getCallingClassName(int stackLevel) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackLevel >= stackTrace.length)
            return null;
        String[] source = stackTrace[stackLevel].getClassName().split("\\.");
        return source[source.length - 1];
    }

    /**
     * Compare 2 lists
     *
     * @param src     source list
     * @param toCheck list to check
     * @param <T>     type of lists
     * @return a list containing elements of src not found in the toCheck list
     */
    static <T> List<T> compareLists(List<T> src, List<T> toCheck) {
        List<T> out = new ArrayList<>();
        for (T obj : src)
            if (!toCheck.contains(obj))
                out.add(obj);
        return out;
    }


    @SuppressWarnings("unchecked")
    static <T> T coalesce(T... items) {
        for (T i : items) if (i != null) return i;
        return null;
    }

    /*
     * Navigate through a JSONObject by keys
     *
     * @param source the original JSONObject
     * @param keys   the keys to find successively
     * @return the found JSONObject or null if the key was not found
     */
    /*public static JSONObject navigateJSON(JSONObject source, String... keys) {
        JSONObject obj = source;
        for (String key : keys) {
            if (!obj.has(key))
                return null;
            obj = obj.getJSONObject(key);
        }
        return obj;
    }*/

    /**
     * @return the current ip on the first local network
     */
    static String getLocalIP() {
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

    static String[][] getTable(String src, String reg1, String reg2) {
        String[] spl1 = src.split(reg1);
        String[][] out = new String[spl1.length][];
        for (int i = 0; i < spl1.length; i++)
            out[i] = spl1[i].split(reg2);
        return out;
    }

    static <T> int maxDepth(T[][] table) {
        int max = 0;
        for (T[] row : table)
            if (row.length > max)
                max = row.length;
        return max;
    }
}
