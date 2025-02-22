package flat.widget.structure;

import flat.animations.StateInfo;
import flat.events.HoverEvent;
import flat.events.PointerEvent;
import flat.graphics.Color;
import flat.graphics.SmartContext;
import flat.graphics.context.Font;
import flat.graphics.cursor.Cursor;
import flat.graphics.image.Drawable;
import flat.math.Vector2;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.uxml.UXChildren;
import flat.widget.Widget;
import flat.widget.enums.ImageFilter;
import flat.widget.enums.VerticalAlign;
import flat.widget.layout.Frame;
import flat.window.Application;

import java.util.Objects;

public class Page extends Widget {

    private String text;

    private boolean textAllCaps;
    private Font font = Font.getDefault();
    private float textSize = 16f;
    private int textColor = 0x000000FF;
    private VerticalAlign verticalAlign = VerticalAlign.MIDDLE;

    private String showText;
    private boolean invalidTextSize;
    private float textWidth;

    private Drawable icon;
    private int iconColor = Color.white;
    private ImageFilter iconImageFilter = ImageFilter.LINEAR;
    private float iconSpacing;
    private float iconWidth;
    private float iconHeight;

    private Drawable closeIcon;
    private int closeIconColor = Color.white;
    private ImageFilter closeIconImageFilter = ImageFilter.LINEAR;
    private float closeIconSpacing;
    private float closeIconWidth;
    private float closeIconHeight;

    private boolean isHoveringClose;
    private Cursor closeIconCursor = Cursor.UNSET;

