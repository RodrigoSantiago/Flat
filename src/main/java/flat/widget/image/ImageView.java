package flat.widget.image;

import flat.animations.StateInfo;
import flat.graphics.SmartContext;
import flat.graphics.image.Drawable;
import flat.graphics.text.Align;
import flat.resources.Resource;
import flat.widget.Widget;
import flat.widget.enuns.ImageScale;

public class ImageView extends Widget {

    private Drawable drawable;
    private float frame;
    private float speed;
    private ImageScale imageScale;

    private Align.Vertical verticalAlign;
    private Align.Horizontal horizontalAlign;

    private long lastTime;

    @Override
    public void applyStyle() {
        super.applyStyle();

        /*StateInfo info = getStateInfo();

        Resource res = getAttrs().asResource("image", info);
        if (res != null) {
            Drawable drawable = res.getDrawable();
            if (drawable != null) {
                setDrawable(drawable);
            }
        }

        setFrame(getAttrs().asNumber("frame", info, getFrame()));
        setSpeed(getAttrs().asNumber("speed", info, getSpeed()));
        setImageScale(getAttrs().asConstant("image-scale", ImageScale.NONE));
        setVerticalAlign(getAttrs().asConstant("v-align", Align.Vertical.MIDDLE));
        setHorizontalAlign(getAttrs().asConstant("h-align", Align.Horizontal.CENTER));*/
    }

    @Override
    public void onDraw(SmartContext context) {
        backgroundDraw(getBackgroundColor(), getBorderColor(), getRippleColor(), context);

        long now = System.currentTimeMillis();
        if (lastTime != 0) {
            frame += ((now - lastTime) / 1000f) * speed;
        }
        lastTime = now;
        frame = frame < 0 ? 1 - Math.abs(frame % 1f) : frame % 1f;

        if (this.drawable != null) {
            float dW = drawable.getWidth();
            float dH = drawable.getHeight();

            if (dW > 0 && dH > 0) {
                final float x = getInX();
                final float y = getInY();
                float width = getInWidth();
                float height = getInHeight();
                if (imageScale == ImageScale.FIT) {
                    if (dW > dH) {
                        dH = (dH / dW) * height;
                        dW = width;
                    } else {
                        dW = (dW / dH) * width;
                        dH = height;
                    }
                } else if (imageScale == ImageScale.CROP) {
                    if (dW > dH) {
                        dW = (dW / dH) * width;
                        dH = height;
                    } else {
                        dH = (dH / dW) * height;
                        dW = width;
                    }
                } else if (imageScale == ImageScale.STRETCH) {
                    dW = width;
                    dH = height;
                }

                context.setTransform2D(getTransform());
                drawable.draw(context, xOff(x, x + width, dW), yOff(y, y + height, dH), dW, dH, frame);
                context.setTransform2D(null);
            }

            if (drawable.isDynamic()) {
                invalidate(false);
            }
        }
    }

    @Override
    public void onMeasure() {
        float mWidth = getPrefWidth(), mHeight = getPrefHeight();
        if (drawable != null) {
            mWidth = mWidth == WRAP_CONTENT ? drawable.getWidth() : mWidth;
            mHeight = mHeight == WRAP_CONTENT ? drawable.getHeight() : mHeight;
        }
        mWidth += getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        mHeight += getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();
        setMeasure(mWidth, mHeight);
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
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

    public ImageScale getImageScale() {
        return imageScale;
    }

    public void setImageScale(ImageScale imageScale) {
        this.imageScale = imageScale;
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
