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

        private void CleanDatabase()
        {
            using (MySqlConnection conn = DatabaseManager.Connect(true))
                CleanDatabase(conn);
        }
        
        private void CleanDatabase(MySqlConnection conn)
        {
            DatabaseManager.ImportSql(typeof(DatabaseManager).Assembly, conn, "sql/clean.sql");
        }
        
        [SetUp]
        public void Setup()
        {
            _config = new IniConfigSource("TestDatabase.ini").Configs["Database"];
            DatabaseManager.Init(_config);
        }

        [Test]
        public void ListTables()
        {
            using (MySqlConnection conn = DatabaseManager.Connect(true))
            {
                List<string> tables = DatabaseManager.ListTables(conn);
                Assert.Contains("db_info", tables);
                Assert.Greater(tables.Count, 1);
                CleanDatabase(conn);
                tables = DatabaseManager.ListTables(conn);
                Assert.AreEqual(0, tables.Count);
            }
        }
        
        [Test]
        public void TableExists()
        {
            using (MySqlConnection conn = DatabaseManager.Connect(true))
            {
                Assert.True(DatabaseManager.TableExists(conn, "db_info"));
                Assert.False(DatabaseManager.TableExists(conn, "db_info_2"));
                CleanDatabase(conn);
                Assert.False(DatabaseManager.TableExists(conn, "db_info"));
            }
        }
    }
}