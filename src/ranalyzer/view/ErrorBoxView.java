package ranalyzer.view;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ErrorBoxView {
    @FXML
    private Label errorMessage;
    @FXML
    private Button closeButton;

    public void setErrorMessage(String message) {
        errorMessage.setText(message);
    }

    public void onClickCloseButton() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
