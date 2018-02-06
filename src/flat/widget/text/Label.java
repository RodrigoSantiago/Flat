package flat.widget.text;

import flat.graphics.SmartContext;
import flat.graphics.context.Context;
import flat.graphics.text.Align;
import flat.graphics.context.Font;
import flat.screen.Application;
import flat.widget.Widget;

public class Label extends Widget {

    String text;
    String showText;
    boolean textAllCaps;
    boolean invalidTextSize = true;
    float textWidth;

    Font font = Font.DEFAULT;
    float fontSize = 16;
    int fontColor = 0x000000FF;

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

    public int getFontColor() {
        return fontColor;
    }

    public void setFontColor(int fontColor) {
        this.fontColor = fontColor;
        invalidate(true);
    }

    private void invalidateTextSize() {
        invalidTextSize = true;
    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);
        context.setTransform2D(getTransformView());
        if (showText != null) {
            context.setColor(fontColor);
            context.setTextFont(font);
            context.setTextSize(fontSize);
            context.setTextVerticalAlign(Align.Vertical.TOP);
            float lm = getMarginLeft() + getPaddingLeft();
            float rm = getMarginRight() + getPaddingRight();
            float tm = getMarginTop() + getPaddingTop();
            float bm = getMarginBottom() + getPaddingBottom();

            float x = lm + rm > getWidth() ? (lm + getWidth() - rm) / 2f : lm;
            float y = tm + bm > getHeight() ? (tm + getHeight() - bm) / 2f : tm;
            float width = Math.max(0, getWidth() - lm - rm);
            float height = Math.max(0, getHeight() - tm - bm);

            context.drawTextSlice(x, y, width, showText);
        }
    }

    @Override
    public void onMeasure() {
        if (invalidTextSize) {
            Context context = Application.getContext();
            context.svgTextFont(font);
            context.svgTextSize(fontSize);
            textWidth = context.svgTextGetWidth(showText);
        }
        float mWidth = Math.max(textWidth,getPrefWidth());
        float mHeight = Math.max(getPrefHeight(), fontSize);
        mWidth += getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        mHeight += getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();
        setMeasure(mWidth, mHeight);
    }
}
