package flat.widget.text;

import flat.animations.StateInfo;
import flat.graphics.Color;
import flat.graphics.SmartContext;
import flat.graphics.context.Context;
import flat.graphics.context.Font;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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
    private boolean invalidTextSize; // TODO - MULTLINE??
    private float textWidth;
    private TextRender textRender = new TextRender();

    public Label() {
        textRender.setFont(font);
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
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;

        if (wrapWidth) {
            mWidth = Math.max(getTextWidth() + extraWidth, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            mHeight = Math.max(getTextHeight() + extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public void onDraw(SmartContext context) {
        drawBackground(context);
        drawRipple(context);

        float x = getInX();
        float y = getInY();
        float width = getInWidth();
        float height = getInHeight();

        if (width <= 0 || height <= 0) return;

        if (getFont() != null && getTextSize() > 0 && Color.getAlpha(getTextColor()) > 0) {
            float xpos = xOff(x, x + width, Math.min(getTextWidth(), width));
            float ypos = yOff(y, y + height, Math.min(getTextHeight(), height));
            drawText(context, xpos, ypos, width, height);
        }
    }

    protected void drawText(SmartContext context, float x, float y, float width, float height) {
        if (getFont() != null && getTextSize() > 0 && Color.getAlpha(getTextColor()) > 0) {
            context.setTransform2D(getTransform());
            context.setColor(getTextColor());
            context.setTextFont(getFont());
            context.setTextSize(getTextSize());
            context.setTextBlur(0);

            textRender.drawText(context, getTextSize(), x, y, width, height, horizontalAlign);
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (!Objects.equals(this.text, text)) {
            this.text = text;
            showText = text == null ? null : textAllCaps ? text.toUpperCase() : text;
            textRender.setText(showText);
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
            textRender.setText(showText);
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
            textRender.setFont(font);
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
            textWidth = textRender.getTextWidth(textSize);
        }
        return textWidth;
    }

    protected float getTextHeight() {
        return textRender.getTextHeight(textSize);
    }

    protected String getShowText() {
        return showText;
    }

    protected boolean isWrapContent() {
        return getPrefWidth() == WRAP_CONTENT || getPrefHeight() == WRAP_CONTENT;
    }

    protected float xOff(float start, float end, float textWidth) {
        if (end < start) return (start + end) / 2f;
        if (horizontalAlign == HorizontalAlign.RIGHT) return end - textWidth;
        if (horizontalAlign == HorizontalAlign.CENTER) return (start + end - textWidth) / 2f;
        return start;
    }

    protected float yOff(float start, float end, float textHeight) {
        if (end < start) return (start + end) / 2f;
        if (verticalAlign == VerticalAlign.BOTTOM) return end - textHeight;
        if (verticalAlign == VerticalAlign.MIDDLE) return (start + end - textHeight) / 2f;
        return start;
    }

    protected float yOffCenter(float start, float end, float textHeight) {
        return (start + end - textHeight) / 2f;
    }
}
