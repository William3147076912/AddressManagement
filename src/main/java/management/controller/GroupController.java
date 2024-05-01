package management.controller;

import com.leewyatt.rxcontrols.controls.RXTextField;
import com.leewyatt.rxcontrols.event.RXActionEvent;
import ezvcard.VCard;
import ezvcard.property.Kind;
import ezvcard.property.Member;
import io.vproxy.vfx.ui.alert.SimpleAlert;
import io.vproxy.vfx.ui.button.FusionButton;
import io.vproxy.vfx.ui.table.VTableColumn;
import io.vproxy.vfx.ui.table.VTableView;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import management.*;
import utils.ConstantSet;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GroupController {
    //由于原版的tableView用起来实在是不习惯，而这个技术栈的tableview又不支持fxml
    //so我就在初始化时添加以下组件，在sceneBuilder上是预览不到效果的
    private static VTableView<Data> contacts;
    private static VTableView<Data> exitingContacts;
    @FXML
    private RXTextField groupName;
    @FXML
    private AnchorPane pane;
    @FXML
    private RXTextField searchField;
    private VBox groupBox = VTableViewScene.groupBox;
    private List<VCard> groups = AddressBook.getGroups();//组表
    private List<List<Data>> peopleList = AddressBook.getPeopleList();//存储所有分组的所有用户信息

    //外部调用exitingContacts的唯一方法
    public static VTableView<Data> getExitingContacts() {
        return exitingContacts;
    }

    @FXML
    void deleteText(RXActionEvent event) {//文本框删除功能
        RXTextField tf = (RXTextField) event.getSource();
        tf.clear();
    }

    @FXML
    void save(MouseEvent event) {
        if (groupName.getText().isEmpty()) {
            SimpleAlert.show(Alert.AlertType.ERROR, "组名不能为空（♯▼皿▼）");
        } else if (exitingContacts.getItems().isEmpty()) {
            SimpleAlert.show(Alert.AlertType.ERROR, "你在创建什么ヽ(#ﾟДﾟ)ﾉ┌┛Σ(ノ´Д`)ノ");
        } else {
            //创建新建组的按钮
            groupBox.getChildren().add(
                    new FusionButton(groupName.getText() + "(" + exitingContacts.getItems().size() + ")") {{
                        setOnMouseClicked(event -> {
                            VTableViewScene.table.getItems().clear();
                            VTableViewScene.table.getItems().addAll(exitingContacts.getItems());
                            for (int i = ConstantSet.GROUP_LIST_OFFSET; i < VTableViewScene.groupBox.getChildren().size(); i++) {
                                FusionButton node = (FusionButton) VTableViewScene.groupBox.getChildren().get(i);
                                if (node != this) node.setDisable(false);
                                else setDisable(true);
                            }
                        });
                    }}
            );
            //将联系人数据存入peopleList
            peopleList.add(exitingContacts.getItems());
            //将联系人uid存入groups组表
            groups.add(new VCard() {{
                setKind(Kind.group());
                setFormattedName(groupName.getText());
                getMembers().addAll(exitingContacts.getItems()
                        .stream().map(contacts -> new Member(contacts.getUid().getValue())).collect(Collectors.toList()));
            }});
            //成功界面展示
            Stage stage = (Stage) pane.getScene().getWindow();
            stage.close();
            SimpleAlert.show(Alert.AlertType.INFORMATION, "Congratulations，添加成功了☆*:.｡. o(≧▽≦)o .｡.:*☆");
        }
    }

    @FXML
    void cancel(MouseEvent event) {
        Stage stage = (Stage) pane.getScene().getWindow();
        stage.close();
    }

    public void initialize() {
        groupName.clear();
        searchField.clear();

        var name1 = new VTableColumn<Data, String>("name", data -> data.getFormattedName() == null ? "" : data.getFormattedName().getValue()) {{
            setAlignment(Pos.CENTER);
            setMinWidth(70);
        }};
        var name2 = new VTableColumn<Data, String>("name", data -> data.getFormattedName() == null ? "" : data.getFormattedName().getValue()) {{
            setAlignment(Pos.CENTER);
            setMinWidth(90);
        }};
        var phone1 = new VTableColumn<Data, String>("phone", data -> data.getTelephoneNumbers().isEmpty() ? "" : data.getTelephoneNumbers().get(0).getText()) {{
            setAlignment(Pos.CENTER);
            setMinWidth(100);
        }};
        var phone2 = new VTableColumn<Data, String>("phone", data -> data.getTelephoneNumbers().isEmpty() ? "" : data.getTelephoneNumbers().get(0).getText()) {{
            setAlignment(Pos.CENTER);
            setMinWidth(100);
        }};
        contacts = new VTableView<>() {{
            //layoutX="117.0" layoutY="172.0" prefHeight="310.0" prefWidth="162.0"
            getNode().setLayoutX(117);
            getNode().setLayoutY(188);
            getNode().setPrefSize(170, 310);
            getColumns().addAll(name1, phone1);
        }};
        exitingContacts = new VTableView<>() {{
            getNode().setLayoutX(316);
            getNode().setLayoutY(160);
            getNode().setPrefSize(180, 340);
            getColumns().addAll(name2, phone2);
        }};
        contacts.getItems().addAll(peopleList.get(0));
        pane.getChildren().addAll(contacts.getNode(), exitingContacts.getNode());
        // 将 TextField 的文本属性绑定到 TableView 的数据源
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                //System.out.println("clear！");
                contacts.getItems().removeAll(peopleList.get(0));
                String text = searchField.getText();
                for (var data : peopleList.get(0)) {
                    if (exitingContacts.getItems().contains(data)) {//如果该联系人信息已经在已有联系人列表中则直接跳过
                        continue;
                    }
                    // 使用正则表达式检查输入框是否含有数字
                    if (searchField.getText().matches(".*\\d.*")) {
                        if (!data.getTelephoneNumbers().isEmpty() && data.getTelephoneNumbers().get(0).getText().contains(text)) {//根据手机号
                            contacts.getItems().add(data);
                        } else if (!data.getEmails().isEmpty() && data.getEmails().get(0).getValue().contains(text)) {//根据邮箱
                            contacts.getItems().add(data);
                        } else if (data.getBirthday() != null && data.getBirthday().getDate().toString().contains(text)) {//根据生日
                            contacts.getItems().add(data);
                        } else if (!data.getAddresses().isEmpty() && data.getAddresses().get(0).getPostalCode().contains(text)) {//根据邮编
                            contacts.getItems().add(data);
                        } else if (!data.getAddresses().isEmpty() && data.getAddresses().get(0).getStreetAddress().matches(text)) {//根据地址（防止含有数字的地址）
                            contacts.getItems().add(data);
                        }
                    } else if (data.getFormattedName().getValue().contains(text)) {//根据名字
                        contacts.getItems().add(data);
                    } else if (Pinyin.getPinYin(data.getFormattedName().getValue()).contains(text)) {//根据拼音
                        contacts.getItems().add(data);
                    } else if (!Objects.requireNonNull(Pinyin.getInitialConsonant(data.getFormattedName().getValue())).isEmpty()
                            && Objects.requireNonNull(Pinyin.getInitialConsonant(data.getFormattedName().getValue())).contains(text)) {//根据名字的声母
                        contacts.getItems().add(data);
                    } else if (!data.getAddresses().isEmpty() && data.getAddresses().get(0).getStreetAddress().matches(text)) {//根据地址
                        contacts.getItems().add(data);
                    } else if (!data.getOrganizations().isEmpty() && data.getOrganizations().get(0).getValues().get(0).matches(text)) {//根据公司
                        contacts.getItems().add(data);
                    }
                }
            } else {
                contacts.getItems().clear();//清除数据
                //用stream流过滤掉那些exitingContacts有的联系人
                contacts.setItems(peopleList.get(0)
                        .stream().filter(contact -> !exitingContacts.getItems().contains(contact)).collect(Collectors.toList()));
            }
        });
        //添加行点击事件
        addTableViewClickEvent(contacts, exitingContacts);
        addTableViewClickEvent(exitingContacts, contacts);
    }

    private void addTableViewClickEvent(VTableView<Data> removeContacts, VTableView<Data> addContacts) {
        removeContacts.getScrollPane().getNode().setOnMouseClicked(event -> {
            //取得该行联系人
            Data selectedItem = removeContacts.getSelectedItem();
            if (selectedItem == null) {
                return;
            }//如果为空则跳过
            removeContacts.getItems().remove(selectedItem);
            try {
                //由于此事件与上面的动态绑定构成了多线程，且exitingContacts，由于所用列表使用了arraylist是一个线程不安全数组
                //所以使用异常捕获去识别越界并且处理问题，当然也可以直接用处理异常的方法解决，不过比较浪费空间而且用用异常捕获学习学习一下owo
                addContacts.getItems().add(selectedItem);
            } catch (Exception e) {
                List<Data> items = addContacts.getItems();
                items.add(selectedItem);
                addContacts.getItems().clear();
                addContacts.setItems(items);
            }
        });
    }
}
