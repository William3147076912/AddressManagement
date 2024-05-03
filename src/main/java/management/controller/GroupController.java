package management.controller;

import com.leewyatt.rxcontrols.controls.RXFillButton;
import com.leewyatt.rxcontrols.controls.RXTextField;
import com.leewyatt.rxcontrols.event.RXActionEvent;
import ezvcard.VCard;
import ezvcard.property.Kind;
import ezvcard.property.Member;
import io.vproxy.vfx.control.dialog.VDialog;
import io.vproxy.vfx.control.dialog.VDialogButton;
import io.vproxy.vfx.ui.alert.SimpleAlert;
import io.vproxy.vfx.ui.button.FusionButton;
import io.vproxy.vfx.ui.layout.HPadding;
import io.vproxy.vfx.ui.scene.VSceneShowMethod;
import io.vproxy.vfx.ui.table.VTableColumn;
import io.vproxy.vfx.ui.table.VTableView;
import io.vproxy.vfx.ui.wrapper.ThemeLabel;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import management.*;
import utils.ConstantSet;
import utils.PopupScene;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GroupController {
    //由于原版的tableView用起来实在是不习惯，而这个技术栈的tableview又不支持fxml
    //so我就在初始化时添加以下组件，在sceneBuilder上是预览不到效果的
    private static VTableView<Data> contacts;
    private static VTableView<Data> exitingContacts;
    private static int groupControl;
    @FXML
    private RXFillButton delGroupBtn;
    @FXML
    private ThemeLabel title;
    @FXML
    private RXTextField groupName;
    @FXML
    private AnchorPane pane;
    @FXML
    private RXTextField searchField;
    private VTableView<Data> table = VTableViewScene.table;
    private VBox groupBox = VTableViewScene.groupBox;
    private List<VCard> groups = AddressBook.getGroups();//组表
    private List<List<Data>> peopleList = AddressBook.getPeopleList();//存储所有分组的所有用户信息
    private int defaultGroupOrNot = VTableViewScene.defaultGroupOrNot;
    private HBox controlBox = VTableViewScene.controlBox;

    //外部调用exitingContacts的唯一方法
    public static VTableView<Data> getExitingContacts() {
        return exitingContacts;
    }

    //外部设置controlBox的唯一方法
    public static void setGroupControl(int groupControl) {
        GroupController.groupControl = groupControl;
    }

    @FXML
    void delGroup(MouseEvent event) {//根据defaultGroupOrNot来删除数据与groupBox里的按钮并且跳转到all people页面
        while (true) {
            //删除确定界面
            var dialog = new VDialog<Integer>();
            dialog.getStage().getStage().initModality(Modality.APPLICATION_MODAL);
            dialog.setText("Are you sure you want to delete this group?");
            dialog.setButtons(Arrays.asList(
                    new VDialogButton<>("Yes", 1),
                    new VDialogButton<>("No", 0)
            ));
            var result = dialog.showAndWait();
            if (result.isPresent()) {
                if (result.get() == 1) {
                    groupBox.getChildren().remove(ConstantSet.GROUP_LIST_OFFSET + defaultGroupOrNot);//清除组按钮
                    groups.remove(defaultGroupOrNot);//清除该组索引
                    peopleList.remove(defaultGroupOrNot);//清除该组数据
                    //返回all people页面
                    table.getItems().clear();
                    table.getItems().addAll(peopleList.get(0));
                    FusionButton allPeopleBtn = (FusionButton) groupBox.getChildren().get(ConstantSet.GROUP_LIST_OFFSET);
                    allPeopleBtn.setDisable(true);
                    //删除manage group按钮和padding
                    controlBox.getChildren().remove(ConstantSet.CONTROL_NODE_SIZE + 1);
                    controlBox.getChildren().remove(ConstantSet.CONTROL_NODE_SIZE);
                    //关闭本舞台
                    Stage stage = (Stage) pane.getScene().getWindow();
                    stage.close();
                } else if (result.get() == 0) {
                    dialog.getStage().close();
                }
                break;
            } else {
                SimpleAlert.showAndWait(Alert.AlertType.ERROR, "不要皮（♯▼皿▼）");
            }
        }
    }

    @FXML
    void deleteText(RXActionEvent event) {//文本框删除功能
        RXTextField tf = (RXTextField) event.getSource();
        tf.clear();
    }

    @FXML
    void save(MouseEvent event) {
        boolean similarGroupName = false;
        for (VCard group : groups) {
            if (groupName.getText().equals(group.getFormattedName().getValue())) {
                similarGroupName = true;
                break;
            }
        }
        if (groupName.getText().isEmpty()) {
            SimpleAlert.show(Alert.AlertType.ERROR, "组名不能为空（♯▼皿▼）");
        } else if (similarGroupName && groupControl == ConstantSet.CREATE_GROUP) {
            SimpleAlert.show(Alert.AlertType.ERROR, "组名不能和已存在的组名一样ლ(ಠ_ಠლ)");
        } else {
            for (int i = ConstantSet.GROUP_LIST_OFFSET + 1; i < VTableViewScene.groupBox.getChildren().size(); i++) {//找到当前界面
                FusionButton node = (FusionButton) VTableViewScene.groupBox.getChildren().get(i);
                if (node.isDisabled()) {
                    defaultGroupOrNot = i - ConstantSet.GROUP_LIST_OFFSET;
                    break;
                }
            }
            //System.out.println(defaultGroupOrNot);
            if (groupControl == ConstantSet.CREATE_GROUP) {//创建分组
                groupBox.getChildren().add(//创建新建组的按钮
                        new FusionButton(groupName.getText() + "(" + exitingContacts.getItems().size() + ")") {{
                            setOnMouseClicked(event -> {
                                for (int i = ConstantSet.GROUP_LIST_OFFSET; i < VTableViewScene.groupBox.getChildren().size(); i++) {//本按钮禁用，其余按钮可用
                                    FusionButton node = (FusionButton) VTableViewScene.groupBox.getChildren().get(i);
                                    if (node != this) node.setDisable(false);
                                    else {
                                        //通过判断按钮是否为all people来切换defaultGroupOrNot进而切换add按钮的逻辑
                                        defaultGroupOrNot = i - ConstantSet.GROUP_LIST_OFFSET;
                                        System.out.println(defaultGroupOrNot);
                                        table.getItems().clear();
                                        table.getItems().addAll(peopleList.get(defaultGroupOrNot));
                                        this.setDisable(true);
                                        if (defaultGroupOrNot == 0 && controlBox.getChildren().size() > ConstantSet.CONTROL_NODE_SIZE) {
                                            //移除最后一个padding和manage group按钮
                                            controlBox.getChildren().remove(ConstantSet.CONTROL_NODE_SIZE + 1);
                                            controlBox.getChildren().remove(ConstantSet.CONTROL_NODE_SIZE);
                                        } else if (defaultGroupOrNot > 0 && controlBox.getChildren().size() == ConstantSet.CONTROL_NODE_SIZE) {//如果是其他组的页面就多展示一个管理组按钮
                                            controlBox.getChildren().addAll(
                                                    new HPadding(30),
                                                    new FusionButton("Manage Group") {{
                                                        setPrefWidth(200);
                                                        setPrefHeight(40);
                                                        setOnMouseClicked(event -> {
                                                            //更新exitingContacts
                                                            exitingContacts.getItems().clear();
                                                            exitingContacts.getItems().addAll(peopleList.get(defaultGroupOrNot));
                                                            //用stream流过滤掉那些exitingContacts有的联系人
                                                            contacts.getItems().clear();
                                                            contacts.setItems(peopleList.get(0)
                                                                    .stream().filter(contact -> !exitingContacts.getItems().contains(contact)).collect(Collectors.toList()));
                                                            //打开分组窗口
                                                            //allContactBtn.setText("All People(" + table.getItems().size() + ")");//刷新按钮文本
                                                            //设置GroupController为编辑状态
                                                            GroupController.setGroupControl(ConstantSet.MANAGE_GROUP);
                                                            Scene scene;
                                                            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/group.fxml"));
                                                            try {
                                                                scene = new Scene(fxmlLoader.load());
                                                            } catch (IOException ex) {
                                                                throw new RuntimeException(ex);
                                                            }
                                                            ObservableMap<String, Object> namespace = fxmlLoader.getNamespace();
                                                            RXTextField groupName = (RXTextField) namespace.get("groupName");
                                                            int index = -1;
                                                            StringBuilder name = new StringBuilder();//小组名称
                                                            for (int i = ConstantSet.GROUP_LIST_OFFSET; i < groups.size() + ConstantSet.GROUP_LIST_OFFSET; i++) {
                                                                //找到分组界面按钮中不可用的那个就能得到当前展示的是哪个分组
                                                                FusionButton fusionButton = (FusionButton) groupBox.getChildren().get(i);
                                                                if (fusionButton.isDisabled()) {
                                                                    index = i - ConstantSet.GROUP_LIST_OFFSET;//取得该组在组列表的下标
                                                                    //GroupController.getExitingContacts().getItems().addAll(peopleList.get(index));
                                                                    for (int j = 0; j < fusionButton.getTextNode().getText().length(); j++) {//取得组名
                                                                        if (fusionButton.getTextNode().getText().charAt(j) != '(') {
                                                                            name.append(fusionButton.getTextNode().getText().charAt(j));
                                                                        } else break;
                                                                    }
                                                                    groupName.setText(name.toString());//设置展示界面的组名文本框
                                                                }
                                                            }
                                                            PopupScene.fadeTransition(scene);//添加淡入淡出的效果
                                                            scene.setFill(Color.TRANSPARENT);//舞台透明
                                                            int finalIndex = index;
                                                            new Stage() {{
                                                                setScene(scene);
                                                                initStyle(StageStyle.TRANSPARENT);//窗口透明
                                                                initModality(Modality.APPLICATION_MODAL);
                                                                show();
                                                                setOnCloseRequest(event -> {//保存数据
                                                                            peopleList.get(finalIndex).clear();
                                                                            peopleList.get(finalIndex).addAll(GroupController.getExitingContacts().getItems());
                                                                            groups.get(finalIndex).getMembers().clear();
                                                                            //利用stream流提取每个联系人的uid组成数组设置groups
                                                                            //groups.get(finalIndex).addMember(new Member(GroupController.getExitingContacts().getItems().get(0).getUid().getValue()));
                                                                            groups.get(finalIndex).getMembers().addAll(GroupController.getExitingContacts().getItems()
                                                                                    .stream().map(contacts -> new Member(contacts.getUid().getValue())).collect(Collectors.toList()));
                                                                        }
                                                                );
                                                            }};
                                                        });
                                                    }});
                                        }
                                    }
                                }
                                //按到本按钮执行跳到所有人组所在的scene
                                MainPane.sceneGroup.show(MainPane.mainScenes.get(1), VSceneShowMethod.FROM_LEFT);
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
            } else if (groupControl == ConstantSet.MANAGE_GROUP) {//编辑分组
                //更新该组在组表与数据列表的信息
                groups.get(defaultGroupOrNot).getMembers().clear();
                groups.get(defaultGroupOrNot).getMembers().addAll(exitingContacts.getItems()
                        .stream().map(contacts -> new Member(contacts.getUid().getValue())).collect(Collectors.toList()));
                peopleList.get(defaultGroupOrNot).clear();
                peopleList.get(defaultGroupOrNot).addAll(exitingContacts.getItems());
                //更新table界面
                table.getItems().clear();
                table.getItems().addAll(peopleList.get(defaultGroupOrNot));
                //更新groupBox信息界面
                FusionButton currentBtn = (FusionButton) groupBox.getChildren().get(defaultGroupOrNot + ConstantSet.GROUP_LIST_OFFSET);
                currentBtn.setText(groupName.getText() + "(" + peopleList.get(defaultGroupOrNot).size() + ")");//更新展示界面的组名文本框
            }
            //成功界面展示
            Stage stage = (Stage) pane.getScene().getWindow();
            stage.close();
            if (groupControl == ConstantSet.CREATE_GROUP)
                SimpleAlert.show(Alert.AlertType.INFORMATION, "Congratulations，创建成功了٩(๑˃̵ᴗ˂̵๑)۶");
            else if (groupControl == ConstantSet.MANAGE_GROUP)
                SimpleAlert.show(Alert.AlertType.INFORMATION, "Congratulations，修改成功了٩(๑˃̵ᴗ˂̵๑)۶");

        }
    }

    @FXML
    void cancel(MouseEvent event) {
        Stage stage = (Stage) pane.getScene().getWindow();
        stage.close();
    }

    public void initialize() {
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
        for (int i = ConstantSet.GROUP_LIST_OFFSET; i < groupBox.getChildren().size(); i++) {
            //找到分组界面按钮中不可用的那个就能得到当前展示的是哪个分组
            FusionButton fusionButton = (FusionButton) groupBox.getChildren().get(i);
            if (fusionButton.isDisabled()) {
                defaultGroupOrNot = i - ConstantSet.GROUP_LIST_OFFSET;
                break;
            }
        }
        if (groupControl == ConstantSet.CREATE_GROUP) {//切换组件以适应新建分组or管理分组界面
            title.setText("新建分组꒰ঌ( ⌯' '⌯)໒꒱");
            delGroupBtn.setVisible(false);
            contacts.getItems().clear();
            contacts.getItems().addAll(peopleList.get(0));
        } else if (groupControl == ConstantSet.MANAGE_GROUP) {
            title.setText("管理分组٩(•̤̀ᵕ•̤́๑)૭✧");
            delGroupBtn.setVisible(true);
            exitingContacts.getItems().clear();
            exitingContacts.getItems().addAll(peopleList.get(defaultGroupOrNot));
            contacts.getItems().clear();
            //用stream流过滤掉那些exitingContacts有的联系人
            contacts.setItems(peopleList.get(0)
                    .stream().filter(contact -> !exitingContacts.getItems().contains(contact)).collect(Collectors.toList()));
        }
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
                    } else if (Pinyin.getInitialConsonant(data.getFormattedName().getValue()) != null && Objects.requireNonNull(Pinyin.getInitialConsonant(data.getFormattedName().getValue())).contains(text)) {//根据名字的声母
                        contacts.getItems().add(data);
                    } else if (!data.getAddresses().isEmpty() && data.getAddresses().get(0).getStreetAddress().contains(text)) {//根据地址
                        contacts.getItems().add(data);
                    } else if (data.getOrganization() != null && data.getOrganization().getValues().get(0).contains(text)) {//根据公司
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
