using System.Reflection;
using EnglishParser.Core;

namespace EnglishParser.Console
{
    class Program
    {
        static void Main(string[] args)
        {
            System.Console.WriteLine(Assembly.GetExecutingAssembly().GetName().Name);
            Core.EnglishParser.Init();
        }
    }
}