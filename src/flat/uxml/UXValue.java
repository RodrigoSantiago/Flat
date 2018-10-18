package flat.uxml;

import flat.Flat;
import flat.animations.Interpolation;
import flat.graphics.context.Font;
import flat.graphics.text.FontPosture;
import flat.graphics.text.FontWeight;
import flat.resources.Dimension;
import flat.resources.Parser;
import flat.resources.Resource;
import flat.resources.ResourcesManager;
import flat.widget.Widget;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class UXValue {
    public static final int string = 0;
    public static final int bool = 1;
    public static final int number = 2;
    public static final int angle = 3;
    public static final int color = 4;
    public static final int font = 5;
    public static final int resource = 6;
    public static final int constant = 7;
    public static final int listener = 8;   // - not by style

    private int type;
    private Object value;
    private String source;
    private byte sizeType;

    public UXValue(String source, boolean styleScript) {
        this.source = source;
        if (source.startsWith("\"") && source.endsWith("\"")) {
            type = string;
            if (styleScript) {
                value = this.source = Parser.string(source.substring(1, source.length() - 2));
            } else {
                value = source;
            }
        } else if (source.equalsIgnoreCase("true") || source.equalsIgnoreCase("false")) {
            type = bool;
            value = source.equalsIgnoreCase("true");
        } else if (source.matches("-?\\d+(\\.\\d+)?((px)|(dp)|(sp)|(in)|(pt)|(pc)|(mm)|(cm))?")) {
            type = number;
            value = parseNumber(source);
        } else if ("WRAP_CONTENT".equalsIgnoreCase(source)) {
            type = number;
            value = Widget.WRAP_CONTENT;
        } else if ("MATCH_PARENT".equalsIgnoreCase(source)) {
            type = number;
            value = Widget.MATCH_PARENT;
        } else if (source.matches("-?\\d+(\\.d+)?º")) {
            type = angle;
            value = Float.parseFloat(source.substring(0, source.length() - 1));
        } else if (source.matches("#[ABCDEFabcdef0-9]{6}([ABCDEFabcdef0-9]{2})?")) {
            type = color;
            if (source.length() == 7) {
                value = ((int) Long.parseLong(source.substring(1), 16) << 8) | 0x000000FF;
            } else {
                value = (int) Long.parseLong(source.substring(1), 16);
            }
        } else if (source.matches("family\\(.+\\)(\\s+weight\\(.+\\))?(\\s+posture\\(.+\\))?")) {
            type = font;
            int fIndex = source.indexOf("family(");
            int wIndex = source.indexOf("weight(");
            int pIndex = source.indexOf("posture(");

            String family = source.substring(fIndex + 7, source.indexOf(")", fIndex));

            FontWeight weight = FontWeight.NORMAL;
            if (wIndex > -1) {
                String w = source.substring(wIndex + 7, source.indexOf(")", wIndex));
                try {
                    weight = FontWeight.valueOf(w.toUpperCase());
                } catch (Exception ignored) {
                }
            }

            FontPosture posture = FontPosture.REGULAR;
            if (pIndex > -1) {
                String p = source.substring(pIndex + 8, source.indexOf(")", wIndex));
                try {
                    posture = FontPosture.valueOf(p.toUpperCase());
                } catch (Exception ignored) {
                }
            }
            Font val = Font.findFont(family, weight, posture);
            if (val == null) {
                val = Font.findFont("ROBOTO", weight, posture);
                if (val == null) {
                    val = Font.DEFAULT;
                }
            }
            value = val;
        } else if (source.matches("url\\(.+\\)")) {
            type = resource;
            value = source.substring(4, source.length() - 1).trim();
        } else {
            type = constant;
            value = source;
        }
    }

    public UXValue(int type, Object value) {
        this.type = type;
        this.value = value;
    }

    public UXValue mix(UXValue uxValue, float t, UXTheme theme) {
        if (uxValue == null) return this;
        if (t == 0) return this;
        if (t == 1) return uxValue;
        if (type != uxValue.type) return t < 0.5 ? this : uxValue;

        switch (type) {
            case string:
            case bool:
            case font:
            case resource:
            case constant:
            case listener:
                return t < 0.5 ? this : uxValue;
            case number:
                if (sizeType != 0 || uxValue.sizeType != 0) {
                    return new UXValue(number, Interpolation.mix(
                            asSize(theme) / (theme.getDimension().dpi / 160F),
                            uxValue.asSize(theme) / (theme.getDimension().dpi / 160F), t));
                } else {
                    return new UXValue(number, Interpolation.mix((float) value, (float) uxValue.value, t));
                }
            case angle:
                return new UXValue(angle, Interpolation.mixAngle((float) value, (float) uxValue.value, t));
            case color:
                return new UXValue(color, Interpolation.mixColor((int) value, (int) uxValue.value, t));
        }
        return this;
    }

    public String asString() {
        return source == null ? value.toString() : source;
    }

    public boolean asBool() {
        return (boolean) value;
    }

    public float asNumber() {
        return (float) value;
    }

    public float asSize(UXTheme theme) {
        if (theme == null || theme.getDimension() == null || sizeType == 1) {
            return (float) value;
        } else if (sizeType == 2) {
            return (float) value * (theme.getDimension().dpi / 160F) * theme.getFontScale();
        } else {
            return (float) value * (theme.getDimension().dpi / 160F);
        }
    }

    public float asSize(Dimension dimension) {
        if (dimension == null || sizeType == 1) {
            return (float) value;
        } else {
            return (float) value * (dimension.dpi / 160F);
        }
    }

    public float asAngle() {
        return (float) value;
    }

    public int asColor() {
        return (int) value;
    }

    public Font asFont() {
        return (Font) value;
    }

    public Resource asResource() {
        if (value instanceof String) {
            return ResourcesManager.getResource((String) value);
        } else {
            return (Resource) value;
        }
    }

    public <T extends Enum> T asConstant(Class<T> tClass) {
        T result = null;
        String value = (String) this.value;
        try {
            for (T each : tClass.getEnumConstants()) {
                if (each.name().compareToIgnoreCase(value) == 0) {
                    result = each;
                    break;
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    public Method asListener(Class<?> argument, Controller controller) {
        Method result = null;
        String value = (String) this.value;

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
        }
        return result;
    }

    private float parseNumber(String source) {
        float val;
        if (source.matches("-?\\d+(\\.\\d+)?")) {
            val = Float.parseFloat(source);
        } else {
            val = Float.parseFloat(source.substring(0, source.length() - 2));
        }

        if (source.endsWith("px")) {
            sizeType = 1;
        } else if (source.endsWith("sp")) {
            sizeType = 2;
        } else if (source.endsWith("in")) {
            val *= 160f;
        } else if (source.endsWith("pt")) {
            val *= 160f / 72f;
        } else if (source.endsWith("pc")) {
            val *= 160f / 6f;
        } else if (source.endsWith("mm")) {
            val *= 160f / 25.4f;
        } else if (source.endsWith("cm")) {
            val *= 160f / 2.54f;
        }
        return val;
    }
}
