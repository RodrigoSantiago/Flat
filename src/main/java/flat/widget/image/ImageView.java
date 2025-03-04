package flat.widget.image;

import flat.animations.StateInfo;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.graphics.image.Drawable;
import flat.math.shapes.Shape;
import flat.uxml.UXAttrs;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.ImageFilter;
import flat.widget.enums.ImageScale;
import flat.widget.enums.VerticalAlign;

public class ImageView extends Widget {

    private Drawable image;
    private ImageFilter imageFilter = ImageFilter.LINEAR;
    private ImageScale imageScale = ImageScale.STRETCH;

    private int color = Color.white;
    private VerticalAlign verticalAlign = VerticalAlign.MIDDLE;
    private HorizontalAlign horizontalAlign = HorizontalAlign.CENTER;

    private float imgWidth;
    private float imgHeight;

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();

        setImage(attrs.getResourceAsDrawable("image", info, getImage(), false));
        setColor(attrs.getColor("color", info, getColor()));
        setImageFilter(attrs.getConstant("image-filter", info, getImageFilter()));
        setImageScale(attrs.getConstant("image-scale", info, getImageScale()));
        setVerticalAlign(attrs.getConstant("vertical-align", info, getVerticalAlign()));
        setHorizontalAlign(attrs.getConstant("horizontal-align", info, getHorizontalAlign()));
    }

    @Override
    public void onMeasure() {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        float mWidth;
        float mHeight;
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;

        if (wrapWidth) {
            mWidth = Math.max(imgWidth + extraWidth, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            mHeight = Math.max(imgHeight + extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public void onDraw(Graphics graphics) {
        drawBackground(graphics);
        drawRipple(graphics);

        float dW = imgWidth;
        float dH = imgHeight;

        final float x = getInX();
        final float y = getInY();
        float width = getInWidth();
        float height = getInHeight();

        if (image != null && width > 0 && height > 0 && dW > 0 && dH > 0) {
            if (imageScale == ImageScale.FIT) {
                float imgAspect = imgWidth / imgHeight;
                float frameAspect = width / height;
                if (imgAspect > frameAspect) {
                    dW = width;
                    dH = width / imgAspect;
                } else {
                    dW = height * imgAspect;
                    dH = height;
                }

            } else if (imageScale == ImageScale.CROP) {
                float scale = Math.max(width / imgWidth, height / imgHeight);
                dW = imgWidth * scale;
                dH = imgHeight * scale;

            } else if (imageScale == ImageScale.STRETCH) {
                dW = width;
                dH = height;
            } else {
                dW = imgWidth;
                dH = imgHeight;
            }

            boolean hasRadius =
                    getRadiusTop() > 0 ||  getRadiusTop() > 0 ||
                    getRadiusLeft() > 0 || getRadiusRight() > 0;

            boolean overflow = dW > width || dH > height;

            Shape oldClip = null;
            if (hasRadius || overflow) {
                graphics.pushClip(getBackgroundShape());
            }
            graphics.setTransform2D(getTransform());
            image.draw(graphics
                    , xOff(x, x + width, dW)
                    , yOff(y, y + height, dH)
                    , dW, dH, color, imageFilter);
            if (hasRadius || overflow) {
                graphics.popClip();
            }
        }
    }

    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable image) {
        if (this.image != image) {
            this.image = image;
            float nW, nH;
            if (image == null) {
                nW = 0;
                nH = 0;
            } else {
                nW = image.getWidth();
                nH = image.getHeight();
            }

            boolean invalidSize = nW != imgWidth || nH != imgHeight;
            imgWidth = nW;
            imgHeight = nH;
            invalidate(invalidSize);
        }
    }

    public ImageFilter getImageFilter() {
        return imageFilter;
    }

    public void setImageFilter(ImageFilter imageFilter) {
        if (imageFilter == null) imageFilter = ImageFilter.LINEAR;

        if (this.imageFilter != imageFilter) {
            this.imageFilter = imageFilter;
            invalidate(false);
        }
    }

    public ImageScale getImageScale() {
        return imageScale;
    }

    public void setImageScale(ImageScale imageScale) {
        if (imageScale == null) imageScale = ImageScale.STRETCH;

        if (this.imageScale != imageScale) {
            this.imageScale = imageScale;
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

    public VerticalAlign getVerticalAlign() {
        return verticalAlign;
    }

    public void setVerticalAlign(VerticalAlign verticalAlign) {
        if (verticalAlign == null) verticalAlign = VerticalAlign.TOP;

        if (this.verticalAlign != verticalAlign) {
            this.verticalAlign = verticalAlign;
            invalidate(false);
        }
    }

    public HorizontalAlign getHorizontalAlign() {
        return horizontalAlign;
    }

    public void setHorizontalAlign(HorizontalAlign horizontalAlign) {
        if (horizontalAlign == null) horizontalAlign = HorizontalAlign.LEFT;

        if (this.horizontalAlign != horizontalAlign) {
            this.horizontalAlign = horizontalAlign;
            invalidate(false);
        }
    }

    protected float xOff(float start, float end, float width) {
        if (end < start) return (start + end) / 2f;
        if (horizontalAlign == HorizontalAlign.RIGHT) return end - width;
        if (horizontalAlign == HorizontalAlign.CENTER) return (start + end - width) / 2f;
        return start;
    }

    protected float yOff(float start, float end, float height) {
        if (end < start) return (start + end) / 2f;
        if (verticalAlign == VerticalAlign.BOTTOM) return end - height;
        if (verticalAlign == VerticalAlign.MIDDLE) return (start + end - height) / 2f;
        return start;
    }
}
