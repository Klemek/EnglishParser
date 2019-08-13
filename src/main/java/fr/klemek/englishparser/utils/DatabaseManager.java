package fr.klemek.englishparser.utils;

import fr.klemek.englishparser.model.DatabaseObject;
import fr.klemek.englishparser.model.dict.*;
import fr.klemek.logger.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.logging.Level;

/**
 * Utility class that store useful database functions.
 */
public final class DatabaseManager {

    private static final String DB_USER = "db_user";
    private static String defaultConnectionString = null;
    private static SessionFactory sessionFactory;
    private static boolean databaseUpToDate = false;

    private DatabaseManager() {
    }

    /**
     * Init the driver, default connection string and update database.
     *
     * @param defaultConnectionString the connectionString to set as default
     * @return true if the operation is successful
     */
    public static boolean init(String defaultConnectionString) {
        Logger.log("Initializing database...");
        long t0 = System.currentTimeMillis();
        DatabaseManager.checkDriver(false);
        DatabaseManager.setDefaultConnectionString(defaultConnectionString);
        try (Connection conn = openConnection()) {
            Logger.log("\tConnection successful with DB user : {0}", Config.getString(DB_USER));
        } catch (SQLException e) {
            Logger.log(e, "Cannot connect with DB user");
            return false;
        }
        try (Connection conn = openConnection(true)) {
            Logger.log("\tConnection successful with DB super user : {0}", Config.getString("db_super_user"));
        } catch (SQLException e) {
            Logger.log(e, "Cannot connect with DB super user");
            return false;
        }
        if (sessionFactory == null)
            DatabaseManager.buildSessionFactory(defaultConnectionString);
        if (!DatabaseManager.updateDatabase())
            return false;
        Logger.log("Database ready in {0}", Utils.getTimeSpent(t0));
        return true;
    }

    /**
     * Change the connection string used by default in functions.
     *
     * @param defaultConnectionString the connectionString to set as default
     */
    static void setDefaultConnectionString(String defaultConnectionString) {
        DatabaseManager.defaultConnectionString = defaultConnectionString;
    }

    /**
     * Register or unregister the mysql jdbc driver.
     *
     * @param unregister change mode
     */
    public static void checkDriver(boolean unregister) {
        try {
            Enumeration<Driver> loadedDrivers = DriverManager.getDrivers();
            while (loadedDrivers.hasMoreElements()) {
                Driver driver = loadedDrivers.nextElement();
                if (driver instanceof com.mysql.cj.jdbc.Driver) {
                    Logger.log("\tDriver registered");
                    if (unregister) {
                        DriverManager.deregisterDriver(driver);
                    }
                    return;
                }
            }
            Logger.log("\tDriver not registered");
            if (!unregister) {
                DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            }
        } catch (SQLException e) {
            Logger.log(e, "Exception while checking driver");
        }

    }

    public static void setDictionaryInitialized(Connection conn, boolean initialized) throws SQLException {
        try (PreparedStatement st = conn.prepareStatement("UPDATE db_info SET dict_init = ? WHERE 1")) {
            st.setString(1, initialized ? "1" : "0");
            st.executeUpdate();
        }
    }

    /**
     * Open a new connection to the default database.
     *
     * @return a Connection object to make queries
     * @throws SQLException if cannot open connection
     */
    public static Connection openConnection() throws SQLException {
        return openConnection(false);
    }

    /**
     * Open a new connection to the default database.
     *
     * @param superuser if the user can modify the structure of the database
     * @return a Connection object to make queries
     * @throws SQLException if cannot open connection
     */
    public static Connection openConnection(boolean superuser) throws SQLException {
        return openConnection(superuser, null);
    }

    /**
     * Open a new connection to the database.
     *
     * @param superuser        if the user can modify the structure of the database
     * @param connectionString the desired database connection string (null for default)
     * @return a Connection object to make queries
     * @throws SQLException if cannot open connection
     */
    private static Connection openConnection(boolean superuser, String connectionString) throws SQLException {
        if (connectionString == null && defaultConnectionString == null)
            throw new ExceptionInInitializerError("Default ConnectionString is null");
        String userName = Config.getString(superuser ? "db_super_user" : DB_USER);
        String password = Config.getString(superuser ? "db_super_password" : "db_password");
        String url = connectionString == null ? defaultConnectionString : connectionString;
        return DriverManager.getConnection(url, userName, password);
    }

