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
import ranalyzer.Main;
import ranalyzer.model.ProjectRepository;

import java.io.File;
import java.io.IOException;

public class ProjectController {
    private final ProjectRepository projectRepository = Main.getProjectRepository();
    private final Stage window = Main.getWindow();
    private File file;

    public void createNewProject(FutureCallback callback) {
        try {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("RAN files (*.ran)", "*.ran");
            fileChooser.getExtensionFilters().add(filter);
            fileChooser.setTitle("Save New Project");
            fileChooser.setInitialFileName("Untitled");

            file = fileChooser.showSaveDialog(window);
            if (file == null) return;
            projectRepository.clear();
            projectRepository.save(file);
            callback.onSuccess(file);
        } catch (IOException e) {
            callback.onFailure(e);
        }
    }

    public void openProject(FutureCallback callback) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("RAN files (*.ran)", "*.ran");
        fileChooser.getExtensionFilters().add(filter);
        fileChooser.setTitle("Open Existing Project");
        file = fileChooser.showOpenDialog(window);
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
                projectRepository.read(file);
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            stage.close();
            callback.onSuccess(file);
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

    public void saveProject(FutureCallback callback) {
        try {
            projectRepository.save(file);
            callback.onSuccess(file);
        } catch (IOException e) {
            callback.onFailure(e);
        }
    }

}
