using System;
using System.Linq;

namespace EnglishParser.Utils
{
    public static class StringUtils
    {
        private static readonly char[] VOWELS = {'a', 'e', 'i', 'o', 'u', 'y'};

        public static string PadLeft(string src, string padding, int len)
        {
            while (src.Length < len)
                src = padding + src;
            return src;
        }

        public static bool PartOf(string word1, string word2, int minLen, params string[] excluded)
        {
            if (word1.Equals(word2))
                return true;
            if (word2.Length < minLen && !excluded.Contains(word2))
                return false;
            return word1.EndsWith(word2);
        } 
        
        
        
        #region Vowels and Consonants

        public static bool IsVowel(char c)
        {
            return VOWELS.Contains(Char.ToLower(c));
        }
        
        public static bool IsConsonant(char c)
        {
            return Char.IsLetter(c) && !IsVowel(c);
        }
        
        #endregion
        
        #region Tables

        public static string[,] ReadTable(string data, string rowDelimiter = "\n", string cellDelimiter = ";")
        {
            string[][] tableData = ReadIrregularTable(data, rowDelimiter, cellDelimiter);
            int width = tableData.Count();
            int height = tableData.Select(line => line.Count()).Min();
            string[,] output = new string[width, height];
            for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                output[x, y] = tableData.ElementAt(x).ElementAt(y);
            return output;
        }

        public static string[][] ReadIrregularTable(string data, string delimiter1, string delimiter2)
        {
            return data.Split(delimiter1, StringSplitOptions.RemoveEmptyEntries).Select(
                a => a.Split(delimiter2, StringSplitOptions.RemoveEmptyEntries)).ToArray();
        }

        public static string[][][] ReadIrregularTable(string data, string delimiter1, string delimiter2,
            string delimiter3)
        {
            return data.Split(delimiter1, StringSplitOptions.RemoveEmptyEntries).Select(
                a => a.Split(delimiter2, StringSplitOptions.RemoveEmptyEntries).Select(
                    b => b.Split(delimiter3, StringSplitOptions.RemoveEmptyEntries)).ToArray()).ToArray();
        }
        
        #endregion
    }
}