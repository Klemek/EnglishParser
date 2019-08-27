namespace EnglishParser.Utils
{
    public static class EnglishUtils
    {
        public static bool ShortVowelConsonant(string word)
        {
            word = word
                .Replace("qui", "qi")
                .Replace("qua", "qa");
            int lp = word.Length - 1;
            return StringUtils.IsConsonant(word[lp]) && StringUtils.IsVowel(word[lp - 1]) &&
                   (lp < 2 || StringUtils.IsConsonant(word[lp - 2]));
        }

        #region Nouns

        public static string GetNounPlural(string noun)
        {
            int lp = noun.Length - 1;
            if (noun.EndsWith("ch") ||
                noun.EndsWith("sh") ||
                noun.EndsWith("zh") ||
                noun.EndsWith('x') ||
                noun.EndsWith('s') ||
                noun.EndsWith('z')) //sibilants
                return noun + "es";
            if (noun.EndsWith('y') && StringUtils.IsConsonant(noun[lp - 1]))
                return noun.Substring(0, lp) + "ies";
            return noun + "s";
        }

        #endregion

        #region Adverbs

        public static string GetAdverb(string adj)
        {
            int lp = adj.Length - 1;
            if (adj.EndsWith('y'))
                return adj.Substring(0, lp) + "ily";
            if (adj.EndsWith("le"))
                return adj.Substring(0, lp) + "y";
            if (adj.EndsWith("ll"))
                return adj + "y";
            if (adj.EndsWith("ic"))
                return adj + "ally";
            return adj + "ly";
        }

        #endregion

        #region Verbs

        public static string GetPresentParticiple(string verb)
        {
            int lp = verb.Length - 1;
            if (verb.EndsWith('x') || verb.EndsWith('s')) // one consonant sibilants
                return verb + "ing";
            if (ShortVowelConsonant(verb)) // short vowel
                return verb + verb[lp] + "ing";
            if (verb.EndsWith('e') && StringUtils.IsConsonant(verb[lp - 1]))
                return verb.Substring(0, lp) + "ing";
            return verb + "ing";
        }

        public static string GetRegularPast(string verb)
        {
            int lp = verb.Length - 1;
            if (verb[lp] == 'y' && StringUtils.IsConsonant(verb[lp - 1]))
                return verb.Substring(0, lp) + "ied";
            if (verb.EndsWith('x') || verb.EndsWith('s')) // one consonant sibilants
                return verb + "ed";
            if (ShortVowelConsonant(verb)) // short vowel
                return verb + verb[lp] + "ed";
            if (verb.EndsWith('e') && StringUtils.IsConsonant(verb[lp - 1]))
                return verb + "d";
            return verb + "ed";
        }

        public static string GetThirdPerson(string verb)
        {
            int lp = verb.Length - 1;
            if (verb.EndsWith('y') && StringUtils.IsConsonant(verb[lp - 1]))
                return verb.Substring(0, lp) + "ies";
            if (verb.EndsWith("ch") ||
                verb.EndsWith("sh") ||
                verb.EndsWith("zh") ||
                verb.EndsWith('x') ||
                verb.EndsWith('s') ||
                verb.EndsWith('z')) //sibilants
                return verb + "es";
            return verb + "s";
        }

        #endregion
    }
}