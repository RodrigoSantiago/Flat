package flat.widget.image;

import flat.graphics.SmartContext;
import flat.graphics.image.Image;
import flat.graphics.text.Align;
import flat.uxml.Controller;
import flat.uxml.UXAttributes;
import flat.widget.enuns.ImageScale;
import flat.widget.Widget;

public class ImageView extends Widget {

    private Image image;
    private float frame;
    private float speed;
    private ImageScale imageScale;

    private Align.Vertical verticalAlign;
    private Align.Horizontal horizontalAlign;

    private long lastTime;

    @Override
    public void applyAttributes(Controller controller, UXAttributes attributes) {
        super.applyAttributes(controller, attributes);

        setImage(attributes.asImage("image"));
        setFrame(attributes.asNumber("frame", 0));
        setSpeed(attributes.asNumber("speed", 1));
        setImageScale(attributes.asEnum("imageScale", ImageScale.class, ImageScale.NOONE));
        setVerticalAlign(attributes.asEnum("verticalAlign", Align.Vertical.class, Align.Vertical.TOP));
        setHorizontalAlign(attributes.asEnum("horizontalAlign", Align.Horizontal.class, Align.Horizontal.LEFT));
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
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

    @Override
    public void onDraw(SmartContext context) {
        backgroundDraw(context);

        long now = System.currentTimeMillis();
        if (lastTime != 0) {
            frame += ((now - lastTime) / 1000f) * speed;
        }
        lastTime = now;
        frame = frame < 0 ? 1 - Math.abs(frame % 1f) : frame % 1f;

        if (this.image != null) {
            float dW = image.getWidth();
            float dH = image.getHeight();

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

                context.setTransform2D(getTransformView());
                image.draw(context, xOff(x, x + width, dW), yOff(y, y + height, dH), dW, dH, frame);
                context.setTransform2D(null);
            }

            if (image.isDynamic()) {
                invalidate(false);
            }
        }
    }

    @Override
    public void onMeasure() {
        float mWidth = getPrefWidth(), mHeight = getPrefHeight();
        if (image != null) {
            mWidth = mWidth == WRAP_CONTENT ? image.getWidth() : mWidth;
            mHeight = mHeight == WRAP_CONTENT ? image.getHeight() : mHeight;
        }
        mWidth += getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        mHeight += getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();
        setMeasure(mWidth, mHeight);
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
