using System;
using System.Collections.Generic;
using System.Data.Common;
using System.Data.Entity.Core.EntityClient;
using System.Linq;
using System.Reflection;
using System.Text.RegularExpressions;
using EnglishParser.Utils;
using MySql.Data.MySqlClient;
using Nini.Config;

namespace EnglishParser.DB
{
    public static class DatabaseManager
    {
        public static bool Initialized { get; private set; }
        public static bool DictInitialized { get; private set; }
        public static DatabaseEntities Entities { get; private set; }
        private static IConfig _config;
        private static bool _verbose;

        #region Connect

        public static MySqlConnection Connect(bool admin = false)
        {
            MySqlConnection conn = new MySqlConnection(BuildConnectionString(admin));
            conn.Open();
            return conn;
        }

        private static string BuildConnectionString(bool admin = false)
        {
            MySqlConnectionStringBuilder builder = new MySqlConnectionStringBuilder()
            {
                Server = _config.GetString("Host"),
                Port = (uint) _config.GetInt("Port"),
                Database = _config.GetString("Database"),
                UserID = admin ? _config.GetString("SuperUser") : _config.GetString("User"),
                Password = admin ? _config.GetString("SuperPassword") : _config.GetString("Password"),
            };
            return builder.ConnectionString;
        }

        #endregion

        #region InitAndUpgrade

        public static void Init(IConfig config)
        {
            _verbose = config.GetBoolean("Verbose");
            if(_verbose) Console.Out.WriteLine("Initializing database...");
            long t0 = TimeUtils.Now();
            _config = config;
            if(_verbose) Console.Error.WriteLine("Connecting successful with DB user \"{0}\"...", _config.GetString("User"));
            Connect();
            if(_verbose) Console.Error.WriteLine("Connecting successful with DB super user \"{0}\"...",
                _config.GetString("SuperUser"));
            Connect(true);
            Entities = new DatabaseEntities(Connect(true));
            UpgradeDatabase();
            Initialized = true;
            if(_verbose) Console.Out.WriteLine("Database initialized in {0}", TimeUtils.GetTimeSpent(t0));
        }

        public static void UpgradeDatabase()
        {
            int version = _config.GetInt("Version");
            int currentVersion = -1;

            long t0 = TimeUtils.Now();

            using (MySqlConnection conn = Connect(true))
            {
                if (!TableExists(conn, "db_info"))
                {
                    if(_verbose) Console.Out.WriteLine("\tNo information on database, assuming empty");
                }
                else
                {
                    QuerySql(conn, "SELECT * FROM db_info", reader =>
                    {
                        if (!reader.HasRows)
                        {
                            if(_verbose) Console.Out.WriteLine("\tNo information on database, assuming empty");
                        }
                        else
                        {
                            reader.Read();
                            currentVersion = reader.GetInt16("version");
                            DateTime lastUpdate = reader.GetDateTime("update_date");
                            DictInitialized = reader.GetInt16("dict_init") == 1;
                            if(_verbose) Console.Out.WriteLine("\tDatabase v{0} last updated: {1}", currentVersion, lastUpdate);
                        }
                    });
                }

                while (currentVersion < version)
                    UpgradeDatabaseToVersion(conn, ++currentVersion);

                if(_verbose) Console.Out.WriteLine("\tDatabase up to date in {0}", TimeUtils.GetTimeSpent(t0));
            }
        }

        private static void UpgradeDatabaseToVersion(MySqlConnection conn, int version)
        {
            string filePath;
            if (version == 0)
            {
                if(_verbose) Console.Out.WriteLine("\tCleaning database...");
                filePath = "sql/clean.sql";
            }
            else
            {
                if(_verbose) Console.Out.WriteLine("\tUpgrading to v{0}...", version);
                filePath = $"sql/v{version}.sql";
            }

            List<string> startTables = ListTables(conn);

            MySqlTransaction transaction = conn.BeginTransaction();

            try
            {
                ImportSql(conn, filePath);
                if (version > 0)
                {
                    ExecSql(conn, "UPDATE db_info SET update_date = CURRENT_TIMESTAMP(), version = @version WHERE 1",
                        ("@version", version));
                }

                transaction.Commit();
            }
            catch (MySqlException)
            {
                transaction.Rollback();
                throw;
            }

            List<string> endTables = ListTables(conn);

            if (_verbose)
            {
                foreach (string table in startTables.Where(t => !endTables.Contains(t)))
                    Console.Out.WriteLine("\t\t(-) table {0}", table);
                foreach (string table in endTables.Where(t => !startTables.Contains(t)))
                    Console.Out.WriteLine("\t\t(+) table {0}", table);
            }
        }

        #endregion

        #region SQLCommand

        public static void ExecSql(MySqlConnection conn, string command, params ValueTuple<string, object>[] args)
        {
            using (MySqlCommand cmd = new MySqlCommand(command, conn))
            {
                foreach (ValueTuple<string, object> param in args)
                    cmd.Parameters.AddWithValue(param.Item1, param.Item2);
                cmd.ExecuteNonQuery();
            }
        }

        public static void QuerySql(MySqlConnection conn, string command,
            Action<MySqlDataReader> action, params ValueTuple<string, object>[] args)
        {
            using (MySqlCommand cmd = new MySqlCommand(command, conn))
            {
                foreach (ValueTuple<string, object> param in args)
                    cmd.Parameters.AddWithValue(param.Item1, param.Item2);
                using (MySqlDataReader reader = cmd.ExecuteReader())
                    action(reader);
            }
        }

        public static void ImportSql(MySqlConnection conn, string resourceName)
        {
            ImportSql(Assembly.GetCallingAssembly(), conn, resourceName);
        }

        public static void ImportSql(Assembly assembly, MySqlConnection conn, string filePath)
        {
            char delimiter = ';';
            string buffer = "";
            Regex rx = new Regex(@"@(\w+)");
            FileUtils.ReadResource(assembly, filePath, line =>
            {
                if (line.Length > 0 && !line.StartsWith("/*"))
                {
                    line = line.Replace("\\\"", "\"");
                    if (line[0] == '@')
                        ImportSql(assembly, conn, line.Substring(1));
                    else if (line.ToUpper().StartsWith("DELIMITER"))
                    {
                        delimiter = line.Split(" ")[1][0];
                    }
                    else if (line[line.Length - 1] == delimiter)
                    {
                        buffer += line.Substring(0, line.Length - 1);
                        foreach (Match match in rx.Matches(buffer).Reverse())
                            buffer = buffer.Substring(0, match.Groups[1].Index) + "`" + match.Groups[1].Value + "`" +
                                     buffer.Substring(match.Groups[1].Index + match.Groups[1].Length);
                        ExecSql(conn, buffer);
                        buffer = "";
                    }
                    else
                    {
                        buffer += line + "\n";
                    }
                }
            });
        }

        #endregion

        #region Utils

        public static bool TableExists(MySqlConnection conn, String name)
        {
            bool exists = false;
            QuerySql(conn, "SHOW TABLES LIKE @name", reader => { exists = reader.HasRows; }, ("@name", name));
            return exists;
        }

        public static List<string> ListTables(MySqlConnection conn)
        {
            List<string> tables = new List<string>();
            QuerySql(conn, "SHOW TABLES", reader =>
            {
                while (reader.Read())
                    tables.Add(reader.GetString(0));
            });
            return tables;
        }

        #endregion
    }
}