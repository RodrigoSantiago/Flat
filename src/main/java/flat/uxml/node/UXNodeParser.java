package flat.uxml.node;

import flat.uxml.sheet.UXSheetParser;
import flat.uxml.value.UXValue;

import java.util.ArrayList;
import java.util.List;

public class UXNodeParser {

    private static final int INVALID    = 0;   // ?
    private static final int NAME       = 1;   // [a-zA-Z-_]
    private static final int STRING     = 2;   // "[a-zA-Z-_*]"
    private static final int TEXT       = 3;   // >[a-zA-Z-_*]<
    private static final int LESS       = 4;   // <
    private static final int GREAT      = 5;   // >
    private static final int BAR        = 6;   // /
    private static final int EQUAL      = 7;   // =
    private static final int EMPTY_TEXT = 8;   // >\s+<
    private static final int PRO_IN     = 9;   // <?
    private static final int PRO_OUT    = 10;  // ?>

    private String text;
    private int pos;
    private int current;
    private int next;
    private int line;
    private int position;

    private String currentText = null;
    private int currentType = -1;
    private String nextText = null;
    private int nextType = -1;
    private int nextLine;
    private int nextPosition;
    private int currentLine;
    private int currentPosition;

    private UXSheetParser parser;
    private UXNodeElement rootElement;
    private UXNodeElement prologElement;
    private StringBuilder builder = new StringBuilder();
    private List<ErroLog> logs;

    public UXNodeParser(String text) {
        this.text = text;
    }

    public UXNodeElement getRootElement() {
        return rootElement;
    }

    public UXNodeElement getPrologElement() {
        return prologElement;
    }

    public List<ErroLog> getLogs() {
        if (logs == null) {
            logs = new ArrayList<>();
        }
        return logs;
    }

    public void parse() {
        int state = 0;
        boolean pro = false;
        read();
        boolean closure = false;
        String currentAttr = null;
        String currentName = null;
        UXNodeElement currentElement = null;
        while (readNext()) {
            if (state == 0 && (currentType == LESS || currentType == PRO_IN)) {
                state = 1;
                pro = currentType == PRO_IN;

            } else if (state == 0 && (currentType == TEXT || currentType == EMPTY_TEXT)) {
                if (currentElement != null) {
                    if (currentElement.getContent() != null) {
                        currentElement.setContent(currentElement.getContent() + currentText);
                    } else {
                        currentElement.setContent(currentText);
                    }

                } else if (currentType == TEXT) {
                    log(ErroLog.UNEXPECTED_TEXT);
                }

            } else if (state == 1 && currentType == BAR) {
                if (closure) {
                    log(ErroLog.UNEXPECTED_TOKEN);
                }
                closure = true;

            } else if (state == 1 && currentType == NAME) {
                if (closure) {
                    currentName = currentText;
                    state = 5;
                } else {
                    var element = new UXNodeElement(currentText, currentElement);
                    if (currentElement != null) {
                        currentElement.getChildren().add(element);
                    } else {
                        if (pro) {
                            if (prologElement != null) {
                                log(ErroLog.MULTIPLE_PROLOG_ELEMENTS);
                            }
                            if (rootElement != null) {
                                log(ErroLog.PROLOG_FIRST);
                            }
                            prologElement = element;
                        } else {
                            if (rootElement != null) {
                                log(ErroLog.MULTIPLE_ROOT_ELEMENTS);
                            }
                            rootElement = element;
                        }
                    }
                    currentElement = element;
                    state = 2;
                }

            } else if (state == 2 && currentType == NAME) {
                currentAttr = currentText;
                state = 3;

            } else if (state == 3 && currentType == EQUAL) {
                state = 4;

            } else if (state == 3 && currentType == NAME) {
                currentElement.getAttributes().put(currentAttr, new UXNodeAttribute(currentAttr, null));
                currentAttr = currentText;

            } else if (state == 4 && currentType == STRING) {
                currentElement.getAttributes().put(currentAttr, new UXNodeAttribute(currentAttr, parseXmlAttribute(currentText)));
                state = 2;

            } else if ((state == 2 || state == 3 || state == 4 || state == 5)
                    && (currentType == GREAT || (currentType == BAR && nextType == GREAT) || currentType == PRO_OUT)) {
                boolean selfClosure = false;
                if (currentType == BAR) {
                    selfClosure = true;
                    readNext();
                }
                if ((pro && currentType != PRO_OUT)) {
                    log(ErroLog.UNEXPECTED_TOKEN);
                }
                if ((!pro && currentType == PRO_OUT)) {
                    log(ErroLog.UNEXPECTED_TOKEN);
                }
                if (closure && selfClosure) {
                    log(ErroLog.UNEXPECTED_TOKEN);
                }

                if (state == 3 || state == 4) {
                    currentElement.getAttributes().put(currentAttr, new UXNodeAttribute(currentAttr, null));
                }
                if (state == 4) {
                    log(ErroLog.MISSING_VALUE);
                }
                if (selfClosure || pro) {
                    if (currentElement != null) currentElement = currentElement.getParent();

                } else if (closure) {
                    if (currentElement == null || !currentElement.getName().equals(currentName)) {
                        log(ErroLog.INVALID_CLOSE_TAG);
                    } else {
                        currentElement = currentElement.getParent();
                    }
                }
                closure = false;
                currentAttr = null;
                currentName = null;
                state = 0;
                pro = false;

            } else {
                log(ErroLog.UNEXPECTED_TOKEN);
            }
        }
        if (rootElement != null && state != 0) {
            log(ErroLog.UNEXPECTED_END_OF_TOKENS);
        }
        if (currentElement != null) {
            log(ErroLog.MISSING_TAG_CLOSURE);
        }
    }

