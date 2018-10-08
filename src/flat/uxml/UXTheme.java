package flat.uxml;

import flat.graphics.image.Drawable;
import flat.resources.Dimension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
        if (style == null) {
            return parent.getStyle(name);
        } else {
            return style;
        }
    }

    private void load(InputStream stream) {
        BufferedReader is = new BufferedReader(new InputStreamReader(stream));

        try {
            String line;
            UXStyle style = null;
            int state = -1;
            while ((line = is.readLine()) != null) {
                line = line.trim();
                // TODO - BLOQUEAR ATRIBUTO CHAMADO 'ID', 'NEXTID', 'PREVIOUSID'
                // TODO - IMPLEMENTAR LEITURA REAL { TER ESPAÇOS INDEFINIDOS E SEM PRECISAR DE NEWLINE e \\\" NO STRING}
                if (!line.isEmpty() && !line.startsWith("//")) {
                    if (line.matches("\\s*\\w+\\s*\\{")) {
                        if (style == null) {
                            style = new UXStyle(line.substring(0, line.lastIndexOf("{")).trim(), this);
                            state = UXStyle.ENABLED;
                            styles.put(style.name, style);
                        } else {
                            String stateName = line.substring(0, line.lastIndexOf("{")).trim();

                            if (stateName.equals("focused")) {
                                state = UXStyle.FOCUSED;
                            } else if (stateName.equals("activated")) {
                                state = UXStyle.ACTIVATED;
                            } else if (stateName.equals("hovered")) {
                                state = UXStyle.HOVERED;
                            } else if (stateName.equals("pressed")) {
                                state = UXStyle.PRESSED;
                            } else if (stateName.equals("dragged")) {
                                state = UXStyle.DRAGGED;
                            } else if (stateName.equals("error")) {
                                state = UXStyle.ERROR;
                            } else if (stateName.equals("disabled")) {
                                state = UXStyle.DISABLED;
                            }
                        }
                    } else if (line.matches("\\s*\\w+\\s*:\\s*\\w+\\s*\\{")) {
                        int div = line.indexOf(":");
                        style = new UXStyle(line.substring(0, div).trim(),
                                styles.get(line.substring(div + 1, line.lastIndexOf("{")).trim()));
                        styles.put(style.name, style);
                    } else if (line.matches("\\s*}\\s*")) {
                        if (state != UXStyle.ENABLED) {
                            state = UXStyle.ENABLED;
                        } else {
                            style = null;
                        }
                    } else if (line.matches("\\s*[\\w\\-]+\\s*:.*")) {
                        int div = line.indexOf(":");
                        String key = line.substring(0, div).trim();
                        String value = line.substring(div + 1).trim();
                        style.add(key, new UXValue(value), state);
                    }
                }
            }
        } catch (Exception e) {
            try {
                is.close();
            } catch (IOException ignored) {
            }
        }
    }
}