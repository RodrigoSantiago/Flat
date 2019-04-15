package flat.widget.value;

import flat.animations.Interpolation;
import flat.animations.NormalizedAnimation;
import flat.animations.StateInfo;
import flat.events.*;
import flat.graphics.SmartContext;
import flat.graphics.context.Font;
import flat.graphics.image.Drawable;
import flat.graphics.text.Align;
import flat.math.Vector2;
import flat.math.shapes.Rectangle;
import flat.resources.Resource;
import flat.uxml.Controller;
import flat.uxml.UXStyleAttrs;
import flat.widget.Widget;

public class Slider extends Widget {

    // Properties
    private int color, expansionColor;
    private float expansion;
    private float min = 0, max = 10;
    private float value, value2;
    private int ticks;
    private boolean rangeEnabled;
    private Drawable icon;

    private int labelDecimal;
    private boolean labelEnabled;
    private Font labelFont;
    private float labelTextSize;
    private int labelTextColor;
    private float labelPosition;
    private long labelTimeOut;
    private Drawable labelIcon;

    // Animation
    private int hover;
    private final SlideAnim anim = new SlideAnim();
    private final SlideAnim anim2 = new SlideAnim();
    private final SlideAnim anim3 = new SlideAnim();

    //Events
    private ActionListener onValueChange;
    private ActionListener onValue2Change;

    @Override
    public void applyAttributes(UXStyleAttrs style, Controller controller) {
        super.applyAttributes(style, controller);

        setMin(style.asNumber("min", getMin()));
        setMax(style.asNumber("max", getMax()));
        _setValue(style.asNumber("value", getValue2()));
        _setValue2(style.asNumber("value2", getValue2()));
        setTicks((int) style.asNumber("ticks", getTicks()));
        setRangeEnabled(style.asBool("range-enabled", isRangeEnabled()));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();
        if (getStyle() == null) return;

        StateInfo info = getStateInfo();

        setColor(getStyle().asColor("color", info, getColor()));

        setExpansion(getStyle().asNumber("expansion", info, getExpansion()));
        setExpansionColor(getStyle().asColor("expansion-color", info, getColor()));

        setLabelDecimal((int) getStyle().asNumber("label-decimal", info, getLabelDecimal()));
        setLabelFont(getStyle().asFont("label-font", info, getLabelFont()));
        setLabelTextSize(getStyle().asSize("label-text-size", info, getLabelTextSize()));
        setLabelTextColor(getStyle().asColor("label-text-color", info, getLabelTextColor()));
        setLabelEnabled(getStyle().asBool("label-enabled", info, isLabelEnabled()));
        setLabelPosition(getStyle().asSize("label-position", info, getLabelPosition()));
        setLabelTimeOut((long) getStyle().asSize("label-time-out", info, getLabelTimeOut()));

        Resource res = getStyle().asResource("icon", info);
        if (res != null) {
            Drawable drawable = res.getDrawable();
            if (drawable != null) {
                setIcon(drawable);
            }
        }
        res = getStyle().asResource("label-icon", info);
        if (res != null) {
            Drawable drawable = res.getDrawable();
            if (drawable != null) {
                setLabelIcon(drawable);
            }
        }
    }

