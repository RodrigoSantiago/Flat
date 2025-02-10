package flat.uxml.value;

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

    public UXValueText(String text) {
        this.text = text;
    }

    @Override
    public String asString(UXTheme theme) {
        return text;
    }

    @Override
    public int asColor(UXTheme theme) {
        return Parser.colorByName(text);
    }

    @Override
    public ResourceStream asResource(UXTheme theme) {
        return Application.getResourcesManager().getResource(text);
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
