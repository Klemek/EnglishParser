using System;
using System.Collections.Generic;
using System.Linq;
using EnglishParser.DB;
using EnglishParser.Utils;
using MySql.Data.MySqlClient;
using Nini.Config;

namespace EnglishParser.Core
{
    public static class DictionaryManager
    {
        public static bool Initialized { get; private set; }
        private static IConfig _config;
        private static bool _verbose;

        public static void Init(IConfig config)
        {
            _config = config;
            _verbose = config.GetBoolean("Verbose");
            if (!DatabaseManager.Initialized)
                throw new Exception("Database is not initialized");
            Initialized = DatabaseManager.DictInitialized;
            if (_verbose) Console.Out.WriteLine("Dictionary {0}initialized", Initialized ? "" : "not ");
            if (!Initialized)
            {
                if (_verbose) Console.Out.WriteLine("Initializing dictionary...");
                long t0 = TimeUtils.Now();
                long t1;
                using (MySqlConnection conn = DatabaseManager.Connect(true))
                {
                    t1 = TimeUtils.Now();
                    EmptyDictionary(conn);
                    if (_verbose) Console.Out.WriteLine("\tEmptied dictionary in {0}", TimeUtils.GetTimeSpent(t1));

                    if (config.GetBoolean("PreComputed"))
                    {
                        if (_verbose) Console.Out.WriteLine("\tImporting pre-computed data...");
                        t1 = TimeUtils.Now();
                        DatabaseManager.ImportSql(conn, "dict/sql/ep_fill.sql");
                        if (_verbose) Console.Out.WriteLine("\tImported pre-computed data in {0}", TimeUtils.GetTimeSpent(t1));
                    }
                    
                    if (_verbose) Console.Out.WriteLine("\t+ {0} Words", DatabaseManager.Entities.Words.Count());
                    //DatabaseManager.ExecSql(conn, "UPDATE db_info SET dict_init = 1 WHERE 1");
                }

                Initialized = true;
                if (_verbose) Console.Out.WriteLine("Dictionary initialized in {0}", TimeUtils.GetTimeSpent(t0));
            }
        }

        private static void EmptyDictionary(MySqlConnection conn)
        {
            List<string> tables = new List<string> {"dict_def", "dict_noun", "dict_verb", "dict_adj", "dict_word"};
            DatabaseManager.ExecSql(conn, String.Join("", tables.Select(name => $"DELETE FROM {name} WHERE 1;")));
        }
    }
}