package flat.graphics.context;

import flat.backend.SVG;
import flat.graphics.context.enums.MagFilter;
import flat.graphics.context.enums.MinFilter;
import flat.graphics.context.enums.PixelFormat;
import flat.graphics.context.enums.WrapMode;
import flat.resources.ResourceStream;
import flat.window.Application;

import java.nio.charset.StandardCharsets;

public class EmojiManager {

    private static Texture2D emojiTexture;

    public static void load(ResourceStream image, ResourceStream unicodes) {
        new Thread(() -> {
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
        }).start();
    }

    public static void unload() {
        emojiTexture.dispose();
        emojiTexture = null;
        SVG.FontDestroyEmoji();
    }
}
