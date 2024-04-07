package flat.widget.dialogs;

import flat.animations.StateInfo;
import flat.events.*;
import flat.graphics.SmartContext;
import flat.graphics.context.Font;
import flat.graphics.image.Drawable;
import flat.graphics.text.Align;
import flat.math.Vector2;
import flat.math.shapes.Shape;
import flat.resources.Resource;
import flat.uxml.Controller;
import flat.uxml.UXChildren;
import flat.uxml.UXStyleAttrs;
import flat.window.Activity;
import flat.widget.Menu;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.enuns.Visibility;

import java.lang.reflect.Method;
import java.util.Objects;

public class MenuItem extends Parent {

    private ActionListener actionListener;

    private String text;
    private boolean textAllCaps;

    private Font font = Font.DEFAULT;
    private float textSize;
    private int textColor;

    private Align.Vertical verticalAlign = Align.Vertical.TOP;
    private Align.Horizontal horizontalAlign = Align.Horizontal.LEFT;

    private String showText;
    private boolean invalidTextSize;
    private float textWidth;

    private Drawable iconImage;
    private float iconSpacing;

    private Drawable actionImage;
    private float actionSpacing;

    private Menu subMenu;

    @Override
    public void applyAttributes(UXStyleAttrs style, Controller controller) {
        super.applyAttributes(style, controller);

        setText(style.asString("text", getText()));

        Method handle = style.asListener("on-action", ActionEvent.class, controller);
        if (handle != null) {
            setActionListener(new ActionListener.AutoActionListener(controller, handle));
        }
    }

    @Override
    public void applyChildren(UXChildren children) {
        Menu menu = children.nextMenu();
        if (menu != null) {
            setSubMenu(menu);
        }
    }

    @Override
    public void applyStyle() {
        super.applyStyle();
        if (getStyle() == null) return;

        StateInfo info = getStateInfo();

        setFont(getStyle().asFont("font", info, getFont()));
        setTextSize(getStyle().asSize("text-size", info, getTextSize()));
        setTextColor(getStyle().asColor("text-color", info, getTextColor()));
        setTextAllCaps(getStyle().asBool("text-all-caps", info, isTextAllCaps()));

        setVerticalAlign(getStyle().asConstant("vertical-align", info, getVerticalAlign()));
        setHorizontalAlign(getStyle().asConstant("horizontal-align", info, getHorizontalAlign()));

        Resource res = getStyle().asResource("icon-image", info);
        if (res != null) {
            Drawable drawable = res.getDrawable();
            if (drawable != null) {
                setIconImage(drawable);
            }
        }

        setIconSpacing(getStyle().asSize("icon-spacing", info, getIconSpacing()));

        res = getStyle().asResource("action-image", info);
        if (res != null) {
            Drawable drawable = res.getDrawable();
            if (drawable != null) {
                setActionImage(drawable);
            }
        }
        setActionSpacing(getStyle().asSize("action-spacing", info, getIconSpacing()));
    }

    @Override
    protected void onActivityChange(Activity prev, Activity activity) {
        super.onActivityChange(prev, activity);
    }

    @Override
    public void onDraw(SmartContext context) {

        if (subMenu == null || desktop || !isActivated()) {
            backgroundDraw(getBackgroundColor(), getBorderColor(), getRippleColor(), context);

            final float x = getInX();
            final float y = getInY();
            final float width = getInWidth();
            final float height = getInHeight();

            context.setColor(getTextColor());
            context.setTextFont(getFont());
            context.setTextSize(getTextSize());
            context.setTextVerticalAlign(Align.Vertical.TOP);
            context.setTextHorizontalAlign(Align.Horizontal.LEFT);

            float is = iconSpacing + (iconImage != null ? iconImage.getWidth() : 0);
            float as = actionSpacing + (actionImage != null ? actionImage.getWidth() : 0);

            float tw = Math.min(getTextWidth() + (is + as), width);
            float xoff = xOff(x, x + width, tw);

            if (getShowText() != null && !getShowText().isEmpty()) {
                context.setTransform2D(getTransform());
                context.drawTextSlice(xoff + is,
                        yOff(y, y + height, getTextHeight()),
                        width - (is + as), getShowText());
            }

            if (iconImage != null) {
                context.setTransform2D(getTransform());
                iconImage.draw(context, xoff,
                        yOff(y, y + height, iconImage.getHeight()),
                        iconImage.getWidth(), iconImage.getHeight(), 0);
            }

            if (actionImage != null) {
                context.setTransform2D(getTransform());
                actionImage.draw(context, getInX() + getInWidth() - actionImage.getWidth(),
                        yOff(y, y + height, actionImage.getHeight()),
                        actionImage.getWidth(), actionImage.getHeight(), 0);
            }
        }
        if (subMenu != null && isActivated()) {
            Shape shape = context.getClip();
            context.clearClip();
            subMenu.onDraw(context);
            context.setTransform2D(null);
            context.setClip(shape);
        }
    }

