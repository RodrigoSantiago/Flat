package flat.uxml.value;

import flat.uxml.UXTheme;

import java.util.Arrays;
import java.util.Objects;

public class UXValueSizeList extends UXValue {

    private final UXValue[] values;
    private final String name;

    public UXValueSizeList(String name, UXValue[] values) {
        this.values = values;
        this.name = name;
    }

    public boolean containsVariable() {
        for (var val : values) {
            if (val instanceof UXValueVariable) {
                return true;
            } else if (val instanceof UXValueSizeList innerList) {
                if (innerList.containsVariable()) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getName() {
        return name;
    }

    @Override
    UXValue internalMix(UXValue uxValue, float t, UXTheme theme, float dpi) {
        if (getSourceType(theme) != uxValue.getSourceType(theme)) {
            return super.internalMix(uxValue, t, theme, dpi);
        } else {
            UXValue[] uxValues = uxValue.getValues(theme);
            if (uxValues.length == values.length) {
                UXValue[] mix = new UXValue[values.length];
                for (int i = 0; i < mix.length; i++) {
                    mix[i] = uxValues[i].mix(values[i], t, theme, dpi);
                }
                return new UXValueSizeList(getName(), mix);
            }
            return super.internalMix(uxValue, t, theme, dpi);
        }
    }

    @Override
    public float[] asSizeList(UXTheme theme) {
        float[] val = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            val[i] = values[i].asSize(theme);
        }
        return val;
    }

    @Override
    public UXValue[] getValues(UXTheme theme) {
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UXValueSizeList that = (UXValueSizeList) o;
        return Objects.equals(name, that.name) && Arrays.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, Arrays.hashCode(values));
    }

    @Override
    public String toString() {
        return "List : " + name + " " + Arrays.toString(values);
    }
}
