package flat.uxml.value;

import flat.graphics.Color;
import flat.graphics.context.Font;
import flat.resources.ResourceStream;
import flat.uxml.Controller;
import flat.uxml.UXListener;
import flat.uxml.UXTheme;
import flat.uxml.UXValueListener;

import java.util.Objects;

public class UXValueVariable extends UXValue {
    private final String name;
    private final float alpha;

    public UXValueVariable(String name) {
        this.name = name;
        this.alpha = 1;
    }

    public UXValueVariable(String name, float alphaFunction) {
        this.name = name;
        this.alpha = Math.min(1, Math.max(0, alphaFunction));
    }

    public String getName() {
        return name;
    }

    @Override
    public UXValue getSource(UXTheme theme) {
        return getVariable(theme);
    }

    @Override
    public UXValue getVariable(UXTheme theme) {
        return theme == null ? null : theme.getVariable(name);
    }

    @Override
    UXValue internalMix(UXValue uxValue, float t, UXTheme theme, float dpi) {
        UXValue variable = getVariable(theme);
        if (variable != null) {
            if (alpha < 1 && variable.isColor(theme)) {
                return new UXValueColor(Color.multiplyColorAlpha(variable.asColor(theme), alpha))
                        .mix(uxValue, t, theme, dpi);
            } else {
                return variable.mix(uxValue, t, theme, dpi);
            }
        } else {
            return super.internalMix(uxValue, t, theme, dpi);
        }
    }

    @Override
    public boolean isSize(UXTheme theme) {
        UXValue variable = getVariable(theme);
        if (variable != null) {
            return variable.isSize(theme);
        } else {
            return super.isSize(theme);
        }
    }

    @Override
    public boolean isColor(UXTheme theme) {
        UXValue variable = getVariable(theme);
        if (variable != null) {
            return variable.isColor(theme);
        } else {
            return super.isColor(theme);
        }
    }

    @Override
    public Class<?> getSourceType(UXTheme theme) {
        UXValue variable = getVariable(theme);
        if (variable != null) {
            return variable.getSourceType(theme);
        } else {
            return super.getSourceType(theme);
        }
    }

    @Override
    public String asString(UXTheme theme) {
        UXValue variable = getVariable(theme);
        if (variable != null) {
            return variable.asString(theme);
        } else {
            return super.asString(theme);
        }
    }

    @Override
    public boolean asBool(UXTheme theme) {
        UXValue variable = getVariable(theme);
        if (variable != null) {
            return variable.asBool(theme);
        } else {
            return super.asBool(theme);
        }
    }

    @Override
    public float asNumber(UXTheme theme) {
        UXValue variable = getVariable(theme);
        if (variable != null) {
            return variable.asNumber(theme);
        } else {
            return super.asNumber(theme);
        }
    }

    @Override
    public float asSize(UXTheme theme, float dpi) {
        UXValue variable = getVariable(theme);
        if (variable != null) {
            return variable.asSize(theme, dpi);
        } else {
            return super.asSize(theme, dpi);
        }
    }

    @Override
    public float[] asSizeList(UXTheme theme, float dpi) {
        UXValue variable = getVariable(theme);
        if (variable != null) {
            return variable.asSizeList(theme, dpi);
        } else {
            return super.asSizeList(theme, dpi);
        }
    }

    @Override
    public UXValue[] getValues(UXTheme theme) {
        UXValue variable = getVariable(theme);
        if (variable != null) {
            return variable.getValues(theme);
        } else {
            return super.getValues(theme);
        }
    }

    @Override
    public float asAngle(UXTheme theme) {
        UXValue variable = getVariable(theme);
        if (variable != null) {
            return variable.asAngle(theme);
        } else {
            return super.asAngle(theme);
        }
    }

    @Override
    public int asColor(UXTheme theme) {
        UXValue variable = getVariable(theme);
        if (variable != null) {
            return Color.multiplyColorAlpha(variable.asColor(theme), alpha);
        } else {
            return super.asColor(theme);
        }
    }

    @Override
    public Font asFont(UXTheme theme) {
        UXValue variable = getVariable(theme);
        if (variable != null) {
            return variable.asFont(theme);
        } else {
            return super.asFont(theme);
        }
    }

    @Override
    public ResourceStream asResource(UXTheme theme) {
        UXValue variable = getVariable(theme);
        if (variable != null) {
            return variable.asResource(theme);
        } else {
            return super.asResource(theme);
        }
    }

    @Override
    public <T extends Enum<T>> T asConstant(UXTheme theme, Class<T> tClass) {
        UXValue variable = getVariable(theme);
        if (variable != null) {
            return variable.asConstant(theme, tClass);
        } else {
            return super.asConstant(theme, tClass);
        }
    }

    @Override
    public <T> UXListener<T> asListener(UXTheme theme, Class<T> argument, Controller controller) {
        UXValue variable = getVariable(theme);
        if (variable != null) {
            return variable.asListener(theme, argument, controller);
        } else {
            return super.asListener(theme, argument, controller);
        }
    }

    @Override
    public <T> UXValueListener<T> asValueListener(UXTheme theme, Class<T> argument, Controller controller) {
        UXValue variable = getVariable(theme);
        if (variable != null) {
            return variable.asValueListener(theme, argument, controller);
        } else {
            return super.asValueListener(theme, argument, controller);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UXValueVariable that = (UXValueVariable) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Variable : " + name;
    }
}
