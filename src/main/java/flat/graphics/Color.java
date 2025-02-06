package flat.graphics;

public final class Color {
    public static final int black = 0x000000FF;
    public static final int silver = 0xC0C0C0FF;
    public static final int gray = 0x808080FF;
    public static final int white = 0xFFFFFFFF;
    public static final int maroon = 0x800000FF;
    public static final int red = 0xFF0000FF;
    public static final int purple = 0x800080FF;
    public static final int fuchsia = 0xFF00FFFF;
    public static final int green = 0x008000FF;
    public static final int lime = 0x00FF00FF;
    public static final int olive = 0x808000FF;
    public static final int yellow = 0xFFFF00FF;
    public static final int navy = 0x000080FF;
    public static final int blue = 0x0000FFFF;
    public static final int teal = 0x008080FF;
    public static final int aqua = 0x00FFFFFF;

    public static int getRed(int rgba) {
        return ((rgba >> 24) & 0xFF);
    }

    public static int getGreen(int rgba) {
        return ((rgba >> 16) & 0xFF);
    }

    public static int getBlue(int rgba) {
        return ((rgba >> 8) & 0xFF);
    }

    public static int getAlpha(int rgba) {
        return (rgba & 0xFF);
    }

    public static float getOpacity(int rgba) {
        return (rgba & 0xFF) / 255f;
    }

    public static int rgbToColor(int red, int green, int blue) {
        return rgbaToColor(red, green, blue, 255);
    }

    public static int rgbaToColor(int red, int green, int blue, int alpha) {
        return ((red & 0xFF) << 24) | ((green & 0xFF) << 16) | ((blue & 0xFF) << 8) | (alpha & 0xFF);
    }

    public static int hsvToColor(float hue, float saturation, float value) {
        return hsvaToColor(hue, saturation, value, 1f);
    }

    public static int hsvaToColor(float hue, float saturation, float value, float alpha) {
        float h = Math.max(0, Math.min(1, hue));
        float s = Math.max(0, Math.min(1, saturation));
        float v = Math.max(0, Math.min(1, value));
        alpha = Math.max(0, Math.min(1, alpha));

        int i = (int) (h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);

        float r, g, b;
        switch (i % 6) {
            case 0: r = v; g = t; b = p; break;
            case 1: r = q; g = v; b = p; break;
            case 2: r = p; g = v; b = t; break;
            case 3: r = p; g = q; b = v; break;
            case 4: r = t; g = p; b = v; break;
            case 5: r = v; g = p; b = q; break;
            default: r = 0; g = 0; b = 0; break;
        }

        int rInt = Math.round(r * 255);
        int gInt = Math.round(g * 255);
        int bInt = Math.round(b * 255);
        int aInt = Math.round(alpha * 255);

        return rgbaToColor(rInt, gInt, bInt, aInt);
    }

    public static int setColorRed(int rgba, int red) {
        int gInt = ((rgba >> 16) & 0xFF);
        int bInt = ((rgba >> 8) & 0xFF);
        int aInt = (rgba & 0xFF);
        return rgbaToColor(red, gInt, bInt, aInt);
    }

    public static int setColorGreen(int rgba, int green) {
        int rInt = ((rgba >> 24) & 0xFF);
        int bInt = ((rgba >> 8) & 0xFF);
        int aInt = (rgba & 0xFF);
        return rgbaToColor(rInt, green, bInt, aInt);
    }

    public static int setColorBlue(int rgba, int blue) {
        int rInt = ((rgba >> 24) & 0xFF);
        int gInt = ((rgba >> 16) & 0xFF);
        int aInt = (rgba & 0xFF);
        return rgbaToColor(rInt, gInt, blue, aInt);
    }

    public static int setColorAlpha(int rgba, float alpha) {
        int rInt = ((rgba >> 24) & 0xFF);
        int gInt = ((rgba >> 16) & 0xFF);
        int bInt = ((rgba >> 8) & 0xFF);
        int aInt = Math.round(Math.max(0, Math.min(1, alpha)) * 255);
        return rgbaToColor(rInt, gInt, bInt, aInt);
    }

    public static int multiplyColorAlpha(int rgba, float opacity) {
        int rInt = ((rgba >> 24) & 0xFF);
        int gInt = ((rgba >> 16) & 0xFF);
        int bInt = ((rgba >> 8) & 0xFF);
        float aFlt = (rgba & 0xFF) / 255f * opacity;
        int aInt = Math.round(Math.max(0, Math.min(1, aFlt)) * 255);
        return rgbaToColor(rInt, gInt, bInt, aInt);
    }
}
