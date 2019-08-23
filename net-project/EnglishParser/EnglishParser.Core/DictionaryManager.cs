using System;
using System.Collections.Generic;
using System.Linq;
using EnglishParser.DB;
using EnglishParser.Model;
using EnglishParser.Utils;
using MySql.Data.MySqlClient;
using Nini.Config;

namespace EnglishParser.Core
{
    public static class DictionaryManager
    {
        private static IConfig _config;
        private static bool _verbose;
        private static Dictionary<string, string> _irregularPlurals;
        public static bool Initialized { get; private set; }

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
                var t0 = TimeUtils.Now();
                long t1;
                using (var conn = DatabaseManager.Connect(true))
                {
                    t1 = TimeUtils.Now();
                    EmptyDictionary(conn);
                    if (_verbose) Console.Out.WriteLine("\tEmptied dictionary in {0}", TimeUtils.GetTimeSpent(t1));

                    if (config.GetBoolean("PreComputed"))
                    {
                        if (_verbose) Console.Out.WriteLine("\tImporting pre-computed data...");
                        t1 = TimeUtils.Now();
                        DatabaseManager.ImportSql(conn, "dict/sql/ep_fill.sql");
                        if (_verbose)
                            Console.Out.WriteLine("\tImported pre-computed data in {0}", TimeUtils.GetTimeSpent(t1));
                    }
                    else
                    {
                        t1 = TimeUtils.Now();
                        LoadIrregularPlurals();
                        if (_verbose)
                            Console.Out.WriteLine("\tLoaded {0} irregular plurals in {1}", _irregularPlurals.Count,
                                TimeUtils.GetTimeSpent(t1));
                        
                        //TODO load others irregulars

                        t1 = TimeUtils.Now();
                        DatabaseManager.ImportSql(conn, "dict/sql/wordnet_init.sql");
                        if (_verbose)
                            Console.Out.WriteLine("\tImported wordnet structure in {0}", TimeUtils.GetTimeSpent(t1));

                        t1 = TimeUtils.Now();
                        DatabaseManager.ImportSql(conn, "dict/sql/wordnet_fill.sql");
                        if (_verbose)
                            Console.Out.WriteLine("\tImported wordnet data in {0}", TimeUtils.GetTimeSpent(t1));

                        //TODO word computing
                        
                        t1 = TimeUtils.Now();
                        DatabaseManager.ImportSql(conn, "dict/sql/wordnet_drop.sql");
                        if (_verbose)
                            Console.Out.WriteLine("\tDropped wordnet structure and data in {0}",
                                TimeUtils.GetTimeSpent(t1));
                    }

                    //DatabaseManager.ExecSql(conn, "UPDATE db_info SET dict_init = 1 WHERE 1");
                }

                Initialized = true;
                if (_verbose)
                {
                    Console.Out.WriteLine("Dictionary initialized in {0}", TimeUtils.GetTimeSpent(t0));
                    Console.Out.WriteLine("\t+ {0} Words", DatabaseManager.DbContext.Words.Count());
                    Console.Out.WriteLine("\t+ {0} Definitions", DatabaseManager.DbContext.Definitions.Count());
                    Console.Out.WriteLine("\t+ {0} Nouns", DatabaseManager.DbContext.Nouns.Count());
                    Console.Out.WriteLine("\t+ {0} Verbs", DatabaseManager.DbContext.Verbs.Count());
                    Console.Out.WriteLine("\t+ {0} Adjectives", DatabaseManager.DbContext.Adjectives.Count());
                }
            }
        }

        private static void EmptyDictionary(MySqlConnection conn)
        {
            var tables = new List<string> {"dict_def", "dict_noun", "dict_verb", "dict_adj", "dict_word"};
            DatabaseManager.ExecSql(conn, string.Join("", tables.Select(name => $"DELETE FROM {name} WHERE 1;")));
        }

        private static void LoadIrregularPlurals()
        {
            _irregularPlurals = new Dictionary<string, string>();
            List<Noun> list = new List<Noun>();
            string[,] data = StringUtils.ReadTable(FileUtils.ReadResource("dict/irregular_plurals.csv"));
            for (var i = 0; i < data.GetLength(0); i++)
            {
                if (!_irregularPlurals.ContainsKey(data[i, 0]))
                {
                    _irregularPlurals.Add(data[i, 0], data[i, 1]);
                    list.Add(new Noun(data[i, 0], data[i, 1]));
                }
            }

            DatabaseManager.DbContext.AddRange(list);
            DatabaseManager.DbContext.SaveChanges();
        }
    }
}