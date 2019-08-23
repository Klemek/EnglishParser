using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace EnglishParser.DB.Model
{
    public class Adjective : WordObject

    {
        public Adjective()
        {
        }

        public Adjective(string @base, string adverb) : base(@base)
        {
            Adverb = adverb ?? throw new ArgumentNullException(nameof(adverb));
        }
        
        public string Adverb { get; set; }
    }
}