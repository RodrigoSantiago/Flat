package flat.uxml.value;

import flat.graphics.Color;
import flat.resources.Parser;
import flat.resources.ResourceStream;
import flat.uxml.Controller;
import flat.uxml.UXListener;
import flat.uxml.UXTheme;
import flat.uxml.UXValueListener;
import flat.window.Application;

import java.util.Objects;

public class UXValueText extends UXValue {
    private final String text;
    private final int color;
    private final boolean isColor;

    public UXValueText(String text) {
        this.text = text;
        this.color = Parser.colorByName(text);
        this.isColor = this.color != 0x0 || text.equalsIgnoreCase("transparent");
    }

    UXValueText(String text, int color) {
        this.text = text;
        this.color = color;
        this.isColor = true;
    }

    @Override
    UXValue internalMix(UXValue uxValue, float t, UXTheme theme, float dpi) {
        if (isColor(theme) && uxValue.isColor(theme)) {
            int v1 = asColor(theme);
            int v2 = uxValue.asColor(theme);
            return new UXValueColor(Color.mixHSV(v1, v2, t));
        } else {
            return super.internalMix(uxValue, t, theme, dpi);
        }
    }

    @Override
    public String asString(UXTheme theme) {
        return text;
    }

    @Override
    public boolean isColor(UXTheme theme) {
        return isColor;
    }

    @Override
    public int asColor(UXTheme theme) {
        return color;
    }

    @Override
    public ResourceStream asResource(UXTheme theme) {
        return text.isEmpty() ? null : Application.getResourcesManager().getResource(text);
    }

    @Override
    public <T extends Enum<T>> T asConstant(UXTheme theme, Class<T> tClass) {
        T result = null;
        try {
            for (T each : tClass.getEnumConstants()) {
                if (each.name().compareToIgnoreCase(text) == 0) {
                    result = each;
                    break;
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    @Override
    public <T> UXListener<T> asListener(UXTheme theme, Class<T> argument, Controller controller) {
        return controller != null ? controller.getListenerMethod(text, argument) : null;
    }

    @Override
    public <T> UXValueListener<T> asValueListener(UXTheme theme, Class<T> argument, Controller controller) {
        return controller != null ? controller.getValueListenerMethod(text, argument) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UXValueText that = (UXValueText) o;
        return Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }

    @Override
    public String toString() {
        return "Text : " + text;
    }
}
