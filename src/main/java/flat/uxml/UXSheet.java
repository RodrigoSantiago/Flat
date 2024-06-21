package flat.uxml;

import flat.exception.FlatException;
import flat.graphics.context.Font;
import flat.graphics.text.FontPosture;
import flat.graphics.text.FontWeight;
import flat.math.Mathf;
import flat.resources.Parser;
import flat.resources.ResourceStream;
import flat.uxml.value.*;
import flat.widget.State;
import flat.widget.Widget;
import org.tinylog.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class UXSheet {

    private final HashMap<String, UXStyle> styles = new HashMap<>();
    private final UXSheet parent;

    public static UXSheet parse(ResourceStream stream) {
        Object cache = stream.getCache();
        if (cache != null) {
            if (cache instanceof UXSheet) {
                return (UXSheet) cache;
            } else {
                throw new FlatException("Invalid UXSheet at: " + stream.getStream());
            }
        }
        UXSheet sheet;
        try {
            sheet = parse(new String(stream.getStream().readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new FlatException(e);
        }
        stream.putCache(sheet);
        return sheet;
    }

    public static UXSheet parse(String str) {
        return readFrom(str);
    }

    public UXSheet() {
        this.parent = null;
    }

    public UXTheme instance() {
        return new UXTheme(this, 160f, 1f, null);
    }

    public UXSheet getParent() {
        return parent;
    }

    public UXStyle getStyle(String name) {
        UXStyle style = styles.get(name);
        if (style == null && parent != null) {
            return parent.getStyle(name);
        } else {
            return style;
        }
    }

    private static UXSheet readFrom(String data) {
        UXSheet sheet = new UXSheet();

        String styleName = null;
        String attrName = null;
        UXStyle style = null;

        boolean scaped = false, inverseBar = false;
        StringBuilder string = new StringBuilder();
        int state = 0;
        State styleState = State.ENABLED;

        int index = 0;
        int line = 0;
        while (index < data.length()) {
            int c = data.charAt(index);
            char debug = (char) c;
            index += Character.charCount(c);

            boolean letter = c == '-' || Character.isLetterOrDigit(c);

            if (index == '\n') {
                line++;
            }

            if (state == 0 && letter) {
                string.appendCodePoint(c);
                state = 1;
            } else if (state == 0 && c == '/') {
                state = -1;
            } else if (state == -1 && c == '*') {
                state = -2;
            } else if (state == -1) {
                state = 0;
            } else if (state == -2 && c == '*') {
                state = -3;
            } else if (state == -3 && c == '/') {
                state = 0;
            } else if (state == -3) {
                state = -2;
            }
            // [nome]
            else if (state == 1 && letter) {
                string.appendCodePoint(c);
            } else if (state == 1 && c == ':') {
                styleName = string.toString();
                string.setLength(0);
                state = 3;  // wait PARENT START NAME
            } else if (state == 1 && c == '{') {
                styleName = string.toString();
                string.setLength(0);
                state = 5;  // wait ATTRIBUTE START
            } else if (state == 1) {
                styleName = string.toString();
                string.setLength(0);
                state = 2;  // wait KEYS : OR {
            } else if (state == 2 && c == ':') {
                state = 3;  // wait PARENT START NAME
            } else if (state == 2 && c == '{') {
                state = 5;  /// wait ATTRIBUTE START

                style = new UXStyle(styleName);
                sheet.styles.put(style.name, style);
                styleState = State.ENABLED;
            } else if (state == 3 && letter) {
                string.appendCodePoint(c);
                state = 4;  // wait PARENT END NAME
            } else if (state == 4 && letter) {
                string.appendCodePoint(c);
            } else if (state == 4 && c == '{') {
                String parentName = string.toString();
                string.setLength(0);
                state = 5;  // wait ATTRIBUTE START

                style = new UXStyle(styleName, parentName);
                sheet.styles.put(style.name, style);
                styleState = State.ENABLED;
            } else if ((state == 5 || state == 8) && letter) {
                string.appendCodePoint(c);
                state = state + 1; // wait ATTRIBUTE END[6, 9]
            } else if ((state == 6 || state == 9) && letter) {
                string.appendCodePoint(c);
            } else if ((state == 6 || state == 9) && c == ':') {
                attrName = string.toString();
                string.setLength(0);
                state = state + 1; // wait VALUE START[7, 10]
            } else if (state == 6 && c == '{') {
                attrName = string.toString();
                string.setLength(0);
                state = 8; // wait INNTER ATTRIBUTE START

                if (attrName.equals("default") || attrName.equals("enabled")) {
                    styleState = State.ENABLED;
                } else if (attrName.equals("focused")) {
                    styleState = State.FOCUSED;
                } else if (attrName.equals("activated")) {
                    styleState = State.ACTIVATED;
                } else if (attrName.equals("hovered")) {
                    styleState = State.HOVERED;
                } else if (attrName.equals("pressed")) {
                    styleState = State.PRESSED;
                } else if (attrName.equals("dragged")) {
                    styleState = State.DRAGGED;
                } else if (attrName.equals("error")) {
                    styleState = State.ERROR;
                } else if (attrName.equals("disabled")) {
                    styleState = State.DISABLED;
                } else {
                    styleState = State.ENABLED;
                    Logger.error("Unexpected state at line " + line);
                }
            } else if ((state == 7 || state == 10) && c == ';' && !scaped) {
                String attrValue = string.toString();
                string.setLength(0);
                state = state - 2;  // wait ATTRIBUTE [INNER] START[5, 8]

                style.add(UXHash.getHash(attrName), styleState, readValue(attrValue.trim(), true));
            } else if ((state == 7 || state == 10) && c == '\\') {
                string.appendCodePoint(c);
                if (scaped) {
                    inverseBar = !inverseBar;
                }
            } else if ((state == 7 || state == 10) && c == '"') {
                if (!scaped) {
                    scaped = true;
                } else if (!inverseBar) {
                    scaped = false;
                }
                string.appendCodePoint(c);
                inverseBar = false;
            } else if (state == 7 || state == 10) {
                string.appendCodePoint(c);
                inverseBar = false;
            } else if (state == 5 && c == '}') {
                state = 0;  // WAIT STYLE
            } else if (state == 8 && c == '}') {
                state = 5;  // wait ATTRIBUTE START
            } else if (c == '}') {
                state = 0;  // RESET
                styleState = State.ENABLED;
                Logger.error("Unexpected closure at line " + line);
            } else if (!Character.isSpaceChar(c)) {
                Logger.error("Unexpected character at line " + line);
            }
        }

        for (UXStyle s : sheet.styles.values()) {
            s.setParent(sheet.styles.get(s.parentName));
        }

        return sheet;
    }

    public static UXValue readValue(String source, boolean stringSource) {
        if (stringSource && source.startsWith("\"") && source.endsWith("\"")) {
            return new UXValueText(Parser.string(source.substring(1, source.length() - 1)));
        }
        if (source.equalsIgnoreCase("true") || source.equalsIgnoreCase("false")) {
            return new UXValueBool(source.equalsIgnoreCase("true"));
        }
        if (source.matches("-?\\d+(\\.\\d+)?((px)|(dp)|(sp)|(in)|(pt)|(pc)|(mm)|(cm))?")) {
            return parseNumber(source);
        }
        if (source.equalsIgnoreCase("wrap_content")) {
            return new UXValueNumber(Widget.WRAP_CONTENT);
        }
        if (source.equalsIgnoreCase("match_parent")) {
            return new UXValueNumber(Widget.MATCH_PARENT);
        }
        if (source.matches("-?\\d+(\\.d+)?º")) {
            return new UXValueAngle(Float.parseFloat(source.substring(0, source.length() - 1)));
        }
        if (source.matches("#[ABCDEFabcdef0-9]{6}([ABCDEFabcdef0-9]{2})?")) {
            if (source.length() == 7) {
                return new UXValueColor(((int) Long.parseLong(source.substring(1), 16) << 8) | 0x000000FF);
            } else {
                return new UXValueColor((int) Long.parseLong(source.substring(1), 16));
            }
        }
        if (source.matches("(rgb|rgba)\\(.+\\)")) {
            return parseColor(source);
        }
        if (source.matches("font\\(.+\\)")) {
            return parseFont(source);
        }
        if (source.matches("url\\(.+\\)")) {
            return new UXValueResource(source.substring(4, source.length() - 1).trim());
        }
        if (source.startsWith("$")) {
            return new UXValueLocale(source.substring(1));
        }
        if (source.startsWith("@")) {
            return new UXValueVariable(source.substring(1));
        }
        return new UXValueText(source);
    }

    private static UXValue parseNumber(String source) {
        float val;
        if (source.matches("-?\\d+(\\.\\d+)?")) {
            val = Float.parseFloat(source);
        } else {
            val = Float.parseFloat(source.substring(0, source.length() - 2));
        }

        if (source.endsWith("px")) {
            return new UXValueNumber(val);
        } else if (source.endsWith("dp")) {
            return new UXValueSizeDp(val);
        } else if (source.endsWith("sp")) {
            return new UXValueSizeSp(val);
        } else if (source.endsWith("in")) {
            return new UXValueSizeIn(val * 160);
        } else if (source.endsWith("pt")) {
            return new UXValueSizeIn(val * 160 / 72.0f);
        } else if (source.endsWith("pc")) {
            return new UXValueSizeIn(val * 160 / 6.0f);
        } else if (source.endsWith("mm")) {
            return new UXValueSizeIn(val * 160 / 25.4f);
        } else if (source.endsWith("cm")) {
            return new UXValueSizeIn(val * 160 / 2.54f);
        } else {
            return new UXValueNumber(val);
        }
    }

    private static UXValue parseColor(String source) {
        String[] blocks;
        byte[] rgba;
        int n;
        if (source.startsWith("rgb(")) {
            rgba = new byte[]{0, 0, 0, 1};
            n = 3;
            blocks = source.substring(4, source.length() - 1).split(",");
        } else {
            rgba = new byte[]{0, 0, 0, 0};
            n = 4;
            blocks = source.substring(5, source.length() - 1).split(",");
        }
        for (int i = 0; i < blocks.length && i < n; i++) {
            try {
                float f = Float.parseFloat(blocks[i].trim());
                f = Math.max(0, Math.min(1, f));
                if (!Float.isNaN(f)) {
                    rgba[i] = (byte) Mathf.round(f * 255);
                }
            } catch (Exception e) {
            }
        }
        int color = ((rgba[3] & 0xFF) << 24) |
                ((rgba[2] & 0xFF) << 16) |
                ((rgba[1] & 0xFF) << 8 ) |
                (rgba[0] & 0xFF);
        return new UXValueColor(color);
    }

    private static UXValue parseFont(String source) {
        int state = 0;
        int start = -1;
        int end = -1;
        boolean inverseBar = false;
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (state == 0 && c == '"') {
                state = 1;
                start = i;
            } else if (state == 1 && c == '"' && !inverseBar) {
                end = i;
                break;
            }
            inverseBar = state == 1 && c == '\\' && !inverseBar;
        }

        String family = null;
        String generic = "Roboto";
        FontWeight weight = FontWeight.NORMAL;
        FontPosture posture = FontPosture.REGULAR;
        if (start != -1 && end != -1) {
            family = source.substring(start + 1, end);
        }
        if (end == -1) end = 4;
        for (String str : source.substring(end + 1, source.length() - 1).split(",")) {
            str = str.toUpperCase().trim();
            switch (str) {
                case "BLACK" -> weight = FontWeight.BLACK;
                case "EXTRA_BOLD" -> weight = FontWeight.EXTRA_BOLD;
                case "BOLD" -> weight = FontWeight.BOLD;
                case "SEMI_BOLD" -> weight = FontWeight.SEMI_BOLD;
                case "MEDIUM" -> weight = FontWeight.MEDIUM;
                case "NORMAL" -> weight = FontWeight.NORMAL;
                case "LIGHT" -> weight = FontWeight.LIGHT;
                case "EXTRA_LIGHT" -> weight = FontWeight.EXTRA_LIGHT;
                case "THIN" -> weight = FontWeight.THIN;
                case "ITALIC" -> posture = FontPosture.ITALIC;
                case "SERIF" -> generic = "Serif";
                case "MONO", "MONOSPACE" -> generic = "Mono";
                case "SANS", "SANS-SERIF" -> generic = "Sans";
                case "FANTASY", "CURSIVE" -> generic = "Cursive";
            }
        }
        if (family == null) {
            family = generic;
        }
        return new UXValueFont(generic, family, weight, posture);
    }
}