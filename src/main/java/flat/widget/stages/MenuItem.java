package flat.widget.stages;

import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.HoverEvent;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.graphics.context.Font;
import flat.graphics.image.Drawable;
import flat.math.Vector2;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.uxml.UXListener;
import flat.widget.enums.DropdownAlign;
import flat.widget.enums.HorizontalPosition;
import flat.widget.enums.ImageFilter;
import flat.widget.text.Button;
import flat.widget.text.data.TextRender;
import flat.window.Activity;

import java.util.Objects;

public class MenuItem extends Button {

    private String shortcutText;
    private Font shortcutTextFont = Font.getDefault();
    private int shortcutTextColor = Color.black;
    private float shortcutTextSize = 16f;
    private float shortcutSpacing;

    private boolean invalidShortcutTextSize;
    private float textWidth;
    private float textHeight;

    private Drawable submenuIcon;
    private ImageFilter submenuImageFilter = ImageFilter.LINEAR;
    private int submenuColor = Color.black;
    private final TextRender shortcutRender = new TextRender();

    public MenuItem() {
        shortcutRender.setFont(shortcutTextFont);
        shortcutRender.setTextSize(shortcutTextSize);
        invalidateShortcutTextSize();
    }

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);

        UXAttrs attrs = getAttrs();
        setShortcutText(attrs.getAttributeString("shortcut-text", getShortcutText()));
    }


    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();

        setShortcutSpacing(attrs.getSize("shortcut-spacing", info, getShortcutSpacing()));
        setShortcutTextColor(attrs.getColor("shortcut-text-color", info, getShortcutTextColor()));
        setShortcutTextSize(attrs.getSize("shortcut-text-size", info, getShortcutTextSize()));
        setShortcutTextFont(attrs.getFont("shortcut-text-font", info, getShortcutTextFont()));
        setSubmenuIcon(attrs.getResourceAsDrawable("submenu-icon", info, getSubmenuIcon(), false));
        setSubmenuColor(attrs.getColor("submenu-color", info, getSubmenuColor()));
        setSubmenuImageFilter(attrs.getConstant("submenu-image-filter", info, getSubmenuImageFilter()));
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
        float ciW = getShortcutTextWidth();
        float ciH = getShortcutTextHeight();

        if (wrapWidth) {
            mWidth = Math.max(getTextWidth() + extraWidth
                    + (iW > 0 ? iW + getIconSpacing() : 0)
                    + (ciW > 0 ? ciW + getShortcutSpacing() : 0), getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            mHeight = Math.max(Math.max(getTextHeight(), Math.max(iH, ciH)) + extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public void onDraw(Graphics graphics) {
        drawBackground(graphics);
        drawRipple(graphics);

        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        if (width <= 0 || height <= 0) return;

        graphics.setTransform2D(getTransform());

        float iW = getLayoutIconWidth();
        float iH = getLayoutIconHeight();
        float ciW = getShortcutTextWidth();
        float ciH = getShortcutTextHeight();
        float scImg = getTextSize();

        float spaceForIcon = (iW > 0 ? iW + getIconSpacing() : 0);
        float spaceForShortcutIcon = getContextMenu() != null ? scImg : (ciW > 0 ? ciW + getShortcutSpacing() : 0);
        float spaceForText = getTextWidth();

        if (spaceForIcon + spaceForShortcutIcon + spaceForText > width) {
            if (spaceForShortcutIcon > width) {
                spaceForIcon = 0;
                spaceForText = 0;
                spaceForShortcutIcon = width;
            } else if (spaceForIcon + spaceForShortcutIcon > width) {
                spaceForText = 0;
                spaceForIcon = width - spaceForShortcutIcon;
            } else {
                spaceForText = width - spaceForShortcutIcon - spaceForIcon;
            }
        }

        float tw = spaceForText;
        float th = Math.min(height, getTextHeight());
        float iw = Math.min(iW, Math.max(spaceForIcon, 0));
        float ih = Math.min(height, iH);
        float ciw = Math.min(ciW, Math.max(spaceForShortcutIcon, 0));
        float cih = Math.min(height, ciH);

        boolean iconLeft = getIconPosition() == HorizontalPosition.LEFT;
        float boxX = xOff(x, x + width - spaceForShortcutIcon, spaceForIcon + spaceForText);

        if (iw > 0 && ih > 0 && getIcon() != null) {
            float xpos = iconLeft ? boxX : spaceForShortcutIcon + boxX + spaceForText + spaceForIcon - iw;
            float ypos = yOff(y, y + height, ih);
            drawIcon(graphics, xpos, ypos, iw, ih);
        }

        if (tw > 0 && th > 0) {
            float xpos = iconLeft ? boxX + spaceForIcon : spaceForShortcutIcon + boxX;
            float ypos = yOff(y, y + height, th);
            drawText(graphics, xpos, ypos, tw, th);
        }

        if (getContextMenu() != null && spaceForShortcutIcon > 0 && getSubmenuIcon() != null) {
            float xpos = iconLeft ? x + width - spaceForShortcutIcon : x;
            float yoff = yOff(y, y + height, spaceForShortcutIcon);
            graphics.setColor(getSubmenuColor());
            getSubmenuIcon().draw(graphics, xpos, yoff, spaceForShortcutIcon, spaceForShortcutIcon, getSubmenuColor(), getSubmenuImageFilter());

        } else if (ciw > 0 && cih > 0 && getShortcutText() != null) {
            float xpos = iconLeft ? x + width - ciw : x;
            float ypos = yOff(y, y + height, cih);

            drawShortcutText(graphics, xpos, ypos, ciw, cih);
        }
    }

    protected void drawShortcutText(Graphics context, float x, float y, float width, float height) {
        if (getShortcutTextFont() != null && getShortcutTextSize() > 0 && Color.getAlpha(getShortcutTextColor()) > 0) {
            context.setTransform2D(getTransform());
            context.setColor(getShortcutTextColor());
            context.setTextFont(getShortcutTextFont());
            context.setTextSize(getShortcutTextSize());
            context.setTextBlur(0);

            shortcutRender.drawText(context, x, y, width, height, getHorizontalAlign());
        }
    }

    @Override
    public void hover(HoverEvent event) {
        super.hover(event);
        Activity act = getActivity();
        if (act != null) {
            if (event.getType() == HoverEvent.ENTERED) {
                if (getContextMenu() != null) {
                    if (!getContextMenu().isShown()) {
                        showContextMenu();
                    }
                } else {
                    hideSiblingSubMenu();
                }
            }
        }
    }

    @Override
    public void action() {
        if (getContextMenu() != null && !getContextMenu().isShown()) {
            showContextMenu();
        }
        if (getActionListener() != null) {
            ActionEvent event = new ActionEvent(this);
            UXListener.safeHandle(getActionListener(), event);
            if (getContextMenu() == null && !event.isConsumed() && getParent() instanceof Menu menu) {
                menu.hide();
            }
        } else {
            if (getContextMenu() == null && getParent() instanceof Menu menu) {
                menu.hide();
            }
        }
    }

    @Override
    public void showContextMenu(float x, float y) {
        showContextMenu();
    }

    public void showContextMenu() {
        Activity act = getActivity();
        if (act != null && getParent() instanceof Menu parentMenu) {
            if (getContextMenu() != null) {
                float x = getOutX();
                float y = getOutY();
                float width = getOutWidth();
                float height = getOutHeight();
                Vector2 screen1 = localToScreen(x, y);
                Vector2 screen2 = localToScreen(x + width, y + height);
                getContextMenu().show(parentMenu
                        , screen1.x, screen1.y
                        , screen2.x - screen1.x, screen2.y - screen1.y
                        , DropdownAlign.TOP_LEFT_ADAPTATIVE);
            }
        }
    }

    protected void hideSiblingSubMenu() {
        Activity act = getActivity();
        if (act != null && getParent() instanceof Menu parentMenu) {
            parentMenu.hideSubMenu();
        }
    }

    public String getShortcutText() {
        return shortcutText;
    }

    public void setShortcutText(String shortcutText) {
        if (!Objects.equals(this.shortcutText, shortcutText)) {
            String old = this.shortcutText;
            this.shortcutText = shortcutText;
            shortcutRender.setText(shortcutText);
            invalidateShortcutTextSize();
        }
    }

    public Font getShortcutTextFont() {
        return shortcutTextFont;
    }

    public void setShortcutTextFont(Font shortcutTextFont) {
        if (this.shortcutTextFont != shortcutTextFont) {
            this.shortcutTextFont = shortcutTextFont;
            shortcutRender.setFont(shortcutTextFont);
            invalidateShortcutTextSize();
        }
    }

    public int getShortcutTextColor() {
        return shortcutTextColor;
    }

    public void setShortcutTextColor(int shortcutTextColor) {
        if (this.shortcutTextColor != shortcutTextColor) {
            this.shortcutTextColor = shortcutTextColor;
            invalidate(false);
        }
    }

    public float getShortcutTextSize() {
        return shortcutTextSize;
    }

    public void setShortcutTextSize(float shortcutTextSize) {
        if (this.shortcutTextSize != shortcutTextSize) {
            this.shortcutTextSize = shortcutTextSize;
            shortcutRender.setTextSize(shortcutTextSize);
            invalidateShortcutTextSize();
        }
    }

    public float getShortcutSpacing() {
        return shortcutSpacing;
    }

    public void setShortcutSpacing(float shortcutSpacing) {
        if (this.shortcutSpacing != shortcutSpacing) {
            this.shortcutSpacing = shortcutSpacing;
            invalidate(isWrapContent());
        }
    }

    public Drawable getSubmenuIcon() {
        return submenuIcon;
    }

    public void setSubmenuIcon(Drawable submenuIcon) {
        if (this.submenuIcon != submenuIcon) {
            this.submenuIcon = submenuIcon;
            invalidate(false);
        }
    }

    public int getSubmenuColor() {
        return submenuColor;
    }

    public void setSubmenuColor(int submenuColor) {
        if (this.submenuColor != submenuColor) {
            this.submenuColor = submenuColor;
            invalidate(false);
        }
    }

    public ImageFilter getSubmenuImageFilter() {
        return submenuImageFilter;
    }

    public void setSubmenuImageFilter(ImageFilter submenuImageFilter) {
        if (submenuImageFilter == null) submenuImageFilter = ImageFilter.LINEAR;

        if (this.submenuImageFilter != submenuImageFilter) {
            this.submenuImageFilter = submenuImageFilter;
            invalidate(false);
        }
    }

    @Override
    protected float getLayoutIconWidth() {
        return (getIconWidth() == 0 || getIconWidth() == MATCH_PARENT) && getIcon() != null ? getTextHeight() : getIconWidth();
    }

    @Override
    protected float getLayoutIconHeight() {
        return (getIconHeight() == 0 || getIconHeight() == MATCH_PARENT) && getIcon() != null ? getTextHeight() : getIconHeight();
    }
    
    protected void invalidateShortcutTextSize() {
        invalidShortcutTextSize = true;
        invalidate(isWrapContent());
    }

    protected float getShortcutTextWidth() {
        if (invalidShortcutTextSize) {
            invalidShortcutTextSize = false;
            textWidth = shortcutRender.getTextWidth();
        }
        return textWidth;
    }

    protected float getShortcutTextHeight() {
        return shortcutRender.getTextHeight();
    }
}
