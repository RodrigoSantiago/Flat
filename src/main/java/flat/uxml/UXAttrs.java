package flat.uxml;

import flat.animations.StateInfo;
import flat.exception.FlatException;
import flat.graphics.cursor.Cursor;
import flat.graphics.symbols.Font;
import flat.graphics.image.Drawable;
import flat.graphics.image.DrawableReader;
import flat.resources.ResourceStream;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueLocale;
import flat.widget.State;
import flat.widget.Widget;
import flat.window.Activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UXAttrs {

    private final Widget widget;
    private final String base;
    private ArrayList<String> styleNames = new ArrayList<>();
    private ArrayList<UXStyle> styles = new ArrayList<>();
    private Activity activity;
    private UXTheme theme;
    private BitArray bitArray;
    private HashMap<Integer, UXValue> attributes;
    private boolean invalidStyles;

    public UXAttrs(Widget widget, String base) {
        this.widget = widget;
        this.base = base;
        styleNames.add(base);
    }

    public static String convertToKebabCase(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }

        return camelCase
                .replaceAll("([a-z])([A-Z])", "$1-$2")
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1-$2")
                .toLowerCase();
    }

    public boolean addStyleName(String name) {
        if (!styleNames.contains(name)) {
            styleNames.add(name);
            invalidStyles = true;
            return true;
        }
        return false;
    }

    public boolean removeStyleName(String name) {
        if (base.equals(name)) {
            return false;
        }
        if (styleNames.remove(name)) {
            invalidStyles = true;
            return true;
        }
        return false;
    }

    public void cleatStyles() {
        styleNames.clear();
        styleNames.add(base);
        invalidStyles = true;
    }

    public int getStyleNameCount() {
        return styleNames.size();
    }

    public String getStyleName(int index) {
        return styleNames.get(index);
    }

    public List<String> getStyleNames() {
        return new ArrayList<>(styleNames);
    }

    public List<UXStyle> getStyles() {
        return new ArrayList<>(getUpdatedStyles());
    }

    public void setTheme(UXTheme theme) {
        if (this.theme != theme) {
            this.theme = theme;
            invalidStyles = true;
        }
    }

    public UXTheme getTheme() {
        return theme;
    }

    public Activity getActivity() {
        return widget.getActivity();
    }

    private ArrayList<UXStyle> getUpdatedStyles() {
        if (invalidStyles && theme != null) {
            invalidStyles = false;
            styles.clear();
            for (String styleName : styleNames) {
                UXStyle style = theme.getStyle(base + "." + styleName);
                if (style != null) {
                    styles.add(style);
                } else {
                    style = theme.getStyle(styleName);
                    if (style != null) {
                        styles.add(style);
                    }
                }
            }
        }
        return styles;
    }

    public boolean contains(String name) {
        Integer hash = UXHash.getHash(name);
        if (attributes != null && attributes.containsKey(hash)) {
            return true;
        }
        for (UXStyle style : getUpdatedStyles()) {
            if (style.contains(hash)) {
                return true;
            }
        }
        return false;
    }

    public boolean containsChange(byte stateA, byte stateB) {
        for (UXStyle style : getUpdatedStyles()) {
            if (style.containsChange(stateA, stateB)) {
                return true;
            }
        }
        return false;
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

    public void unfollow(String name) {
        Integer hash = UXHash.getHash(name);
        if (bitArray == null) {
            bitArray = new BitArray();
        }
        bitArray.set(hash, true);
    }

    public void clearUnfollow(String name) {
        if (bitArray != null) {
            bitArray.set(UXHash.getHash(name), false);
        }
    }

    public boolean isUnfollow(String name) {
        return bitArray != null && bitArray.get(UXHash.getHash(name));
    }

    public String getAttributeString(String name, String def) {
        UXValue value = getAttribute(name);
        return value != null ? value.asString(theme) : def;
    }

    public String getAttributeLocale(String name, String def) {
        UXValue value = getAttribute(name);
        if (value != null && value.getSource(theme) instanceof UXValueLocale locale) {
            return locale.asString(theme);
        }
        return def;
    }

    public boolean getAttributeBool(String name, boolean def) {
        UXValue value = getAttribute(name);
        return value != null ? value.asBool(theme) : def;
    }

    public float getAttributeNumber(String name, float def) {
        UXValue value = getAttribute(name);
        return value != null ? value.asNumber(theme) : def;
    }

    public float getAttributeSize(String name, float def) {
        UXValue value = getAttribute(name);
        return value != null ? value.asSize(theme) : def;
    }

    public float getAttributeAngle(String name, float def) {
        UXValue value = getAttribute(name);
        return value != null ? value.asAngle(theme) : def;
    }

    public int getAttributeColor(String name, int def) {
        UXValue value = getAttribute(name);
        return value != null ? value.asColor(theme) : def;
    }

    public Font getAttributeFont(String name, Font def) {
        UXValue value = getAttribute(name);
        return value != null ? value.asFont(theme) : def;
    }

    public ResourceStream getAttributeResource(String name, ResourceStream def) {
        UXValue value = getAttribute(name);
        return value != null ? value.asResource(theme) : def;
    }

    public Drawable getAttributeDrawable(String name, Drawable def, boolean handleException) {
        UXValue value = getAttribute(name);
        if (value != null) {
            Drawable drawable = value.asDrawable(theme);
            if (drawable != null) {
                return drawable;
            }

            ResourceStream resource = value.asResource(theme);
            if (resource != null && !resource.isFolder()) {
                try {
                    return DrawableReader.parse(resource);
                } catch (Exception exception) {
                    if (handleException) throw new FlatException("Failed to load image", exception);
                    return null;
                }
            }
        }

        return def;
    }

    public <T extends Enum<T>>T getAttributeConstant(String name, T def) {
        UXValue value = getAttribute(name);
        if (value != null) {
            var constant = value.asConstant(theme, def.getDeclaringClass());
            if (constant != null) {
                return constant;
            }
        }
        return def;
    }

    public <T> UXListener<T> getAttributeListener(String name, Class<T> argument, Controller controller) {
        UXValue value = getAttribute(name);
        return value != null ? value.asListener(theme, argument, controller) : null;
    }

    public <T> UXValueListener<T> getAttributeValueListener(String name, Class<T> argument, Controller controller) {
        UXValue value = getAttribute(name);
        return value != null ? value.asValueListener(theme, argument, controller) : null;
    }

    public float[] getAttributeSizeList(String name, float[] def) {
        UXValue value = getAttribute(name);
        return value != null ? value.asSizeList(theme) : def;
    }

    public String getString(String name) {
        return getString(name, null, null);
    }

    public String getString(String name, StateInfo state, String def) {
        UXValue value = getValue(UXHash.getHash(name), state);
        if (value != null) {
            def = value.asString(theme);
        }
        return def;
    }

    public boolean getBool(String name) {
        return getBool(name, null, false);
    }

    public boolean getBool(String name, StateInfo state, boolean def) {
        UXValue value = getValue(UXHash.getHash(name), state);
        return value != null ? value.asBool(theme) : def;
    }

    public float getNumber(String name) {
        return getNumber(name, null, 0);
    }

    public float getNumber(String name, StateInfo state, float def) {
        UXValue value = getValue(UXHash.getHash(name), state);
        return value != null ? value.asNumber(theme) : def;
    }

    public float getSize(String name) {
        return getSize(name, null, 0);
    }

    public float getSize(String name, StateInfo state, float def) {
        UXValue value = getValue(UXHash.getHash(name), state);
        return value != null ? value.asSize(theme) : def;
    }

    public float getAngle(String name) {
        return getAngle(name, null, 0);
    }

    public float getAngle(String name, StateInfo state, float def) {
        UXValue value = getValue(UXHash.getHash(name), state);
        return value != null ? value.asAngle(theme) : def;
    }

    public int getColor(String name) {
        return getColor(name, null, 0x00000000);
    }

    public int getColor(String name, StateInfo state, int def) {
        UXValue value = getValue(UXHash.getHash(name), state);
        return value != null ? value.asColor(theme) : def;
    }

    public Font getFont(String name) {
        return getFont(name, null, Font.getDefault());
    }

    public Font getFont(String name, StateInfo state, Font def) {
        UXValue value = getValue(UXHash.getHash(name), state);
        if (value != null) {
            def = value.asFont(theme);
        }
        return def;
    }

    public ResourceStream getResource(String name) {
        return getResource(name, null, null);
    }

    public ResourceStream getResource(String name, StateInfo state, ResourceStream def) {
        UXValue value = getValue(UXHash.getHash(name), state);
        if (value != null) {
            def = value.asResource(theme);
        }
        return def;
    }

    public Drawable getDrawable(String name, StateInfo state, Drawable def, boolean handleException) {
        UXValue value = getValue(UXHash.getHash(name), state);
        if (value != null) {
            Drawable drawable = value.asDrawable(theme);
            if (drawable != null) {
                return drawable;
            }

            ResourceStream resource = value.asResource(theme);
            if (resource != null && !resource.isFolder()) {
                try {
                    return DrawableReader.parse(resource);
                } catch (Exception exception) {
                    if (handleException) throw new FlatException("Failed to load image", exception);
                    return null;
                }
            }
        }

        return def;
    }

    public <T extends Enum<T>> T getConstant(String name, T def) {
        return getConstant(name, null, def);
    }

    public <T extends Enum<T>> T getConstant(String name, StateInfo state, T def) {
        UXValue value = getValue(UXHash.getHash(name), state);
        if (value != null) {
            if (def == null) {
                throw new FlatException("Constant default value cannot be null");
            }

            var constant = value.asConstant(theme, def.getDeclaringClass());
            if (constant != null) {
                return constant;
            }
        }
        return def;
    }

    public Cursor getCursor(String name, Cursor def) {
        return getCursor(name, null, def);
    }

    public Cursor getCursor(String name, StateInfo state, Cursor def) {
        UXValue value = getValue(UXHash.getHash(name), state);
        if (value != null) {
            if (def == null) {
                throw new FlatException("Cursor default value cannot be null");
            }

            var cursor = Cursor.getByName(value.asString(theme));
            if (cursor != null) {
                return cursor;
            }
        }
        return def;
    }

    public float[] getSizeList(String name) {
        return getSizeList(name, null, null);
    }

    public float[] getSizeList(String name, StateInfo state, float[] def) {
        UXValue value = getValue(UXHash.getHash(name), state);
        return value != null ? value.asSizeList(theme) : def;
    }

    private UXValue getValue(Integer hash, StateInfo state) {
        if (bitArray != null && bitArray.get(hash)) return null;
        if (attributes != null) {
            UXValue att = attributes.get(hash);
            if (att != null) {
                return att;
            }
        }

        UXValue[] currentValues = null;
        var currentStyles = getUpdatedStyles();
        for (int i = currentStyles.size() - 1; i >= 0; i--) {
            UXValue[] values = currentStyles.get(i).get(hash);
            if (values != null) {
                currentValues = values;
                break;
            }
        }
        if (currentValues == null) {
            return null;
        } else {
            float dpi = theme == null ? 160 : theme.getDpi();
            return mixStyle(state, currentValues, dpi);
        }
    }

    private UXValue mixStyle(StateInfo state, UXValue[] values, float dpi) {
        UXValue fullMix = null;
        for (int i = 0; i < 8; i++) {
            UXValue value = values[i];
            if (fullMix == null) {
                fullMix = value;
            } else {
                if (state == null) {
                    return fullMix;
                }
                float t = state.get(State.values()[i]);
                if (value != null && t > 0.001f) {
                    fullMix = fullMix.mix(value, t, theme, dpi);
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