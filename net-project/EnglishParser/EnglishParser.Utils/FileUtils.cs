using System;
using System.IO;
using System.Reflection;

namespace EnglishParser.Utils
{
    public static class FileUtils
    {
        private static string GetResourceName(Assembly assembly, string filePath)
        {
            return assembly.GetName().Name + ".Resources." + filePath.Replace("/", ".");
        }

        public static string ReadResource(string filePath)
        {
            return ReadResource(Assembly.GetCallingAssembly(), filePath);
        }
        
        public static string ReadResource(Assembly assembly, string filePath)
        {
            var resourceName = GetResourceName(assembly, filePath);
            using (var stream = assembly.GetManifestResourceStream(resourceName))
            using (var reader =
                new StreamReader(
                    stream ?? throw new FileNotFoundException("Resource \"" + resourceName + "\" not found")))
            {
                return reader.ReadToEnd();
            }
        }
    }
}