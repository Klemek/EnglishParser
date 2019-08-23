using NUnit.Framework;

namespace EnglishParser.Utils.Tests
{
    public class FileUtilsTests
    {
        [SetUp]
        public void Setup()
        {
        }

        [Test]
        public void ReadResource()
        {
            Assert.AreEqual("Hello there", FileUtils.ReadResource("Sample.txt"));
        }
    }
}