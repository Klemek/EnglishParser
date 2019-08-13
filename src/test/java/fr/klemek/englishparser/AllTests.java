package fr.klemek.englishparser;

import fr.klemek.englishparser.model.dict.*;
import fr.klemek.englishparser.utils.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        UtilsTest.class, ConfigTest.class, StringUtilsTest.class, FileUtilsTest.class, HttpUtilsTest.class,
        DatabaseManagerTest.class, DatabaseManagerErrorsTest.class,
        WordObjectTest.class, WordTest.class, NounTest.class, VerbTest.class, AdjectiveTest.class,
        DictionaryManagerTest.class
})
public class AllTests {

}
