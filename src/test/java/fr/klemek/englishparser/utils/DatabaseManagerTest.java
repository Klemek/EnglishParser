package fr.klemek.englishparser.utils;

import fr.klemek.englishparser.TestUtils;
import fr.klemek.englishparser.model.dict.Noun;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DatabaseManagerTest {

    @Before
    public void setUp() throws Exception {
        TestUtils.initTest();
    }

    @Test
    public void testUpdateDatabase() throws SQLException, IOException {
        try (Connection conn = DatabaseManager.openConnection(true)) {
            TestUtils.cleanDatabase(conn);
        }

        assertFalse(DatabaseManager.tableExists(TestUtils.getConnection(), "db_info"));
        assertTrue(DatabaseManager.updateDatabase());
        assertTrue(DatabaseManager.tableExists(TestUtils.getConnection(), "db_info"));

        try (Statement st = TestUtils.getConnection().createStatement()) {
            try (ResultSet rs = st.executeQuery("SELECT * FROM db_info")) {
                rs.first();
                assertEquals(Utils.getInt("db_version"), rs.getInt("version"));
            }
        }
    }

    @Test
    public void testTableExists() {
        assertTrue(DatabaseManager.tableExists(TestUtils.getConnection(), "db_info"));
        assertFalse(DatabaseManager.tableExists(TestUtils.getConnection(), "invalid_table_name"));
    }

    @Test
    public void testDatabaseOpenConnectionError() throws SQLException {
        try {
            DatabaseManager.setDefaultConnectionString(null);
            DatabaseManager.openConnection();
            fail("No error");
        } catch (ExceptionInInitializerError ignored) {
        }
        DatabaseManager.setDefaultConnectionString(TestUtils.DB_CONNECTION_STRING);
    }

    @Test
    public void testGetFirstFromSessionQueryNamed() throws SQLException {
        TestUtils.emptyDictionary();
        Noun n = new Noun("base", "plural");
        n.save();

        HashMap<String, Object> params = new HashMap<>();
        params.put("word", "base");

        Object obj = DatabaseManager.getFirstFromSessionQueryNamed("FROM Noun WHERE base = :word", params);

        assertTrue(obj instanceof Noun);
        Noun n2 = (Noun) obj;
        assertEquals(n, n2);
    }

    @Test
    public void testGetFirstFromSessionQuery() throws SQLException {
        TestUtils.emptyDictionary();
        Noun n = new Noun("base", "plural");
        n.save();

        Object obj = DatabaseManager.getFirstFromSessionQuery("FROM Noun WHERE base = ?0", n.getBase());

        assertTrue(obj instanceof Noun);
        Noun n2 = (Noun) obj;
        assertEquals(n, n2);
    }

    @Test
    public void testGetFirstFromSessionQueryError() throws SQLException {
        TestUtils.emptyDictionary();
        Noun n = new Noun("base", "plural");
        n.save();

        Object obj = DatabaseManager.getFirstFromSessionQuery("FROM Nound WHERE base = ?0", n.getBase());

        assertNull(obj);
    }

    @Test
    public void testGetFirstFromSessionQueryError2() throws SQLException, IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
        TestUtils.emptyDictionary();

        Noun n = new Noun("base", "plural");
        n.save();
		
		Field sessionFactory = DatabaseManager.class.getDeclaredField("sessionFactory");
		SessionFactory tmp = DatabaseManager.getSessionFactory();
		sessionFactory.setAccessible(true);
		sessionFactory.set(DatabaseManager.class, null);

        Object obj = DatabaseManager.getFirstFromSessionQuery("FROM Noun WHERE base = ?0", n.getBase());

		assertNull(obj);

        sessionFactory.set(DatabaseManager.class, tmp);
    }

    @Test
    public void testGetFirstFromSessionQueryNoResult() throws SQLException {
        TestUtils.emptyDictionary();

        Noun n = new Noun("base", "plural");
        n.save();

        Object obj = DatabaseManager.getFirstFromSessionQuery("FROM Noun WHERE base = ?0", n.getBase() + 1);

        assertNull(obj);
    }

    @Test
    public void testGetRowsFromSessionQueryNamed() throws SQLException {
        TestUtils.emptyDictionary();

        Noun n = new Noun("base", "plural");
        n.save();

		HashMap<String, Object> params = new HashMap<>();
        params.put("word", n.getBase());

        List<Object> lst = DatabaseManager.getRowsFromSessionQueryNamed("FROM Noun WHERE base = :word", params);

		assertEquals(1, lst.size());
        assertTrue(lst.get(0) instanceof Noun);
        Noun n2 = (Noun) lst.get(0);
        assertEquals(n, n2);
    }

    @Test
    public void testGetRowsFromSessionQuery() throws SQLException {
        TestUtils.emptyDictionary();

        Noun n = new Noun("base", "plural");
        n.save();

        List<Object> lst = DatabaseManager.getRowsFromSessionQuery("FROM Noun WHERE base = ?0", n.getBase());

		assertEquals(1, lst.size());
        assertTrue(lst.get(0) instanceof Noun);
        Noun n2 = (Noun) lst.get(0);
        assertEquals(n, n2);
    }

    @Test
    public void testGetRowsFromSessionQueryError() throws SQLException {
        TestUtils.emptyDictionary();

        Noun n = new Noun("base", "plural");
        n.save();

        List<Object> lst = DatabaseManager.getRowsFromSessionQuery("FROM Nound WHERE base = ?0", n.getBase());

        assertEquals(0, lst.size());
    }

    @Test
    public void testGetRowsFromSessionQueryError2() throws SQLException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
        TestUtils.emptyDictionary();

        Noun n = new Noun("base", "plural");
        n.save();
		
		Field sessionFactory = DatabaseManager.class.getDeclaredField("sessionFactory");
		SessionFactory tmp = DatabaseManager.getSessionFactory();
		sessionFactory.setAccessible(true);
		sessionFactory.set(DatabaseManager.class, null);

        List<Object> lst = DatabaseManager.getRowsFromSessionQuery("FROM Noun WHERE base = ?0", n.getBase());

		assertEquals(0, lst.size());

        sessionFactory.set(DatabaseManager.class, tmp);
    }

    @Test
    public void testGetRowsFromSessionQueryNotFound() throws SQLException {
        TestUtils.emptyDictionary();

        Noun n = new Noun("base", "plural");
        n.save();

        List<Object> lst = DatabaseManager.getRowsFromSessionQuery("FROM Noun WHERE base = ?0", n.getBase() + 1);

        assertEquals(0, lst.size());
    }

}
