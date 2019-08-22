using System;
using System.Linq;
using EnglishParser.DB;
using Nini.Config;

namespace EnglishParser.Core
{
    public static class EnglishParser
    {
        public static void Init(ConfigCollection configs)
        {
            DatabaseManager.Init(configs["Database"]);
            Console.Out.WriteLine("\t+ {0} Words", DatabaseManager.Entities.Words.Count());
            //DictionaryManager.Init(configs["Dictionary"]);
        }
        
    }
}