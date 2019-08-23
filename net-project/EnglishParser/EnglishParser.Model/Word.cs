using System;

namespace EnglishParser.Model
{
    public class Word
    {
        public Word()
        {
        }

        public Word(int synSetId, int wordNumber, string text, int type)
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
    }
}