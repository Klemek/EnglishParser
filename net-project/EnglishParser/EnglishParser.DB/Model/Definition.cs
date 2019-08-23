using System;
using System.Collections.Generic;

namespace EnglishParser.DB.Model
{
    public class Definition
    {
        public Definition()
        {
        }

        public Definition(int synSetId, string text)
        {
            SynSetId = synSetId;
            Text = text ?? throw new ArgumentNullException(nameof(text));
        }

        public int SynSetId { get; set; }
        public string Text { get; set; }
        
        public IEnumerable<Word> Synonyms { get; set; }
    }
}