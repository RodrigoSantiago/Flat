package flat.graphics.emojis;

import flat.backend.SVG;
import flat.graphics.context.Texture2D;
import flat.graphics.context.enums.MagFilter;
import flat.graphics.context.enums.MinFilter;
import flat.graphics.context.enums.PixelFormat;
import flat.graphics.context.enums.WrapMode;
import flat.resources.ResourceStream;
import flat.window.Application;

import java.nio.charset.StandardCharsets;

public class EmojiManager {

    private static Texture2D emojiTexture;

    private static boolean enabled;

    public static boolean isEnabled() {
        return enabled;
    }

    public static void load(ResourceStream image, ResourceStream unicodes) {
        SVG.FontSetEmojiEnabled(enabled = true);
        EmojiDictionary.getInstance();
        new Thread(() -> {
            try {
                String[] lines = new String(unicodes.readData(), StandardCharsets.UTF_8).split("\n");
                int[] emojis = new int[lines.length];
                for (int i = 0; i < lines.length; i++) {
                    emojis[i] = Integer.parseInt(lines[i].trim());
                }
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
                    SVG.FontCreateEmoji(emojiTexture.getInternalId(), emojis);
                    for (var window : Application.getAssignedWindows()) {
                        window.getActivity().invalidate();
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
}