    private UXValue parseXmlAttribute(String value) {
        if (parser == null) {
            parser = new UXSheetParser(value);
        } else {
            parser.reset(value);
        }

        return parser.parseXmlAttribute();
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

    private boolean read() {
        while (readNextChar()) {
            if (currentType == GREAT && current != '<') {
                readTextToken();
                return true;
            }

            if (isWhitespace(current)) {
                consumeWhiteSpace();
                continue;
            }
            if (current == '<' && next == '!') {
                consumeComment();
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

    private void consumeWhiteSpace() {
        while (isWhitespace(next)) {
            readNextChar();
        }
    }

    private void consumeComment() {
        readNextChar();
        readNextChar();
        int before = current;
        readNextChar();
        while (before != '-' || current != '-' || next != '>') {
            if (!readNextChar()) break;
        }
        readNextChar();
    }

    private void readTextToken() {
        nextText = readText();
        nextType = nextText.isBlank() ? EMPTY_TEXT : TEXT;
    }

    private void readToken() {
        if (current == '<' && next == '?') {
            readNextChar();
            nextText = "<?";
            nextType = PRO_IN;

        } else if (current == '?' && next == '>') {
            readNextChar();
            nextText = "?>";
            nextType = PRO_OUT;

        } else if (current == '<') {
            nextText = "<";
            nextType = LESS;

        } else if (current == '>') {
            nextText = ">";
            nextType = GREAT;

        } else if (current == '\'' || current == '"') {
            nextText = readString();
            nextType = STRING;

        } else if (isCharacter(current)) {
            nextText = readName();
            nextType = NAME;

        } else if (current == '=') {
            nextText = "=";
            nextType = EQUAL;

        } else if (current == '/') {
            nextText = "/";
            nextType = BAR;

        } else {
            nextText = "";
            nextType = INVALID;

        }
    }

    private String readName() {
        // [a-zA-Z_\-]+

        builder.appendCodePoint(current);
        while (isCharacter(next) || next == ':') {
            readNextChar();
            builder.appendCodePoint(current);
        }
        String value = builder.toString();
        builder.setLength(0);
        return value;
    }

    private String readString() {
        // "XML String &#10;"

        boolean scape = false;
        int start = current;
        builder.appendCodePoint(current);
        while (next != start && readNextChar()) {
            builder.appendCodePoint(current);
        }
        if (readNextChar()) {
            builder.appendCodePoint(current);
        }

        String value = builder.toString();
        builder.setLength(0);
        return parseText(value, true);
    }

    private String readText() {
        // >Anything&#10;<

        boolean scape = false;
        builder.appendCodePoint(current);
        while (next != '<' && readNextChar()) {
            builder.appendCodePoint(current);
        }

        String value = builder.toString();
        builder.setLength(0);
        return parseText(value, false);
    }

    private void log(String message) {
        getLogs().add(new ErroLog(currentLine, currentPosition, message));
    }

    private String parseText(String content, boolean quoted) {
        int quot = 0;
        int i = 0;
        int e = content.length();
        if (quoted) {
            if (content.length() < 2 || content.charAt(0) != content.charAt(content.length() - 1)) {
                log(ErroLog.MALFORMED_STRING);
                return "";
            }
            if (content.length() == 2) {
                return "";
            }
            quot = content.codePointAt(0);
            i = 1;
            e = content.length() - 1;
        }

        while (i < e) {
            int cp = content.codePointAt(i);
            i += Character.charCount(cp);

            if (quoted && cp == quot) {
                log(ErroLog.MALFORMED_STRING);
                return "";
            }

            if (cp == '&') {
                int max = 8;
                int backup = i;
                int result = -1;
                while (i < content.length() && max-- > 0) {
                    int cp2 = content.codePointAt(i);
                    if (cp2 == ';') {
                        String subText = content.substring(backup, i);
                        if (subText.equals("lt")) result = '<';
                        else if (subText.equals("gt")) result = '>';
                        else if (subText.equals("amp")) result = '&';
                        else if (subText.equals("quot")) result = '"';
                        else if (subText.equals("apos")) result = '\'';
                        else if (subText.matches("#x[0-9A-Fa-f]+")) {
                            result = Integer.parseInt(subText.substring(2), 16);
                        } else if (subText.matches("#[0-9]+")) {
                            result = Integer.parseInt(subText.substring(1));
                        }
                        i += Character.charCount(cp2);
                        break;
                    }
                    i += Character.charCount(cp2);
                }
                if (result > -1) {
                    builder.appendCodePoint(result);
                } else {
                    log(ErroLog.MALFORMED_STRING);
                    return "";
                }
            } else {
                builder.appendCodePoint(cp);
            }
        }
        String value = builder.toString();
        builder.setLength(0);

        return value;
    }

    public record ErroLog(int line, int position, String message) {
        public static final String UNEXPECTED_TOKEN = "Unexpected token";
        public static final String UNEXPECTED_END_OF_TOKENS = "Unexpected end of tokens";
        public static final String INVALID_CLOSE_TAG = "Invalid close tag";
        public static final String MULTIPLE_ROOT_ELEMENTS = "Multiple root elements";
        public static final String MULTIPLE_PROLOG_ELEMENTS = "Multiple prolog elements";
        public static final String PROLOG_FIRST = "Prolog must be first";
        public static final String MALFORMED_STRING = "Malformed String";
        public static final String MISSING_TAG_CLOSURE = "Missing tag closure";
        public static final String UNEXPECTED_TEXT = "Unexpected text";
        public static final String MISSING_VALUE = "Missing value";
    }
}
