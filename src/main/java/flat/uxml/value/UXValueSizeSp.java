package flat.uxml.value;

import flat.animations.Interpolation;
import flat.resources.Dimension;
import flat.uxml.UXTheme;

import java.util.Objects;

public class UXValueSizeSp extends UXValue {
    private final float value;

    public UXValueSizeSp(float value) {
        this.value = value;
    }

    @Override
    UXValue internalMix(UXValue uxValue, float t, UXTheme theme, float dpi) {
        if (uxValue.isSize(theme)) {
            float v1 = asSize(theme);
            float v2 = uxValue.asSize(theme);
            if (Math.abs(v1 - v2) > 0.01f) {
                return new UXValueNumber(Interpolation.mix(v1, v2, t));
            }
            return this;
        } else {
            return super.internalMix(uxValue, t, theme, dpi);
        }
    }

    @Override
    public boolean isSize(UXTheme theme) {
        return true;
    }

    @Override
    public float asNumber(UXTheme theme) {
        return value;
    }

    @Override
    public float asSize(UXTheme theme) {
        float density = theme == null ? 160f : theme.getDpi();
        float fontScale = theme == null ? 1f : theme.getFontScale();
        return Math.round(value * (Dimension.getDensity(density).dpi / 160f * fontScale));
    }

    @Override
    public float[] asSizeList(UXTheme theme) {
        return new float[] {asSize(theme)};
    }

    @Override
    public float asAngle(UXTheme theme) {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UXValueSizeSp that = (UXValueSizeSp) o;
        return Float.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Size SP : " + value;
    }
}
