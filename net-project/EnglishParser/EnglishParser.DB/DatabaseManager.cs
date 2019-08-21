using System;
using System.Data.Entity.Core.EntityClient;
using System.Resources;
using Nini.Config;
using MySql.Data.MySqlClient;
using EnglishParser.Utils;

namespace EnglishParser.DB
{
    public static class DatabaseManager
    {
        public static bool Initialized { get; private set; }
        private static DatabaseEntities Db;
        private static IConfig Config;

        public static void Init(IConfig config)
        {
            if(Initialized)
                return;
            Console.Out.WriteLine("Initializing database...");
            long t0 = TimeUtils.Now();
            Config = config;
            try
            {
                Connect();
                Console.Error.WriteLine("Connection successful with DB user \"{0}\"",Config.GetString("User"));
            }
            catch(MySqlException e)
            {
                throw new Exception("Cannot connect with DB user: "+Config.GetString("User"), e);
            }
            try
            {
                Connect(true);
                Console.Error.WriteLine("Connection successful with DB super user \"{0}\"",Config.GetString("SuperUser"));
            }
            catch(MySqlException e)
            {
                throw new Exception("Cannot connect with DB super user: "+Config.GetString("SuperUser"), e);
            }
            Db = new DatabaseEntities(BuildEntityConnectionString());
            UpdateDatabase();
            Initialized = true;
            Console.Out.WriteLine("Database initialized in {0}", TimeUtils.GetTimeSpent(t0));
        }

        private static void UpdateDatabase()
        {
            int version = Config.GetInt("Version");
            int currentVersion = 0;

            long t0 = TimeUtils.Now();

            //TODO
        }

        private static string BuildConnectionString(bool admin = false)
        {
            MySqlConnectionStringBuilder builder = new MySqlConnectionStringBuilder()
            {
                Server = Config.GetString("Host"),
                Port = (uint) Config.GetInt("Port"),
                Database = Config.GetString("Database"),
                UserID = admin ? Config.GetString("SuperUser") : Config.GetString("User"),
                Password = admin ? Config.GetString("SuperPassword") : Config.GetString("Password"),
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

        private static MySqlConnection Connect(bool admin = false)
        {
            MySqlConnection conn = new MySqlConnection(BuildConnectionString(admin));
            conn.Open();
            return conn;
        }
    }
}