package flat.uxml.value;

import flat.animations.Interpolation;
import flat.uxml.UXTheme;

import java.util.Objects;

public class UXValueColor extends UXValue {
    private final int rgba;

    public UXValueColor(int rgba) {
        this.rgba = rgba;
    }

    @Override
    UXValue internalMix(UXValue uxValue, float t, UXTheme theme, float dpi) {
        if (uxValue.isColor(theme)) {
            int v1 = asColor(theme);
            int v2 = uxValue.asColor(theme);
            return new UXValueColor(Interpolation.mixColor(v1, v2, t));
        } else {
            return super.internalMix(uxValue, t, theme, dpi);
        }
    }

    @Override
    public int asColor(UXTheme theme) {
        return rgba;
    }

    @Override
    public boolean isColor(UXTheme theme) {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UXValueColor that = (UXValueColor) o;
        return rgba == that.rgba;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rgba);
    }

    @Override
    public String toString() {
        return "Color : " + Integer.toHexString(rgba).toUpperCase();
    }
}
