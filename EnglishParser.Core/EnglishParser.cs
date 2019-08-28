using EnglishParser.DB;
using EnglishParser.Utils;
using Nini.Config;

namespace EnglishParser.Core
{
    public static class EnglishParser
    {
        public static void Init(ConfigCollection configs)
        {
            Logger.Init(configs["Logger"]);
            DatabaseManager.Init(configs["Database"]);
            DictionaryManager.Init(configs["Dictionary"]);
        }
    }
}