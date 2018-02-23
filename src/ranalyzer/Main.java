package ranalyzer;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.Getter;
import ranalyzer.model.ProjectRepository;
import ranalyzer.view.WorksheetView;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Getter
    private static Stage window;
    @Getter
    private static ProjectRepository projectRepository;
    private WorksheetView controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        projectRepository = new ProjectRepository();

        window.setTitle("RAnalyzer - Requirement Analyzer for Class Diagram");
        window.setMaximized(true);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/WorksheetView.fxml"));
        Parent root = loader.load();
        window.setScene(new Scene(root));
        controller = loader.getController();
        window.show();
        primaryStage.setOnCloseRequest(confirmCloseEventHandler);
    }

    private EventHandler<WindowEvent> confirmCloseEventHandler = event -> {
        if (!controller.exitApplication()) event.consume();
    };
}
