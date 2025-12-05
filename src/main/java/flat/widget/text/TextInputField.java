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

public class TextInputField extends TextField {

    private UXListener<ActionEvent> actionListener;

    private Drawable icon;
    private int iconColor = Color.white;
    private ImageFilter iconImageFilter = ImageFilter.LINEAR;
    private float iconSpacing;
    private float iconWidth;
    private float iconHeight;

    private Drawable actionIcon;
    private int actionIconColor = Color.white;
    private ImageFilter actionIconImageFilter = ImageFilter.LINEAR;
    private float actionIconSpacing;
    private float actionIconWidth;
    private float actionIconHeight;

    private float x1, y1, x2, y2;
    private boolean pressOnAction;

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);
        UXAttrs attrs = getAttrs();

        setActionListener(attrs.getAttributeListener("on-action", ActionEvent.class, controller, getActionListener()));
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

        setActionIcon(attrs.getDrawable("action-icon", info, getActionIcon(), false));
        setActionIconColor(attrs.getColor("action-icon-color", info, getActionIconColor()));
        setActionIconWidth(attrs.getSize("action-icon-width", info, getActionIconWidth()));
        setActionIconHeight(attrs.getSize("action-icon-height", info, getActionIconHeight()));
        setActionIconSpacing(attrs.getSize("action-icon-spacing", info, getActionIconSpacing()));
        setActionIconImageFilter(attrs.getConstant("action-icon-image-filter", info, getActionIconImageFilter()));
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

        float aiw = getLayoutActionIconWidth();
        float aih = getLayoutActionIconHeight();

        if (aiw > 0 && aih > 0 && getActionIcon() != null) {
            aiw = Math.min(width, aiw);
            aih = Math.min(height, aih);
            float xpos = x + width - aiw;
            float ypos = yOff(y, y + height, aih);

            getActionIcon().draw(graphics, xpos, ypos, aiw, aih, getActionIconColor(), getActionIconImageFilter());
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
            if (getActionIcon() != null) {
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
            setUndefined(isOverActionButton(screenToLocal(event.getX(), event.getY())));
        }
        if (!event.isConsumed() && event.getType() == HoverEvent.EXITED) {
            setUndefined(false);
        }
    }

    @Override
    public void pointer(PointerEvent event) {
        UXListener.safeHandle(getPointerListener(), event);
        if (!event.isConsumed() && event.getPointerID() == 1 && event.getType() == PointerEvent.PRESSED) {
            if (isOverActionButton(screenToLocal(event.getX(), event.getY()))) {
                pressOnAction = true;
            }
        }
        if (pressOnAction) {
            event.consume();
        }
        if (pressOnAction && event.getPointerID() == 1 && event.getType() == PointerEvent.RELEASED) {
            pressOnAction = false;
            action();
        }
        if (!pressOnAction) {
            Vector2 point = screenToLocal(event.getX(), event.getY());
            textPointer(event, point);
        }
    }

    @Override
    public void fireRipple(float x, float y) {
        if (isOverActionButton(screenToLocal(x, y))) {
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
        if (!isOverActionButton(screenToLocal(event.getX(), event.getY()))) {
            super.textPointer(event, point);
        }
    }

    public void action() {
        fireAction();
    }

    private void fireAction() {
        if (actionListener != null) {
            UXListener.safeHandle(actionListener, new ActionEvent(this));
        }
    }

    public UXListener<ActionEvent> getActionListener() {
        return actionListener;
    }

    public void setActionListener(UXListener<ActionEvent> actionListener) {
        this.actionListener = actionListener;
    }

    protected boolean isOverActionButton(Vector2 local) {
        return actionIcon != null && !(local.x < x1 || local.x > x2 || local.y < y1 || local.y > y2);
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

    public int getActionIconColor() {
        return actionIconColor;
    }

    public void setActionIconColor(int actionIconColor) {
        if (this.actionIconColor != actionIconColor) {
            this.actionIconColor = actionIconColor;
            invalidate(false);
        }
    }

    public Drawable getActionIcon() {
        return actionIcon;
    }

    public void setActionIcon(Drawable actionIcon) {
        if (this.actionIcon != actionIcon) {
            this.actionIcon = actionIcon;
            invalidate(isWrapContent());
        }
    }

    public float getActionIconSpacing() {
        return actionIconSpacing;
    }

    public void setActionIconSpacing(float actionIconSpacing) {
        if (this.actionIconSpacing != actionIconSpacing) {
            this.actionIconSpacing = actionIconSpacing;
            invalidate(isWrapContent());
        }
    }

    public float getActionIconWidth() {
        return actionIconWidth;
    }

    public void setActionIconWidth(float actionIconWidth) {
        if (this.actionIconWidth != actionIconWidth) {
            this.actionIconWidth = actionIconWidth;
            invalidate(isWrapContent());
        }
    }

    private float getLayoutActionIconWidth() {
        if (actionIcon == null) {
            return 0;
        }
        return actionIconWidth == 0 || actionIconWidth == MATCH_PARENT ? getLineHeight() : actionIconWidth;
    }

    public float getActionIconHeight() {
        return actionIconHeight;
    }

    public void setActionIconHeight(float actionIconHeight) {
        if (this.actionIconHeight != actionIconHeight) {
            this.actionIconHeight = actionIconHeight;
            invalidate(isWrapContent());
        }
    }

    private float getLayoutActionIconHeight() {
        if (actionIcon == null) {
            return 0;
        }
        return actionIconHeight == 0 || actionIconHeight == MATCH_PARENT ? getLineHeight() : actionIconHeight;
    }

    public ImageFilter getActionIconImageFilter() {
        return actionIconImageFilter;
    }

    public void setActionIconImageFilter(ImageFilter actionIconImageFilter) {
        if (actionIconImageFilter == null) actionIconImageFilter = ImageFilter.LINEAR;

        if (this.actionIconImageFilter != actionIconImageFilter) {
            this.actionIconImageFilter = actionIconImageFilter;
            invalidate(false);
        }
    }
}
