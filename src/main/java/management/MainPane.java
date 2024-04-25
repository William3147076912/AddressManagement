package management;

import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.io.text.VCardReader;
import ezvcard.io.text.VCardWriter;
import ezvcard.property.*;
import io.vproxy.vfx.control.globalscreen.GlobalScreenUtils;
import io.vproxy.vfx.manager.task.TaskManager;
import io.vproxy.vfx.theme.Theme;
import io.vproxy.vfx.ui.button.FusionButton;
import io.vproxy.vfx.ui.button.FusionImageButton;
import io.vproxy.vfx.ui.layout.HPadding;
import io.vproxy.vfx.ui.layout.VPadding;
import io.vproxy.vfx.ui.pane.FusionPane;
import io.vproxy.vfx.ui.scene.*;
import io.vproxy.vfx.ui.stage.VStage;
import io.vproxy.vfx.util.FXUtils;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import utils.MyImageManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.interfaces.ECPublicKey;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Author fcj
 * @ClassName MainPane
 * @date 2024/4/19 下午7:17
 */
public class MainPane extends Application {
    private final List<VScene> mainScenes = new ArrayList<>();
    private VSceneGroup sceneGroup;
    public static AddressBook addressBook;
    private final Path file= Paths.get("D:\\desktop\\00001.vcf");

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void init() throws Exception {
        super.init();
        addressBook = new AddressBook();
        VCardReader reader = new VCardReader(file);
        try {
            Data person;
            VCard temp;
            while (  (temp=reader.readNext()) != null) {
                person =Data.vCardtoData(temp);
                addressBook.add(person);
                List<Photo> photoList = person.getPhotos();
                if (!photoList.isEmpty()) {
                    Photo photo;
                    for (int i = 0; i < photoList.size(); i++) {
                        photo = photoList.get(i);
                        byte[] data = photo.getData();
                        String filepath = "src/main/resources/vCard/" + person.getFormattedName().getValue() + i + ".jpg";
                        File file1 = new File(filepath);
                        file1.createNewFile();
                        FileOutputStream fos = new FileOutputStream(file1);
                        fos.write(data);
                        fos.close();
                    }

                }
            }
        } finally {
            reader.close();
        }

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        MyImageManager.get().loadBlackAndChangeColor("file:src/main/resources/images/setting.png", Map.of("white", 0xffffffff));
        MyImageManager.get().loadBlackAndChangeColor("file:src/main/resources/images/up-arrow.png", Map.of("white", 0xffffffff));

        var stage = new VStage(primaryStage) {
            @Override
            public void close() {
                try {
                    VCardWriter vCardWriter=new VCardWriter(file, VCardVersion.V3_0);
                    ArrayList<Data> PersonList= addressBook.getAll();
                    for(Data person:PersonList)
                    {
                        vCardWriter.write(person);
                    }
                    vCardWriter.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                super.close();
                TaskManager.get().terminate();
                //GlobalScreenUtils.unregister();
            }
        };
        stage.getInitialScene().enableAutoContentWidthHeight();

        stage.setTitle("VFX Intro");
        mainScenes.add(new IntroScene());
        mainScenes.add(new VTableViewScene(() -> sceneGroup));
        var initialScene = mainScenes.get(0);
        sceneGroup = new VSceneGroup(initialScene);
        for (var scene : mainScenes) {
            if (scene == initialScene) continue;
            sceneGroup.addScene(scene);
        }

        var navigatePane = new FusionPane();

        navigatePane.getNode().setPrefHeight(60);
        FXUtils.observeHeight(stage.getInitialScene().getContentPane(), sceneGroup.getNode(), -10 - 60 - 5 - 10);

        FXUtils.observeWidth(stage.getInitialScene().getContentPane(), sceneGroup.getNode(), -20);
        FXUtils.observeWidth(stage.getInitialScene().getContentPane(), navigatePane.getNode(), -20);

        var prevButton = new FusionButton("<< Previous") {{
            setPrefWidth(150);
            setPrefHeight(navigatePane.getNode().getPrefHeight() - FusionPane.PADDING_V * 2);
            setOnlyAnimateWhenNotClicked(true);

            var current = sceneGroup.getCurrentMainScene();
            //noinspection SuspiciousMethodCalls
            var index = mainScenes.indexOf(current);
            if (index == 0) {
                setDisable(true);
            }
        }};
        var nextButton = new FusionButton("Next >>") {{
            setPrefWidth(150);
            setPrefHeight(navigatePane.getNode().getPrefHeight() - FusionPane.PADDING_V * 2);
            setOnlyAnimateWhenNotClicked(true);

            var current = sceneGroup.getCurrentMainScene();
            //noinspection SuspiciousMethodCalls
            var index = mainScenes.indexOf(current);
            if (index == mainScenes.size() - 1) {
                setDisable(true);
            }
        }};
        prevButton.setOnAction(e -> {
            var current = sceneGroup.getCurrentMainScene();
            //noinspection SuspiciousMethodCalls
            var index = mainScenes.indexOf(current);
            if (index == 0) {
                return;
            }
            sceneGroup.show(mainScenes.get(index - 1), VSceneShowMethod.FROM_LEFT);
            if (index - 1 == 0) {
                prevButton.setDisable(true);
            }
            nextButton.setDisable(false);
        });
        nextButton.setOnAction(e -> {
            var current = sceneGroup.getCurrentMainScene();
            //noinspection SuspiciousMethodCalls
            var index = mainScenes.indexOf(current);
            if (index == mainScenes.size() - 1) return;
            sceneGroup.show(mainScenes.get(index + 1), VSceneShowMethod.FROM_RIGHT);
            if (index + 1 == mainScenes.size() - 1) {
                nextButton.setDisable(true);
            }
            prevButton.setDisable(false);
        });

        navigatePane.getContentPane().getChildren().add(prevButton);
        navigatePane.getContentPane().getChildren().add(nextButton);
        navigatePane.getContentPane().widthProperty().addListener((ob, old, now) -> {
            if (now == null) return;
            var v = now.doubleValue();
            nextButton.setLayoutX(v - nextButton.getPrefWidth());
        });

        var box = new HBox(
                new HPadding(10),
                new VBox(
                        new VPadding(10),
                        sceneGroup.getNode(),
                        new VPadding(5),
                        navigatePane.getNode()
                )
        );
        stage.getInitialScene().getContentPane().getChildren().add(box);

        var settingScene = new VScene(VSceneRole.DRAWER_VERTICAL);
        settingScene.getNode().setPrefWidth(450);
        settingScene.enableAutoContentWidth();
        settingScene.getNode().setBackground(new Background(new BackgroundFill(
                Theme.current().subSceneBackgroundColor(),
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));
        stage.getRootSceneGroup().addScene(settingScene, VSceneHideMethod.TO_LEFT);
        var settingBox = new VBox() {{
            setPadding(new Insets(0, 0, 0, 24));
            getChildren().add(new VPadding(20));
        }};
        settingScene.getContentPane().getChildren().add(settingBox);
        settingBox.getChildren().add(new VPadding(20));

        var settingBtn = new FusionImageButton(MyImageManager.get().load("file:src/main/resources/images/setting.png")) {{
            setPrefWidth(40);
            setPrefHeight(VStage.TITLE_BAR_HEIGHT + 1);
            getImageView().setFitHeight(15);
            setLayoutX(-2);
            setLayoutY(-1);
        }};
        settingBtn.setOnAction(e -> stage.getRootSceneGroup().show(settingScene, VSceneShowMethod.FROM_LEFT));
        stage.getRoot().getContentPane().getChildren().add(settingBtn);

        stage.getStage().setWidth(1280);
        stage.getStage().setHeight(800);
        stage.getStage().centerOnScreen();
        stage.getStage().initStyle(StageStyle.TRANSPARENT);
        stage.getStage().show();
    }
}
