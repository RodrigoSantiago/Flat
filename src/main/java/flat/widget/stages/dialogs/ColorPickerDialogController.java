package flat.widget.stages.dialogs;

import flat.Flat;
import flat.events.*;
import flat.graphics.Color;
import flat.graphics.Surface;
import flat.graphics.context.ShaderProgram;
import flat.graphics.context.enums.PixelFormat;
import flat.graphics.image.PixelMap;
import flat.math.Mathf;
import flat.math.Vector2;
import flat.math.Vector4;
import flat.math.shapes.Rectangle;
import flat.math.stroke.BasicStroke;
import flat.resources.Dimension;
import flat.uxml.UXListener;
import flat.uxml.UXWidgetValueListener;
import flat.widget.Parent;
import flat.widget.enums.Visibility;
import flat.widget.image.Canvas;
import flat.widget.image.ImageView;
import flat.widget.stages.Dialog;
import flat.widget.text.Button;
import flat.widget.text.TextArea;
import flat.widget.value.Slider;

public class ColorPickerDialogController extends DefaultDialogController {

    private static ThreadLocal<ShaderProgram> paletteShader = new ThreadLocal<>();

    private float paletteH = 0;
    private float paletteS = 0;
    private float paletteV = 0;
    private int color = 0x000000FF;
    private int palettePointer;

    private float radius = 80f;
    private float radiusIn = 72f;
    private float radiusSize = 12f;

    // --------------
    private final boolean artistic;
    private final boolean cancelable;
    private final boolean alpha;
    private final int[] palette;
    private final int initialColor;
    private final UXListener<Dialog> onShowListener;
    private final UXListener<Dialog> onHideListener;
    private final UXWidgetValueListener<Dialog, Integer> onColorPickListener;

    private Surface surface;
    private PixelMap preview;

    ColorPickerDialogController(Dialog dialog, ColorPickerDialogBuilder builder) {
        super(dialog);
        this.cancelable = builder.cancelable;
        this.initialColor = builder.initialColor;
        this.onShowListener = builder.onShowListener;
        this.onHideListener = builder.onHideListener;
        this.onColorPickListener = builder.onColorPickListener;
        this.artistic = builder.artistic;
        this.alpha = builder.alpha;
        this.palette = builder.palette;
    }

    @Flat
    public Button cancelButton;

    @Flat
    public ImageView palettePreview;

    @Flat
    public Parent paletteBtns;
    @Flat
    public Parent paletteAlpha;

    @Flat
    public Button btnCol0, btnCol1, btnCol2, btnCol3, btnCol4, btnCol5, btnCol6, btnCol7;
    private Button[] btns;

    @Flat
    public void hide() {
        dialog.smoothHide();
    }

    @Flat
    public void onChoose(ActionEvent event) {
        if (onColorPickListener != null) {
            UXWidgetValueListener.safeHandle(onColorPickListener, dialog, Color.setColorAlpha(color, getAlpha()));
        }
        dialog.smoothHide();
    }

    @Flat
    public void onCancel(ActionEvent event) {
        dialog.smoothHide();
    }

    @Override
    public void onShow() {
        btns = new Button[]{btnCol0, btnCol1, btnCol2, btnCol3, btnCol4, btnCol5, btnCol6, btnCol7};
        int w = Mathf.round(paletteCanvas.getWidth());
        int h = Mathf.round(paletteCanvas.getHeight());
        surface = new Surface(w, h, 8);
        preview = new PixelMap(new byte[w * h * 4], w, h, PixelFormat.RGBA);
        palettePreview.setImage(preview);
        sliderAlpha.setValue(255);
        fieldAlpha.setText("255");
        paletteAlpha.setVisibility(alpha ? Visibility.VISIBLE : Visibility.GONE);
        paletteBtns.setVisibility(palette != null ? Visibility.VISIBLE : Visibility.GONE);
        if (palette != null) {
            for (int i = 0; i < palette.length; i++) {
                btns[i].setFollowStyleProperty("icon-color", false);
                btns[i].setIconColor(palette[i]);
                int finalI = i;
                btns[i].setActionListener(event -> setCurrentColor(palette[finalI]));
            }
        }
        setCurrentColor(initialColor);
        if (!cancelable && cancelButton != null) {
            cancelButton.setVisibility(Visibility.GONE);
        }
        UXListener.safeHandle(onShowListener, dialog);
    }

    @Override
    public void onHide() {
        UXListener.safeHandle(onHideListener, dialog);
    }

