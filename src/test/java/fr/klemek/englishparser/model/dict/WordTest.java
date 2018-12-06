package fr.klemek.englishparser.model.dict;

import fr.klemek.englishparser.TestUtils;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class WordTest {

    private Word word;

    @Before
    public void setUp() throws Exception {
        TestUtils.initTest(false, true);

        word = new Word("test", Word.Type.NOUN, 1234, 1);
        assertTrue(word.save());
    }

    @Test
    public void getDefinition() {
        assertNull(word.getDefinition());

        Definition def = new Definition(word.getSynSetID(), "is a test");
        assertTrue(def.save());

        Definition def2 = word.getDefinition();
        assertEquals(def, def2);
    }

    @Test
    public void getSynonyms() {
        List<Word> syns = word.getSynonyms();
        assertEquals(1, syns.size());
        assertEquals(word, syns.get(0));

        Word word2 = new Word("test0", Word.Type.NOUN, word.getSynSetID(), 0);
        assertTrue(word2.save());
        Word word3 = new Word("test2", Word.Type.NOUN, word.getSynSetID(), 2);
        assertTrue(word3.save());

        syns = word.getSynonyms();
        assertEquals(3, syns.size());
        assertEquals(word2, syns.get(0));
        assertEquals(word, syns.get(1));
        assertEquals(word3, syns.get(2));
    }

    @Test
    public void getByWord() {
        List<Word> lst = Word.getByWord("test2");
        assertEquals(0, lst.size());

        lst = Word.getByWord("test");
        assertEquals(1, lst.size());
        assertEquals(word, lst.get(0));

        Word word2 = new Word("test", Word.Type.NOUN, 1364, 0);
        assertTrue(word2.save());
        Word word3 = new Word("test", Word.Type.ADJ, 1235, 2);
        assertTrue(word3.save());

        lst = Word.getByWord("test");
        assertEquals(3, lst.size());
        assertEquals(word2, lst.get(0));
        assertEquals(word, lst.get(1));
        assertEquals(word3, lst.get(2));
    }

    @Test
    public void getByWordAndType() {
        List<Word> lst = Word.getByWord("test", Word.Type.ADJ);
        assertEquals(0, lst.size());

        lst = Word.getByWord("test", Word.Type.NOUN);
        assertEquals(1, lst.size());
        assertEquals(word, lst.get(0));

        Word word2 = new Word("test", Word.Type.NOUN, 1364, 0);
        assertTrue(word2.save());
        Word word3 = new Word("test", Word.Type.ADJ, 1235, 2);
        assertTrue(word3.save());

        lst = Word.getByWord("test", Word.Type.NOUN);
        assertEquals(2, lst.size());
        assertEquals(word2, lst.get(0));
        assertEquals(word, lst.get(1));
    }
}