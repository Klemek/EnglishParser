using System;
using System.IO;
using System.Linq;
using System.Reflection;

namespace EnglishParser.Utils
{
    public static class FileUtils
    {
        public static string ReadResource(string resourceName)
        {
            Assembly assembly = Assembly.GetCallingAssembly();
            resourceName = assembly.GetName().Name + ".Resources." + resourceName;
            using (Stream stream = assembly.GetManifestResourceStream(resourceName))
            using (StreamReader reader = new StreamReader(stream ?? throw new FileNotFoundException("Resource \""+resourceName+"\" not found")))
            {
                return reader.ReadToEnd();
            }
        }
    }
}