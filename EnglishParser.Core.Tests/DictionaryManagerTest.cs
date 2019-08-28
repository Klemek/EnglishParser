using System;
using EnglishParser.DB;
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
        
        [OneTimeSetUp]
        public void Init()
        {
            IConfigSource configSource = new IniConfigSource("Test.ini");
            Logger.Init(configSource.Configs["Logger"]);
            DatabaseManager.Init(configSource.Configs["Database"]); //prepare connection
            _conn = DatabaseManager.Connect(true);
            DatabaseManager.ImportSql(typeof(DatabaseManager).Assembly, _conn, "sql/clean.sql");
            DatabaseManager.Init(configSource.Configs["Database"]); //rebuild database
            _config = configSource.Configs["Dictionary"];
        }

        [OneTimeTearDown]
        public void Cleanup()
        {
            _conn.Close();
        }

        [SetUp]
        public void Setup()
        {
            
        }

        [Test]
        public void SampleTest()
        {
            DictionaryManager.Init(_config);
        }
        
    }
}