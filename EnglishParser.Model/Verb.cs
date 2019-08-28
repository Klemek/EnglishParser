using System;

namespace EnglishParser.Model
{
    public class Verb : WordObject
    {
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

        public string PastTense { get; set; }
        public string PastParticiple { get; set; }
        public string PresParticiple { get; set; }
        public string ThirdPerson { get; set; }

        protected bool Equals(Verb other)
        {
            return base.Equals(other) && PastTense == other.PastTense && PastParticiple == other.PastParticiple && PresParticiple == other.PresParticiple && ThirdPerson == other.ThirdPerson;
        }

        public override bool Equals(object obj)
        {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            if (obj.GetType() != this.GetType()) return false;
            return Equals((Verb) obj);
        }

        public override int GetHashCode()
        {
            unchecked
            {
                int hashCode = base.GetHashCode();
                hashCode = (hashCode * 397) ^ (PastTense != null ? PastTense.GetHashCode() : 0);
                hashCode = (hashCode * 397) ^ (PastParticiple != null ? PastParticiple.GetHashCode() : 0);
                hashCode = (hashCode * 397) ^ (PresParticiple != null ? PresParticiple.GetHashCode() : 0);
                hashCode = (hashCode * 397) ^ (ThirdPerson != null ? ThirdPerson.GetHashCode() : 0);
                return hashCode;
            }
        }
    }
}