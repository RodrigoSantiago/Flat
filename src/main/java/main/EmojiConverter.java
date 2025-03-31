package main;

import flat.graphics.Graphics;
import flat.graphics.Surface;
import flat.graphics.context.enums.MagFilter;
import flat.graphics.context.enums.MinFilter;
import flat.graphics.emojis.EmojiCharacter;
import flat.graphics.emojis.EmojiDictionary;
import flat.graphics.emojis.EmojiGroup;
import flat.graphics.image.PixelMap;
import flat.resources.ResourceStream;
import flat.window.Application;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;

import com.fasterxml.jackson.databind.ObjectMapper;

public class EmojiConverter {

    private static String toStr(String arr) {
        return "\"" + arr.replaceAll("\"", "\\\\\"") + "\"";
    }

    private static String toStr(String[] arr, String s) {
        StringBuilder sb = new StringBuilder();
        for (var i : arr) {
            if (!sb.isEmpty()) sb.append(s);
            sb.append(i);
        }
        return sb.toString();
    }

    private static String toStr(int[] arr) {
        // 8.205
        if (arr[0] < 58) {
            arr = new int[] {arr[2], arr[0]};
        }
        StringBuilder sb = new StringBuilder();
        for (var i : arr) {
            if (i != 0x200D && i != 0xFE0F && i!= 0xFE0E) {
                if (!sb.isEmpty()) sb.append(",");
                sb.append(Integer.toHexString(i).toUpperCase());
            }
        }
        return sb.toString();
    }

    private static String convert2(int[] numbers) {
        int count = 0;
        if (numbers[0] < 58) {
            numbers = new int[] {numbers[2], numbers[0]};
        }
        for (var i : numbers) {
            if (i != 0x200D && i != 0xFE0F && i!= 0xFE0E && i != 0) {
                count++;
            }
        }
        int[] arr = new int[count];
        int p = 0;
        for (var i : numbers) {
            if (i != 0x200D && i != 0xFE0F && i != 0xFE0E && i != 0) {
                arr[p++] = i;
            }
        }

        return new String(arr, 0, arr.length);
    }

    private static String convert(int[] numbers) {
        int count = 0;
        for (var i : numbers) {
            if (i != 0x200D && i != 0xFE0F && i!= 0xFE0E && i != 0) {
                count++;
            }
        }
        int[] arr = new int[count];
        int p = 0;
        for (var i : numbers) {
            if (i != 0x200D && i != 0xFE0F && i != 0xFE0E && i != 0) {
                arr[p++] = i;
            }
        }

        return new String(arr, 0, arr.length);
    }

    public static void convertNotoMetaToDic(String path) {
        String[] lines = new String(new ResourceStream("/default/emojis/emojis.txt").readData(), StandardCharsets.UTF_8).split("\n");
        int[] arr = new int[6];
        HashSet<String> allEmojis = new HashSet<>();
        for (int i = 0; i < lines.length; i+= 6) {
            arr[0] = Integer.parseInt(lines[i].trim());
            arr[1] = Integer.parseInt(lines[i + 1].trim());
            arr[2] = Integer.parseInt(lines[i + 2].trim());
            arr[3] = Integer.parseInt(lines[i + 3].trim());
            arr[4] = Integer.parseInt(lines[i + 4].trim());
            arr[5] = Integer.parseInt(lines[i + 5].trim());
            allEmojis.add(convert(arr));
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
           // EmojiGroup[] emojiDictionary = EmojiDictionary.getInstance().getGroups().toArray(new EmojiGroup[0]);
            EmojiGroup[] emojiDictionary = objectMapper.readValue(new File(path), EmojiGroup[].class);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < emojiDictionary.length; i++) {
                var group = emojiDictionary[i];
                sb.append("group=" + group.getGroup()).append("\n");
                var chars = group.getEmoji();
                for (EmojiCharacter emoji : chars) {
                    if (!allEmojis.contains(convert2(emoji.getBase()))) {
                        continue;
                    }

                    sb.append("emoji=").append(toStr(emoji.getBase()));
                    sb.append("\t").append(toStr(emoji.getEmoticons(), "  "));
                    sb.append("\t").append(toStr(emoji.getShortcodes(), "  "));
                    sb.append("\t");
                    var alternates = emoji.getAlternates();
                    for (int j = 0; j < alternates.length; j++) {
                        int[] alternate = alternates[j];
                        if (!allEmojis.contains(convert2(alternates[i]))) {
                            continue;
                        }
                        if (j != 0) sb.append("  ");
                        sb.append(toStr(alternate));
                    }
                    sb.append("\n");
                }
            }
            //System.out.println(sb.toString());
            Files.write(new File("C:/Nova/emojis.doc").toPath(), sb.toString().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PixelMap createFromNotoEmoji(String path, ArrayList<int[]> allUnicodes, int size) {
        File file = new File(path);
        File[] children = file.listFiles();

        Graphics gr = Application.getCurrentContext().getGraphics();
        Surface surface = new Surface(size, size);
        gr.setTransform2D(null);
        gr.setSurface(surface);
        gr.clear(0x0, 0x0, 0x0);
        StringBuilder sb = new StringBuilder();

        int iconSize = size / 64;

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
            map.getTexture().setLevels(8);
            map.getTexture().generateMipmapLevels();
            map.getTexture().setScaleFilters(MagFilter.LINEAR, MinFilter.LINEAR_MIPMAP_LINEAR);
            if (map.getWidth() != map.getHeight()) {
                if (iconSize < 32) {
                }
                float d = map.getWidth() / map.getHeight();
                int w, h;
                if (d > 1) {
                    w = iconSize;
                    h = (int) (w / d);
                } else {
                    h = iconSize;
                    w = (int) (h * d);
                }
                gr.drawImage(map, i % 64 * iconSize + (iconSize - w) / 2.0f, i / 64 * iconSize + (iconSize - h) / 2.0f, w, h);
            } else {
                gr.drawImage(map, i % 64 * iconSize, i / 64 * iconSize, iconSize, iconSize);
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
