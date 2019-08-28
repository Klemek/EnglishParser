using System.Reflection;
using Nini.Config;

namespace EnglishParser.Console
{
    internal class Program
    {
        private static void Main(string[] args)
        {
            IConfigSource source = new IniConfigSource("EnglishParser.ini");
            Core.EnglishParser.Init(source.Configs);
        }
    }
}