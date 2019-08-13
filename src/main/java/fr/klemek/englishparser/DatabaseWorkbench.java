package fr.klemek.englishparser;

import fr.klemek.englishparser.utils.FileUtils;
import fr.klemek.englishparser.utils.StringUtils;
import fr.klemek.englishparser.utils.Utils;
import fr.klemek.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class DatabaseWorkbench {

    private static final String TABLE_CREATE = "" +
            "CREATE TABLE `wb_test` (" +
            "`num` DECIMAL(10,0)," +
            "`txt` VARCHAR(255)" +
            ");";

    private static final String TABLE_INSERT = "" +
            "INSERT INTO `wb_test` VALUES (?, ?);";

    private static final String TABLE_UPDATE = "" +
            "UPDATE `wb_test` SET `txt` = ? WHERE `num` = ?;";

    private static final String TABLE_QUERY = "" +
            "SELECT * FROM `wb_test` WHERE `num` = ?;";

    private static final String TABLE_INDEX = "" +
            "CREATE INDEX `IDX_Test` ON `wb_test`(`num`);";

    private static final String TABLE_EMPTY = "" +
            "DELETE FROM `wb_test` WHERE 1;";

    private static final String TABLE_DROP = "" +
            "DROP TABLE IF EXISTS `wb_test`;";

    public static void main(String... args) {
        Logger.init("logging.properties");

        int max = 1000;

        if (args.length < 1) {
            Logger.log("First argument must be credentials file");
            return;
        }

        if (args.length > 1) {
            Integer tmpmax = StringUtils.stringToInteger(args[1]);
            if (tmpmax != null)
                max = tmpmax;
        }

        List<String[]> connectionStrings = new ArrayList<>();

        try (BufferedReader reader = FileUtils.readFile(args[0])) {
            String line;
            String[] spl;
            while ((line = reader.readLine()) != null) {
                spl = line.trim().split(";");
                if (spl.length >= 3)
                    connectionStrings.add(spl);
            }
        } catch (IOException e) {
            Logger.log(e);
            return;
        }

        long t0;
        long t1;
        for (String[] connectionString : connectionStrings) {
            Logger.log("Testing with database at {0} with user {1}",
                    connectionString[0].split("mysql://")[1].split(":")[0],
                    connectionString[1]);
            t0 = System.currentTimeMillis();
            t1 = System.currentTimeMillis();
            try (Connection conn = DriverManager.getConnection(connectionString[0], connectionString[1], connectionString[2])) {
                Logger.log("\tConnected in {0}", Utils.getTimeSpent(t1));

                try (Statement st = conn.createStatement()) {
                    st.execute(TABLE_DROP);
                }

                t1 = System.currentTimeMillis();
                try (Statement st = conn.createStatement()) {
                    st.execute(TABLE_CREATE);
                }
                Logger.log("\tTable created in {0}", Utils.getTimeSpent(t1));

                t1 = System.currentTimeMillis();
                for (int n = 0; n < max; n++) {
                    try (PreparedStatement st = conn.prepareStatement(TABLE_INSERT)) {
                        st.setInt(1, n);
                        st.setString(2, Integer.toString(n));
                        st.executeUpdate();
                    }
                }
                Logger.log("\t{1} insertions in {0}", Utils.getTimeSpent(t1), max);

                t1 = System.currentTimeMillis();
                for (int n = 0; n < max; n++) {
                    try (PreparedStatement st = conn.prepareStatement(TABLE_QUERY)) {
                        st.setInt(1, n);
                        st.executeQuery().close();
                    }
                }
                Logger.log("\t{1} queries in {0}", Utils.getTimeSpent(t1), max);

                t1 = System.currentTimeMillis();
                try (Statement st = conn.createStatement()) {
                    st.execute(TABLE_INDEX);
                }
                Logger.log("\tIndex created in {0}", Utils.getTimeSpent(t1));

                t1 = System.currentTimeMillis();
                for (int n = 0; n < max; n++) {
                    try (PreparedStatement st = conn.prepareStatement(TABLE_QUERY)) {
                        st.setInt(1, n);
                        st.executeQuery().close();
                    }
                }
                Logger.log("\t{1} indexed queries in {0}", Utils.getTimeSpent(t1), max);

                t1 = System.currentTimeMillis();
                for (int n = 0; n < max; n++) {
                    try (PreparedStatement st = conn.prepareStatement(TABLE_UPDATE)) {
                        st.setInt(1, n);
                        st.setString(2, Integer.toString(n * 2));
                        st.executeUpdate();
                    }
                }
                Logger.log("\t{1} updates in {0}", Utils.getTimeSpent(t1), max);

                t1 = System.currentTimeMillis();
                try (Statement st = conn.createStatement()) {
                    st.execute(TABLE_EMPTY);
                }
                Logger.log("\tTable emptied in {0}", Utils.getTimeSpent(t1));

                t1 = System.currentTimeMillis();
                try (PreparedStatement st = conn.prepareStatement(TABLE_INSERT)) {
                    for (int n = 0; n < max; n++) {
                        st.setInt(1, n);
                        st.setString(2, Integer.toString(n));
                        st.addBatch();
                    }
                    st.executeBatch();
                }
                Logger.log("\tbatch insert of {1} rows in {0}", Utils.getTimeSpent(t1), max);

                t1 = System.currentTimeMillis();
                try (PreparedStatement st = conn.prepareStatement(TABLE_UPDATE)) {
                    for (int n = 0; n < max; n++) {
                        st.setInt(1, n);
                        st.setString(2, Integer.toString(n * 2));
                        st.addBatch();
                    }
                    st.executeBatch();
                }
                Logger.log("\tbatch update of {1} rows in {0}", Utils.getTimeSpent(t1), max);

                t1 = System.currentTimeMillis();
                try (Statement st = conn.createStatement()) {
                    st.execute(TABLE_DROP);
                }
                Logger.log("\tTable dropped in {0}", Utils.getTimeSpent(t1));

                Logger.log("Finished in {0}", Utils.getTimeSpent(t0));
            } catch (SQLException e) {
                Logger.log(e);
            }
        }
    }

}
