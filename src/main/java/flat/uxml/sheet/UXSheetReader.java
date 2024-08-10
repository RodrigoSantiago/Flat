package flat.uxml.sheet;

import flat.graphics.text.FontPosture;
import flat.graphics.text.FontStyle;
import flat.graphics.text.FontWeight;
import flat.resources.Parser;
import flat.uxml.UXValue;
import flat.uxml.value.*;
import flat.widget.Widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UXSheetReader {

    private static final int INVALID    = 0;   // ?
    private static final int TEXT       = 1;   // [a-zA-Z-_]
    private static final int NUMBER     = 2;   // [+-][0-9].[0-9][dp|sp|cm|mm|in|px]
    private static final int VARIABLE   = 3;   // @[a-zA-Z]
    private static final int PARAM      = 4;   // (
    private static final int CPARAM     = 5;   // )
    private static final int BRACE      = 6;   // {
    private static final int CBRACE     = 7;   // }
    private static final int COMMA      = 8;   // ,
    private static final int HEX        = 9;   // #ABCDEF10
    private static final int COLON      = 10;  // :
    private static final int SEMICOLON  = 11;  // ;
    private static final int STRING     = 12;  // "String"|'String'
    private static final int LOCALE     = 13;  // $[a-zA-Z]

    private static final List<String> pseudo = List.of("enabled", "focused", "activated", "hovered", "pressed", "dragged", "error", "disabled");
    private static final char[][] measureChar = {{'c', 'm'}, {'d', 'p'}, {'i', 'n'}, {'m', 'm'}, {'p', 'x', 'c'}, {'s', 'p'}};

    private int pos;
    private int current;
    private int next;
    private int line;
    private int position;
    private String text;
    private StringBuilder builder = new StringBuilder();
    private String currentDebugName;
    private String nextDebugName;

    private String currentText = null;
    private int currentType = -1;
    private String nextText = null;
    private int nextType = -1;
    private int nextLine;
    private int nextPosition;
    private int currentLine;
    private int currentPosition;

    private HashMap<String, UXSheetStyle> styles = new HashMap<>();
    private HashMap<String, UXSheetAttribute> variables = new HashMap<>();
    private List<ErroLog> logs = new ArrayList<>();

    public UXSheetReader(String text) {
        this.text = text;
    }

    public void parse() {
        read();
        while (readNext()) {
            if (currentType == VARIABLE) {
                var variable = parseAttribute();
                variables.put(variable.getName(), variable);

            } else if (currentType == TEXT) {
                var style = parseStyle();
                styles.put(style.getName(), style);

            } else {

                log(ErroLog.UNEXPECTED_TOKEN);
            }
        }
    }

    public void load() {

    }

    public HashMap<String, UXSheetAttribute> getVariables() {
        return variables;
    }

    public HashMap<String, UXSheetStyle> getStyles() {
        return styles;
    }

    public List<ErroLog> getLogs() {
        return logs;
    }

    private UXSheetStyle parseStyle() {
        UXSheetStyle style = new UXSheetStyle(currentText);
        UXSheetStyle styleState = null;
        int state = 0;
        while (readNext()) {
            if (state == 0 && currentType == COLON) {
                state = 1;
            } else if (state == 1 && currentType == TEXT) {
                style.setParent(currentText);
                state = 2;
            } else if ((state == 0 || state == 2) && currentType == BRACE) {
                state = 3;
            } else if (state == 1 && currentType == BRACE) {
                state = 3;
                log(ErroLog.NAME_EXPECTED);

            } else if (state == 3 && currentType == TEXT) {
                if (pseudo.contains(currentText)) {
                    styleState = new UXSheetStyle(currentText);
                    style.addState(styleState);
                    state = 4;
                } else {
                    style.addAttribute(parseAttribute());
                }
            } else if (state == 4 && currentType == BRACE) {
                state = 5;

            } else if (state == 5 && currentType == TEXT) {
                styleState.addAttribute(parseAttribute());

            } else if (state == 5 && currentType == CBRACE) {
                state = 3;
                styleState = null;

            } else if (state == 3 && currentType == CBRACE) {
                state = 6;
                break;

            } else if (currentType == CBRACE) {
                log(ErroLog.UNEXPECTED_TOKEN);
                break;

            } else {
                log(ErroLog.UNEXPECTED_TOKEN);
            }
        }
        if (state != 6) {
            log(ErroLog.UNEXPECTED_END_OF_TOKENS);
        }
        return style;
    }

    private UXSheetAttribute parseAttribute() {
        String name = currentText;
        UXValue value = null;
        int state = 0;
        while (readNext()) {
            if (state == 0 && currentType == COLON) {
                state = 1;
            } else if (state == 1 && (value = parseValue()) != null) {
                state = 2;
            } else if (state == 2 && currentType == SEMICOLON) {
                state = 3;
                break;
            } else if (currentType == INVALID) {
                log(ErroLog.UNEXPECTED_TOKEN);

            } else {
                log(ErroLog.UNEXPECTED_TOKEN);
                break;
            }
            if (nextType == CBRACE) {
                break;
            }
        }
        if (state != 3) {
            log(ErroLog.UNEXPECTED_END_OF_TOKENS);
        }
        return new UXSheetAttribute(name, value == null ? new UXValue() : value);
    }

    private UXValue parseValue() {

        if (currentType == VARIABLE) {
            return new UXValueVariable(currentText);

        } else if (currentType == LOCALE) {
            return new UXValueLocale(currentText);

        } else if (currentType == NUMBER) {
            return parseNumber(currentText);

        } else if (currentType == HEX) {
            return parseHex(currentText);

        } else if (currentType == STRING) {
            return parseString(currentText);

        } else if (currentType == TEXT && nextType != PARAM) {
            return new UXValueText(currentText);

        }

        if (currentType == TEXT) {
            String functionName = currentText;
            List<Object> values =  new ArrayList<>();

            int state = 0;
            while (readNext()) {
                if (state == 0 && currentType == PARAM) {
                    state = 1;

                } else if (state == 1 && currentType == TEXT) {
                    state = 2;
                    values.add(currentText);

                } else if (state == 1 && currentType == STRING ) {
                    state = 2;
                    values.add(parseString(currentText));

                } else if (state == 1 && currentType == NUMBER) {
                    state = 2;
                    values.add(parseNumber(currentText));

                } else if (state == 2 && currentType == COMMA) {
                    state = 1;

                } else if (state == 2 && currentType == CPARAM) {
                    return parseFunction(functionName, values);

                } else {
                    log(ErroLog.UNEXPECTED_TOKEN);
                }
                if (nextType == SEMICOLON || nextType == CBRACE) {
                    break;
                }
            }
            log(ErroLog.UNEXPECTED_END_OF_TOKENS);
            return parseFunction(functionName, values);
        }

        return null;
    }

    private boolean read() {
        while (readNextChar()) {
            if (isWhitespace(current)) {
                consumeWhiteSpace();
                continue;
            }

            if (current == '/' && next == '*') {
                consumeComment();
                continue;
            }

            if (current == '/' && next == '/') {
                consumeLineComment();
                continue;
            }

            readToken();
            return true;
        }
        return false;
    }

    private boolean readNext() {
        currentText = nextText;
        currentType = nextType;
        currentLine = nextLine;
        currentPosition = nextPosition;
        if (!read()) {
            nextText = null;
            nextType = -1;
        }
        nextLine = line;
        nextPosition = position;
        return currentText != null;
    }

    private boolean readNextChar() {
        if (next == -1 || text.length() == 0) {
            return false;
        }

        current = text.codePointAt(pos);
        pos += Character.charCount(current);
        if (pos < text.length()) {
            next = text.codePointAt(pos);
        } else {
            next = -1;
        }
        if (current == '\n') {
            line++;
            position = 0;
        } else {
            position++;
        }

        currentDebugName = current == -1 ? "-1" : new String(Character.toChars(current));
        nextDebugName = next == -1 ? "-1" : new String(Character.toChars(next));
        return true;
    }

    private boolean isWhitespace(int codePoint) {
        return Character.isSpaceChar(codePoint) || Character.isWhitespace(codePoint);
    }

    private boolean isCharacter(int codePoint) {
        return (codePoint >= 'a' && codePoint <= 'z')
                || (codePoint >= 'A' && codePoint <= 'Z')
                || (codePoint >= '0' && codePoint <= '9')
                || codePoint == '-'
                || codePoint == '_';
    }

    private boolean isNumber(int codePoint) {
        return codePoint >= '0' && codePoint <= '9';
    }

    private boolean isHex(int codePoint) {
        return (codePoint >= '0' && codePoint <= '9')
                || (codePoint >= 'A' && codePoint <= 'F')
                || (codePoint >= 'a' && codePoint <= 'f');
    }

    private void consumeWhiteSpace() {
        while (isWhitespace(next)) {
            readNextChar();
        }
    }

    private void consumeComment() {
        readNextChar();
        readNextChar();
        while (current != '*' || next != '/') {
            readNextChar();
        }
        readNextChar();
    }

    private void consumeLineComment() {
        readNextChar();
        readNextChar();
        while (current != '\n') {
            readNextChar();
        }
    }

    private void readToken() {
        if (current == '\'' || current == '"') {
            nextText = readString();
            nextType = STRING;

        } else if (current == '$') {
            nextText = readName();
            nextType = LOCALE;

        } else if (current == '@') {
            nextText = readName();
            nextType = VARIABLE;

        } else if (current == '#') {
            nextText = readHex();
            nextType = HEX;

        }else if (isNumber(current) || ((current == '-' || current == '+' || current == '.') && isNumber(next))) {
            nextText = readNumber();
            nextType = NUMBER;

        } else if (isCharacter(current)) {
            nextText = readName();
            nextType = TEXT;
            if (nextText.equalsIgnoreCase("MATCH_PARENT") || nextText.equalsIgnoreCase("WRAP_CONTENT") ) {
                nextType = NUMBER;
            }

        } else if (current == '(') {
            nextText = "(";
            nextType = PARAM;

        } else if (current == ')') {
            nextText = ")";
            nextType = CPARAM;

        } else if (current == '{') {
            nextText = "{";
            nextType = BRACE;

        } else if (current == '}') {
            nextText = "}";
            nextType = CBRACE;

        } else if (current == ',') {
            nextText = ",";
            nextType = COMMA;

        } else if (current == ':') {
            nextText = ":";
            nextType = COLON;

        } else if (current == ';') {
            nextText = ";";
            nextType = SEMICOLON;

        } else {
            nextText = "";
            nextType = INVALID;

        }
    }

    private String readName() {
        // [a-zA-Z_\-]+

        builder.appendCodePoint(current);
        while (isCharacter(next)) {
            readNextChar();
            builder.appendCodePoint(current);
        }
        String value = builder.toString();
        builder.setLength(0);
        return value;
    }

    private String readNumber() {
        // [0-9+-][0-9.]+

        boolean single = false;
        builder.appendCodePoint(current);
        while (isNumber(next) || (!single && next == '.')) {
            single = single || next == '.';
            readNextChar();
            builder.appendCodePoint(current);
        }
        if (next == 'º') {
            readNextChar();
            builder.appendCodePoint(current);
        } else {
            for (var chars : measureChar) {
                if (next == chars[0]) {
                    readNextChar();
                    if (next == chars[1] || (chars.length > 2 && next == chars[2])) {
                        builder.appendCodePoint(current);
                        readNextChar();
                        builder.appendCodePoint(current);
                    } else {
                        log(ErroLog.UNEXPECTED_TOKEN);
                    }
                    break;
                }
            }
        }

        String value = builder.toString();
        builder.setLength(0);
        return value;
    }

    private String readString() {
        // "STRING"

        boolean scape = false;
        int start = current;
        while ((scape || next != start) && next != '\n' && readNextChar()) {
            scape = current == start && !scape;
            builder.appendCodePoint(current);
        }
        readNextChar();

        String value = builder.toString();
        builder.setLength(0);
        return value;
    }

    private String readHex() {
        // #[a-fA-F0-9]+

        builder.appendCodePoint(current);
        while (isHex(next)) {
            readNextChar();
            builder.appendCodePoint(current);
        }
        String value = builder.toString();
        builder.setLength(0);
        return value;
    }

    private void log(String message) {
        logs.add(new ErroLog(currentLine, currentPosition, message));
    }

    private UXValue parseNumber(String source) {
        if (source.equalsIgnoreCase("MATCH_PARENT")) return new UXValueNumber(Widget.MATCH_PARENT);
        if (source.equalsIgnoreCase("WRAP_CONTENT")) return new UXValueNumber(Widget.WRAP_CONTENT);

        float val;
        try {
            char lastChar = source.charAt(source.length() - 1);
            if (isNumber(lastChar) || lastChar == '.') {
                val = Float.parseFloat(source);
            } else if (lastChar == 'º') {
                val = Float.parseFloat(source.substring(0, source.length() - 1));
                return new UXValueAngle(val);
            } else {
                val = Float.parseFloat(source.substring(0, source.length() - 2));
            }
        } catch (Exception e) {
            log(ErroLog.INVALID_NUMBER);
            return new UXValue();
        }

        if (source.endsWith("px")) {
            return new UXValueNumber(val);
        } else if (source.endsWith("dp")) {
            return new UXValueSizeDp(val);
        } else if (source.endsWith("sp")) {
            return new UXValueSizeSp(val);
        } else if (source.endsWith("in")) {
            return new UXValueSizeIn(val);
        } else if (source.endsWith("pc")) {
            return new UXValueSizeIn(val * 6.0f);
        } else if (source.endsWith("mm")) {
            return new UXValueSizeIn(val * 25.4f);
        } else if (source.endsWith("cm")) {
            return new UXValueSizeIn(val * 2.54f);
        } else {
            return new UXValueNumber(val);
        }
    }

    private UXValue parseHex(String source) {
        try {
            int color;
            if (source.length() == 7) {
                color = (int) ((Long.parseLong(source.substring(1), 16) << 8) | 0x000000FF);
            } else if (source.length() == 9) {
                color = (int) Long.parseLong(source.substring(1), 16);
            } else {
                log(ErroLog.INVALID_COLOR);
                return new UXValue();
            }
            return new UXValueColor(color);
        } catch (Exception e) {
            log(ErroLog.INVALID_COLOR);
            return new UXValue();
        }
    }

    private UXValue parseFunction(String source, List<Object> values) {
        if (source.equalsIgnoreCase("rgb")) {
            if (values.size() == 3 && values.get(0) instanceof UXValueNumber v0
                    && values.get(1) instanceof UXValueNumber v1
                    && values.get(2) instanceof UXValueNumber v2) {
                int r = (int) v0.asNumber(null);
                int g = (int) v1.asNumber(null);
                int b = (int) v2.asNumber(null);
                int rgb = ((r & 0xFF) << 24) | ((g & 0xFF) << 16) | ((b & 0xFF) << 8 ) | (0xFF);
                return new UXValueColor(rgb);
            } else {
                log(ErroLog.INVALID_COLOR);
            }
        } else if (source.equalsIgnoreCase("rgba")) {
            if (values.size() == 4 && values.get(0) instanceof UXValueNumber v0
                    && values.get(1) instanceof UXValueNumber v1
                    && values.get(2) instanceof UXValueNumber v2
                    && values.get(3) instanceof UXValueNumber v3) {
                int r = (int) v0.asNumber(null);
                int g = (int) v1.asNumber(null);
                int b = (int) v2.asNumber(null);
                int a = (int) v3.asNumber(null);
                int rgba = ((r & 0xFF) << 24) | ((g & 0xFF) << 16) | ((b & 0xFF) << 8 ) | (a & 0xFF);
                return new UXValueColor(rgba);
            } else {
                log(ErroLog.INVALID_COLOR);
            }
        } else if (source.equalsIgnoreCase("url")) {
            if (values.size() == 1 && values.get(0) instanceof UXValueText vt) {
                String url = vt.asString(null);
                return new UXValueResource(url);
            } else {
                log(ErroLog.INVALID_URL);
            }
        } else if (source.equalsIgnoreCase("font")) {
            String family = null;
            FontWeight weight = null;
            FontPosture posture = null;
            FontStyle style = null;
            for (var value : values) {
                if (value instanceof UXValueText name) {
                    if (family != null) log(ErroLog.INVALID_FONT);
                    family = name.asString(null);
                } else if (value instanceof String str) {
                    var w = FontWeight.parse(str.toUpperCase());
                    var p = FontPosture.parse(str.toUpperCase());
                    var s = FontStyle.parse(str.toUpperCase());
                    if (w != null) {
                        if (weight != null) log(ErroLog.INVALID_FONT);
                        weight = w;
                    }
                    if (p != null) {
                        if (posture != null) log(ErroLog.INVALID_FONT);
                        posture = p;
                    }
                    if (s != null) {
                        if (style != null) log(ErroLog.INVALID_FONT);
                        style = s;
                    }
                    if (w == null && p == null && s == null) {
                        log(ErroLog.INVALID_FONT);
                    }
                } else if (value instanceof UXValueNumber number) {
                    var w = FontWeight.parse((int) number.asNumber(null));
                    if (w != null) {
                        weight = w;
                    } else {
                        log(ErroLog.INVALID_FONT);
                    }
                } else {
                    log(ErroLog.INVALID_FONT);
                }
            }
            return new UXValueFont(family, weight, posture, style);
        } else {
            log(ErroLog.INVALID_FUNCTION);
        }
        return new UXValue();
    }

    private UXValue parseString(String source) {
        return new UXValueText(Parser.string(source));
    }

    public static class ErroLog {
        public static final String UNEXPECTED_TOKEN = "Unexpected token";
        public static final String UNEXPECTED_END_OF_TOKENS = "Unexpected end of tokens";
        public static final String INVALID_NUMBER = "Invalid number";
        public static final String INVALID_FONT = "Invalid font";
        public static final String INVALID_URL = "Invalid url";
        public static final String INVALID_COLOR = "Invalid color";
        public static final String INVALID_FUNCTION = "Invalid function";
        public static final String NAME_EXPECTED = "Name expected";
        
        private final int line;
        private final int position;
        private final String message;

        public ErroLog(int line, int position, String message) {
            this.line = line;
            this.position = position;
            this.message = message;
        }

        public int getLine() {
            return line;
        }

        public int getPosition() {
            return position;
        }

        public String getMessage() {
            return message;
        }
    }
}
