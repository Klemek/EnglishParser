package fr.klemek.englishparser.utils;

import java.io.*;

/**
 * Utility class that store useful file functions.
 */
public final class FileUtils {

    private FileUtils() {
    }

    /**
     * Return the extension of a file.
     *
     * @param file the file
     * @return the extension of the file
     */
    static String getExtension(File file) {
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
    static String getExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i + 1);
        }
        return null;
    }

    static BufferedReader readResourceFile(String name) {
        InputStream is = FileUtils.class.getClassLoader().getResourceAsStream(name);
        if (is == null)
            throw new RuntimeException("Resource file '" + name + "' not found");
        return new BufferedReader(new InputStreamReader(is));
    }

    public static BufferedReader readFile(String path) throws FileNotFoundException {
        return new BufferedReader(new FileReader(new File(path)));
    }

    static boolean resourceFileExists(String name) {
        return FileUtils.class.getClassLoader().getResourceAsStream(name) != null;
    }
}
