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
@Table(name = "dict_noun")
public class Noun extends WordObject {

    @Id
    @Column(name = "plural")
    private String plural;
    @Column(name = "male")
    private boolean male;
    @Column(name = "female")
    private String female;
    @Column(name = "female_plural")
    private String femalePlural;

    public Noun() {
    }

    public Noun(String base, String plural) {
        super(base);
        this.plural = plural;
    }

    public Noun(String base, String plural, String female, String femalePlural) {
        this(base, plural);
        this.setFemale(female, femalePlural);
    }

    public void setFemale(String female, String femalePlural) {
        this.male = female != null;
        this.female = female;
        this.femalePlural = femalePlural;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Noun noun = (Noun) o;
        return male == noun.male &&
                Objects.equals(plural, noun.plural) &&
                Objects.equals(female, noun.female) &&
                Objects.equals(femalePlural, noun.femalePlural);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), plural, male, female, femalePlural);
    }

    public static List<Noun> getAll() {
        return DatabaseObject.getAll(Noun.class);
    }

    @Override
    public String toString() {
        return "Noun{" +
                "base='" + getBase() + '\'' +
                ", plural='" + plural + '\'' +
                ", male=" + male +
                ", female='" + female + '\'' +
                ", femalePlural='" + femalePlural + '\'' +
                '}';
    }

    public static Noun getByFemale(String word) {
        return DatabaseManager.getFirstFromSessionQuery("FROM Noun WHERE female = ?0", word);
    }

    public static Noun getByWord(String word) {
        return DatabaseManager.getFirstFromSessionQuery("FROM Noun WHERE base = ?0 OR plural = ?0", word);
    }

    public static List<Noun> searchByWord(String word) {
        return DatabaseManager.getRowsFromSessionQuery("FROM Noun WHERE base = ?0 OR plural = ?0 OR (male = 1 AND (female = ?0 OR female_plural = ?0))", word);
    }
}
