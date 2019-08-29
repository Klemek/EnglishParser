using System;
using System.Collections.Generic;
using System.Linq;
using EnglishParser.DB;
using EnglishParser.Model;
using EnglishParser.Utils;
using MySql.Data.MySqlClient;
using Nini.Config;
using NUnit.Framework;

namespace EnglishParser.Core.Tests
{
    public class DictionaryManagerTest
    {
        private static IConfig _config;
        private static MySqlConnection _conn;
        private static DbContext DbContext => DatabaseManager.DbContext;

        [OneTimeSetUp]
        public static void InitClass()
        {
            Console.Out.WriteLine("hello");
            IConfigSource configSource = new IniConfigSource("EnglishParser.Core.Tests.ini");
            Logger.Init(configSource.Configs["Logger"]);
            DatabaseManager.Init(configSource.Configs["Database"]); //prepare connection
            _conn = DatabaseManager.Connect(true);
            DatabaseManager.ImportSql(typeof(DatabaseManager).Assembly, _conn, "sql/clean.sql");
            DatabaseManager.Init(configSource.Configs["Database"]); //rebuild database
            _config = configSource.Configs["Dictionary"];
            _config.Set("PreComputed", false);
        }

        [OneTimeTearDown]
        public void Cleanup()
        {
            _conn.Close();
        }

        [SetUp]
        public void Setup()
        {
            if (!DictionaryManager.Initialized)
            {
                DictionaryManager.Init(_config);
                Assert.IsTrue(DictionaryManager.Initialized);
            }
        }

        [Test]
        public void DatabaseUpdated()
        {
            DatabaseManager.QuerySql(_conn, "SELECT dict_init FROM db_info", reader =>
            {
                reader.Read();
                Assert.IsTrue(reader.GetBoolean("dict_init"));
            });
        }

        [Test]
        public void Words()
        {
            Dictionary<Word.WordType, string[]> expected = new Dictionary<Word.WordType, string[]>
            {
                {
                    Word.WordType.Noun,
                    new[] {"entity", "frog", "frogs", "toad", "Frenchman", "Frenchwoman", "spadefoot"}
                },
                {
                    Word.WordType.Adjective,
                    new[] {"easy", "economic", "dying", "nonpublic"}
                },
                {
                    Word.WordType.Adverb,
                    new[] {"easily", "economically"}
                },
                {
                    Word.WordType.Verb,
                    new[] {"hack", "do", "can"}
                },
            };
            foreach (KeyValuePair<Word.WordType, string[]> expectedType in expected)
            foreach (string word in expectedType.Value)
            {
                Assert.IsTrue(DbContext.Words.Any(w => w.Text == word), $"{word} does not exist");
                Assert.IsTrue(DbContext.Words.Any(w => w.Text == word && w.Type == (int) expectedType.Key),
                    $"{word} is not {expectedType.Key}");
            }

            Assert.IsFalse(DbContext.Words.Any(w => w.Text == "French_person"), "French_person was not discarded");
            Assert.AreEqual(2, DbContext.Words.Count(w => w.Text == "frog" && w.Type == (int) Word.WordType.Noun),
                "not 2 entries for frog");
        }

        [Test]
        public void Definitions()
        {
            foreach (string w in new[] {"entity", "frog", "Frenchman"})
            foreach (Word word in DbContext.GetWord(w))
            {
                Assert.NotNull(word.Definition);
                Assert.GreaterOrEqual(word.Definition.Synonyms.Count, 1);
            }
        }

        [Test]
        public void Synonyms()
        {
            Word word = DbContext.GetWord("toad")[0];
            Assert.AreEqual(2, word.Synonyms.Count);
            Assert.AreEqual("frog", word.Synonyms[0].Text);
            Assert.AreEqual("toad", word.Synonyms[1].Text);
        }

        [Test]
        public void Nouns()
        {
            Assert.AreEqual(new Noun("entity", "entities"), DbContext.GetNoun("entity"));
            Assert.AreEqual(new Noun("frog", "frogs"), DbContext.GetNoun("frog"));
            Assert.AreEqual(new Noun("toad", "toads"), DbContext.GetNoun("toad"));
            Assert.AreEqual(new Noun("Frenchman", "Frenchmen", "Frenchwoman", "Frenchwomen"),
                DbContext.GetNoun("Frenchman"));
            Assert.IsNull(DbContext.GetNoun("Frenchwoman"));
            Assert.AreEqual(new Noun("spadefoot", "spadefeet"), DbContext.GetNoun("spadefoot"));
            Assert.IsNull(DbContext.GetNoun("French_person"));
        }

        [Test]
        public void Adjectives()
        {
            Assert.AreEqual(new Adjective("easy", "easily"), DbContext.GetAdjective("easy"));
            Assert.AreEqual(new Adjective("economic", "economically"), DbContext.GetAdjective("economic"));
            Assert.AreEqual(new Adjective("dying", "dyingly"), DbContext.GetAdjective("dying"));
            Assert.AreEqual(new Adjective("nonpublic", "nonpublicly"), DbContext.GetAdjective("nonpublic"));
        }

        [Test]
        public void Verbs()
        {
            Assert.AreEqual(new Verb("hack", "hacked", "hacked", "hacking", "hacks"), DbContext.GetVerb("hack"));
            Assert.AreEqual(new Verb("do", "did", "done", "doing", "does"), DbContext.GetVerb("do"));
            Assert.AreEqual(new Verb("can", "could", "", "", "can"), DbContext.GetVerb("can"));
            Assert.AreEqual(new Verb("babysit", "babysat", "babysat", "babysitting", "babysits"),
                DbContext.GetVerb("babysit"));
        }
    }
}
