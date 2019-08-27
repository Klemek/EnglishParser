using System;
using System.Collections.Generic;
using System.Linq;
using EnglishParser.DB;
using EnglishParser.Model;
using EnglishParser.Utils;
using Microsoft.EntityFrameworkCore.Internal;
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

        private static DatabaseEntities DbContext => DatabaseManager.DbContext;

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
                    if (_verbose)
                        Console.Out.Write("\tEmptying dictionary...");
                    t1 = TimeUtils.Now();
                    EmptyDictionary(conn);
                    if (_verbose) Console.Out.WriteLine("\r\tEmptied dictionary in {0}", TimeUtils.GetTimeSpent(t1));

                    if (config.GetBoolean("PreComputed"))
                    {
                        if (_verbose) Console.Out.Write("\tImporting pre-computed data...");
                        t1 = TimeUtils.Now();
                        DatabaseManager.ImportSql(conn, "dict/sql/ep_fill.sql");
                        if (_verbose)
                            Console.Out.WriteLine("\r\tImported pre-computed data in {0}", TimeUtils.GetTimeSpent(t1));
                    }
                    else
                    {
                        if (_verbose)
                            Console.Out.Write("\tLoading irregular plurals...");
                        t1 = TimeUtils.Now();
                        LoadIrregularPlurals();
                        if (_verbose)
                            Console.Out.WriteLine("\r\tLoaded {0} irregular plurals in {1}", _irregularPlurals.Count,
                                TimeUtils.GetTimeSpent(t1));

                        if (_verbose)
                            Console.Out.Write("\tLoading irregular third persons...");
                        t1 = TimeUtils.Now();
                        LoadIrregularThirdPersons();
                        if (_verbose)
                            Console.Out.WriteLine("\r\tLoaded {0} irregular third persons in {1}",
                                _irregularThirdPersons.Count,
                                TimeUtils.GetTimeSpent(t1));

                        if (_verbose)
                            Console.Out.Write("\tLoading irregular verbs...");
                        t1 = TimeUtils.Now();
                        LoadIrregularVerbs();
                        if (_verbose)
                            Console.Out.WriteLine("\r\tLoaded {0} irregular verbs in {1}", _irregularVerbs.Count,
                                TimeUtils.GetTimeSpent(t1));

                        if (_verbose)
                            Console.Out.Write("\tLoading irregular adverbs...");
                        t1 = TimeUtils.Now();
                        LoadIrregularAdverbs();
                        if (_verbose)
                            Console.Out.WriteLine("\r\tLoaded {0} irregular adverbs in {1}", _irregularAdverbs.Count,
                                TimeUtils.GetTimeSpent(t1));

                        if (_verbose)
                            Console.Out.Write("\tLoading gender nouns...");
                        t1 = TimeUtils.Now();
                        LoadGenderNouns();
                        if (_verbose)
                            Console.Out.WriteLine("\r\tLoaded {0} gender nouns in {1}", _genderNouns.Count,
                                TimeUtils.GetTimeSpent(t1));

                        if (_verbose)
                            Console.Out.Write("\tImporting wordnet structure...");
                        t1 = TimeUtils.Now();
                        DatabaseManager.ImportSql(conn, "dict/sql/wordnet_init.sql");
                        if (_verbose)
                            Console.Out.WriteLine("\r\tImported wordnet structure in {0}", TimeUtils.GetTimeSpent(t1));

                        if (_verbose)
                            Console.Out.Write("\tImporting wordnet data...");
                        t1 = TimeUtils.Now();
                        DatabaseManager.ImportSql(conn, "dict/sql/wordnet_fill.sql");
                        if (_verbose)
                            Console.Out.WriteLine("\r\tImported wordnet data in {0}", TimeUtils.GetTimeSpent(t1));

                        if (_verbose)
                            Console.Out.Write("\tComputing words...");
                        t1 = TimeUtils.Now();
                        int count = ComputeWords(conn);
                        if (_verbose)
                            Console.Out.WriteLine("\r\tComputed {0} words in {1}                 ", count,
                                TimeUtils.GetTimeSpent(t1));

                        if (_verbose)
                            Console.Out.Write("\tComputing definitions...");
                        t1 = TimeUtils.Now();
                        count = ComputeDefinitions(conn);
                        if (_verbose)
                            Console.Out.WriteLine("\r\tComputed {0} definitions in {1}                ", count,
                                TimeUtils.GetTimeSpent(t1));

                        if (_verbose)
                            Console.Out.Write("\tDropping wordnet structure...");
                        t1 = TimeUtils.Now();
                        DatabaseManager.ImportSql(conn, "dict/sql/wordnet_drop.sql");
                        if (_verbose)
                            Console.Out.WriteLine("\r\tDropped wordnet structure and data in {0}",
                                TimeUtils.GetTimeSpent(t1));
                    }

                    int res = DatabaseManager.ExecSql(conn, "UPDATE db_info SET dict_init = 1 WHERE 1");
                    DatabaseManager.QuerySql(conn, "SELECT dict_init FROM db_info", reader =>
                    {
                        reader.Read();
                        Console.Out.WriteLine("rows affected={0}; dict_init={1}",res,reader.GetInt16(0));
                    });
                }

                Initialized = true;
                if (_verbose)
                {
                    Console.Out.WriteLine("Dictionary initialized in {0}", TimeUtils.GetTimeSpent(t0));
                    Console.Out.WriteLine("\t+ {0} Words", DbContext.Words.Count());
                    Console.Out.WriteLine("\t+ {0} Definitions", DbContext.Definitions.Count());
                    Console.Out.WriteLine("\t+ {0} Nouns", DbContext.Nouns.Count());
                    Console.Out.WriteLine("\t+ {0} Verbs", DbContext.Verbs.Count());
                    Console.Out.WriteLine("\t+ {0} Adjectives", DbContext.Adjectives.Count());
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
                    DbContext.Add(new Noun(data[i, 0], data[i, 1]));
                }

            DbContext.SaveChanges();
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

            DbContext.SaveChanges();
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
                    DbContext.Add(new Verb(root, pt, pp,
                        pp.Length == 0 ? "" : EnglishUtils.GetPresentParticiple(root),
                        GetThirdPerson(root)));
                }
            }

            DbContext.SaveChanges();
        }

        private static void LoadIrregularAdverbs()
        {
            _irregularAdverbs = new Dictionary<string, string>();
            string[,] data = StringUtils.ReadTable(FileUtils.ReadResource("dict/irregular_adverbs.csv"));
            for (int i = 0; i < data.GetLength(0); i++)
                if (!_irregularAdverbs.ContainsKey(data[i, 0]))
                {
                    _irregularAdverbs.Add(data[i, 0], data[i, 1]);
                    DbContext.Add(new Adjective(data[i, 0], data[i, 1]));
                }

            DbContext.SaveChanges();
        }


        private static void LoadGenderNouns()
        {
            _genderNouns = new Dictionary<string, string>();
            string[,] data = StringUtils.ReadTable(FileUtils.ReadResource("dict/gender_nouns.csv"));
            for (int i = 0; i < data.GetLength(0); i++)
            {
                _genderNouns.Add(data[i, 0], data[i, 1]);
                Noun nm = DbContext.GetNoun(data[i, 0]);
                if (nm == null)
                {
                    nm = new Noun(data[i, 0], GetNounPlural(data[i, 0]));
                    DbContext.Add(nm);
                    DbContext.SaveChanges();
                }

                Noun nf = DbContext.GetNoun(data[i, 1]);
                if (nf != null)
                    DbContext.Remove(nf);
                nm.SetFemale(data[i, 1], GetNounPlural(data[i, 1]));
                DbContext.Update(nm);
            }

            DbContext.SaveChanges();
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
            int rowStep = rowCount / 100; // 1%

            string word;
            string type;
            int synSetId;
            int wNum;

            List<Noun> nounBuffer = new List<Noun>();
            List<Verb> verbBuffer = new List<Verb>();
            List<Adjective> adjectiveBuffer = new List<Adjective>();

            List<long> ts = new List<long>
            {
                TimeUtils.Now()
            };

            while (row < rowCount)
            {
                nounBuffer.Clear();
                verbBuffer.Clear();
                adjectiveBuffer.Clear();
                DatabaseManager.QuerySql(conn, "SELECT * FROM wn_synset LIMIT @rowStep OFFSET @row", reader =>
                {
                    while (reader.Read())
                    {
                        row++;

                        word = reader.GetString("word");
                        type = reader.GetString("ss_type");
                        synSetId = reader.GetInt32("synset_id");
                        wNum = reader.GetInt32("w_num");

                        Word.WordType wordType = Word.WordType.Undef;

                        if (word.Contains("_") || word == null)
                            continue;

                        word = word.Split("\\(")[0];
                        switch (type)
                        {
                            case "n":
                                wordType = Word.WordType.Noun;
                                string word1 = word;
                                if (nounBuffer.All(w => w.Base != word1) && !DbContext.NounExists(word) &&
                                    !DbContext.FemaleNounExists(word))
                                    nounBuffer.AddIfNotNull(ComputeNewNoun(word));
                                break;
                            case "v":
                                string word2 = word;
                                if (verbBuffer.All(w => w.Base != word2) && !DbContext.VerbExists(word))
                                    verbBuffer.Add(ComputeNewVerb(word));
                                break;
                            case "a":
                            case "s":
                                wordType = Word.WordType.Adjective;
                                string word3 = word;
                                if (adjectiveBuffer.All(w => w.Base != word3) && !DbContext.AdjectiveExists(word))
                                    adjectiveBuffer.Add(ComputeNewAdjective(word));
                                break;
                            case "r":
                                wordType = Word.WordType.Adverb;
                                break;
                            default:
                                continue;
                        }

                        DbContext.Add(new Word(word, (int) wordType, GetSynSetId(synSetId), wNum));
                    }
                }, ("@rowstep", rowStep), ("@row", row));
                DbContext.AddRange(nounBuffer);
                DbContext.AddRange(verbBuffer);
                DbContext.AddRange(adjectiveBuffer);
                DbContext.SaveChanges();
                Console.Out.Write("\r\tComputed {0}/{1} words ({2}%) (ETA {3})         ", row, rowCount,
                    Math.Round(100 * row / (decimal) rowCount), TimeUtils.GetEta(ts, rowStep, rowCount));
            }

            return row;
        }

        private static Noun ComputeNewNoun(string word)
        {
            foreach (KeyValuePair<string, string> gn in _genderNouns)
            {
                if (word.EndsWith(gn.Key))
                {
                    string prefix = word.Substring(0, word.LastIndexOf(gn.Key, StringComparison.Ordinal));
                    return new Noun(word, GetNounPlural(word),
                        prefix + gn.Value, prefix + GetNounPlural(gn.Value));
                }

                if (word.EndsWith(gn.Value))
                {
                    string prefix = word.Substring(0, word.LastIndexOf(gn.Value, StringComparison.Ordinal));
                    if (!DbContext.NounExists(prefix + gn.Key))
                        return null;
                    return new Noun(prefix + gn.Key, prefix + GetNounPlural(gn.Key),
                        word, GetNounPlural(word));
                }
            }

            return new Noun(word, GetNounPlural(word));
        }

        private static Verb ComputeNewVerb(string word)
        {
            string pastTense = null;
            string pastPart = null;
            string presPart = EnglishUtils.GetPresentParticiple(word);
            string thirdPers = GetThirdPerson(word);

            foreach (KeyValuePair<string, string[]> irregular in _irregularVerbs)
                if (StringUtils.PartOf(word, irregular.Key, '-'))
                {
                    string[] pasts = irregular.Value;
                    if (pasts[1].Length > 0)
                    {
                        string prefix = word.Substring(0, word.LastIndexOf(irregular.Key, StringComparison.Ordinal));
                        pastTense = prefix + pasts[0];
                        pastPart = prefix + pasts[1];
                        break;
                    }
                }

            if (pastTense == null)
            {
                pastTense = EnglishUtils.GetRegularPast(word);
                pastPart = EnglishUtils.GetRegularPast(word);
            }

            return new Verb(word, pastTense, pastPart, presPart, thirdPers);
        }

        private static Adjective ComputeNewAdjective(string word)
        {
            return new Adjective(word, GetAdverb(word));
        }

        private static int ComputeDefinitions(MySqlConnection conn)
        {
            int row = 0;
            int rowCount = DatabaseManager.QuerySqlInt(conn, "SELECT COUNT(*) FROM wn_gloss");
            int rowStep = rowCount / 20; // 5%
            string gloss;
            int synSetId;
            List<long> ts = new List<long>
            {
                TimeUtils.Now()
            };
            while (row < rowCount)
            {
                DatabaseManager.QuerySql(conn, "SELECT * FROM wn_gloss LIMIT @rowstep OFFSET @row", reader =>
                {
                    while (reader.Read())
                    {
                        row++;

                        gloss = reader.GetString("gloss");
                        synSetId = reader.GetInt32("synset_id");
                        if (_synSetMapping.ContainsKey(synSetId))
                            DbContext.Add(new Definition(GetSynSetId(synSetId), gloss));
                    }
                }, ("@rowstep", rowStep), ("@row", row));
                DbContext.SaveChanges();
                Console.Out.Write("\r\tComputed {0}/{1} definitions ({2}%) (ETA {3})         ", row, rowCount,
                    Math.Round(100 * row / (decimal) rowCount), TimeUtils.GetEta(ts, rowStep, rowCount));
            }

            return row;
        }

        #endregion
    }
}