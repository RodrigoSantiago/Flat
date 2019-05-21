package flat.uxml;

import flat.graphics.context.Font;
import flat.resources.Resource;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public final class UXStyleAttrs extends UXStyle {

    private WeakReference<UXLoader> loader;

    public UXStyleAttrs(String name, UXStyle parent) {
        this(name, parent, null);
    }

    public UXStyleAttrs(String name, UXStyle parent, UXLoader loader) {
        this(name, parent, loader, new HashMap<>());
    }

    public UXStyleAttrs(String name, UXStyle parent, UXLoader loader, HashMap<String, UXValue> values) {
        super(name, parent);
        this.loader = loader == null ? null : new WeakReference<>(loader);
        entries[0] = values;
    }

    public UXStyleAttrs(String name, HashMap<String, UXValue>[] values) {
        this(name, new UXStyle(name, values, null));
    }

    public UXStyleAttrs(String name, HashMap<String, UXValue>[] values, UXStyle parent) {
        this(name, new UXStyle(name, values, parent));
    }

    @Override
    protected void instance() {
        entries = new HashMap[1];
    }

    @Override
    public boolean contains(String name) {
        return (properties == null || !properties.contains(name)) &&
                (entries[0].containsKey(name) || (parent != null && parent.contains(name)));
    }

    @Override
    public boolean containsChange(byte stateA, byte stateB) {
        if (parent != null) {
            return parent.containsChange(stateA, stateB);
        } else {
            return false;
        }
    }

    @Override
    public UXValue get(String name, int index) {
        if (properties != null && properties.contains(name)) {
            return null;
        }

        UXValue value = entries[0].get(name);
        if (value != null) {
            return value;
        } else {
            return parent == null ? null : parent.get(name, index);
        }
    }

    @Override
    public UXTheme getTheme() {
        return theme;
    }

    public void link(String name, UXGadgetLinker linker) {
        if (this.loader != null) {
            UXLoader loader = this.loader.get();
            if (loader != null) {
                UXValue value = getValue(name);
                if (value != null) {
                    loader.addLink(value.asString(), linker);
                }
            } else {
                this.loader = null;
            }
        }
    }

    public void addValue(String name, UXValue value) {
        entries[0].put(name, value);
        if (properties != null) {
            properties.remove(name);
        }
    }

    public void remove(String name) {
        entries[0].remove(name);
    }

    public void unfollow(String name) {
        entries[0].remove(name);
        if (parent != null && parent.contains(name)) {
            if (properties == null) {
                properties = new HashSet<>();
            }
            properties.add(name);
        }
    }

    public void add(String name, String value) {
        addValue(name, new UXValue(UXValue.string, value));
    }

    public void add(String name, boolean value) {
        addValue(name, new UXValue(UXValue.bool, value));
    }

    public void add(String name, float value) {
        addValue(name, new UXValue(UXValue.number, value));
    }

    public void add(String name, int value) {
        addValue(name, new UXValue(UXValue.color, value));
    }

    public void add(String name, Font value) {
        addValue(name, new UXValue(UXValue.font, value));
    }

    public void add(String name, Resource value) {
        addValue(name, new UXValue(UXValue.resource, value));
    }

    public void add(String name, Enum value) {
        addValue(name, new UXValue(UXValue.constant, value.toString()));
    }

    public void set(String name, String value) {
        if (!contains(name)) add(name, value);
    }

    public void set(String name, boolean value) {
        if (!contains(name)) add(name, value);
    }

    public void set(String name, float value) {
        if (!contains(name)) add(name, value);
    }

    public void set(String name, int value) {
        if (!contains(name)) add(name, value);
    }

    public void set(String name, Font value) {
        if (!contains(name)) add(name, value);
    }

    public void set(String name, Resource value) {
        if (!contains(name)) add(name, value);
    }

    public void set(String name, Enum value) {
        if (!contains(name)) add(name, value);
    }

    public Method asListener(String name, Class<?> argument, Controller controller) {
        UXValue value = get(name, 0);
        if (value == null) {
            return null;
        } else {
            return value.asListener(argument, controller);
        }
    }
}