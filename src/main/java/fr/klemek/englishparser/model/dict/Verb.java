package fr.klemek.englishparser.model.dict;

import fr.klemek.englishparser.model.DatabaseObject;
import fr.klemek.englishparser.utils.DatabaseManager;

import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "dict_verb")
public class Verb extends WordObject {

    @Column(name = "past_tense")
    private String pastTense;
    @Column(name = "past_part")
    private String pastParticiple;
    @Column(name = "pres_part")
    private String presentParticiple;
    @Column(name = "third_pers")
    private String thirdPerson;

    public Verb() {
    }

    public Verb(String base, String pastTense, String pastParticiple, String presentParticiple, String thirdPerson) {
        super(base);
        this.pastTense = pastTense;
        this.pastParticiple = pastParticiple;
        this.presentParticiple = presentParticiple;
        this.thirdPerson = thirdPerson;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Verb verb = (Verb) o;
        return Objects.equals(pastTense, verb.pastTense) &&
                Objects.equals(pastParticiple, verb.pastParticiple) &&
                Objects.equals(presentParticiple, verb.presentParticiple) &&
                Objects.equals(thirdPerson, verb.thirdPerson);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), pastTense, pastParticiple, presentParticiple, thirdPerson);
    }

    public static List<Verb> getAll() {
        return DatabaseObject.getAll(Verb.class);
    }

    public static Verb getByWord(String word) {
        return WordObject.getByWord(Verb.class, word);
    }

    public static List<Verb> searchByWord(String word) {
        return DatabaseManager.getRowsFromSessionQuery("FROM Verb WHERE base = ?0 OR past_tense = ?0 OR past_part = ?0 OR pres_part = ?0 OR third_pers = ?0", word);
    }

    @Override
    public String toString() {
        return "Verb{" +
                "base='" + getBase() + '\'' +
                ", pastTense='" + pastTense + '\'' +
                ", pastParticiple='" + pastParticiple + '\'' +
                ", presentParticiple='" + presentParticiple + '\'' +
                ", thirdPerson='" + thirdPerson + '\'' +
                '}';
    }
}
