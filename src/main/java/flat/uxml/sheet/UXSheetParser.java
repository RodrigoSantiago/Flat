package flat.uxml.sheet;

import flat.graphics.Color;
import flat.graphics.text.FontPosture;
import flat.graphics.text.FontStyle;
import flat.graphics.text.FontWeight;
import flat.resources.Parser;
import flat.uxml.value.UXValue;
import flat.uxml.value.*;

import java.util.ArrayList;
import java.util.List;

public class UXSheetParser {

    private static final int INVALID    = 0;   // ?
    private static final int TEXT       = 1;   // [a-zA-Z-_]
    private static final int NUMBER     = 2;   // [+-][0-9].[0-9][dp|sp|cm|mm|in|px]
    private static final int VARIABLE   = 3;   // $[a-zA-Z]
    private static final int PARAM      = 4;   // (
    private static final int CPARAM     = 5;   // )
    private static final int BRACE      = 6;   // {
    private static final int CBRACE     = 7;   // }
    private static final int COMMA      = 8;   // ,
    private static final int HEX        = 9;   // #ABCDEF10
    private static final int COLON      = 10;  // :
    private static final int SEMICOLON  = 11;  // ;
    private static final int STRING     = 12;  // "String"|'String'
    private static final int LOCALE     = 13;  // @[a-zA-Z]

    private static final List<String> pseudo = List.of("enabled", "focused", "activated", "hovered", "pressed", "dragged", "undefined", "disabled");
    private static final List<String> lists = List.of("list", "translate", "scale", "rotate", "skewX", "skewX", "matrix");
    private static final char[][] measureChar = {{'c', 'm'}, {'d', 'p'}, {'i', 'n'}, {'m', 'm'}, {'p', 'x', 'c'}, {'s', 'p'}};

    private int pos;
    private int current;
    private int next;
    private int line;
    private int position;
    private String text;
    private StringBuilder builder = new StringBuilder();

    private String currentText = null;
    private int currentType = -1;
    private String nextText = null;
    private int nextType = -1;
    private int nextLine;
    private int nextPosition;
    private int currentLine;
    private int currentPosition;
    private boolean endsWithWhitespace;

    private List<UXSheetStyle> styles = new ArrayList<>();
    private List<UXSheetAttribute> variables = new ArrayList<>();
    private List<UXSheetAttribute> includes = new ArrayList<>();
    private List<ErroLog> logs = new ArrayList<>();

    public UXSheetParser(String text) {
        this.text = text;
    }

    public void reset(String text) {
        this.text = text;
        pos = 0;
        current = 0;
        next = 0;
        line = 0;
        position = 0;
        builder.setLength(0);

        currentText = null;
        currentType = -1;
        nextText = null;
        nextType = -1;
        nextLine = 0;
        nextPosition = 0;
        currentLine = 0;
        currentPosition = 0;

        if (styles != null) styles.clear();
        if (variables != null) variables.clear();
        if (logs != null) logs.clear();
    }

    public void parse() {
        read();
        while (readNext()) {
            if (currentType == VARIABLE) {
                var variable = parseAttribute();
                if (variable != null) {
                    if (variable.getValue() instanceof UXValueVariable ||
                        variable.getValue() instanceof UXValueSizeList list && list.containsVariable()) {
                        log(ErroLog.VARIABLE_CANNOT_REFERENCE_A_VARIABLE);
                    } else {
                        getVariables().add(variable);
                    }
                }

            } else if (currentType == LOCALE) {
                var value = parseImport();
                if (value == null
                        || !"@include".equals(value.getName())
                        || !(value.getValue() instanceof UXValueText)) {
                    log(ErroLog.INVALID_INCLUDE);
                } else {
                    getIncludes().add(value);
                }

            } else if (currentType == TEXT) {
                var style = parseStyle();
                getStyles().add(style);

            } else {

                log(ErroLog.UNEXPECTED_TOKEN);
            }
        }
    }

