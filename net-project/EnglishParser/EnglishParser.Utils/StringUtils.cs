using System;
using System.Linq;

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

        public static string[,] ReadTable(string data, string cellDelimiter = ";", string rowDelimiter = "\n")
        {
            string[][] tableData = data.Split(rowDelimiter, StringSplitOptions.RemoveEmptyEntries)
                .Select(line => line.Split(cellDelimiter)).ToArray();
            int width = tableData.Count();
            int height = tableData.Select(line => line.Count()).Min();
            string[,] output = new string[width, height];
            for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                output[x, y] = tableData.ElementAt(x).ElementAt(y);
            return output;
        }
    }
}