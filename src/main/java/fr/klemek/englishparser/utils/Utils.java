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
        return getTimeSpan(System.currentTimeMillis() - t0);
    }

    static String getTimeSpan(long dt) {
        long sec = dt / 1000;
        dt %= 1000;
        long min = sec / 60;
        sec %= 60;
        long hour = min / 60;
        min %= 60;

        List<String> times = new ArrayList<>();
        if (hour > 0)
            times.add(hour + " h");
        if (min > 0)
            times.add((times.size() > 0 ? StringUtils.padLeft("" + min, "0", 2) : min) + " m");
        if ((times.isEmpty() && sec > 0) || times.size() == 1)
            times.add((times.size() > 0 ? StringUtils.padLeft("" + sec, "0", 2) : sec) + " s");
        if (times.size() < 2)
            times.add((times.size() > 0 ? StringUtils.padLeft("" + dt, "0", 3) : dt) + " ms");

        if (times.isEmpty())
            return "no time";

        return String.join(" ", times);
    }

    static String getETA(float processed, float total, long t0) {
        return getTimeSpan((long) (((System.currentTimeMillis() - t0) / processed) * (total - processed)));
    }

    static String getETA(List<Long> timestamps, float step, float total) {
        long t0 = timestamps.get(0);
        int length = timestamps.size();
        long t2 = System.currentTimeMillis();
        timestamps.add(t2);
        if (length == 1)
            return getETA(step, total, t0);
        float current = step * length;
        long /*min = 0, max = 0,*/ sum = 0;
        long t1, eta;
        float processed1;
        for (int i = 1; i < length; i++) {
            t1 = timestamps.get(i);
            processed1 = step * i;
            eta = getETA(t0, processed1, t1, current, t2, total);
            /*if (min == 0 || eta < min)
                min = eta;
            if (max == 0 || eta > max)
                max = eta;*/
            sum += eta;
        }

        /*System.out.println("min: " + getTimeSpan(min));
        System.out.println("median: " + getTimeSpan((max + min) / 2));
        System.out.println("mean: " + getTimeSpan(sum / (length - 1)));
        System.out.println("max: " + getTimeSpan(max));*/

        return getTimeSpan(sum / (length - 1));
    }

    private static long getETA(long t0, float processed1, long t1, float processed2, long t2, float total) {
        float x1 = processed1 / total;
        float x2 = processed2 / total;
        float dt1 = t1 - t0;
        float dt2 = t2 - t0;

        float dk = (dt2 - dt1 * x2 / x1) * (2 / (x2 * (x2 - x1)));
        float k0 = dt1 / x1 - dk * x1 / 2;

        return (long) (k0 * (1 - x2) + dk * (1 - x2 * x2) / 2);
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
