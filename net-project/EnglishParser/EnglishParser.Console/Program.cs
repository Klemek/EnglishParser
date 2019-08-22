using System.Reflection;
using EnglishParser.Core;
using Nini.Config;

namespace EnglishParser.Console
{
    class Program
    {
        static void Main(string[] args)
        {
            System.Console.WriteLine(Assembly.GetExecutingAssembly().GetName().Name);
            IConfigSource source = new IniConfigSource("EnglishParser.ini");
            Core.EnglishParser.Init(source.Configs);
        }
    }
}