using System;
using System.IO;
using System.Linq;
using System.Reflection;

namespace EnglishParser.Utils
{
    public static class FileUtils
    {
        private static string GetResourceName(Assembly assembly, string filePath)
        {
            return assembly.GetName().Name + ".Resources." + filePath.Replace("/",".");
        }
        
        public static string ReadResource(string filePath)
        {
            Assembly assembly = Assembly.GetCallingAssembly();
            string resourceName = GetResourceName(assembly, filePath);
            using (Stream stream = assembly.GetManifestResourceStream(resourceName))
            using (StreamReader reader = new StreamReader(stream ?? throw new FileNotFoundException("Resource \""+resourceName+"\" not found")))
            {
                return reader.ReadToEnd();
            }
        }
        
        public static void ReadResource(Assembly assembly, string filePath, Action<string> readLine)
        {
            string resourceName = GetResourceName(assembly, filePath);
            using (Stream stream = assembly.GetManifestResourceStream(resourceName))
            using (StreamReader reader = new StreamReader(stream ?? throw new FileNotFoundException("Resource \""+resourceName+"\" not found")))
            {
                string line;
                while ((line = reader.ReadLine()) != null)
                {
                    readLine(line);
                }
            }
        }
    }
}