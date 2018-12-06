package fr.klemek.englishparser.model.dict;

import fr.klemek.englishparser.TestUtils;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class NounTest {

    private Noun noun;

    @Before
    public void setUp() throws Exception {
        TestUtils.initTest(false, true);

        noun = new Noun("test", "tests");
        assertTrue(noun.save());
    }

    @Test
    public void getByWord() {
        assertNull(Noun.getByWord("test2"));

        assertEquals(noun, Noun.getByWord("test"));
        assertEquals(noun, Noun.getByWord("tests"));
    }

    @Test
    public void getByFemale() {
        assertNull(Noun.getByFemale("test"));

        noun.setFemale("ftest", "ftests");
        assertTrue(noun.update());

        assertEquals(noun, Noun.getByFemale("ftest"));
    }

    @Test
    public void searchByWord() {
        List<Noun> nouns = Noun.searchByWord("test2");
        assertEquals(0, nouns.size());

        Noun noun2 = new Noun("test2", "test");
        assertTrue(noun2.save());

        Noun noun3 = new Noun("test3", "test3s", "test", "test3fs");
        assertTrue(noun3.save());

        Noun noun4 = new Noun("test4", "test4s", "test4f", "test");
        assertTrue(noun4.save());

        Noun noun5 = new Noun("test5", "test5s", "test5f", "test5fs");
        assertTrue(noun5.save());

        nouns = Noun.searchByWord("test");
        assertEquals(4, nouns.size());
        assertTrue(nouns.contains(noun));
        assertTrue(nouns.contains(noun2));
        assertTrue(nouns.contains(noun3));
        assertTrue(nouns.contains(noun4));
        assertFalse(nouns.contains(noun5));
    }
}