    @Override
    public void onDraw(SmartContext context) {
        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        final boolean fValue = Math.abs(anim3.isPlaying() ? anim3.value : hover) == 1;
        final float val = anim.isPlaying() ? anim.value : value;
        final float val2 = anim2.isPlaying() ? anim2.value : value2;
        final float x1 = Interpolation.mix(x + height / 2f, x + width - height / 2f, max == min ? max : (val - min) / (max - min));
        final float x2 = Interpolation.mix(x + height / 2f, x + width - height / 2f, max == min ? max : (val2 - min) / (max - min));
        final float y1 = y + height / 2f;

        // Background
        context.setTransform2D(getTransform());
        context.setColor(getBackgroundColor());
        context.drawRoundRect(x, y, width, height,
                getRadiusTop(), getRadiusRight(), getRadiusBottom(), getRadiusLeft(), true);

        // Filled area
        context.setColor(color);
        if (!rangeEnabled) {
            context.drawRoundRect(x, y, x1 - x, height,
                    getRadiusTop(), getRadiusRight(), getRadiusBottom(), getRadiusLeft(), true);
        } else {
            context.drawRoundRect(Math.min(x1, x2), y, Math.abs(x2 - x1), height,
                    getRadiusTop(), getRadiusRight(), getRadiusBottom(), getRadiusLeft(), true);
        }

        // Ticks
        if (ticks > 0) {
            for (int i = 0; i <= ticks; i++) {
                float tval = (max - min) / ticks * i;
                if ((!rangeEnabled && ((max > min && tval < val) || (max < min && tval > val))) ||
                        (rangeEnabled && tval >= Math.min(val, val2) && tval <= Math.max(val, val2))) {
                    context.setColor(getBackgroundColor());
                } else {
                    context.setColor(color);
                }
                context.drawRect(x + width / ticks * i, y, height, height, true);
            }
        }

        // Icon Shadow
        Drawable ic = icon;

        if (ic != null && isShadowEnabled()) {
            context.setColor(0x00000047);
            context.setTransform2D(getTransform().preTranslate(0, Math.max(0, getElevation())));
            ic.draw(context, x1 - ic.getWidth() / 2f, y1 - ic.getHeight() / 2f, 0);

            if (rangeEnabled) {
                context.setColor(0x00000047);
                context.setTransform2D(getTransform().preTranslate(0, Math.max(0, getElevation())));
                ic.draw(context, x2 - ic.getWidth() / 2f, y1 - ic.getHeight() / 2f, 0);
            }
        }

        // Expansion
        if (expansion > 0 && hover > 0) {
            context.setColor(expansionColor);
            context.setTransform2D(getTransform());
            context.drawCircle(hover == 1 ? x1 : x2, y1, expansion, true);
        }

        // Icon
        if (ic != null) {
            context.setColor(color);
            context.setTransform2D(getTransform());
            ic.draw(context, x1 - ic.getWidth() / 2f, y1 - ic.getHeight() / 2f, 0);

            if (rangeEnabled) {
                context.setColor(color);
                context.setTransform2D(getTransform());
                ic.draw(context, x2 - ic.getWidth() / 2f, y1 - ic.getHeight() / 2f, 0);
            }
        }

        // Label
        ic = labelIcon;

        if (ic != null && (isPressed() || anim3.isPlaying())) {
            context.setTransform2D(getTransform());
            context.setColor(color);
            ic.draw(context, (fValue ? x1 : x2) - ic.getWidth() / 2f, y1 - ic.getHeight() / 2f, 0);

            if (labelEnabled) {
                String str = String.format("%.0"+ labelDecimal +"f", fValue ? val : val2);
                context.setTransform2D(getTransform());
                context.setColor(labelTextColor);
                context.setTextSize(labelTextSize);
                context.setTextFont(labelFont);
                context.setTextHorizontalAlign(Align.Horizontal.CENTER);
                context.drawText((fValue ? x1 : x2), y1 - labelPosition, str);
            }
        }

        // Ripple
        if (isRippleEnabled() && getRipple().isVisible()) {
            context.setTransform2D(getTransform().translate(fValue ? x1 : x2, y1));
            getRipple().drawRipple(context, null, getRippleColor());
        }

        context.setTransform2D(null);
    }

    @Override
    public void fireHover(HoverEvent hoverEvent) {
        super.fireHover(hoverEvent);
        if (!hoverEvent.isConsumed() && icon != null && !isDragged()) {
            Vector2 point = new Vector2(hoverEvent.getX(), hoverEvent.getY());
            screenToLocal(point);
            int p = checkPoint(point.x, point.y);
            if (p == 0 && hover != 0) {
                p = -Math.abs(hover);
            }
            if (p != hover) {
                hover = p;
                invalidate(false);
            }
        }
    }

