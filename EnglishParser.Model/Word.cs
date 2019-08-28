using System;
using System.Collections.Generic;
using EnglishParser.Utils;

namespace EnglishParser.Model
{
    public class Word
    {
        public enum WordType
        {
            Undef = -1,
            Noun = 0,
            Verb = 1,
            Adjective = 2,
            Adverb = 3
        }

        public Word()
        {
        }

        public Word(string text, int type, int synSetId, int wordNumber)
        {
            SynSetId = synSetId;
            WordNumber = wordNumber;
            Text = text ?? throw new ArgumentNullException(nameof(text));
            Type = type;
        }

        public int SynSetId { get; set; }
        public int WordNumber { get; set; }
        public string Text { get; set; }
        public int Type { get; set; }

        public Definition Definition { get; set; }

        public List<Word> Synonyms => Definition.Synonyms;

        public override string ToString()
        {
            return StringUtils.ToString(this);
        }
    }
}