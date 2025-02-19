package flat.widget.layout;

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
    private boolean iconScaleHeight;

    private Drawable closeIcon;
    private int closeIconColor = Color.white;
    private ImageFilter closeIconImageFilter = ImageFilter.LINEAR;
    private float closeIconSpacing;
    private boolean closeIconScaleHeight;

    private Frame frame;

    private float iconWidth;
    private float iconHeight;
    private float closeIconWidth;
    private float closeIconHeight;
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
        setIconScaleHeight(attrs.getBool("icon-scale-height", info, getIconScaleHeight()));
        setIconSpacing(attrs.getSize("icon-spacing", info, getIconSpacing()));
        setIconImageFilter(attrs.getConstant("icon-image-filter", info, getIconImageFilter()));

        setCloseIcon(attrs.getResourceAsDrawable("close-icon", info, getCloseIcon(), false));
        setCloseIconColor(attrs.getColor("close-icon-color", info, getCloseIconColor()));
        setCloseIconScaleHeight(attrs.getBool("close-icon-scale-height", info, getCloseIconScaleHeight()));
        setCloseIconSpacing(attrs.getSize("close-icon-spacing", info, getCloseIconSpacing()));
        setCloseIconImageFilter(attrs.getConstant("close-icon-image-filter", info, getCloseIconImageFilter()));

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

        float spaceForIcon = icon == null ? 0 : iconWidth + iconSpacing;
        float spaceForCloseIcon = closeIcon == null ? 0 : closeIconWidth + closeIconSpacing;
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
        float iw = Math.max(spaceForIcon - iconSpacing, 0);
        float ih = Math.min(height, iconHeight);
        float iaw = Math.max(spaceForCloseIcon - closeIconSpacing, 0);
        float iah = Math.min(height, closeIconHeight);
        if (iw > 0 && ih > 0) {
            icon.draw(context
                    , x
                    , yOff(y, y + height, ih)
                    , iw, ih, iconColor, iconImageFilter);
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

        if (iaw > 0 && iah > 0) {
            closeIcon.draw(context
                    , x + width - iaw
                    , yOff(y, y + height, iah)
                    , iaw, iah, closeIconColor, closeIconImageFilter);
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

        float space = icon == null ? 0 : iconSpacing;
        iconWidth = icon == null ? 0 : icon.getWidth();
        iconHeight = icon == null ? 0 : icon.getHeight();
        if (icon != null && iconScaleHeight && getTextHeight() > 0) {
            float diff = iconWidth / iconHeight;
            iconHeight = getTextHeight();
            iconWidth = iconHeight * diff;
        }

        float aSpace = closeIcon == null ? 0 : closeIconSpacing;
        closeIconWidth = closeIcon == null ? 0 : closeIcon.getWidth();
        closeIconHeight = closeIcon == null ? 0 : closeIcon.getHeight();
        if (closeIcon != null && closeIconScaleHeight && getTextHeight() > 0) {
            float diff = closeIconWidth / closeIconHeight;
            closeIconHeight = getTextHeight();
            closeIconWidth = closeIconHeight * diff;
        }

        if (wrapWidth) {
            mWidth = Math.max(getTextWidth() + extraWidth + iconWidth + space + closeIconWidth + aSpace, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            mHeight = Math.max(Math.max(getTextHeight(), Math.max(iconHeight, closeIconHeight)) + extraHeight, getLayoutMinHeight());
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

        float spaceForCloseIcon = Math.min(width, closeIcon == null ? 0 : closeIconWidth + closeIconSpacing);

        float iaw = Math.max(spaceForCloseIcon - closeIconSpacing, 0);
        float iah = Math.min(height, closeIconHeight);

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
            if (isOverActionButton(screenToLocal(event.getX(), event.getY()))) {
                setCursor(Cursor.HAND);
            } else {
                setCursor(Cursor.UNSET);
            }
        }
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

    public boolean getIconScaleHeight() {
        return iconScaleHeight;
    }

    public void setIconScaleHeight(boolean iconScaleHeight) {
        if (this.iconScaleHeight != iconScaleHeight) {
            this.iconScaleHeight = iconScaleHeight;
            invalidate(isWrapContent());
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

    public boolean getCloseIconScaleHeight() {
        return closeIconScaleHeight;
    }

    public void setCloseIconScaleHeight(boolean closeIconScaleHeight) {
        if (this.closeIconScaleHeight != closeIconScaleHeight) {
            this.closeIconScaleHeight = closeIconScaleHeight;
            invalidate(isWrapContent());
        }
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
