package fr.klemek.englishparser;

import fr.klemek.englishparser.utils.Config;
import fr.klemek.englishparser.utils.DatabaseManager;
import fr.klemek.logger.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import static org.junit.Assert.assertTrue;

public final class TestUtils {

    public static final String DB_CONNECTION_STRING = Config.getConnectionString("db_connection_string");

    public static final Level LOG_LEVEL = Level.INFO;

    private static Connection conn = null;

    private TestUtils() {

    }

    /*
     * Database test utils
     */

    public static void cleanDatabase(Connection conn) throws SQLException, IOException {
        DatabaseManager.importSQL(conn, "sql/clean.sql");
        Logger.log("Database wiped");
    }

    private static void emptyDatabase() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeBatch();
        }
        Logger.log("Database emptied");
    }

    public static void emptyDictionary() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.addBatch("DELETE FROM dict_def WHERE 1");
            st.addBatch("DELETE FROM dict_noun WHERE 1");
            st.addBatch("DELETE FROM dict_verb WHERE 1");
            st.addBatch("DELETE FROM dict_adj WHERE 1");
            st.addBatch("DELETE FROM dict_word WHERE 1");
            st.executeBatch();
        }
        DatabaseManager.setDictionaryInitialized(conn, false);
        Logger.log("Dictionary emptied");
    }

    public static void initTest() throws Exception {
        TestUtils.initTest(false);
    }

    private static void initTest(boolean emptyDatabase) throws Exception {
        TestUtils.initTest(emptyDatabase, false);
    }

    /**
     * prepare different elements for tests : database, LDAPUtils
     *
     * @param emptyDatabase if the database is emptied
     * @param emptyDictionary if the database is emptied
     * @throws Exception on error
     */
    public static void initTest(boolean emptyDatabase, boolean emptyDictionary) throws Exception {
        Logger.setLevel(LOG_LEVEL);
        if (conn == null) {
            Logger.init("logging.properties", LOG_LEVEL);
            assertTrue(DatabaseManager.init(TestUtils.DB_CONNECTION_STRING));
            conn = DatabaseManager.openConnection();
        }
        if (emptyDatabase)
            TestUtils.emptyDatabase();
        if (emptyDictionary)
            TestUtils.emptyDictionary();
    }

    public static Connection getConnection() {
        return conn;
    }


}
