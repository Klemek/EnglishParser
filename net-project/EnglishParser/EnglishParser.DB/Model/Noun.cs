using System;

namespace EnglishParser.Core.Model
{
    public class Noun : WordObject
    {
         public string Plural { get; set; }
         public string Male { get; set; }
         public string Female { get; set; }
         public string FemalePlural { get; set; }

         public Noun()
         {
         }

         public Noun(string @base, string plural, string male, string female, string femalePlural) : base(@base)
         {
             Plural = plural ?? throw new ArgumentNullException(nameof(plural));
             Male = male ?? throw new ArgumentNullException(nameof(male));
             Female = female ?? throw new ArgumentNullException(nameof(female));
             FemalePlural = femalePlural ?? throw new ArgumentNullException(nameof(femalePlural));
         }
    }
}