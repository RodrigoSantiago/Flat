package flat.uxml.value;

import flat.graphics.context.Font;
import flat.resources.ResourceStream;
import flat.uxml.Controller;
import flat.uxml.UXListener;
import flat.uxml.UXTheme;
import flat.uxml.UXValueListener;

public class UXValue {
    public UXValue() {
    }

    public UXValue getSource(UXTheme theme) {
        return this;
    }

    public UXValue getVariable(UXTheme theme) {
        return null;
    }

    public UXValue mix(UXValue uxValue, float t, UXTheme theme, float dpi) {
        if (uxValue == null) return this;
        if (t <= 0.001f) return this;
        if (t >= 0.999f) return uxValue;
        return internalMix(uxValue, t, theme, dpi);
    }

    UXValue internalMix(UXValue uxValue, float t, UXTheme theme, float dpi) {
        return t < 0.5 ? this : uxValue;
    }

    public Class<?> getSourceType(UXTheme theme) {
        return getClass();
    }

    public boolean isSize(UXTheme theme) {
        return false;
    }

    public boolean isColor(UXTheme theme) {
        return false;
    }

    public String asString(UXTheme theme) {
        return "";
    }

    public boolean asBool(UXTheme theme) {
        return false;
    }

    public float asNumber(UXTheme theme) {
        return 0;
    }

    public float asSize(UXTheme theme, float dpi) {
        return 0;
    }

    public float[] asSizeList(UXTheme theme, float dpi) {
        return null;
    }

    public UXValue[] getValues(UXTheme theme) {
        return null;
    }

    public float asAngle(UXTheme theme) {
        return 0;
    }

    public int asColor(UXTheme theme) {
        return 0;
    }

    public Font asFont(UXTheme theme) {
        return Font.getDefault();
    }

    public ResourceStream asResource(UXTheme theme) {
        return null;
    }

    public <T extends Enum<T>> T asConstant(UXTheme theme, Class<T> tClass) {
        return null;
    }

    public <T> UXListener<T> asListener(UXTheme theme, Class<T> argument, Controller controller) {
        return null;
    }

    public <T> UXValueListener<T> asValueListener(UXTheme theme, Class<T> argument, Controller controller) {
        return null;
    }

    @Override
    public String toString() {
        return "Empty Value";
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.getClass() == getClass();
    }
}
