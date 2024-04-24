package management;

import com.leewyatt.rxcontrols.controls.RXAvatar;
import com.leewyatt.rxcontrols.controls.RXLineButton;
import com.leewyatt.rxcontrols.controls.RXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;

public class ContactController {
    private static Data data;
    @FXML
    private AnchorPane pane;
    @FXML
    private RXTextField addressField;
    @FXML
    private DatePicker birthdayField;
    @FXML
    private RXTextField companyField;
    @FXML
    private RXTextField emailField;
    @FXML
    private RXTextField homepageField;
    @FXML
    private RXAvatar image;
    @FXML
    private RXTextField nameField;
    @FXML
    private RXTextField phoneField;
    @FXML
    private RXTextField postalCodeField;
    @FXML
    private TextArea remarkField;

    public Data getData() {
        return ContactController.data;
    }

    public void setData(Data data) {
        ContactController.data = data;
    }

    @FXML
    void setImage(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择图片");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.gif", "*.bmp")
        );
        Window window = image.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(window);
        if (selectedFile != null) {
            // 如果用户选择了图片文件，则加载并显示在图片视图中
            image.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    @FXML
    void save(MouseEvent event) {
        String name = nameField.getText();
        String phone = phoneField.getText();
        String email = emailField.getText();
        String homepage = homepageField.getText();
        String birthday = birthdayField.getValue().toString();
        String company = companyField.getText();
        String address = addressField.getText();
        String text = postalCodeField.getText();
        String remark = remarkField.getText();
        setData(new Data(/*写入*/));
        Stage stage = (Stage) pane.getScene().getWindow();
        stage.close();
    }

    @FXML
    void cancel(MouseEvent event) {
        Stage stage = (Stage) pane.getScene().getWindow();
        stage.close();
    }
}
