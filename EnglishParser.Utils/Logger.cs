using System;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using System.Text;
using Nini.Config;

namespace EnglishParser.Utils
{
    public static class Logger
    {
        private static string _filename;
        private static FileStream _stream;
        private static StreamWriter _writer;
        private static IConfig _config;

        public static void Init(IConfig config)
        {
            _config = config;
            _filename = config.GetString("LogFile");
            if (_filename != null)
                WriteLine($"Logs stored at '{Path.GetFullPath(_filename)}'");
        }

        private static string GetCallingName()
        {
            int stack = 3;
            MethodBase method;
            do
            {
                method = new StackTrace().GetFrame(stack).GetMethod();
                if (method.DeclaringType == null)
                    return $"unknown.{method.Name}";
                stack += 2; // nested methods used as () => {} inside another
            } while (method.DeclaringType.IsNested);

            return $"{method.DeclaringType.FullName}.{method.Name}";
        }

        private static void LogFile(string format, object[] args)
        {
            if (_filename == null)
                return;
            try
            {
                if (_stream == null || _writer == null)
                {
                    _stream = File.Open(_filename, _config.GetBoolean("Append") ? FileMode.Append : FileMode.Create);
                    _writer = new StreamWriter(_stream, Encoding.UTF8) {AutoFlush = true};
                    AppDomain.CurrentDomain.ProcessExit += OnProcessExit;
                }

                _writer.WriteLine(
                    $"\r[{DateTime.Now:yyyy-MM-dd HH:mm:ss.fff}][{GetCallingName()}] {string.Format(format.Replace("\r",""), args)}");
            }
            catch (Exception)
            {
                // ignored
            }
        }

        private static void OnProcessExit(object sender, EventArgs e)
        {
            try
            {
                _writer.Close();
            }
            catch (Exception)
            {
                // ignored
            }

            try
            {
                _stream.Close();
            }
            catch (Exception)
            {
                // ignored
            }
        }

        public static void Write(string format, params object[] args)
        {
            LogFile(format, args);
            Console.Out.Write(format, args);
        }

        public static void WriteLine(string format, params object[] args)
        {
            LogFile(format, args);
            Console.Out.WriteLine(format, args);
        }
    }
}