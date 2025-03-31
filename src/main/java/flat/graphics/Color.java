package flat.graphics;

import flat.math.Vector4;

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
    public static final int transparent = 0x00000000;

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

    public static Vector4 toFloat(int rgba) {
        float red = ((rgba >> 24) & 0xFF) / 255.0f;
        float green = ((rgba >> 16) & 0xFF) / 255.0f;
        float blue = ((rgba >> 8) & 0xFF) / 255.0f;
        float alpha = (rgba & 0xFF) / 255f;

        return new Vector4(red, green, blue, alpha);
    }

    public static float getHue(int rgba) {
        float red = ((rgba >> 24) & 0xFF) / 255.0f;
        float green = ((rgba >> 16) & 0xFF) / 255.0f;
        float blue = ((rgba >> 8) & 0xFF) / 255.0f;

        float max = Math.max(red, Math.max(green, blue));
        float min = Math.min(red, Math.min(green, blue));
        float delta = max - min;

        float hue = 0f;

        if (delta != 0) {
            if (max == red) {
                hue = (green - blue) / delta + (green < blue ? 6 : 0);
            } else if (max == green) {
                hue = (blue - red) / delta + 2;
            } else {
                hue = (red - green) / delta + 4;
            }
            hue /= 6;
        }

        return hue;
    }

    public static float getSaturation(int rgba) {
        float red = ((rgba >> 24) & 0xFF) / 255.0f;
        float green = ((rgba >> 16) & 0xFF) / 255.0f;
        float blue = ((rgba >> 8) & 0xFF) / 255.0f;

        float max = Math.max(red, Math.max(green, blue));
        float min = Math.min(red, Math.min(green, blue));
        float delta = max - min;

        return max == 0 ? 0 : delta / max;
    }

    public static float getValue(int rgba) {
        float red = ((rgba >> 24) & 0xFF) / 255.0f;
        float green = ((rgba >> 16) & 0xFF) / 255.0f;
        float blue = ((rgba >> 8) & 0xFF) / 255.0f;

        return Math.max(red, Math.max(green, blue));
    }

    public static float[] rgbToHsv(int rgba) {
        float[] data = new float[3];
        rgbToHsv(rgba, data);
        return data;
    }

    public static void rgbToHsv(int rgba, float[] data) {
        float red = ((rgba >> 24) & 0xFF) / 255.0f;
        float green = ((rgba >> 16) & 0xFF) / 255.0f;
        float blue = ((rgba >> 8) & 0xFF) / 255.0f;

        float max = Math.max(red, Math.max(green, blue));
        float min = Math.min(red, Math.min(green, blue));
        float delta = max - min;

        float hue = 0f;
        float saturation;
        float value = max;

        if (delta != 0) {
            if (max == red) {
                hue = (green - blue) / delta + (green < blue ? 6 : 0);
            } else if (max == green) {
                hue = (blue - red) / delta + 2;
            } else {
                hue = (red - green) / delta + 4;
            }
            hue /= 6;
        }

        saturation = max == 0 ? 0 : delta / max;

        data[0] = hue;
        data[1] = saturation;
        data[2] = value;
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

    public static int setColorAlpha(int rgba, int alpha) {
        int rInt = ((rgba >> 24) & 0xFF);
        int gInt = ((rgba >> 16) & 0xFF);
        int bInt = ((rgba >> 8) & 0xFF);
        return rgbaToColor(rInt, gInt, bInt, alpha);
    }

    public static int setColorOpacity(int rgba, float alpha) {
        int rInt = ((rgba >> 24) & 0xFF);
        int gInt = ((rgba >> 16) & 0xFF);
        int bInt = ((rgba >> 8) & 0xFF);
        int aInt = Math.round(Math.max(0, Math.min(1, alpha)) * 255);
        return rgbaToColor(rInt, gInt, bInt, aInt);
    }

    public static int multiply(int rgba1, int rgba2) {
        float rFlt1 = ((rgba1 >> 24) & 0xFF) / 255f;
        float gFlt1 = ((rgba1 >> 16) & 0xFF) / 255f;
        float bFlt1 = ((rgba1 >> 8) & 0xFF) / 255f;
        float aFlt1 = (rgba1 & 0xFF) / 255f;

        float rFlt2 = ((rgba2 >> 24) & 0xFF) / 255f * rFlt1;
        float gFlt2 = ((rgba2 >> 16) & 0xFF) / 255f * gFlt1;
        float bFlt2 = ((rgba2 >> 8) & 0xFF) / 255f * bFlt1;
        float aFlt2 = (rgba2 & 0xFF) / 255f * aFlt1;

        int rInt = Math.round(rFlt2 * 255);
        int gInt = Math.round(gFlt2 * 255);
        int bInt = Math.round(bFlt2 * 255);
        int aInt = Math.round(aFlt2 * 255);
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

    public static int mix(int rgba1, int rgba2, float t) {

        int r1 = (rgba1 >> 24) & 0xFF;
        int g1 = (rgba1 >> 16) & 0xFF;
        int b1 = (rgba1 >> 8) & 0xFF;
        int a1 = rgba1 & 0xFF;

        int r2 = (rgba2 >> 24) & 0xFF;
        int g2 = (rgba2 >> 16) & 0xFF;
        int b2 = (rgba2 >> 8) & 0xFF;
        int a2 = rgba2 & 0xFF;

        int r3 = Math.round(r1 + t * (r2 - r1));
        int g3 = Math.round(g1 + t * (g2 - g1));
        int b3 = Math.round(b1 + t * (b2 - b1));
        int a3 = Math.round(a1 + t * (a2 - a1));

        return rgbaToColor(r3, g3, b3, a3);
    }

    public static int mixHSV(int rgba1, int rgba2, float t) {
        if (t <= 0) return rgba1;
        if (t >= 1) return rgba2;
        if (rgba1 == rgba2) return rgba1;
        if (rgba1 == 0) return multiplyColorAlpha(rgba2, t);
        if (rgba2 == 0) return multiplyColorAlpha(rgba1, 1 - t);

        int r1 = (rgba1 >> 24) & 0xFF;
        int g1 = (rgba1 >> 16) & 0xFF;
        int b1 = (rgba1 >> 8) & 0xFF;
        int a1 = rgba1 & 0xFF;

        int r2 = (rgba2 >> 24) & 0xFF;
        int g2 = (rgba2 >> 16) & 0xFF;
        int b2 = (rgba2 >> 8) & 0xFF;
        int a2 = rgba2 & 0xFF;

        // HSV 1
        float fr1 = r1 / 255.0f;
        float fg1 = g1 / 255.0f;
        float fb1 = b1 / 255.0f;

        float v1 = Math.max(fr1, Math.max(fg1, fb1));
        float d1 = v1 - Math.min(fr1, Math.min(fg1, fb1));
        float h1 = 0f;
        float s1 = v1 == 0 ? 0 : d1 / v1;
        if (d1 != 0) {
            if (v1 == fr1) {
                h1 = (fg1 - fb1) / d1 + (fg1 < fb1 ? 6 : 0);
            } else if (v1 == fg1) {
                h1 = (fb1 - fr1) / d1 + 2;
            } else {
                h1 = (fr1 - fg1) / d1 + 4;
            }
            h1 /= 6;
        }

        // HSV 2
        float fr2 = r2 / 255.0f;
        float fg2 = g2 / 255.0f;
        float fb2 = b2 / 255.0f;

        float v2 = Math.max(fr2, Math.max(fg2, fb2));
        float d2 = v2 - Math.min(fr2, Math.min(fg2, fb2));
        float h2 = 0f;
        float s2 = v2 == 0 ? 0 : d2 / v2;
        if (d2 != 0) {
            if (v2 == fr2) {
                h2 = (fg2 - fb2) / d2 + (fg2 < fb2 ? 6 : 0);
            } else if (v2 == fg2) {
                h2 = (fb2 - fr2) / d2 + 2;
            } else {
                h2 = (fr2 - fg2) / d2 + 4;
            }
            h2 /= 6;
        }

        float h = v1 == 0 || s1 == 0 ? h2 : v2 == 0 || s2 == 0 ? h1 : interpolateHue(h1, h2, t);
        float s = s1 + t * (s2 - s1);
        float v = v1 + t * (v2 - v1);
        float a = (a1 / 255.0f) + t * (a2 - a1) / 255.0f;
        return hsvaToColor(h, s, v, a);
    }

    private static float interpolateHue(float h1, float h2, float t) {
        float delta = h2 - h1;
        if (delta < -0.5f) {
            delta += 1.0f;
        } else if (delta > 0.5f) {
            delta -= 1.0f;
        }

        float interpolated = (h1 + t * delta) % 1;
        if (interpolated < 0) {
            interpolated += 1;
        }
        return interpolated;
    }
}
