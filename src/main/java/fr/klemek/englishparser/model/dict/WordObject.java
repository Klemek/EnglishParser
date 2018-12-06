package fr.klemek.englishparser.model.dict;

import fr.klemek.englishparser.model.DatabaseObject;
import fr.klemek.englishparser.utils.DatabaseManager;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class WordObject extends DatabaseObject {

    @Id
    @Column(name = "base")
    private String base;

    WordObject() {
    }

    WordObject(String base) {
        this.base = base;
    }

    public String getBase() {
        return base;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WordObject w = (WordObject) o;
        return Objects.equals(base, w.base);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base);
    }

    static <T> T getByWord(Class<T> objectClass, String word) {
        return DatabaseManager.getFirstFromSessionQuery("FROM " + objectClass.getSimpleName() + " WHERE base = ?0", word);
    }

    @Override
    public String toString() {
        return "WordObject{'" + base + "'}";
    }
}
