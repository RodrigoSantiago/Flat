package flat.widget.text;

import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.HoverEvent;
import flat.events.PointerEvent;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.graphics.image.Drawable;
import flat.math.Vector2;
import flat.math.shapes.RoundRectangle;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.uxml.UXListener;
import flat.widget.enums.ImageFilter;
import flat.widget.text.data.Caret;

public class NumberInputField extends TextField {

    private UXListener<ActionEvent> actionIncreaseListener;
    private UXListener<ActionEvent> actionDecreaseListener;

    private Drawable icon;
    private int iconColor = Color.white;
    private ImageFilter iconImageFilter = ImageFilter.LINEAR;
    private float iconSpacing;
    private float iconWidth;
    private float iconHeight;

    private Drawable increaseIcon;
    private int increaseIconColor = Color.white;
    private ImageFilter increaseIconImageFilter = ImageFilter.LINEAR;
    private float increaseIconSpacing;
    private float increaseIconWidth;
    private float increaseIconHeight;

    private Drawable decreaseIcon;
    private int decreaseIconColor = Color.white;
    private ImageFilter decreaseIconImageFilter = ImageFilter.LINEAR;
    private float decreaseIconSpacing;
    private float decreaseIconWidth;
    private float decreaseIconHeight;
    private int minValue = Integer.MIN_VALUE;
    private int maxValue = Integer.MAX_VALUE;

    private int actionIconColor = Color.transparent;

