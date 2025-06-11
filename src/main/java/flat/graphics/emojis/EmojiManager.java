package flat.graphics.emojis;

import flat.backend.SVG;
import flat.graphics.ImageTexture;
import flat.graphics.context.Texture2D;
import flat.graphics.context.enums.MagFilter;
import flat.graphics.context.enums.MinFilter;
import flat.graphics.context.enums.PixelFormat;
import flat.graphics.context.enums.WrapMode;
import flat.graphics.image.Drawable;
import flat.graphics.image.SpriteMap;
import flat.resources.ResourceStream;
import flat.window.Application;

import java.util.HashMap;

public class EmojiManager {

    private static Texture2D emojiTexture;
    private static final HashMap<Integer, Drawable> drawables = new HashMap<>();
    private static final ImageTexture imageTexture = () -> emojiTexture;

    private static boolean enabled;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void load(ResourceStream image) {
        SVG.FontSetEmojiEnabled(enabled = true);
        EmojiDictionary.getInstance();
        new Thread(() -> {
            try {
                int[] imageData = new int[3];
                byte[] data = SVG.ReadImage(image.readData(), imageData);

                Application.runOnContextSync(() -> {
                    emojiTexture = new Texture2D(imageData[0], imageData[1], PixelFormat.RGBA);
                    emojiTexture.setData(0, data, 0, 0, 0, imageData[0], imageData[1]);
                    emojiTexture.setLevels(0);
                    emojiTexture.generateMipmapLevels();
                    emojiTexture.setScaleFilters(MagFilter.LINEAR, MinFilter.LINEAR_MIPMAP_LINEAR);
                    emojiTexture.setWrapModes(WrapMode.CLAMP_TO_EDGE, WrapMode.CLAMP_TO_EDGE);
                    emojiTexture.setLevels(8);
                    emojiTexture.generateMipmapLevels();
                    SVG.FontCreateEmoji(emojiTexture.getInternalId(), EmojiDictionary.getInstance().getUnicodes());
                    for (var window : Application.getAssignedWindows()) {
                        window.getActivity().repaint();
                    }
                });
            } catch (Exception e) {
                Application.handleException(e);
            }
        }).start();
    }

    public static void unload() {
        SVG.FontSetEmojiEnabled(enabled = false);
        if (emojiTexture != null) {
            emojiTexture.dispose();
            emojiTexture = null;
            SVG.FontDestroyEmoji();
        }
    }

    public static Drawable getIcon(String text) {
        if (!isEnabled()) return null;

        int index = EmojiDictionary.getInstance().findEmoji(text);
        if (index != -1) {
            Drawable drawable = drawables.get(index);
            if (drawable == null) {
                int s = (Application.getSystemQuality() * 1024) / 64;
                int x = index % 64;
                int y = index / 64;
                drawable = new SpriteMap(imageTexture, x * s, y * s, s, s);
                drawables.put(index, drawable);
            }
            return drawable;
        }
        return null;
    }
}