    @Override
    public void onMeasure() {
        float is = iconSpacing + (iconImage != null ? iconImage.getWidth() : 0);
        float as = actionSpacing + (actionImage != null ? actionImage.getWidth() : 0);
        float h = Math.max(iconImage != null ? iconImage.getHeight() : 0, actionImage != null ? actionImage.getHeight() : 0);

        float mWidth = getPrefWidth();
        float mHeight = getPrefHeight();
        mWidth = mWidth == WRAP_CONTENT ? getTextWidth() + is + as : mWidth;
        mHeight = mHeight == WRAP_CONTENT ? Math.max(getTextHeight(), h) : mHeight;
        mWidth += getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        mHeight += getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();
        setMeasure(mWidth, mHeight);

        if (subMenu != null && isActivated()) {
            subMenu.onMeasure();
        }
    }

    @Override
    public void onLayout(float width, float height) {
        super.onLayout(width, height);
        if (subMenu != null && isActivated()) {
            subMenu.onLayout(Math.min(width, subMenu.mWidth()), Math.max(height, subMenu.mHeight()));

            if (desktop) {
                float x, y;
                Vector2 p = new Vector2(getOutX() + getOutWidth(), getOutY());
                localToScreen(p);
                if (p.x + subMenu.getWidth() > getActivity().getWidth()) {
                    x = getOutX() - subMenu.getWidth();
                } else {
                    x = getOutX() + getOutWidth();
                }
                if (p.y + subMenu.getHeight() > getActivity().getHeight()) {
                    y = screenToLocal(0, getActivity().getHeight() - subMenu.getHeight() - subMenu.getMarginBottom()).y;
                } else {
                    y = getOutY();
                }
                subMenu.setPosition(x, y);
            } else {
                subMenu.setPosition(-getX(), -getY());
            }
        }
    }

    @Override
    public void remove(Widget widget) {
        if (widget == subMenu) {
            subMenu = null;
        }
        super.remove(widget);
    }

    @Override
    public Widget findByPosition(float x, float y, boolean includeDisabled) {
        if ((includeDisabled || isEnabled()) &&
                (getVisibility() == Visibility.VISIBLE || getVisibility() == Visibility.INVISIBLE)) {

            if (subMenu != null && isActivated()) {
                Widget widget = subMenu.findByPosition(x, y, includeDisabled);
                if (widget != null) {
                    return widget;
                }
            }
            return isClickable() && contains(x, y) ? this : null;
        } else {
            return null;
        }
    }

    public static boolean desktop = true;

