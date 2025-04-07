package flat.widget.text;

import flat.animations.StateInfo;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.graphics.symbols.Font;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;
import flat.widget.text.data.TextRender;

import java.util.Objects;

public class Label extends Widget {

    private String text;

    private boolean textAllCaps;
    private Font textFont = Font.getDefault();
    private float textSize = 16f;
    private int textColor = 0x000000FF;

    private VerticalAlign verticalAlign = VerticalAlign.TOP;
    private HorizontalAlign horizontalAlign = HorizontalAlign.LEFT;

    private String showText;
    protected float textWidth;
    protected final TextRender textRender = new TextRender();

    public Label() {
        textRender.setFont(textFont);
        textRender.setTextSize(textSize);
    }

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);
        UXAttrs attrs = getAttrs();

        setText(attrs.getAttributeString("text", getText()));
    }

    @Override
    public void applyLocalization() {
        super.applyLocalization();
        UXAttrs attrs = getAttrs();

        setText(attrs.getAttributeLocale("text", getText()));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();

        setTextFont(attrs.getFont("text-font", info, getTextFont()));
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
    public void onDraw(Graphics graphics) {
        if (discardDraw(graphics)) return;

        drawBackground(graphics);
        drawRipple(graphics);

        float x = getInX();
        float y = getInY();
        float width = getInWidth();
        float height = getInHeight();

        if (width <= 0 || height <= 0) return;

        if (getTextFont() != null && getTextSize() > 0 && Color.getAlpha(getTextColor()) > 0) {
            float xpos = xOff(x, x + width, Math.min(getTextWidth(), width));
            float ypos = yOff(y, y + height, Math.min(getTextHeight(), height));
            drawText(graphics, xpos, ypos, width, height);
        }
    }

    protected void drawText(Graphics graphics, float x, float y, float width, float height) {
        if (getTextFont() != null && getTextSize() > 0 && Color.getAlpha(getTextColor()) > 0) {
            graphics.setTransform2D(getTransform());
            graphics.setColor(getTextColor());
            graphics.setTextFont(getTextFont());
            graphics.setTextSize(getTextSize());
            graphics.setTextBlur(0);

            textRender.drawText(graphics, x, y, width, height, horizontalAlign);
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
        }
    }

    public Font getTextFont() {
        return textFont;
    }

    public void setTextFont(Font textFont) {
        if (this.textFont != textFont) {
            this.textFont = textFont;
            textRender.setFont(textFont);
            invalidate(isWrapContent());
        }
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        if (this.textSize != textSize) {
            this.textSize = textSize;
            textRender.setTextSize(textSize);
            invalidate(isWrapContent());
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
        return textRender.getTextWidth();
    }

    protected float getTextHeight() {
        return textRender.getTextHeight();
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
        if (verticalAlign == VerticalAlign.BOTTOM) return end - textHeight;
        if (verticalAlign == VerticalAlign.MIDDLE) return (start + end - textHeight) / 2f;
        return start;
    }

    protected float yOffCenter(float start, float end, float textHeight) {
        return (start + end - textHeight) / 2f;
    }
}
