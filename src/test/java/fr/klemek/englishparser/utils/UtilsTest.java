package fr.klemek.englishparser.utils;

import fr.klemek.englishparser.TestUtils;
import fr.klemek.logger.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UtilsTest {

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
    public void testMxDepth() {
        String[][] table = Utils.getTable("a#b#c:d#e", ":", "#");
        assertEquals(3, Utils.maxDepth(table));
        table = new String[0][];
        assertEquals(0, Utils.maxDepth(table));
        table = new String[1][5];
        assertEquals(5, Utils.maxDepth(table));
    }

    @Test
    public void testGetTimeSpan() {
        assertEquals("23 ms", Utils.getTimeSpan(23));
        assertEquals("1 s 023 ms", Utils.getTimeSpan(1023));
        assertEquals("15 s 000 ms", Utils.getTimeSpan(15000));
        assertEquals("1 m 00 s", Utils.getTimeSpan(60 * 1000 + 23));
        assertEquals("15 m 05 s", Utils.getTimeSpan(15 * 60 * 1000 + 5 * 1000 + 23));
        assertEquals("5 h 02 m", Utils.getTimeSpan(5 * 60 * 60 * 1000 + 2 * 60 * 1000 + 5 * 1000 + 23));
    }

    @BeforeClass
    public static void setUpClass() {
        Logger.init("logging.properties", TestUtils.LOG_LEVEL);
    }
}
