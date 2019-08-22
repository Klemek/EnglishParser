using System;
using System.Collections.Generic;
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
        private static DatabaseEntities _db;
        private static IConfig _config;

        public static void Init(IConfig config)
        {
            if (Initialized)
                return;
            Console.Out.WriteLine("Initializing database...");
            long t0 = TimeUtils.Now();
            _config = config;
            Console.Error.WriteLine("Connecting successful with DB user \"{0}\"...", _config.GetString("User"));
            Connect();
            Console.Error.WriteLine("Connecting successful with DB super user \"{0}\"...",
                _config.GetString("SuperUser"));
            Connect(true);
            _db = new DatabaseEntities(BuildEntityConnectionString());
            UpdateDatabase();
            Initialized = true;
            Console.Out.WriteLine("Database initialized in {0}", TimeUtils.GetTimeSpent(t0));
        }

        private static void UpdateDatabase()
        {
            int version = _config.GetInt("Version");
            int currentVersion = -1;

            long t0 = TimeUtils.Now();

            using (MySqlConnection conn = Connect(true))
            {
                if (!TableExists(conn, "db_info"))
                {
                    Console.Out.WriteLine("\tNo information on database, assuming empty");
                }
                else
                {
                    using (MySqlCommand cmd = new MySqlCommand("SELECT * FROM db_info", conn))
                    using (MySqlDataReader reader = cmd.ExecuteReader())
                    {
                        if (!reader.HasRows)
                        {
                            Console.Out.WriteLine("\tNo information on database, assuming empty");
                        }
                        else
                        {
                            reader.Read();
                            currentVersion = reader.GetInt16("version");
                            DateTime lastUpdate = reader.GetDateTime("update_date");
                            //DictionaryManager.Initialized = reader.GetInt16("dict_init") == 1;
                            Console.Out.WriteLine("\tDatabase v{0} last updated: {1}", currentVersion, lastUpdate);
                        }
                    }
                }

                while (currentVersion < version)
                    UpdateDatabaseToVersion(conn, ++currentVersion);

                Console.Out.WriteLine("\tDatabase up to date in {0}", TimeUtils.GetTimeSpent(t0));
            }
        }

        private static void UpdateDatabaseToVersion(MySqlConnection conn, int version)
        {
            string filePath;
            if (version == 0)
            {
                Console.Out.WriteLine("\tCleaning database...");
                filePath = "sql/clean.sql";
            }
            else
            {
                Console.Out.WriteLine("\tUpgrading to v{0}...", version);
                filePath = $"sql/v{version}.sql";
            }

            List<string> startTables = ListTables(conn);

            MySqlTransaction transaction = conn.BeginTransaction();

            try
            {
                ImportSql(conn, filePath);
                if (version > 0)
                {
                    using (MySqlCommand cmd = new MySqlCommand(
                        "UPDATE db_info SET update_date = CURRENT_TIMESTAMP(), version = @version WHERE 1", conn))
                    {
                        cmd.Parameters.AddWithValue("@version", version);
                        cmd.ExecuteNonQuery();
                    }
                }

                transaction.Commit();
            }
            catch (MySqlException)
            {
                transaction.Rollback();
                throw;
            }

            List<string> endTables = ListTables(conn);

            foreach (string table in startTables.Where(t => !endTables.Contains(t)))
                Console.Out.WriteLine("\t\t(-) table {0}", table);
            foreach (string table in endTables.Where(t => !startTables.Contains(t)))
                Console.Out.WriteLine("\t\t(+) table {0}", table);
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
                        using (MySqlCommand cmd = new MySqlCommand(buffer, conn))
                            cmd.ExecuteNonQuery();
                        buffer = "";
                    }
                    else
                    {
                        buffer += line + "\n";
                    }
                }
            });
        }

        public static bool TableExists(MySqlConnection conn, String name)
        {
            using (MySqlCommand cmd = new MySqlCommand("SHOW TABLES LIKE @name", conn))
            {
                cmd.Parameters.AddWithValue("@name", name);
                using (MySqlDataReader reader = cmd.ExecuteReader())
                {
                    return reader.HasRows;
                }
            }
        }

        public static List<string> ListTables(MySqlConnection conn)
        {
            using (MySqlCommand cmd = new MySqlCommand("SHOW TABLES", conn))
            using (MySqlDataReader reader = cmd.ExecuteReader())
            {
                List<string> tables = new List<string>();
                while (reader.Read())
                    tables.Add(reader.GetString(0));
                return tables;
            }
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

        private static string BuildEntityConnectionString(bool admin = false)
        {
            EntityConnectionStringBuilder builder = new EntityConnectionStringBuilder()
            {
                Provider = "System.Data.SqlClient",
                ProviderConnectionString = BuildConnectionString(admin)
            };
            return builder.ConnectionString;
        }

        public static MySqlConnection Connect(bool admin = false)
        {
            MySqlConnection conn = new MySqlConnection(BuildConnectionString(admin));
            conn.Open();
            return conn;
        }
    }
}