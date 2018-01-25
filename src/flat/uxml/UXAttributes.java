package flat.uxml;

import flat.graphics.context.Font;
import flat.uxml.data.Dimension;
import flat.widget.Widget;

import java.util.HashMap;
import java.util.Map;

public class UXAttributes {
    private final HashMap<String, Attribute> attributes = new HashMap<>();
    private final UXLoader loader;
    private boolean readOnly;

    public UXAttributes(UXLoader loader) {
        this.loader = loader;
    }

    public UXLoader getLoader() {
        return loader;
    }

    public void set(String name, String value) {
        if (readOnly) {
            throw new RuntimeException("Read only attributes");
        }
        attributes.put(name, new Attribute(value));
    }

    public void pack() {
        this.readOnly = true;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public String get(String name) {
        Attribute val = attributes.get(name);
        return val == null ? null : val.use();
    }

    public <T extends Enum<T>> T asConstant(String name, Class<T> tClass, T def) {
        T result = def;
        String value = get(name);
        if (value != null) {
            try {
                result = Enum.valueOf(tClass, value);
            } catch (Exception e) {
                e.printStackTrace();
                loader.log("Invalid constant : [" + name + " = " + value + "]");
            }
        }
        return result;
    }

    public String asString(String name, String def) {
        String result = def;
        String value = get(name);
        if (value != null) {
            result = value;
        }
        return result;
    }

    public float asSize(String name, float def) {
        float result = def;
        String value = get(name);
        if (value != null) {
            Dimension dimension = loader.getDimension();
            if ("WRAP_CONTENT".equalsIgnoreCase(value)) {
                result = Widget.WRAP_CONTENT;
            } else if ("MATCH_PARENT".equalsIgnoreCase(value)) {
                result = Widget.MATCH_PARENT;
            } else if (value.matches("\\d+(\\.\\d+)?")) {
                result = Float.parseFloat(value) * (dimension.dpi / 160F);
            } else if (value.matches("\\d+(\\.\\d+)?dp")) {
                result = Float.parseFloat(value.substring(0, value.length() - 2)) * (dimension.dpi / 160F);
            } else if (value.matches("\\d+(\\.\\d+)?sp")) {
                result = Float.parseFloat(value.substring(0, value.length() - 2)) * (dimension.dpi / 160F) * loader.getFontScale();
            } else if (value.matches("\\d+(\\.\\d+)?px")) {
                result = Float.parseFloat(value.substring(0, value.length() - 2));
            } else if (value.matches("\\d+(\\.\\d+)?in")) {
                result = Float.parseFloat(value.substring(0, value.length() - 2)) * dimension.dpi;
            } else if (value.matches("\\d+(\\.\\d+)?pt")) {
                result = Float.parseFloat(value.substring(0, value.length() - 2)) * (dimension.dpi / 72F);
            } else if (value.matches("\\d+(\\.\\d+)?mm")) {
                result = Float.parseFloat(value.substring(0, value.length() - 2)) * (dimension.dpi / 25.4F);
            } else if (value.matches("\\d+(\\.\\d+)?cm")) {
                result = Float.parseFloat(value.substring(0, value.length() - 2)) * (dimension.dpi / 2.54F);
            } else {
                loader.log("Invalid size : [" + name + " = " + value + "]");
            }
        }
        return Math.round(result);
    }

    public float asNumber(String name, float def) {
        float result = def;
        String value = get(name);
        if (value != null) {
            if (value.matches("\\d+(\\.\\d+)?")) {
                result = Float.parseFloat(value);
            } else {
                loader.log("Invalid number : [" + name + " = " + value + "]");
            }
        }
        return result;
    }

    public boolean asBoolean(String name, boolean def) {
        boolean result = def;
        String value = get(name);
        if (value != null) {
            if ("true".equalsIgnoreCase(value)) {
                result = true;
            } else if ("false".equalsIgnoreCase(value)) {
                result = false;
            } else {
                loader.log("Invalid boolean : [" + name + " = " + value + "]");
            }
        }
        return result;
    }

    public int asColor(String name, int def) {
        int result = def;
        String value = get(name);
        if (value != null) {
            if (value.matches("#[ABCDEFabcdef0-9]{6}")) {
                result = ((int) Long.parseLong(value.substring(1), 16) << 8) | 0x000000FF;
            } else if (value.matches("#[ABCDEFabcdef0-9]{8}")) {
                result = (int) Long.parseLong(value.substring(1), 16);
            } else {
                loader.log("Invalid color : [" + name + " = " + value + "]");
            }
        }
        return result;
    }

    public Font asFont(String name, Font def) {
        Font result = def;
        String value = get(name);
        if (value != null) {
            result = Font.findFont(value);
        }
        return result;
    }

    public String asString(String name) {
        return asString(name, null);
    }

    public float asSize(String name) {
        return asSize(name, 0F);
    }

    public float asNumber(String name) {
        return asNumber(name, 0F);
    }

    public boolean asBoolean(String name) {
        return asBoolean(name, false);
    }

    public int asColor(String name) {
        return asColor(name, 0);
    }

    public Font asFont(String name) {
        return asFont(name, Font.DEFAULT);
    }

    public void logUnusedAttributes() {
        for (Map.Entry<String, Attribute> val : attributes.entrySet()) {
            if (!val.getValue().used) {
                loader.log("Unused attribute : [" + val.getKey() + " = " + val.getValue().value + "]");
            }
        }
    }

    private static class Attribute {
        public String value;
        public boolean used;
        public Attribute(String value) {
            this.value = value;
        }

        private String use() {
            used = true;
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) return false;
            if (obj == this) return true;
            if (obj instanceof  Attribute) {
                String otherValue = ((Attribute) obj).value;
                return (value != null && value.equals(otherValue)) || otherValue == null;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }
}
