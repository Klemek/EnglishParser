using System;

namespace EnglishParser.Model
{
    public class Noun : WordObject
    {
        public Noun()
        {
        }

        public Noun(string @base, string plural) : base(@base)
        {
            Plural = plural ?? throw new ArgumentNullException(nameof(plural));
        }

        public Noun(string @base, string plural, string female, string femalePlural) : this(@base, plural)
        {
            SetFemale(female, femalePlural);
        }

        public string Plural { get; set; }
        public string Female { get; set; }
        public string FemalePlural { get; set; }

        public bool Proper => Char.IsUpper(Base[0]);

        public void SetFemale(string female, string femalePlural)
        {
            Female = female;
            FemalePlural = femalePlural;
        }
    }
}