    public UXValue parseXmlAttribute() {
        if (text.length() == 0) return new UXValueText(text);

        int init = text.codePointAt(0);
        if (isCharacter(init) || init == '+' || init == '-' || init == '#' || init == '$' || init == '@') {
            read();
            if (readNext()) {
                UXValue value = parseValue();
                if (getLogs().size() > 0 || endsWithWhitespace || next != -1) {
                    getLogs().clear();
                    return new UXValueText(text);
                }
                if (value instanceof UXValueText) {
                    return new UXValueText(text);
                }
                if (value instanceof UXValueVariable || value instanceof UXValueLocale) {
                    return value;
                }
                return new UXValueXML(text, value);
            }
        }
        return new UXValueText(text);
    }

    public List<UXSheetAttribute> getVariables() {
        return variables;
    }

    public List<UXSheetStyle> getStyles() {
        return styles;
    }

    public List<ErroLog> getLogs() {
        return logs;
    }

    public List<UXSheetAttribute> getIncludes() {
        return includes;
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
                    var attr = parseAttribute();
                    if (attr != null) {
                        style.addAttribute(attr);
                    }
                }
            } else if (state == 4 && currentType == BRACE) {
                state = 5;

            } else if (state == 5 && currentType == TEXT) {
                var attr = parseAttribute();
                if (attr != null) {
                    styleState.addAttribute(attr);
                }

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
        while (nextType != CBRACE && readNext()) {
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
        }
        if (state != 3) {
            log(ErroLog.UNEXPECTED_END_OF_TOKENS);
        }
        if (state < 1) {
            return null;
        }
        return new UXSheetAttribute(name, value == null ? new UXValue() : value);
    }

    private UXSheetAttribute parseImport() {
        String name = currentText;
        UXValue value = null;
        int state = 0;
        while (nextType != CBRACE && readNext()) {
            if (state == 0 && (value = parseValue()) != null) {
                state = 1;
            } else if (state == 1 && currentType == SEMICOLON) {
                state = 2;
                break;
            } else if (currentType == INVALID) {
                log(ErroLog.UNEXPECTED_TOKEN);

            } else {
                log(ErroLog.UNEXPECTED_TOKEN);
                break;
            }
        }
        if (state != 2) {
            log(ErroLog.UNEXPECTED_END_OF_TOKENS);
        }
        if (state < 1) {
            return null;
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
            if (currentText.equalsIgnoreCase("true")) {
                return new UXValueBool(true);
            }
            if (currentText.equalsIgnoreCase("false")) {
                return new UXValueBool(false);
            }
            if (currentText.equalsIgnoreCase("MATCH_PARENT")) {
                return new UXValueSizeMp();
            }
            if (currentText.equalsIgnoreCase("WRAP_CONTENT")) {
                 return new UXValueNumber(0);
            }
            return new UXValueText(currentText);
        }

        if (currentType == TEXT) {
            String functionName = currentText;
            List<UXValue> values = new ArrayList<>();

            int state = 0;
            while (nextType != SEMICOLON && nextType != CBRACE && readNext()) {
                if (state == 0 && currentType == PARAM) {
                    state = 1;

                } else if (state == 2 && currentType == COMMA) {
                    state = 1;

                } else if (state == 2 && currentType == CPARAM) {
                    return parseFunction(functionName, values);

                } else if (state == 1 || state == 2) {
                    state = 2;
                    var val = parseValue();
                    if (val != null) values.add(val);

                }  else {
                    log(ErroLog.UNEXPECTED_TOKEN);
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
        endsWithWhitespace = true;
        while (isWhitespace(next)) {
            readNextChar();
        }
    }

    private void consumeComment() {
        endsWithWhitespace = true;
        readNextChar();
        readNextChar();
        while (current != '*' || next != '/') {
            if (!readNextChar()) break;
        }
        readNextChar();
    }

    private void consumeLineComment() {
        endsWithWhitespace = true;
        readNextChar();
        readNextChar();
        while (current != '\n') {
            if (!readNextChar()) break;
        }
    }

    private void readToken() {
        endsWithWhitespace = false;
        if (current == '\'' || current == '"') {
            nextText = readString();
            nextType = STRING;

        } else if (current == '@') {
            nextText = readName();
            nextType = LOCALE;

        } else if (current == '$') {
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
        while (isCharacter(next) || next == '.') {
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
                        readNextChar();
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
            scape = current == '\\' && !scape;
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
        getLogs().add(new ErroLog(currentLine, currentPosition, message));
    }

    private UXValue parseNumber(String source) {
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
            return new UXValueSizeIn(val / 6.0f);
        } else if (source.endsWith("mm")) {
            return new UXValueSizeIn(val / 25.4f);
        } else if (source.endsWith("cm")) {
            return new UXValueSizeIn(val / 2.54f);
        } else {
            return new UXValueNumber(val);
        }
    }

    private UXValue parseHex(String source) {
        try {
            int color;
            if (source.length() == 4) {
                int r = Integer.parseInt(source.substring(1, 2), 16);
                int g = Integer.parseInt(source.substring(2, 3), 16);
                int b = Integer.parseInt(source.substring(3, 4), 16);
                r = r | (r << 4);
                g = g | (g << 4);
                b = b | (b << 4);
                color = Color.rgbToColor(r, g, b);
            } else if (source.length() == 7) {
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

    private UXValue parseFunction(String source, List<UXValue> values) {
        if (source.equalsIgnoreCase("alpha")) {
            if (values.size() >= 2 && values.get(1) instanceof UXValueNumber v0) {
                float alpha = Math.min(1, Math.max(0, v0.asNumber(null)));
                if (values.get(0) instanceof UXValueVariable variable) {
                    return new UXValueVariable(variable.getName(), alpha);
                } else {
                    return new UXValueColor(Color.multiplyColorAlpha(values.get(0).asColor(null), alpha));
                }
            }
            if (values.size() != 2) {
                log(ErroLog.INVALID_ALPHA);
            }

        } else if (source.equalsIgnoreCase("rgb")) {
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
                int a = (int) Math.max(255, v3.asNumber(null) * 255);
                int rgba = ((r & 0xFF) << 24) | ((g & 0xFF) << 16) | ((b & 0xFF) << 8 ) | (a & 0xFF);
                return new UXValueColor(rgba);
            } else {
                log(ErroLog.INVALID_COLOR);
            }
        } else if (source.equalsIgnoreCase("font")) {
            String family = null;
            FontWeight weight = null;
            FontPosture posture = null;
            FontStyle style = null;
            for (var value : values) {
                if (value instanceof UXValueText name) {
                    var str = name.asString(null);
                    var w = FontWeight.parse(str.toUpperCase());
                    var p = FontPosture.parse(str.toUpperCase());
                    var s = FontStyle.parse(str.toUpperCase());
                    if (w != null) {
                        if (weight != null) log(ErroLog.INVALID_FONT);
                        weight = w;
                    } else if (p != null) {
                        if (posture != null) log(ErroLog.INVALID_FONT);
                        posture = p;
                    } else if (s != null) {
                        if (style != null) log(ErroLog.INVALID_FONT);
                        style = s;
                    } else if (family == null) {
                        family = str;
                    } else {
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
        } else if (lists.contains(source)) {
            return new UXValueSizeList(source, values.toArray(new UXValue[0]));

        }  else {
            log(ErroLog.INVALID_FUNCTION);
        }
        return new UXValue();
    }

    private UXValue parseString(String source) {
        return new UXValueText(Parser.string(source));
    }

    public record ErroLog(int line, int position, String message) {
        public static final String VARIABLE_CANNOT_REFERENCE_A_VARIABLE = "Variable cannot reference a variable";
        public static final String UNEXPECTED_TOKEN = "Unexpected token";
        public static final String UNEXPECTED_END_OF_TOKENS = "Unexpected end of tokens";
        public static final String INVALID_INCLUDE = "Invalid import";
        public static final String INVALID_NUMBER = "Invalid number";
        public static final String INVALID_FONT = "Invalid font";
        public static final String INVALID_COLOR = "Invalid color";
        public static final String INVALID_ALPHA = "Invalid alpha function";
        public static final String INVALID_FUNCTION = "Invalid function";
        public static final String NAME_EXPECTED = "Name expected";
        public static final String PARENT_NOT_FOUND = "Parent Not Found";
        public static final String CYCLIC_PARENT = "Cyclic Parent";
        public static final String CYCLIC_INCLUDE = "Cyclic Include";
        public static final String REPEATED_STYLE = "Repeated Style";
        public static final String REPEATED_VARIABLE = "Repeated Variable";
        public static final String INVALID_PROCESSOR = "Invalid processor";
        public static final String IMPORT_NOT_FOUND = "Import not found for ";
        public static final String INVALID_SIZE_LIST = "Invalid size list";
    }
}
