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

        public bool Proper => char.IsUpper(Base[0]);

        public void SetFemale(string female, string femalePlural)
        {
            Female = female;
            FemalePlural = femalePlural;
        }

        protected bool Equals(Noun other)
        {
            return base.Equals(other) && Plural == other.Plural && Female == other.Female && FemalePlural == other.FemalePlural;
        }

        public override bool Equals(object obj)
        {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            if (obj.GetType() != this.GetType()) return false;
            return Equals((Noun) obj);
        }

        public override int GetHashCode()
        {
            unchecked
            {
                int hashCode = base.GetHashCode();
                hashCode = (hashCode * 397) ^ (Plural != null ? Plural.GetHashCode() : 0);
                hashCode = (hashCode * 397) ^ (Female != null ? Female.GetHashCode() : 0);
                hashCode = (hashCode * 397) ^ (FemalePlural != null ? FemalePlural.GetHashCode() : 0);
                return hashCode;
            }
        }
    }
}