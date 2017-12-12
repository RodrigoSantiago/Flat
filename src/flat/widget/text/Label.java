package flat.widget.text;

import flat.graphics.Context;
import flat.graphics.Font;
import flat.graphics.text.VAlign;
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
    public void onDraw(Context context) {
        super.onDraw(context);
        context.setTransform(getTransformView());
        if (showText != null) {
            context.setColor(fontColor);
            context.setTextFont(font);
            context.setTextSize(fontSize);
            context.setTextVerticalAlign(VAlign.TOP);
            context.drawTextSlice(showText, 0, 0, getWidth());
        }
    }

    @Override
    public void onMeasure() {
        if (invalidTextSize) {
            Context.getContext().setTextFont(font);
            Context.getContext().setTextSize(fontSize);
            textWidth = Context.getContext().getTextWidth(showText);
        }
        setMeasure(getPrefWidth() == WRAP_CONTENT ? textWidth : getPrefWidth(), getPrefHeight() == WRAP_CONTENT ? fontSize : getPrefHeight());
    }
}
