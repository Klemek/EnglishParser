package fr.klemek.englishparser.model.dict;

import fr.klemek.englishparser.TestUtils;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AdjectiveTest {

    private Adjective adj;

    @Before
    public void setUp() throws Exception {
        TestUtils.initTest(false, true);

        adj = new Adjective("test", "testa");
        assertTrue(adj.save());
    }

    @Test
    public void getByWord() {
        assertNull(Adjective.getByWord("test2"));
        assertEquals(adj, Adjective.getByWord("test"));
    }

    @Test
    public void searchByWord() {
        List<Adjective> adjs = Adjective.searchByWord("test2");
        assertEquals(0, adjs.size());

        Adjective adj2 = new Adjective("test2", "test");
        assertTrue(adj2.save());

        Adjective adj3 = new Adjective("test3", "test3a");
        assertTrue(adj3.save());

        adjs = Adjective.searchByWord("test");
        assertEquals(2, adjs.size());
        assertTrue(adjs.contains(adj));
        assertTrue(adjs.contains(adj2));
        assertFalse(adjs.contains(adj3));
    }
}