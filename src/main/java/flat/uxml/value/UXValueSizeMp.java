package flat.uxml.value;

import flat.uxml.UXTheme;
import flat.widget.Widget;

import java.util.Objects;

public class UXValueSizeMp extends UXValue {

    public UXValueSizeMp() {
    }

    @Override
    public float asSize(UXTheme theme, float dpi) {
        return Widget.MATCH_PARENT;
    }

    @Override
    public float[] asSizeList(UXTheme theme, float dpi) {
        return new float[] {Widget.MATCH_PARENT};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Widget.MATCH_PARENT);
    }

    @Override
    public String toString() {
        return "Size MATCH_PARENT";
    }
}
