//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package utils;

import io.vproxy.base.util.LogType;
import io.vproxy.base.util.Logger;
import io.vproxy.vfx.util.FXUtils;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

public class MyImageManager {
    private static final MyImageManager instance = new MyImageManager();
    private final Map<String, Image> map = new ConcurrentHashMap<>();
    private final WeakHashMap<String, Image> weakMap = new WeakHashMap<>();

    public static MyImageManager get() {
        return instance;
    }

    private MyImageManager() {
        this.loadBlackAndChangeColor("io/vproxy/vfx/res/image/close.png", Map.of("red", -1217954, "white", -1));
        this.loadBlackAndChangeColor("io/vproxy/vfx/res/image/maximize.png", Map.of("white", -1, "green", -10369964));
        this.loadBlackAndChangeColor("io/vproxy/vfx/res/image/reset-window-size.png", Map.of("white", -1, "green", -10369964));
        this.loadBlackAndChangeColor("io/vproxy/vfx/res/image/iconify.png", Map.of("white", -1, "yellow", -737969));
        this.loadBlackAndChangeColor("io/vproxy/vfx/res/image/arrow.png", Map.of("white", -1));
    }

    public Image load(String path) {
        try {
            return this.load(path, false);
        } catch (Exception var3) {
            return null;
        }
    }

    public Image load(String path, boolean throwException) throws Exception {
        Image image = (Image)this.map.get(path);
        if (image == null) {
            image = (Image)this.weakMap.get(path);
        }

        if (image != null) {
            assert Logger.lowLevelDebug("using cached image: " + path);

            return image;
        } else {
            try {
                image = new Image(path, false);
            } catch (Exception var5) {
                Exception e = var5;
                Logger.error(LogType.FILE_ERROR, "failed loading image " + path, e);
                if (throwException) {
                    throw e;
                }

                return null;
            }

            this.map.put(path, image);

            assert Logger.lowLevelDebug("new image loaded: " + path);

            return image;
        }
    }

    public void loadBlackAndChangeColor(String path, Map<String, Integer> argbs) {


        Image img = this.load(path);
        if (img != null) {
            Iterator var4 = argbs.entrySet().iterator();

            while(var4.hasNext()) {
                Map.Entry<String, Integer> entry = (Map.Entry)var4.next();
                String name = (String)entry.getKey();
                int setArgb = (Integer)entry.getValue();
                WritableImage wImg = FXUtils.changeColorOfBlackImage(img, setArgb);
                String newPath = path + ":" + name;

                assert Logger.lowLevelDebug("new image loaded: " + newPath);

                this.map.put(newPath, wImg);
            }

        }
    }

    public Image loadSubImageOrMake(String baseName, String subName, Function<Image, Image> makeFunc) {
        if (!baseName.startsWith("/")) {
            baseName = "/" + baseName;
        }

        String key = baseName + ":" + subName;
        Image img = (Image)this.map.get(key);
        if (img == null) {
            img = (Image)this.weakMap.get(key);
        }

        if (img != null) {
            assert Logger.lowLevelDebug("using cached image: " + key);

            return img;
        } else {
            img = (Image)this.map.get(baseName);
            if (img == null) {
                img = (Image)this.weakMap.get(baseName);
            }

            if (img == null) {
                Logger.warn(LogType.ALERT, "unable to find base image " + baseName + ", cannot make sub image for it");
                return null;
            } else {
                img = (Image)makeFunc.apply(img);
                if (img == null) {
                    Logger.warn(LogType.ALERT, "failed making image for " + key + ", the make function returns null");
                    return null;
                } else {
                    this.map.put(key, img);

                    assert Logger.lowLevelDebug("new image loaded: " + key);

                    return img;
                }
            }
        }
    }

    public Image weakRef(String path) {
        Image audio = (Image)this.map.remove(path);
        if (audio == null) {
            return null;
        } else {
            this.weakMap.put(path, audio);
            return audio;
        }
    }

    public void remove(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        if (this.map.containsKey(path) || this.weakMap.containsKey(path)) {
            this.map.remove(path);
            this.weakMap.remove(path);

            assert Logger.lowLevelDebug("image removed: " + path);

        }
    }
}
