package utils;

import io.vproxy.vfx.control.scroll.VScrollPane;
import io.vproxy.vfx.theme.Theme;
import io.vproxy.vfx.ui.layout.HPadding;
import io.vproxy.vfx.ui.layout.VPadding;
import io.vproxy.vfx.ui.scene.VScene;
import io.vproxy.vfx.ui.scene.VSceneGroup;
import io.vproxy.vfx.ui.scene.VSceneRole;
import io.vproxy.vfx.ui.wrapper.ThemeLabel;
import io.vproxy.vfx.util.FXUtils;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @Author fcj
 * @ClassName PopupScene
 * @date 2024/4/21 下午1:12
 */
public final class PopupScene {
    private PopupScene() {
    }

    @NotNull
    @Contract("_ -> new")
    public static VScene setPopUpScene(Supplier<VSceneGroup> sceneGroupSup) {
        return new VScene(VSceneRole.POPUP) {{
            enableAutoContentWidthHeight();

            getNode().setBackground(new Background(new BackgroundFill(
                    Theme.current().subSceneBackgroundColor(),
                    CornerRadii.EMPTY,
                    Insets.EMPTY
            )));
            getNode().setPrefWidth(500);
            getNode().setPrefHeight(500);
        }};
    }

    //实现界面缓慢出现和消失
    public static void fadeTransition(Scene scene) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.5), scene.getRoot());
        fadeTransition.setFromValue(0.0); // 透明度从 0 开始
        fadeTransition.setToValue(1.0);   // 透明度渐变到 1
        fadeTransition.setCycleCount(1);   // 播放一次
        fadeTransition.play();// 播放透明度动画
    }
    //实现界面缓慢出现和消失，可控制速度
    public static void fadeTransition(Scene scene, double seconds) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(seconds), scene.getRoot());
        fadeTransition.setFromValue(0.0); // 透明度从 0 开始
        fadeTransition.setToValue(1.0);   // 透明度渐变到 1
        fadeTransition.setCycleCount(1);   // 播放一次
        fadeTransition.play();// 播放透明度动画
    }

    public static VScene setContactScene(Supplier<VSceneGroup> sceneGroupSup) {
        var contactScene = setPopUpScene(sceneGroupSup);
        var gridPane = new GridPane();
        var menuPane = new VScrollPane() {{
            getNode().setPrefWidth(600);
            getNode().setPrefHeight(500);
        }};
        var imageVbox = new VBox();
        var labelAndTextVbox = new VBox();
        Label[] images = new Label[]{
                new Label() {{
                    var image = new Image("file:resources/images/person.png", 40, 40, false, false);
                    setGraphic(new ImageView(image));
                    setPadding(new Insets(20, 20, 0, 0));
                }},
                new Label() {{
                    var image = new Image("file:resources/images/phone.png", 40, 40, false, false);
                    setGraphic(new ImageView(image));
                    setPadding(new Insets(20, 20, 0, 0));
                }},
                new Label() {{
                    var image = new Image("file:resources/images/email.png", 40, 40, false, false);
                    setGraphic(new ImageView(image));
                    setPadding(new Insets(20, 20, 0, 0));
                }},

                new Label() {{
                    var image = new Image("file:resources/images/birthday.png", 40, 40, false, false);
                    setGraphic(new ImageView(image));
                    setPadding(new Insets(20, 20, 0, 0));
                }},
                new Label() {{
                    var image = new Image("file:resources/images/company.png", 40, 40, false, false);
                    setGraphic(new ImageView(image));
                    setPadding(new Insets(20, 20, 0, 0));
                }},
                new Label() {{
                    var image = new Image("file:resources/images/address.png", 40, 40, false, false);
                    setGraphic(new ImageView(image));
                    setPadding(new Insets(20, 20, 0, 0));
                }},
                new Label() {{
                    var image = new Image("file:resources/images/remark.png", 40, 40, false, false);
                    setGraphic(new ImageView(image));
                    setPadding(new Insets(20, 20, 0, 0));
                }}
        };
        imageVbox.getChildren().addAll(images);
        Label[] labels = new Label[]{
                new ThemeLabel("姓名"),
                new ThemeLabel("号码"),
                new ThemeLabel("电子邮箱"),
                new ThemeLabel("个人主页"),
                new ThemeLabel("生日"),
                new ThemeLabel("公司"),
                new ThemeLabel("家庭地址"),
                new ThemeLabel("备注")
        };
        TextField[] textField = new TextField[8];
        labelAndTextVbox.getChildren().addAll(labels);

        menuPane.setContent(new HBox() {{
            getChildren().addAll(new HPadding(80), imageVbox, labelAndTextVbox);
        }});
        contactScene.getContentPane().getChildren().add(menuPane.getNode());
        FXUtils.observeWidthCenter(contactScene.getContentPane(), menuPane.getNode());//组件水平居中
        FXUtils.observeHeightCenter(contactScene.getContentPane(), menuPane.getNode());//组件竖直居中
        return contactScene;
    }
}
