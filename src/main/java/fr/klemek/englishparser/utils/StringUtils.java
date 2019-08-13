package fr.klemek.englishparser.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class that store useful string functions.
 */
public final class StringUtils {

    private static final List<Character> vowels = Arrays.asList('a', 'e', 'i', 'o', 'u', 'y');

    private StringUtils() {
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
    static Long stringToLong(String text) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Check if a String is alphanumeric including some chars
     *
     * @param source   the String to test
     * @param included included chars other than alphanumerics
     * @return true if it passes
     */
    static boolean isAlphaNumeric(String source, Character... included) {
        if (source == null)
            return true;
        List<Character> includedList = Arrays.asList(included);
        for (char c : source.toCharArray())
            if (!Character.isAlphabetic(c) && !Character.isDigit(c) && !includedList.contains(c))
                return false;
        return true;
    }

    static boolean containsIgnoreCase(String s1, String s2) {
        return s1.toLowerCase().contains(s2.toLowerCase());
    }

    static boolean isVowel(char c) {
        return vowels.contains(c);
    }

    static boolean isConsonant(char c) {
        return Character.isAlphabetic(c) && !vowels.contains(c);
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

    static int countChar(String src, char c) {
        int count = 0;
        for (char c2 : src.toCharArray())
            if (c2 == c)
                count++;
        return count;
    }
}
