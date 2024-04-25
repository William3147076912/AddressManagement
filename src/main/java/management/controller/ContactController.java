package management.controller;

import com.leewyatt.rxcontrols.controls.RXAvatar;
import com.leewyatt.rxcontrols.controls.RXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import management.Data;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ContactController {
    public static Data data;
    @FXML
    private AnchorPane pane;
    @FXML
    private static RXTextField addressField;

    @FXML
    private static DatePicker birthdayField;

    @FXML
    private static RXTextField companyField;

    @FXML
    private static AnchorPane contactPane;

    @FXML
    private static RXTextField emailField;

    @FXML
    private static RXTextField homepageField;

    @FXML
    private static RXAvatar image;

    @FXML
    private static RXTextField nameField;

    @FXML
    private static RXTextField phoneField;

    @FXML
    private static RXTextField postalCodeField;

    @FXML
    private static TextArea remarkField;

    //private static Data;

    public  void init(int flag, Data data) {//判断是新建联系人（0）or修改联系人（1），新建联系人则不需要初始化界面，修改联系人则要把联系人信息存入文本框
        //用int不用flag是为了后期增加新功能的方便
        if (flag == 1) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
            //nameField.setText();
            //phoneField.setText();
            //emailField.setText();
            //homepageField.setText();
            birthdayField.setValue(LocalDate.parse("2022/01/01", formatter));
            //companyField.setText();
            //addressField.setText();
            //postalCodeField.setText();
            //remarkField.setText();
        } else if (flag == 0) {
            //。。。。
            return;
        }
    }

    public void setData(Data data) {
        ContactController.data = data;
    }

    public void getData() {

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
