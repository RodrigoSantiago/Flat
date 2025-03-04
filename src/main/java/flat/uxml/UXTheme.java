package flat.uxml;

import flat.resources.StringBundle;
import flat.uxml.value.UXValue;

import java.util.HashMap;

public class UXTheme {
    private UXSheet sheet;
    private StringBundle stringBundle;
    private float fontScale;
    private HashMap<String, UXValue> variables = new HashMap<>();

    public UXTheme(UXSheet sheet) {
        this(sheet, 1f, null);
    }

    public UXTheme(UXSheet sheet, float fontScale, StringBundle stringBundle) {
        this.sheet = sheet;
        this.fontScale = fontScale;
        this.stringBundle = stringBundle;
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

    public void setSheet(UXSheet sheet) {
        this.sheet = sheet;
    }

    public float getFontScale() {
        return fontScale;
    }

    public void setFontScale(float fontScale) {
        this.fontScale = fontScale;
    }

    public StringBundle getStringBundle() {
        return stringBundle;
    }

    public void setStringBundle(StringBundle stringBundle) {
        this.stringBundle = stringBundle;
    }

    public UXValue getVariable(String name) {
        UXValue value = variables.get(name);
        if (value == null) {
            value = sheet.getVariableInitialValue(name);
            if (value == null) {
                value = new UXValue();
            }
        }

        return value;
    }

    public void setVariable(String name, UXValue value) {
        variables.put(name, value);
    }
}
