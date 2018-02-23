package ranalyzer.model;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class ClassDiagram implements Serializable {
    private List<UmlClass> classes = new ArrayList<>();
    private List<UmlRelation> relations = new ArrayList<>();

    public String toPrettyString() {
        StringBuilder sb = new StringBuilder();
        for (UmlClass i : classes) {
            sb.append(i.toString());
            sb.append('\n');
        }
        for (UmlRelation i : relations) {
            sb.append(i.toString());
            sb.append('\n');
        }
        return sb.toString();
    }
}
