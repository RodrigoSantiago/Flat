package flat.resources;

import flat.math.shapes.Path;

public final class Parser {

    private static final double[] pow10 = new double[128];
    private static final ColorPair[] colors = {
        new ColorPair("black", 0x000000FF),
        new ColorPair("silver", 0xC0C0C0FF),
        new ColorPair("gray", 0x808080FF),
        new ColorPair("white", 0xFFFFFFFF),
        new ColorPair("maroon", 0x800000FF),
        new ColorPair("red", 0xFF0000FF),
        new ColorPair("purple", 0x800080FF),
        new ColorPair("fuchsia", 0xFF00FFFF),
        new ColorPair("green", 0x008000FF),
        new ColorPair("lime", 0x00FF00FF),
        new ColorPair("olive", 0x808000FF),
        new ColorPair("yellow", 0xFFFF00FF),
        new ColorPair("navy", 0x000080FF),
        new ColorPair("blue", 0x0000FFFF),
        new ColorPair("teal", 0x008080FF),
        new ColorPair("aqua", 0x00FFFFFF)
    };

    static {
        for (int i = 0; i < pow10.length; i++) {
            pow10[i] = Math.pow(10, i);
        }
    }

    public static String string(String st) {
        StringBuilder sb = new StringBuilder(st.length());

        for (int i = 0; i < st.length(); i++) {
            char ch = st.charAt(i);
            if (ch == '\\') {
                char nextChar = (i == st.length() - 1) ? '\\' : st.charAt(i + 1);
                if (nextChar >= '0' && nextChar <= '7') {
                    String code = "" + nextChar;
                    i++;
                    if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                            && st.charAt(i + 1) <= '7') {
                        code += st.charAt(i + 1);
                        i++;
                        if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                                && st.charAt(i + 1) <= '7') {
                            code += st.charAt(i + 1);
                            i++;
                        }
                    }
                    sb.append((char) Integer.parseInt(code, 8));
                    continue;
                }
                switch (nextChar) {
                    case '\\' -> ch = '\\';
                    case 'b' -> ch = '\b';
                    case 'f' -> ch = '\f';
                    case 'n' -> ch = '\n';
                    case 'r' -> ch = '\r';
                    case 't' -> ch = '\t';
                    case '\"' -> ch = '\"';
                    case '\'' -> ch = '\'';
                    case 'u' -> {
                        if (i >= st.length() - 5) {
                            ch = 'u';
                            break;
                        }
                        int code = Integer.parseInt(st.substring(i + 2, i + 6), 16);
                        sb.append(Character.toChars(code));
                        i += 5;
                        continue;
                    }
                    default -> ch = nextChar;
                }
                i++;
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    public static Path svg(String str, int offset) {
        try {
            return new SVGParser().svg(str, offset);
        } catch (Exception e) {
            return null;
        }
    }

    public static int color(String color) {
        if ("transparent".equals(color)) {
            return 0;
        } else if (color.length() == 7) {
            return ((int) Long.parseLong(color.substring(1), 16) << 8) | 0x000000FF;
        } else {
            return (int) Long.parseLong(color.substring(1), 16);
        }
    }

    public static int colorByName(String color) {
        for (int i = 0; i < colors.length; i++) {
            if (color.equalsIgnoreCase(colors[i].name)) return colors[i].color;
        }
        return 0;
    }

    private static class SVGParser {
        private String s;
        private int n;
        private char current;
        private int pos;

        public Path svg(String str, int offset) {
            this.s = str;
            this.pos = offset;
            this.n = s.length();
            this.current = s.charAt(pos);

            skipWhitespace();

            Path p = new Path();
            float lastX = 0, lastY = 0;
            float lastX1 = 0, lastY1 = 0;
            float subPathStartX = 0, subPathStartY = 0;

            char prevCmd = 0;
            char prevCmd2 = 0;

            while (pos < n) {
                char cmd = s.charAt(pos);
                switch (cmd) {
                    case '-':
                    case '+':
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        if (prevCmd == 'm' || prevCmd == 'M') {
                            cmd = (char) ((prevCmd) - 1);
                            break;
                        } else if (("lhvcsqta").indexOf(Character.toLowerCase(prevCmd)) >= 0) {
                            cmd = prevCmd;
                            break;
                        }
                    default: {
                        advance();
                        prevCmd = cmd;
                    }
                }

                switch (cmd) {
                    case 'M':
                    case 'm': {
                        float x = nextFloat();
                        float y = nextFloat();
                        if (cmd == 'm') {
                            subPathStartX += x;
                            subPathStartY += y;
                            p.moveTo(lastX + x, lastY + y);
                            lastX += x;
                            lastY += y;
                        } else {
                            subPathStartX = x;
                            subPathStartY = y;
                            p.moveTo(x, y);
                            lastX = x;
                            lastY = y;
                        }
                        break;
                    }
                    case 'Z':
                    case 'z': {
                        p.closePath();
                        p.moveTo(subPathStartX, subPathStartY);
                        lastX1 = lastX = subPathStartX;
                        lastY1 = lastY = subPathStartY;
                        break;
                    }
                    case 'L':
                    case 'l': {
                        float x = nextFloat();
                        float y = nextFloat();
                        if (cmd == 'l') {
                            x += lastX;
                            y += lastY;
                        }
                        p.lineTo(x, y);
                        lastX1 = lastX = x;
                        lastY1 = lastY = y;
                        break;
                    }
                    case 'H':
                    case 'h': {
                        float x = nextFloat();
                        if (cmd == 'h') {
                            x += lastX;
                        }
                        p.lineTo(x, lastY);
                        lastX1 = lastX = x;
                        break;
                    }
                    case 'V':
                    case 'v': {
                        float y = nextFloat();
                        if (cmd == 'v') {
                            y += lastY;
                        }
                        p.lineTo(lastX, y);
                        lastY1 = lastY = y;
                        break;
                    }
                    case 'Q':
                    case 'q': {
                        float x1 = nextFloat();
                        float y1 = nextFloat();
                        float x = nextFloat();
                        float y = nextFloat();
                        if (cmd == 'q') {
                            x1 += lastX;
                            x += lastX;
                            y1 += lastY;
                            y += lastY;
                        }
                        p.quadTo(x1, y1, x, y);
                        lastX1 = x1;
                        lastY1 = y1;
                        lastX = x;
                        lastY = y;
                        break;
                    }
                    case 'T':
                    case 't': {
                        float x = nextFloat();
                        float y = nextFloat();
                        if (cmd == 't') {
                            x += lastX;
                            y += lastY;
                        }
                        float x1 = 2 * lastX - lastX1;
                        float y1 = 2 * lastY - lastY1;
                        p.quadTo(x1, y1, x, y);
                        lastX1 = x1;
                        lastY1 = y1;
                        lastX = x;
                        lastY = y;
                        break;
                    }
                    case 'C':
                    case 'c': {
                        float x1 = nextFloat();
                        float y1 = nextFloat();
                        float x2 = nextFloat();
                        float y2 = nextFloat();
                        float x = nextFloat();
                        float y = nextFloat();
                        if (cmd == 'c') {
                            x1 += lastX;
                            x2 += lastX;
                            x += lastX;
                            y1 += lastY;
                            y2 += lastY;
                            y += lastY;
                        }
                        p.curveTo(x1, y1, x2, y2, x, y);
                        lastX1 = x2;
                        lastY1 = y2;
                        lastX = x;
                        lastY = y;
                        break;
                    }
                    case 'S':
                    case 's': {
                        float x2 = nextFloat();
                        float y2 = nextFloat();
                        float x = nextFloat();
                        float y = nextFloat();
                        if (cmd == 's') {
                            x2 += lastX;
                            x += lastX;
                            y2 += lastY;
                            y += lastY;
                        }
                        float x1, y1;
                        if (prevCmd2 == 'C' || prevCmd2 == 'c' || prevCmd2 == 'S' || prevCmd2 == 's') {
                            x1 = 2 * lastX - lastX1;
                            y1 = 2 * lastY - lastY1;
                        } else {
                            x1 = lastX;
                            y1 = lastY;
                        }
                        p.curveTo(x1, y1, x2, y2, x, y);
                        lastX1 = x2;
                        lastY1 = y2;
                        lastX = x;
                        lastY = y;
                        break;
                    }
                    case 'A':
                    case 'a': {
                        float rx = nextFloat();
                        float ry = nextFloat();
                        float theta = nextFloat();
                        int largeArc = nextFlag();
                        int sweepArc = nextFlag();
                        float x = nextFloat();
                        float y = nextFloat();
                        if (cmd == 'a') {
                            x += lastX;
                            y += lastY;
                        }
                        p.arcTo(rx, ry, theta, largeArc, sweepArc, x, y);
                        lastX1 = lastX = x;
                        lastY1 = lastY = y;
                        break;
                    }
                    default:
                        advance();
                }

                prevCmd2 = cmd;
                skipWhitespace();
            }
            s = null;
            return p;
        }

        private char read() {
            if (pos < n) {
                pos++;
            }
            return pos == n ? '\0' : s.charAt(pos);
        }

        private void skipWhitespace() {
            while (pos < n) {
                if (Character.isWhitespace(s.charAt(pos))) {
                    advance();
                } else {
                    break;
                }
            }
        }

        private void skipNumberSeparator() {
            while (pos < n) {
                char c = s.charAt(pos);
                switch (c) {
                    case ' ':
                    case ',':
                    case '\n':
                    case '\t':
                        advance();
                        break;
                    default:
                        return;
                }
            }
        }

        private void advance() {
            current = read();
        }

        private float parseFloat() {
            int mant = 0;
            int mantDig = 0;
            boolean mantPos = true;
            boolean mantRead = false;

            int exp = 0;
            int expDig = 0;
            int expAdj = 0;
            boolean expPos = true;

            switch (current) {
                case '-':
                    mantPos = false;
                case '+':
                    current = read();
            }

            m1:
            switch (current) {
                default:
                    return Float.NaN;

                case '.':
                    break;

                case '0':
                    mantRead = true;
                    l:
                    for (; ; ) {
                        current = read();
                        switch (current) {
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                break l;
                            case '.':
                            case 'e':
                            case 'E':
                                break m1;
                            default:
                                return 0.0f;
                            case '0':
                        }
                    }

                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    mantRead = true;
                    l:
                    for (; ; ) {
                        if (mantDig < 9) {
                            mantDig++;
                            mant = mant * 10 + (current - '0');
                        } else {
                            expAdj++;
                        }
                        current = read();
                        switch (current) {
                            default:
                                break l;
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                        }
                    }
            }

            if (current == '.') {
                current = read();
                m2:
                switch (current) {
                    default:
                    case 'e':
                    case 'E':
                        if (!mantRead) {
                            reportUnexpectedCharacterError(current);
                            return 0.0f;
                        }
                        break;

                    case '0':
                        if (mantDig == 0) {
                            l:
                            for (; ; ) {
                                current = read();
                                expAdj--;
                                switch (current) {
                                    case '1':
                                    case '2':
                                    case '3':
                                    case '4':
                                    case '5':
                                    case '6':
                                    case '7':
                                    case '8':
                                    case '9':
                                        break l;
                                    default:
                                        if (!mantRead) {
                                            return 0.0f;
                                        }
                                        break m2;
                                    case '0':
                                }
                            }
                        }
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        l:
                        for (; ; ) {
                            if (mantDig < 9) {
                                mantDig++;
                                mant = mant * 10 + (current - '0');
                                expAdj--;
                            }
                            current = read();
                            switch (current) {
                                default:
                                    break l;
                                case '0':
                                case '1':
                                case '2':
                                case '3':
                                case '4':
                                case '5':
                                case '6':
                                case '7':
                                case '8':
                                case '9':
                            }
                        }
                }
            }

            switch (current) {
                case 'e':
                case 'E':
                    current = read();
                    switch (current) {
                        default:
                            reportUnexpectedCharacterError(current);
                            return 0f;
                        case '-':
                            expPos = false;
                        case '+':
                            current = read();
                            switch (current) {
                                default:
                                    reportUnexpectedCharacterError(current);
                                    return 0f;
                                case '0':
                                case '1':
                                case '2':
                                case '3':
                                case '4':
                                case '5':
                                case '6':
                                case '7':
                                case '8':
                                case '9':
                            }
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                    }

                    en:
                    switch (current) {
                        case '0':
                            l:
                            for (; ; ) {
                                current = read();
                                switch (current) {
                                    case '1':
                                    case '2':
                                    case '3':
                                    case '4':
                                    case '5':
                                    case '6':
                                    case '7':
                                    case '8':
                                    case '9':
                                        break l;
                                    default:
                                        break en;
                                    case '0':
                                }
                            }

                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                            l:
                            for (; ; ) {
                                if (expDig < 3) {
                                    expDig++;
                                    exp = exp * 10 + (current - '0');
                                }
                                current = read();
                                switch (current) {
                                    default:
                                        break l;
                                    case '0':
                                    case '1':
                                    case '2':
                                    case '3':
                                    case '4':
                                    case '5':
                                    case '6':
                                    case '7':
                                    case '8':
                                    case '9':
                                }
                            }
                    }
                default:
            }

            if (!expPos) {
                exp = -exp;
            }
            exp += expAdj;
            if (!mantPos) {
                mant = -mant;
            }

            return buildFloat(mant, exp);
        }

        private void reportUnexpectedCharacterError(char c) {
            throw new RuntimeException("Unexpected char '" + c + "'.");
        }

        private float buildFloat(int mant, int exp) {
            if (exp < -125 || mant == 0) {
                return 0.0f;
            }

            if (exp >= 128) {
                return (mant > 0) ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
            }

            if (exp == 0) {
                return mant;
            }

            if (mant >= (1 << 26)) {
                mant++;
            }

            return (float) ((exp > 0) ? mant * pow10[exp] : mant / pow10[-exp]);
        }

        private float nextFloat() {
            skipWhitespace();
            float f = parseFloat();
            skipNumberSeparator();
            return f;
        }

        private int nextFlag() {
            skipWhitespace();
            int flag = current - '0';
            current = read();
            skipNumberSeparator();
            return flag;
        }
    }

    private static class ColorPair {
        public final String name;
        public final int color;

        public ColorPair(String name, int color) {
            this.name = name;
            this.color = color;
        }
    }
}