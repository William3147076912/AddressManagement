package management;

import com.leewyatt.rxcontrols.controls.RXAvatar;
import com.leewyatt.rxcontrols.controls.RXLineButton;
import com.leewyatt.rxcontrols.controls.RXTextField;
import ezvcard.VCard;
import ezvcard.parameter.ImageType;
import ezvcard.property.*;
import io.vproxy.vfx.control.scroll.VScrollPane;
import io.vproxy.vfx.manager.font.FontManager;
import io.vproxy.vfx.ui.alert.SimpleAlert;
import io.vproxy.vfx.ui.button.FusionButton;
import io.vproxy.vfx.ui.layout.HPadding;
import io.vproxy.vfx.ui.pane.FusionPane;
import io.vproxy.vfx.ui.scene.*;
import io.vproxy.vfx.ui.table.VTableColumn;
import io.vproxy.vfx.ui.table.VTableView;
import io.vproxy.vfx.ui.wrapper.ThemeLabel;
import io.vproxy.vfx.util.FXUtils;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import management.controller.ContactController;
import management.controller.GroupController;
import utils.ConstantSet;
import utils.MyImageManager;
import utils.Pinyin;
import utils.PopupScene;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author fcj
 */
public class VTableViewScene extends VScene {
    public static VBox groupBox;//组列表菜单
    public static VTableView<Data> table;
    public static int defaultGroupOrNot = 0;//识别当前界面展示的是”all people“组（0）还是其他组（>0）,数字代表组下标
    public static HBox controlBox;
    private List<Data> delList = new ArrayList<>();//存储待删除的联系人
    private VTableView<Data> searchTable;//搜索界面的table列表
    private List<List<Data>> peopleList = AddressBook.getPeopleList();//存储所有分组的所有用户信息
    private List<VCard> groups = AddressBook.getGroups();//包含已存在的所有组信息
    private VTableColumn<Data, String> groupCol;

