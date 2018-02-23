package ranalyzer.model;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

@Data
public class UmlClass implements Serializable {
    private String name;
    private List<String> attributes;
    private List<String> methods;

    public UmlClass() {
        attributes = new LinkedList<>();
        methods = new LinkedList<>();
    }

    // convert class to string used for text-mining purpose
    public String toMiningString() {
        StringBuilder sb = new StringBuilder();
        // assume that user use camel case when typing the class
        sb.append(splitCamelCase(name)).append(' ');
        for (String i : attributes) sb.append(splitCamelCase(i)).append(' ');
        for (String i : methods) sb.append(splitCamelCase(i)).append(' ');
        return sb.toString().trim();
    }

    private StringBuilder splitCamelCase(String word) {
        StringBuilder sb = new StringBuilder();
        for (char c : word.toCharArray()) {
            if (c >= 'A' && c<= 'Z') sb.append(' ');
            sb.append(c);
        }
        return sb;
    }
}
