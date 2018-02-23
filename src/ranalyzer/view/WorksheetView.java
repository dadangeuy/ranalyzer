package ranalyzer.view;

import com.google.common.util.concurrent.FutureCallback;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ranalyzer.Main;
import ranalyzer.controller.GraphController;
import ranalyzer.controller.DocumentController;
import ranalyzer.controller.ProjectController;
import ranalyzer.controller.StatementController;
import ranalyzer.model.ProjectRepository;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class WorksheetView implements Initializable {
    private GraphController graphController;
    private DocumentController documentController;
    private ProjectController projectController;
    private StatementController statementController;
    private String projectName;
    private final ProjectRepository projectRepository = Main.getProjectRepository();
    private final Stage window = Main.getWindow();

    // user-interface
    @FXML
    private Menu documentMenu;
    @FXML
    private Menu statementMenu;
    @FXML
    private Menu graphMenu;
    @FXML
    private MenuItem saveProject;
    @FXML
    private ListView documentListView;
    @FXML
    private ListView statementListView;
    @FXML
    private TextArea preview;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        graphController = new GraphController();
        documentController = new DocumentController();
        projectController = new ProjectController();
        statementController = new StatementController();
        updateState(WorksheetState.INITIALIZED);
    }

    public enum WorksheetState {
        INITIALIZED,
        CREATED,
        MODIFIED,
        SAVED
    }

    private WorksheetState currentState;

    private void updateState(WorksheetState state) {
        this.currentState = state;
        switch (state) {
            case INITIALIZED:
                documentMenu.setDisable(true);
                statementMenu.setDisable(true);
                graphMenu.setDisable(true);
                saveProject.setDisable(true);
                break;
            case CREATED:
                documentMenu.setDisable(false);
                statementMenu.setDisable(false);
                graphMenu.setDisable(false);
                saveProject.setDisable(false);
                loadDocumentsToView();
                loadStatementsToView();
                preview.clear();
                break;
            case SAVED:
                Main.getWindow().setTitle("RAnalyzer - [" + projectName + "]");
                break;
            case MODIFIED:
                Main.getWindow().setTitle("RAnalyzer - [" + projectName + "]*");
                break;
        }
    }

    public boolean exitApplication() {
        if (currentState == WorksheetState.MODIFIED) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ConfirmationBoxView.fxml"));
            try {
                Parent root = (Parent) loader.load();
                Stage stage = new Stage();
                stage.initOwner(window);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initStyle(StageStyle.UTILITY);
                stage.setScene(new Scene(root));
                ConfirmationBoxView controller = loader.getController();
                controller.setLabel(projectName + " has been modified. Exit without saving?");
                stage.showAndWait();
                return controller.isConfirmed();
            } catch (IOException e) {
                showErrorBox(e);
                return true;
            }
        } else {
            return true;
        }
    }

    private void loadDocumentsToView() {
        documentListView.getItems().clear();
        for (int i = 1; i <= projectRepository.getDocuments().size(); i++) {
            documentListView.getItems().add("Document #" + i);
        }
    }

    private void loadStatementsToView() {
        statementListView.getItems().clear();
        for (int i = 1; i <= projectRepository.getStatements().size(); i++) {
            statementListView.getItems().add("Statement #" + i);
        }
    }

    // Project Controller
    public void onClickCreateNewProject() {
        projectController.createNewProject(new FutureCallback<File>() {
            @Override
            public void onSuccess(File file) {
                projectName = file.getName();
                updateState(WorksheetState.CREATED);
                updateState(WorksheetState.SAVED);
            }

            @Override
            public void onFailure(Throwable throwable) {
                showErrorBox(throwable);
            }
        });
    }

    public void onClickOpenExistingProject() {
        projectController.openProject(new FutureCallback<File>() {
            @Override
            public void onSuccess(File file) {
                projectName = file.getName();
                updateState(WorksheetState.CREATED);
                updateState(WorksheetState.SAVED);
            }

            @Override
            public void onFailure(Throwable throwable) {
                showErrorBox(throwable);
            }
        });
    }

    public void onClickSaveProject() {
        projectController.saveProject(new FutureCallback<File>() {
            @Override
            public void onSuccess(File file) {
                updateState(WorksheetState.SAVED);
            }

            @Override
            public void onFailure(Throwable throwable) {
                showErrorBox(throwable);
            }
        });
    }

    // Document Controller
    public void onClickAddDocument() {
        documentController.addDocument(new FutureCallback<File>() {
            @Override
            public void onSuccess(File file) {
                updateState(WorksheetState.MODIFIED);
                loadDocumentsToView();
            }

            @Override
            public void onFailure(Throwable throwable) {
                showErrorBox(throwable);
            }
        });
    }

    public void onClickRemoveDocument() {
        Integer idx = documentListView.getFocusModel().getFocusedIndex();
        if (idx < 0) return;
        documentController.removeDocument(idx,
                new FutureCallback() {
                    @Override
                    public void onSuccess(Object o) {
                        updateState(WorksheetView.WorksheetState.MODIFIED);
                        loadDocumentsToView();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        showErrorBox(throwable);
                    }
                });
    }

    public void onSelectDocument() {
        Integer idx = documentListView.getFocusModel().getFocusedIndex();
        if (idx < 0) return;
        preview.setText(projectRepository.getDocuments().get(idx.intValue()).toPrettyString());
    }

    // Statement Controller
    public void onClickAddStatement() {
        statementController.addStatement(new FutureCallback() {
            @Override
            public void onSuccess(Object o) {
                updateState(WorksheetState.MODIFIED);
                loadStatementsToView();
            }

            @Override
            public void onFailure(Throwable throwable) {
                showErrorBox(throwable);
            }
        });
    }

    public void onClickEditStatement() {
        Integer idx = statementListView.getFocusModel().getFocusedIndex();
        if (idx < 0) return;
        statementController.editStatement(idx,
                new FutureCallback<String>() {
                    @Override
                    public void onSuccess(String statement) {
                        updateState(WorksheetView.WorksheetState.MODIFIED);
                        loadStatementsToView();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        showErrorBox(throwable);
                    }
                });
    }

    public void onClickDeleteStatement() {
        int idx = statementListView.getFocusModel().getFocusedIndex();
        if (idx < 0) return;
        statementController.deleteStatement(idx,
                new FutureCallback() {
                    @Override
                    public void onSuccess(Object o) {
                        updateState(WorksheetState.MODIFIED);
                        loadStatementsToView();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        showErrorBox(throwable);
                    }
                });
    }

    public void onSelectStatement() {
        Integer idx = statementListView.getFocusModel().getFocusedIndex();
        if (idx < 0) return;
        preview.setText(projectRepository.getStatements().get(idx));
    }

    // Dependency Controller
    public void onClickCheckDependency() {
        graphController.checkDependency(new FutureCallback() {
            @Override
            public void onSuccess(Object o) {

            }

            @Override
            public void onFailure(Throwable throwable) {
                showErrorBox(throwable);
            }
        });
    }

    public void onClickViewDependency() {
        try {
            graphController.viewDependency();
        } catch (Throwable throwable) {
            showErrorBox(throwable);
        }
    }

    public void showErrorBox(Throwable e) {
        e.printStackTrace();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ErrorBoxView.fxml"));
            Parent root = (Parent) loader.load();
            Stage stage = new Stage();
            stage.initOwner(window);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();
            ErrorBoxView controller = loader.getController();
            controller.setErrorMessage(e.getMessage());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

}
