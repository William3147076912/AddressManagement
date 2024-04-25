package management;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import management.controller.ContactController;

public class Test extends Application {
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/contact.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.setFill(Color.TRANSPARENT);//背景透明化
        scene.setUserData(this.getHostServices());
        scene.setCamera(new PerspectiveCamera());//透视相机
        primaryStage.setScene(scene);
        new ContactController().initialize();
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setTitle("MainTest Window");
        primaryStage.show();
    }
}