package flat.widget.image;

import flat.graphics.SmartContext;
import flat.graphics.image.Drawable;
import flat.graphics.text.Align;
import flat.uxml.Controller;
import flat.uxml.UXAttributes;
import flat.widget.Widget;

import java.util.HashMap;

public class ImageView extends Widget {

    public static final int NOONE = 0;
    public static final int STRETCH = 1;
    public static final int FIT = 2;
    public static final int CROP = 3;

    protected static final HashMap<String, Integer> scaleTypes = UXAttributes.atts(
            "NOONE", NOONE,
            "STRETCH", STRETCH,
            "FIT", FIT,
            "CROP", CROP
    );

    private Drawable drawable;
    private int scaleType;

    private float frame;
    private float speed;
    private long lastTime;

    private Align.Vertical verticalAlign;
    private Align.Horizontal horizontalAlign;

    @Override
    public void applyAttributes(Controller controller, UXAttributes attributes) {
        super.applyAttributes(controller, attributes);

        setScaleType(attributes.asConstant("scaleType", scaleTypes, NOONE));
        setVerticalAlign(attributes.asEnum("verticalAlign", Align.Vertical.class, Align.Vertical.TOP));
        setHorizontalAlign(attributes.asEnum("horizontalAlign", Align.Horizontal.class, Align.Horizontal.LEFT));

        setFrame(attributes.asNumber("frame", 0));
        setSpeed(attributes.asNumber("speed", 1));
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
        invalidate(false);
    }

    public void setDrawable(Drawable drawable, float speed) {
        this.drawable = drawable;
        this.speed = speed;
        invalidate(false);
    }

    public float getFrame() {
        return frame;
    }

    public void setFrame(float frame) {
        this.frame = Math.max(Math.min(1, frame), 0);
        lastTime = 0;
        invalidate(false);
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
        invalidate(false);
    }

    public void redraw() {
        invalidate(false);
    }

    public int getScaleType() {
        return scaleType;
    }

    public void setScaleType(int scaleType) {
        this.scaleType = scaleType;
    }

    public Align.Vertical getVerticalAlign() {
        return verticalAlign;
    }

    public void setVerticalAlign(Align.Vertical verticalAlign) {
        if (this.verticalAlign != verticalAlign) {
            this.verticalAlign = verticalAlign;
            invalidate(true);
        }
    }

    public Align.Horizontal getHorizontalAlign() {
        return horizontalAlign;
    }

    public void setHorizontalAlign(Align.Horizontal horizontalAlign) {
        if (this.horizontalAlign != horizontalAlign) {
            this.horizontalAlign = horizontalAlign;
            invalidate(true);
        }
    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);

        long now = System.currentTimeMillis();
        if (lastTime != 0) {
            frame += ((now - lastTime) / 1000f) * speed;
        }
        lastTime = now;
        frame = Math.abs(frame % 1f);

        if (this.drawable != null) {
            float dW = drawable.getWidth();
            float dH = drawable.getHeight();

            if (dW > 0 && dH > 0) {
                final float x = getInX();
                final float y = getInY();
                float width = getInWidth();
                float height = getInHeight();
                if (scaleType == FIT) {
                    if (dW > dH) {
                        dH = (dH / dW) * height;
                        dW = width;
                    } else {
                        dW = (dW / dH) * width;
                        dH = height;
                    }
                } else if (scaleType == CROP) {
                    if (dW > dH) {
                        dW = (dW / dH) * width;
                        dH = height;
                    } else {
                        dH = (dH / dW) * height;
                        dW = width;
                    }
                } else if (scaleType == STRETCH) {
                    dW = width;
                    dH = height;
                }

                context.setTransform2D(getTransformView());
                drawable.draw(context, xOff(x, x + width, dW), yOff(y, y + height, dH), dW, dH, frame);
                context.setTransform2D(null);
            }

            if (drawable.isDynamic()) {
                invalidate(false);
            }
        }
    }

    protected float xOff(float start, float end, float width) {
        if (end < start) return (start + end) / 2f;
        if (horizontalAlign == Align.Horizontal.RIGHT) return end - width;
        if (horizontalAlign == Align.Horizontal.CENTER) return (start + end - width) / 2f;
        return start;
    }

    protected float yOff(float start, float end, float height) {
        if (end < start) return (start + end) / 2f;
        if (verticalAlign == Align.Vertical.BOTTOM || verticalAlign == Align.Vertical.BASELINE) return end - height;
        if (verticalAlign == Align.Vertical.MIDDLE) return (start + end - height) / 2f;
        return start;
    }
}
