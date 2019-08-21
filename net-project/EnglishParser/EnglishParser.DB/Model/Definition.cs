using System;

namespace EnglishParser.Core.Model
{
    public class Definition
    {
        public int SynSetId { get; set; }
        public string Text { get; set; }

        public Definition()
        {
        }

        public Definition(int synSetId, string text)
        {
            SynSetId = synSetId;
            Text = text ?? throw new ArgumentNullException(nameof(text));
        }
    }
}