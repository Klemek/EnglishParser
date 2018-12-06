package fr.klemek.englishparser;

import fr.klemek.englishparser.model.dict.AdjectiveTest;
import fr.klemek.englishparser.model.dict.NounTest;
import fr.klemek.englishparser.model.dict.VerbTest;
import fr.klemek.englishparser.model.dict.WordObjectTest;
import fr.klemek.englishparser.model.dict.WordTest;
import fr.klemek.englishparser.utils.DatabaseManagerErrorsTest;
import fr.klemek.englishparser.utils.DatabaseManagerTest;
import fr.klemek.englishparser.utils.DictionaryManagerTest;
import fr.klemek.englishparser.utils.HttpUtilsTest;
import fr.klemek.englishparser.utils.UtilsTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        UtilsTest.class, HttpUtilsTest.class,
        DatabaseManagerTest.class, DatabaseManagerErrorsTest.class,
        WordObjectTest.class, WordTest.class, NounTest.class, VerbTest.class, AdjectiveTest.class,
        DictionaryManagerTest.class
})
public class AllTests {

}
