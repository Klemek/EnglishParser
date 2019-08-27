using System;
using System.Text;
using EnglishParser.Utils;

namespace EnglishParser.Model
{
    public abstract class WordObject
    {
        protected WordObject()
        {
        }

        protected WordObject(string @base)
        {
            Base = @base ?? throw new ArgumentNullException(nameof(@base));
        }
        
        public string Base { get; set; }

        public override string ToString()
        {
            return StringUtils.ToString(this);
        }
    }
}