package flat.widget.text.styled;

import java.util.ArrayList;
import java.util.HashMap;

public class TextStyleBundle {
    
    private int stringStyle;
    private int charsetStyle;
    private int numberStyle;
    private int hexStyle;
    private int commentStyle;
    
    private HashMap<Character, Integer> charStyle;
    private HashMap<TextWord, Integer> wordStyles;
    private HashMap<TextWord, Integer> identifierStyles;
    private ArrayList<TextStyle> styles;
    
    public TextStyleBundle(TextStyle defaultStyle) {
        wordStyles = new HashMap<>();
        charStyle = new HashMap<>();
        styles = new ArrayList<>();
        styles.add(defaultStyle);
        identifierStyles = new HashMap<>();
    }
    
    public TextStyleBundle(TextStyleBundle copy) {
        this.stringStyle = copy.stringStyle;
        this.charsetStyle = copy.charsetStyle;
        this.numberStyle = copy.numberStyle;
        this.hexStyle = copy.hexStyle;
        this.commentStyle = copy.commentStyle;
        this.wordStyles = new HashMap<>(copy.wordStyles);
        this.styles = new ArrayList<>(copy.styles);
        this.charStyle = new HashMap<>(copy.charStyle);
        this.identifierStyles = new HashMap<>(copy.identifierStyles);
    }
    
    public int findCharStyle(char letter) {
        Integer i = charStyle.get(letter);
        if (i == null) return 0;
        return i;
    }
    
    public int findWordStyle(TextWord search) {
        Integer i = wordStyles.get(search);
        if (i == null) return 0;
        return i;
    }
    
    public int findIdentifierStyle(TextWord search) {
        Integer i = identifierStyles.get(search);
        if (i == null) return 0;
        return i;
    }
    
    public int getStringStyle() {
        return stringStyle;
    }
    
    public int getCharsetStyle() {
        return charsetStyle;
    }
    
    public int getNumberStyle() {
        return numberStyle;
    }
    
    public int getHexStyle() {
        return hexStyle;
    }
    
    public int getCommentStyle() {
        return commentStyle;
    }
    
    public void addCharStyle(char letter, TextStyle style) {
        if (!charStyle.containsKey(letter)) {
            int index = styles.indexOf(style);
            if (index == -1) {
                index = styles.size();
                styles.add(style);
            }
            charStyle.put(letter, index);
        }
    }
    
    public void addWordStyle(String word, TextStyle style) {
        var tk = new TextWord(word);
        if (!wordStyles.containsKey(tk)) {
            int index = styles.indexOf(style);
            if (index == -1) {
                index = styles.size();
                styles.add(style);
            }
            wordStyles.put(tk, index);
        }
    }
    
    public void addIdentifierStyle(String word, TextStyle style) {
        var tk = new TextWord(word);
        if (!identifierStyles.containsKey(tk)) {
            int index = styles.indexOf(style);
            if (index == -1) {
                index = styles.size();
                styles.add(style);
            }
            identifierStyles.put(tk, index);
        }
    }
    
    public void setStringStyle(TextStyle style) {
        int index = styles.indexOf(style);
        if (index == -1) {
            index = styles.size();
            styles.add(style);
        }
        stringStyle = index;
    }
    
    public void setCharsetStyle(TextStyle style) {
        int index = styles.indexOf(style);
        if (index == -1) {
            index = styles.size();
            styles.add(style);
        }
        charsetStyle = index;
    }
    
    public void setNumberStyle(TextStyle style) {
        int index = styles.indexOf(style);
        if (index == -1) {
            index = styles.size();
            styles.add(style);
        }
        numberStyle = index;
    }
    
    public void setHexStyle(TextStyle style) {
        int index = styles.indexOf(style);
        if (index == -1) {
            index = styles.size();
            styles.add(style);
        }
        hexStyle = index;
    }
    
    public void setCommentStyle(TextStyle style) {
        int index = styles.indexOf(style);
        if (index == -1) {
            index = styles.size();
            styles.add(style);
        }
        commentStyle = index;
    }
    
    public int[] getLocalStyles() {
        int[] localStyles = new int[styles.size() * 3];
        for (int i = 0; i < styles.size(); i++) {
            var style = styles.get(i);
            localStyles[i * 3] = style.getColor();
            localStyles[i * 3 + 1] = style.isBold() ? 1 : 0;
            localStyles[i * 3 + 2] = style.isItalic() ? 1 : 0;
        }
        return localStyles;
    }
}
