package management;

import com.jfoenix.assets.JFoenixResources;
import io.vproxy.vfx.ui.scene.VScene;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import management.controller.ContactController;
import utils.ConstantSet;
import utils.PopupScene;

public class Test extends Application {
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        ContactController.flag = ConstantSet.UPDATE_CONTACT;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/contact.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        PopupScene.fadeTransition(scene);//实现界面淡入淡出
        scene.setFill(Color.TRANSPARENT);//背景透明化
        scene.setUserData(this.getHostServices());
        scene.setCamera(new PerspectiveCamera());//透视相机
        //scene.getStylesheets().add("css/style.css");
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setTitle("MainTest Window");
        primaryStage.show();
    }

   /* 实现界面淡入淡出的示例代码
   @Override
    public void start(Stage primaryStage) {
        // 创建一个矩形节点
        Rectangle rectangle = new Rectangle(200, 100, Color.BLUE);
        StackPane root = new StackPane(rectangle);
        Scene scene = new Scene(root, 400, 200);

        // 创建透明度动画
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(3), rectangle);
        fadeTransition.setFromValue(0.0); // 透明度从 0 开始
        fadeTransition.setToValue(1.0);   // 透明度渐变到 1
        fadeTransition.setCycleCount(1);   // 播放一次

        // 点击场景时启动动画
        scene.setOnMouseClicked(event -> {
            // 如果动画正在播放，则停止它
            if (fadeTransition.getStatus() == javafx.animation.Animation.Status.RUNNING) {
                fadeTransition.stop();
            }
            // 启动透明度动画
            fadeTransition.play();
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Fade Animation Example");
        primaryStage.show();
    }*/
}