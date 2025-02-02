package flat.uxml.value;

import flat.animations.Interpolation;
import flat.uxml.UXTheme;

import java.util.Objects;

public class UXValueAngle extends UXValue {
    private final float angle;

    public UXValueAngle(float angle) {
        this.angle = angle;
    }

    @Override
    UXValue internalMix(UXValue uxValue, float t, UXTheme theme, float dpi) {
        if (getSourceType(theme) != uxValue.getSourceType(theme)) {
            return super.internalMix(uxValue, t, theme, dpi);
        } else {
            return new UXValueAngle(Interpolation.mixAngle(angle, uxValue.asAngle(theme), t));
        }
    }

    @Override
    public float asAngle(UXTheme theme) {
        return angle;
    }

    @Override
    public float asNumber(UXTheme theme) {
        return angle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UXValueAngle that = (UXValueAngle) o;
        return Float.compare(that.angle, angle) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(angle);
    }

    @Override
    public String toString() {
        return "Angle : " + angle;
    }
}
