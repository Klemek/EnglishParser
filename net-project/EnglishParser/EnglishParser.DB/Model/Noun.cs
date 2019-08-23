using System;
using System.ComponentModel.DataAnnotations;

namespace EnglishParser.DB.Model
{
    public class Noun : WordObject
    {
        public Noun()
        {
        }

        public Noun(string @base, string plural, bool male, string female, string femalePlural) : base(@base)
        {
            Plural = plural ?? throw new ArgumentNullException(nameof(plural));
            Male = male;
            Female = female ?? throw new ArgumentNullException(nameof(female));
            FemalePlural = femalePlural ?? throw new ArgumentNullException(nameof(femalePlural));
        }
        
        public string Plural { get; set; }
        public bool Male { get; set; }
        public string Female { get; set; }
        public string FemalePlural { get; set; }
    }
}