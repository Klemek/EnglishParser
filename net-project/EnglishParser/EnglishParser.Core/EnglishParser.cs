using EnglishParser.DB;
using Nini.Config;

namespace EnglishParser.Core
{
    public static class EnglishParser
    {
        public static void Init()
        {
            IConfigSource source = new IniConfigSource("EnglishParser.ini");
            DatabaseManager.Init(source.Configs["Database"]);
        }
    }
}