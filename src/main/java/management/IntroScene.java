package management;

import io.vproxy.vfx.manager.font.FontManager;
import io.vproxy.vfx.ui.scene.VScene;
import io.vproxy.vfx.ui.scene.VSceneRole;
import io.vproxy.vfx.ui.wrapper.ThemeLabel;
import io.vproxy.vfx.util.FXUtils;

/**
 * @author fcj
 */
public class IntroScene extends VScene {
    public IntroScene() {
        super(VSceneRole.MAIN);
        enableAutoContentWidthHeight();

        var label = new ThemeLabel("Welcome to VFX AddressBook") {{
            FontManager.get().setFont(this, settings -> settings.setSize(50));
        }};
        getContentPane().getChildren().add(label);
        FXUtils.observeWidthHeightCenter(getContentPane(), label);//让label居中
    }


}
