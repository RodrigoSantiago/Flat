package flat.widget.text;

import flat.graphics.SmartContext;
import flat.graphics.context.Context;
import flat.graphics.text.Align;
import flat.graphics.context.Font;
import flat.screen.Application;
import flat.uxml.Controller;
import flat.uxml.UXAttributes;
import flat.uxml.data.Dimension;
import flat.widget.Widget;

public class Label extends Widget {

    private String text;
    private boolean textAllCaps;

    private Font font;
    private float fontSize;
    private int textColor;

    private Align.Vertical verticalAlign;
    private Align.Horizontal horizontalAlign;

    private String showText;
    private boolean invalidTextSize;
    protected float textWidth;

    @Override
    public void applyAttributes(Controller controller, UXAttributes attributes) {
        super.applyAttributes(controller, attributes);
        setFont(attributes.asFont("font", Font.DEFAULT));
        setFontSize(attributes.asNumber("fontSize", Dimension.ptPx(16)));

        setText(attributes.asString("text", ""));
        setTextColor(attributes.asColor("textColor", 0x000000FF));
        setTextAllCaps(attributes.asBoolean("textAllCaps", false));

        setVerticalAlign(attributes.asEnum("verticalAlign", Align.Vertical.class, Align.Vertical.TOP));
        setHorizontalAlign(attributes.asEnum("horizontalAlign", Align.Horizontal.class, Align.Horizontal.LEFT));
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        showText = text == null ? null : textAllCaps ? text.toUpperCase() : text;
        invalidate(true);
        invalidateTextSize();
    }

    public boolean isTextAllCaps() {
        return textAllCaps;
    }

    public void setTextAllCaps(boolean textAllCaps) {
        this.textAllCaps = textAllCaps;
        showText = text == null ? null : textAllCaps ? text.toUpperCase() : text;
        invalidate(true);
        invalidateTextSize();
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
        invalidate(true);
        invalidateTextSize();
    }

    public float getFontSize() {
        return fontSize;
    }

    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
        invalidate(true);
        invalidateTextSize();
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        invalidate(true);
    }

    private void invalidateTextSize() {
        invalidTextSize = true;
    }

    public Align.Vertical getVerticalAlign() {
        return verticalAlign;
    }

    public void setVerticalAlign(Align.Vertical verticalAlign) {
        if (this.verticalAlign != verticalAlign) {
            this.verticalAlign = verticalAlign;
            invalidate(true);
        }
    }

    public Align.Horizontal getHorizontalAlign() {
        return horizontalAlign;
    }

    public void setHorizontalAlign(Align.Horizontal horizontalAlign) {
        if (this.horizontalAlign != horizontalAlign) {
            this.horizontalAlign = horizontalAlign;
            invalidate(true);
        }
    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);
        context.setTransform2D(getTransformView());
        if (showText != null) {
            context.setColor(textColor);
            context.setTextFont(font);
            context.setTextSize(fontSize);
            context.setTextVerticalAlign(Align.Vertical.TOP);
            context.setTextHorizontalAlign(Align.Horizontal.LEFT);

            final float x = getInX();
            final float y = getInY();
            final float width = getInWidth();
            final float height = getInHeight();

            context.drawTextSlice(
                    xOff(x, x + width, Math.min(textWidth, width)),
                    yOff(y, y + height, Math.min(fontSize, height)),
                    width, showText);
        }
    }

    @Override
    public void onMeasure() {
        if (invalidTextSize) {
            Context context = Application.getContext();
            context.svgTransform(getTransformView());
            context.svgTextFont(font);
            context.svgTextSize(fontSize);
            textWidth = context.svgTextGetWidth(showText);
        }
        float mWidth = Math.max(textWidth, getPrefWidth());
        float mHeight = Math.max(fontSize, getPrefHeight());
        mWidth += getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        mHeight += getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();
        setMeasure(mWidth, mHeight);
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
}
