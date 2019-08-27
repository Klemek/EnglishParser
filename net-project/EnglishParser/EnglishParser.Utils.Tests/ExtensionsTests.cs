using System.Collections.Generic;
using NUnit.Framework;

namespace EnglishParser.Utils.Tests
{
    public class ExtensionsTests
    {
        #region List
        
        [Test]
        public static void AddIfNotNull()
        {
            List<string> list = new List<string>();
            list.AddIfNotNull("hello");
            list.AddIfNotNull(null);
            Assert.AreEqual(1, list.Count);
            Assert.AreEqual("hello", list[0]);
        }
        
        #endregion
    }
}