    @Flat
    public Canvas paletteCanvas;
    @Flat
    public ImageView paletteAlphaColor;
    @Flat
    public Slider sliderRed;
    @Flat
    public Slider sliderGreen;
    @Flat
    public Slider sliderBlue;
    @Flat
    public Slider sliderAlpha;
    @Flat
    public TextArea fieldRed;
    @Flat
    public TextArea fieldGreen;
    @Flat
    public TextArea fieldBlue;
    @Flat
    public TextArea fieldAlpha;

    @Flat
    public void onSlideRed(SlideEvent event) {
        setColorFromSlider();
    }

    @Flat
    public void onSlideGreen(SlideEvent event) {
        setColorFromSlider();
    }

    @Flat
    public void onSlideBlue(SlideEvent event) {
        setColorFromSlider();
    }

    @Flat
    public void onSlideAlpha(SlideEvent event) {
        fieldAlpha.setText(Mathf.round(sliderAlpha.getValue())+"");
    }

    @Flat
    public void onFieldFilter(TextEvent event) {
        event.setText(event.getText().replaceAll("\\D", ""));
    }

    @Flat
    public void onTypeRed(TextEvent event) {
        setColorFromField();
    }

    @Flat
    public void onTypeBlue(TextEvent event) {
        setColorFromField();
    }

    @Flat
    public void onTypeGreen(TextEvent event) {
        setColorFromField();
    }

    @Flat
    public void onFieldLostFocus(FocusEvent event) {
        if (event.getType() == FocusEvent.LOST) {
            setColorFromSlider();
        }
    }

    @Flat
    public void onTypeAlpha(TextEvent event) {
        sliderAlpha.setValue(getNumber(fieldAlpha));
    }

    @Flat
    public void onFieldAlphaLostFocus(FocusEvent event) {
        if (event.getType() == FocusEvent.LOST) {
            fieldAlpha.setText(Mathf.round(sliderAlpha.getValue())+"");
        }
    }

    @Flat
    public TextArea fieldHex;

    @Flat
    public void onFieldHexFilter(TextEvent event) {
        event.setText(event.getText().replaceAll("[^0-9ABCDEFabcdef]", "").toUpperCase());
    }

    @Flat
    public void onTypeHex(TextEvent event) {
        setColorFromHex();
    }

    @Flat
    public void onFieldHexLostFocus(FocusEvent event) {
        if (event.getType() == FocusEvent.LOST) {
            setCurrentColor(getCurrentColor());
        }
    }

    public int getAlpha() {
        return Mathf.round(sliderAlpha.getValue());
    }

    public int getCurrentColor() {
        return color;
    }

    private int getNumber(TextArea textArea) {
        try {
            return Math.min(255, Integer.parseInt(textArea.getText()));
        } catch (Exception e) {
            return 0;
        }
    }

    private void setHex() {
        StringBuilder hex = new StringBuilder(Integer.toHexString((color >> 8) & 0xFFFFFF));
        while (hex.length() < 6) {
            hex.insert(0, "0");
        }
        fieldHex.setText(hex.toString());
    }

    private void setField() {
        fieldRed.setText(Color.getRed(color)+"");
        fieldGreen.setText(Color.getGreen(color)+"");
        fieldBlue.setText(Color.getBlue(color)+"");
    }

    private void setSlider() {
        sliderRed.setValue(Color.getRed(color));
        sliderGreen.setValue(Color.getGreen(color));
        sliderBlue.setValue(Color.getBlue(color));
    }

    private void setPalette() {
        float h = Color.getHue(color);
        float s = Color.getSaturation(color);
        paletteV = Color.getValue(color);
        if (paletteV > 0.025f) {
            paletteS = s;
            if (paletteS > 0.025f || paletteV < 0.975f) {
                paletteH = unshiftHue(h);
            }
        }
    }
    
