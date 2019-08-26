using System.Collections.Generic;
using MySql.Data.MySqlClient;
using Nini.Config;
using NUnit.Framework;

namespace EnglishParser.DB.Tests
{
    public class DatabaseManagerTests
    {
        private static IConfig _config;
        private static MySqlConnection _conn;

        private void CleanDatabase()
        {
            DatabaseManager.ImportSql(typeof(DatabaseManager).Assembly, _conn, "sql/clean.sql");
        }


        [OneTimeSetUp]
        public void Init()
        {
            _config = new IniConfigSource("TestDatabase.ini").Configs["Database"];
            DatabaseManager.Init(_config);
            _conn = DatabaseManager.Connect(true);
        }

        [OneTimeTearDown]
        public void Cleanup()
        {
            _conn.Close();
        }

        [SetUp]
        public void Setup()
        {
            DatabaseManager.Init(_config);
        }

        [Test]
        public void ListTables()
        {
            List<string> tables = DatabaseManager.ListTables(_conn);
            Assert.Contains("db_info", tables);
            Assert.Greater(tables.Count, 1);
            CleanDatabase();
            tables = DatabaseManager.ListTables(_conn);
            Assert.AreEqual(0, tables.Count);
        }

        [Test]
        public void TableExists()
        {
            Assert.True(DatabaseManager.TableExists(_conn, "db_info"));
            Assert.False(DatabaseManager.TableExists(_conn, "db_info_2"));
            CleanDatabase();
            Assert.False(DatabaseManager.TableExists(_conn, "db_info"));
        }

        [Test]
        public void UpgradeDatabase()
        {
            CleanDatabase();
            Assert.False(DatabaseManager.TableExists(_conn, "db_info"));
            DatabaseManager.UpgradeDatabase();
            Assert.True(DatabaseManager.TableExists(_conn, "db_info"));
            DatabaseManager.QuerySql(_conn, "SELECT * FROM db_info", reader =>
            {
                reader.Read();
                Assert.AreEqual(_config.GetInt("Version"), reader.GetInt32("version"));
            });
        }
    }
}