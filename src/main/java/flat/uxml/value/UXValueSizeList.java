package flat.uxml.value;

import flat.uxml.UXTheme;

import java.util.Arrays;

public class UXValueSizeList extends UXValue {

    private final UXValue[] values;

    public UXValueSizeList(UXValue[] values) {
        this.values = values;
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
                return new UXValueSizeList(mix);
            }
            return super.internalMix(uxValue, t, theme, dpi);
        }
    }

    @Override
    public float[] asSizeList(UXTheme theme, float dpi) {
        float[] val = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            val[i] = values[i].asSize(theme, dpi);
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
        return Arrays.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    @Override
    public String toString() {
        return "Size LIST : " + Arrays.toString(values);
    }
}