    private void updateColor() {
        if (paletteShader.get() == null) {
            paletteShader.set(getGraphics().createImageRenderShader("""
                #version 330 core
                uniform float radius;
                uniform float radiusIn;
                uniform float size;
                uniform float hue;
                uniform vec2 center;
                uniform vec4 colors[6];
                
                vec3 hsv2rgb(vec3 c) {
                    vec4 K = vec4(1.0, 2.0/3.0, 1.0/3.0, 3.0);
                    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
                    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
                }
                
                vec4 fragment(vec2 pos, vec2 uv) {
                    vec2 dir = pos - center;
                    float len = length(dir);

                    float angle = atan(dir.y, -dir.x);
                    float t = (angle + 3.14159265 + 0.5235) / (2.0 * 3.14159265);
                    float scaled = t * 6.0;
                    int i0 = int(floor(scaled)) % 6;
                    int i1 = (i0 + 1) % 6;
                    float f = fract(scaled);
                    vec3 mixed = mix(colors[i0], colors[i1], f).rgb;

                    vec2 sv = (pos - (center - radiusIn / 1.4142)) / (radiusIn * 1.4142);
                    vec3 rgb = hsv2rgb(vec3(hue, sv.x, 1 - sv.y));
                    float d = max(abs(pos.x - center.x), abs(pos.y - center.y)) - radiusIn / 1.4142;
                    float alpha = 1.0 - smoothstep(0.0, 1.0, d);
               
                    vec4 tri = vec4(rgb, alpha);
                    vec4 whe = vec4(mixed,
                        1 - clamp(0, 1, abs(len - radius - size) - (size - 2))
                    );
                    return len < radius ? tri : whe;
                }
                """));
        }
        var shader = paletteShader.get();

        var graphics = getGraphics();
        graphics.setSurface(surface);
        graphics.clear(0, 0, 0);
        graphics.setAntialiasEnabled(true);
        shader.set("radius", radius);
        shader.set("radiusIn", radiusIn);
        shader.set("size", radiusSize);
        shader.set("center", new Vector2(preview.getWidth() / 2f, preview.getHeight() / 2f));
        shader.set("hue", shiftHue(paletteH));
        shader.set("colors", artistic ? colorWheelRYB : colorWheelRGB);
        graphics.blitCustomShader(shader);
        graphics.renderToTexture(preview.getTexture());
        graphics.setSurface(null);
        palettePreview.repaint();
    }

    public void setCurrentColor(int color) {
        this.color = color;
        setSlider();
        setField();
        setPalette();
        setHex();
        updateColor();
    }

    private void setColorFromSlider() {
        color = Color.rgbToColor(
                Mathf.round(sliderRed.getValue()),
                Mathf.round(sliderGreen.getValue()),
                Mathf.round(sliderBlue.getValue()));
        setField();
        setPalette();
        setHex();
        updateColor();
    }

    private void setColorFromField() {
        color = Color.rgbToColor(getNumber(fieldRed), getNumber(fieldGreen), getNumber(fieldBlue));
        setSlider();
        setPalette();
        setHex();
        updateColor();
    }

    private void setColorFromPalette() {
        color = Color.hsvToColor(shiftHue(paletteH), paletteS, paletteV);
        setSlider();
        setField();
        setHex();
        updateColor();
    }

    private void setColorFromHex() {
        try {
            color = ((int) Long.parseLong(fieldHex.getText(), 16) << 8) | 0xFF;
        } catch (Exception ignored) {
        }
        setSlider();
        setField();
        setPalette();
        updateColor();
    }

    private Vector2 fromPointerToSV(float px, float py) {
        Rectangle box = new Rectangle(
                paletteCanvas.getInX(), paletteCanvas.getInY(),
                paletteCanvas.getInWidth(), paletteCanvas.getInHeight());
        float r2 = getRadiusIn() / 1.4142f;
        float cx = box.x + box.width * 0.5f - r2;
        float cy = box.y + box.height * 0.5f - r2;
        return new Vector2(Mathf.clamp((px - cx) / (r2 * 2), 0, 1), 1 - Mathf.clamp((py - cy) / (r2 * 2), 0, 1));
    }

    private Vector2 fromSVToPointer(float vValue, float sValue) {
        Rectangle box = new Rectangle(
                paletteCanvas.getInX(), paletteCanvas.getInY(),
                paletteCanvas.getInWidth(), paletteCanvas.getInHeight());
        float r2 = getRadiusIn() / 1.4142f;
        float cx = box.x + box.width * 0.5f - r2;
        float cy = box.y + box.height * 0.5f - r2;
        return new Vector2(cx + (vValue) * r2 * 2, cy + (1 - sValue) * r2 * 2);
    }

