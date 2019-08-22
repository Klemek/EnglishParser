using Nini.Config;
using NUnit.Framework;

namespace EnglishParser.DB.Tests
{
    public class DatabaseManagerTests
    {
        [SetUp]
        public void Setup()
        {
            IConfigSource source = new IniConfigSource("TestDatabase.ini");
            DatabaseManager.Init(source.Configs["Database"]);
        }

        [Test]
        public void Test1()
        {
            Assert.Pass();
        }
    }
}