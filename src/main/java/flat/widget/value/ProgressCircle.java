package flat.widget.value;

import flat.animations.StateInfo;
import flat.graphics.SmartContext;
import flat.math.Mathf;
import flat.math.shapes.Arc;
import flat.math.stroke.BasicStroke;
import flat.uxml.Controller;
import flat.uxml.UXStyle;
import flat.uxml.UXStyleAttrs;
import flat.widget.Widget;

public class ProgressCircle extends Widget {

    private float progress;
    private int color0;
    private int color1;
    private int color2;
    private int color3;
    private float indicatorSize;
    private float animationDuration;
    private float anim;
    private long time;
    private Arc arc = new Arc(Arc.Type.OPEN);

    @Override
    public void applyAttributes(UXStyleAttrs style, Controller controller) {
        super.applyAttributes(style, controller);

        setProgress(style.asNumber("progress", getProgress()));
        setAnimationDuration(style.asNumber("animation-duration", getAnimationDuration()));
        if (style.contains("color")) {
            setColor(style.asColor("color", getColor0()));
        }
    }

    @Override
    public void applyStyle() {
        super.applyStyle();
        UXStyle style = getStyle();
        if (style == null) return;

        StateInfo info = getStateInfo();

        setColor0(style.asColor("color-0", info, getColor0()));
        setColor1(style.asColor("color-1", info, getColor1()));
        setColor2(style.asColor("color-2", info, getColor2()));
        setColor3(style.asColor("color-3", info, getColor3()));

        setIndicatorSize(style.asSize("indicator-size", info, getIndicatorSize()));
    }

    @Override
    public void onDraw(SmartContext context) {
        backgroundDraw(0, getBorderColor(), getRippleColor(), context);

        context.setTransform2D(getTransform());
        float x = getInX();
        float y = getInY();
        float w = getInWidth();
        float h = getInHeight();

        context.setStroker(new BasicStroke(indicatorSize));
        context.setColor(getBackgroundColor());
        context.drawEllipse(x + 2, y + 2, w - 4, h - 4, false);

        if (progress >= 0) {
            arc.set(x + 2, y + 2, w - 4, h - 4, 90, 0, Arc.Type.OPEN);
            context.setColor(color0);
            context.drawShape(arc, false);
        } else if (animationDuration > 0) {
            long t = System.currentTimeMillis();
            long pass = t - time;
            time = t;

            anim += pass / Math.abs(animationDuration);
            anim = Mathf.clamp(anim, 0, 1);
            float p = anim;
            float p0 = p < 0.125f ? p * 6 :
                    p < 0.250f ? 0.75f :
                            p < 0.375f ? (p - 0.125f) * 6 :
                                    p < 0.500f ? 1.5f :
                                            p < 0.625f ? (p - 0.250f) * 6 :
                                                    p < 0.750f ? 2.25f :
                                                            p < 0.875f ? (p - 0.375f) * 6 : 3.00f;
            float p1 = p < 0.125f ? 0 :
                    p < 0.250f ? (p - 0.125f) * 6 :
                            p < 0.375f ? 0.75f :
                                    p < 0.500f ? (p - 0.250f) * 6 :
                                            p < 0.625f ? 1.5f :
                                                    p < 0.750f ? (p - 0.375f) * 6 :
                                                            p < 0.875f ? 2.25f : (p - 0.500f) * 6;

            context.setColor(p < 0.25 ? color0 : p < 0.5 ? color1 : p < 0.75 ? color2 : color3);
            arc.set(x + 2, y + 2, w - 4, h - 4, -((anim * 2160) + (p1 * 360)), ((p1 - p0) * 360), Arc.Type.OPEN);
            context.drawShape(arc, false);

            if (anim == 1) {
                anim = 0;
            }

            invalidate(false);
        }
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        if (progress < 0) progress = -1;
        if (progress > 1) progress = 1;

        if (this.progress != progress) {
            this.progress = progress;
            if (progress == -1) {
                time = System.currentTimeMillis();
            }
            anim = 0;
            invalidate(false);
        }
    }

    public void setColor(int color) {
        if (color != color0 || color != color1 || color != color2 || color != color3) {
            this.color0 = this.color1 = this.color2 = this.color3 = color;
            invalidate(false);
        }
    }

    public void setColor(int colorIn, int colorOut) {
        if (colorIn != color0 || colorIn != color1 || colorOut != color2 || colorOut != color3) {
            this.color0 = this.color1 = colorIn;
            this.color2 = this.color3 = colorOut;
            invalidate(false);
        }
    }

    public void setColor(int color0, int color1, int color2, int color3) {
        if (this.color0 != color0 || this.color1 != color1 || this.color2 != color2 || this.color3 != color3) {
            this.color0 = color0;
            this.color1 = color1;
            this.color2 = color2;
            this.color3 = color3;
            invalidate(false);
        }
    }

    public int getColor0() {
        return color0;
    }

    public void setColor0(int color0) {
        if (this.color0 != color0) {
            this.color0 = color0;
            invalidate(false);
        }
    }

    public int getColor1() {
        return color1;
    }

    public void setColor1(int color1) {
        if (this.color1 != color1) {
            this.color1 = color1;
            invalidate(false);
        }
    }

    public int getColor2() {
        return color2;
    }

    public void setColor2(int color2) {
        if (this.color2 != color2) {
            this.color2 = color2;
            invalidate(false);
        }
    }

    public int getColor3() {
        return color3;
    }

    public void setColor3(int color3) {
        if (this.color3 != color3) {
            this.color3 = color3;
            invalidate(false);
        }
    }

    public float getIndicatorSize() {
        return indicatorSize;
    }

    public void setIndicatorSize(float indicatorSize) {
        if (this.indicatorSize != indicatorSize) {
            this.indicatorSize = indicatorSize;
            invalidate(false);
        }
    }

    public float getAnimationDuration() {
        return animationDuration;
    }

    public void setAnimationDuration(float milis) {
        if (this.animationDuration != milis) {
            this.animationDuration = milis;
        }
    }
}