    @Flat
    public void onPaletteDraw(DrawEvent drawEvent) {
        var graphics = drawEvent.getGraphics(); 
        var box = drawEvent.getInBox();
        var start = graphics.getTransform2D().transform(new Vector2(box.x, box.y));
        var center = graphics.getTransform2D().transform(new Vector2(box.x + box.width / 2, box.y + box.height / 2));
        int solidColor = Color.setColorAlpha(getCurrentColor(), 255);

        Vector2 pos = fromSVToPointer(paletteS, paletteV);
        float dp_2 = Dimension.dpPx(getActivity().getDensity(), 2);
        graphics.setColor(solidColor);
        graphics.drawCircle(pos.x, pos.y, dp_2 * 3, true);
        graphics.setStroke(new BasicStroke(dp_2 + 2));
        graphics.setColor(Color.rgbaToColor(0, 0, 0, 80));
        graphics.drawCircle(pos.x, pos.y, dp_2 * 3, false);
        graphics.setStroke(new BasicStroke(dp_2));
        graphics.setColor(Color.white);
        graphics.drawCircle(pos.x, pos.y, dp_2 * 3, false);

        Vector2 rCenter = new Vector2(box.x + box.width * 0.5f, box.y + box.height * 0.5f);
        Vector2 pos2 = rCenter.add(
                new Vector2(Mathf.cos(Mathf.toRadians(paletteH * 360 - 30)),
                        -Mathf.sin(Mathf.toRadians(paletteH * 360 - 30))).mul(getRadius() + getRadiusSize()));

        graphics.setStroke(new BasicStroke(dp_2 + 1));
        graphics.setColor(Color.rgbaToColor(0, 0, 0, 80));
        graphics.drawCircle(pos2.x, pos2.y, dp_2 * 4, false);
        graphics.setStroke(new BasicStroke(dp_2));
        graphics.setColor(Color.white);
        graphics.drawCircle(pos2.x, pos2.y, dp_2 * 4, false);

        paletteAlphaColor.setColor(solidColor);
        sliderAlpha.setFollowStyleProperty("icon-color", false);
        sliderAlpha.setIconColor(Color.getLuminescence(solidColor) < 0.40 ? Color.white : Color.black);
    }

    @Flat
    public void onPalettePointer(PointerEvent event) {
        Rectangle box = new Rectangle(
                paletteCanvas.getInX(), paletteCanvas.getInY(),
                paletteCanvas.getInWidth(), paletteCanvas.getInHeight());

        Vector2 center = new Vector2(box.x + box.width * 0.5f, box.y + box.height * 0.5f);

        if (event.getPointerID() == 1 && event.getType() == PointerEvent.PRESSED) {
            float d = paletteCanvas.screenToLocal(event.getX(), event.getY()).distance(center);
            if (d < getRadius()) {
                palettePointer = 1;
            } else if (d < getRadius() + getRadiusSize() * 2) {
                palettePointer = 2;
            } else {
                palettePointer = 0;
            }
        }
        if (event.getPointerID() == 1 && event.getType() == PointerEvent.DRAGGED || event.getType() == PointerEvent.PRESSED) {
            Vector2 p = paletteCanvas.screenToLocal(event.getX(), event.getY());
            if (palettePointer == 1) {
                var sv = fromPointerToSV(p.x, p.y);
                paletteS = sv.x;
                paletteV = sv.y;
                setColorFromPalette();
            } else if (palettePointer == 2) {
                paletteH = ((new Vector2(center).sub(p).angle(0, 0) + 360 + 30) % 360) / 360f;
                setColorFromPalette();
            }
        }
    }

    float[] gradient = {
            0, 0.083f, 0.166f, 0.333f, 0.666f, 0.833f, 1
    };

    float[] gradientStandard = {
            0, 0.166f, 0.333f, 0.500f, 0.666f, 0.833f, 1
    };

    Vector4[] colorWheelRGB = {
            new Vector4(1.00f, 0.15f, 0.07f, 1), new Vector4(0.99f, 0.90f, 0.19f, 1),
            new Vector4(0.39f, 0.68f, 0.19f, 1), new Vector4(0.39f, 0.68f, 1.00f, 1),
            new Vector4(0.01f, 0.27f, 1.00f, 1), new Vector4(0.52f, 0.00f, 0.68f, 1)
    };

    Vector4[] colorWheelRYB = {
            new Vector4(1.00f, 0.15f, 0.07f, 1), new Vector4(0.98f, 0.59f, 0.01f, 1),
            new Vector4(0.99f, 0.90f, 0.19f, 1), new Vector4(0.39f, 0.68f, 0.19f, 1),
            new Vector4(0.01f, 0.27f, 1.00f, 1), new Vector4(0.52f, 0.00f, 0.68f, 1)
    };

    public float shiftHue(float f) {
        if (!artistic) return f;

        float scaled = f * 6.0f;
        int i = (int) Math.floor(scaled);
        int j = (i + 1) % 7;
        float t = scaled - i;

        return (1 - t) * gradient[i] + t * gradient[j];
    }

    public float unshiftHue(float value) {
        if (!artistic) return value;

        for (int i = 0; i < gradient.length - 1; i++) {
            float g1 = gradient[i];
            float g2 = gradient[i + 1];

            if (value >= g1 && value <= g2) {
                float t = (value - g1) / (g2 - g1);
                float scaled = i + t;
                return scaled / 6.0f;
            }
        }
        return value;
    }

    private float getRadius() {
        return radius * (palettePreview.getInWidth() / preview.getWidth());
    }

    private float getRadiusIn() {
        return radiusIn * (palettePreview.getInWidth() / preview.getWidth());
    }

    private float getRadiusSize() {
        return radiusSize * (palettePreview.getInWidth() / preview.getWidth());
    }
}
