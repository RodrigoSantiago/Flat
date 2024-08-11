package flat.uxml.value;

import flat.animations.Interpolation;
import flat.uxml.UXTheme;

import java.util.Objects;

public class UXValueNumber extends UXValue {
    private final float value;

    public UXValueNumber(float value) {
        this.value = value;
    }

    @Override
    UXValue internalMix(UXValue uxValue, float t, UXTheme theme) {
        if (uxValue.isSize()) {
            float v1 = asSize(theme);
            float v2 = uxValue.asSize(theme);
            if (Math.abs(v1 - v2) > 0.01f) {
                return new UXValueNumber(Interpolation.mix(v1, v2, t));
            }
            return this;
        } else {
            return super.internalMix(uxValue, t, theme);
        }
    }

    @Override
    public boolean isSize() {
        return true;
    }

    @Override
    public float asNumber(UXTheme theme) {
        return value;
    }

    @Override
    public float asSize(UXTheme theme) {
        return value;
    }

    @Override
    public float asAngle(UXTheme theme) {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UXValueNumber that = (UXValueNumber) o;
        return Float.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Number : " + value;
    }
}
