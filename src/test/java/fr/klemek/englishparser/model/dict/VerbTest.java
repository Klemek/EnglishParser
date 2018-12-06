package fr.klemek.englishparser.model.dict;

import fr.klemek.englishparser.TestUtils;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class VerbTest {

    private Verb verb;

    @Before
    public void setUp() throws Exception {
        TestUtils.initTest(false, true);

        verb = new Verb("test", "testpt", "testpap", "testprp", "testtp");
        assertTrue(verb.save());
    }

    @Test
    public void getByWord() {
        assertNull(Verb.getByWord("test2"));
        assertEquals(verb, Verb.getByWord("test"));
    }

    @Test
    public void searchByWord() {
        List<Verb> verbs = Verb.searchByWord("test2");
        assertEquals(0, verbs.size());

        Verb verb2 = new Verb("test2", "test", "test2pap", "test2prp", "test2tp");
        assertTrue(verb2.save());

        Verb verb3 = new Verb("test3", "test3pt", "test", "test3prp", "test3tp");
        assertTrue(verb3.save());

        Verb verb4 = new Verb("test4", "test4pt", "test4pap", "test", "test4tp");
        assertTrue(verb4.save());

        Verb verb5 = new Verb("test5", "test5pt", "test5pap", "test5prp", "test");
        assertTrue(verb5.save());

        Verb verb6 = new Verb("test6", "test6pt", "test6pap", "test6prp", "test6tp");
        assertTrue(verb6.save());

        verbs = Verb.searchByWord("test");
        assertEquals(5, verbs.size());
        assertTrue(verbs.contains(verb));
        assertTrue(verbs.contains(verb2));
        assertTrue(verbs.contains(verb3));
        assertTrue(verbs.contains(verb4));
        assertTrue(verbs.contains(verb5));
        assertFalse(verbs.contains(verb6));
    }
}