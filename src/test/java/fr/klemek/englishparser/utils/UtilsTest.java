package fr.klemek.englishparser.utils;

import fr.klemek.logger.Logger;
import fr.klemek.englishparser.TestUtils;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class UtilsTest {

    @Test
    public void testStringToIntegerSuccess() {
        String test = "123456";
        assertEquals(Integer.valueOf(123456), Utils.stringToInteger(test));
    }

    @Test
    public void testStringToLongSuccess() {
        String test = "123456";
        assertEquals(Long.valueOf(123456), Utils.stringToLong(test));
    }

    @Test
    public void testStringToIntegerFail() {
        String test = "test";
        assertNull(Utils.stringToInteger(test));
        assertNull(Utils.stringToInteger(null));
    }

    @Test
    public void testStringToLongFail() {
        String test = "test";
        assertNull(Utils.stringToLong(test));
        assertNull(Utils.stringToLong(null));
    }

    @Test
    public void testContainsIgnoreCase() {
        assertTrue(Utils.containsIgnoreCase("abcdef", "def"));
        assertTrue(Utils.containsIgnoreCase("abcDef", "def"));
        assertTrue(Utils.containsIgnoreCase("abcdef", "dEf"));
        assertTrue(Utils.containsIgnoreCase("abcdeF", "Def"));
        assertFalse(Utils.containsIgnoreCase("abcdef", "aef"));
    }

    @Test
    public void testGetStringFail() {
        assertNull(Utils.getString("invalid key"));
    }

    @Test
    public void testGetIntFail() {
        assertEquals(0, Utils.getInt("invalid key"));
    }

    @Test
    public void testGetIntFail2() {
        assertEquals(0, Utils.getInt("db_user"));
    }

    @Test
    public void testGetConnectionStringFail() {
        assertNull(Utils.getConnectionString("invalid key"));
    }

    @Test
    public void testGetExtension() {
        File file = new File("test.ova");
        assertEquals("ova", Utils.getExtension(file));
        file = new File("test");
        assertNull(Utils.getExtension(file));
        assertNull(Utils.getExtension((File) null));
    }

    @Test
    public void testCoalesce() {
        assertNull(Utils.coalesce());
        assertNull(Utils.coalesce((String) null));
        assertNull(Utils.coalesce((String) null, null));
        assertEquals("a", Utils.coalesce(null, "a"));
        assertEquals("a", Utils.coalesce(null, "a", "b"));
    }

    @Test
    public void testIsAlphaNumeric() {
        assertTrue(Utils.isAlphaNumeric("aBc"));
        assertTrue(Utils.isAlphaNumeric("123"));
        assertTrue(Utils.isAlphaNumeric("1B2a3Z45bc"));
        assertFalse(Utils.isAlphaNumeric(" -;!:,%"));
        assertFalse(Utils.isAlphaNumeric("1B2a3Z4 5bc"));
        assertTrue(Utils.isAlphaNumeric(""));
        assertTrue(Utils.isAlphaNumeric(null));
        assertTrue(Utils.isAlphaNumeric("1B2a3Z4 5bc", ' '));
        assertTrue(Utils.isAlphaNumeric(" -;!:,%", ' ', '-', ';', '!', ':', ',', '%'));
    }

    @Test
    public void testGetDefaultPackage() {
        assertEquals("fr.klemek.englishparser", Utils.getDefaultPackage());
    }

    @Test
    public void testGetCallingClassName() {
        assertEquals("Thread", Utils.getCallingClassName(0));
        assertEquals("Utils", Utils.getCallingClassName(1));
        assertEquals("UtilsTest", Utils.getCallingClassName(2));
    }

    @Test
    public void testIsVowel() {
        assertTrue(Utils.isVowel('a'));
        assertFalse(Utils.isVowel('b'));
        assertFalse(Utils.isVowel(' '));
        assertFalse(Utils.isVowel('à'));
    }

    @Test
    public void testIsConsonant() {
        assertTrue(Utils.isConsonant('b'));
        assertFalse(Utils.isConsonant('a'));
        assertFalse(Utils.isConsonant(' '));
        assertTrue(Utils.isConsonant('ç'));
    }

    @Test
    public void testPartOf() {
        assertTrue(Utils.partOf("abcdef", "def", 3));
        assertFalse(Utils.partOf("abcdaf", "def", 3));
        assertFalse(Utils.partOf("abcdef", "def", 4));
        assertTrue(Utils.partOf("abcdef", "def", 4, "def"));
    }

    @Test
    public void testPartOfDelimiter() {
        assertTrue(Utils.partOf("abc-def", "def", '-'));
        assertFalse(Utils.partOf("abcdef", "def", '-'));
        assertFalse(Utils.partOf("abc-daf", "def", '-'));
    }

    @Test
    public void testGetTable() {
        String data = "a#b#c:d#e";
        String[][] table = Utils.getTable(data, ":", "#");
        assertEquals(2, table.length);
        assertEquals(3, table[0].length);
        assertEquals(2, table[1].length);
        assertEquals("a-b-c", String.join("-", table[0]));
        assertEquals("d-e", String.join("-", table[1]));
    }

    @Test
    public void countChar() {
        assertEquals(0, Utils.countChar("abc", 'd'));
        assertEquals(3, Utils.countChar("dabddc", 'd'));
    }

    @Test
    public void maxDepth() {
        String[][] table = Utils.getTable("a#b#c:d#e", ":", "#");
        assertEquals(3, Utils.maxDepth(table));
        table = new String[0][];
        assertEquals(0, Utils.maxDepth(table));
        table = new String[1][5];
        assertEquals(5, Utils.maxDepth(table));
    }

    @BeforeClass
    public static void setUpClass() {
        Logger.init("logging.properties", TestUtils.LOG_LEVEL);
    }
}
