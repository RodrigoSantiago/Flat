package flat.uxml.value;

import flat.graphics.context.Font;
import flat.resources.ResourceStream;
import flat.uxml.Controller;
import flat.uxml.UXTheme;

import java.lang.reflect.Method;
import java.util.Objects;

public class UXValueXML extends  UXValue {
    private final UXValue value;
    private final String text;

    public UXValueXML(String text, UXValue value) {
        this.text = text;
        this.value = value;
    }

    @Override
    public UXValue getVariable(UXTheme theme) {
        return value.getVariable(theme);
    }

    @Override
    public UXValue mix(UXValue uxValue, float t, UXTheme theme) {
        return value.mix(uxValue, t, theme);
    }

    @Override
    UXValue internalMix(UXValue uxValue, float t, UXTheme theme) {
        return value.internalMix(uxValue, t, theme);
    }

    @Override
    public Class<?> getSourceType(UXTheme theme) {
        return value.getSourceType(theme);
    }

    @Override
    public boolean isSize() {
        return value.isSize();
    }

    @Override
    public String asString(UXTheme theme) {
        return text;
    }

    @Override
    public boolean asBool(UXTheme theme) {
        return value.asBool(theme);
    }

    @Override
    public float asNumber(UXTheme theme) {
        return value.asNumber(theme);
    }

    @Override
    public float asSize(UXTheme theme) {
        return value.asSize(theme);
    }

    @Override
    public float asAngle(UXTheme theme) {
        return value.asAngle(theme);
    }

    @Override
    public int asColor(UXTheme theme) {
        return value.asColor(theme);
    }

    @Override
    public Font asFont(UXTheme theme) {
        return value.asFont(theme);
    }

    @Override
    public ResourceStream asResource(UXTheme theme) {
        return value.asResource(theme);
    }

    @Override
    public <T extends Enum> T asConstant(UXTheme theme, Class<T> tClass) {
        return value.asConstant(theme, tClass);
    }

    @Override
    public Method asListener(UXTheme theme, Class<?> argument, Controller controller) {
        return value.asListener(theme, argument, controller);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UXValueXML that = (UXValueXML) o;
        return value.equals(that.value) && text.equals(that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, text);
    }
}