    private Menu getParentMenu() {
        Parent parent = getParent();
        while (parent != null) {
            if (parent instanceof Menu) {
                return (Menu) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    @Override
    public void firePointer(PointerEvent pointerEvent) {
        super.firePointer(pointerEvent);
        if (!pointerEvent.isConsumed() && pointerEvent.getType() == PointerEvent.RELEASED) {
            Vector2 point = new Vector2(pointerEvent.getX(), pointerEvent.getY());
            screenToLocal(point);

            if (pointerEvent.getSource() == this && !desktop && subMenu != null) {
                Menu parent = getParentMenu();
                if (parent != null) {
                    parent.setChooseItem(this);
                }
                invalidate(true);
            }
            fire();
        }
    }

    @Override
    public void fireHover(HoverEvent hoverEvent) {
        super.fireHover(hoverEvent);
        if (desktop) {
            if (hoverEvent.getType() == HoverEvent.ENTERED) {
                Menu parent = getParentMenu();
                if (parent != null) {
                    parent.setChooseItem(this);
                }
                invalidate(true);
            }
        }
    }

    @Override
    public void fireScroll(ScrollEvent scrollEvent) {
        if (subMenu != null && isActivated()) {
            scrollEvent.consume();
        }
        super.fireScroll(scrollEvent);
    }

    @Override
    public void setActivated(boolean actived) {
        super.setActivated(actived);
        if (!isActivated()) {
            if (subMenu != null) {
                subMenu.setChooseItem(null);
            }
        }
    }

    @Override
    public void setContextMenu(Menu contextMenu) {
        super.setContextMenu(null);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (!Objects.equals(this.text, text)) {
            this.text = text;
            showText = text == null ? null : textAllCaps ? text.toUpperCase() : text;
            invalidate(true);
            invalidateTextSize();
        }
    }

    public boolean isTextAllCaps() {
        return textAllCaps;
    }

    public void setTextAllCaps(boolean textAllCaps) {
        if (this.textAllCaps != textAllCaps) {
            this.textAllCaps = textAllCaps;
            showText = text == null ? null : textAllCaps ? text.toUpperCase() : text;
            invalidate(true);
            invalidateTextSize();
        }
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        if (this.font != font) {
            this.font = font;
            invalidate(true);
            invalidateTextSize();
        }
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        if (this.textSize != textSize) {
            this.textSize = textSize;
            invalidate(true);
            invalidateTextSize();
        }
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        if (this.textColor != textColor) {
            this.textColor = textColor;
            invalidate(false);
        }
    }

    private void invalidateTextSize() {
        invalidTextSize = true;
    }

    public Align.Vertical getVerticalAlign() {
        return verticalAlign;
    }

    public void setVerticalAlign(Align.Vertical verticalAlign) {
        if (verticalAlign == null) verticalAlign = Align.Vertical.TOP;

        if (this.verticalAlign != verticalAlign) {
            this.verticalAlign = verticalAlign;
            invalidate(false);
        }
    }

    public Align.Horizontal getHorizontalAlign() {
        return horizontalAlign;
    }

    public void setHorizontalAlign(Align.Horizontal horizontalAlign) {
        if (horizontalAlign == null) horizontalAlign = Align.Horizontal.LEFT;

        if (this.horizontalAlign != horizontalAlign) {
            this.horizontalAlign = horizontalAlign;
            invalidate(false);
        }
    }

    protected float getTextWidth() {
        if (invalidTextSize) {
            if (showText == null) {
                return textWidth = 0;
            }
            textWidth = font.getWidth(showText, textSize, 1);
            invalidTextSize = false;
        }
        return textWidth;
    }

    protected float getTextHeight() {
        return font.getHeight(textSize);
    }

    protected String getShowText() {
        return showText;
    }

    protected float xOff(float start, float end, float textWidth) {
        if (end < start) return (start + end) / 2f;
        if (horizontalAlign == Align.Horizontal.RIGHT) return end - textWidth;
        if (horizontalAlign == Align.Horizontal.CENTER) return (start + end - textWidth) / 2f;
        return start;
    }

    protected float yOff(float start, float end, float textHeight) {
        if (end < start) return (start + end) / 2f;
        if (verticalAlign == Align.Vertical.BOTTOM || verticalAlign == Align.Vertical.BASELINE) return end - textHeight;
        if (verticalAlign == Align.Vertical.MIDDLE) return (start + end - textHeight) / 2f;
        return start;
    }

    public ActionListener getActionListener() {
        return actionListener;
    }

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void fireAction(ActionEvent event) {
        if (actionListener != null) {
            actionListener.handle(event);
        }
    }

    public void fire() {
        fireAction(new ActionEvent(this));
    }

    public Drawable getIconImage() {
        return iconImage;
    }

    public void setIconImage(Drawable iconImage) {
        if (this.iconImage != iconImage) {
            this.iconImage = iconImage;
            invalidate(true);
        }
    }

    public float getIconSpacing() {
        return iconSpacing;
    }

    public void setIconSpacing(float iconSpacing) {
        if (this.iconSpacing != iconSpacing) {
            this.iconSpacing = iconSpacing;
            invalidate(true);
        }
    }

    public Drawable getActionImage() {
        return actionImage;
    }

    public void setActionImage(Drawable actionImage) {
        if (this.actionImage != actionImage) {
            this.actionImage = actionImage;
            invalidate(true);
        }
    }

    public float getActionSpacing() {
        return actionSpacing;
    }

    public void setActionSpacing(float actionSpacing) {
        if (this.actionSpacing != actionSpacing) {
            this.actionSpacing = actionSpacing;
            invalidate(true);
        }
    }

    public Menu getSubMenu() {
        return subMenu;
    }

    public void setSubMenu(Menu subMenu) {
        if (this.subMenu != subMenu) {
            if (this.subMenu != null) remove(this.subMenu);
            this.subMenu = subMenu;
            add(subMenu);
            invalidate(true);
        }
    }
}
