package management;

import com.leewyatt.rxcontrols.controls.RXLineButton;
import ezvcard.VCard;
import ezvcard.property.Address;
import ezvcard.property.FormattedName;
import ezvcard.property.Telephone;
import ezvcard.property.Uid;
import io.vproxy.vfx.control.click.ClickEventHandler;
import io.vproxy.vfx.control.scroll.VScrollPane;
import io.vproxy.vfx.manager.font.FontManager;
import io.vproxy.vfx.manager.font.FontUsages;
import io.vproxy.vfx.theme.Theme;
import io.vproxy.vfx.ui.button.FusionButton;
import io.vproxy.vfx.ui.layout.HPadding;
import io.vproxy.vfx.ui.layout.VPadding;
import io.vproxy.vfx.ui.pane.FusionPane;
import io.vproxy.vfx.ui.scene.*;
import io.vproxy.vfx.ui.table.VTableColumn;
import io.vproxy.vfx.ui.table.VTableView;
import io.vproxy.vfx.ui.wrapper.FusionW;
import io.vproxy.vfx.ui.wrapper.ThemeLabel;
import io.vproxy.vfx.util.FXUtils;
import io.vproxy.vfx.util.MiscUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import management.controller.ContactController;
import org.jetbrains.annotations.NotNull;
import utils.ConstantSet;
import utils.PopupScene;
import utils.TUtils;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * @author fcj
 */
public class VTableViewScene extends VScene {
    public static List<Data> delList = new ArrayList<>();
    public static VBox groupList;//组列表
    public static List<List<Data>> peopleList = new ArrayList<>();//存储所有分组的所有用户信息
    public static VTableView<Data> table;
    public static VTableView<Data> searchTable;
    public static boolean defaultGroupOrNot = true;//识别当前界面展示的是所有人的那个组还是其他组
    public static FusionButton allContactBtn = new FusionButton() {{
        setDisable(true);//默认“所有联系人”按钮不可用
    }};

