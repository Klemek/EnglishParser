using System.IO;
using System.Net.Mime;
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

        private static Stream GetStream(Assembly assembly, string filePath)
        {
            string resourceName = GetResourceName(assembly, filePath);
            string pathName = Path.Combine(Directory.GetCurrentDirectory(), "Resources", filePath);
            Stream stream;
            try
            {
                stream = File.OpenRead(pathName);
            }
            catch (FileNotFoundException)
            {
                stream = assembly.GetManifestResourceStream(resourceName);
            }
            return stream ?? throw new FileNotFoundException($"Resource '{pathName}' or '{resourceName}' not found");
        }

        public static string ReadResource(Assembly assembly, string filePath)
        {
            using (Stream stream = GetStream(assembly, filePath))
            using (StreamReader reader = new StreamReader(stream))
            {
                return reader.ReadToEnd();
            }
        }
    }
}