package management;

import ezvcard.property.Address;
import ezvcard.property.FormattedName;
import ezvcard.property.Uid;
import io.vproxy.vfx.control.scroll.VScrollPane;
import io.vproxy.vfx.manager.font.FontManager;
import io.vproxy.vfx.manager.font.FontUsages;
import io.vproxy.vfx.ui.button.FusionButton;
import io.vproxy.vfx.ui.layout.HPadding;
import io.vproxy.vfx.ui.pane.FusionPane;
import io.vproxy.vfx.ui.scene.*;
import io.vproxy.vfx.ui.table.VTableColumn;
import io.vproxy.vfx.ui.table.VTableView;
import io.vproxy.vfx.ui.wrapper.FusionW;
import io.vproxy.vfx.ui.wrapper.ThemeLabel;
import io.vproxy.vfx.util.FXUtils;
import io.vproxy.vfx.util.MiscUtils;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jetbrains.annotations.NotNull;
import utils.PopupScene;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author fcj
 */
public class VTableViewScene extends VScene {
    public static List<Data> delList = new ArrayList<>();
    public static List<FusionButton> groupList = new ArrayList<>();//组列表

    public static FusionButton allContactBtn = new FusionButton() {{
        setDisable(true);//默认“所有联系人”按钮不可用
        setLayoutX(300);
        setLayoutY(300);
        setPrefWidth(100);
        setPrefHeight(50);
    }};

    public VTableViewScene(Supplier<VSceneGroup> sceneGroupSup) throws IOException {
        super(VSceneRole.MAIN);
        enableAutoContentWidthHeight();

        var table = setTable();
        var vBox = setContentPane(table, sceneGroupSup);

        FXUtils.observeWidthCenter(getContentPane(), vBox);

        getContentPane().getChildren().addAll(vBox);
    }

    public VTableView<Data> setTable() {
        var table = new VTableView<Data>();
        table.getNode().setPrefWidth(1000);
        table.getNode().setPrefHeight(500);
        //选中与否的按钮
        var choiceCol = new VTableColumn<Data, Data>(null, data -> data);
        var idCol = new VTableColumn<Data, Data>("id", data -> data);
        var nameCol = new VTableColumn<Data, Data>("name", data -> data);
        var addressCol = new VTableColumn<Data, Data>("address", data -> data);
        var typeCol = new VTableColumn<Data, String>("type", data -> data.type);
        var createTimeCol = new VTableColumn<Data, ZonedDateTime>("createTime", data ->
                ZonedDateTime.ofInstant(
                        Instant.ofEpochMilli(data.createTime), ZoneId.systemDefault()
                ));

        choiceCol.setAlignment(Pos.CENTER);
        choiceCol.setPrefWidth(50);
        choiceCol.setNodeBuilder(data -> {
            data.choiceButton.setOnAction(e -> {
                Label textNode = data.choiceButton.getTextNode();
                String text = textNode.getText();
                if ("✓".equals(text)) {//取消选中
                    textNode.setText("");
                    delList.remove(data);
                    /*for (var v : delList) {
                        System.out.println(v.name);
                    }*/
                } else {//选中
                    textNode.setText("✓");
                    //...
                    delList.add(data);
                    /*for (var v : delList) {
                        System.out.println(v.name);
                    }*/
                }
            });
            return data.choiceButton;
        });
        idCol.setMinWidth(300);
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
        });
        nameCol.setComparator(Comparator.comparing(data -> data.getFormattedName().getValue()));
        nameCol.setAlignment(Pos.CENTER);
        nameCol.setNodeBuilder(data -> {
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
        });
        addressCol.setAlignment(Pos.CENTER);
        addressCol.setComparator(Comparator.comparing(data -> data.getAddresses().get(0).getStreetAddress()));
        addressCol.setNodeBuilder(data -> {
            var textField = new TextField();
            var text = new FusionW(textField) {{
                FontManager.get().setFont(FontUsages.tableCellText, getLabel());
            }};

            textField.setText(data.getAddresses().get(0).getStreetAddress());
            textField.focusedProperty().addListener((ob, old, now) -> {
                if (old == null || now == null) return;
                if (old && !now) {
                    String addr = textField.getText();
                    Address address = new Address();
                    address.setStreetAddress(addr);
                    data.addAddress(address);
//                    data.address =;
                }
            });
            return text;
        });
        typeCol.setAlignment(Pos.CENTER);
        typeCol.setComparator(String::compareTo);
        addressCol.setNodeBuilder(data -> {
            var textField = new TextField();
            var text = new FusionW(textField) {{
                FontManager.get().setFont(FontUsages.tableCellText, getLabel());
            }};
            List<Address> addresses = data.getAddresses();
            Address address = addresses.get(0);
            textField.setText(address.getStreetAddress());
            textField.focusedProperty().addListener((ob, old, now) -> {
                if (old == null || now == null) return;
                if (old && !now) {
                    String addr = textField.getText();
                    address.setStreetAddress(addr);
                    addresses.add(address);
                }
            });
            return text;
        });
        createTimeCol.setMinWidth(200);
        createTimeCol.setAlignment(Pos.CENTER);
        createTimeCol.setTextBuilder(MiscUtils.YYYYMMddHHiissDateTimeFormatter::format);

        //noinspection unchecked
        table.getColumns().addAll(choiceCol, idCol, nameCol, addressCol, typeCol, createTimeCol);

        for (int i = 0; i < 10; ++i) {
            table.getItems().add(new Data());

        }
        return table;
    }

    public VBox setContentPane(@NotNull VTableView<Data> table, Supplier<VSceneGroup> sceneGroupSup) {
        var controlPane = new FusionPane(false) {{
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
                new FusionButton("Add Contact") {{
                    setOnAction(e -> {
                        //打开添加联系人窗口
                        table.getItems().add(new Data());
                        allContactBtn.setText("All People(" + table.getItems().size() + ")");//刷新按钮文本
                        Scene scene;
                        try {
                            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/contact.fxml"));
                            scene = new Scene(fxmlLoader.load());
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
                new FusionButton("Del Contact") {{
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
                            for (Data data : delList) {//删除数据
                                table.getItems().remove(data);
                            }
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
                }}
        ));

        var menuPane = new VScrollPane() {{
            getNode().setPrefWidth(150);
            getNode().setPrefHeight(400);
        }};
        var contactLabel = new Label("all people") {{
            FontManager.get().setFont(this, settings -> settings.setSize(15));
        }};
        FXUtils.observeWidthCenter(getContentPane(), contactLabel);
        var vbox = new VBox(
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
        menuPane.setContent(vbox);

        allContactBtn.setText("All People(" + table.getItems().size() + ")");//初始化按钮文本
        vbox.getChildren().addAll(allContactBtn);

        var hBox = new HBox(
                table.getNode(),
                new HPadding(10),
                menuPane.getNode()
        );
        FXUtils.observeWidthCenter(getContentPane(), hBox);

        var msgLabel = new ThemeLabel(
                "Click the column name to sort the rows (some of them are sortable).\n" +
                        "Tips: try to sort by multiple columns, and try to hover on \"name\" cells, and try to drag the table:)"
        );
        FXUtils.observeWidthCenter(controlPane.getContentPane(), msgLabel);
        msgLabel.setLayoutY(40);

        return new VBox(msgLabel, controlPane.getNode(), hBox);
    }

}
