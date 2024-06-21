package flat.uxml.value;

import flat.uxml.UXTheme;
import flat.uxml.UXValue;

import java.util.Objects;

public class UXValueBool extends UXValue {
    private boolean bool;

    public UXValueBool(boolean bool) {
        this.bool = bool;
    }

    @Override
    public boolean asBool(UXTheme theme) {
        return bool;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UXValueBool that = (UXValueBool) o;
        return bool == that.bool;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bool);
    }
}
