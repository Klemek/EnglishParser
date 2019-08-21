using System;

namespace EnglishParser.Utils
{
    public static class TimeUtils
    {
        public static long Now()
        {
            return DateTimeOffset.Now.ToUnixTimeMilliseconds();
        }
        
        public static string GetTimeSpent(long t0)
        {
            return GetTimeSpan(TimeUtils.Now() - t0);
        }
        public static string GetTimeSpan(long ms)
        {
            long s = ms / 1000;
            ms %= 1000;
            long m = s / 60;
            s %= 60;
            long h = m / 60;
            m %= 60;
            long d = h / 24;
            h %= 24;
            if (d > 0)
                return $"{d.ToString()} d {StringUtils.PadLeft(h.ToString(), "0", 2)} h";
            if (h > 0)
                return $"{h.ToString()} h {StringUtils.PadLeft(m.ToString(), "0", 2)} m";
            if (m > 0)
                return $"{m.ToString()} m {StringUtils.PadLeft(s.ToString(), "0", 2)} s";
            if (s > 0)
                return $"{s.ToString()} s {StringUtils.PadLeft(ms.ToString(), "0", 3)} ms";
            if (ms > 0)
                return $"{ms.ToString()} ms";
            return "no time";
        }
    }
}