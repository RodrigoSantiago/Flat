package flat.uxml;

import flat.uxml.value.UXValue;
import flat.widget.Widget;

import java.util.HashMap;

public class UXChild {
    private Widget widget;
    private HashMap<Integer, UXValue> parentValues;

    public UXChild(Widget widget, HashMap<Integer, UXValue> parentValues) {
        this.widget = widget;
        this.parentValues = parentValues;
    }

    public Widget getWidget() {
        return widget;
    }

    private UXValue getAttribute(String name) {
        return parentValues == null ? null : parentValues.get(UXHash.getHash(name));
    }

    public String getAttributeString(String name, String def) {
        UXValue value = getAttribute(name);
        return value != null ? value.asString(null) : def;
    }

    public boolean getAttributeBool(String name, boolean def) {
        UXValue value = getAttribute(name);
        return value != null ? value.asBool(null) : def;
    }

    public float getAttributeNumber(String name, float def) {
        UXValue value = getAttribute(name);
        return value != null ? value.asNumber(null) : def;
    }

    public <T extends Enum<T>>T getAttributeConstant(String name, T def) {
        UXValue value = getAttribute(name);
        if (value != null) {
            var constant = value.asConstant(null, def.getDeclaringClass());
            if (constant != null) {
                return constant;
            }
        }
        return def;
    }
}
