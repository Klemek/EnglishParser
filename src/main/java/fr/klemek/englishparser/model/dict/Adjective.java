package fr.klemek.englishparser.model.dict;

import fr.klemek.englishparser.model.DatabaseObject;
import fr.klemek.englishparser.utils.DatabaseManager;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "dict_adj")
public class Adjective extends WordObject {

    @Id
    @Column(name = "adverb")
    private String adverb;

    public Adjective() {
    }

    public Adjective(String base, String adverb) {
        super(base);
        this.adverb = adverb;
    }

    public String getAdverb() {
        return adverb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Adjective adjective = (Adjective) o;
        return Objects.equals(adverb, adjective.adverb);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), adverb);
    }

    public static List<Adjective> getAll() {
        return DatabaseObject.getAll(Adjective.class);
    }

    public static Adjective getByWord(String word) {
        return WordObject.getByWord(Adjective.class, word);
    }

    public static List<Adjective> searchByWord(String word) {
        return DatabaseManager.getRowsFromSessionQuery("FROM Adjective WHERE base = ?0 OR adverb = ?0", word);
    }

    @Override
    public String toString() {
        return "Adjective{" +
                "base='" + getBase() + '\'' +
                ", adverb='" + adverb + '\'' +
                '}';
    }
}
