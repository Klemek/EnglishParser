using System;

namespace EnglishParser.Model
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