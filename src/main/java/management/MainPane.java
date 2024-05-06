package management;

import com.leewyatt.rxcontrols.controls.RXTranslationButton;
import ezvcard.VCard;
import io.vproxy.vfx.manager.task.TaskManager;
import io.vproxy.vfx.theme.Theme;
import io.vproxy.vfx.ui.alert.SimpleAlert;
import io.vproxy.vfx.ui.button.FusionButton;
import io.vproxy.vfx.ui.button.FusionImageButton;
import io.vproxy.vfx.ui.layout.HPadding;
import io.vproxy.vfx.ui.layout.VPadding;
import io.vproxy.vfx.ui.pane.FusionPane;
import io.vproxy.vfx.ui.scene.*;
import io.vproxy.vfx.ui.stage.VStage;
import io.vproxy.vfx.ui.wrapper.ThemeLabel;
import io.vproxy.vfx.util.FXUtils;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.*;
import javafx.stage.Window;
import utils.ConstantSet;
import utils.Export;
import utils.Import;
import utils.MyImageManager;

import java.io.*;
import java.util.*;
import java.util.List;

/**
 * @Author fcj
 * @ClassName MainPane
 * @date 2024/4/19 下午7:17
 */
public class MainPane extends Application {
    public static List<VScene> mainScenes = new ArrayList<>();
    public static VSceneGroup sceneGroup;
    public static boolean running = true;
    public static VTableViewScene vTableViewScene = new VTableViewScene(() -> sceneGroup);
    public VBox groupBox = VTableViewScene.groupBox;
    //    private final Path file= Paths.get("src/main/resources/vCard/make_area_phone_186_5586.vcf");
    public String filepath = "resources/vCard/sample.vcf";
    //public String filepath = "../vCard/sample.vcf";
    private List<VCard> groups = AddressBook.getGroups();
    private List<List<Data>> peopleList = AddressBook.getPeopleList();

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void init() throws Exception {
        super.init();
        Import.importVcard(filepath);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        var stage = new VStage(primaryStage) {
            // 在程序关闭前添加一个回调
            @Override
            public void close() {
                running = false;//关闭自定义线程
                //保存数据
                Export.export(filepath);
                super.close();
                TaskManager.get().terminate();
                //GlobalScreenUtils.unregister();
            }
        };
        stage.getInitialScene().enableAutoContentWidthHeight();

        stage.setTitle("VFX AddressBook");
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

        var menuScene = new VScene(VSceneRole.DRAWER_VERTICAL);
        menuScene.getNode().getStylesheets().add("/css/my_theme.css");
        menuScene.getNode().setPrefWidth(450);
        menuScene.enableAutoContentWidth();
        menuScene.getNode().setBackground(new Background(new BackgroundFill(
                Theme.current().subSceneBackgroundColor(),
                CornerRadii.EMPTY,
                Insets.EMPTY
        )));
        stage.getRootSceneGroup().addScene(menuScene, VSceneHideMethod.TO_LEFT);
        var menuBox = new VBox() {{
            setPadding(new Insets(0, 0, 0, 24));
            getChildren().add(new VPadding(20));
        }};
        menuScene.getContentPane().getChildren().add(menuBox);
        menuBox.getChildren().add(new VPadding(20));

        var menuBtn = new FusionImageButton(MyImageManager.get().load("file:resources/images/menu.png")) {{
            setPrefWidth(40);
            setPrefHeight(VStage.TITLE_BAR_HEIGHT + 1);
            getImageView().setFitHeight(15);
            setLayoutX(-2);
            setLayoutY(-1);
        }};
        var importExportIntro = new ThemeLabel("可导入导出vCard文件，即.vcf文件");
        importExportIntro.setFont(Font.font(20));
        var importBtn = new RXTranslationButton("Import") {{
            getStyleClass().add("import_export_btn");
            setPrefSize(200, 100);
            setTranslationDir(TranslationDir.TOP_TO_BOTTOM);
            setGraphic(new ImageView(new Image("file:resources/images/import.png", 100, 100, true, true)));
            setOnMouseClicked(event -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("选择vCard文件");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("联系人文件", "*.vcf")
                );
                Window window = menuScene.getSelfNode().getScene().getWindow();
                File selectedFile = fileChooser.showOpenDialog(window);
                if (selectedFile != null) {
                    try {
                        Import.importVcard(selectedFile.getPath());
                        for (int i = ConstantSet.GROUP_LIST_OFFSET; groupBox != null && i < groupBox.getChildren().size(); i++) {
                            FusionButton fusionButton = (FusionButton) groupBox.getChildren().get(i);
                            fusionButton.setDisable(i == ConstantSet.GROUP_LIST_OFFSET);
                        }
                        VTableViewScene.table.getItems().clear();
                        VTableViewScene.table.getItems().addAll(peopleList.get(0));
                        //System.out.println(AddressBook.getPeopleList().get(0).size());
                        SimpleAlert.show(Alert.AlertType.INFORMATION, "导入成功(๑¯ω¯๑)");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }};
        var exportBtn = new RXTranslationButton("Export") {{
            getStyleClass().add("import_export_btn");
            setPrefSize(200, 100);
            setTranslationDir(TranslationDir.BOTTOM_TO_TOP);
            setGraphic(new ImageView(new Image("file:resources/images/export.png", 100, 100, true, true)));
            setOnMouseClicked(event -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("选择要保存的路径");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("vCard文件", "*.vcf")
                );
                Window window = menuScene.getSelfNode().getScene().getWindow();
                File selectedFile = fileChooser.showSaveDialog(window);
                if (selectedFile != null) {
                    String directory = selectedFile.getPath();
                    System.out.println(directory);
                    Export.export(directory);
                    SimpleAlert.show(Alert.AlertType.INFORMATION, "导出成功(๑•́ ₃ •̀๑)ｴｰ");
                }

                /*Frame frame=new Frame("选择");
                FileDialog fileDialog=new FileDialog(frame,"选择",FileDialog.SAVE);

                //文件后缀过滤好像没起作用
                fileDialog.setFilenameFilter(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".vcf");
                    }
                });

                fileDialog.setFile("exportfile.vcf");
                fileDialog.setVisible(true);
                if(fileDialog.getDirectory()!=null&&fileDialog.getFile()!=null)
                {
                    String directory=fileDialog.getDirectory();
                    String filename = fileDialog.getFile();
                    // 在选择的文件夹中创建文件对象
                    directory=directory.replace('\\','/');
                    Export.export(directory+filename);
                    System.out.println(directory+filename);
                }*/
            });
        }};
        menuBox.getChildren().addAll(importExportIntro, new VPadding(40), importBtn, new VPadding(40), exportBtn);
        FXUtils.observeWidthCenter(menuScene.getContentPane(), menuBox);

        menuBtn.setOnAction(e -> stage.getRootSceneGroup().show(menuScene, VSceneShowMethod.FROM_LEFT));
        stage.getRoot().getContentPane().getChildren().add(menuBtn);
        stage.getStage().setWidth(1280);
        stage.getStage().setHeight(800);
        stage.getStage().centerOnScreen();
        stage.getStage().initStyle(StageStyle.TRANSPARENT);
        stage.getStage().show();
    }
}
