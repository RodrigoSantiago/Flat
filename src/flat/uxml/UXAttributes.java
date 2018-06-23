package flat.uxml;

import flat.Flat;
import flat.application.ResourcesManager;
import flat.graphics.context.Font;
import flat.graphics.context.enuns.LineCap;
import flat.graphics.context.enuns.LineJoin;
import flat.graphics.image.Image;
import flat.math.shapes.Rectangle;
import flat.math.shapes.Shape;
import flat.math.shapes.Stroke;
import flat.math.stroke.BasicStroke;
import flat.uxml.data.Dimension;
import flat.widget.Widget;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

    public void link(String name, UXWidgetLinker linker) {
        String val = asString(name);
        if (val != null) {
            loader.addLink(val, linker);
        }
    }

    public <T extends Enum<T>> T asEnum(String name, Class<T> tClass, T def) {
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

    public <T> T asConstant(String name, HashMap<String, T> map, T def) {
        T result = def;
        String value = get(name);
        if (value != null) {
            try {
                result = map.get(value);
            } catch (Exception e) {
                e.printStackTrace();
                loader.log("Invalid constant : [" + name + " = " + value + "]");
            }
        }
        return result == null ? def : result;
    }

    public Method asListener(String name, Class<?> argument) {
        return asListener(name, argument, getLoader().getController());
    }

    public Method asListener(String name, Class<?> argument, Object controller) {
        Method result = null;
        String value = get(name);
        if (value != null && controller != null) {
            try {
                Method method = controller.getClass().getMethod(value, argument);
                method.setAccessible(true);
                if (method.isAnnotationPresent(Flat.class)
                        && Modifier.isPublic(method.getModifiers())
                        && !Modifier.isStatic(method.getModifiers())) {
                    result = method;
                }
            } catch (NoSuchMethodException ignored) {
            }
            if (result == null) {
                getLoader().log("Method not found : " + value);
            }
        }
        return result;
    }

    public Image asImage(String name) {
        Image result = null;
        String value = get(name);
        if (value != null) {
            if (value.startsWith("img:")) {
                result = ResourcesManager.getImage(value.substring(4));
            } else if (value.startsWith("svg:")) {
                result = ResourcesManager.getVector(value.substring(4));
            }
            if (result == null) {
                getLoader().log("Image not found : " + value);
            }
        }
        return result;
    }

    public String asString(String name) {
        return asString(name, null);
    }

    public String asString(String name, String def) {
        String result = def;
        String value = get(name);
        if (value != null) {
            result = value;
        }
        return result;
    }

    public float asSize(String name) {
        return asSize(name, 0F);
    }

    public float asSize(String name, float def) {
        float result = def;
        String value = get(name);
        if (value != null) {
            if ("WRAP_CONTENT".equalsIgnoreCase(value)) {
                result = Widget.WRAP_CONTENT;
            } else if ("MATCH_PARENT".equalsIgnoreCase(value)) {
                result = Widget.MATCH_PARENT;
            } else {
                result = sizeConvert(value);
                if (Float.isNaN(result)) {
                    result = def;
                    loader.log("Invalid size : [" + name + " = " + value + "]");
                }
            }
        }
        return Math.round(result);
    }

    public float asNumber(String name) {
        return asNumber(name, 0F);
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

    public boolean asBoolean(String name) {
        return asBoolean(name, false);
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

    public Font asFont(String name) {
        return asFont(name, Font.DEFAULT);
    }

    public Font asFont(String name, Font def) {
        Font result = def;
        String value = get(name);
        if (value != null) {
            result = Font.findFont(value);
        }
        return result;
    }

    // STYLE

    public int asColor(String name) {
        return asColor(name, 0);
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

    public Stroke asStroke(String name) {
        return asStroke(name, BasicStroke.line);
    }

    public Stroke asStroke(String name, Stroke def) {
        Stroke result = def;
        String value = get(name);
        if (value != null) {
            try {
                float width = 1, mitter = 10;
                int cap = 0, join = 0;
                String[] val = value.split(" ");
                if (val.length >= 1) {
                    width = sizeConvert(val[0]);
                    if (Float.isNaN(width)) {
                        loader.log("Invalid stroke : [" + value + "]");
                        return result;
                    }
                }
                if (val.length >= 2) {
                    cap = LineCap.valueOf(val[1]).ordinal();
                }
                if (val.length >= 3) {
                    join = LineJoin.valueOf(val[2]).ordinal();
                }
                if (val.length >= 4) {
                    mitter = sizeConvert(val[3]);
                    if (Float.isNaN(mitter)) {
                        loader.log("Invalid stroke : [" + value + "]");
                        return result;
                    }
                }
                result = new BasicStroke(width, cap, join, mitter);
            } catch (Exception e) {
                loader.log("Invalid stroke : [" + value + "]");
                return result;
            }
        }
        return result;
    }

    public Shape asShape(String name) {
        return asShape(name, null);
    }

    public Shape asShape(String name, Shape def) {
        Shape result = def;
        String value = get(name);
        if (value != null) {
            try {
                result = new SVGParser().parse(value, 0);
            } catch (Exception e) {
                loader.log("Invalid shape : [" + value + "]");
            }
        }
        return result;
    }

    public Rectangle asBounds(String name) {
        return asBounds(name, null);
    }

    public Rectangle asBounds(String name, Rectangle def) {
        Rectangle result = def;
        String value = get(name);
        if (value != null) {
            try {
                String[] vals = value.split(" ");
                if (vals.length == 1 && vals[0].equals("center")) {
                    result = null;
                } else if (vals.length == 2) {
                    result = new Rectangle(0, 0, Float.valueOf(vals[0]), Float.valueOf(vals[1]));
                } else if (vals.length == 4) {
                    result = new Rectangle(Float.valueOf(vals[0]), Float.valueOf(vals[1]), Float.valueOf(vals[2]), Float.valueOf(vals[3]));
                } else {
                    loader.log("Invalid bound : [" + value + "]");
                }
            } catch (Exception e) {
                loader.log("Invalid bound : [" + value + "]");
            }
        }
        return result;
    }

    private float sizeConvert(String value) {
        Dimension dimension = loader.getDimension();
        float r;
        if (value.matches("\\d+(\\.\\d+)?")) {
            r = Float.parseFloat(value) * (dimension.dpi / 160F);
        } else if (value.matches("\\d+(\\.\\d+)?dp")) {
            r = Float.parseFloat(value.substring(0, value.length() - 2)) * (dimension.dpi / 160F);
        } else if (value.matches("\\d+(\\.\\d+)?sp")) {
            r = Float.parseFloat(value.substring(0, value.length() - 2)) * (dimension.dpi / 160F) * loader.getFontScale();
        } else if (value.matches("\\d+(\\.\\d+)?px")) {
            r = Float.parseFloat(value.substring(0, value.length() - 2));
        } else if (value.matches("\\d+(\\.\\d+)?in")) {
            r = Float.parseFloat(value.substring(0, value.length() - 2)) * dimension.dpi;
        } else if (value.matches("\\d+(\\.\\d+)?pt")) {
            r = Float.parseFloat(value.substring(0, value.length() - 2)) * (dimension.dpi / 72F);
        } else if (value.matches("\\d+(\\.\\d+)?mm")) {
            r = Float.parseFloat(value.substring(0, value.length() - 2)) * (dimension.dpi / 25.4F);
        } else if (value.matches("\\d+(\\.\\d+)?cm")) {
            r = Float.parseFloat(value.substring(0, value.length() - 2)) * (dimension.dpi / 2.54F);
        } else {
            return Float.NaN;
        }
        return (float) Math.ceil(r);
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

    @SuppressWarnings("unchecked")
    public static <T> HashMap<String, T> atts(Object... dataPair) {
        HashMap hashMap = new HashMap();
        for (int i = 0; i < dataPair.length / 2; i++) {
            Object key = dataPair[i * 2];
            Object value = dataPair[i * 2 + 1];
            hashMap.put(key, value);
        }
        return (HashMap<String, T>)hashMap;
    }
}
