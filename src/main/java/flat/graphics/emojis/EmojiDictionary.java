package flat.graphics.emojis;

import flat.resources.ResourceStream;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class EmojiDictionary implements Serializable {

    private static EmojiDictionary instance;
    private List<EmojiGroup> groups;

    public EmojiDictionary(EmojiGroup... groups) {
        this.groups = List.of(groups);
    }

    public List<EmojiGroup> getGroups() {
        return groups;
    }

    public static EmojiDictionary getInstance() {
        if (instance == null) {
            instance = read(new String(new ResourceStream("/default/emojis/emojis.dic").readData(), StandardCharsets.UTF_8));
        }
        return instance;
    }

    private static EmojiDictionary read(String content) {
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
        return new EmojiDictionary(groups.toArray(new EmojiGroup[0]));
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