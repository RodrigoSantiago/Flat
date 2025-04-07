package flat.graphics.emojis;

import flat.resources.ResourceStream;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EmojiDictionary implements Serializable {

    private static EmojiDictionary instance;
    private List<EmojiGroup> groups;
    private int[] unicodes;
    private HashMap<String, EmojiCharacter> shortcodeMap = new HashMap<>();
    private HashMap<String, Integer> characterIndex = new HashMap<>();

    public EmojiDictionary(EmojiGroup[] groups, int[] unicodes) {
        this.groups = List.of(groups);
        this.unicodes = unicodes;
        for (int i = 0; i < unicodes.length; i += 6) {
            characterIndex.put(fromIntArray(unicodes, i, 6), characterIndex.size());
        }
        for (var group : groups) {
            for (var emoji : group.getEmoji()) {
                for (var shortCodes : emoji.getShortcodes()) {
                    shortcodeMap.put(shortCodes.substring(1, shortCodes.length() - 1), emoji);
                }
            }
        }
    }

    private String fromIntArray(int[] chr, int off, int length) {
        int count = 0;
        int[] chars = new int[6];
        for (int i = 0; i < length; i++) {
            int c = chr[off + i];
            if (c != 0x200D && c != 0xFE0F && c != 0xFE0E && c != 0) {
                chars[count++] = c;
                if (count == 6) break;
            }
        }
        return new String(chars, 0, count);
    }

    public List<EmojiGroup> getGroups() {
        return groups;
    }

    public int[] getUnicodes() {
        return unicodes;
    }

    public int findEmoji(String shortCode) {
        var chr = shortcodeMap.get(shortCode);
        if (chr != null) {
            Integer index = characterIndex.get(fromIntArray(chr.getBase(), 0, chr.getBase().length));
            if (index != null) {
                return index;
            }
        }

        var codePoints = shortCode.codePoints().toArray();
        Integer index = characterIndex.get(fromIntArray(codePoints, 0, codePoints.length));
        if (index != null) {
            return index;
        }
        return  -1;
    }

    public static EmojiDictionary getInstance() {
        if (instance == null) {
            instance = read(
                    new String(new ResourceStream("/default/emojis/emojis.dic").readData(), StandardCharsets.UTF_8),
                    new String(new ResourceStream("/default/emojis/emojis.txt").readData(), StandardCharsets.UTF_8));
        }
        return instance;
    }

    private static EmojiDictionary read(String content, String emojiCharacters) {
        String[] lines = content.split("\n");
        ArrayList<EmojiGroup> groups = new ArrayList<>();
        EmojiGroup group = null;
        for (var line : lines) {
            if (line.startsWith("group")) {
                group = new EmojiGroup(line.substring(line.indexOf("=") + 1));
                group.setEmoji(new ArrayList<>());
                groups.add(group);
            } else if (line.startsWith("emoji")) {
                EmojiCharacter character = new EmojiCharacter();
                String[] parts = line.substring(line.indexOf("=") + 1).split("\t", -1);
                character.setBase(readUnicodeCharacter(parts[0]));
                if (parts[1].isEmpty()) {
                    character.setEmoticons(new String[0]);
                } else {
                    character.setEmoticons(parts[1].split("  ", -1));
                }
                if (parts[2].isEmpty()) {
                    character.setShortcodes(new String[0]);
                } else {
                    character.setShortcodes(parts[2].split("  ", -1));
                }
                if (parts[3].isEmpty()) {
                    character.setAlternates(new int[0][]);
                } else {
                    String[] alternatesText = parts[3].split("  ", -1);
                    int[][] alternates = new int[alternatesText.length][];
                    for (int i = 0; i < alternates.length; i++) {
                        alternates[i] = readUnicodeCharacter(alternatesText[i]);
                    }
                    character.setAlternates(alternates);
                }
                group.add(character);
            }
        }

        String[] emojiLines = emojiCharacters.split("\n");
        int[] unicodes = new int[emojiLines.length];
        for (int i = 0; i < emojiLines.length; i++) {
            unicodes[i] = Integer.parseInt(emojiLines[i].trim());
        }

        return new EmojiDictionary(groups.toArray(new EmojiGroup[0]), unicodes);
    }

    private static int[] readUnicodeCharacter(String numbers) {
        String[] each = numbers.split(",", -1);
        int[] unicodes = new int[6];
        for (int i = 0; i < each.length; i++) {
            unicodes[i] = Integer.parseInt(each[i], 16);
        }
        return unicodes;
    }
}