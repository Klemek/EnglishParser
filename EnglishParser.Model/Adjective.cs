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

        protected bool Equals(Adjective other)
        {
            return base.Equals(other) && Adverb == other.Adverb;
        }

        public override bool Equals(object obj)
        {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            if (obj.GetType() != this.GetType()) return false;
            return Equals((Adjective) obj);
        }

        public override int GetHashCode()
        {
            unchecked
            {
                return (base.GetHashCode() * 397) ^ (Adverb != null ? Adverb.GetHashCode() : 0);
            }
        }
    }
}