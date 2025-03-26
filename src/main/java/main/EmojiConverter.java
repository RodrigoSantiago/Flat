package main;

import flat.graphics.Graphics;
import flat.graphics.Surface;
import flat.graphics.image.PixelMap;
import flat.resources.ResourceStream;
import flat.window.Application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class EmojiConverter {

    public static PixelMap createFromNotoEmoji(String path, ArrayList<int[]> allUnicodes) {
        File file = new File(path);
        File[] children = file.listFiles();

        Graphics gr = Application.getCurrentContext().getGraphics();
        Surface surface = new Surface(Application.getCurrentContext(), 4096, 4096);
        gr.setTransform2D(null);
        gr.setSurface(surface);
        gr.clear(0x0, 0x0, 0x0);
        StringBuilder sb = new StringBuilder();

        int[] unicodes = new int[20];
        for (int i = 0; i < children.length; i++) {
            if (i % 100 == 0) System.out.println("100");
            var child = children[i];
            String name = child.getName();
            String emojiname = name.substring(name.indexOf("_") + 1, name.indexOf("."));
            String[] chars = emojiname.split("_");
            int p = 0;
            for (int j = 0; j < chars.length; j++) {
                if (!chars[j].equals("200d")) {
                    if (chars[j].startsWith("u")) chars[j] = chars[j].substring(1);
                    unicodes[p++] = Integer.parseInt(chars[j], 16);
                }
            }
            while (p < 6) {
                unicodes[p++] = 0;
            }
            if (p > 6) {
                System.out.println("Extra AT " + name);
            }
            int[] uni = new int[6];
            System.arraycopy(unicodes, 0, uni, 0, 6);
            allUnicodes.add(uni);

            PixelMap map = PixelMap.parse(new ResourceStream(child));
            if (map.getWidth() != map.getHeight()) {
                float d = map.getWidth() / map.getHeight();
                int w, h;
                if (d > 1) {
                    w = 64;
                    h = (int) (w / d);
                } else {
                    h = 64;
                    w = (int) (h * d);
                }
                gr.drawImage(map, i % 64 * 64 + (64 - w) / 2.0f, i / 64 * 64 + (64 - h) / 2.0f, w, h);
            } else {
                gr.drawImage(map, i % 64 * 64, i / 64 * 64, 64, 64);
            }
        }
        PixelMap complete = gr.createPixelMap();
        gr.setSurface(null);

        return complete;
    }

    public static void renameFlags(String path) throws IOException {
        File file = new File(path);
        File[] children = file.listFiles();
        for (int i = 0; i < children.length; i++) {
            if (i % 100 == 0) System.out.println("100");
            var child = children[i];
            String name = child.getName().substring(0, child.getName().indexOf(".")).toUpperCase();
            if (name.length() == 2) {
                char a = name.charAt(0);
                char b = name.charAt(1);
                int numA = (a - ('A')) + 0x1f1e6;
                int numB = (b - ('A')) + 0x1f1e6;

                File destin = new File(child.getParent(),
                        "emoji_u" + Integer.toHexString(numA) + "_" + Integer.toHexString(numB) + ".png");
                if (child.length() < 50) {
                    String str = Files.readString(child.toPath());
                    Files.copy(new File(child.getParent(), str).toPath(), destin.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        for (int i = 0; i < children.length; i++) {
            if (i % 100 == 0) System.out.println("100");
            var child = children[i];
            String name = child.getName().substring(0, child.getName().indexOf(".")).toUpperCase();
            if (name.length() == 2) {
                char a = name.charAt(0);
                char b = name.charAt(1);
                int numA = (a - ('A')) + 0x1f1e6;
                int numB = (b - ('A')) + 0x1f1e6;

                File destin = new File(child.getParent(),
                        "emoji_u" + Integer.toHexString(numA) + "_" + Integer.toHexString(numB) + ".png");
                if (child.length() > 50) {
                    System.out.println(child.renameTo(destin));
                }
            }
        }
    }
}
