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
        if (uxValue.asColor(theme) == 0x0 && getSourceType(theme) != uxValue.getSourceType(theme)) {
            return super.internalMix(uxValue, t, theme, dpi);
        } else {
            return new UXValueColor(Interpolation.mixColor(rgba, uxValue.asColor(theme), t));
        }
    }

    @Override
    public int asColor(UXTheme theme) {
        return rgba;
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
