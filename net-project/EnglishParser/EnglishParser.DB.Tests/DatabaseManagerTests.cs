using System;
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
        public void QuerySql()
        {
            bool called = false;
            int n1 = new Random().Next(10000);
            int n2 = new Random().Next(10000);
            DatabaseManager.QuerySql(_conn, "SELECT @n1 + @n2", reader =>
            {
                reader.Read();
                Assert.AreEqual(n1 + n2, reader.GetInt32(0));
                called = true;
            }, ("@n1", n1), ("@n2", n2));
            Assert.IsTrue(called);
        }

        [Test]
        public void ExecSql()
        {
            int n1 = new Random().Next(10000);
            int res = DatabaseManager.ExecSql(_conn, "DROP TABLE IF EXISTS hello; CREATE TABLE hello ( `key` INT NOT NULL DEFAULT @n1); INSERT INTO hello VALUES ();",
                ("@n1", n1));
            Assert.IsTrue(DatabaseManager.TableExists(_conn, "hello"));
            Assert.AreEqual(1, res);
            DatabaseManager.QuerySql(_conn, "SELECT * FROM hello", reader =>
            {
                reader.Read();
                Assert.AreEqual(n1, reader.GetInt32(0));
            });
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
        public void DictInitialized()
        {
            DatabaseManager.ExecSql(_conn, "UPDATE db_info SET dict_init = 0 WHERE 1");
            DatabaseManager.Init(_config);
            Assert.IsFalse(DatabaseManager.DictInitialized);
            DatabaseManager.ExecSql(_conn, "UPDATE db_info SET dict_init = 1 WHERE 1");
            DatabaseManager.Init(_config);
            Assert.IsTrue(DatabaseManager.DictInitialized);
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