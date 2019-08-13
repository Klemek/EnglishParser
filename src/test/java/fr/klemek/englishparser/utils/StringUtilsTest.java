package fr.klemek.englishparser.utils;

import fr.klemek.englishparser.TestUtils;
import fr.klemek.logger.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class StringUtilsTest {

    @BeforeClass
    public static void setUpClass() {
        Logger.init("logging.properties", TestUtils.LOG_LEVEL);
    }

    @Test
    public void testStringToIntegerSuccess() {
        String test = "123456";
        assertEquals(Integer.valueOf(123456), StringUtils.stringToInteger(test));
    }

    @Test
    public void testStringToLongSuccess() {
        String test = "123456";
        assertEquals(Long.valueOf(123456), StringUtils.stringToLong(test));
    }

    @Test
    public void testStringToIntegerFail() {
        String test = "test";
        assertNull(StringUtils.stringToInteger(test));
        assertNull(StringUtils.stringToInteger(null));
    }

    @Test
    public void testStringToLongFail() {
        String test = "test";
        assertNull(StringUtils.stringToLong(test));
        assertNull(StringUtils.stringToLong(null));
    }

    @Test
    public void testContainsIgnoreCase() {
        assertTrue(StringUtils.containsIgnoreCase("abcdef", "def"));
        assertTrue(StringUtils.containsIgnoreCase("abcDef", "def"));
        assertTrue(StringUtils.containsIgnoreCase("abcdef", "dEf"));
        assertTrue(StringUtils.containsIgnoreCase("abcdeF", "Def"));
        assertFalse(StringUtils.containsIgnoreCase("abcdef", "aef"));
    }

    @Test
    public void testIsAlphaNumeric() {
        assertTrue(StringUtils.isAlphaNumeric("aBc"));
        assertTrue(StringUtils.isAlphaNumeric("123"));
        assertTrue(StringUtils.isAlphaNumeric("1B2a3Z45bc"));
        assertFalse(StringUtils.isAlphaNumeric(" -;!:,%"));
        assertFalse(StringUtils.isAlphaNumeric("1B2a3Z4 5bc"));
        assertTrue(StringUtils.isAlphaNumeric(""));
        assertTrue(StringUtils.isAlphaNumeric(null));
        assertTrue(StringUtils.isAlphaNumeric("1B2a3Z4 5bc", ' '));
        assertTrue(StringUtils.isAlphaNumeric(" -;!:,%", ' ', '-', ';', '!', ':', ',', '%'));
    }

    @Test
    public void testIsVowel() {
        assertTrue(StringUtils.isVowel('a'));
        assertFalse(StringUtils.isVowel('b'));
        assertFalse(StringUtils.isVowel(' '));
        assertFalse(StringUtils.isVowel('à'));
    }

    @Test
    public void testIsConsonant() {
        assertTrue(StringUtils.isConsonant('b'));
        assertFalse(StringUtils.isConsonant('a'));
        assertFalse(StringUtils.isConsonant(' '));
        assertTrue(StringUtils.isConsonant('ç'));
    }

    @Test
    public void testPartOf() {
        assertTrue(StringUtils.partOf("abcdef", "def", 3));
        assertFalse(StringUtils.partOf("abcdaf", "def", 3));
        assertFalse(StringUtils.partOf("abcdef", "def", 4));
        assertTrue(StringUtils.partOf("abcdef", "def", 4, "def"));
    }

    @Test
    public void testPartOfDelimiter() {
        assertTrue(StringUtils.partOf("abc-def", "def", '-'));
        assertFalse(StringUtils.partOf("abcdef", "def", '-'));
        assertFalse(StringUtils.partOf("abc-daf", "def", '-'));
    }

    @Test
    public void countChar() {
        assertEquals(0, StringUtils.countChar("abc", 'd'));
        assertEquals(3, StringUtils.countChar("dabddc", 'd'));
    }
}
