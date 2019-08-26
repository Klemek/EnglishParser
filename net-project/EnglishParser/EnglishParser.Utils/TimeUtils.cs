using System;
using System.Collections.Generic;

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
            return GetTimeSpan(Now() - t0);
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

        #region ETA

        public static string GetETA(List<long> timestamps, float step, float total)
        {
            long t0 = timestamps[0];
            int length = timestamps.Count;
            long t2 = Now();
            timestamps.Add(t2);
            float delta = 0;
            float current = step;
            if (length != 1)
            {
                float p2 = current = step * length;
                float sum = 0;
                long t1;
                float p1;
                for (int i = 1; i < length; i++)
                {
                    t1 = timestamps[i];
                    p1 = i * step;
                    sum += ComputeDelta(t0, p1, t1, p2, t2, total);
                }

                delta = sum / length;
            }

            return GetTimeSpan((long) ComputeETA(t0, current, total, delta));
        }

        private static float ComputeETA(long t0, float processed, float total, float delta)
        {
            return (long) ((total - processed) * ((Now() - t0) / processed) + delta);
        }

        private static float ComputeDelta(long t0, float processed1, long t1, float processed2, long t2, float total)
        {
            float x1 = processed1 / total;
            float x2 = processed2 / total;
            float dt1 = t1 - t0;
            float dt2 = t2 - t0;
            return dt2 / (x2 * (x2 - x1)) - dt1 / (x1 * (x2 - x1));
        }

        #endregion
    }
}