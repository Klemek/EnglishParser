package fr.klemek.englishparser.utils;

import fr.klemek.englishparser.TestUtils;
import fr.klemek.logger.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.fail;

public class ConfigTest {

    @BeforeClass
    public static void setUpClass() {
        Logger.init("logging.properties", TestUtils.LOG_LEVEL);
    }

    @Test
    public void testGetStringFail() {
        try {
            Config.getString("invalid key");
            fail();
        } catch (RuntimeException e) {
            //success
        }
    }

    @Test
    public void testGetIntFail() {
        try {
            Config.getInt("invalid key");
            fail();
        } catch (RuntimeException e) {
            //success
        }
    }

    @Test
    public void testGetConnectionStringFail() {
        try {
            Config.getConnectionString("invalid key");
            fail();
        } catch (RuntimeException e) {
            //success
        }
    }
}