    public VTableViewScene(Supplier<VSceneGroup> sceneGroupSup) {
        super(VSceneRole.MAIN);
        enableAutoContentWidthHeight();

        //将数据结构里的数据copy一份到peopleList
        List<Data> dataList = new ArrayList<>();
        AddressBookHeadNode[] headNodes = MainPane.addressBook.getAddressBookHeadNodes();//取联系人链表数组的地址
        for (int i = 0; i < 27; i++) {//遍历该链表数组
            AddressBookHeadNode headNode = headNodes[i];
            AddressBookNode bookNode = headNode.getFirstNode();
            while (bookNode != null) {
                dataList.add(bookNode.getData());
                bookNode = bookNode.getNext();
            }
        }
        peopleList.add(dataList);

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
        table.getItems().addAll(peopleList.get(0));
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
        var controlPane = new FusionPane(false) {{
            getNode().setLayoutY(100);
            getNode().setPrefHeight(60);
        }};
        controlPane.getContentPane().getChildren().add(new HBox(
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
                new FusionButton("Add") {{
                    setOnAction(e -> {
                        //打开添加联系人窗口
//                        table.getItems().add(new Data());
                        allContactBtn.setText("All People(" + table.getItems().size() + ")");//刷新按钮文本
                        Scene scene;
                        try {
                            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/contact.fxml"));
                            ContactController.flag = ConstantSet.CREATE_CONTACT;//切换为添加联系人功能
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
                new FusionButton("Del") {{
                    setOnAction(e -> {
                        if (delList.isEmpty()) return;
                        var popUpScene = PopupScene.setPopUpScene(sceneGroupSup);
                        //进行删除确认
                        Label msgLabel = new ThemeLabel("Are you sure you want to delete this contact?") {{
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
                            allContactBtn.setText("All People(" + table.getItems().size() + ")");//刷新按钮文本
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
                        //popUpScene.setBackgroundImage(MyImageManager.get().load("file:resources/images/delete_confirm.gif"));设置背景图片
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
                            allContactBtn.setText("All People(" + table.getItems().size() + ")");//刷新按钮文本
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
                new HPadding(50),
                new FusionButton("Search") {{
                    setOnAction(e -> {
                        var popUpScene = PopupScene.setPopUpScene(sceneGroupSup);
                        popUpScene.getNode().setPrefSize(800, 1000);
                        Label msgLabel = new ThemeLabel("Search Contacts(⊙o⊙)？") {{
                            FontManager.get().setFont(this, settings -> settings.setSize(20));
                            setAlignment(Pos.CENTER);
                        }};
                        FXUtils.observeWidthCenter(popUpScene.getContentPane(), msgLabel);
                        msgLabel.setCenterShape(true);
                        msgLabel.setLayoutY(160);

                        TextField searchField = new TextField();
                        searchField.setPromptText("搜索全部联系人...");
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
                        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                            if (!newValue.isEmpty()) {
                                //System.out.println("clear！");
                                searchTable.getItems().removeAll(peopleList.get(0));
                                String text = searchField.getText();
                                for (var data : peopleList.get(0)) {
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
                                    } else if (!Objects.requireNonNull(Pinyin.getInitialConsonant(data.getFormattedName().getValue())).isEmpty()
                                            && Objects.requireNonNull(Pinyin.getInitialConsonant(data.getFormattedName().getValue())).contains(text)) {//根据名字的声母
                                        searchTable.getItems().add(data);
                                    } else if (!data.getAddresses().isEmpty() && data.getAddresses().get(0).getStreetAddress().matches(text)) {//根据地址
                                        searchTable.getItems().add(data);
                                    } else if (!data.getOrganizations().isEmpty() && data.getOrganizations().get(0).getValues().get(0).matches(text)) {//根据公司
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
        ));
        FXUtils.observeWidthCenter(getContentPane(), controlPane.getNode());

        var menuPane = new VScrollPane() {{
            getNode().setPrefWidth(150);
            getNode().setPrefHeight(400);
        }};
        FXUtils.observeWidthCenter(getContentPane(), menuPane.getNode());
        var contactLabel = new Label("all people") {{
            FontManager.get().setFont(this, settings -> settings.setSize(15));
        }};
        FXUtils.observeWidthCenter(menuPane.getNode(), contactLabel);
        groupList = new VBox(
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
        menuPane.setContent(groupList);
        allContactBtn.setText("All People(" + table.getItems().size() + ")");//初始化按钮文本
        groupList.getChildren().addAll(allContactBtn);
        allContactBtn.setOnMouseClicked(event -> {
            for (int i = ConstantSet.GROUP_LIST_OFFSET; i < VTableViewScene.groupList.getChildren().size(); i++) {
                FusionButton node = (FusionButton) VTableViewScene.groupList.getChildren().get(i);
                if (node != allContactBtn) node.setDisable(false);
                else allContactBtn.setDisable(true);
            }
            //按到本按钮执行跳到所有人组所在的scene
            MainPane.sceneGroup.show(MainPane.mainScenes.get(1), VSceneShowMethod.FROM_LEFT);
        });

        var hBox = new HBox(
                hScrollPane.getNode(),
                new HPadding(10),
                menuPane.getNode()
        );
        hBox.setLayoutY(170);
        FXUtils.observeWidthCenter(getContentPane(), hBox);
        getContentPane().getChildren().addAll(msgLabel, controlPane.getNode(), hBox);
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
        var birthdayCol = new VTableColumn<Data, String>("birthday", data -> data.getBirthday() == null ? "" : data.getBirthday().getDate().toString());
        var companyCol = new VTableColumn<Data, String>("company", data -> data.getOrganizations().isEmpty() ? "" : data.getOrganizations().get(0).getValues().get(0));
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
        nameCol.setMaxWidth(70);
        nameCol.setAlignment(Pos.CENTER);
       /* nameCol.setNodeBuilder(data -> {
            var textField = new TextField();
            var text = new FusionW(textField) {{
                FontManager.get().setFont(FontUsages.tableCellText, getLabel());
            }};
            textField.setText(data.getFormattedName().getValue());
            textField.focusedProperty().addListener((ob, old, now) -> {
                if (old == null || now == null) return;
                if (old && !now) {
                    FormattedName name = new FormattedName(textField.getText());
                    data.setFormattedName(name);
                }
            });
            return text;
        });*/

        phoneCol.setMaxWidth(100);
        phoneCol.setAlignment(Pos.CENTER);
        phoneCol.setComparator(String::compareTo);

        emailCol.setAlignment(Pos.CENTER);
        emailCol.setMaxWidth(200);
        emailCol.setAlignment(Pos.CENTER);

        homePageCol.setAlignment(Pos.CENTER);
        homePageCol.setMinWidth(600);
        birthdayCol.setAlignment(Pos.CENTER);
        birthdayCol.setComparator(String::compareTo);

        companyCol.setAlignment(Pos.CENTER);

        addressCol.setAlignment(Pos.CENTER);
        addressCol.setMinWidth(600);

        postalCodeCol.setAlignment(Pos.CENTER);
        //postalCodeCol.setComparator(Comparator.comparing(data -> data.getAddresses().get(0).getPostalCode()));

        remarkCol.setAlignment(Pos.CENTER);
        remarkCol.setMaxWidth(800);
        //noinspection unchecked
        table.getColumns().addAll(choiceCol, nameCol, phoneCol, emailCol, homePageCol, birthdayCol, companyCol, addressCol, postalCodeCol, remarkCol);
//        for (int i = 0; i < 10; ++i) {
//            table.getItems().add(new Data());
//
//        }
        // 添加双击事件监听器
        table.getScrollPane().getNode().setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // 检查双击事件
               /* String selectedItem = table.getSelectedItem().getFormattedName().getValue();
                // 在这里展示详细内容，可以是一个新的Stage或者一个弹出的窗口
                // 这里只是简单地在控制台打印选中的行数据
                System.out.println("双击了：" + selectedItem);*/
                Stage stage = new Stage();
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/contact.fxml"));
                ContactController.flag = ConstantSet.UPDATE_CONTACT;//切换为修改联系人功能
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
                    var name = selectedItem.getFormattedName().getValue();
                    var phone = selectedItem.getTelephoneNumbers().isEmpty() ? null : selectedItem.getTelephoneNumbers().get(0).getText();
                    var email = selectedItem.getEmails().isEmpty() ? null : selectedItem.getEmails().get(0).getValue();
                    var homePage = selectedItem.getUrls().isEmpty() ? null : selectedItem.getUrls().get(0).getValue();
                    var birthday = selectedItem.getBirthday() == null ? null : selectedItem.getBirthday().getDate().toString();
                    var company = selectedItem.getOrganizations().isEmpty() ? null : selectedItem.getOrganizations().get(0).getValues().get(0);
                    var address = selectedItem.getAddresses().isEmpty() ? null : selectedItem.getAddresses().get(0).getStreetAddress();
                    var postalCode = selectedItem.getAddresses().isEmpty() ? null : selectedItem.getAddresses().get(0).getPostalCode();
                    var remark = selectedItem.getNotes().isEmpty() ? null : selectedItem.getNotes().get(0).getValue();
                    ObservableMap<String, Object> namespace = fxmlLoader.getNamespace();//取得fxml中所有拥有fx:id的组件
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
                    if (birthday != null) {
                        birthdayField.setValue(LocalDate.parse(birthday));
                    }
                    companyField.setText(company);
                    addressField.setText(address);
                    remarkField.setText(remark);
                    postalCodeField.setText(postalCode);
                    //紧接着，监听save与cancel按钮
                    RXLineButton save = (RXLineButton) namespace.get("save");
                    RXLineButton cancel = (RXLineButton) namespace.get("cancel");
                    save.setOnMouseClicked(new ClickEventHandler() {
                        @Override
                        protected void onMouseClicked() {
                            super.onMouseClicked();

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