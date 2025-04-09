package flat.uxml;

import flat.uxml.value.UXValue;

import java.util.HashMap;

public class UXTheme {
    private final UXSheet sheet;
    private final UXStringBundle stringBundle;
    private final float fontScale;
    private final float dpi;
    private final HashMap<String, UXValue> variables;

    public UXTheme(UXSheet sheet) {
        this(sheet, 1f, 160f, null, null);
    }

    public UXTheme(UXSheet sheet, float fontScale, float dpi, UXStringBundle stringBundle, HashMap<String, UXValue> variables) {
        this.sheet = sheet;
        this.fontScale = fontScale;
        this.dpi = dpi;
        this.stringBundle = stringBundle;
        if (variables != null) {
            this.variables = new HashMap<>(variables);
        } else {
            this.variables = null;
        }
    }

    public UXTheme createInstance(float fontScale, float dpi, UXStringBundle stringBundle, HashMap<String, UXValue> variables) {
        return new UXTheme(sheet, fontScale, dpi, stringBundle, variables);
    }

    public UXStyle getStyle(String name) {
        return sheet == null ? null : sheet.getStyle(name);
    }

    public String getText(String name) {
        return stringBundle == null ? name : stringBundle.get(name, name);
    }

    public UXSheet getSheet() {
        return sheet;
    }

    public float getFontScale() {
        return fontScale;
    }

    public float getDpi() {
        return dpi;
    }

    public UXStringBundle getStringBundle() {
        return stringBundle;
    }

    public UXValue getVariable(String name) {
        UXValue value = variables == null ? null : variables.get(name);
        if (value == null) {
            value = sheet.getVariableInitialValue(name);
            if (value == null) {
                value = new UXValue();
            }
        }

        return value;
    }
}
