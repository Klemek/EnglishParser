using System.IO;
using System.Reflection;
using Nini.Config;
using NUnit.Framework;

namespace EnglishParser.Utils.Tests
{
    public class LoggerTest
    {
        private IConfig _config;

        [OneTimeSetUp]
        public void Init()
        {
            IConfigSource configSource = new IniConfigSource("EnglishParser.Utils.Tests.ini");
            _config = configSource.Configs["Logger"];
        }
        
        [Test]
        public void LogFile()
        {
            Logger.Init(_config);
            Logger.WriteLine("Hello");
            FieldInfo field = typeof(Logger).GetField("_stream", BindingFlags.NonPublic | BindingFlags.Static);
            ((Stream)field.GetValue(null)).Close();
            using(Stream stream = File.OpenRead("EnglishParser.Tests.log"))
            using (StreamReader reader = new StreamReader(stream))
            {
                string data = reader.ReadToEnd();
                Assert.AreEqual("[EnglishParser.Utils.Tests.LoggerTest.LogFile] Hello", data.Split("\n")[1].Substring(26));
            }
        }
    }
}
