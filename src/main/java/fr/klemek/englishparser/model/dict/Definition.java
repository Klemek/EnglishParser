package fr.klemek.englishparser.model.dict;

import fr.klemek.englishparser.model.DatabaseObject;

import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "dict_def")
public class Definition extends DatabaseObject {

    @Id
    @Column(name = "syn_set_id")
    private int synSetID;
    @Column(name = "definition")
    private String definition;

    public Definition() {
    }

    public Definition(int synSetID, String definition) {
        this.synSetID = synSetID;
        this.definition = definition;
    }

    public int getSynSetID() {
        return synSetID;
    }

    public String getDefinition() {
        return definition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Definition that = (Definition) o;
        return synSetID == that.synSetID;
    }

    @Override
    public int hashCode() {

        return Objects.hash(synSetID);
    }

    public static List<Definition> getAll() {
        return DatabaseObject.getAll(Definition.class);
    }

    @Override
    public String toString() {
        return "Definition{" +
                "synSetID=" + synSetID +
                ", definition='" + definition + '\'' +
                '}';
    }
}
