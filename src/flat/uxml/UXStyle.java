package flat.uxml;

import flat.animations.StateInfo;
import flat.graphics.context.Font;
import flat.resources.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class UXStyle {

    public final String name;
    public final UXTheme theme;
    public final UXStyle parent;
    protected HashSet<String> properties;
    protected HashMap<String, UXValue>[] entries;

    UXStyle(String name, UXTheme theme) {
        this.name = name;
        this.theme = theme;
        this.parent = null;
        instance();
    }

    UXStyle(String name, UXStyle parent) {
        this.name = name;
        this.theme = parent == null ? null : parent.theme;
        this.parent = parent;
        instance();
    }

    UXStyle(String name,  HashMap<String, UXValue>[] values, UXStyle parent) {
        this.name = name;
        this.theme = null;
        this.parent = parent;
        this.entries = values;
        properties = new HashSet<>();
        for (int i = 0; i < 8; i++) {
            properties.addAll(entries[i].keySet());
        }
    }

    public ArrayList<String> dynamicFinder(String startWith) {
        ArrayList<String> names = new ArrayList<>();
        for (HashMap<String, UXValue> entry : entries) {
            for (String name : entry.keySet()){
                if (name.startsWith(startWith) && !names.contains(name)) {
                    names.add(name);
                }
            }
        }
        return names;
    }

    public UXTheme getTheme() {
        return theme;
    }

    protected void instance() {
        entries = new HashMap[8];
        properties = new HashSet<>();
        for (int i = 0; i < 8; i++) {
            entries[i] = new HashMap<>();
        }
    }

    protected void add(String name, UXValue value, int state) {
        entries[state].put(name, value);
        properties.add(name);
    }

    /**
     * Retorna um valor de atributo para o nome e estado especificado
     *
     * @param name Nome
     * @param index Indice de Estado
     * @return UXValue ou Null para valores não definidos
     */
    public UXValue get(String name, int index) {
        UXValue value = entries[index].get(name);
        if (value == null && parent != null) {
            return parent.get(name, index);
        }
        return value;
    }

    /**
     * Verifica se este estilo contem algum modificador do atributo, em pelo menos um estado
     *
     * @param name Atributo
     * @return true-false
     */
    public boolean contains(String name) {
        return properties.contains(name);
    }

    /**
     * Verifica se este estilo contem algum modificador de atributo diferente nesta mudanca de estado
     *
     * @param stateA Estado A
     * @param stateB Estado B
     * @return
     */
    public boolean containsChange(byte stateA, byte stateB) {
        for (int i = 0; i < 8; i++) {
            if (((stateA & (1 << i)) == (1 << i)) != ((stateB & (1 << i)) == (1 << i))) {
                if (entries[i].size() > 0) {
                    return true;
                }
            }
        }
        return parent != null && parent.containsChange(stateA, stateB);
    }

    public String asString(String name) {
        return asString(name, null, null);
    }

    public String asString(String name, String def) {
        return asString(name, null, def);
    }

    public String asString(String name, StateInfo state) {
        return asString(name, state, null);
    }

    public String asString(String name, StateInfo state, String def) {
        UXValue value = getValue(name, state);
        if (value == null) {
            return def;
        } else {
            return value.asString();
        }
    }

    public boolean asBool(String name) {
        return asBool(name, null, false);
    }

    public boolean asBool(String name, boolean def) {
        return asBool(name, null, def);
    }

    public boolean asBool(String name, StateInfo state) {
        return asBool(name, state, false);
    }

    public boolean asBool(String name, StateInfo state, boolean def) {
        UXValue value = getValue(name, state);
        if (value == null) {
            return def;
        } else {
            return value.asBool();
        }
    }

    public float asNumber(String name) {
        return asNumber(name, null, 0);
    }

    public float asNumber(String name, float def) {
        return asNumber(name, null, def);
    }

    public float asNumber(String name, StateInfo state) {
        return asNumber(name, state, 0);
    }

    public float asNumber(String name, StateInfo state, float def) {
        UXValue value = getValue(name, state);
        if (value == null) {
            return def;
        } else {
            return value.asNumber();
        }
    }

    public float asSize(String name) {
        return asSize(name, null, 0);
    }

    public float asSize(String name, float def) {
        return asSize(name, null, def);
    }

    public float asSize(String name, StateInfo state) {
        return asSize(name, state, 0);
    }

    public float asSize(String name, StateInfo state, float def) {
        UXValue value = getValue(name, state);
        if (value == null) {
            return def;
        } else {
            return value.asSize(theme);
        }
    }

    public float asAngle(String name) {
        return asAngle(name, null, 0);
    }

    public float asAngle(String name, float def) {
        return asAngle(name, null, def);
    }

    public float asAngle(String name, StateInfo state) {
        return asAngle(name, state, 0);
    }

    public float asAngle(String name, StateInfo state, float def) {
        UXValue value = getValue(name, state);
        if (value == null) {
            return def;
        } else {
            return value.asAngle();
        }
    }

    public int asColor(String name) {
        return asColor(name, null, 0);
    }

    public int asColor(String name, int def) {
        return asColor(name, null, def);
    }

    public int asColor(String name, StateInfo state) {
        return asColor(name, state, 0);
    }

    public int asColor(String name, StateInfo state, int def) {
        UXValue value = getValue(name, state);
        if (value == null) {
            return def;
        } else {
            return value.asColor();
        }
    }

    public Font asFont(String name) {
        return asFont(name, null, Font.DEFAULT);
    }

    public Font asFont(String name, Font def) {
        return asFont(name, null, def);
    }

    public Font asFont(String name, StateInfo state) {
        return asFont(name, state, Font.DEFAULT);
    }

    public Font asFont(String name, StateInfo state, Font def) {
        UXValue value = getValue(name, state);
        if (value == null) {
            return def;
        } else {
            return value.asFont();
        }
    }

    public Resource asResource(String name) {
        return asResource(name, null, null);
    }

    public Resource asResource(String name, Resource def) {
        return asResource(name, null, def);
    }

    public Resource asResource(String name, StateInfo state) {
        return asResource(name, state, null);
    }

    public Resource asResource(String name, StateInfo state, Resource def) {
        UXValue value = getValue(name, state);
        if (value == null) {
            return def;
        } else {
            return value.asResource();
        }
    }

    public <T extends Enum> T asConstant(String name, T def) {
        return asConstant(name, null, def);
    }

    public <T extends Enum> T asConstant(String name, StateInfo state, T def) {
        UXValue value = getValue(name, state);
        if (value == null) {
            return def;
        } else {
            return value.asConstant((Class<T>) def.getClass());
        }
    }

    public UXValue getValue(String name) {
        return getValue(name, null);
    }

    public UXValue getValue(String name, StateInfo state) {
        return _getValue(name, state, 7);
    }

    private UXValue _getValue(String name, StateInfo state, int index) {
        if (index == 0 || state == null) return get(name, 0);

        float t = state.get(index);
        if (t == 0) {
            return _getValue(name, state, index - 1);
        } else {
            UXValue value = get(name, index);
            if (value != null) {
                if (t == 1) {
                    return value;
                } else {
                    UXValue left = _getValue(name, state, index - 1);
                    if (left != null) {
                        return left.mix(value, t, theme);
                    } else {
                        return value;
                    }
                }
            } else {
                return _getValue(name, state, index - 1);
            }
        }
    }
}
