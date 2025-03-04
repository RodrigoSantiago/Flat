package flat.widget.value;

import flat.animations.StateInfo;
import flat.events.PointerEvent;
import flat.events.SlideEvent;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.graphics.image.Drawable;
import flat.math.stroke.BasicStroke;
import flat.uxml.*;
import flat.widget.Widget;
import flat.widget.enums.Direction;
import flat.widget.enums.ImageFilter;

public class Slider extends Widget {

    private UXListener<SlideEvent> slideListener;
    private UXListener<SlideEvent> slideFilter;
    private UXValueListener<Float> valueListener;
    
    private Direction direction = Direction.HORIZONTAL;

    private Drawable icon;
    private float iconWidth;
    private float iconHeight;
    private int iconColor = Color.white;
    private ImageFilter iconImageFilter = ImageFilter.LINEAR;
    private int iconBgColor = Color.transparent;
    
    private float lineWidth = 1;
    private int lineColor = Color.white;
    private int lineFilledColor = Color.black;

    private float minValue = 0;
    private float maxValue = 1;
    private float steps;

    private float value;

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);
        UXAttrs attrs = getAttrs();

        setRangeLimits(attrs.getAttributeNumber("min-value", getMinValue()), attrs.getAttributeNumber("max-value", getMaxValue()));
        setSteps(attrs.getAttributeNumber("steps", getSteps()));
        setValue(attrs.getAttributeNumber("value", getValue()));
        setSlideListener(attrs.getAttributeListener("on-slide", SlideEvent.class, controller));
        setSlideFilter(attrs.getAttributeListener("on-slide-filter", SlideEvent.class, controller));
        setValueListener(attrs.getAttributeValueListener("on-value-change", Float.class, controller));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();

        setIcon(attrs.getResourceAsDrawable("icon", info, getIcon(), false));
        setIconWidth(attrs.getSize("icon-width", info, getIconWidth()));
        setIconHeight(attrs.getSize("icon-height", info, getIconHeight()));
        setIconColor(attrs.getColor("icon-color", info, getIconColor()));
        setIconBgColor(attrs.getColor("icon-bg-color", info, getIconBgColor()));
        setIconImageFilter(attrs.getConstant("icon-image-filter", info, getIconImageFilter()));
        setLineWidth(attrs.getSize("line-width", info, getLineWidth()));
        setLineColor(attrs.getColor("line-color", info, getLineColor()));
        setLineFilledColor(attrs.getColor("line-filled-color", info, getLineFilledColor()));
        setDirection(attrs.getAttributeConstant("direction", getDirection()));
    }

    @Override
    public void onMeasure() {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        float mWidth;
        float mHeight;
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;
        boolean hor = direction == Direction.HORIZONTAL || direction == Direction.IHORIZONTAL;

        if (wrapWidth) {
            mWidth = Math.max(getLayoutIconWidth() * (hor ? 2f : 1f) + extraWidth, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            mHeight = Math.max(getLayoutIconHeight() * (hor ? 1f : 2f) + extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public void onDraw(Graphics graphics) {
        drawBackground(graphics);

        float x = getInX();
        float y = getInY();
        float width = getInWidth();
        float height = getInHeight();

        if (width <= 0 || height <= 0) return;

        float iw = Math.min(width, getLayoutIconWidth());
        float ih = Math.min(height, getLayoutIconHeight());

        boolean hor = direction == Direction.HORIZONTAL || direction == Direction.IHORIZONTAL;
        boolean rev = direction == Direction.IHORIZONTAL || direction == Direction.IVERTICAL;

        float lineStart;
        float lineEnd;
        if (hor) {
            lineStart = x + iw * 0.5f;
            lineEnd = x + width - iw * 0.5f;
        } else {
            lineStart = y + ih * 0.5f;
            lineEnd = y + height - ih * 0.5f;
        }

        float diff = maxValue - minValue;
        float pos = diff == 0 ? 0 : value / diff;
        if (rev) {
            pos = 1 - pos;
        }
        graphics.setTransform2D(getTransform());

        float xpos;
        float ypos;
        if (hor) {
            xpos = (lineStart * (1 - pos) + lineEnd * pos);
            ypos = (y + height * 0.5f);
        } else {
            xpos = (x + width * 0.5f);
            ypos = (lineStart * (1 - pos) + lineEnd * pos);
        }

        float lineWidth = Math.min(getLineWidth(), Math.min(width, height));

        graphics.setStroker(new BasicStroke(lineWidth));
        if (hor) {
            graphics.setColor(getLineColor());
            graphics.drawLine(lineStart, ypos, lineEnd, ypos);
            if (getValue() > getMinValue()) {
                graphics.setColor(getLineFilledColor());
                if (rev) {
                    graphics.drawLine(xpos, ypos, lineEnd, ypos);
                } else {
                    graphics.drawLine(lineStart, ypos, xpos, ypos);
                }
            }
        } else {
            graphics.setColor(getLineColor());
            graphics.drawLine(xpos, lineStart, xpos, lineEnd);
            if (getValue() > getMinValue()) {
                graphics.setColor(getLineFilledColor());
                if (rev) {
                    graphics.drawLine(xpos, ypos, xpos, lineEnd);
                } else {
                    graphics.drawLine(xpos, lineStart, xpos, ypos);
                }
            }
        }

        if (iw > 0 && ih > 0 && getIcon() != null) {
            if (Color.getAlpha(getIconBgColor()) > 0) {
                float bgw = hor ? getOutHeight() : getOutWidth();
                float bgh = hor ? getOutHeight() : getOutWidth();
                graphics.setColor(getIconBgColor());
                graphics.drawEllipse(xpos - bgw * 0.5f, ypos - bgh * 0.5f, bgw, bgh, true);
            }
            getIcon().draw(graphics, xpos - iw * 0.5f, ypos - ih * 0.5f, iw, ih, getIconColor(), getIconImageFilter());
        }
        if (isRippleEnabled()) {
            getRipple().release();
            getRipple().setSize(Math.min(Math.max(iw, ih) * 0.7f, Math.min(getLayoutWidth(), getLayoutHeight()) * 0.5f));
            getRipple().setPosition(xpos, ypos);
            drawRipple(graphics);
        }
    }

    @Override
    public void pointer(PointerEvent event) {
        super.pointer(event);
        if (!event.isConsumed() && event.getPointerID() == 1) {
            var point = screenToLocal(event.getX(), event.getY());

            float x = getInX();
            float y = getInY();
            float width = getInWidth();
            float height = getInHeight();
            float iw = Math.min(width, getLayoutIconWidth());
            float ih = Math.min(height, getLayoutIconHeight());

            boolean hor = direction == Direction.HORIZONTAL || direction == Direction.IHORIZONTAL;
            boolean rev = direction == Direction.IHORIZONTAL || direction == Direction.IVERTICAL;

            float lineStart;
            float lineEnd;
            if (hor) {
                lineStart = x + iw * 0.5f;
                lineEnd = x + width - iw * 0.5f;
            } else {
                lineStart = y + ih * 0.5f;
                lineEnd = y + height - ih * 0.5f;
            }

            float lineSize = lineEnd - lineStart;

            float pos = lineSize == 0 ? 0 : ((hor ? point.x : point.y) - lineStart) / lineSize;
            if (rev) {
                pos = 1 - pos;
            }
            if (event.getType() == PointerEvent.PRESSED || event.getType() == PointerEvent.DRAGGED) {
                slideTo(getMinValue() * (1 - pos) + getMaxValue() * pos);
            }
        }
    }

    public void slide(float offset) {
        slideTo(getValue() + offset);
    }

    public void slideTo(float value) {
        value = steps <= 0 ? value : Math.round(value / steps) * steps;
        value = Math.max(minValue, Math.min(maxValue, value));

        float old = getValue();
        if (value != old && filterSlide(value)) {
            setValue(value);
            fireSlide();
        }
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        if (direction == null) direction = Direction.HORIZONTAL;
        
        if (this.direction != direction) {
            this.direction = direction;
            invalidate(false);
        }
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        if (this.icon != icon) {
            this.icon = icon;
            invalidate(isWrapContent());
        }
    }

    public float getIconWidth() {
        return iconWidth;
    }

    public void setIconWidth(float iconWidth) {
        if (this.iconWidth != iconWidth) {
            this.iconWidth = iconWidth;
            invalidate(isWrapContent());
        }
    }

    protected float getLayoutIconWidth() {
        return icon == null ? 0 : iconWidth == 0 || iconWidth == MATCH_PARENT ? icon.getWidth() : iconWidth;
    }

    public float getIconHeight() {
        return iconHeight;
    }

    public void setIconHeight(float iconHeight) {
        if (this.iconHeight != iconHeight) {
            this.iconHeight = iconHeight;
            invalidate(isWrapContent());
        }
    }

    protected float getLayoutIconHeight() {
        return icon == null ? 0 : iconHeight == 0 || iconHeight == MATCH_PARENT ? icon.getHeight() : iconHeight;
    }

    public int getIconColor() {
        return iconColor;
    }

    public void setIconColor(int iconColor) {
        if (this.iconColor != iconColor) {
            this.iconColor = iconColor;
            invalidate(false);
        }
    }

    public int getIconBgColor() {
        return iconBgColor;
    }

    public void setIconBgColor(int iconBgColor) {
        if (this.iconBgColor != iconBgColor) {
            this.iconBgColor = iconBgColor;
            invalidate(false);
        }
    }

    public ImageFilter getIconImageFilter() {
        return iconImageFilter;
    }

    public void setIconImageFilter(ImageFilter iconImageFilter) {
        if (iconImageFilter == null) iconImageFilter = ImageFilter.LINEAR;
        
        if (this.iconImageFilter != iconImageFilter) {
            this.iconImageFilter = iconImageFilter;
            invalidate(false);
        }
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        if (this.lineWidth != lineWidth) {
            this.lineWidth = lineWidth;
            invalidate(false);
        }
    }

    public int getLineColor() {
        return lineColor;
    }

    public void setLineColor(int lineColor) {
        if (this.lineColor != lineColor) {
            this.lineColor = lineColor;
            invalidate(false);
        }
    }

    public int getLineFilledColor() {
        return lineFilledColor;
    }

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public void setRangeLimits(float minValue, float maxValue) {
        float cMinValue = Math.min(minValue, maxValue);
        float cMaxValue = Math.max(minValue, maxValue);
        if (cMinValue != this.minValue || cMaxValue != this.maxValue) {
            this.minValue = cMinValue;
            this.maxValue = cMaxValue;
            setValue(getValue());
            invalidate(false);
        }
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        value = steps <= 0 ? value : Math.round(value / steps) * steps;
        value = Math.max(minValue, Math.min(maxValue, value));

        if (this.value != value) {
            float old = this.value;
            this.value = value;
            invalidate(false);
            fireValueListener(old);
        }
    }

    public float getSteps() {
        return steps;
    }

    public void setSteps(float steps) {
        if (this.steps != steps) {
            this.steps = steps;
            setValue(getValue());
            invalidate(false);
        }
    }

    public void setLineFilledColor(int lineFilledColor) {
        if (this.lineFilledColor != lineFilledColor) {
            this.lineFilledColor = lineFilledColor;
            invalidate(false);
        }
    }

    public UXListener<SlideEvent> getSlideFilter() {
        return slideFilter;
    }

    public void setSlideFilter(UXListener<SlideEvent> slideFilter) {
        this.slideFilter = slideFilter;
    }

    private boolean filterSlide(float viewOffset) {
        if (slideFilter != null) {
            var event = new SlideEvent(this, SlideEvent.FILTER, viewOffset);
            UXListener.safeHandle(slideFilter, event);
            return !event.isConsumed();
        }
        return true;
    }

    public UXListener<SlideEvent> getSlideListener() {
        return slideListener;
    }

    public void setSlideListener(UXListener<SlideEvent> slideListener) {
        this.slideListener = slideListener;
    }

    private void fireSlide() {
        if (slideListener != null) {
            UXListener.safeHandle(slideListener, new SlideEvent(this, SlideEvent.SLIDE, value));
        }
    }

    public void setValueListener(UXValueListener<Float> valueListener) {
        this.valueListener = valueListener;
    }

    public UXValueListener<Float> getValueListener() {
        return valueListener;
    }

    private void fireValueListener(float old) {
        if (valueListener != null && old != value) {
            UXValueListener.safeHandle(valueListener, new ValueChange<>(this, old, value));
        }
    }

    protected boolean isWrapContent() {
        return getPrefWidth() == WRAP_CONTENT || getPrefHeight() == WRAP_CONTENT;
    }
}
