using System;

namespace EnglishParser.Core.Model
{
    public class Adjective : WordObject

    {
        public string Adverb { get; set; }

        public Adjective()
        {
        }

        public Adjective(string @base, string adverb) : base(@base)
        {
            Adverb = adverb ?? throw new ArgumentNullException(nameof(adverb));
        }
    }
}