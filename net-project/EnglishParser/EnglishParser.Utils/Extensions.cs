using System.Collections;
using System.Collections.Generic;

namespace EnglishParser.Utils
{
    public static class Extensions
    {
        #region List
        
        public static void AddIfNotNull<T>(this IList<T> list, T value)
        {
            if(value != null)
                list.Add(value);
        }
        
        #endregion
    }
}