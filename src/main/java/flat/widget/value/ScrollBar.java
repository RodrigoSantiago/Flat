package flat.widget.value;

import flat.animations.Interpolation;
import flat.animations.NormalizedAnimation;
import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.graphics.context.Font;
import flat.graphics.text.Align;
import flat.math.Vector2;
import flat.uxml.Controller;
import flat.uxml.UXBuilder;
import flat.uxml.UXListener;
import flat.uxml.UXTheme;
import flat.window.Activity;
import flat.widget.Widget;
import flat.widget.enuns.Direction;

import java.util.Objects;

public class ScrollBar extends Widget {

    private Direction direction = Direction.HORIZONTAL;

    private int color, popupColor;
    private boolean popupEnabled;
    private float popupX, popupY, popupWidth, popupHeight;
    private float popupRadiusTop, popupRadiusRight, popupRadiusBottom, popupRadiusLeft;
    private float popupTimeOut;

    private String labelText;
    private int labelTextColor;
    private Font labelFont;
    private float labelTextSize;

    private float maxRange, range, value, minRangeDisplay;
    private float pOffset;

    private UXListener<ActionEvent> onValueChange;

    private final SlideAnim anim = new SlideAnim();
    private final SlideAnim anim2 = new SlideAnim();

    @Override
    public void applyAttributes(UXTheme theme, Controller controller, UXBuilder builder) {
        super.applyAttributes(theme, controller, builder);

        /*setDirection(theme.asConstant("direction", getDirection()));
        setPopupEnabled(theme.asBool("popup-enabled", isPopupEnabled()));
        setMaxRange(theme.asNumber("max-range", getMaxRange()));
        setRange(theme.asNumber("range", getMaxRange()));
        setValueDirect(theme.asNumber("value", getValue()));
        setMinRangeDisplay(theme.asSize("min-range-display", getMinRangeDisplay()));
        setLabelText(theme.asString("label-text", getLabelText()));*/
    }

    @Override
    public void applyStyle() {
        super.applyStyle();
        /*if (getAttrs() == null) return;

        StateInfo info = getStateInfo();

        setColor(getAttrs().asColor("color", info, getColor()));

        setLabelFont(getAttrs().asFont("label-font", info, getLabelFont()));
        setLabelTextColor(getAttrs().asColor("label-text-color", info, getLabelTextColor()));
        setLabelTextSize(getAttrs().asSize("label-text-size", info, getLabelTextSize()));

        setPopupColor(getAttrs().asColor("popup-color", info, getPopupColor()));
        setPopupX(getAttrs().asSize("popup-x", info, getPopupX()));
        setPopupY(getAttrs().asSize("popup-y", info, getPopupY()));
        setPopupWidth(getAttrs().asSize("popup-width", info, getPopupWidth()));
        setPopupHeight(getAttrs().asSize("popup-height", info, getPopupHeight()));
        setPopupRadiusTop(getAttrs().asSize("popup-radius-top", info, getPopupRadiusTop()));
        setPopupRadiusRight(getAttrs().asSize("popup-radius-right", info, getPopupRadiusRight()));
        setPopupRadiusBottom(getAttrs().asSize("popup-radius-bottom", info, getPopupRadiusBottom()));
        setPopupRadiusLeft(getAttrs().asSize("popup-radius-left", info, getPopupRadiusLeft()));
        setPopupTimeOut(getAttrs().asNumber("popup-time-out", info, getPopupTimeOut()));*/
    }

    @Override
    protected void onActivityChange(Activity prev, Activity activity) {
        super.onActivityChange(prev, activity);

        if (anim.isPlaying()) {
            if (prev != null) prev.removeAnimation(anim);
            if (activity != null) activity.addAnimation(anim);
        }

        if (anim2.isPlaying()) {
            if (prev != null) prev.removeAnimation(anim2);
            if (activity != null) activity.addAnimation(anim2);
        }
    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);
        boolean lh = direction == Direction.HORIZONTAL || direction == Direction.IHORIZONTAL;
        boolean li = direction == Direction.IVERTICAL || direction == Direction.IHORIZONTAL;

        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        float w, h, x1, y1, xc, yc, t;

        t = (anim.isPlaying() ? anim.value : value);
        if (li) {
            t = 1 - t;
        }

