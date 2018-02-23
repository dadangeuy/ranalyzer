package ranalyzer.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class UmlRelation implements Serializable {
    private String type;
    private String from;
    private String to;

    public void setFromTo(String from, String to) {
        setFrom(from);
        setTo(to);
    }
}