    private float x1, y1, x2, y2;
    private int hoverOnAction;
    private int pressOnAction;

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);
        UXAttrs attrs = getAttrs();

        setRangeLimits((int)attrs.getAttributeNumber("min-value", getMinValue()), (int)attrs.getAttributeNumber("max-value", getMaxValue()));
        setActionIncreaseListener(attrs.getAttributeListener("on-increase", ActionEvent.class, controller));
        setActionDecreaseListener(attrs.getAttributeListener("on-decrease", ActionEvent.class, controller));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();

        setIcon(attrs.getDrawable("icon", info, getIcon(), false));
        setIconColor(attrs.getColor("icon-color", info, getIconColor()));
        setIconWidth(attrs.getSize("icon-width", info, getIconWidth()));
        setIconHeight(attrs.getSize("icon-height", info, getIconHeight()));
        setIconSpacing(attrs.getSize("icon-spacing", info, getIconSpacing()));
        setIconImageFilter(attrs.getConstant("icon-image-filter", info, getIconImageFilter()));

        setActionIconColor(attrs.getColor("action-icon-color", info, getActionIconColor()));

        setIncreaseIcon(attrs.getDrawable("increase-icon", info, getIncreaseIcon(), false));
        setIncreaseIconColor(attrs.getColor("increase-icon-color", info, getIncreaseIconColor()));
        setIncreaseIconWidth(attrs.getSize("increase-icon-width", info, getIncreaseIconWidth()));
        setIncreaseIconHeight(attrs.getSize("increase-icon-height", info, getIncreaseIconHeight()));
        setIncreaseIconSpacing(attrs.getSize("increase-icon-spacing", info, getIncreaseIconSpacing()));
        setIncreaseIconImageFilter(attrs.getConstant("increase-icon-image-filter", info, getIncreaseIconImageFilter()));

        setDecreaseIcon(attrs.getDrawable("decrease-icon", info, getDecreaseIcon(), false));
        setDecreaseIconColor(attrs.getColor("decrease-icon-color", info, getDecreaseIconColor()));
        setDecreaseIconWidth(attrs.getSize("decrease-icon-width", info, getDecreaseIconWidth()));
        setDecreaseIconHeight(attrs.getSize("decrease-icon-height", info, getDecreaseIconHeight()));
        setDecreaseIconSpacing(attrs.getSize("decrease-icon-spacing", info, getDecreaseIconSpacing()));
        setDecreaseIconImageFilter(attrs.getConstant("decrease-icon-image-filter", info, getDecreaseIconImageFilter()));
    }

    @Override
    public void onMeasure() {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        if (getHorizontalBar() != null) {
            getHorizontalBar().onMeasure();
        }
        if (getVerticalBar() != null) {
            getVerticalBar().onMeasure();
        }

        float mWidth;
        float mHeight;
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;
        float iw = getLayoutIconWidth();
        float ih = getLayoutIconHeight();
        float iaw = getLayoutActionIconWidth();
        float iah = getLayoutActionIconHeight();
        if (iw > 0) {
            iw += getIconSpacing();
        }
        if (iaw > 0) {
            iaw += getActionIconSpacing();
        }

        if (wrapWidth) {
            mWidth = Math.max(getNaturalTextWidth() + iw + iaw + extraWidth, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            float titleHeight = hasTitle() ? getTitleHeight() + getTitleSpacing() : 0;
            mHeight = Math.max(Math.max(getTextHeight(), Math.max(ih, iah)) + titleHeight + extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public Vector2 onLayoutViewDimension(float width, float height) {
        if (isTextEmpty()) return super.onLayoutViewDimension(width, height);

        float iw = getLayoutIconWidth();
        float aiw = getLayoutActionIconWidth();
        float exWidth = (iw > 0 ? iw + getIconSpacing(): 0)
                + (aiw <= 0 ? 0 : aiw + getActionIconSpacing());

        Vector2 fromBase = super.onLayoutViewDimension(width, height);
        fromBase.x -= exWidth;
        return fromBase;
    }

    @Override
    public Vector2 onLayoutTotalDimension(float width, float height) {
        if (isTextEmpty()) return super.onLayoutTotalDimension(width, height);

        if (isLineWrapReallyEnabled()) {
            float iw = getLayoutIconWidth();
            float aiw = getLayoutActionIconWidth();
            float exWidth = (iw > 0 ? iw + getIconSpacing(): 0)
                    + (aiw <= 0 ? 0 : aiw + getActionIconSpacing());
            return new Vector2(getInWidth() - exWidth, getTextHeight());
        } else {
            return new Vector2(getTextWidth(), getTextHeight());
        }
    }

    @Override
    public void setLayout(float layoutWidth, float layoutHeight) {
        super.setLayout(layoutWidth, layoutHeight);
        updateActionPosition();
    }

    @Override
    protected boolean isLineWrapReallyEnabled() {
        return false;
    }

    RoundRectangle textClip = new RoundRectangle();

    @Override
    public void onDraw(Graphics graphics) {
        if (discardDraw(graphics)) return;

        drawBackground(graphics);
        drawRipple(graphics);

        if (getOutWidth() <= 0 || getOutHeight() <= 0) return;

        float titleHeight = hasTitle() ? getTitleHeight() + getTitleSpacing() : 0;

        float x = getInX();
        float y = getInY();
        float width = getInWidth();
        float height = getInHeight();
        float fieldHeight = Math.max(0, getInHeight() - titleHeight);

        float iw = getLayoutIconWidth();
        float ih = getLayoutIconHeight();

        if (iw > 0 && ih > 0 && getIcon() != null) {
            iw = Math.min(width, iw);
            ih = Math.min(fieldHeight, ih);
            float xpos = x;
            float ypos = yOff(y, y + height, ih);

            getIcon().draw(graphics, xpos, ypos, iw, ih, getIconColor(), getIconImageFilter());
        }

        if (iw > 0) {
            x += iw + getIconSpacing();
            width = Math.max(0, width - iw - getIconSpacing());
        }

        float aiw = Math.min(getLayoutActionIconWidth(), width);
        float aih = Math.min(getLayoutActionIconHeight(), height);

        if (aiw > getDecreaseIconSpacing() && aih > 0 && (getIncreaseIconWidth() > 0 || getDecreaseIconWidth() > 0) && hasActionIcons()) {
            aiw = Math.min(width, aiw);
            aih = Math.min(height, aih);
            float space = Math.max(0, aiw - getDecreaseIconSpacing());
            float icw = getIncreaseIconWidth() * space / (getIncreaseIconWidth() + getDecreaseIconWidth());
            float dcw = getDecreaseIconWidth() * space / (getIncreaseIconWidth() + getDecreaseIconWidth());
            float ich = Math.min(aih, getIncreaseIconHeight());
            float dch = Math.min(aih, getDecreaseIconHeight());

            float xpos1 = x + width - aiw;
            float ypos1 = yOff(y, y + height, ich);
            float xpos2 = xpos1 + icw + getDecreaseIconSpacing();
            float ypos2 = yOff(y, y + height, dch);

            if (getIncreaseIcon() != null) {
                int col = pressOnAction == 1 || pressOnAction == 0 && hoverOnAction == 1 ? getActionIconColor() : getIncreaseIconColor();
                getIncreaseIcon().draw(graphics, xpos1, ypos1, icw, ich, col, getIncreaseIconImageFilter());
            }
            if (getDecreaseIcon() != null) {
                int col = pressOnAction == 2 || pressOnAction == 0 && hoverOnAction == 2 ? getActionIconColor() : getDecreaseIconColor();
                getDecreaseIcon().draw(graphics, xpos2, ypos2, dcw, dch, col, getDecreaseIconImageFilter());
            }
        }

        if (aiw > 0) {
            width = Math.max(0, width - aiw - getActionIconSpacing());
        }

        if (isHorizontalDimensionScroll() || isVerticalDimensionScroll()) {
            float off = getPaddingTop() + titleHeight;
            textClip.set(getOutX(), getOutY(), getOutWidth(), getOutHeight(),
                    getRadiusTop(), getRadiusRight(), getRadiusBottom(), getRadiusLeft());
            if (hasTitle()) {
                textClip.y += getPaddingTop() + titleHeight;
                textClip.height -= getPaddingTop() + titleHeight;
                textClip.arcTop = 0;
                textClip.arcRight = 0;
            }
            if (getIcon() != null) {
                textClip.x += iw + getIconSpacing() + getPaddingLeft();
                textClip.width -= iw + getIconSpacing() + getPaddingLeft();
                textClip.arcTop = 0;
                textClip.arcLeft = 0;
            }
            if (hasActionIcons()) {
                textClip.width -= aiw + getActionIconSpacing() + getPaddingRight();
                textClip.arcRight = 0;
                textClip.arcBottom = 0;
            }

            if (textClip.width > 0 && textClip.height > 0) {
                graphics.pushClip(textClip);
                onDrawText(graphics, x, y + titleHeight, width, Math.max(0, getInHeight() - titleHeight));
                graphics.popClip();
            }

        } else {
            onDrawText(graphics, x, y + titleHeight, width, Math.max(0, getInHeight() - titleHeight));
        }

        onDrawTitle(graphics, x, y, width, height);
        onDrawTextDivider(graphics, getOutX(), getOutY() + getOutHeight(), getOutWidth(), getTextDividerSize());

        if (getHorizontalBar() != null && isHorizontalVisible()) {
            getHorizontalBar().onDraw(graphics);
        }

        if (getVerticalBar() != null && isVerticalVisible()) {
            getVerticalBar().onDraw(graphics);
        }

    }

    private void updateActionPosition() {
        float titleSize = hasTitle() ? getTitleHeight() + getTitleSpacing() : 0;
        float x = getInX();
        float y = getInY() + titleSize;
        float width = getInWidth();
        float height = getInHeight() - titleSize;

        float iaw = Math.min(width, getLayoutActionIconWidth());
        float iah = Math.min(height, getLayoutActionIconHeight());

        x1 = x + width - iaw;
        x2 = x + width;
        y1 = yOff(y, y + height, iah);
        y2 = yOff(y, y + height, iah) + iah;
    }

    @Override
    public void hover(HoverEvent event) {
        super.hover(event);
        if (!event.isConsumed() && event.getType() == HoverEvent.MOVED) {
            int act = getCurrentActionButton(screenToLocal(event.getX(), event.getY()));
            if (hoverOnAction != act) {
                hoverOnAction = act;
                setUndefined(act != 0);
                invalidate(false);
            }
        }
        if (!event.isConsumed() && event.getType() == HoverEvent.EXITED) {
            if (hoverOnAction != 0) {
                hoverOnAction = 0;
                setUndefined(false);
                invalidate(false);
            }
        }
    }

    @Override
    public void pointer(PointerEvent event) {
        UXListener.safeHandle(getPointerListener(), event);
        if (!event.isConsumed() && event.getPointerID() == 1 && event.getType() == PointerEvent.PRESSED) {
            int act = getCurrentActionButton(screenToLocal(event.getX(), event.getY()));
            if (act != 0) {
                pressOnAction = act;
                invalidate(false);
            }
        }
        if (pressOnAction != 0) {
            event.consume();
        }
        if (pressOnAction != 0 && event.getPointerID() == 1 && event.getType() == PointerEvent.RELEASED) {
            if (pressOnAction == 1) {
                pressOnAction = 0;
                increase();
            } else if (pressOnAction == 2) {
                pressOnAction = 0;
                decrease();
            }
            invalidate(false);
        }
        if (pressOnAction == 0) {
            Vector2 point = screenToLocal(event.getX(), event.getY());
            point.x += getViewOffsetX();
            point.y += getViewOffsetY();
            textPointer(event, point);
        }
    }

    @Override
    public void fireRipple(float x, float y) {
        int act = getCurrentActionButton(screenToLocal(x, y));
        if (act != 0) {
            if (isRippleEnabled()) {
                var ripple = getRipple();
                ripple.setSize(Math.max(Math.abs(x1 - x2), Math.abs(y1 - y2)) * 0.5f);
                ripple.fire((x1 + x2) / 2f, (y1 + y2) / 2f);
            }
        } else {
            super.fireRipple(x, y);
        }
    }

    @Override
    protected float getVisibleTextX() {
        float iw = getLayoutIconWidth();
        return getInX() + (iw > 0 ? iw + getIconSpacing() : 0);
    }

    @Override
    protected float getVisibleTextWidth() {
        float iw = getLayoutIconWidth();
        float aiw = getLayoutActionIconWidth();
        return getInWidth() - (iw > 0 ? iw + getIconSpacing() : 0) - (aiw <= 0 ? 0 : aiw + getActionIconSpacing());
    }

    @Override
    protected void textPointer(PointerEvent event, Vector2 point) {
        if (getCurrentActionButton(screenToLocal(event.getX(), event.getY())) == 0) {
            super.textPointer(event, point);
        }
    }

    @Override
    protected void editText(Caret first, Caret second, String input) {
        if (!isEditable()) return;
        if (first.getOffset() == 0 && input.startsWith("-")) {
            super.editText(first, second, "-" + input.substring(1).replaceAll("\\D", ""));
        } else {
            super.editText(first, second, input.replaceAll("\\D", ""));
        }
    }

    @Override
    public void setText(String input) {
        if (input != null && input.startsWith("-")) {
            super.setText("-" + input.substring(1).replaceAll("\\D", ""));
        } else {
            super.setText(input == null ? null : input.replaceAll("\\D", ""));
        }
    }

    public long getNumber() {
        String text = getText();
        if (text == null) {
            return Math.max(minValue, Math.min(maxValue, 0));
        }
        try {
            return Math.max(minValue, Math.min(maxValue, Long.parseLong(text)));
        } catch (Exception e) {
            return Math.max(minValue, Math.min(maxValue, 0));
        }
    }

    public void setNumber(long number) {
        number = Math.max(minValue, Math.min(maxValue, number));
        setText(String.valueOf(number));
    }

    public void increase() {
        setNumber(getNumber() + 1);
        fireActionIncrease();
        fireTextType();
    }

    public void decrease() {
        setNumber(getNumber() - 1);
        fireActionDecrease();
        fireTextType();
    }

    @Override
    protected void setCaretHidden() {
        super.setCaretHidden();
        setNumber(getNumber());
    }

    private void fireActionIncrease() {
        if (actionIncreaseListener != null) {
            UXListener.safeHandle(actionIncreaseListener, new ActionEvent(this));
        }
    }

    public UXListener<ActionEvent> getActionIncreaseListener() {
        return actionIncreaseListener;
    }

    public void setActionIncreaseListener(UXListener<ActionEvent> actionIncreaseListener) {
        this.actionIncreaseListener = actionIncreaseListener;
    }

    private void fireActionDecrease() {
        if (actionDecreaseListener != null) {
            UXListener.safeHandle(actionDecreaseListener, new ActionEvent(this));
        }
    }

    public UXListener<ActionEvent> getActionDecreaseListener() {
        return actionDecreaseListener;
    }

    public void setActionDecreaseListener(UXListener<ActionEvent> actionDecreaseListener) {
        this.actionDecreaseListener = actionDecreaseListener;
    }

    private int getCurrentActionButton(Vector2 local) {
        if (hasActionIcons()) {
            if (local.x < x1 || local.x > x2 || local.y < y1 || local.y > y2) {
                return 0;
            }
            float iaw = x2 - x1;
            float sum = getIncreaseIconWidth() * iaw / (getDecreaseIconWidth() + getIncreaseIconWidth());
            return local.x <= x1 + sum ? 1 : 2;
        }
        return 0;
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

    private float getLayoutIconWidth() {
        return icon == null ? 0 : iconWidth == 0 || iconWidth == MATCH_PARENT ? getLineHeight() : iconWidth;
    }

    public float getIconHeight() {
        return iconHeight;
    }

    public void setIconHeight(float iconHeight) {
        if (this.iconHeight != iconHeight) {
            this.iconHeight = iconHeight;
            invalidate(false);
        }
    }

    private float getLayoutIconHeight() {
        return icon == null ? 0 : iconHeight == 0 || iconHeight == MATCH_PARENT ? getLineHeight() : iconHeight;
    }

    public float getIconSpacing() {
        return iconSpacing;
    }

    public void setIconSpacing(float iconSpacing) {
        if (this.iconSpacing != iconSpacing) {
            this.iconSpacing = iconSpacing;
            invalidate(isWrapContent());
        }
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

    public int getIncreaseIconColor() {
        return increaseIconColor;
    }

    public void setIncreaseIconColor(int increaseIconColor) {
        if (this.increaseIconColor != increaseIconColor) {
            this.increaseIconColor = increaseIconColor;
            invalidate(false);
        }
    }

    public Drawable getIncreaseIcon() {
        return increaseIcon;
    }

    public void setIncreaseIcon(Drawable increaseIcon) {
        if (this.increaseIcon != increaseIcon) {
            this.increaseIcon = increaseIcon;
            invalidate(isWrapContent());
        }
    }

    public float getIncreaseIconSpacing() {
        return increaseIconSpacing;
    }

    public void setIncreaseIconSpacing(float increaseIconSpacing) {
        if (this.increaseIconSpacing != increaseIconSpacing) {
            this.increaseIconSpacing = increaseIconSpacing;
            invalidate(isWrapContent());
        }
    }

    public float getIncreaseIconWidth() {
        return increaseIconWidth;
    }

    public void setIncreaseIconWidth(float increaseIconWidth) {
        if (this.increaseIconWidth != increaseIconWidth) {
            this.increaseIconWidth = increaseIconWidth;
            invalidate(isWrapContent());
        }
    }

    private float getLayoutIncreaseIconWidth() {
        if (increaseIcon == null) {
            return 0;
        }
        return increaseIconWidth == 0 || increaseIconWidth == MATCH_PARENT ? getLineHeight() : increaseIconWidth;
    }

    public float getIncreaseIconHeight() {
        return increaseIconHeight;
    }

    public void setIncreaseIconHeight(float increaseIconHeight) {
        if (this.increaseIconHeight != increaseIconHeight) {
            this.increaseIconHeight = increaseIconHeight;
            invalidate(isWrapContent());
        }
    }

    private float getLayoutIncreaseIconHeight() {
        if (increaseIcon == null) {
            return 0;
        }
        return increaseIconHeight == 0 || increaseIconHeight == MATCH_PARENT ? getLineHeight() : increaseIconHeight;
    }

    public ImageFilter getIncreaseIconImageFilter() {
        return increaseIconImageFilter;
    }

    public void setIncreaseIconImageFilter(ImageFilter increaseIconImageFilter) {
        if (increaseIconImageFilter == null) increaseIconImageFilter = ImageFilter.LINEAR;

        if (this.increaseIconImageFilter != increaseIconImageFilter) {
            this.increaseIconImageFilter = increaseIconImageFilter;
            invalidate(false);
        }
    }

    public int getDecreaseIconColor() {
        return decreaseIconColor;
    }

    public void setDecreaseIconColor(int decreaseIconColor) {
        if (this.decreaseIconColor != decreaseIconColor) {
            this.decreaseIconColor = decreaseIconColor;
            invalidate(false);
        }
    }

    public Drawable getDecreaseIcon() {
        return decreaseIcon;
    }

    public void setDecreaseIcon(Drawable decreaseIcon) {
        if (this.decreaseIcon != decreaseIcon) {
            this.decreaseIcon = decreaseIcon;
            invalidate(isWrapContent());
        }
    }

    public float getDecreaseIconSpacing() {
        return decreaseIconSpacing;
    }

    public void setDecreaseIconSpacing(float decreaseIconSpacing) {
        if (this.decreaseIconSpacing != decreaseIconSpacing) {
            this.decreaseIconSpacing = decreaseIconSpacing;
            invalidate(isWrapContent());
        }
    }

    public float getDecreaseIconWidth() {
        return decreaseIconWidth;
    }

    public void setDecreaseIconWidth(float decreaseIconWidth) {
        if (this.decreaseIconWidth != decreaseIconWidth) {
            this.decreaseIconWidth = decreaseIconWidth;
            invalidate(isWrapContent());
        }
    }

    private float getLayoutDecreaseIconWidth() {
        if (decreaseIcon == null) {
            return 0;
        }
        return decreaseIconWidth == 0 || decreaseIconWidth == MATCH_PARENT ? getLineHeight() : decreaseIconWidth;
    }

    public float getDecreaseIconHeight() {
        return decreaseIconHeight;
    }

    public void setDecreaseIconHeight(float decreaseIconHeight) {
        if (this.decreaseIconHeight != decreaseIconHeight) {
            this.decreaseIconHeight = decreaseIconHeight;
            invalidate(isWrapContent());
        }
    }

    private float getLayoutDecreaseIconHeight() {
        if (decreaseIcon == null) {
            return 0;
        }
        return decreaseIconHeight == 0 || decreaseIconHeight == MATCH_PARENT ? getLineHeight() : decreaseIconHeight;
    }

    public ImageFilter getDecreaseIconImageFilter() {
        return decreaseIconImageFilter;
    }

    public void setDecreaseIconImageFilter(ImageFilter decreaseIconImageFilter) {
        if (decreaseIconImageFilter == null) decreaseIconImageFilter = ImageFilter.LINEAR;

        if (this.decreaseIconImageFilter != decreaseIconImageFilter) {
            this.decreaseIconImageFilter = decreaseIconImageFilter;
            invalidate(false);
        }
    }

    public boolean hasActionIcons() {
        return getDecreaseIcon() != null || getIncreaseIcon() != null;
    }

    public float getActionIconSpacing() {
        return getIncreaseIconSpacing();
    }
    
    public float getLayoutActionIconWidth() {
        return getLayoutIncreaseIconWidth() + getLayoutDecreaseIconWidth() + getDecreaseIconSpacing();
    }

    public float getLayoutActionIconHeight() {
        return Math.max(getLayoutIncreaseIconHeight(), getLayoutDecreaseIconHeight());
    }

    public int getActionIconColor() {
        return actionIconColor;
    }

    public void setActionIconColor(int actionIconColor) {
        if (this.actionIconColor != actionIconColor) {
            this.actionIconColor = actionIconColor;
            invalidate(false);
        }
    }

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public void setRangeLimits(int minValue, int maxValue) {
        int cMinValue = Math.min(minValue, maxValue);
        int cMaxValue = Math.max(minValue, maxValue);
        if (cMinValue != this.minValue || cMaxValue != this.maxValue) {
            this.minValue = cMinValue;
            this.maxValue = cMaxValue;
            setNumber(getNumber());
        }
    }
}
