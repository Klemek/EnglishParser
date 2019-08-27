using NUnit.Framework;

namespace EnglishParser.Utils.Tests
{
    public class EnglishUtilsTests
    {
        [Test]
        public void GetNounPlural()
        {
            foreach (string root in new[]
            {
                "file", "centre", "girl", "book", "computer", "ambition", "chief", "spoof", "cliff", "journey", "boy",
                "radio", "stereo", "video"
            })
                Assert.AreEqual(root + "s", EnglishUtils.GetNounPlural(root));
            foreach (string root in new[] {"wash", "box", "match", "glass", "bus", "business", "coach", "peach"})
                Assert.AreEqual(root + "es", EnglishUtils.GetNounPlural(root));
            foreach (string root in new[] {"country", "baby", "body", "memory"})
                Assert.AreEqual(root.Substring(0, root.Length - 1) + "ies", EnglishUtils.GetNounPlural(root));
        }

        [Test]
        public void GetAdverb()
        {
            foreach (string root in new[] {"cheap", "quick", "slow"})
                Assert.AreEqual(root + "ly", EnglishUtils.GetAdverb(root));
            foreach (string root in new[] {"easy", "angry", "happy", "lucky"})
                Assert.AreEqual(root.Substring(0, root.Length - 1) + "ily", EnglishUtils.GetAdverb(root));
            foreach (string root in new[] {"probable", "terrible", "gentle"})
                Assert.AreEqual(root.Substring(0, root.Length - 1) + "y", EnglishUtils.GetAdverb(root));
            foreach (string root in new[] {"basic", "tragic", "economic"})
                Assert.AreEqual(root + "ally", EnglishUtils.GetAdverb(root));
        }

        [Test]
        public void GetVerbForms()
        {
            //long vowel or diphthong followed by a consonant or ending in a consonant cluster
            foreach (string root in new[] {"paint", "claim", "devour", "play", "delight", "clamp", "lacquer"})
            {
                Assert.AreEqual(root + "ed", EnglishUtils.GetRegularPast(root));
                Assert.AreEqual(root + "ing", EnglishUtils.GetPresentParticiple(root));
                Assert.AreEqual(root + "s", EnglishUtils.GetThirdPerson(root));
            }

            //short vowel
            foreach (string root in new[] {"chat", "chop", "compel", "quiz", "squat", "quit", "equal", "whiz"})
            {
                char lastChar = root[root.Length - 1];
                Assert.AreEqual(root + lastChar + "ed", EnglishUtils.GetRegularPast(root));
                Assert.AreEqual(root + lastChar + "ing", EnglishUtils.GetPresentParticiple(root));
                if (!root.Equals("quiz") && !root.Equals("whiz")) //irregular third person
                    Assert.AreEqual(root + "s", EnglishUtils.GetThirdPerson(root));
            }

            //consonant followed by e
            foreach (string root in new[] {"dance", "save", "devote", "evolve", "quote"})
            {
                Assert.AreEqual(root + "d", EnglishUtils.GetRegularPast(root));
                Assert.AreEqual(root.Substring(0, root.Length - 1) + "ing", EnglishUtils.GetPresentParticiple(root));
                Assert.AreEqual(root + "s", EnglishUtils.GetThirdPerson(root));
            }

            //sibilants
            foreach (string root in new[] {"kiss", "bless", "box", "polish", "preach", "bias", "box"})
            {
                Assert.AreEqual(root + "ed", EnglishUtils.GetRegularPast(root));
                Assert.AreEqual(root + "ing", EnglishUtils.GetPresentParticiple(root));
                Assert.AreEqual(root + "es", EnglishUtils.GetThirdPerson(root));
            }

            //consonant followed by y
            foreach (string root in new[] {"comply", "copy", "magnify"})
            {
                Assert.AreEqual(root.Substring(0, root.Length - 1) + "ied", EnglishUtils.GetRegularPast(root));
                Assert.AreEqual(root + "ing", EnglishUtils.GetPresentParticiple(root));
                Assert.AreEqual(root.Substring(0, root.Length - 1) + "ies", EnglishUtils.GetThirdPerson(root));
            }
        }
    }
}