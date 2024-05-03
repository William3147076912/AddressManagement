package management.controller;

import com.leewyatt.rxcontrols.controls.RXAvatar;
import com.leewyatt.rxcontrols.controls.RXLineButton;
import com.leewyatt.rxcontrols.controls.RXTextField;
import com.leewyatt.rxcontrols.event.RXActionEvent;
import ezvcard.VCard;
import ezvcard.parameter.ImageType;
import ezvcard.property.*;
import io.vproxy.vfx.ui.alert.SimpleAlert;
import io.vproxy.vfx.ui.button.FusionButton;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import management.AddressBook;
import management.Data;
import management.VTableViewScene;
import utils.ConstantSet;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ContactController {
    //flag与data是外界与本类交互的工具
    public static int contactControl = ConstantSet.CREATE_CONTACT;//默认显示添加联系人界面
    private VBox groupBox = VTableViewScene.groupBox;
    private List<VCard> groups = AddressBook.getGroups();//组表
    private List<List<Data>> peopleList = AddressBook.getPeopleList();//存储所有分组的所有用户信息
    private int defaultGroupOrNot = VTableViewScene.defaultGroupOrNot;

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

    @FXML
    void deleteText(RXActionEvent event) {//文本框删除功能
        RXTextField tf = (RXTextField) event.getSource();
        tf.clear();
    }

    @FXML
    void setImage(MouseEvent event) throws MalformedURLException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择图片");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("图片文件", "*.png", "*.jpg", "*.gif")
        );
        Window window = image.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(window);
        if (selectedFile != null) {
            // 如果用户选择了图片文件，则加载并显示在图片视图中
            image.setImage(new Image(selectedFile.toURI().toString()));
            System.out.println(image.getImage().getUrl() + "替换了界面图片");
//            //将image存到本地
//            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image.getImage(), null);
//            String suffix = selectedFile.getName().substring(selectedFile.getName().lastIndexOf('.') + 1);
//            File outputFile = new File("src/main/resources/images/" + nameField.getText() + "." + suffix);
//            try {
//                // Write the BufferedImage to the file
//                ImageIO.write(bufferedImage, suffix, outputFile);
//                System.out.println("Image saved successfully.");
//            } catch (IOException e) {
//                System.out.println("Error: " + e.getMessage());
//            }
        }
    }

    @FXML
    void save(MouseEvent event) throws MalformedURLException {
        if (contactControl == ConstantSet.CREATE_CONTACT) {
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

            if (name.matches(".*\\d.*")) {
                SimpleAlert.show(Alert.AlertType.ERROR, "姓名不能含有数字(ꐦ ಠ皿ಠ )");
                return;
            }
            VCard person = new VCard();

            String imageName = new URL(image.getImage().getUrl()).getFile().toLowerCase();
            if (imageName.contains(".png")) person.addPhoto(new Photo(image.getImage().getUrl(), ImageType.PNG));
            else if (imageName.contains(".jpg")) person.addPhoto(new Photo(image.getImage().getUrl(), ImageType.JPEG));
            else if (imageName.contains(".gif")) person.addPhoto(new Photo(image.getImage().getUrl(), ImageType.GIF));
            person.setUid(new Uid(UUID.randomUUID().toString()));
            person.addFormattedName(new FormattedName(name));
            person.addTelephoneNumber(new Telephone(phone));
            person.addEmail(email);
            person.addUrl(new Url(homepage));
            person.setBirthday(birthday);
            person.setOrganization(company);
            Address address1 = new Address();
            address1.setStreetAddress(address);
            address1.setPostalCode(postalCode);
            person.getAddresses().add(address1);
            System.out.println(person.getAddresses().get(0).getStreetAddress());
            person.addNote(remark);
            Data vCardProperties = Data.vCardtoData(person);
            AddressBook.add(vCardProperties);
            VTableViewScene.table.getItems().add(vCardProperties);
            //刷新groupBox
            for (int i = ConstantSet.GROUP_LIST_OFFSET; i < groupBox.getChildren().size(); i++) {
                FusionButton fusionButton = (FusionButton) groupBox.getChildren().get(i);
                int index = i - ConstantSet.GROUP_LIST_OFFSET;
                fusionButton.setText(groups.get(index).getFormattedName().getValue() + "(" + peopleList.get(index).size() + ")");
            }
            Stage stage = (Stage) pane.getScene().getWindow();
            stage.close();
            SimpleAlert.show(Alert.AlertType.INFORMATION, "Congratulations，添加成功了(* ^ ω ^)");
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
        if (contactControl == ConstantSet.CREATE_CONTACT) {
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
