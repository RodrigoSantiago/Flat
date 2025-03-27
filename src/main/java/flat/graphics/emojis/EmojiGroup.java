package flat.graphics.emojis;

import java.io.Serializable;
import java.util.List;

public class EmojiGroup implements Serializable {
    private String group;
    private List<EmojiCharacter> emoji;

    public EmojiGroup() {
    }

    public EmojiGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public List<EmojiCharacter> getEmoji() {
        return emoji;
    }

    public void setEmoji(List<EmojiCharacter> emoji) {
        this.emoji = emoji;
    }

    public void add(EmojiCharacter character) {
        emoji.add(character);
    }
}
