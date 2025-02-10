package flat.uxml;

import flat.widget.Widget;

public class ValueChange<T> {
    private Widget source;
    private T oldValue;
    private T value;

    public ValueChange(Widget source, T oldValue, T value) {
        this.source = source;
        this.oldValue = oldValue;
        this.value = value;
    }

    public Widget getSource() {
        return source;
    }

    public T getOldValue() {
        return oldValue;
    }

    public T getValue() {
        return value;
    }
}