    @Override
    public void firePointer(PointerEvent pointerEvent) {
        super.firePointer(pointerEvent);
        if (!pointerEvent.isConsumed()) {
            Vector2 point = new Vector2(pointerEvent.getX(), pointerEvent.getY());
            boolean valMove = !(rangeEnabled && Math.abs(point.x - getX(1)) > Math.abs(point.x - getX(2)));

            point.set(pointerEvent.getX() - getInX(), pointerEvent.getY() - getInY());
            screenToLocal(point);

            float val = max == min ? max : (point.x / (getInWidth() - getInHeight())) * (max - min) + min;
            if (ticks > 0 && max != min) {
                val = Math.round(val / ((max - min) / ticks)) * ((max - min) / ticks);
            }

            if (pointerEvent.getType() == PointerEvent.DRAGGED) {
                if (hover == 2) {
                    _setValue2(val);
                } else {
                    _setValue(val);
                }
            } else if (pointerEvent.getType() == PointerEvent.PRESSED && !isDragged() && !isPressed()) {
                if (icon != null && !isDragged()) {
                    hover = valMove ? 1 : 2;
                    invalidate(false);
                }

                if (hover == 2) {
                    setValue2(val);
                } else {
                    setValue(val);
                }
            } else if (pointerEvent.getType() == PointerEvent.RELEASED && isPressed()) {
                anim3.stop();
                anim3.tValue = hover;
                anim3.fValue = hover;
                anim3.setDuration(labelTimeOut);
                anim3.play();
            }
        }
    }

    @Override
    public void fireKey(KeyEvent keyEvent) {
        super.fireKey(keyEvent);
        if (!keyEvent.isConsumed() && keyEvent.getType() == KeyEvent.RELEASED) {
            if (keyEvent.getKeycode() == KeyCode.KEY_LEFT) {
                setValue(value - (max - min) / ticks);
            } else if (keyEvent.getKeycode() == KeyCode.KEY_RIGHT) {
                setValue(value + (max - min) / ticks);
            }
        }
    }

    @Override
    public void fireRipple(float x, float y) {
        if (isRippleEnabled()) {
            getRipple().setSize(expansion);
            getRipple().fire(0, 0);
        }
    }

    private int checkPoint(float x, float y) {
        Rectangle rec = new Rectangle();
        rec.x = getX(1) - (icon.getWidth() / 2f);
        rec.y = getInY() + getInHeight() / 2f - (icon.getHeight() / 2f);
        rec.width = icon.getWidth();
        rec.height = icon.getWidth();

        if (rec.contains(x, y)) {
            return 1;
        } else if (rangeEnabled) {
            rec.x = getX(2) - (icon.getWidth() / 2f);
            return rec.contains(x, y) ? 2 : 0;
        }
        return 0;
    }

    private float getX(int id) {
        float v = max == min ? 0 : ((id == 1 ? value : value2) - min) / (max - min);
        return Interpolation.mix(getInX() + getInHeight() / 2f, getInX() + getInWidth() - getInHeight() / 2f, v);
    }

    public ActionListener getOnValueChange() {
        return onValueChange;
    }

    public void setOnValueChange(ActionListener onValueChange) {
        this.onValueChange = onValueChange;
    }

    public ActionListener getOnValue2Change() {
        return onValue2Change;
    }

    public void setOnValue2Change(ActionListener onValue2Change) {
        this.onValue2Change = onValue2Change;
    }

    public float getMin() {
        return min;
    }

    public void setMin(float min) {
        if (this.min != min) {
            this.min = min;
            float v = Math.min(Math.max(max, min), Math.max(Math.min(max, min), value));
            if (v != value) {
                _setValue(v);
            }
            float v2 = Math.min(Math.max(max, min), Math.max(Math.min(max, min), value2));
            if (v2 != value2) {
                _setValue2(v);
            }
            invalidate(false);
        }
    }

    public float getMax() {
        return max;
    }

