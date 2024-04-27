package management.controller;

import com.leewyatt.rxcontrols.controls.RXLineButton;
import com.leewyatt.rxcontrols.controls.RXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class GroupController {

    @FXML
    private ListView<?> contacts;

    @FXML
    private ListView<?> exitingContacts;

    @FXML
    private RXTextField groupName;

    @FXML
    private AnchorPane pane;

    @FXML
    private RXLineButton save;

    @FXML
    private RXTextField searchField;

    @FXML
    void cancel(MouseEvent event) {
        Stage stage = (Stage) pane.getScene().getWindow();
        stage.close();
    }

    @FXML
    void save(MouseEvent event) {

    }

}
