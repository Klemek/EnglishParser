using System;

namespace EnglishParser.Core.Model
{
    public class Verb : WordObject
    {
        public string PastTense { get; set; }
        public string PastParticiple { get; set; }
        public string PresParticiple { get; set; }
        public string ThirdPerson { get; set; }

        public Verb()
        {
        }

        public Verb(string @base, string pastTense, string pastParticiple, string presParticiple,
            string thirdPerson) : base(@base)
        {
            PastTense = pastTense ?? throw new ArgumentNullException(nameof(pastTense));
            PastParticiple = pastParticiple ?? throw new ArgumentNullException(nameof(pastParticiple));
            PresParticiple = presParticiple ?? throw new ArgumentNullException(nameof(presParticiple));
            ThirdPerson = thirdPerson ?? throw new ArgumentNullException(nameof(thirdPerson));
        }
    }
}