package flat.uxml;

import flat.animations.StateInfo;
import flat.graphics.context.Font;
import flat.resources.ResourceStream;
import flat.uxml.value.UXValue;
import flat.widget.State;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Objects;

public class UXAttrs {

    private final String base;
    private String name;
    private UXTheme theme;
    private UXStyle style;
    private UXStyle baseStyle;
    private BitArray bitArray;
    private HashMap<Integer, UXValue> attributes;

    private boolean forAtt;
    private String forStyleStrTemp;
    private Integer forStyleTemp;
    private Object forStyleValue;

    private int forStyleValueInt;
    private float forStyleValueFloat;
    private boolean forStyleValueBool;

    public UXAttrs(String base) {
        this.base = base;
    }

    public void setName(String name) {
        if (!Objects.equals(this.name, name)) {
            this.name = name;
            updateStyle();
        }
    }

    public String getName() {
        return name;
    }

    public String getBase() {
        return base;
    }

    public void setTheme(UXTheme theme) {
        if (this.theme != theme) {
            this.theme = theme;
            updateStyle();
        }
    }

    public UXTheme getTheme() {
        return theme;
    }

    public UXStyle getStyle() {
        return style;
    }

    public UXStyle getBaseStyle() {
        return baseStyle;
    }

    private void updateStyle() {
        this.style = theme != null ? theme.getStyle(name) : null;
        this.baseStyle = theme != null ? theme.getStyle(base) : null;
    }

    public boolean contains(String name) {
        Integer hash = UXHash.getHash(name);
        return (attributes.containsKey(hash) ||
                (style != null && style.contains(hash)) ||
                (baseStyle != null && baseStyle.contains(hash))
        );
    }

    public boolean containsChange(byte stateA, byte stateB) {
        return (style != null && style.containsChange(stateA, stateB)) ||
                (baseStyle != null && baseStyle.containsChange(stateA, stateB));
    }

    public void link(String name, UXBuilder builder, UXGadgetLinker linker) {
        if (builder != null && attributes != null) {
            UXValue value = attributes.get(UXHash.getHash(name));
            if (value != null) {
                builder.addLink(value.asString(theme), linker);
            }
        }
    }

    public Method linkListener(String name, Class<?> argument, Controller controller) {
        clearTemp();
        if (controller != null && attributes != null) {
            UXValue value = attributes.get(UXHash.getHash(name));
            if (value != null) {
                return value.asListener(theme, argument, controller);
            }
        }
        return null;
    }

    public UXValue getAttribute(String name) {
        if (attributes == null) {
            return null;
        }
        return attributes.get(UXHash.getHash(name));
    }

