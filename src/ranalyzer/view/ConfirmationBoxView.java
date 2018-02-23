package ranalyzer.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import lombok.Getter;

public class ConfirmationBoxView {

    @FXML
    private Label confirmationLabel;

    @Getter
    private boolean isConfirmed;

    public void setLabel(String text) {
        confirmationLabel.setText(text);
    }

    public void onClickYes(ActionEvent event) {
        isConfirmed = true;
        Stage stage = (Stage) confirmationLabel.getScene().getWindow();
        stage.close();
    }

    public void onClickNo(ActionEvent event) {
        isConfirmed = false;
        Stage stage = (Stage) confirmationLabel.getScene().getWindow();
        stage.close();
    }
}
