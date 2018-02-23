package ranalyzer.controller;

import com.google.common.util.concurrent.FutureCallback;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.w3c.dom.Document;
import ranalyzer.Main;
import ranalyzer.model.ProjectRepository;
import ranalyzer.model.ClassDiagram;
import ranalyzer.utility.XmlToUmlParser;
import ranalyzer.view.ConfirmationBoxView;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;

public class DocumentController {
    private final Stage window = Main.getWindow();
    private final ProjectRepository projectRepository = Main.getProjectRepository();

    public void addDocument(FutureCallback callback) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("XMI files (*.xmi)", "*.xmi");
        fileChooser.getExtensionFilters().add(filter);
        fileChooser.setTitle("Add Document");
        File file = fileChooser.showOpenDialog(window);
        if (file == null) return;
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
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(file);
                doc.getDocumentElement().normalize();
                XmlToUmlParser parser = new XmlToUmlParser();
                parser.parse(doc);
                projectRepository.getDocuments().add(parser.getClassDiagram());
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            callback.onSuccess(file);
            stage.close();
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

    public void removeDocument(Integer idx, FutureCallback callback) {
        try {
            ClassDiagram document = projectRepository.getDocuments().get(idx);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ConfirmationBoxView.fxml"));
            Parent root = (Parent) loader.load();
            Stage stage = new Stage();
            stage.initOwner(window);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setScene(new Scene(root));
            ConfirmationBoxView controller = loader.getController();
            stage.showAndWait();
            if (controller.isConfirmed()) {
                projectRepository.getDocuments().remove(idx.intValue());
                callback.onSuccess(document);
            } else {
                callback.onSuccess(null);
            }
        } catch (IOException e) {
            callback.onFailure(e);
        }
    }
}