    private Frame frame;
    private float x1, y1, x2, y2;

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        Frame autoFrame = null;
        Widget widget;
        while ((widget = children.next()) != null ) {
            if (autoFrame == null && widget instanceof Frame firstFrame) {
                if (!children.hasNext()) {
                    autoFrame = firstFrame;
                    break;
                }
            } else {
                if (autoFrame == null) {
                    autoFrame = new Frame();
                    autoFrame.setPrefSize(MATCH_PARENT, MATCH_PARENT);
                }
                autoFrame.add(widget);
            }
        }
        if (autoFrame != null) {
            setFrame(autoFrame);
        }
    }

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);

        UXAttrs attrs = getAttrs();
        setText(attrs.getAttributeString("text", getText()));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();
        setVerticalAlign(attrs.getConstant("vertical-align", info, getVerticalAlign()));

        setIcon(attrs.getResourceAsDrawable("icon", info, getIcon(), false));
        setIconColor(attrs.getColor("icon-color", info, getIconColor()));
        setIconWidth(attrs.getSize("icon-width", info, getIconWidth()));
        setIconHeight(attrs.getSize("icon-height", info, getIconHeight()));
        setIconSpacing(attrs.getSize("icon-spacing", info, getIconSpacing()));
        setIconImageFilter(attrs.getConstant("icon-image-filter", info, getIconImageFilter()));

        setCloseIcon(attrs.getResourceAsDrawable("close-icon", info, getCloseIcon(), false));
        setCloseIconColor(attrs.getColor("close-icon-color", info, getCloseIconColor()));
        setCloseIconWidth(attrs.getSize("close-icon-width", info, getCloseIconWidth()));
        setCloseIconHeight(attrs.getSize("close-icon-height", info, getCloseIconHeight()));
        setCloseIconSpacing(attrs.getSize("close-icon-spacing", info, getCloseIconSpacing()));
        setCloseIconImageFilter(attrs.getConstant("close-icon-image-filter", info, getCloseIconImageFilter()));
        setCloseIconCursor(attrs.getConstant("close-icon-cursor", info, getCloseIconCursor()));

        setFont(attrs.getFont("font", info, getFont()));
        setTextSize(attrs.getSize("text-size", info, getTextSize()));
        setTextColor(attrs.getColor("text-color", info, getTextColor()));
        setTextAllCaps(attrs.getBool("text-all-caps", info, isTextAllCaps()));
    }

    @Override
    public void onDraw(SmartContext context) {
        drawBackground(context);
        drawRipple(context);

        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        if (width <= 0 || height <= 0) return;

        context.setTransform2D(getTransform());

        float iW = getLayoutIconWidth();
        float iH = getLayoutIconHeight();
        float ciW = getLayoutCloseIconWidth();
        float ciH = getLayoutCloseIconHeight();

        float spaceForIcon = (iW > 0 ? iW + getIconSpacing() : 0);
        float spaceForCloseIcon = (ciW > 0 ? ciW + getCloseIconSpacing() : 0);
        float spaceForText = getTextWidth();

        if (spaceForIcon + spaceForCloseIcon + spaceForText > width) {
            if (spaceForCloseIcon > width) {
                spaceForIcon = 0;
                spaceForText = 0;
                spaceForCloseIcon = width;
            } else if (spaceForIcon + spaceForCloseIcon > width) {
                spaceForText = 0;
                spaceForIcon = width - spaceForCloseIcon;
            } else {
                spaceForText = width - spaceForCloseIcon - spaceForIcon;
            }
        }

        float tw = spaceForText;
        float th = Math.min(height, getTextHeight());
        float iw = Math.min(iW, Math.max(spaceForIcon, 0));
        float ih = Math.min(height, iH);
        float ciw = Math.min(ciW, Math.max(spaceForCloseIcon, 0));
        float cih = Math.min(height, ciH);
        if (iw > 0 && ih > 0 && getIcon() != null) {
            getIcon().draw(context
                    , x
                    , yOff(y, y + height, ih)
                    , iw, ih, getIconColor(), getIconImageFilter());
        }

        if (tw > 0 && th > 0) {
            context.setColor(getTextColor());
            context.setTextFont(getFont());
            context.setTextSize(getTextSize());
            context.setTextBlur(0);
            
            context.drawTextSlice(
                    x + spaceForIcon
                    , yOff(y, y + height, th)
                    , tw
                    , th
                    , getShowText());
        }

        if (ciw > 0 && cih > 0 && getCloseIcon() != null) {
            getCloseIcon().draw(context
                    , x + width - ciw
                    , yOff(y, y + height, cih)
                    , ciw, cih, getCloseIconColor(), getCloseIconImageFilter());
        }
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
        float ciW = getLayoutCloseIconWidth();
        float ciH = getLayoutCloseIconHeight();

        if (wrapWidth) {
            mWidth = Math.max(getTextWidth() + extraWidth
                    + (iW > 0 ? iW + getIconSpacing() : 0)
                    + (ciW > 0 ? ciW + getCloseIconSpacing() : 0), getLayoutMinWidth());
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
    public void onLayout(float width, float height) {
        super.onLayout(width, height);
        updateClosePosition();
    }

    private void updateClosePosition() {
        float x = getInX();
        float y = getInY();
        float width = getInWidth();
        float height = getInHeight();

        float iaw = Math.min(width, getLayoutCloseIconWidth());
        float iah = Math.min(height, getLayoutCloseIconHeight());

        x1 = x + width - iaw;
        x2 = x + width;
        y1 = yOff(y, y + height, iah);
        y2 = yOff(y, y + height, iah) + iah;
    }

    @Override
    public void firePointer(PointerEvent event) {
        super.firePointer(event);
        if (!event.isConsumed() && event.getType() == PointerEvent.RELEASED && event.getPointerID() == 1) {
            if (getParent() instanceof Tab tab) {
                if (isOverActionButton(screenToLocal(event.getX(), event.getY()))) {
                    requestClose(true);
                } else {
                    requestSelect();
                }
            }
        }
    }

    @Override
    public void fireHover(HoverEvent event) {
        super.fireHover(event);
        if (!event.isConsumed() && event.getType() == HoverEvent.MOVED) {
            isHoveringClose = isOverActionButton(screenToLocal(event.getX(), event.getY()));
        }
    }

    @Override
    public Cursor getCurrentCursor() {
        return isHoveringClose && closeIconCursor != Cursor.UNSET ? closeIconCursor : super.getCursor();
    }

    private boolean isOverActionButton(Vector2 local) {
        return !(local.x < x1 || local.x > x2 || local.y < y1 || local.y > y2);
    }

    public void setFrame(Frame frame) {
        if (this.frame != frame) {
            this.frame = frame;
            if (getParent() instanceof Tab tab) {
                if (isSelected()) {
                    tab.refreshPage(this);
                }
            }
        }
    }

    public Frame getFrame() {
        return frame;
    }

    public boolean isSelected() {
        if (getParent() instanceof Tab tab) {
            return tab.getSelectedPage() == this;
        }
        return false;
    }

    public void requestSelect() {
        if (getParent() instanceof Tab tab) {
            tab.selectPage(this);
        }
    }

    public void requestClose(boolean systemRequest) {
        boolean close = true;
        if (getFrame() != null) {
            Controller controller = getFrame().getController();
            if (controller != null) {
                try {
                    close = controller.onCloseRequest(systemRequest);
                } catch (Exception e) {
                    Application.handleException(e);
                }
            }
        }
        if (close) {
            if (getParent() instanceof Tab tab) {
                tab.removePage(this);
            }
        }
    }

    void refreshSelectedState() {
        setActivated(isSelected());
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

    public float getIconSpacing() {
        return iconSpacing;
    }

    public void setIconSpacing(float iconSpacing) {
        if (this.iconSpacing != iconSpacing) {
            this.iconSpacing = iconSpacing;
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
        return icon == null ? 0 : iconWidth == 0 || iconWidth == MATCH_PARENT ? getTextHeight() : iconWidth;
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

    private float getLayoutIconHeight() {
        return icon == null ? 0 : iconHeight == 0 || iconHeight == MATCH_PARENT ? getTextHeight() : iconHeight;
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

    public int getCloseIconColor() {
        return closeIconColor;
    }

    public void setCloseIconColor(int closeIconColor) {
        if (this.closeIconColor != closeIconColor) {
            this.closeIconColor = closeIconColor;
            invalidate(false);
        }
    }

    public Drawable getCloseIcon() {
        return closeIcon;
    }

    public void setCloseIcon(Drawable closeIcon) {
        if (this.closeIcon != closeIcon) {
            this.closeIcon = closeIcon;
            invalidate(isWrapContent());
        }
    }

    public float getCloseIconSpacing() {
        return closeIconSpacing;
    }

    public void setCloseIconSpacing(float closeIconSpacing) {
        if (this.closeIconSpacing != closeIconSpacing) {
            this.closeIconSpacing = closeIconSpacing;
            invalidate(isWrapContent());
        }
    }

    public float getCloseIconWidth() {
        return closeIconWidth;
    }

    public void setCloseIconWidth(float closeIconWidth) {
        if (this.closeIconWidth != closeIconWidth) {
            this.closeIconWidth = closeIconWidth;
            invalidate(isWrapContent());
        }
    }

    public Cursor getCloseIconCursor() {
        return closeIconCursor;
    }

    public void setCloseIconCursor(Cursor closeIconCursor) {
        if (closeIconCursor == null) closeIconCursor = Cursor.UNSET;

        this.closeIconCursor = closeIconCursor;
    }

    private float getLayoutCloseIconWidth() {
        return closeIcon == null ? 0 : closeIconWidth == 0 || closeIconWidth == MATCH_PARENT ? getTextHeight() : closeIconWidth;
    }

    public float getCloseIconHeight() {
        return closeIconHeight;
    }

    public void setCloseIconHeight(float closeIconHeight) {
        if (this.closeIconHeight != closeIconHeight) {
            this.closeIconHeight = closeIconHeight;
            invalidate(isWrapContent());
        }
    }

    private float getLayoutCloseIconHeight() {
        return closeIcon == null ? 0 : closeIconHeight == 0 || closeIconHeight == MATCH_PARENT ? getTextHeight() : closeIconHeight;
    }

    public ImageFilter getCloseIconImageFilter() {
        return closeIconImageFilter;
    }

    public void setCloseIconImageFilter(ImageFilter closeIconImageFilter) {
        if (closeIconImageFilter == null) closeIconImageFilter = ImageFilter.LINEAR;

        if (this.closeIconImageFilter != closeIconImageFilter) {
            this.closeIconImageFilter = closeIconImageFilter;
            invalidate(false);
        }
    }

    public VerticalAlign getVerticalAlign() {
        return verticalAlign;
    }

    public void setVerticalAlign(VerticalAlign verticalAlign) {
        if (verticalAlign == null) verticalAlign = VerticalAlign.MIDDLE;

        if (this.verticalAlign != verticalAlign) {
            this.verticalAlign = verticalAlign;
            invalidate(true);
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (!Objects.equals(this.text, text)) {
            this.text = text;
            showText = text == null ? null : textAllCaps ? text.toUpperCase() : text;
            invalidate(isWrapContent());
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
            invalidate(isWrapContent());
            invalidateTextSize();
        }
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        if (this.font != font) {
            this.font = font;
            invalidate(isWrapContent());
            invalidateTextSize();
        }
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        if (this.textSize != textSize) {
            this.textSize = textSize;
            invalidate(isWrapContent());
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

    protected float getTextWidth() {
        if (invalidTextSize) {
            invalidTextSize = false;
            if (showText == null || font == null) {
                return textWidth = 0;
            }
            textWidth = font.getWidth(showText, textSize, 1);
        }
        return textWidth;
    }

    protected float getTextHeight() {
        return font == null ? 0 : font.getHeight(textSize);
    }

    protected String getShowText() {
        return showText;
    }

    protected boolean isWrapContent() {
        return getPrefWidth() == WRAP_CONTENT || getPrefHeight() == WRAP_CONTENT;
    }

    protected float yOff(float start, float end, float size) {
        if (verticalAlign == VerticalAlign.BOTTOM) return end - size;
        if (verticalAlign == VerticalAlign.MIDDLE) return (start + end - size) / 2f;
        return start;
    }
}
