package fr.klemek.englishparser.model.dict;

import fr.klemek.englishparser.TestUtils;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WordObjectTest {

    @Before
    public void setUp() throws Exception {
        TestUtils.initTest(false, true);
    }

    @Test
    public void word() {
        Word word = new Word("test", Word.Type.NOUN, 1234, 1);
        assertTrue(word.save());

        word = new Word("test2", Word.Type.NOUN, 1234, 1);
        assertTrue(word.update());

        assertTrue(word.delete());
    }

    @Test
    public void definition() {
        Word word = new Word("test", Word.Type.NOUN, 1234, 1);
        assertTrue(word.save());

        Definition def = new Definition(word.getSynSetID(), "is a test");
        assertTrue(def.save());

        def = new Definition(word.getSynSetID(), "is a test 2");
        assertTrue(def.update());

        assertTrue(def.delete());
    }

    @Test
    public void noun() {
        Noun noun = new Noun("test", "test");
        assertTrue(noun.save());

        noun.setFemale("test", "test");
        assertTrue(noun.update());

        assertTrue(noun.delete());
    }

    @Test
    public void verb() {
        Verb verb = new Verb("test", "test", "test", "test", "test");
        assertTrue(verb.save());

        assertTrue(verb.delete());
    }

    @Test
    public void adjective() {
        Adjective adj = new Adjective("test", "test");
        assertTrue(adj.save());
        assertTrue(adj.delete());
    }

    @Test
    public void getAll() {
        Word word = new Word("test", Word.Type.NOUN, 1234, 1);
        assertTrue(word.save());

        List<Word> all = Word.getAll();
        assertEquals(1, all.size());
        assertTrue(all.contains(word));

        Word word2 = new Word("test0", Word.Type.NOUN, word.getSynSetID(), 0);
        assertTrue(word2.save());

        all = Word.getAll();
        assertEquals(2, all.size());
        assertTrue(all.contains(word));
        assertTrue(all.contains(word2));

        word.delete();
        word2.delete();

        all = Word.getAll();
        assertEquals(0, all.size());
    }
}