package io.vproxy.vfx.intro;

import app.ui.TestDigit;
import com.leewyatt.rxcontrols.animation.fillbutton.FillAnimation;
import com.leewyatt.rxcontrols.controls.RXFillButton;
import com.leewyatt.rxcontrols.controls.RXTranslationButton;
import com.leewyatt.rxcontrols.skins.RXFillButtonSkin;
import io.vproxy.vfx.manager.font.FontManager;
import io.vproxy.vfx.ui.button.FusionButton;
import io.vproxy.vfx.ui.scene.VSceneRole;
import io.vproxy.vfx.ui.wrapper.ThemeLabel;
import io.vproxy.vfx.util.FXUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class _00IntroScene extends DemoVScene {
    public _00IntroScene() {
        super(VSceneRole.MAIN);
        enableAutoContentWidthHeight();

        var label = new ThemeLabel("Welcome to VFX") {{
            FontManager.get().setFont(this, settings -> settings.setSize(50));
        }};
        getContentPane().getChildren().add(label);

        FXUtils.observeWidthHeightCenter(getContentPane(), label);//让label居中
    }

    @Override
    public String title() {
        return "Intro";
    }//给予scene一个标题
}
