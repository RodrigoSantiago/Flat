package flat.widget.structure;

import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.PointerEvent;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.graphics.image.Drawable;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.uxml.UXListener;
import flat.widget.Widget;
import flat.widget.enums.ImageFilter;

import java.util.Objects;

public class ToolItem extends Widget {
    private UXListener<ActionEvent> actionListener;

    private Drawable icon;
    private int iconColor = Color.white;
    private float iconWidth;
    private float iconHeight;
    private ImageFilter iconImageFilter = ImageFilter.LINEAR;

    private String menuText;
    private String menuShortcutText;

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);

        UXAttrs attrs = getAttrs();
        setActionListener(attrs.getAttributeListener("on-action", ActionEvent.class, controller));
        setMenuText(attrs.getAttributeString("menu-text", getMenuText()));
        setMenuShortcutText(attrs.getAttributeString("menu-shortcut-text", getMenuShortcutText()));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();

        setIcon(attrs.getResourceAsDrawable("icon", info, getIcon(), false));
        setIconColor(attrs.getColor("icon-color", info, getIconColor()));
        setIconImageFilter(attrs.getConstant("icon-image-filter", info, getIconImageFilter()));
        setIconWidth(attrs.getSize("icon-width", info, getIconWidth()));
        setIconHeight(attrs.getSize("icon-height", info, getIconHeight()));
    }

    @Override
    public void onMeasure() {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        float mWidth;
        float mHeight;
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;

        float iW = getLayoutIconWidth();
        float iH = getLayoutIconHeight();

        if (wrapWidth) {
            mWidth = Math.max(extraWidth + iW, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            mHeight = Math.max(extraHeight + iH, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public void onDraw(Graphics graphics) {
        drawBackground(graphics);
        drawRipple(graphics);

        float x = getInX();
        float y = getInY();
        float width = getInWidth();
        float height = getInHeight();

        if (width <= 0 || height <= 0 || getIcon() == null) return;

        float iW = Math.min(getLayoutIconWidth(), width);
        float iH = Math.min(getLayoutIconHeight(), height);
        if (iW > 0 && iH > 0 && getIcon() != null) {
            graphics.setTransform2D(getTransform());
            getIcon().draw(graphics, x, y, iW, iH, getIconColor(), getIconImageFilter());
        }
    }

    @Override
    public void pointer(PointerEvent event) {
        super.pointer(event);
        if (!event.isConsumed() && event.getPointerID() == 1 && event.getType() == PointerEvent.RELEASED) {
            action();
        }
    }

    public UXListener<ActionEvent> getActionListener() {
        return actionListener;
    }

    public void setActionListener(UXListener<ActionEvent> actionListener) {
        this.actionListener = actionListener;
    }

    private void fireAction() {
        if (actionListener != null) {
            UXListener.safeHandle(actionListener, new ActionEvent(this));
        }
    }

    public void action() {
        fireAction();
    }

    public String getMenuText() {
        return menuText;
    }

    public void setMenuText(String menuText) {
        if (!Objects.equals(this.menuText, menuText)) {
            this.menuText = menuText;
            invalidateMenu();
        }
    }

    public String getMenuShortcutText() {
        return menuShortcutText;
    }

    public void setMenuShortcutText(String menuShortcutText) {
        if (!Objects.equals(this.menuShortcutText, menuShortcutText)) {
            this.menuShortcutText = menuShortcutText;
            invalidateMenu();
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
        return iconWidth == 0 && icon != null ? icon.getWidth() : iconWidth;
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
        return iconHeight == 0 && icon != null ? icon.getHeight() : iconHeight;
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

    protected void invalidateMenu() {
        if (getParent() instanceof ToolBar toolBar) {
            toolBar.invalidateToolItem(this);
        }
    }
}