    public void setMax(float max) {
        if (this.max != max) {
            this.max = max;
            float v = Math.min(Math.max(max, min), Math.max(Math.min(max, min), value));
            if (v != value) {
                _setValue(v);
            }
            float v2 = Math.min(Math.max(max, min), Math.max(Math.min(max, min), value2));
            if (v2 != value2) {
                _setValue2(v);
            }
            invalidate(false);
        }
    }

    public float getValue() {
        return value;
    }

    private void _setValue(float value) {
        value = Math.min(Math.max(max, min), Math.max(Math.min(max, min), value));

        if (this.value != value) {
            anim.stop();

            this.value = value;
            invalidate(false);
            if (onValueChange != null) {
                onValueChange.handle(new ActionEvent(this, ActionEvent.ACTION));
            }
        }
    }

    public void setValue(float value) {
        value = Math.min(Math.max(max, min), Math.max(Math.min(max, min), value));

        if (this.value != value) {
            float oldValue = this.value;
            _setValue(value);
            anim.fValue = oldValue;
            anim.tValue = this.value;
            anim.setDuration(getTransitionDuration());
            anim.play();
        }
    }

    public float getValue2() {
        return rangeEnabled ? value2 : 0;
    }

    private void _setValue2(float value2) {
        value2 = Math.min(Math.max(max, min), Math.max(Math.min(max, min), value2));

        if (this.value2 != value2) {
            anim2.stop();

            this.value2 = value2;
            invalidate(false);
            if (onValue2Change != null) {
                onValue2Change.handle(new ActionEvent(this, ActionEvent.ACTION));
            }
        }
    }

    public void setValue2(float value2) {
        value2 = Math.min(Math.max(max, min), Math.max(Math.min(max, min), value2));

        if (this.value2 != value2) {
            float oldRange = this.value2;
            _setValue2(value2);
            anim2.fValue = oldRange;
            anim2.tValue = this.value2;
            anim2.setDuration(getTransitionDuration());
            anim2.play();
        }
    }

    public boolean isRangeEnabled() {
        return rangeEnabled;
    }

    public void setRangeEnabled(boolean rangeEnabled) {
        if (this.rangeEnabled != rangeEnabled) {
            this.rangeEnabled = rangeEnabled;
        }
    }

    public int getTicks() {
        return ticks;
    }

    public void setTicks(int ticks) {
        if (this.ticks != ticks) {
            this.ticks = ticks;
            invalidate(false);
        }
    }

    public float getExpansion() {
        return expansion;
    }

    public void setExpansion(float expansion) {
        if (this.expansion != expansion) {
            this.expansion = expansion;
            invalidate(false);
        }
    }

    public int getLabelDecimal() {
        return labelDecimal;
    }

    public void setLabelDecimal(int labelDecimal) {
        if (labelDecimal < 0) labelDecimal = 0;

        if (this.labelDecimal != labelDecimal) {
            this.labelDecimal = labelDecimal;
            invalidate(false);
        }
    }

    public boolean isLabelEnabled() {
        return labelEnabled;
    }

    public void setLabelEnabled(boolean labelEnabled) {
        if (this.labelEnabled != labelEnabled) {
            this.labelEnabled = labelEnabled;
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

    public float getLabelPosition() {
        return labelPosition;
    }

    public void setLabelPosition(float labelPosition) {
        if (this.labelPosition != labelPosition) {
            this.labelPosition = labelPosition;
            invalidate(false);
        }
    }

    public long getLabelTimeOut() {
        return labelTimeOut;
    }

    public void setLabelTimeOut(long labelTimeOut) {
        if (this.labelTimeOut != labelTimeOut) {
            this.labelTimeOut = labelTimeOut;
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

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        if (this.icon != icon) {
            this.icon = icon;
            invalidate(false);
        }
    }

    public Drawable getLabelIcon() {
        return labelIcon;
    }

    public void setLabelIcon(Drawable labelIcon) {
        if (this.labelIcon != labelIcon) {
            this.labelIcon = labelIcon;
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

    public int getExpansionColor() {
        return expansionColor;
    }

    public void setExpansionColor(int expansionColor) {
        if (this.expansionColor != expansionColor) {
            this.expansionColor = expansionColor;
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
