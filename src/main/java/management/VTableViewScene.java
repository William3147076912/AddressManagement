package management;

import ezvcard.VCard;
import ezvcard.property.Telephone;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import utils.MyImageManager;
import utils.TUtils;

import io.vproxy.vfx.ui.wrapper.FusionW;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * @author fcj
 */
public class VTableViewScene extends VScene {
    public static List<Data> delList = new ArrayList<>();
    public static VScene popUpScene = new VScene(VSceneRole.POPUP) {{
        enableAutoContentWidthHeight();

        getNode().setBackground(new Background(new BackgroundFill(
                Theme.current().subSceneBackgroundColor(),
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));
        getNode().setPrefWidth(500);
        getNode().setPrefHeight(500);
    }};

    public VTableViewScene(Supplier<VSceneGroup> sceneGroupSup) {
        super(VSceneRole.MAIN);
        enableAutoContentWidthHeight();


        var msgLabel = new ThemeLabel(
                "Click the column name to sort the rows (some of them are sortable).\n" +
                        "Tips: try to sort by multiple columns, and try to hover on \"name\" cells :)"
        );
        FXUtils.observeWidthCenter(getContentPane(), msgLabel);
        msgLabel.setLayoutY(40);

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
            textField.setText(data.id);
            textField.focusedProperty().addListener((ob, old, now) -> {
                if (old == null || now == null) return;
                if (old && !now) {
                    data.id = textField.getText();
                }
            });
            return text;
        });
        nameCol.setComparator(Comparator.comparing(data -> data.name));
        nameCol.setAlignment(Pos.CENTER);
        nameCol.setNodeBuilder(data -> {
            var textField = new TextField();
            var text = new FusionW(textField) {{
                FontManager.get().setFont(FontUsages.tableCellText, getLabel());
            }};
            textField.setText(data.name);
            textField.focusedProperty().addListener((ob, old, now) -> {
                if (old == null || now == null) return;
                if (old && !now) {
                    data.name = textField.getText();
                }
            });
            return text;
        });
        addressCol.setAlignment(Pos.CENTER);
        addressCol.setComparator(Comparator.comparing(data -> data.address));
        addressCol.setNodeBuilder(data -> {
            var textField = new TextField();
            var text = new FusionW(textField) {{
                FontManager.get().setFont(FontUsages.tableCellText, getLabel());
            }};
            textField.setText(data.address);
            textField.focusedProperty().addListener((ob, old, now) -> {
                if (old == null || now == null) return;
                if (old && !now) {
                    data.address = textField.getText();
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
            textField.setText(data.address);
            textField.focusedProperty().addListener((ob, old, now) -> {
                if (old == null || now == null) return;
                if (old && !now) {
                    data.address = textField.getText();
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
                    setOnAction(e -> table.getItems().add(new Data()));
                    setPrefWidth(120);
                    setPrefHeight(40);
                }},
                new HPadding(10),
                new FusionButton("Del Contact") {{
                    setOnAction(e -> {
                        if (delList.isEmpty()) return;
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
                        FXUtils.observeWidthHeightCenter(popUpScene.getContentPane(), this);
                        sceneGroupSup.get().addScene(popUpScene, VSceneHideMethod.FADE_OUT);
                        FXUtils.runDelay(50, () -> sceneGroupSup.get().show(popUpScene, VSceneShowMethod.FADE_IN));
                    });
                    setPrefWidth(120);
                    setPrefHeight(40);
                }}
        ));

        var menuPane = new FusionPane(false) {{
            getNode().setPrefWidth(150);
        }};
        var contactLabel = new Label("all people") {{
            FontManager.get().setFont(this, settings -> settings.setSize(30));
        }};
        FXUtils.observeWidthCenter(getContentPane(), contactLabel);
        menuPane.getContentPane().getChildren().addAll(contactLabel);

        var hBox = new HBox(
                table.getNode(),
                new HPadding(10),
                menuPane.getNode()
        );
        var vBox = new VBox(controlPane.getNode(), new HPadding(10), hBox);
        FXUtils.observeWidthCenter(getContentPane(), vBox);
        vBox.setLayoutY(100);


        getContentPane().getChildren().addAll(msgLabel, vBox);
    }


    public static class Data extends VCard{
        public FusionButton choiceButton;
        public String id;
        public String name;
        public String address;
        public String type;

        public Data() {
            Data person=new Data();
            Telephone telephone=new Telephone("80284392084");
            person.addTelephoneNumber(telephone);
            choiceButton = new FusionButton() {{
                setPrefWidth(10);
                setPrefHeight(50);
                //setOnlyAnimateWhenNotClicked(true);
            }};
            id = UUID.randomUUID().toString();
            name = TUtils.randomString(10, 15);
            address = TUtils.randomIPAddress();
            type = ThreadLocalRandom.current().nextBoolean() ? "classic" : "new";
            bandwidth = ThreadLocalRandom.current().nextInt(10) * 100 + 100;
            createTime = System.currentTimeMillis();
        }
    }


}