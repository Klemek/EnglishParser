using NUnit.Framework;

namespace EnglishParser.Utils.Tests
{
    public class StringUtilsTests
    {
        [SetUp]
        public void Setup()
        {
        }

        [Test]
        public void PadLeft()
        {
            Assert.AreEqual("01234",StringUtils.PadLeft("1234","0",5));
            Assert.AreEqual("12345",StringUtils.PadLeft("12345","0",5));
            Assert.AreEqual("00000",StringUtils.PadLeft("","0",5));
            Assert.AreEqual("aabbcc",StringUtils.PadLeft("bbcc","aa",5));
        }
    }
}