        if (lh) {
            w = Math.max(minRangeDisplay, (range * width) / maxRange);
            h = height;

            x1 = x + (width - w) * t;
            y1 = y;
            xc = x + (width - w) * t + w / 2f;
            yc = y + height / 2f;
        } else {
            w = width;
            h = Math.max(minRangeDisplay, (range * height) / maxRange);

            x1 = x;
            y1 = y + (height - h) * t;
            xc = x + width / 2f;
            yc = y + (height - h) * t + h / 2f;
        }

        context.setTransform2D(getTransform());
        context.setColor(color);
        context.drawRoundRect(x1, y1, w, h,
                getRadiusTop(), getRadiusRight(), getRadiusBottom(), getRadiusLeft(), true);

        if (popupEnabled && (isPressed() || anim2.isPlaying())) {
            context.drawRoundRect(xc + popupX, yc + popupY, popupWidth, popupHeight,
                    popupRadiusTop, popupRadiusRight, popupRadiusBottom, popupRadiusLeft, true);

            if (labelText != null) {
                context.setTransform2D(getTransform());
                context.setColor(labelTextColor);
                context.setTextSize(labelTextSize);
                context.setTextFont(labelFont);
                context.setTextVerticalAlign(Align.Vertical.MIDDLE);
                context.setTextHorizontalAlign(Align.Horizontal.CENTER);
                context.drawText(xc + popupX + popupWidth / 2f, yc + popupY + popupHeight / 2f, labelText);
            }
        }
    }

    @Override
    public void firePointer(PointerEvent pointerEvent) {
        super.firePointer(pointerEvent);
        if (!pointerEvent.isConsumed()) {
            Vector2 point = new Vector2(pointerEvent.getX(), pointerEvent.getY());
            screenToLocal(point);

            boolean h = direction == Direction.HORIZONTAL || direction == Direction.IHORIZONTAL;
            boolean i = direction == Direction.IVERTICAL || direction == Direction.IHORIZONTAL;

            float tPoint = (h ? point.x : point.y);
            float size = (h ? getInWidth() : getInHeight());
            // Bar Width
            float w = Math.max(minRangeDisplay, (range * size) / maxRange);
            // Bar position
            float p = (size - w) * value + w / 2f;

            if (pointerEvent.getType() == PointerEvent.PRESSED && !isDragged() && !isPressed()) {
                if (tPoint >= p - w / 2f && tPoint <= p + w / 2f) {
                    pOffset = p - tPoint;
                } else {
                    if (tPoint > p + w / 2f) {
                        pOffset = -w / 2f;
                    } else {
                        pOffset = w / 2f;
                    }
                    float t = Math.max(0, Math.min(1, (tPoint + pOffset - w / 2f) / (size - w)));
                    if (i) {
                        t = 1 - t;
                    }
                    setValue(t);
                }
            }
            if (pointerEvent.getType() == PointerEvent.DRAGGED) {
                float t = Math.max(0, Math.min(1, (tPoint + pOffset - w / 2f) / (size - w)));
                if (i) {
                    t = 1 - t;
                }
                setValue(t);
                anim.stop();
            }
            if (pointerEvent.getType() == PointerEvent.RELEASED && popupEnabled && isPressed()) {
                anim2.play(getActivity());
            }
        }
    }

    public UXListener<ActionEvent> getOnValueChange() {
        return onValueChange;
    }

    public void setOnValueChange(UXListener<ActionEvent> onValueChange) {
        this.onValueChange = onValueChange;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        if (this.direction != direction) {
            this.direction = direction;
            invalidate(false);
        }
    }

    public float getMaxRange() {
        return maxRange;
    }

    public void setMaxRange(float maxRange) {
        if (this.maxRange != maxRange) {
            this.maxRange = maxRange;
            invalidate(false);
        }
    }

    public float getMinRangeDisplay() {
        return minRangeDisplay;
    }

    public void setMinRangeDisplay(float minRangeDisplay) {
        if (this.minRangeDisplay != minRangeDisplay) {
            this.minRangeDisplay = minRangeDisplay;
            invalidate(false);
        }
    }

    public float getRange() {
        return range;
    }

    public void setRange(float range) {
        if (this.range != range) {
            this.range = range;
            invalidate(false);
        }
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        value = Math.max(0, Math.min(1, value));

        if (this.value != value) {
            anim.fValue = this.value;
            anim.tValue = value;
            anim.setDuration(getTransitionDuration());
            anim.play(getActivity());

            this.value = value;
            if (onValueChange != null) {
                onValueChange.handle(new ActionEvent(this));
            }
            invalidate(false);
        }
    }

    public void setValueDirect(float value) {
        value = Math.max(0, Math.min(1, value));

        if (this.value != value) {
            this.value = value;
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

    public boolean isPopupEnabled() {
        return popupEnabled;
    }

    public void setPopupEnabled(boolean popupEnabled) {
        if (this.popupEnabled != popupEnabled) {
            this.popupEnabled = popupEnabled;
            invalidate(false);
        }
    }

    public int getPopupColor() {
        return popupColor;
    }

    public void setPopupColor(int popupColor) {
        if (this.popupColor != popupColor) {
            this.popupColor = popupColor;
            invalidate(false);
        }
    }

    public float getPopupWidth() {
        return popupWidth;
    }

    public void setPopupWidth(float popupWidth) {
        if (this.popupWidth != popupWidth) {
            this.popupWidth = popupWidth;
            invalidate(false);
        }
    }

    public float getPopupHeight() {
        return popupHeight;
    }

    public void setPopupHeight(float popupHeight) {
        if (this.popupHeight != popupHeight) {
            this.popupHeight = popupHeight;
            invalidate(false);
        }
    }

    public float getPopupX() {
        return popupX;
    }

    public void setPopupX(float popupX) {
        if (this.popupX != popupX) {
            this.popupX = popupX;
            invalidate(false);
        }
    }

    public float getPopupY() {
        return popupY;
    }

    public void setPopupY(float popupY) {
        if (this.popupY != popupY) {
            this.popupY = popupY;
            invalidate(false);
        }
    }

    public float getPopupRadiusTop() {
        return popupRadiusTop;
    }

    public void setPopupRadiusTop(float popupRadiusTop) {
        if (this.popupRadiusTop != popupRadiusTop) {
            this.popupRadiusTop = popupRadiusTop;
            invalidate(false);
        }
    }

    public float getPopupRadiusRight() {
        return popupRadiusRight;
    }

    public void setPopupRadiusRight(float popupRadiusRight) {
        if (this.popupRadiusRight != popupRadiusRight) {
            this.popupRadiusRight = popupRadiusRight;
            invalidate(false);
        }
    }

    public float getPopupRadiusBottom() {
        return popupRadiusBottom;
    }

    public void setPopupRadiusBottom(float popupRadiusBottom) {
        if (this.popupRadiusBottom != popupRadiusBottom) {
            this.popupRadiusBottom = popupRadiusBottom;
            invalidate(false);
        }
    }

    public float getPopupRadiusLeft() {
        return popupRadiusLeft;
    }

    public void setPopupRadiusLeft(float popupRadiusLeft) {
        if (this.popupRadiusLeft != popupRadiusLeft) {
            this.popupRadiusLeft = popupRadiusLeft;
            invalidate(false);
        }
    }

    public float getPopupTimeOut() {
        return popupTimeOut;
    }

    public void setPopupTimeOut(float popupTimeOut) {
        if (this.popupTimeOut != popupTimeOut) {
            this.popupTimeOut = popupTimeOut;
            anim2.setDuration(popupTimeOut);
            invalidate(false);
        }
    }

    public String getLabelText() {
        return labelText;
    }

    public void setLabelText(String labelText) {
        if (!Objects.equals(this.labelText, labelText)) {
            this.labelText = labelText;
            invalidate(false);
        }
    }

    public Font getLabelFont() {
        return labelFont;
    }

    public void setLabelFont(Font labelFont) {
        if (this.labelFont != labelFont) {
            this.labelFont = labelFont;
            invalidate(false);
        }
    }

    public float getLabelTextSize() {
        return labelTextSize;
    }

    public void setLabelTextSize(float labelTextSize) {
        if (this.labelTextSize != labelTextSize) {
            this.labelTextSize = labelTextSize;
            invalidate(false);
        }
    }

    public int getLabelTextColor() {
        return labelTextColor;
    }

    public void setLabelTextColor(int labelTextColor) {
        if (this.labelTextColor != labelTextColor) {
            this.labelTextColor = labelTextColor;
            invalidate(false);
        }
    }

    private class SlideAnim extends NormalizedAnimation {
        float fValue;
        float tValue;
        float value;

        @Override
        protected void compute(float t) {
            value = Interpolation.mix(fValue, tValue, t);
            invalidate(false);
        }
    }
}
