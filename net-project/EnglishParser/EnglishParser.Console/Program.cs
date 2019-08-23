using System.Reflection;
using Nini.Config;

namespace EnglishParser.Console
{
    internal class Program
    {
        private static void Main(string[] args)
        {
            System.Console.WriteLine(Assembly.GetExecutingAssembly().GetName().Name);
            IConfigSource source = new IniConfigSource("EnglishParser.ini");
            Core.EnglishParser.Init(source.Configs);
        }
    }
}