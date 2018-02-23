package ranalyzer.model;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import lombok.Data;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProjectRepository implements Serializable {
    private List<ClassDiagram> documents = new ArrayList<>();
    private List<String> statements = new ArrayList<>();
    private Graph<String, String> dependency = new DirectedSparseMultigraph<>();

    public void setRepository(ProjectRepository projectRepository) {
        this.documents = projectRepository.getDocuments();
        this.statements = projectRepository.getStatements();
        this.dependency = projectRepository.getDependency();
    }

    public void read(File file) throws IOException, ClassNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());
        ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
        setRepository((ProjectRepository) objectInputStream.readObject());
    }

    public void save(File file) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(file.getAbsolutePath());
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(this);
    }

    public void clear() {
        documents.clear();
        statements.clear();
        for (String v : dependency.getVertices()) dependency.removeVertex(v);
    }
}
