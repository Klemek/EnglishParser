using NUnit.Framework;

namespace EnglishParser.Utils.Tests
{
    public class FileUtilsTests
    {
        [Test]
        public void ReadResource()
        {
            Assert.AreEqual("Hello there", FileUtils.ReadResource("Sample.txt"));
        }
    }
}