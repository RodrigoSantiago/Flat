package flat.graphics.symbols;

import flat.graphics.emojis.EmojiManager;
import flat.graphics.image.Drawable;
import flat.math.shapes.Path;
import flat.window.Application;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class IconsManager {

    private static IconBundle DefaultIconBundle;
    private static final HashMap<String, IconBundle> bundles = new HashMap<>();

    public static void install(IconBundle bundle) {
        bundles.put(bundle.getName().toLowerCase(), bundle);
    }

    private static void readDefaultIcons() {
        if (DefaultIconBundle != null) {
            return;
        }

        var res = Application.getResourcesManager();

        var fill = new Font("MaterialIcons", FontWeight.NORMAL, FontPosture.REGULAR, FontStyle.FANTASY
                , res.getData("default/fonts/icons/MaterialSymbolsSharp_Fill.ttf"));
        var fillNames = new String(res.getData("default/fonts/icons/MaterialSymbolsSharp_Fill.codepoints")
                , StandardCharsets.UTF_8);
        var bundleFill = new IconBundle("fill", fill, fillNames);
        install(bundleFill);

        var outline = new Font("MaterialIcons", FontWeight.NORMAL, FontPosture.REGULAR, FontStyle.FANTASY
                , res.getData("default/fonts/icons/MaterialSymbolsSharp_Outline.ttf"));
        var outlineNames = new String(res.getData("default/fonts/icons/MaterialSymbolsSharp_Outline.codepoints")
                , StandardCharsets.UTF_8);
        var bundleOutline = new IconBundle("outline", outline, outlineNames);
        install(bundleOutline);

        DefaultIconBundle = bundleFill;
    }

    public static Drawable getIcon(String iconName) {
        readDefaultIcons();

        return DefaultIconBundle.getIcon(iconName);
    }

    public static Drawable getIcon(String bundleName, String iconName) {
        readDefaultIcons();

        if ("emoji".equals(bundleName) && !bundles.containsKey("emoji")) {
            return EmojiManager.getIcon(iconName);
        }

        IconBundle icons = bundles.get(bundleName.toLowerCase());
        if (icons != null) {
            return icons.getIcon(iconName);
        }
        return null;
    }
}
