using System;
using System.Collections.Generic;
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
        private static IConfig _config;
        private static bool _verbose;
        public static bool Initialized { get; private set; }
        public static bool DictInitialized { get; private set; }
        public static DatabaseEntities Entities { get; private set; }

        #region Connect

        public static MySqlConnection Connect(bool admin = false)
        {
            var conn = new MySqlConnection(BuildConnectionString(admin));
            conn.Open();
            return conn;
        }

        private static string BuildConnectionString(bool admin = false)
        {
            var builder = new MySqlConnectionStringBuilder
            {
                Server = _config.GetString("Host"),
                Port = (uint) _config.GetInt("Port"),
                Database = _config.GetString("Database"),
                UserID = admin ? _config.GetString("SuperUser") : _config.GetString("User"),
                Password = admin ? _config.GetString("SuperPassword") : _config.GetString("Password")
            };
            return builder.ConnectionString;
        }

        #endregion

        #region InitAndUpgrade

        public static void Init(IConfig config)
        {
            _verbose = config.GetBoolean("Verbose");
            if (_verbose) Console.Out.WriteLine("Initializing database...");
            var t0 = TimeUtils.Now();
            _config = config;
            if (_verbose)
                Console.Error.WriteLine("Connecting successful with DB user \"{0}\"...", _config.GetString("User"));
            Connect();
            if (_verbose)
                Console.Error.WriteLine("Connecting successful with DB super user \"{0}\"...",
                    _config.GetString("SuperUser"));
            Connect(true);
            Entities = new DatabaseEntities(BuildConnectionString(true));
            UpgradeDatabase();
            Initialized = true;
            if (_verbose) Console.Out.WriteLine("Database initialized in {0}", TimeUtils.GetTimeSpent(t0));
        }

        public static void UpgradeDatabase()
        {
            var version = _config.GetInt("Version");
            var currentVersion = -1;

            var t0 = TimeUtils.Now();

            using (var conn = Connect(true))
            {
                if (!TableExists(conn, "db_info"))
                {
                    if (_verbose) Console.Out.WriteLine("\tNo information on database, assuming empty");
                }
                else
                {
                    QuerySql(conn, "SELECT * FROM db_info", reader =>
                    {
                        if (!reader.HasRows)
                        {
                            if (_verbose) Console.Out.WriteLine("\tNo information on database, assuming empty");
                        }
                        else
                        {
                            reader.Read();
                            currentVersion = reader.GetInt16("version");
                            var lastUpdate = reader.GetDateTime("update_date");
                            DictInitialized = reader.GetInt16("dict_init") == 1;
                            if (_verbose)
                                Console.Out.WriteLine("\tDatabase v{0} last updated: {1}", currentVersion, lastUpdate);
                        }
                    });
                }

                while (currentVersion < version)
                    UpgradeDatabaseToVersion(conn, ++currentVersion);

                if (_verbose) Console.Out.WriteLine("\tDatabase up to date in {0}", TimeUtils.GetTimeSpent(t0));
            }
        }

        private static void UpgradeDatabaseToVersion(MySqlConnection conn, int version)
        {
            string filePath;
            if (version == 0)
            {
                if (_verbose) Console.Out.WriteLine("\tCleaning database...");
                filePath = "sql/clean.sql";
            }
            else
            {
                if (_verbose) Console.Out.WriteLine("\tUpgrading to v{0}...", version);
                filePath = $"sql/v{version}.sql";
            }

            var startTables = ListTables(conn);

            var transaction = conn.BeginTransaction();

            try
            {
                ImportSql(conn, filePath);
                if (version > 0)
                    ExecSql(conn, "UPDATE db_info SET update_date = CURRENT_TIMESTAMP(), version = @version WHERE 1",
                        ("@version", version));

                transaction.Commit();
            }
            catch (MySqlException)
            {
                transaction.Rollback();
                throw;
            }

            var endTables = ListTables(conn);

            if (_verbose)
            {
                foreach (var table in startTables.Where(t => !endTables.Contains(t)))
                    Console.Out.WriteLine("\t\t(-) table {0}", table);
                foreach (var table in endTables.Where(t => !startTables.Contains(t)))
                    Console.Out.WriteLine("\t\t(+) table {0}", table);
            }
        }

        #endregion

        #region SQLCommand

        public static void ExecSql(MySqlConnection conn, string command, params ValueTuple<string, object>[] args)
        {
            using (var cmd = new MySqlCommand(command, conn))
            {
                foreach (var param in args)
                    cmd.Parameters.AddWithValue(param.Item1, param.Item2);
                cmd.ExecuteNonQuery();
            }
        }

        public static void QuerySql(MySqlConnection conn, string command,
            Action<MySqlDataReader> action, params ValueTuple<string, object>[] args)
        {
            using (var cmd = new MySqlCommand(command, conn))
            {
                foreach (var param in args)
                    cmd.Parameters.AddWithValue(param.Item1, param.Item2);
                using (var reader = cmd.ExecuteReader())
                {
                    action(reader);
                }
            }
        }

        public static void ImportSql(MySqlConnection conn, string resourceName)
        {
            ImportSql(Assembly.GetCallingAssembly(), conn, resourceName);
        }

        public static void ImportSql(Assembly assembly, MySqlConnection conn, string filePath)
        {
            var delimiter = ';';
            var buffer = "";
            var rx = new Regex(@"@(\w+)");
            FileUtils.ReadResource(assembly, filePath, line =>
            {
                if (line.Length > 0 && !line.StartsWith("/*"))
                {
                    line = line.Replace("\\\"", "\"");
                    if (line[0] == '@')
                    {
                        ImportSql(assembly, conn, line.Substring(1));
                    }
                    else if (line.ToUpper().StartsWith("DELIMITER"))
                    {
                        delimiter = line.Split(" ")[1][0];
                    }
                    else if (line[line.Length - 1] == delimiter)
                    {
                        buffer += line.Substring(0, line.Length - 1);
                        foreach (var match in rx.Matches(buffer).Reverse())
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

        public static bool TableExists(MySqlConnection conn, string name)
        {
            var exists = false;
            QuerySql(conn, "SHOW TABLES LIKE @name", reader => { exists = reader.HasRows; }, ("@name", name));
            return exists;
        }

        public static List<string> ListTables(MySqlConnection conn)
        {
            var tables = new List<string>();
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