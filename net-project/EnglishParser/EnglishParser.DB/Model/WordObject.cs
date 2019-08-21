using System;

namespace EnglishParser.Core.Model
{
    public abstract class WordObject
    {
        public string Base { get; set; }

        protected WordObject()
        {
        }

        protected WordObject(string @base)
        {
            Base = @base ?? throw new ArgumentNullException(nameof(@base));
        }
    }
}