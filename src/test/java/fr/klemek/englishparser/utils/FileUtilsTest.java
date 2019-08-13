package fr.klemek.englishparser.utils;

import fr.klemek.englishparser.TestUtils;
import fr.klemek.logger.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FileUtilsTest {

    @BeforeClass
    public static void setUpClass() {
        Logger.init("logging.properties", TestUtils.LOG_LEVEL);
    }

    @Test
    public void testGetExtension() {
        File file = new File("test.ova");
        assertEquals("ova", FileUtils.getExtension(file));
        file = new File("test");
        assertNull(FileUtils.getExtension(file));
        assertNull(FileUtils.getExtension((File) null));
    }
}