    /**
     * Get an hibernate session factory for database manipulation.
     *
     * @return the hibernate session factory
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static boolean isInitialized() {
        return databaseUpToDate && isHibernateInitialized();
    }

    public static boolean isHibernateInitialized() {
        return sessionFactory != null;
    }

    /**
     * Initiate the singleton sessionFactory.
     *
     * @param connectionString the desired connectionString (null for default)
     */
    private static void buildSessionFactory(String connectionString) {
        try {
            Logger.log("\tCreating SessionFactory...");
            long t0 = System.currentTimeMillis();
            Level lvl = Logger.getLevel();
            Logger.setLevel(Level.SEVERE);
            sessionFactory = new Configuration().configure()
                    .setProperty("hibernate.connection.url",
                            connectionString == null ? defaultConnectionString : connectionString)
                    .setProperty("hibernate.connection.username", Config.getString(DB_USER))
                    .setProperty("hibernate.connection.password", Config.getString("db_password"))
                    .addAnnotatedClass(DatabaseObject.class)
                    .addAnnotatedClass(Word.class)
                    .addAnnotatedClass(Definition.class)
                    .addAnnotatedClass(WordObject.class)
                    .addAnnotatedClass(Noun.class)
                    .addAnnotatedClass(Verb.class)
                    .addAnnotatedClass(Adjective.class)
                    .buildSessionFactory();
            Logger.setLevel(lvl);
            Logger.log("\tSessionFactory created in {0}", Utils.getTimeSpent(t0));
        } catch (Exception ex) {
            Logger.log(ex, "Initial SessionFactory creation failed");
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Execute an hibernate query and returns the first element.
     *
     * @param <T>            the type of the hibernate object
     * @param hibernateQuery with unnamed parameters (ex : '?1')
     * @param parameters     the unnamed parameters in order
     * @return the first object or null if not found
     */
    public static <T> T getFirstFromSessionQuery(String hibernateQuery, Object... parameters) {
        return getFirstFromSessionQueryBase(hibernateQuery, null, parameters);
    }

    /**
     * Execute an hibernate query and returns the first element.
     *
     * @param <T>            the type of the hibernate object
     * @param hibernateQuery with named parameters (ex : ':id')
     * @param parameters     named parameters and their values
     * @return the first object or null if not found
     */
    static <T> T getFirstFromSessionQueryNamed(String hibernateQuery, Map<String, Object> parameters) {
        return getFirstFromSessionQueryBase(hibernateQuery, parameters);
    }

    /**
     * Execute an hibernate query and returns the first element.
     *
     * @param <T>            the type of the hibernate object
     * @param hibernateQuery the hibernate query
     * @param parametersMap  named parameters and their values (null for named parameters)
     * @param parameters     the unnamed parameters in order
     * @return the first object or null if not found
     */
    private static <T> T getFirstFromSessionQueryBase(String hibernateQuery, Map<String, Object> parametersMap,
                                                      Object... parameters) {
        if (!isHibernateInitialized()) {
            Logger.log(Level.SEVERE, "Database not initialized, cannot do query");
            return null;
        }
        Session session = getSessionFactory().getCurrentSession();

        Transaction tx = null;
        try {
            if (!session.getTransaction().isActive())
                tx = session.beginTransaction();
            Query query = session.createQuery(hibernateQuery);
            loadQueryParameters(query, parametersMap, parameters);
            @SuppressWarnings("unchecked")
            T result = (T) query.setMaxResults(1).getSingleResult();
            if (tx != null)
                tx.commit();
            return result;
        } catch (NoResultException e) {
            if (tx != null)
                tx.commit();
            return null;
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            Logger.log(Level.WARNING, e);
            return null;
        }
    }

    /**
     * Execute an hibernate query and returns all the rows.
     *
     * @param <T>            the type of the hibernate object
     * @param hibernateQuery with unnamed parameters (ex : '?1')
     * @param parameters     the unnamed parameters in order
     * @return all the returned rows
     */
    public static <T> List<T> getRowsFromSessionQuery(String hibernateQuery, Object... parameters) {
        return getRowsFromSessionQueryBase(hibernateQuery, 0, 0, null, parameters);
    }

    /**
     * Execute an hibernate query and returns all the rows.
     *
     * @param <T>            the type of the hibernate object
     * @param hibernateQuery with named parameters (ex : ':id')
     * @param start          the first result
     * @param max            the max size of the query
     * @param parameters     named parameters and their values
     * @return all the returned rows
     */
    public static <T> List<T> getRowsFromSessionQueryNamed(String hibernateQuery, int start, int max,
                                                           Map<String, Object> parameters) {
        return getRowsFromSessionQueryBase(hibernateQuery, start, max, parameters);
    }

    /**
     * Execute an hibernate query and returns all the rows.
     *
     * @param <T>            the type of the hibernate object
     * @param hibernateQuery with named parameters (ex : ':id')
     * @param parameters     named parameters and their values
     * @return all the returned rows
     */
    static <T> List<T> getRowsFromSessionQueryNamed(String hibernateQuery, Map<String, Object> parameters) {
        return getRowsFromSessionQueryBase(hibernateQuery, 0, 0, parameters);
    }

    /**
     * Execute an hibernate query and returns all the rows.
     *
     * @param <T>            the type of the hibernate object
     * @param hibernateQuery the hibernate query
     * @param start          the first result
     * @param max            the max size of the query
     * @param parametersMap  named parameters and their values (null for named parameters)
     * @param parameters     the unnamed parameters in order
     * @return all the returned rows
     */
    private static <T> List<T> getRowsFromSessionQueryBase(String hibernateQuery, int start, int max,
                                                           Map<String, Object> parametersMap, Object... parameters) {
        if (!isHibernateInitialized()) {
            Logger.log(Level.SEVERE, "Database not initialized, cannot do query");
            return new ArrayList<>();
        }
        Transaction tx = null;
        Session session = getSessionFactory().getCurrentSession();
        try {
            if (!session.getTransaction().isActive())
                tx = session.beginTransaction();
            Query query = session.createQuery(hibernateQuery);
            if (start > 0)
                query.setFirstResult(start);
            if (max > 0)
                query.setMaxResults(max);
            loadQueryParameters(query, parametersMap, parameters);
            @SuppressWarnings("unchecked")
            List<T> rows = (List<T>) query.getResultList();
            if (tx != null)
                tx.commit();
            return rows;
        } catch (NoResultException e) {
            if (tx != null)
                tx.commit();
            return new ArrayList<>(0);
        } catch (Exception e) {
            if (tx != null)
                tx.rollback();
            Logger.log(e);
            return new ArrayList<>(0);
        }
    }

    /**
     * Load parameters into the query.
     *
     * @param query         the hibernate query object
     * @param parametersMap named parameters and their values (null for named parameters)
     * @param parameters    the unnamed parameters in order
     */
    private static void loadQueryParameters(Query query, Map<String, Object> parametersMap, Object... parameters) {
        if (parametersMap != null)
            for (Map.Entry<String, Object> entry : parametersMap.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
        else
            for (int i = 0; i < parameters.length; i++) {
                query.setParameter(i, parameters[i]);
            }
    }

    /**
     * Execute SQL statements from a file.
     *
     * @param conn         the SQL Connection
     * @param resourceName the file name in resources
     * @throws SQLException if sql error in file
     * @throws IOException  if the file is not found
     */
    public static void importSQL(Connection conn, String resourceName) throws IOException, SQLException {
        if (FileUtils.getExtension(resourceName) == null)
            resourceName += ".sql";
        try (InputStream is = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(resourceName)) {
            if (is != null)
                DatabaseManager.importSQL(conn, is);
            else
                throw new IOException("Failed to load file " + resourceName);
        }
    }

    /**
     * Execute SQL statements from a file.
     *
     * @param conn the SQL Connection
     * @param in   the file InputStream
     * @throws SQLException if sql error in file
     */
    private static void importSQL(Connection conn, InputStream in) throws SQLException, IOException {
        StringBuilder buffer = new StringBuilder();
        char delimiter = ';';
        try (Scanner s = new Scanner(in)) {
            try (Statement st = conn.createStatement()) {
                while (s.hasNext()) {
                    String line = s.nextLine().trim();
                    if (line.length() > 0 && !line.startsWith("/*")) {
                        line = line.replace("\\\"", "\"");
                        if (line.charAt(0) == '@')
                            DatabaseManager.importSQL(conn, line.substring(1));
                        else if (line.startsWith("DELIMITER")) {
                            delimiter = line.split(" ")[1].charAt(0);
                        } else if (line.charAt(line.length() - 1) == delimiter) {
                            buffer.append(line, 0, line.length() - 1);
                            if (buffer.toString().trim().length() > 0) {
                                //Logger.log("SQL : {0}", buffer.toString());
                                st.execute(buffer.toString());
                            }
                            buffer = new StringBuilder();
                        } else {
                            buffer.append(line);
                            buffer.append('\n');
                        }

                    }
                }
            }
        }
    }

    static int getRowCount(ResultSet rs) throws SQLException {
        int rowcount = 0;
        if (rs.last()) {
            rowcount = rs.getRow();
            rs.beforeFirst();
        }
        return rowcount;
    }

    /**
     * Check if a table exists in the database.
     *
     * @param conn the SQL Connection
     * @param name the name of the table
     * @return true if it exists
     */
    static boolean tableExists(Connection conn, String name) {
        try (PreparedStatement st = conn.prepareStatement("SHOW TABLES LIKE ?")) {
            st.setString(1, name);
            try (ResultSet rs = st.executeQuery()) {
                return rs.first();
            }
        } catch (SQLException e) {
            Logger.log(e);
            return false;
        }
    }

    private static List<String> listTables(Connection conn) {
        List<String> tables = new ArrayList<>();
        try (Statement st = conn.createStatement()) {
            try (ResultSet rs = st.executeQuery("SHOW TABLES")) {
                while (rs.next())
                    tables.add(rs.getString(1));
            }
        } catch (SQLException e) {
            Logger.log(e);
        }
        return tables;
    }

    /**
     * Update the database.
     *
     * @return true if the operation is successful
     */
    static boolean updateDatabase() {
        return updateDatabase(null);
    }

    /**
     * Update the database.
     *
     * @param connectionString the desired connectionString (null for default)
     * @return true if the operation is successful
     */
    private static boolean updateDatabase(String connectionString) {
        int version = Config.getInt("db_version");
        int currentVersion = 0;

        long t0 = System.currentTimeMillis();

        try (Connection conn = openConnection(true, connectionString)) {
            if (!tableExists(conn, "db_info")) {
                Logger.log("\tNo information on database, assuming empty");
            } else {
                try (Statement st = conn.createStatement()) {
                    try (ResultSet rs = st.executeQuery("SELECT * FROM db_info")) {
                        if (!rs.first()) {
                            Logger.log("\tNo information on database, assuming empty");
                        } else {
                            currentVersion = rs.getInt("version");
                            Date lastUpdate = rs.getTimestamp("update_date");
                            DictionaryManager.setInitialized(rs.getString("dict_init").equals("1"));
                            Logger.log("\tDatabase v{0} last updated : {1}", currentVersion, lastUpdate);
                        }
                    }
                }
            }

            conn.setAutoCommit(false);

            if (currentVersion == 0 && !updateDatabaseToVersion(conn, currentVersion)) {
                Logger.log(Level.SEVERE, "Error updating database");
                return false;
            }
            while (currentVersion < version) {
                currentVersion++;
                if (!updateDatabaseToVersion(conn, currentVersion)) {
                    currentVersion--;
                    break;
                }
            }
            if (currentVersion == version) {
                Logger.log("\tDatabase up to date in {0}", Utils.getTimeSpent(t0));
                databaseUpToDate = true;
                return true;
            } else {
                Logger.log(Level.SEVERE, "Error updating database - bad version : {0} expected, got {1}", version, currentVersion);
                return false;
            }

        } catch (SQLException e) {
            Logger.log(e);
            return false;
        }
    }

    /**
     * Execute an update script on the database.
     *
     * @param conn    the SQLConnection
     * @param version the version of the database or 0 to clean it
     * @return true if the operation is successful
     */
    private static boolean updateDatabaseToVersion(Connection conn, int version) {
        String filePath;
        if (version <= 0) {
            Logger.log("\tCleaning database...");
            filePath = "sql/clean.sql";
        } else {
            Logger.log("\tUpdating to v{0}...", version);
            filePath = "sql/v" + version + ".sql";
        }

        List<String> startTables = DatabaseManager.listTables(conn);

        try {
            // Import SQL file for current version update and set the version in the db
            DatabaseManager.importSQL(conn, filePath);
            if (version > 0) {
                try (PreparedStatement st = conn.prepareStatement(
                        "UPDATE db_info SET update_date = CURRENT_TIMESTAMP(), version = ? WHERE 1")) {
                    st.setInt(1, version);
                    st.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            // In case of error rollback last version update and quit updating version
            Logger.log(e);
            try {
                conn.rollback();
            } catch (SQLException e2) {
                Logger.log(e2, "Error during rollback");
            }
            return false;
        } catch (IOException e) {
            Logger.log(Level.WARNING, "Error reading file {0} : {1}", filePath, e);
        }

        List<String> endTables = listTables(conn);

        for (String table : Utils.compareLists(startTables, endTables))
            Logger.log("\t\t(-) table {0}", table);
        for (String table : Utils.compareLists(endTables, startTables))
            Logger.log("\t\t(+) table {0}", table);

        return true;
    }
}
