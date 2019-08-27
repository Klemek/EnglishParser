using System;
using System.Linq;
using System.Reflection;
using System.Text;

namespace EnglishParser.Utils
{
    public static class StringUtils
    {
        public static string PadLeft(string src, string padding, int len)
        {
            StringBuilder sb = new StringBuilder(src);
            while (sb.Length < len)
                sb.Insert(0, padding);
            return sb.ToString();
        }

        public static bool PartOf(string word1, string word2, int minLen, params string[] excluded)
        {
            if (word1.Equals(word2))
                return true;
            if (word2.Length < minLen && !excluded.Contains(word2))
                return false;
            return word1.EndsWith(word2);
        }

        public static bool PartOf(string word1, string word2, char separator)
        {
            int i = word1.LastIndexOf(separator);
            if (i < 0)
                return false;
            return word1.Substring(i + 1).Equals(word2);
        }

        #region Vowels and Consonants

        private static readonly char[] VOWELS = {'a', 'e', 'i', 'o', 'u', 'y'};

        public static bool IsVowel(char c)
        {
            return VOWELS.Contains(char.ToLower(c));
        }

        public static bool IsConsonant(char c)
        {
            return char.IsLetter(c) && !IsVowel(c);
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

        #region Formatting

        public static string Ellipsis(string str, int maxLen)
        {
            if (str == null || str.Length <= maxLen)
                return str;
            return str.Substring(0, maxLen - 3) + "...";
        }

        public static string DumpString(string str)
        {
            if (str == null)
                return "null";
            str = str.Replace("\n", "\\n")
                .Replace("\t", "\\t")
                .Replace("\r", "\\r");
            return "'" + Ellipsis(str, 15) + "'";
        }

        public static string ToString(object obj)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(obj.GetType().Name);
            sb.Append("{");
            foreach (FieldInfo field in obj.GetType().GetFields())
                if (field.FieldType == typeof(string))
                    sb.AppendFormat("{0}: {1}, ", field.Name, DumpString((string) field.GetValue(obj)));
                else
                    sb.AppendFormat("{0}: {1}, ", field.Name, field.GetValue(obj));

            if (sb[sb.Length - 1] != '{')
                sb.Remove(sb.Length - 2, 2);
            sb.Append("}");
            return sb.ToString();
        }

        #endregion
    }
}