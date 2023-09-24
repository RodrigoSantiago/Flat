package flat.uxml;

import flat.animations.StateInfo;
import flat.resources.Dimension;

import java.io.*;
import java.util.HashMap;

public class UXTheme {
    public final UXTheme parent;

    private Dimension dimension;
    private float fontScale = 1;
    private final HashMap<String, UXStyle> styles = new HashMap<>();

    public UXTheme(InputStream stream) {
        this.parent = null;
        load(stream);
    }

    public UXTheme(InputStream stream, UXTheme parent) {
        this.parent = parent;
        load(stream);
    }

    public Dimension getDimension() {
        return dimension;
    }

    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    public float getFontScale() {
        return this.fontScale;
    }

    public void setFontScale(float fontScale) {
        this.fontScale = fontScale;
    }

    public UXStyle getStyle(String name) {
        UXStyle style = styles.get(name);
        if (style == null && parent != null) {
            return parent.getStyle(name);
        } else {
            return style;
        }
    }

    private void load(InputStream stream) {
        try {
            int i;

            String styleName = null;
            String attrName = null;
            UXStyle style = null;

            boolean scaped = false, inverseBar = false;
            StringBuilder string = new StringBuilder();
            int state = 0, styleState = 0;

            while ((i = stream.read()) != -1) {
                char c = (char)i;
                boolean letter = c == '-' || Character.isLetterOrDigit(c);

                if (state == 0 && letter) {
                    string.append(c);
                    state = 1;
                } else if (state == 0 && c == '/') {
                    state = -1;
                } else if (state == -1 && c =='*') {
                    state = -2;
                } else if (state == -1) {
                    state = 0;
                } else if (state == -2 && c =='*') {
                    state = -3;
                } else if (state == -3 && c =='/') {
                    state = 0;
                } else if (state == -3) {
                    state = -2;
                }
                // [nome]
                else if (state == 1 && letter) {
                    string.append(c);
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
                }

                else if (state == 2 && c == ':') {
                    state = 3;  // wait PARENT START NAME
                } else if (state == 2 && c == '{') {
                    state = 5;  /// wait ATTRIBUTE START

                    style = new UXStyle(styleName, this);
                    styles.put(style.name, style);
                    styleState = 0;
                }

                else if (state == 3 && letter) {
                    string.append(c);
                    state = 4;  // wait PARENT END NAME
                }

                else if (state == 4 && letter) {
                    string.append(c);
                } else if (state == 4 && c == '{') {
                    String parentName = string.toString();
                    string.setLength(0);
                    state = 5;  // wait ATTRIBUTE START

                    style = new UXStyle(styleName, styles.get(parentName));
                    styles.put(style.name, style);
                    styleState = 0;
                }

                else if ((state == 5 || state == 8) && letter) {
                    string.append(c);
                    state = state + 1; // wait ATTRIBUTE END[6, 9]
                }

                else if ((state == 6 || state == 9) && letter) {
                    string.append(c);
                } else if ((state == 6 || state == 9) && c == ':') {
                    attrName = string.toString();
                    string.setLength(0);
                    state = state + 1; // wait VALUE START[7, 10]
                } else if (state == 6 && c == '{') {
                    attrName = string.toString();
                    string.setLength(0);
                    state = 8; // wait INNTER ATTRIBUTE START

                    if (attrName.equals("default") || attrName.equals("enabled")) {
                        styleState = StateInfo.ENABLED;
                    } else if (attrName.equals("focused")) {
                        styleState = StateInfo.FOCUSED;
                    } else if (attrName.equals("activated")) {
                        styleState = StateInfo.ACTIVATED;
                    } else if (attrName.equals("hovered")) {
                        styleState = StateInfo.HOVERED;
                    } else if (attrName.equals("pressed")) {
                        styleState = StateInfo.PRESSED;
                    } else if (attrName.equals("dragged")) {
                        styleState = StateInfo.DRAGGED;
                    } else if (attrName.equals("error")) {
                        styleState = StateInfo.ERROR;
                    } else if (attrName.equals("disabled")) {
                        styleState = StateInfo.DISABLED;
                    } else {
                        styleState = 0;
                    }
                }

                else if ((state == 7 || state == 10) && c == ';' && !scaped) {
                    String attrValue = string.toString();
                    string.setLength(0);
                    state = state - 2;  // wait ATTRIBUTE [INNER] START[5, 8]

                    style.add(attrName, new UXValue(attrValue.trim(), true), styleState);
                }
                else if ((state == 7 || state == 10) && c == '\\') {
                    string.append(c);
                    if (scaped) {
                        inverseBar = !inverseBar;
                    }
                }
                else if ((state == 7 || state == 10) && c == '"') {
                    if (!scaped) {
                        scaped = true;
                    } else if (!inverseBar) {
                        scaped = false;
                    }
                    string.append(c);
                    inverseBar = false;
                }
                else if (state == 7 || state == 10) {
                    string.append(c);
                    inverseBar = false;
                }

                else if (state == 5 && c == '}') {
                    state = 0;  // WAIT STYLE
                }
                else if (state == 8 && c == '}') {
                    state = 5;  // wait ATTRIBUTE START
                }
            }
        } catch (Exception e) {
            try {
                stream.close();
            } catch (IOException ignored) {
            }
        }
    }
}