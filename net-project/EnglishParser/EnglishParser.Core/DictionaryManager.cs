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
        private static Dictionary<string, string> _irregularThirdPersons;
        private static Dictionary<string, string[]> _irregularVerbs;
        private static Dictionary<string, string> _irregularAdverbs;
        private static Dictionary<string, string> _genderNouns;

        private static Dictionary<int, int> _synSetMapping;
        private static int _autoInc;

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

                        t1 = TimeUtils.Now();
                        LoadIrregularThirdPersons();
                        if (_verbose)
                            Console.Out.WriteLine("\tLoaded {0} irregular third persons in {1}",
                                _irregularThirdPersons.Count,
                                TimeUtils.GetTimeSpent(t1));

                        t1 = TimeUtils.Now();
                        LoadIrregularVerbs();
                        if (_verbose)
                            Console.Out.WriteLine("\tLoaded {0} irregular verbs in {1}", _irregularVerbs.Count,
                                TimeUtils.GetTimeSpent(t1));

                        t1 = TimeUtils.Now();
                        LoadIrregularAdverbs();
                        if (_verbose)
                            Console.Out.WriteLine("\tLoaded {0} irregular adverbs in {1}", _irregularAdverbs.Count,
                                TimeUtils.GetTimeSpent(t1));

                        t1 = TimeUtils.Now();
                        LoadGenderNouns();
                        if (_verbose)
                            Console.Out.WriteLine("\tLoaded {0} gender nouns in {1}", _genderNouns.Count,
                                TimeUtils.GetTimeSpent(t1));

                        t1 = TimeUtils.Now();
                        DatabaseManager.ImportSql(conn, "dict/sql/wordnet_init.sql");
                        if (_verbose)
                            Console.Out.WriteLine("\tImported wordnet structure in {0}", TimeUtils.GetTimeSpent(t1));

                        if (_verbose)
                            Console.Out.WriteLine("\tImporting wordnet data...");
                        t1 = TimeUtils.Now();
                        DatabaseManager.ImportSql(conn, "dict/sql/wordnet_fill.sql");
                        if (_verbose)
                            Console.Out.WriteLine("\tImported wordnet data in {0}", TimeUtils.GetTimeSpent(t1));

                        if (_verbose)
                            Console.Out.WriteLine("\tComputing words");
                        t1 = TimeUtils.Now();
                        int count = ComputeWords(conn);
                        if (_verbose)
                            Console.Out.WriteLine("\tComputed {0} words in {1}", count, TimeUtils.GetTimeSpent(t1));

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
            List<string> tables = new List<string> {"dict_def", "dict_noun", "dict_verb", "dict_adj", "dict_word"};
            DatabaseManager.ExecSql(conn, string.Join("", tables.Select(name => $"DELETE FROM {name} WHERE 1;")));
        }

        #region Load Irregulars

        private static void LoadIrregularPlurals()
        {
            _irregularPlurals = new Dictionary<string, string>();
            string[,] data = StringUtils.ReadTable(FileUtils.ReadResource("dict/irregular_plurals.csv"));
            for (int i = 0; i < data.GetLength(0); i++)
                if (!_irregularPlurals.ContainsKey(data[i, 0]))
                {
                    _irregularPlurals.Add(data[i, 0], data[i, 1]);
                    DatabaseManager.DbContext.Add(new Noun(data[i, 0], data[i, 1]));
                }

            DatabaseManager.DbContext.SaveChanges();
        }

        private static void LoadIrregularThirdPersons()
        {
            _irregularThirdPersons = new Dictionary<string, string>();
            string[,] data = StringUtils.ReadTable(FileUtils.ReadResource("dict/irregular_third_persons.csv"));
            for (int i = 0; i < data.GetLength(0); i++)
                if (!_irregularThirdPersons.ContainsKey(data[i, 0]))
                {
                    _irregularThirdPersons.Add(data[i, 0], data[i, 1]);
                }

            DatabaseManager.DbContext.SaveChanges();
        }

        private static void LoadIrregularVerbs()
        {
            _irregularVerbs = new Dictionary<string, string[]>();
            string[][][] data =
                StringUtils.ReadIrregularTable(FileUtils.ReadResource("dict/irregular_verbs.csv"), "\n", ";", "/");
            foreach (string[][] subdata in data)
            {
                string[][] line = subdata;
                if (subdata.Length == 1)
                    line = new[] {subdata[0], new[] {""}, new[] {""}};
                else if (subdata.Length == 2)
                    line = new[] {subdata[0], subdata[1], new[] {""}};

                _irregularVerbs.Add(line[0][0], new[] {line[1][0], line[2][0]});

                string root, pt, pp;
                int maxDepth = line.Max(c => c.Length);
                for (int i = 0; i < maxDepth; i++)
                {
                    root = i < line[0].Length ? line[0][i] : line[0][0];
                    pt = i < line[1].Length ? line[1][i] : line[1][0];
                    pp = i < line[2].Length ? line[2][i] : line[2][0];
                    DatabaseManager.DbContext.Add(new Verb(root, pt, pp,
                        pp.Length == 0 ? "" : EnglishUtils.GetPresentParticiple(root),
                        GetThirdPerson(root)));
                }
            }

            DatabaseManager.DbContext.SaveChanges();
        }

        private static void LoadIrregularAdverbs()
        {
            _irregularAdverbs = new Dictionary<string, string>();
            string[,] data = StringUtils.ReadTable(FileUtils.ReadResource("dict/irregular_adverbs.csv"));
            for (int i = 0; i < data.GetLength(0); i++)
                if (!_irregularAdverbs.ContainsKey(data[i, 0]))
                {
                    _irregularAdverbs.Add(data[i, 0], data[i, 1]);
                    DatabaseManager.DbContext.Add(new Adjective(data[i, 0], data[i, 1]));
                }

            DatabaseManager.DbContext.SaveChanges();
        }


        private static void LoadGenderNouns()
        {
            _genderNouns = new Dictionary<string, string>();
            string[,] data = StringUtils.ReadTable(FileUtils.ReadResource("dict/gender_nouns.csv"));
            for (int i = 0; i < data.GetLength(0); i++)
            {
                _genderNouns.Add(data[i, 0], data[i, 1]);
                Noun nm = DatabaseManager.DbContext.Nouns.FirstOrDefault(n =>
                    n.Base == data[i, 0] || n.Plural == data[i, 0]);
                if (nm == null)
                {
                    nm = new Noun(data[i, 0], GetNounPlural(data[i, 0]));
                    DatabaseManager.DbContext.Add(nm);
                    DatabaseManager.DbContext.SaveChanges();
                }

                Noun nf = DatabaseManager.DbContext.Nouns.FirstOrDefault(n =>
                    n.Base == data[i, 1] || n.Plural == data[i, 1]);
                if (nf != null)
                    DatabaseManager.DbContext.Remove(nf);
                nm.SetFemale(data[i, 1], GetNounPlural(data[i, 1]));
                DatabaseManager.DbContext.Update(nm);
            }

            DatabaseManager.DbContext.SaveChanges();
        }

        #endregion

        #region English Utils with irregulars

        private static string GetNounPlural(string noun)
        {
            foreach (KeyValuePair<string, string> irregular in _irregularPlurals)
                if (StringUtils.PartOf(noun, irregular.Key, 4, "man"))
                {
                    string prefix = noun.Substring(0, noun.LastIndexOf(irregular.Key, StringComparison.Ordinal));
                    return prefix + irregular.Value;
                }

            return EnglishUtils.GetNounPlural(noun);
        }

        private static string GetAdverb(string adj)
        {
            foreach (KeyValuePair<string, string> irregular in _irregularAdverbs)
                if (StringUtils.PartOf(adj, irregular.Key, 4))
                {
                    string prefix = adj.Substring(0, adj.LastIndexOf(irregular.Key, StringComparison.Ordinal));
                    return prefix + irregular.Value;
                }

            return EnglishUtils.GetAdverb(adj);
        }

        private static string GetThirdPerson(string verb)
        {
            if (_irregularThirdPersons.ContainsKey(verb))
                return _irregularThirdPersons[verb];
            return EnglishUtils.GetThirdPerson(verb);
        }

        #endregion

        #region Compute Words

        private static int GetSynSetId(int synSetId)
        {
            if (_synSetMapping == null)
            {
                _synSetMapping = new Dictionary<int, int>();
                _autoInc = 1;
            }

            if (!_synSetMapping.ContainsKey(synSetId))
                _synSetMapping.Add(synSetId, _autoInc++);
            return _synSetMapping[synSetId];
        }

        private static int ComputeWords(MySqlConnection conn)
        {
            int row = 0;
            int rowCount = DatabaseManager.QuerySqlInt(conn, "SELECT COUNT(*) FROM wn_synset");
            int rowStep = rowCount / 20; // 5%

            string word;
            string type;
            int synSetId;
            int wNum;

            List<long> ts = new List<long>()
            {
                TimeUtils.Now()
            };

            DatabaseManager.QuerySql(conn, "SELECT * FROM wn_synset WHERE 1", reader =>
            {
                while (reader.Read())
                {
                    word = reader.GetString("word");
                    type = reader.GetString("ss_type");
                    synSetId = reader.GetInt32("synset_id");
                    wNum = reader.GetInt32("w_num");

                    Word.WordType wordType = Word.WordType.Undef;

                    if (word.Contains("_"))
                        continue;

                    word = word.Split("\\(")[0];
                    switch (type)
                    {
                        case "n":
                            wordType = Word.WordType.Noun;

                            //TODO compute noun
                            break;
                        case "v":
                            wordType = Word.WordType.Verb;
                            //TODO compute verb
                            break;
                        case "a":
                        case "s":
                            wordType = Word.WordType.Adjective;
                            //TODO compute adjective
                            break;
                        case "r":
                            wordType = Word.WordType.Adverb;
                            //TODO compute adverb
                            break;
                        default:
                            continue;
                    }

                    DatabaseManager.DbContext.Add(new Word(word, (int) wordType, GetSynSetId(synSetId), wNum));

                    if (row++ > 0 && row % rowStep == 0)
                    {
                        DatabaseManager.DbContext.SaveChanges();
                        Console.Out.WriteLine("\t\tComputed {0}/{1} words ({2}%) (ETA {3})", row, rowCount,
                            Math.Round(100 * row / (decimal) rowCount), TimeUtils.GetETA(ts, rowStep, rowCount));
                    }
                }
            });

            //TODO definitions

            DatabaseManager.DbContext.SaveChanges();

            return row;
        }

        #endregion
    }
}