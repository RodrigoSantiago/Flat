package flat.uxml;

import flat.resources.StringBundle;

public class UXTheme {
    private UXSheet sheet;
    private StringBundle stringBundle;
    private float density;
    private float fontScale;

    public UXTheme(UXSheet sheet) {
        this(sheet, 160f, 1f, null);
    }

    public UXTheme(UXSheet sheet, float density, float fontScale, StringBundle stringBundle) {
        this.sheet = sheet;
        this.density = density;
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

    public float getDensity() {
        return density;
    }

    public void setDensity(float density) {
        this.density = density;
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
        return null;
    }

    public void setVariable(String name, UXValue value) {

    }
}