    public void addAttribute(String name, UXValue value) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        attributes.put(UXHash.getHash(name), value);
    }

    public void setAttributes(HashMap<Integer, UXValue> attrs) {
        if (attrs == null || attrs.isEmpty()) {
            attributes = null;
        } else {
            attributes = new HashMap<>(attrs);
        }
    }

    public void removeAttribute(String name) {
        if (attributes != null) {
            attributes.remove(UXHash.getHash(name));
        }
    }

    public UXAttrs att(String name) {
        forAtt = true;
        forStyleStrTemp = name;
        forStyleTemp = UXHash.getHash(name);
        forStyleValue = null;
        return this;
    }

    public UXAttrs value(String name) {
        forAtt = false;
        forStyleStrTemp = name;
        forStyleTemp = UXHash.getHash(name);
        forStyleValue = null;
        return this;
    }

    private void clearTemp() {
        forAtt = false;
        forStyleStrTemp = null;
        forStyleTemp = null;
        forStyleValue = null;
    }

    public void unfollow(String name) {
        Integer hash = UXHash.getHash(name);
        if (bitArray == null) {
            bitArray = new BitArray();
        }
        bitArray.set(hash, true);
    }

    public void unfollow(String name, int value) {
        if (forStyleStrTemp != null && forStyleValueInt == value && forStyleStrTemp.equals(name)) {
            clearTemp();
        } else {
            unfollow(name);
        }
    }

    public void unfollow(String name, float value) {
        if (forStyleStrTemp != null && forStyleValueFloat == value && forStyleStrTemp.equals(name)) {
            clearTemp();
        } else {
            unfollow(name);
        }
    }

    public void unfollow(String name, boolean value) {
        if (forStyleStrTemp != null && forStyleValueBool == value && forStyleStrTemp.equals(name)) {
            clearTemp();
        } else {
            unfollow(name);
        }
    }

    public void unfollow(String name, Object object) {
        if (forStyleStrTemp != null && forStyleStrTemp.equals(name) && Objects.equals(forStyleValue, object)) {
            clearTemp();
        } else {
            unfollow(name);
        }
    }

    public void clearUnfollow(String name) {
        if (bitArray != null) {
            bitArray.set(UXHash.getHash(name), false);
        }
    }

    public boolean isUnfollow(String name) {
        return bitArray != null && bitArray.get(UXHash.getHash(name));
    }

    public String asString() {
        return asString(null, null);
    }

    public String asString(StateInfo state, String def) {
        UXValue value = getValue(forStyleTemp, state);
        if (value != null) {
            def = value.asString(theme);
        }
        forStyleValue = def;
        return def;
    }

    public boolean asBool() {
        return asBool(null, false);
    }

    public boolean asBool(StateInfo state, boolean def) {
        UXValue value = getValue(forStyleTemp, state);
        if (value != null) {
            def = value.asBool(theme);
        }
        forStyleValueBool = def;
        return def;
    }

    public float asNumber() {
        return asNumber(null, 0);
    }

    public float asNumber(StateInfo state, float def) {
        UXValue value = getValue(forStyleTemp, state);
        if (value != null) {
            def = value.asNumber(theme);
        }
        forStyleValueFloat = def;
        return def;
    }

    public float asSize() {
        return asSize(null, 0);
    }

    public float asSize(StateInfo state, float def) {
        UXValue value = getValue(forStyleTemp, state);
        if (value != null) {
            def = value.asSize(theme);
        }
        forStyleValueFloat = def;
        return def;
    }

    public float asAngle() {
        return asAngle(null, 0);
    }

    public float asAngle(StateInfo state, float def) {
        UXValue value = getValue(forStyleTemp, state);
        if (value != null) {
            def = value.asAngle(theme);
        }
        forStyleValueFloat = def;
        return def;
    }

    public int asColor() {
        return asColor(null, 0);
    }

    public int asColor(StateInfo state, int def) {
        UXValue value = getValue(forStyleTemp, state);
        if (value != null) {
            def = value.asColor(theme);
        }
        forStyleValueInt = def;
        return def;
    }

    public Font asFont() {
        return asFont(null, Font.getDefault());
    }

    public Font asFont(StateInfo state, Font def) {
        UXValue value = getValue(forStyleTemp, state);
        if (value != null) {
            def = value.asFont(theme);
        }
        forStyleValue = def;
        return def;
    }

    public ResourceStream asResource() {
        return asResource(null, null);
    }

    public ResourceStream asResource(StateInfo state, ResourceStream def) {
        UXValue value = getValue(forStyleTemp, state);
        if (value != null) {
            def = value.asResource(theme);
        }
        forStyleValue = def;
        return def;
    }

    public <T extends Enum> T asConstant(T def) {
        return asConstant(null, def);
    }

    public <T extends Enum> T asConstant(StateInfo state, T def) {
        UXValue value = getValue(forStyleTemp, state);
        if (value != null) {
            def = value.asConstant(theme, (Class<T>) def.getClass());
        }
        forStyleValue = def;
        return def;
    }

    UXValue getValue(Integer hash, StateInfo state) {
        if (bitArray != null && bitArray.get(hash)) return null;
        if (attributes != null) {
            UXValue att = attributes.get(hash);
            if (att != null) {
                return att;
            }
        }
        if (forAtt) {
            return null;
        }
        UXValue[] sValue = style != null ? style.get(hash) : null;
        UXValue[] pValue = baseStyle != null ? baseStyle.get(hash) : null;
        return mixStyle(state, sValue, pValue);
    }

    private UXValue mixStyle(StateInfo state, UXValue[] sValue, UXValue[] pValue) {
        UXValue fullMix = null;
        for (int i = 0; i < 8; i++) {
            UXValue value = sValue != null && sValue[i] != null ? sValue[i] : pValue != null ? pValue[i] : null;
            if (fullMix == null) {
                fullMix = value;
            } else {
                if (state == null) {
                    return fullMix;
                }
                float t = state.get(State.values()[i]);
                if (value != null && t > 0.001f) {
                    fullMix = fullMix.mix(value, t, theme);
                }
            }
        }
        return fullMix;
    }

    private static class BitArray {
        private int[] array;
        private int size;
        private static final int INT_SIZE = 32;
        private static final int INITIAL_CAPACITY = 1;

        public BitArray() {
            array = new int[INITIAL_CAPACITY];
            size = 0;
        }

        public void set(int index, boolean value) {
            if (index >= size * INT_SIZE) {
                if (!value) {
                    return;
                }

                resize(index / INT_SIZE + 1);
            }

            int arrayIndex = index / INT_SIZE;
            int bitIndex = index % INT_SIZE;

            if (value) {
                array[arrayIndex] |= (1 << bitIndex);
            } else {
                array[arrayIndex] &= ~(1 << bitIndex);
            }

            size = Math.max(size, index / INT_SIZE + 1);
        }

        public boolean get(int index) {
            if (index >= size * INT_SIZE) {
                return false;
            }

            int arrayIndex = index / INT_SIZE;
            int bitIndex = index % INT_SIZE;

            return (array[arrayIndex] & (1 << bitIndex)) != 0;
        }

        private void resize(int newCapacity) {
            int[] newArray = new int[newCapacity];
            System.arraycopy(array, 0, newArray, 0, array.length);
            array = newArray;
        }
    }
}