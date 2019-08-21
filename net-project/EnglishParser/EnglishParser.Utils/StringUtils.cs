namespace EnglishParser.Utils
{
    public static class StringUtils
    {
        public static string PadLeft(string src, string padding, int len)
        {
            while (src.Length < len)
                src = padding + src;
            return src;
        }
    }
}