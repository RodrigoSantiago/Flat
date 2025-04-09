package flat.graphics.emojis;

import java.io.Serializable;

public class EmojiCharacter implements Serializable {
    private int[] base;
    private int[][] alternates;
    private String[] emoticons;
    private String[] shortcodes;
    private String text;
    private Boolean animated;
    private Boolean directional;

    public Boolean getDirectional() {
        return directional;
    }

    public void setDirectional(Boolean directional) {
        this.directional = directional;
    }

    public Boolean getAnimated() {
        return animated;
    }

    public void setAnimated(Boolean animated) {
        this.animated = animated;
    }

    public int[] getBase() {
        return base;
    }

    public void setBase(int[] base) {
        this.base = base;
        if ((base[0] >= 0x1f1e6 && base[0] <= 0x1f1ff) && (base[1] >= 0x1f1e6 && base[1] <= 0x1f1ff)) {
            text = new String(base, 0, 2);
            return;
        }

        int len = 0;
        int[] max = new int[12];
        for (int i = 0; i < base.length; i++) {
            if (base[i] == 0) break;
            int j = base[i];
            if (i > 0 && j != 0x1F3FB && j != 0x1F3FC && j != 0x1F3FD && j != 0x1F3FE && j != 0x1F3FF) {
                max[len++] = 0x200D;
            }
            max[len++] = j;
        }
        text = new String(max, 0, len);
    }

    public String getText() {
        return text;
    }

    public int[][] getAlternates() {
        return alternates;
    }

    public void setAlternates(int[][] alternates) {
        this.alternates = alternates;
    }

    public String[] getEmoticons() {
        return emoticons;
    }

    public void setEmoticons(String[] emoticons) {
        this.emoticons = emoticons;
    }

    public String[] getShortcodes() {
        return shortcodes;
    }

    public void setShortcodes(String[] shortcodes) {
        this.shortcodes = shortcodes;
    }
}
