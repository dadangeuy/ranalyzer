package ranalyzer.controller;

import com.google.common.util.concurrent.FutureCallback;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ranalyzer.Main;
import ranalyzer.model.ProjectRepository;
import ranalyzer.view.ConfirmationBoxView;
import ranalyzer.view.ModifyStatementView;

import java.io.IOException;

public class StatementController {
    private final Stage window = Main.getWindow();
    private final ProjectRepository projectRepository = Main.getProjectRepository();

    public void addStatement(FutureCallback callback) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifyStatementView.fxml"));
            Parent root = (Parent) loader.load();
            Stage stage = new Stage();
            stage.initOwner(window);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setScene(new Scene(root));
            ModifyStatementView controller = loader.getController();
            stage.showAndWait();
            if (controller.isClicked()) {
                String statement = controller.getStatement().getText();
                projectRepository.getStatements().add(statement);
                callback.onSuccess(statement);
            } else {
                callback.onSuccess(null);
            }
        } catch (IOException e) {
            callback.onFailure(e);
        }
    }

    public void editStatement(Integer idx, FutureCallback callback) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifyStatementView.fxml"));
            Parent root = (Parent) loader.load();
            Stage stage = new Stage();
            stage.initOwner(window);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UTILITY);
            stage.setScene(new Scene(root));
            ModifyStatementView controller = loader.getController();
            controller.setStatement(projectRepository.getStatements().get(idx));
            stage.showAndWait();
            if (controller.isClicked()) {
                String statement = controller.getStatement().getText();
                projectRepository.getStatements().set(idx.intValue(), statement);
                callback.onSuccess(statement);
            } else {
                callback.onSuccess(null);
            }
        } catch (IOException e) {
            callback.onFailure(e);
        }
    }

    public void deleteStatement(Integer idx, FutureCallback callback) {
        try {
            String statement = projectRepository.getStatements().get(idx);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ConfirmationBoxView.fxml"));
            Parent root = (Parent) loader.load();
            Stage stage = new Stage();
            stage.initOwner(window);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            stage.setScene(new Scene(root));
            ConfirmationBoxView controller = loader.getController();
            stage.showAndWait();
            if (controller.isConfirmed()) {
                projectRepository.getStatements().remove(idx.intValue());
                callback.onSuccess(statement);
            } else {
                callback.onSuccess(null);
            }
        } catch (IOException e) {
            callback.onFailure(e);
        }
    }
}
