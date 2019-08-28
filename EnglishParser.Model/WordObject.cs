using System;
using EnglishParser.Utils;

namespace EnglishParser.Model
{
    public abstract class WordObject
    {
        protected WordObject()
        {
        }

        protected WordObject(string @base)
        {
            Base = @base ?? throw new ArgumentNullException(nameof(@base));
        }

        public string Base { get; set; }

        public override string ToString()
        {
            return StringUtils.ToString(this);
        }

        protected bool Equals(WordObject other)
        {
            return Base == other.Base;
        }

        public override bool Equals(object obj)
        {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            if (obj.GetType() != this.GetType()) return false;
            return Equals((WordObject) obj);
        }

        public override int GetHashCode()
        {
            return (Base != null ? Base.GetHashCode() : 0);
        }
    }
}