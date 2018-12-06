package fr.klemek.englishparser.model.dict;

import fr.klemek.englishparser.model.DatabaseObject;
import fr.klemek.englishparser.utils.DatabaseManager;

import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "dict_word")
public class Word extends DatabaseObject {

    @Id
    @Column(name = "syn_set_id")
    private int synSetID;
    @Id
    @Column(name = "word_num")
    private int wordNumber;
    @Column(name = "word")
    private String word;
    @Column(name = "type")
    private int type;

    public Word() {
    }

    public Word(String word, Type type, int synSetID, int wordNumber) {
        this.word = word;
        this.type = type.value;
        this.synSetID = synSetID;
        this.wordNumber = wordNumber;
    }

    public String getWord() {
        return word;
    }

    public Type getType() {
        return Word.parseType(this.type);
    }

    public int getSynSetID() {
        return synSetID;
    }

    public int getWordNumber() {
        return wordNumber;
    }

    public Definition getDefinition() {
        return DatabaseManager.getFirstFromSessionQuery("FROM Definition WHERE syn_set_id = ?0", synSetID);
    }

    public List<Word> getSynonyms() {
        return DatabaseManager.getRowsFromSessionQuery("FROM Word WHERE syn_set_id = ?0 ORDER BY word_num ASC", synSetID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word = (Word) o;
        return synSetID == word.synSetID &&
                wordNumber == word.wordNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(synSetID, wordNumber);
    }

    @Override
    public String toString() {
        return "Word{" +
                "synSetID=" + synSetID +
                ", wordNumber=" + wordNumber +
                ", word='" + word + '\'' +
                ", type=" + type +
                '}';
    }

    private static Type parseType(int type) {
        for (Type t : Type.values())
            if (t.value == type)
                return t;
        return Type.UNDEF;
    }

    public static List<Word> getAll() {
        return DatabaseObject.getAll(Word.class);
    }

    public static List<Word> getByWord(String word) {
        return DatabaseManager.getRowsFromSessionQuery("FROM Word WHERE word = ?0 ORDER BY word_num ASC", word);
    }

    public static List<Word> getByWord(String word, Type type) {
        return DatabaseManager.getRowsFromSessionQuery("FROM Word WHERE word = ?0 AND type = ?1 ORDER BY word_num ASC", word, type.value);
    }

    public enum Type {
        UNDEF(-1),
        NOUN(0),
        VERB(1),
        ADJ(2),
        ADV(3);
        final int value;

        Type(int value) {
            this.value = value;
        }
    }
}
