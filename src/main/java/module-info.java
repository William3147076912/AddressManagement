module management {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires pinyin4j;
    requires io.vproxy.vfx;
    requires java.desktop;
    requires JTransforms;
    requires io.vproxy.base;
    requires com.googlecode.ezvcard;
    requires rxcontrols;
    requires org.jetbrains.annotations;
    requires jfairy;

    opens management to javafx.fxml;
    exports management;
    exports utils;
    opens utils to javafx.fxml;
}