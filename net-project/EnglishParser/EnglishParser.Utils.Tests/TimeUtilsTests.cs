using NUnit.Framework;

namespace EnglishParser.Utils.Tests
{
    public class TimeUtilsTests
    {
        [Test]
        public void GetTimeSpan()
        {
            Assert.AreEqual("no time", TimeUtils.GetTimeSpan(0));
            Assert.AreEqual("no time", TimeUtils.GetTimeSpan(-10));
            Assert.AreEqual("23 ms", TimeUtils.GetTimeSpan(23));
            Assert.AreEqual("15 s 000 ms", TimeUtils.GetTimeSpan(15000));
            Assert.AreEqual("1 m 00 s", TimeUtils.GetTimeSpan(60 * 1000 + 23));
            Assert.AreEqual("15 m 05 s", TimeUtils.GetTimeSpan(15 * 60 * 1000 + 5 * 1000 + 23));
            Assert.AreEqual("5 h 02 m", TimeUtils.GetTimeSpan(5 * 60 * 60 * 1000 + 2 * 60 * 1000 + 5 * 1000 + 23));
        }
    }
}