    public VTableViewScene(Supplier<VSceneGroup> sceneGroupSup) {
        super(VSceneRole.MAIN);
        enableAutoContentWidthHeight();
        getNode().getStylesheets().add("/css/my_theme.css");//设置自定义css主题


        var msgLabel = new ThemeLabel(
                "             Click the column name to sort the rows (name, phoneNumber and birthday can be sortable).\n" +
                        "Tips: try to sort by multiple columns, hover on \"name\" cells, drag the table, and click certain line twice:)"
        );
        FXUtils.observeWidthCenter(getContentPane(), msgLabel);
        msgLabel.setLayoutY(40);
        msgLabel.setAlignment(Pos.CENTER);
        msgLabel.setFont(Font.font(20));
        FXUtils.observeWidthCenter(getContentPane(), msgLabel);//组件水平居中
        table = setTable();
        table.getItems().addAll(peopleList.get(defaultGroupOrNot));
        /*// 监听table中数据的变化  失败品，有待研究
        table.getNode().getProperties().addListener(new MapChangeListener<Object, Object>() {
            @Override
            public void onChanged(Change<? extends Object, ? extends Object> change) {
                if(change.wasRemoved()){
                    System.out.println("removed "+change.getKey());
                }
                System.out.println("removed "+change.getKey());
                *//*if (c.wasAdded() && SET_CONTENT_WIDTH.equals(c.getKey())) {
                    if (c.getValueAdded() instanceof Number) {
                        setContentWidth((Double) c.getValueAdded());
                    }
                    getProperties().remove(SET_CONTENT_WIDTH);
                }*//*
            }
        });*/

       /* ObservableList<Data> tableItems = FXCollections.observableList(table.getItems());//list转为observableList
        tableItems.addListener((ListChangeListener.Change<? extends Data> change) -> {
            System.out.println("removed " + change.getRemoved());
            while (change.next()) {
                if (change.wasAdded()) {
                    // 处理添加项目的逻辑
                }
                if (change.wasRemoved()) {
                    // 处理移除项目的逻
                    System.out.println("removed " + change.getRemoved());
                }
                // 其他变化类型的处理逻辑
                System.out.println(change.getRemovedSize());
            }
        });*/


        var hScrollPane = VScrollPane.makeHorizontalScrollPaneToManage(table);
        hScrollPane.getNode().setPrefWidth(800);
        hScrollPane.getNode().setLayoutY(300);
        FXUtils.observeWidthCenter(getContentPane(), hScrollPane.getNode());

        var menuPane = new VScrollPane() {{
            getNode().setPrefWidth(150);
            getNode().setPrefHeight(400);
        }};
        FXUtils.observeWidthCenter(getContentPane(), menuPane.getNode());
        var contactLabel = new Label("All People") {{
            FontManager.get().setFont(this, settings -> settings.setSize(15));
        }};
        FXUtils.observeWidthCenter(menuPane.getNode(), contactLabel);
        groupBox = new VBox(
                new ThemeLabel("address list") {{
                    FontManager.get().setFont(this, settings -> settings.setSize(20));
                    setAlignment(Pos.CENTER_RIGHT);
                    setBackground(new Background(new BackgroundFill(
                            null,
                            CornerRadii.EMPTY,
                            Insets.EMPTY
                    )));
                }},
                new ThemeLabel("----------------------")
        );
        menuPane.setContent(groupBox);
        //根据当前组表个数创建分组按钮
        for (int i = 0; i < groups.size(); i++) {
            int finalI = i;
            groupBox.getChildren().add(
                    new FusionButton() {{
                        if (finalI == 0) setDisable(true);//默认“所有联系人”按钮不可用
                        setText(groups.get(finalI).getFormattedName().getValue() + "(" + peopleList.get(finalI).size() + ")");//初始化按钮文本
                        setOnMouseClicked(event -> {
                            table.getItems().clear();
                            table.getItems().addAll(peopleList.get(finalI));
                            for (int i = ConstantSet.GROUP_LIST_OFFSET; i < VTableViewScene.groupBox.getChildren().size(); i++) {//本按钮禁用，其余按钮可用
                                FusionButton node = (FusionButton) VTableViewScene.groupBox.getChildren().get(i);
                                if (node != this) node.setDisable(false);
                                else {
                                    //通过判断按钮是否为all people来切换defaultGroupOrNot进而切换add按钮的逻辑
                                    defaultGroupOrNot = i - ConstantSet.GROUP_LIST_OFFSET;
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
                                                        StringBuilder name = new StringBuilder();//小组名称
                                                        for (int i = ConstantSet.GROUP_LIST_OFFSET; i < groupBox.getChildren().size(); i++) {
                                                            //找到分组界面按钮中不可用的那个就能得到当前展示的是哪个分组
                                                            FusionButton fusionButton = (FusionButton) groupBox.getChildren().get(i);
                                                            if (fusionButton.isDisabled()) {
                                                                defaultGroupOrNot = i - ConstantSet.GROUP_LIST_OFFSET;//取得该组在组列表的下标
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
                                                        new Stage() {{
                                                            setScene(scene);
                                                            initStyle(StageStyle.TRANSPARENT);//窗口透明
                                                            initModality(Modality.APPLICATION_MODAL);
                                                            show();
                                                            setOnCloseRequest(event -> {
                                                                        //保存数据
                                                                        peopleList.get(defaultGroupOrNot).clear();
                                                                        peopleList.get(defaultGroupOrNot).addAll(GroupController.getExitingContacts().getItems());
                                                                        groups.get(defaultGroupOrNot).getMembers().clear();
                                                                        groups.get(defaultGroupOrNot).getMembers().addAll(GroupController.getExitingContacts().getItems()
                                                                                .stream().map(contacts -> new Member(contacts.getUid().getValue())).collect(Collectors.toList()));
                                                                        //刷新groupBox
                                                                        for (int i = ConstantSet.GROUP_LIST_OFFSET; i < groupBox.getChildren().size(); i++) {
                                                                            FusionButton fusionButton = (FusionButton) groupBox.getChildren().get(i);
                                                                            int index = i - ConstantSet.GROUP_LIST_OFFSET;
                                                                            fusionButton.setText(groups.get(index).getFormattedName().getValue() + "(" + peopleList.get(index).size() + ")");
                                                                        }
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
        }

        var hBox = new HBox(
                hScrollPane.getNode(),
                new HPadding(10),
                menuPane.getNode()
        );
        hBox.setLayoutY(170);
        FXUtils.observeWidthCenter(getContentPane(), hBox);

        var controlPane = new FusionPane(false) {{
            getNode().setLayoutY(100);
            getNode().setPrefHeight(60);
        }};
        //FusionButton allContactBtn = (FusionButton) groupBox.getChildren().get(ConstantSet.GROUP_LIST_OFFSET);
        controlBox = new HBox();
        controlBox.getChildren().addAll(
                new FusionButton("Select All") {{
                    setOnAction(e -> {
                        if (delList.isEmpty()) {
                            delList.addAll(table.getItems());//添加所有联系人进删除列表
                            for (Data item : table.getItems()) {//选中所有联系人
                                item.choiceButton.getTextNode().setText("✓");
                            }
                        } else {
                            delList.clear();//清空删除列表
                            for (Data item : table.getItems()) {
                                item.choiceButton.getTextNode().setText("");
                            }
                        }
                    });
                    setPrefWidth(120);
                    setPrefHeight(40);
                }},
                new HPadding(10),
                new FusionButton("Add Contact") {{
                    setOnAction(e -> {
                        //打开添加联系人窗口
                        //table.getItems().add(new Data());
                        //allContactBtn.setText("All People(" + peopleList.get(0).size() + ")");//刷新按钮文本
                        Scene scene;
                        try {
                            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/contact.fxml"));
                            ContactController.contactControl = ConstantSet.CREATE_CONTACT;//切换为添加联系人功能
                            scene = new Scene(fxmlLoader.load());
                            PopupScene.fadeTransition(scene);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        scene.setFill(Color.TRANSPARENT);//舞台透明
                        new Stage() {{
                            setScene(scene);
                            initStyle(StageStyle.TRANSPARENT);//窗口透明
                            initModality(Modality.APPLICATION_MODAL);
                            show();
                        }};
                    });
                    setPrefWidth(120);
                    setPrefHeight(40);
                }},
                new HPadding(10),
                new FusionButton("Del Contacts") {{
                    setOnAction(e -> {
                        if (delList.isEmpty()) return;
                        var popUpScene = PopupScene.setPopUpScene();
                        //进行删除确认
                        Label msgLabel = new ThemeLabel("Are you sure you want to delete this or them?") {{
                            FontManager.get().setFont(this, settings -> settings.setSize(20));
                            setAlignment(Pos.CENTER);
                        }};
                        FXUtils.observeWidth(popUpScene.getContentPane(), msgLabel);
                        msgLabel.setCenterShape(true);
                        msgLabel.setLayoutY(100);
                        var sureBtn = new FusionButton("Yes") {{
                            setLayoutX(100);
                            setLayoutY(300);
                            setPrefWidth(100);
                            setPrefHeight(50);
                        }};
                        sureBtn.setOnAction(ee -> {
                            table.getItems().removeAll(delList);
                            //allContactBtn.setText("All People(" + table.getItems().size() + ")");//刷新按钮文本
                            //更新数据与组按钮的文本
                            for (int i = ConstantSet.GROUP_LIST_OFFSET; i < groupBox.getChildren().size(); i++) {
                                FusionButton fusionButton = (FusionButton) groupBox.getChildren().get(i);
                                int index = i - ConstantSet.GROUP_LIST_OFFSET;//分组按钮所代表的组的下标
                                if (index == 0 && fusionButton.isDisabled()) {//如果是all people界面
                                    //删除delList列表中联系人的所有数据，包括其uid
                                    for (int j = 0; j < peopleList.size(); j++) {//由于peopleList 与groups大小必定相同，所以用同一个循环清除数据
                                        peopleList.get(j).removeAll(delList);
                                        groups.get(j).getMembers().removeAll(delList
                                                .stream().map(contacts -> new Member(contacts.getUid().getValue())).collect(Collectors.toList()));
                                    }
                                } else if (fusionButton.isDisabled()) {//其他界面
                                    peopleList.get(index).removeAll(delList);
                                    groups.get(index).getMembers().removeAll(delList
                                            .stream().map(contacts -> new Member(contacts.getUid().getValue())).collect(Collectors.toList()));
                                }
                                StringBuilder name = new StringBuilder();
                                for (int j = 0; j < fusionButton.getTextNode().getText().length(); j++) {//取得组名
                                    if (fusionButton.getTextNode().getText().charAt(j) != '(') {
                                        name.append(fusionButton.getTextNode().getText().charAt(j));
                                    } else break;
                                }
                                fusionButton.setText(name + "(" + peopleList.get(i - ConstantSet.GROUP_LIST_OFFSET).size() + ")");//更新展示界面的组名文本框
                            }
                            for (Data item : peopleList.get(0)) {//清空选择按钮
                                item.choiceButton.getTextNode().setText("");
                            }
                            delList.clear();//清空删除列表
                            sceneGroupSup.get().hide(popUpScene, VSceneHideMethod.FADE_OUT);
                            FXUtils.runDelay(VScene.ANIMATION_DURATION_MILLIS, () -> sceneGroupSup.get().removeScene(popUpScene));
                        });
                        var closeBtn = new FusionButton("No") {{
                            setLayoutX(300);
                            setLayoutY(300);
                            setPrefWidth(100);
                            setPrefHeight(50);
                        }};
                        closeBtn.setOnAction(ee -> {
                            sceneGroupSup.get().hide(popUpScene, VSceneHideMethod.FADE_OUT);
                            FXUtils.runDelay(VScene.ANIMATION_DURATION_MILLIS, () -> sceneGroupSup.get().removeScene(popUpScene));
                        });
                        //popUpScene.setBackgroundImage(MyImageManager.get().load("file:resources/images/delete_confirm.gif"));//设置背景图片
                        popUpScene.getContentPane().getChildren().addAll(msgLabel, sureBtn, closeBtn);
                        sceneGroupSup.get().addScene(popUpScene, VSceneHideMethod.FADE_OUT);
                        FXUtils.runDelay(50, () -> sceneGroupSup.get().show(popUpScene, VSceneShowMethod.FADE_IN));
                    });
                    setPrefWidth(120);
                    setPrefHeight(40);
                }},
                new HPadding(10),
                new FusionButton("Create Group") {
                    {
                        setOnAction(e -> {
                            //打开新建分组窗口
                            //allContactBtn.setText("All People(" + table.getItems().size() + ")");//刷新按钮文本
                            //设置GroupController为创建状态
                            GroupController.setGroupControl(ConstantSet.CREATE_GROUP);
                            Scene scene;
                            try {
                                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/group.fxml"));
                                scene = new Scene(fxmlLoader.load());
                                PopupScene.fadeTransition(scene);//添加淡入淡出的效果
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                            scene.setFill(Color.TRANSPARENT);//舞台透明
                            new Stage() {{
                                setScene(scene);
                                initStyle(StageStyle.TRANSPARENT);//窗口透明
                                initModality(Modality.APPLICATION_MODAL);
                                show();
                            }};
                        });
                        setPrefWidth(200);
                        setPrefHeight(40);
                    }
                },
                new HPadding(10),
                new FusionButton("Search") {{
                    setOnAction(e -> {
                        var popUpScene = PopupScene.setPopUpScene();
                        popUpScene.getNode().setPrefSize(800, 1000);
                        Label msgLabel = new ThemeLabel("Search Contacts(⊙o⊙)？") {{
                            FontManager.get().setFont(this, settings -> settings.setSize(20));
                            setAlignment(Pos.CENTER);
                        }};
                        FXUtils.observeWidthCenter(popUpScene.getContentPane(), msgLabel);
                        msgLabel.setCenterShape(true);
                        msgLabel.setLayoutY(160);

                        RXTextField searchField = new RXTextField();
                        searchField.getStyleClass().add("text-field");
                        searchField.setPromptText("搜索联系人...");
                        searchField.setLayoutY(200);
                        searchField.setMaxWidth(400);
                        FXUtils.observeWidthCenter(popUpScene.getContentPane(), searchField);

                        searchTable = setTable();
                        searchTable.getColumns().remove(0);//删掉选中按钮列
                        searchTable.getColumns().get(0).setPrefWidth(100);//重新设置nameCol的宽度
                        searchTable.getNode().setPrefHeight(500);
                        //从table中取数据放入table中
                        //searchTable.setItems(table.getItems());
                        // 创建 TableView 的数据源
                        searchTable.setItems(peopleList.get(0));
                        /*// 创建 TableColumn
                        //column.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
                        // 表示创建了一个 SimpleStringProperty 对象的值工厂。这个值工厂会将每个单元格的数据作为参数传入，并将其作为 SimpleStringProperty 的值。
                        // 这样，每个单元格将显示一个字符串，该字符串的值与该单元格中的数据相同。
                        TableColumn<String, String> column = new TableColumn<>("Data");
                        column.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
                        // 将 TableColumn 添加到 TableView
                        tableView.getColumns().add(column);*/


                        // 将 TextField 的文本属性绑定到 TableView 的数据源
                        searchField.setOnClickButton(event -> {//增加按到×清空文本框的功能
                            RXTextField tf = (RXTextField) event.getSource();
                            tf.clear();
                        });
                        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                            if (!newValue.isEmpty()) {
                                //System.out.println("clear！");
                                searchTable.getItems().removeAll(peopleList.get(0));
                                String text = searchField.getText();
                                for (var data : peopleList.get(0)) {
                                    if (data.in.contains(text)) {
                                        searchTable.getItems().add(data);
                                    }
                                    // 使用正则表达式检查输入框是否含有数字
                                    if (searchField.getText().matches(".*\\d.*")) {
                                        if (!data.getTelephoneNumbers().isEmpty() && data.getTelephoneNumbers().get(0).getText().contains(text)) {//根据手机号
                                            searchTable.getItems().add(data);
                                        } else if (!data.getEmails().isEmpty() && data.getEmails().get(0).getValue().contains(text)) {//根据邮箱
                                            searchTable.getItems().add(data);
                                        } else if (data.getBirthday() != null && data.getBirthday().getDate().toString().contains(text)) {//根据生日
                                            searchTable.getItems().add(data);
                                        } else if (!data.getAddresses().isEmpty() && data.getAddresses().get(0).getPostalCode().contains(text)) {//根据邮编
                                            searchTable.getItems().add(data);
                                        } else if (!data.getAddresses().isEmpty() && data.getAddresses().get(0).getStreetAddress().matches(text)) {//根据地址（防止含有数字的地址）
                                            searchTable.getItems().add(data);
                                        }
                                    } else if (data.getFormattedName().getValue().contains(text)) {//根据名字
                                        searchTable.getItems().add(data);
                                    } else if (Pinyin.getPinYin(data.getFormattedName().getValue()).contains(text)) {//根据拼音
                                        searchTable.getItems().add(data);
                                    } else if (Pinyin.getInitialConsonant(data.getFormattedName().getValue()) != null && !Objects.requireNonNull(Pinyin.getInitialConsonant(data.getFormattedName().getValue())).isEmpty()
                                            && Objects.requireNonNull(Pinyin.getInitialConsonant(data.getFormattedName().getValue())).contains(text)) {//根据名字的声母
                                        searchTable.getItems().add(data);
                                    } else if (!data.getAddresses().isEmpty() && data.getAddresses().get(0).getStreetAddress().contains(text)) {//根据地址
                                        searchTable.getItems().add(data);
                                    } else if (!data.getAddresses().isEmpty() && Pinyin.getInitialConsonant(data.getAddresses().get(0).getStreetAddress()) != null && Objects.requireNonNull(Pinyin.getInitialConsonant(data.getAddresses().get(0).getStreetAddress())).contains(text)) {//根据地址的声母
                                        searchTable.getItems().add(data);
                                    } else if (data.getOrganization() != null && data.getOrganization().getValues().get(0).contains(text)) {//根据公司
                                        searchTable.getItems().add(data);
                                    } else if (data.getOrganization() != null && Pinyin.getInitialConsonant(data.getOrganization().getValues().get(0)) != null && Objects.requireNonNull(Pinyin.getInitialConsonant(data.getOrganization().getValues().get(0))).contains(text)) {//根据公司的声母
                                        searchTable.getItems().add(data);
                                    }
                                }
                            } else {
                                searchTable.getItems().removeAll(peopleList.get(0));
                                searchTable.setItems(peopleList.get(0));
                            }
                        });

                        var hScrollPane = VScrollPane.makeHorizontalScrollPaneToManage(searchTable);
                        hScrollPane.getNode().setPrefWidth(600);
                        hScrollPane.getNode().setLayoutY(150);
                        FXUtils.observeWidthHeightCenter(popUpScene.getContentPane(), hScrollPane.getNode());
                        var closeBtn = new FusionButton("Close") {{
                            setLayoutY(780);
                            setPrefWidth(100);
                            setPrefHeight(50);
                        }};
                        closeBtn.setOnAction(ee -> {
                            sceneGroupSup.get().hide(popUpScene, VSceneHideMethod.FADE_OUT);
                            FXUtils.runDelay(VScene.ANIMATION_DURATION_MILLIS, () -> sceneGroupSup.get().removeScene(popUpScene));
                        });
                        FXUtils.observeWidthCenter(popUpScene.getContentPane(), closeBtn);
                        //popUpScene.setBackgroundImage(MyImageManager.get().load("file:resources/images/delete_confirm.gif"));设置背景图片
                        popUpScene.getContentPane().getChildren().addAll(msgLabel, searchField, hScrollPane.getNode(), closeBtn);
                        sceneGroupSup.get().addScene(popUpScene, VSceneHideMethod.FADE_OUT);
                        FXUtils.runDelay(50, () -> sceneGroupSup.get().show(popUpScene, VSceneShowMethod.FADE_IN));


                    });
                    setPrefWidth(120);
                    setPrefHeight(40);
                }}
        );
        controlPane.getContentPane().getChildren().add(controlBox);
        FXUtils.observeWidthCenter(getContentPane(), controlPane.getNode());

        getContentPane().getChildren().addAll(msgLabel, controlPane.getNode(), hBox);
        //设置一个线程专门负责界面数据与peopleList和groups组表的同步
        new Thread(() -> {
            while (MainPane.running) {//接收主界面的运行or停止信号
                try {
                    Thread.sleep(1000);//每1s刷新一次groupBox界面
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Platform.runLater(() -> {
                    for (var person : peopleList.get(0)) {//更新每个成员的所属分组
                        for (int i = 1; i < groups.size(); i++) {
                            //System.out.println(groups.get(j).getMembers().get(j).getValue());
                            StringJoiner stringJoiner = new StringJoiner(" ");
                            boolean containOrNot = false;
                            for (var str : person.in.split(" ")) {
                                stringJoiner.add(str);
                            }
                            for (var member : groups.get(i).getMembers()) {
                                if (person.getUid() != null && person.getUid().getValue().equals(member.getValue())) {
                                    containOrNot = true;
                                    if (!person.in.contains(groups.get(i).getFormattedName().getValue())) {
                                        stringJoiner.add(groups.get(i).getFormattedName().getValue());
                                    }
                                }
                            }
                            if (containOrNot) person.in = stringJoiner.toString();
                            else person.in = person.in.replace(" " + groups.get(i).getFormattedName().getValue(), "");
                        }
                    }
                    if (groupBox.getChildren().size() - ConstantSet.GROUP_LIST_OFFSET != groups.size()) {
                        while (groupBox.getChildren().size() > ConstantSet.GROUP_LIST_OFFSET)
                            groupBox.getChildren().remove(ConstantSet.GROUP_LIST_OFFSET);
                        int temp = defaultGroupOrNot;
                        for (int i = 0; i < groups.size(); i++) {
                            int finalI = i;
                            groupBox.getChildren().add(
                                    new FusionButton() {{
                                        if (finalI == temp) setDisable(true);//默认“所有联系人”按钮不可用
                                        setText(groups.get(finalI).getFormattedName().getValue() + "(" + peopleList.get(finalI).size() + ")");//初始化按钮文本
                                        setOnMouseClicked(event -> {
                                            table.getItems().clear();
                                            table.getItems().addAll(peopleList.get(finalI));
                                            for (int i = ConstantSet.GROUP_LIST_OFFSET; i < VTableViewScene.groupBox.getChildren().size(); i++) {//本按钮禁用，其余按钮可用
                                                FusionButton node = (FusionButton) VTableViewScene.groupBox.getChildren().get(i);
                                                if (node != this) node.setDisable(false);
                                                else {
                                                    //通过判断按钮是否为all people来切换defaultGroupOrNot进而切换add按钮的逻辑
                                                    defaultGroupOrNot = i - ConstantSet.GROUP_LIST_OFFSET;
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
                                                                        GroupController.getExitingContacts().getItems().addAll(peopleList.get(defaultGroupOrNot));
                                                                        //打开分组窗口
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
                                                                        StringBuilder name = new StringBuilder();//小组名称
                                                                        for (int i = ConstantSet.GROUP_LIST_OFFSET; i < groupBox.getChildren().size(); i++) {
                                                                            //找到分组界面按钮中不可用的那个就能得到当前展示的是哪个分组
                                                                            FusionButton fusionButton = (FusionButton) groupBox.getChildren().get(i);
                                                                            if (fusionButton.isDisabled()) {
                                                                                defaultGroupOrNot = i - ConstantSet.GROUP_LIST_OFFSET;//取得该组在组列表的下标
                                                                                //GroupController.getExitingContacts().getItems().addAll(peopleList.get(defaultGroupOrNot));
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
                                                                        new Stage() {{
                                                                            setScene(scene);
                                                                            initStyle(StageStyle.TRANSPARENT);//窗口透明
                                                                            initModality(Modality.APPLICATION_MODAL);
                                                                            show();
                                                                            setOnCloseRequest(event -> {
                                                                                        //保存数据
                                                                                        peopleList.get(defaultGroupOrNot).clear();
                                                                                        peopleList.get(defaultGroupOrNot).addAll(GroupController.getExitingContacts().getItems());
                                                                                        groups.get(defaultGroupOrNot).getMembers().clear();
                                                                                        groups.get(defaultGroupOrNot).getMembers().addAll(GroupController.getExitingContacts().getItems()
                                                                                                .stream().map(contacts -> new Member(contacts.getUid().getValue())).collect(Collectors.toList()));
                                                                                        //刷新groupBox
                                                                                        for (int i = ConstantSet.GROUP_LIST_OFFSET; i < groupBox.getChildren().size(); i++) {
                                                                                            FusionButton fusionButton = (FusionButton) groupBox.getChildren().get(i);
                                                                                            int index = i - ConstantSet.GROUP_LIST_OFFSET;
                                                                                            fusionButton.setText(groups.get(index).getFormattedName().getValue() + "(" + peopleList.get(index).size() + ")");
                                                                                        }
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
                        }
                        for (int i = ConstantSet.GROUP_LIST_OFFSET; i < groupBox.getChildren().size(); i++) {//更新组按钮文本
                            FusionButton fusionButton = (FusionButton) groupBox.getChildren().get(i);
                            int index = i - ConstantSet.GROUP_LIST_OFFSET;
                            fusionButton.setText(groups.get(index).getFormattedName().getValue() + "(" + peopleList.get(index).size() + ")");
                        }
                    }
                });
            }
        }, "MyThread").start();
    }

    public VTableView<Data> setTable() {
        var table = new VTableView<Data>();
        table.getNode().setPrefWidth(3000);
        table.getNode().setPrefHeight(500);
        //选中与否的按钮
        var choiceCol = new VTableColumn<Data, Data>("", data -> data);
        var nameCol = new VTableColumn<Data, String>("name", data -> data.getFormattedName().getValue());
        var phoneCol = new VTableColumn<Data, String>("phone", data -> data.getTelephoneNumbers().isEmpty() ? "" : data.getTelephoneNumbers().get(0).getText());
        var emailCol = new VTableColumn<Data, String>("email", data -> data.getEmails().isEmpty() ? "" : data.getEmails().get(0).getValue());
        var homePageCol = new VTableColumn<Data, String>("homePage", data -> data.getUrls().isEmpty() ? "" : data.getUrls().get(0).getValue());
        groupCol = new VTableColumn<>("in", data -> data.in);
        var birthdayCol = new VTableColumn<Data, String>("birthday", data -> data.getBirthday() == null ? "" : data.getBirthday().getDate().toString());
        var companyCol = new VTableColumn<Data, String>("company", data -> data.getOrganization() == null ? "" : data.getOrganization().getValues().get(0));
        var addressCol = new VTableColumn<Data, String>("address", data -> data.getAddresses().isEmpty() ? "" : data.getAddresses().get(0).getStreetAddress());
        var postalCodeCol = new VTableColumn<Data, String>("postalCode", data -> data.getAddresses().isEmpty() ? "" : data.getAddresses().get(0).getPostalCode());
        var remarkCol = new VTableColumn<Data, String>("remark", data -> data.getNotes().isEmpty() ? "" : data.getNotes().get(0).getValue());

        /*idCol.setMinWidth(300);
        idCol.setAlignment(Pos.CENTER);

        idCol.setNodeBuilder(data -> {
            var textField = new TextField();
            var text = new FusionW(textField) {{
                FontManager.get().setFont(FontUsages.tableCellText, getLabel());
            }};

            textField.setText(data.getUid().getValue());
            textField.focusedProperty().addListener((ob, old, now) -> {
                if (old == null || now == null) return;
                if (old && !now) {
                    Uid uid = new Uid(textField.getText());
                    data.setUid(uid);
                }
            });
            return text;
        });*/
        choiceCol.setAlignment(Pos.CENTER);
        choiceCol.setPrefWidth(50);
        choiceCol.setNodeBuilder(data -> {
            data.choiceButton.setOnAction(e -> {
                Label textNode = data.choiceButton.getTextNode();
                String text = textNode.getText();
                if ("✓".equals(text)) {//取消选中
                    textNode.setText("");
                    delList.remove(data);
                    //System.out.println(data.type);
                } else {//选中
                    textNode.setText("✓");
                    //...
                    delList.add(data);
                    //System.out.println(data.type);
                }
            });
            return data.choiceButton;
        });

        //nameCol.setComparator(Comparator.comparing(data -> Pinyin.getPinYin(data.getFormattedName().getValue())));//按拼音排序
        nameCol.setComparator(Comparator.comparing(Pinyin::getPinYin));//按拼音排序
        //nameCol.setMaxWidth(70);
        nameCol.setAlignment(Pos.CENTER);

        //phoneCol.setMaxWidth(100);
        phoneCol.setAlignment(Pos.CENTER);
        phoneCol.setComparator(String::compareTo);

        emailCol.setAlignment(Pos.CENTER);
        //emailCol.setMaxWidth(200);
        emailCol.setAlignment(Pos.CENTER);

        groupCol.setAlignment(Pos.CENTER);
        groupCol.setComparator(String::compareTo);

        homePageCol.setAlignment(Pos.CENTER);
        //homePageCol.setMinWidth(600);
        birthdayCol.setAlignment(Pos.CENTER);
        birthdayCol.setComparator(String::compareTo);

        companyCol.setAlignment(Pos.CENTER);

        addressCol.setAlignment(Pos.CENTER);
        //addressCol.setMinWidth(600);

        postalCodeCol.setAlignment(Pos.CENTER);
        //postalCodeCol.setComparator(Comparator.comparing(data -> data.getAddresses().get(0).getPostalCode()));

        remarkCol.setAlignment(Pos.CENTER);
        //remarkCol.setMaxWidth(800);
        //noinspection unchecked
        table.getColumns().addAll(choiceCol, nameCol, phoneCol, emailCol, groupCol, homePageCol, birthdayCol, companyCol, addressCol, postalCodeCol, remarkCol);
//        for (int i = 0; i < 10; ++i) {
//            table.getItems().add(new Data());
//
//        }
        // 添加双击事件监听器，编辑联系人
        table.getScrollPane().getNode().setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // 检查双击事件
               /* String selectedItem = table.getSelectedItem().getFormattedName().getValue();
                // 在这里展示详细内容，可以是一个新的Stage或者一个弹出的窗口
                // 这里只是简单地在控制台打印选中的行数据
                System.out.println("双击了：" + selectedItem);*/
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/contact.fxml"));
                ContactController.contactControl = ConstantSet.UPDATE_CONTACT;//切换为修改联系人功能
                Scene scene;
                try {
                    scene = new Scene(fxmlLoader.load());
                    PopupScene.fadeTransition(scene);//实现界面淡入淡出
                    scene.setFill(Color.TRANSPARENT);//背景透明化
                    scene.setCamera(new PerspectiveCamera());//透视相机
                    stage.setScene(scene);
                    stage.initStyle(StageStyle.TRANSPARENT);
                    stage.setTitle("MainTest Window");
                    stage.show();
                    //取得该行联系人所有信息并展示在界面文本框
                    Data selectedItem = table.getSelectedItem();
                    //selectedItem.getPhotos().get(0).getData()
                    var name = selectedItem.getFormattedName().getValue();
                    var phone = selectedItem.getTelephoneNumbers().isEmpty() ? "" : selectedItem.getTelephoneNumbers().get(0).getText();
                    var email = selectedItem.getEmails().isEmpty() ? "" : selectedItem.getEmails().get(0).getValue();
                    var homePage = selectedItem.getUrls().isEmpty() ? "" : selectedItem.getUrls().get(0).getValue();
                    var birthday = selectedItem.getBirthday() == null ? "" : selectedItem.getBirthday().getDate().toString();
                    var company = selectedItem.getOrganization() == null ? "" : selectedItem.getOrganization().getValues().get(0);
                    var address = selectedItem.getAddresses().isEmpty() ? "" : selectedItem.getAddresses().get(0).getStreetAddress();
                    var postalCode = selectedItem.getAddresses().isEmpty() ? "" : selectedItem.getAddresses().get(0).getPostalCode();
                    var remark = selectedItem.getNotes().isEmpty() ? "" : selectedItem.getNotes().get(0).getValue();
                    ObservableMap<String, Object> namespace = fxmlLoader.getNamespace();//取得fxml中所有拥有fx:id的组件
                    RXAvatar image = (RXAvatar) namespace.get("image");//
                    if (!selectedItem.getPhotos().isEmpty()) {
                        Photo photo = selectedItem.getPhotos().get(0);
                        byte[] data = photo.getData();//转二进制
                        String filepath = "resources/images/" + selectedItem.getUid().getValue() + "." + photo.getContentType().getValue();
                        File file = new File(filepath);
                        file.createNewFile();
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(data);
                        fos.close();
                        //System.out.println(filepath);
                        image.setImage(new Image("file:" + filepath));
                    }
                    TextField nameField = (TextField) namespace.get("nameField");
                    TextField phoneField = (TextField) namespace.get("phoneField");
                    TextField emailField = (TextField) namespace.get("emailField");
                    TextField homepageField = (TextField) namespace.get("homepageField");
                    DatePicker birthdayField = (DatePicker) namespace.get("birthdayField");
                    TextField companyField = (TextField) namespace.get("companyField");
                    TextField addressField = (TextField) namespace.get("addressField");
                    TextField postalCodeField = (TextField) namespace.get("postalCodeField");
                    TextArea remarkField = (TextArea) namespace.get("remarkField");
                    nameField.setText(name);
                    phoneField.setText(phone);
                    emailField.setText(email);
                    homepageField.setText(homePage);
                    if (!birthday.isEmpty()) {
                        birthdayField.setValue(LocalDate.parse(birthday));
                    }
                    companyField.setText(company);
                    addressField.setText(address);
                    postalCodeField.setText(postalCode);
                    remarkField.setText(remark);
                    //紧接着，监听save与cancel按钮
                    RXLineButton save = (RXLineButton) namespace.get("save");
                    save.setOnAction(mouseEvent -> {
                        if (event.getClickCount() == 2) { // 检查双击事件
                            if (nameField.getText().isEmpty()) {
                                SimpleAlert.show(Alert.AlertType.ERROR, "姓名不能为空ヽ(#ﾟДﾟ)ﾉ┌┛Σ(ノ´Д`)ノ");
                                return;
                            }
                            //设置新数据
                            VCard newItem = new VCard();
                            newItem.setUid(selectedItem.getUid());
                            newItem.setFormattedName(nameField.getText());
                            newItem.addTelephoneNumber(phoneField.getText());
                            newItem.addEmail(emailField.getText());
                            newItem.addUrl(homepageField.getText());
                            newItem.setBirthday(birthdayField.getValue());
                            newItem.setOrganization(companyField.getText());
                            Address address1 = new Address();
                            address1.setStreetAddress(addressField.getText());
                            address1.setPostalCode(postalCodeField.getText());
                            newItem.getAddresses().add(address1);

                            String imageName;
                            try {
                                imageName = new URL(image.getImage().getUrl()).getFile().toLowerCase();
                            } catch (MalformedURLException e) {
                                throw new RuntimeException(e);
                            }
                            if (!imageName.endsWith("康纳酱.gif")) {//如果不是默认图片
                                String str = image.getImage().getUrl();
                                if (str.startsWith("file:")) str = str.substring(5);
                                if (str.startsWith("/")) str = str.substring(1);
                                Path path = Paths.get(str);
                                try {
                                    if (imageName.contains(".png")) {
                                        newItem.addPhoto(new Photo(path, ImageType.PNG));
                                    } else if (imageName.contains(".jpg")) {
                                        newItem.addPhoto(new Photo(path, ImageType.JPEG));
                                    } else if (imageName.contains(".gif")) {
                                        newItem.addPhoto(new Photo(path, ImageType.GIF));
                                    }
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            /*newItem.addAddress(new Address() {{
                                setStreetAddress(addressField.getText());
                                setPostalCode(postalCodeField.getText());
                            }});*/

                            newItem.addNote(remarkField.getText());
                            if (selectedItem.equals(newItem))//没修改但点了保存
                                SimpleAlert.showAndWait(Alert.AlertType.INFORMATION, "您改了什么Owo");
                            for (var group : peopleList) {//替换掉原数据
                                if (group.contains(selectedItem)) {
                                    group.set(group.indexOf(selectedItem), Data.vCardtoData(newItem));
                                }
                            }

                            //刷新table
                            table.getItems().clear();
                            table.getItems().addAll(peopleList.get(defaultGroupOrNot));

                            //成功界面展示
                            stage.close();
                            SimpleAlert.show(Alert.AlertType.INFORMATION, "Congratulations，修改成功了☆*:.｡. o(≧▽≦)o .｡.:*☆");
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return table;
    }
}