package flat.widget.value;

import flat.animations.StateInfo;
import flat.events.HoverEvent;
import flat.events.PointerEvent;
import flat.events.SlideEvent;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.graphics.image.Drawable;
import flat.math.Vector2;
import flat.math.stroke.BasicStroke;
import flat.uxml.*;
import flat.widget.Widget;
import flat.widget.enums.Direction;
import flat.widget.enums.ImageFilter;
import flat.widget.enums.LineCap;

public class RangedSlider extends Widget {

    private UXListener<SlideEvent> startSlideListener;
    private UXListener<SlideEvent> startSlideFilter;
    private UXValueListener<Float> startValueListener;
    private UXListener<SlideEvent> endSlideListener;
    private UXListener<SlideEvent> endSlideFilter;
    private UXValueListener<Float> endValueListener;
    
    private Direction direction = Direction.HORIZONTAL;

    private Drawable icon;
    private float iconWidth;
    private float iconHeight;
    private int iconColor = Color.white;
    private int iconBgColor = Color.transparent;
    private ImageFilter iconImageFilter = ImageFilter.LINEAR;
    
    private float lineWidth = 1;
    private LineCap lineCap = LineCap.BUTT;
    private int lineColor = Color.white;
    private int lineFilledColor = Color.black;

    private float minValue = 0;
    private float maxValue = 1;
    private float steps;

    private float startValue;
    private float endValue;

