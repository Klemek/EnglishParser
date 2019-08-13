package fr.klemek.englishparser.utils;

import fr.klemek.englishparser.TestUtils;
import fr.klemek.logger.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UtilsTest {

    @Test
    public void testCoalesce() {
        assertNull(Utils.coalesce());
        assertNull(Utils.coalesce((String) null));
        assertNull(Utils.coalesce((String) null, null));
        assertEquals("a", Utils.coalesce(null, "a"));
        assertEquals("a", Utils.coalesce(null, "a", "b"));
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
