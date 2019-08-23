using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace EnglishParser.DB.Model
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
    }
}