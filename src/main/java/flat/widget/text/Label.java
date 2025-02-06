package flat.widget.text;

import flat.animations.StateInfo;
import flat.graphics.Color;
import flat.graphics.SmartContext;
import flat.graphics.context.Font;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;

import java.util.Objects;

public class Label extends Widget {

    private String text;
    private boolean textAllCaps;

    private Font font = Font.getDefault();
    private float textSize = 16f;
    private int textColor = 0x000000FF;

    private VerticalAlign verticalAlign = VerticalAlign.TOP;
    private HorizontalAlign horizontalAlign = HorizontalAlign.LEFT;

    private String showText;
    private boolean invalidTextSize;
    private float textWidth;

    public Label() {

    }

    public Label(String text) {
        setText(text);

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

        attrs.getSize("width", info, getPrefWidth());

        setFont(attrs.getFont("font", info, getFont()));
        setTextSize(attrs.getSize("text-size", info, getTextSize()));
        setTextColor(attrs.getColor("text-color", info, getTextColor()));
        setTextAllCaps(attrs.getBool("text-all-caps", info, isTextAllCaps()));

        setVerticalAlign(attrs.getConstant("vertical-align", info, getVerticalAlign()));
        setHorizontalAlign(attrs.getConstant("horizontal-align", info, getHorizontalAlign()));
    }

    @Override
    public void onMeasure() {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        float mWidth;
        float mHeight;
        boolean wrapWidth = getPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getPrefHeight() == WRAP_CONTENT;

        if (wrapWidth) {
            mWidth = Math.max(getTextWidth() + extraWidth, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            mHeight = Math.max(getTextHeight() + extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public void onDraw(SmartContext context) {
        backgroundDraw(context);

        if (showText != null && font != null && textSize > 0 && Color.getAlpha(textColor) > 0) {
            context.setTransform2D(getTransform());
            context.setColor(textColor);
            context.setTextFont(font);
            context.setTextSize(textSize);
            context.setTextBlur(0);

            final float x = getInX();
            final float y = getInY();
            final float width = getInWidth();
            final float height = getInHeight();

            if (width <= 0 || height <= 0) return;

            context.drawTextSlice(
                    xOff(x, x + width, Math.min(getTextWidth(), width)),
                    yOff(y, y + height, Math.min(getTextHeight(), height)),
                    width, height, showText);
        }
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

    public VerticalAlign getVerticalAlign() {
        return verticalAlign;
    }

    public void setVerticalAlign(VerticalAlign verticalAlign) {
        if (verticalAlign == null) verticalAlign = VerticalAlign.TOP;

        if (this.verticalAlign != verticalAlign) {
            this.verticalAlign = verticalAlign;
            invalidate(false);
        }
    }

    public HorizontalAlign getHorizontalAlign() {
        return horizontalAlign;
    }

    public void setHorizontalAlign(HorizontalAlign horizontalAlign) {
        if (horizontalAlign == null) horizontalAlign = HorizontalAlign.LEFT;

        if (this.horizontalAlign != horizontalAlign) {
            this.horizontalAlign = horizontalAlign;
            invalidate(false);
        }
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

    protected float xOff(float start, float end, float textWidth) {
        if (end < start) return (start + end) / 2f;
        if (horizontalAlign == HorizontalAlign.RIGHT) return end - textWidth;
        if (horizontalAlign == HorizontalAlign.CENTER) return (start + end - textWidth) / 2f;
        return start;
    }

    protected float yOff(float start, float end, float textHeight) {
        if (end < start) return (start + end) / 2f;
        if (verticalAlign == VerticalAlign.BOTTOM || verticalAlign == VerticalAlign.BASELINE) return end - textHeight;
        if (verticalAlign == VerticalAlign.MIDDLE) return (start + end - textHeight) / 2f;
        return start;
    }

    protected float yOffCenter(float start, float end, float textHeight) {
        return (start + end - textHeight) / 2f;
    }
}
