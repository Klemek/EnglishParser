using NUnit.Framework;

namespace EnglishParser.Utils.Tests
{
    public class StringUtilsTests
    {
        [Test]
        public void PadLeft()
        {
            Assert.AreEqual("01234", StringUtils.PadLeft("1234", "0", 5));
            Assert.AreEqual("12345", StringUtils.PadLeft("12345", "0", 5));
            Assert.AreEqual("00000", StringUtils.PadLeft("", "0", 5));
            Assert.AreEqual("aabbcc", StringUtils.PadLeft("bbcc", "aa", 5));
        }

        [Test]
        public void PartOf()
        {
            Assert.IsTrue(StringUtils.PartOf("abcdef", "def", 3));
            Assert.IsFalse(StringUtils.PartOf("abcdaf", "def", 3));
            Assert.IsFalse(StringUtils.PartOf("abcdef", "def", 4));
            Assert.IsTrue(StringUtils.PartOf("abcdef", "def", 4, "def"));
        }

        [Test]
        public void PartOfDelimiter()
        {
            Assert.IsTrue(StringUtils.PartOf("abc-def", "def", '-'));
            Assert.IsFalse(StringUtils.PartOf("abcdef", "def", '-'));
            Assert.IsFalse(StringUtils.PartOf("abc-daf", "def", '-'));
        }

        [Test]
        public void IsVowelConstant()
        {
            foreach (char c in new[] {'A', 'i'})
            {
                Assert.IsTrue(StringUtils.IsVowel(c));
                Assert.IsFalse(StringUtils.IsConsonant(c));
            }

            foreach (char c in new[] {'b', 'P'})
            {
                Assert.IsFalse(StringUtils.IsVowel(c));
                Assert.IsTrue(StringUtils.IsConsonant(c));
            }

            foreach (char c in new[] {'!', ' '})
            {
                Assert.IsFalse(StringUtils.IsVowel(c));
                Assert.IsFalse(StringUtils.IsConsonant(c));
            }
        }

        #region Tables

        [Test]
        public void ReadTable()
        {
            Assert.AreEqual(new[,] {{"a", "b"}, {"d", "e"}, {"f", "g"}},
                StringUtils.ReadTable("a.b.c-d.e-f.g", "-", "."));
        }

        [Test]
        public void ReadIrregularTable()
        {
            Assert.AreEqual(new[]
            {
                new[] {"a", "b", "c"},
                new[] {"d", "e"},
                new[] {"f", "g"}
            }, StringUtils.ReadIrregularTable("a.b.c-d.e-f.g", "-", "."));
        }

        [Test]
        public void ReadIrregularTable2()
        {
            Assert.AreEqual(
                new[]
                {
                    new[] {new[] {"a"}, new[] {"b"}, new[] {"c"}},
                    new[] {new[] {"d"}, new[] {"e"}},
                    new[] {new[] {"f", "g"}}
                }, StringUtils.ReadIrregularTable("a.b.c-d.e-f/g", "-", ".", "/"));
        }

        #endregion

        #region Formatting

        [Test]
        public void Ellipsis()
        {
            Assert.AreEqual(null, StringUtils.Ellipsis(null, 5));
            Assert.AreEqual("", StringUtils.Ellipsis("", 5));
            Assert.AreEqual("abc", StringUtils.Ellipsis("abc", 5));
            Assert.AreEqual("abcde", StringUtils.Ellipsis("abcde", 5));
            Assert.AreEqual("ab...", StringUtils.Ellipsis("abcdef", 5));
        }

        [Test]
        public void DumpString()
        {
            Assert.AreEqual("null", StringUtils.DumpString(null));
            Assert.AreEqual("''", StringUtils.DumpString(""));
            Assert.AreEqual("'abc'", StringUtils.DumpString("abc"));
            Assert.AreEqual("'abcdefghijkl...'", StringUtils.DumpString("abcdefghijklmnopqrstuvwxyz"));
            Assert.AreEqual("'abcde\\nabcde...'", StringUtils.DumpString("abcde\nabcde\nabcde"));
        }

        [Test]
        public void TestToString()
        {
            TestClass1 test = new TestClass1
            {
                A = "A",
                B = 12
            };
            Assert.AreEqual("TestClass1{A: 'A', B: 12}", StringUtils.ToString(test));
            test = new TestClass1();
            Assert.AreEqual("TestClass1{A: null, B: 0}", StringUtils.ToString(test));
            TestClass2 test2 = new TestClass2();
            Assert.AreEqual("TestClass2{}", StringUtils.ToString(test2));
        }

        private class TestClass1
        {
            public string A;
            public int B;
        }

        private class TestClass2
        {
        }

        #endregion
    }
}