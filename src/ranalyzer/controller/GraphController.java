package ranalyzer.controller;

import com.google.common.util.concurrent.FutureCallback;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import ranalyzer.Main;
import ranalyzer.model.ProjectRepository;
import ranalyzer.model.UmlClass;
import ranalyzer.model.ClassDiagram;
import ranalyzer.model.UmlRelation;
import ranalyzer.utility.CosineSimeTest;
import ranalyzer.utility.GraphDemo;

import javax.swing.*;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class GraphController {
    private final ProjectRepository projectRepository = Main.getProjectRepository();
    private final Stage window = Main.getWindow();

    public void checkDependency(FutureCallback callback) {
        Stage stage = new Stage();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoadingView.fxml"));
            Parent root = (Parent) loader.load();
            stage.initOwner(window);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setScene(new Scene(root));
            stage.show();
            System.out.println("running loading");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Graph<String, String> dependencyGraph = new DirectedSparseMultigraph<>();
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    // do some text-mining magic with statements
                    List<String> processedState = new ArrayList<>();
                    for (String i : projectRepository.getStatements()) {
                        // convert to lower case
                        String clean = i.toLowerCase();
                        Analyzer analyzer = new StandardAnalyzer();
                        // tokenizing
                        TokenStream stream = analyzer.tokenStream(null, new StringReader(clean));
                        // stemming
                        stream = new PorterStemFilter(stream);

                        CharTermAttribute attribute = stream.getAttribute(CharTermAttribute.class);
                        List<String> result = new LinkedList<>();
                        stream.reset();
                        while (stream.incrementToken()) result.add(attribute.toString());
                        stream.end();
                        stream.close();
                        StringBuilder sb = new StringBuilder();
                        for (String j : result) sb.append(j).append(' ');
                        processedState.add(sb.toString().trim());
                    }

                    // build string document from umlclass
                    List<UmlClass> classes = new ArrayList<>();
                    List<String> processedClass = new ArrayList<>();
                    for (ClassDiagram i : projectRepository.getDocuments()) {
                        for (UmlClass j : i.getClasses()) {
                            classes.add(j);
                            String process = j.toMiningString();
                            process = process.toLowerCase();
                            Analyzer analyzer = new StandardAnalyzer();
                            TokenStream stream = analyzer.tokenStream(null, new StringReader(process));
                            stream = new PorterStemFilter(stream);
                            CharTermAttribute attribute = stream.getAttribute(CharTermAttribute.class);
                            List<String> result = new LinkedList<>();
                            try {
                                stream.reset();
                                while (stream.incrementToken()) result.add(attribute.toString());
                                stream.end();
                                stream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            StringBuilder sb = new StringBuilder();
                            for (String k : result) sb.append(k).append(' ');
                            processedClass.add(sb.toString().trim());
                        }
                    }

                    // compute similarity & get maximum
                    Double threshold = 0.0;
                    List<String> matchingClass = new ArrayList<>();
                    // best candidate
                    for (String i : processedState) {
                        double max = threshold;
                        int candidate = -1;
                        for (int j = 0; j < processedClass.size(); j++) {
                            String k = processedClass.get(j);
                            try {
                                CosineSimeTest test = new CosineSimeTest(i, k);
                                double similarity = test.getCosineSimilarity();
                                if (similarity > max) {
                                    max = similarity;
                                    candidate = j;
                                }
                                System.out.println(i + " & " + k + ": " + similarity);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (candidate >= 0) matchingClass.add(classes.get(candidate).getName());
                        else matchingClass.add(null);
                    }
                    System.out.println(matchingClass.toString());

                    // generate diagram dependency
                    Map<String, String> diagramType = new HashMap<>();
                    Graph<String, String> diagramGraph = new DirectedSparseMultigraph<>();
                    for (UmlClass umlClass : classes) diagramGraph.addVertex(umlClass.getName());
                    List<UmlRelation> relations = new ArrayList<>();
                    for (ClassDiagram i : projectRepository.getDocuments()) relations.addAll(i.getRelations());
                    for (UmlRelation umlRelation : relations) {
                        String id = umlRelation.getFrom() + umlRelation.getTo();
                        diagramGraph.addEdge(id, umlRelation.getFrom(), umlRelation.getTo());
                        diagramType.put(id, umlRelation.getType());
                    }
                    System.out.println(diagramGraph.toString());

                    // generate statement dependency
                    Graph<String, String> dependencyRelation = new DirectedSparseMultigraph<>();
                    for (int i = 1; i <= projectRepository.getStatements().size(); i++)
                        dependencyGraph.addVertex("Statement #" + Integer.toString(i));
                    for (int i = 1; i <= processedState.size(); i++) {
                        String stateFrom = "Statement #" + Integer.toString(i), classFrom = matchingClass.get(i - 1);
                        if (classFrom == null) continue;
                        for (int j = 1; j <= processedState.size(); j++) {
                            if (j == i) continue;
                            String stateTo = "Statement #" + Integer.toString(j), classTo = matchingClass.get(j - 1);
                            if (classTo == null) continue;
                            if (diagramGraph.findEdge(classFrom, classTo) != null) {
                                String type = diagramType.get(diagramGraph.findEdge(classFrom, classTo));
                                if (type.equals("Dependency")) {
                                    dependencyRelation.addEdge(stateFrom + stateTo, stateFrom, stateTo);
                                } else {
                                    dependencyGraph.addEdge(stateFrom + stateTo, stateFrom, stateTo);
                                }
                            }
                        }
                    }
                    System.out.println(dependencyRelation.toString());

                    for (String i : dependencyRelation.getVertices()) {
                        Set<String> visited = new HashSet<>();
                        Set<String> related = new HashSet<>();
                        Queue<String> notVisited = new LinkedList<>();
                        notVisited.addAll(dependencyRelation.getSuccessors(i));
                        visited.addAll(dependencyRelation.getSuccessors(i));
                        while (!notVisited.isEmpty()) {
                            String current = notVisited.poll();
                            related.addAll(dependencyGraph.getSuccessors(current));
                            for (String j : dependencyRelation.getSuccessors(current)) {
                                if (!visited.contains(j)) {
                                    notVisited.add(j);
                                    visited.add(j);
                                }
                            }
                        }
                        for (String j : related) {
                            dependencyGraph.addEdge(i + j, i, j);
                        }
                    }
                    System.out.println(dependencyGraph.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        task.setOnSucceeded(event -> {
            stage.close();
            projectRepository.setDependency(dependencyGraph);
            viewDependency();
            callback.onSuccess(null);
        });
        task.setOnFailed(event -> {
            stage.close();
        });
        task.setOnCancelled(event -> {
            stage.close();
        });
        task.exceptionProperty().addListener(((observable, oldValue, newValue) -> callback.onFailure((Exception) newValue)));

        new Thread(task).start();
    }

    public void viewDependency() {
        GraphDemo demo = new GraphDemo();
        JFrame frame = new JFrame("Dependency Graph");
        frame.getContentPane().add(demo);
        demo.init(projectRepository.getDependency());
        frame.setVisible(true);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

}
