package ranalyzer.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Getter;

import java.net.URL;
import java.util.ResourceBundle;

public class ModifyStatementView implements Initializable {

    @FXML
    @Getter
    private TextField statement;
    @FXML
    private Button saveButton;
    @Getter
    private boolean isClicked;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        isClicked = false;
    }

    public void setStatement(String statement) {
        this.statement.setText(statement);
    }

    public void onClickSave(ActionEvent event) {
        isClicked = true;
        Stage window = (Stage) ((Node)event.getTarget()).getScene().getWindow();
        window.close();
    }

}