    private int hoverIndex;
    private int grabIndex;
    private boolean grabbed;

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);
        UXAttrs attrs = getAttrs();

        setRangeLimits(attrs.getAttributeNumber("min-value", getMinValue()), attrs.getAttributeNumber("max-value", getMaxValue()));
        setSteps(attrs.getAttributeNumber("steps", getSteps()));
        setValue(attrs.getAttributeNumber("start-value", getStartValue()), attrs.getAttributeNumber("end-value", getEndValue()));
        setStartSlideListener(attrs.getAttributeListener("on-start-slide", SlideEvent.class, controller));
        setStartSlideFilter(attrs.getAttributeListener("on-start-slide-filter", SlideEvent.class, controller));
        setStartValueListener(attrs.getAttributeValueListener("on-start-value-change", Float.class, controller));
        setEndSlideListener(attrs.getAttributeListener("on-end-slide", SlideEvent.class, controller));
        setEndSlideFilter(attrs.getAttributeListener("on-end-slide-filter", SlideEvent.class, controller));
        setEndValueListener(attrs.getAttributeValueListener("on-end-value-change", Float.class, controller));
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
        setLineCap(attrs.getConstant("line-cap", info, getLineCap()));
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
        if (discardDraw(graphics)) return;

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
        float posStart = diff == 0 ? 0 : startValue / diff;
        float posEnd = diff == 0 ? 0 : endValue / diff;
        if (rev) {
            posStart = 1 - posStart;
            posEnd = 1 - posEnd;
        }
        graphics.setTransform2D(getTransform());

        float xposStart;
        float yposStart;
        if (hor) {
            xposStart = (lineStart * (1 - posStart) + lineEnd * posStart);
            yposStart = (y + height * 0.5f);
        } else {
            xposStart = (x + width * 0.5f);
            yposStart = (lineStart * (1 - posStart) + lineEnd * posStart);
        }

        float xposEnd;
        float yposEnd;
        if (hor) {
            xposEnd = (lineStart * (1 - posEnd) + lineEnd * posEnd);
            yposEnd = (y + height * 0.5f);
        } else {
            xposEnd = (x + width * 0.5f);
            yposEnd = (lineStart * (1 - posEnd) + lineEnd * posEnd);
        }

        float lineWidth = Math.min(getLineWidth(), Math.min(width, height));

        graphics.setStroke(new BasicStroke(lineWidth, getLineCap().ordinal(), 0));
        if (hor) {
            graphics.setColor(getLineColor());
            graphics.drawLine(lineStart, yposStart, lineEnd, yposStart);

            if (getStartValue() != getEndValue()) {
                graphics.setColor(getLineFilledColor());
                graphics.drawLine(xposStart, yposStart, xposEnd, yposStart);
            }
        } else {
            graphics.setColor(getLineColor());
            graphics.drawLine(xposStart, lineStart, xposStart, lineEnd);

            if (getStartValue() != getEndValue()) {
                graphics.setColor(getLineFilledColor());
                graphics.drawLine(xposStart, yposStart, xposStart, yposEnd);
            }
        }

        if (Color.getAlpha(getIconBgColor()) > 0) {
            float w = Math.min(getOutWidth(), getOutHeight());
            graphics.setColor(getIconBgColor());
            int index = grabbed ? grabIndex : hoverIndex;
            if (index == 0) {
                graphics.drawEllipse(xposStart - w * 0.5f, yposStart - w * 0.5f, w, w, true);
            } else {
                graphics.drawEllipse(xposEnd - w * 0.5f, yposEnd - w * 0.5f, w, w, true);
            }
        }

        if (isRippleEnabled()) {
            if (grabIndex == 0) {
                getRipple().setPosition(xposStart, yposStart);
            } else {
                getRipple().setPosition(xposEnd, yposEnd);
            }
            drawRipple(graphics);
        }

        if (iw > 0 && ih > 0 && getIcon() != null) {
            drawIconShadow(graphics, xposStart, yposStart, iw, ih);
            getIcon().draw(graphics, xposStart - iw * 0.5f, yposStart - ih * 0.5f, iw, ih, getIconColor(), getIconImageFilter());
            drawIconShadow(graphics, xposEnd, yposEnd, iw, ih);
            getIcon().draw(graphics, xposEnd - iw * 0.5f, yposEnd - ih * 0.5f, iw, ih, getIconColor(), getIconImageFilter());
        }
    }

    protected void drawIconShadow(Graphics graphics, float x, float y, float iw, float ih) {
        float el = getElevation();
        float sw = Math.min(getOutWidth(), iw);
        float sh = Math.min(getOutHeight(), ih);
        float c = Math.max(sw, sh);
        float op = Color.getOpacity(getIconColor()) * 0.20f;
        graphics.drawRoundRectShadow(x - sw * 0.5f, y - sh * 0.5f, sw, sh, c, c, c, c, el + 2f, op);
    }

    private float findPos(Vector2 point) {
        float x = getInX();
        float y = getInY();
        float width = getInWidth();
        float height = getInHeight();
        float iw = Math.min(width, getLayoutIconWidth());
        float ih = Math.min(height, getLayoutIconHeight());
        float range = getMaxValue() - getMinValue();

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
        return pos;
    }

    @Override
    public void hover(HoverEvent event) {
        super.hover(event);

        if (event.getType() == HoverEvent.MOVED) {
            var point = screenToLocal(event.getX(), event.getY());
            float pos = findPos(point);

            float val = getMinValue() * (1 - pos) + getMaxValue() * pos;
            float diff = Math.abs(getStartValue() - val) - Math.abs(getEndValue() - val);
            int lastHover = hoverIndex;
            if (diff < -0.001f) {
                hoverIndex = 0;
            } else if (diff > 0.001f) {
                hoverIndex = 1;
            } else if (getStartValue() == getMinValue()) {
                hoverIndex = 1;
            } else if (getStartValue() == getMaxValue()) {
                hoverIndex = 0;
            } else {
                hoverIndex = val <= getStartValue() ? 0 : 1;
            }
            if (lastHover != hoverIndex) {
                invalidate(false);
            }
        }
    }

    @Override
    public void pointer(PointerEvent event) {
        super.pointer(event);
        if (!event.isConsumed() && event.getPointerID() == 1) {
            var point = screenToLocal(event.getX(), event.getY());
            float pos = findPos(point);

            if (event.getType() == PointerEvent.PRESSED) {
                grabbed = true;
                float val = getMinValue() * (1 - pos) + getMaxValue() * pos;
                float diff = Math.abs(getStartValue() - val) - Math.abs(getEndValue() - val);
                if (diff < -0.001f) {
                    grabIndex = 0;
                } else if (diff > 0.001f) {
                    grabIndex = 1;
                } else if (getStartValue() == getMinValue()) {
                    grabIndex = 1;
                } else if (getStartValue() == getMaxValue()) {
                    grabIndex = 0;
                } else {
                    grabIndex = val <= getStartValue() ? 0 : 1;
                }
                if (grabIndex == 0) {
                    slideStartTo(val);
                } else {
                    slideEndTo(val);
                }
            }

            if (event.getType() == PointerEvent.DRAGGED) {
                float val = getMinValue() * (1 - pos) + getMaxValue() * pos;
                if (grabIndex == 0) {
                    slideStartTo(val);
                } else {
                    slideEndTo(val);
                }
            }

            if (event.getType() == PointerEvent.RELEASED) {
                grabbed = false;
            }
        }
    }

    private float constraintStart(float value) {
        value = steps <= 0 ? value : Math.round(value / steps) * steps;
        return Math.max(minValue, Math.min(maxValue,  Math.min(getEndValue(), value)));
    }

    private float constraintEnd(float value) {
        value = steps <= 0 ? value : Math.round(value / steps) * steps;
        return Math.max(getStartValue(), Math.max(minValue, Math.min(maxValue, value)));
    }

    public void slideStart(float offset) {
        slideStartTo(getStartValue() + offset);
    }

    public void slideStartTo(float value) {
        value = constraintStart(value);

        float old = getStartValue();
        if (value != old && filterStartSlide(value)) {
            setStartValue(value);
            fireStartSlide();
        }
    }

    public void slideEnd(float offset) {
        slideEndTo(getEndValue() + offset);
    }

    public void slideEndTo(float value) {
        value = constraintEnd(value);

        float old = getEndValue();
        if (value != old && filterEndSlide(value)) {
            setEndValue(value);
            fireEndSlide();
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

    public LineCap getLineCap() {
        return lineCap;
    }

    public void setLineCap(LineCap lineCap) {
        if (lineCap == null) lineCap = LineCap.BUTT;

        if (this.lineCap != lineCap) {
            this.lineCap = lineCap;
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

    public void setValue(float start, float end) {
        start = steps <= 0 ? start : Math.round(start / steps) * steps;
        start = Math.max(minValue, Math.min(maxValue, start));

        end = steps <= 0 ? end : Math.round(end / steps) * steps;
        end = Math.max(minValue, Math.min(maxValue, end));


        if (start != this.startValue || end != this.endValue) {
            float oldStart = this.startValue;
            float oldEnd = this.endValue;
            this.startValue = Math.min(start, end);
            this.endValue = Math.max(start, end);

            invalidate(false);
            fireStartValueListener(oldStart);
            fireEndValueListener(oldEnd);
        }
    }

    public void setRangeLimits(float minValue, float maxValue) {
        float cMinValue = Math.min(minValue, maxValue);
        float cMaxValue = Math.max(minValue, maxValue);
        if (cMinValue != this.minValue || cMaxValue != this.maxValue) {
            this.minValue = cMinValue;
            this.maxValue = cMaxValue;
            setValue(getStartValue(), getEndValue());
            invalidate(false);
        }
    }

    public float getStartValue() {
        return startValue;
    }

    public void setStartValue(float startValue) {
        startValue = constraintStart(startValue);

        if (this.startValue != startValue) {
            float old = this.startValue;
            this.startValue = startValue;
            invalidate(false);
            fireStartValueListener(old);
        }
    }

    public float getEndValue() {
        return endValue;
    }

    public void setEndValue(float endValue) {
        endValue = constraintEnd(endValue);

        if (this.endValue != endValue) {
            float old = this.endValue;
            this.endValue = endValue;
            invalidate(false);
            fireEndValueListener(old);
        }
    }

    public float getSteps() {
        return steps;
    }

    public void setSteps(float steps) {
        if (this.steps != steps) {
            this.steps = steps;
            setStartValue(getStartValue());
            invalidate(false);
        }
    }

    public void setLineFilledColor(int lineFilledColor) {
        if (this.lineFilledColor != lineFilledColor) {
            this.lineFilledColor = lineFilledColor;
            invalidate(false);
        }
    }

    public UXListener<SlideEvent> getStartSlideFilter() {
        return startSlideFilter;
    }

    public void setStartSlideFilter(UXListener<SlideEvent> startSlideFilter) {
        this.startSlideFilter = startSlideFilter;
    }

    private boolean filterStartSlide(float viewOffset) {
        if (startSlideFilter != null) {
            var event = new SlideEvent(this, SlideEvent.FILTER, viewOffset);
            UXListener.safeHandle(startSlideFilter, event);
            return !event.isConsumed();
        }
        return true;
    }

    public UXListener<SlideEvent> getStartSlideListener() {
        return startSlideListener;
    }

    public void setStartSlideListener(UXListener<SlideEvent> startSlideListener) {
        this.startSlideListener = startSlideListener;
    }

    private void fireStartSlide() {
        if (startSlideListener != null) {
            UXListener.safeHandle(startSlideListener, new SlideEvent(this, SlideEvent.SLIDE, startValue));
        }
    }

    public void setStartValueListener(UXValueListener<Float> startValueListener) {
        this.startValueListener = startValueListener;
    }

    public UXValueListener<Float> getStartValueListener() {
        return startValueListener;
    }

    private void fireStartValueListener(float old) {
        if (startValueListener != null && old != startValue) {
            UXValueListener.safeHandle(startValueListener, new ValueChange<>(this, old, startValue));
        }
    }

    public UXListener<SlideEvent> getEndSlideFilter() {
        return endSlideFilter;
    }

    public void setEndSlideFilter(UXListener<SlideEvent> endSlideFilter) {
        this.endSlideFilter = endSlideFilter;
    }

    private boolean filterEndSlide(float viewOffset) {
        if (endSlideFilter != null) {
            var event = new SlideEvent(this, SlideEvent.FILTER, viewOffset);
            UXListener.safeHandle(endSlideFilter, event);
            return !event.isConsumed();
        }
        return true;
    }

    public UXListener<SlideEvent> getEndSlideListener() {
        return endSlideListener;
    }

    public void setEndSlideListener(UXListener<SlideEvent> endSlideListener) {
        this.endSlideListener = endSlideListener;
    }

    private void fireEndSlide() {
        if (endSlideListener != null) {
            UXListener.safeHandle(endSlideListener, new SlideEvent(this, SlideEvent.SLIDE, endValue));
        }
    }

    public void setEndValueListener(UXValueListener<Float> endValueListener) {
        this.endValueListener = endValueListener;
    }

    public UXValueListener<Float> getEndValueListener() {
        return endValueListener;
    }

    private void fireEndValueListener(float old) {
        if (endValueListener != null && old != endValue) {
            UXValueListener.safeHandle(endValueListener, new ValueChange<>(this, old, endValue));
        }
    }
}
