using EnglishParser.DB;
using NUnit.Framework;

namespace EnglishParser.Utils.Tests
{
    public class FileUtilsTests
    {
        [Test]
        public void ReadResourceEmbedded()
        {
            Assert.AreEqual("Hello there", FileUtils.ReadResource("Sample.txt"));
        }
        
        [Test]
        public void ReadResourceWorkingDirectory()
        {
            Assert.AreEqual("Hello World", FileUtils.ReadResource("Sample2.txt"));
        }
        
        [Test]
        public void ReadResourceOverride()
        {
            Assert.AreEqual("It was me! Dio!", FileUtils.ReadResource(typeof(DatabaseManager).Assembly, "sql/clean.sql"));
        }
    }
}