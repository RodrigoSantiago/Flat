package flat.widget.text;

import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.HoverEvent;
import flat.events.PointerEvent;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.graphics.cursor.Cursor;
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
    private int actionIconBgColor = Color.transparent;
    private Cursor actionIconCursor = Cursor.UNSET;
    private boolean isHoveringAction;

    private float x1, y1, x2, y2;


    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);
        UXAttrs attrs = getAttrs();

        setActionListener(attrs.getAttributeListener("on-action", ActionEvent.class, controller));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();

        setIcon(attrs.getResourceAsDrawable("icon", info, getIcon(), false));
        setIconColor(attrs.getColor("icon-color", info, getIconColor()));
        setIconWidth(attrs.getSize("icon-width", info, getIconWidth()));
        setIconHeight(attrs.getSize("icon-height", info, getIconHeight()));
        setIconSpacing(attrs.getSize("icon-spacing", info, getIconSpacing()));
        setIconImageFilter(attrs.getConstant("icon-image-filter", info, getIconImageFilter()));

        setActionIcon(attrs.getResourceAsDrawable("action-icon", info, getActionIcon(), false));
        setActionIconColor(attrs.getColor("action-icon-color", info, getActionIconColor()));
        setActionIconBgColor(attrs.getColor("action-icon-bg-color", info, getActionIconBgColor()));
        setActionIconWidth(attrs.getSize("action-icon-width", info, getActionIconWidth()));
        setActionIconHeight(attrs.getSize("action-icon-height", info, getActionIconHeight()));
        setActionIconSpacing(attrs.getSize("action-icon-spacing", info, getActionIconSpacing()));
        setActionIconImageFilter(attrs.getConstant("action-icon-image-filter", info, getActionIconImageFilter()));
        setActionIconCursor(attrs.getConstant("action-icon-cursor", info, getActionIconCursor()));
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
            mWidth = Math.max(getTextWidth() + iw + iaw + extraWidth, getLayoutMinWidth());
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
        float iw = getLayoutIconWidth();
        float aiw = getLayoutActionIconWidth();
        float exWidth = (iw > 0 ? iw + getIconSpacing() : 0) + (aiw <= 0 ? 0 : aiw + getActionIconSpacing());

        Vector2 fromBase = super.onLayoutViewDimension(width, height);
        fromBase.x -= exWidth;
        return fromBase;
    }

    @Override
    public void onLayout(float width, float height) {
        super.onLayout(width, height);
        updateActionPosition();
    }

    @Override
    public void onDraw(Graphics graphics) {
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
            aih = Math.min(fieldHeight, aih);
            float xpos = x + width - aiw;
            float ypos = yOff(y, y + height, aih);

            if (isHoveringAction) {
                graphics.setColor(getActionIconBgColor());
                graphics.drawEllipse(xpos, ypos, aiw, aih, true);
            }
            getActionIcon().draw(graphics, xpos, ypos, aiw, aih, getActionIconColor(), getActionIconImageFilter());
        }

        if (aiw > 0) {
            width = Math.max(0, width - aiw - getActionIconSpacing());
        }

        if (isHorizontalDimensionScroll() || isVerticalDimensionScroll()) {
            float off = getPaddingTop() + titleHeight;
            RoundRectangle bg = getBackgroundShape();
            if (hasTitle()) {
                bg.y += getPaddingTop() + titleHeight;
                bg.height -= getPaddingTop() + titleHeight;
            }
            if (getIcon() != null) {
                bg.x += iw + getIconSpacing();
                bg.width -= iw + getIconSpacing();
            }
            if (getActionIcon() != null) {
                bg.width -= aiw + getActionIconSpacing();
            }

            if (bg.width > 0 && bg.height > 0) {
                graphics.pushClip(bg);
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
    public void fireHover(HoverEvent event) {
        super.fireHover(event);
        if (!event.isConsumed() && event.getType() == HoverEvent.MOVED) {
            if (isOverActionButton(screenToLocal(event.getX(), event.getY())) != isHoveringAction) {
                isHoveringAction = !isHoveringAction;
                invalidate(false);
            }
        }
        if (!event.isConsumed() && event.getType() == HoverEvent.EXITED) {
            isHoveringAction = false;
            invalidate(false);
        }
    }

    @Override
    public void firePointer(PointerEvent event) {
        super.firePointer(event);
        if (isHoveringAction && event.getPointerID() == 1
                && !event.isConsumed() && event.getType() == PointerEvent.RELEASED) {
            action();
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
        if (!isHoveringAction) {
            super.textPointer(event, point);
        }
    }

    @Override
    public Cursor getCurrentCursor() {
        return isHoveringAction && actionIconCursor != Cursor.UNSET ? actionIconCursor : super.getCursor();
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

    private boolean isOverActionButton(Vector2 local) {
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

    public int getActionIconBgColor() {
        return actionIconBgColor;
    }

    public void setActionIconBgColor(int actionIconBgColor) {
        if (this.actionIconBgColor != actionIconBgColor) {
            this.actionIconBgColor = actionIconBgColor;
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

    public Cursor getActionIconCursor() {
        return actionIconCursor;
    }

    public void setActionIconCursor(Cursor actionIconCursor) {
        if (actionIconCursor == null) actionIconCursor = Cursor.UNSET;

        this.actionIconCursor = actionIconCursor;
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
