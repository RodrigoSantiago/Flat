package flat.uxml;

import flat.graphics.context.Font;
import flat.resources.ResourceStream;

import java.lang.reflect.Method;

public class UXValue {
    public UXValue() {
    }

    public UXValue getVariable(UXTheme theme) {
        return null;
    }

    public UXValue mix(UXValue uxValue, float t, UXTheme theme) {
        if (uxValue == null) return this;
        if (t <= 0.001f) return this;
        if (t >= 0.999f) return uxValue;
        return internalMix(uxValue, t, theme);
    }

    protected UXValue internalMix(UXValue uxValue, float t, UXTheme theme) {
        return t < 0.5 ? this : uxValue;
    }

    public Class<?> getSourceType(UXTheme theme) {
        return getClass();
    }

    public boolean isSize() {
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

    public float asSize(UXTheme theme) {
        return 0;
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

    public <T extends Enum> T asConstant(UXTheme theme, Class<T> tClass) {
        return null;
    }

    public Method asListener(UXTheme theme, Class<?> argument, Controller controller) {
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
