package flat.uxml.value;

import flat.Flat;
import flat.resources.Parser;
import flat.uxml.Controller;
import flat.uxml.UXTheme;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
    public <T extends Enum> T asConstant(UXTheme theme, Class<T> tClass) {
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
    public Method asListener(UXTheme theme, Class<?> argument, Controller controller) {
        Method result = null;

        if (controller != null) {
            try {
                Method method = controller.getClass().getMethod(text, argument);
                method.setAccessible(true);
                if (method.isAnnotationPresent(Flat.class)
                        && Modifier.isPublic(method.getModifiers())
                        && !Modifier.isStatic(method.getModifiers())) {
                    result = method;
                }
            } catch (Exception ignored) {
            }
        }
        return result;
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
