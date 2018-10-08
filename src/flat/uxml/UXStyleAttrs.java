package flat.uxml;

import flat.graphics.context.Font;
import flat.graphics.image.Drawable;
import flat.math.shapes.Stroke;
import flat.resources.Resource;

import java.util.HashMap;

public final class UXStyleAttrs extends UXStyle {

    private UXLoader loader;

    public UXStyleAttrs(String name, UXStyle parent, UXLoader loader) {
        this(name, parent, loader, new HashMap<>());
    }

    public UXStyleAttrs(String name, UXStyle parent, UXLoader loader, HashMap<String, UXValue> values) {
        super(name, parent);
        this.loader = loader;
        entries[0] = values;
    }

    @Override
    protected void instance() {
        entries = new HashMap[1];
    }

    public final void link(String name, UXGadgetLinker linker) {
        if (loader != null) {
            loader.addLink(getValue(name).asString(), linker);
        }
    }

    public final void setValue(String name, UXValue value) {
        entries[0].put(name, value);
    }

    public final void unsetValue(String name) {
        entries[0].remove(name);
    }

    public final void setString(String name, String value) {
        setValue(name, new UXValue(UXValue.string, value));
    }

    public final void setNumber(String name, float value) {
        setValue(name, new UXValue(UXValue.number, value));
    }

    public final void setColor(String name, int value) {
        setValue(name, new UXValue(UXValue.color, value));
    }

    public final void setRect(String name, float[] value) {
        setValue(name, new UXValue(UXValue.rect, value.clone()));
    }

    public final void setFont(String name, Font value) {
        setValue(name, new UXValue(UXValue.font, value));
    }

    public final void setResource(String name, Resource value) {
        setValue(name, new UXValue(UXValue.resource, value));
    }

    public final void setResource(String name, String value) {
        setValue(name, new UXValue(UXValue.resource, value));
    }

    public final void setConstant(String name, String value) {
        setValue(name, new UXValue(UXValue.constant, value));
    }

    public final void setListener(String name, String value) {
        setValue(name, new UXValue(UXValue.listener, value));
    }

    @Override
    public UXValue get(String name, int index) {
        UXValue value = entries[0].get(name);
        if (value != null) {
            return value;
        } else {
            return parent == null ? null : parent.get(name, index);
        }
    }
}
