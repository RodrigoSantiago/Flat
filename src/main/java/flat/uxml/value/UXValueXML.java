package flat.uxml.value;

import flat.graphics.context.Font;
import flat.resources.ResourceStream;
import flat.uxml.Controller;
import flat.uxml.UXListener;
import flat.uxml.UXTheme;
import flat.uxml.UXValueListener;

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
    public UXValue mix(UXValue uxValue, float t, UXTheme theme, float dpi) {
        return value.mix(uxValue, t, theme, dpi);
    }

    @Override
    UXValue internalMix(UXValue uxValue, float t, UXTheme theme, float dpi) {
        return value.internalMix(uxValue, t, theme, dpi);
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
    public float asSize(UXTheme theme, float dpi) {
        return value.asSize(theme, dpi);
    }

    @Override
    public float[] asSizeList(UXTheme theme, float dpi) {
        return value.asSizeList(theme, dpi);
    }

    @Override
    public UXValue[] getValues(UXTheme theme) {
        return value.getValues(theme);
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
    public <T extends Enum<T>> T asConstant(UXTheme theme, Class<T> tClass) {
        return value.asConstant(theme, tClass);
    }

    @Override
    public <T> UXListener<T> asListener(UXTheme theme, Class<T> argument, Controller controller) {
        return value.asListener(theme, argument, controller);
    }

    @Override
    public <T> UXValueListener<T> asValueListener(UXTheme theme, Class<T> argument, Controller controller) {
        return value.asValueListener(theme, argument, controller);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UXValueXML that = (UXValueXML) o;
        return Objects.equals(value, that.value) && Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, text);
    }

    @Override
    public String toString() {
        return "XML : '" + text + "' >> " + value;
    }
}
