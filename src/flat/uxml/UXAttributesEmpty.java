package flat.uxml;

import flat.graphics.context.Font;

public class UXAttributesEmpty extends UXAttributes {

    public UXAttributesEmpty() {
        super(null);
        pack();
    }

    public <T extends Enum<T>> T asConstant(String name, Class<T> tClass, T def) {
        return def;
    }

    public String asString(String name, String def) {
        return def;
    }

    public float asSize(String name, float def) {
        return Math.round(def);
    }

    public float asNumber(String name, float def) {
        return def;
    }

    public boolean asBoolean(String name, boolean def) {
        return def;
    }

    public int asColor(String name, int def) {
        return def;
    }

    public Font asFont(String name, Font def) {
        return def;
    }

    public String asString(String name) {
        return asString(name, null);
    }

    public float asSize(String name) {
        return asSize(name, 0F);
    }

    public float asNumber(String name) {
        return asNumber(name, 0F);
    }

    public boolean asBoolean(String name) {
        return asBoolean(name, false);
    }

    public int asColor(String name) {
        return asColor(name, 0);
    }

    public Font asFont(String name) {
        return asFont(name, Font.DEFAULT);
    }

    public void logUnusedAttributes() {
    }
}
