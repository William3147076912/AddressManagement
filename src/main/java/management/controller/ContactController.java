package management.controller;

import com.leewyatt.rxcontrols.controls.RXAvatar;
import com.leewyatt.rxcontrols.controls.RXLineButton;
import com.leewyatt.rxcontrols.controls.RXTextField;
import com.leewyatt.rxcontrols.event.RXActionEvent;
import ezvcard.VCard;
import ezvcard.property.*;
import io.vproxy.vfx.manager.font.FontManager;
import io.vproxy.vfx.manager.font.FontUsage;
import io.vproxy.vfx.ui.alert.SimpleAlert;
import io.vproxy.vfx.ui.button.FusionButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import management.Data;
import management.MainPane;
import management.VTableViewScene;
import utils.ConstantSet;
import utils.TUtils;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.UUID;

public class ContactController {
    //flag与data是外界与本类交互的工具
    public static int flag = ConstantSet.CREATE_CONTACT;//默认显示添加联系人界面
    public static Data data;

    @FXML
    private RXLineButton save;
    @FXML
    private RXLineButton cancel;
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
    @FXML
    private AnchorPane pane;


    public Data getData() {
        return ContactController.data;
    }

    public void setData(Data data) {
        ContactController.data = data;
    }

    @FXML
    void deleteText(RXActionEvent event) {//文本框删除功能
        RXTextField tf = (RXTextField) event.getSource();
        tf.clear();
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
        if (flag == ConstantSet.CREATE_CONTACT) {
            String name = nameField.getText();
            String phone = phoneField.getText();
            String email = emailField.getText();
            String homepage = homepageField.getText();
            LocalDate birthday = birthdayField.getValue();
            String company = companyField.getText();
            String address = addressField.getText();
            String postalCode = postalCodeField.getText();
            String remark = remarkField.getText();

            if (name == null) {
                SimpleAlert.show(Alert.AlertType.ERROR, "姓名不能为空<(｀^´)>");
                return;
            }
            for (char c : name.toCharArray()) {
                if (c >= '0' && c <= '9') {
                    SimpleAlert.show(Alert.AlertType.ERROR, "姓名不能含有数字(ꐦ ಠ皿ಠ )");
                    return;
                }
            }
            VCard person = new VCard();
            person.addFormattedName(new FormattedName(name));
            person.addTelephoneNumber(new Telephone(phone));
            person.addEmail(email);
            person.addUrl(new Url(homepage));
            person.setBirthday(birthday);
            person.setOrganization(company);
            person.addAddress(new Address() {{
                setStreetAddress(address);
                setPostalCode(postalCode);
            }});
            person.addNote(remark);
            Data vCardProperties = Data.vCardtoData(person);
            MainPane.addressBook.add(vCardProperties);
            VTableViewScene.table.getItems().add(vCardProperties);
            Stage stage = (Stage) pane.getScene().getWindow();
            stage.close();
            SimpleAlert.show(Alert.AlertType.INFORMATION, "Congratulations，添加成功了(* ^ ω ^)");

        } else if (flag == ConstantSet.UPDATE_CONTACT) {

        }
    }

    @FXML
    void cancel(MouseEvent event) {
        Stage stage = (Stage) pane.getScene().getWindow();
        stage.close();
    }

    public void initialize() {
        //判断是新建联系人（0）or修改联系人（1），新建联系人则不需要初始化界面，修改联系人则要把联系人信息存入文本框（控制器外部实现改初始化）
        //用int不用flag是为了后期增加新功能的方便
        if (flag == ConstantSet.CREATE_CONTACT) {
            //清除文本框数据
            nameField.setText(null);
            phoneField.setText(null);
            emailField.setText(null);
            homepageField.setText(null);
            companyField.setText(null);
            addressField.setText(null);
            postalCodeField.setText(null);
            remarkField.setText(null);
        }
    }
}
