package flat.widget.value;

import flat.animations.Interpolation;
import flat.animations.StateInfo;
import flat.graphics.SmartContext;
import flat.math.Mathf;
import flat.math.shapes.Shape;
import flat.uxml.Controller;
import flat.uxml.UXStyle;
import flat.uxml.UXStyleAttrs;
import flat.widget.Widget;

public class ProgressBar extends Widget {

    private float progress;
    private int color;
    private float animationDuration;
    private float anim;
    private long time;

    @Override
    public void applyAttributes(UXStyleAttrs style, Controller controller) {
        super.applyAttributes(style, controller);

        setProgress(style.asNumber("progress", getProgress()));
        setAnimationDuration(style.asNumber("animation-duration", getAnimationDuration()));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();
        UXStyle style = getStyle();
        if (style == null) return;

        StateInfo info = getStateInfo();

        setColor(style.asColor("color", info, getColor()));
    }

    @Override
    public void onDraw(SmartContext context) {

        Shape shape = null;
        boolean clip = getRadiusTop() != 0 || getRadiusRight() != 0 || getRadiusBottom() != 0 || getRadiusLeft() != 0;
        if (clip) {
            shape = backgroundClip(context);
        }
        backgroundDraw(getBackgroundColor(), getBorderColor(), getRippleColor(), context);

        context.setTransform2D(getTransform());
        float x = getInX();
        float y = getInY();
        float w = getInWidth();
        float h = getInHeight();
        if (progress >= 0) {
            context.setColor(color);
            context.drawRect(x, y, w * progress, h, true);
        } else if (animationDuration > 0) {
            long t = System.currentTimeMillis();
            long pass = t - time;
            time = t;

            anim += pass / Math.abs(animationDuration);
            anim = Mathf.clamp(anim, 0, 1);

            float p = anim;
            float p0 = Math.max(0, p * 2);
            float p1 = Math.max(0, Interpolation.quadIn.apply(Mathf.clamp((p - 0.20f) * 2, 0, 1)));
            float p2 = Math.max(0, (p - 0.60f) * 4);
            float p3 = Math.max(0, Interpolation.quadOut.apply(Mathf.clamp((p - 0.75f) * 4, 0, 1)));

            context.setColor(color);
            context.drawRect(x + p1 * w, y, Math.min(w - p1 * w, w * (p0 - p1)), h, true);
            context.drawRect(x + p3 * w, y, Math.min(w - p3 * w, w * (p2 - p3)), h, true);

            if (anim == 1) {
                anim = 0;
            }

            invalidate(false);
        }

        if (clip) {
            context.setTransform2D(null);
            context.setClip(shape);
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

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        if (this.color != color) {
            this.color = color;
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
