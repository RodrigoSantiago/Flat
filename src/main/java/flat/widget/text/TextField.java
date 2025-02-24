package flat.widget.text;

import flat.animations.NormalizedAnimation;
import flat.animations.StateInfo;
import flat.events.*;
import flat.graphics.Color;
import flat.graphics.SmartContext;
import flat.graphics.context.Font;
import flat.math.Vector2;
import flat.math.stroke.BasicStroke;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;
import flat.window.Activity;
import flat.window.Application;

import java.util.Objects;

public class TextField extends Widget {

    private String text;
    private String textHint;

    private Font font = Font.getDefault();
    private float textSize = 16f;
    private int textColor = 0x000000FF;
    private int textHintColor = 0x000000FF;

    private VerticalAlign verticalAlign = VerticalAlign.TOP;
    private HorizontalAlign horizontalAlign = HorizontalAlign.LEFT;

    private boolean invalidTextSize;
    private boolean invalidTextHintSize;
    private float textWidth;
    private float textHintWidth;
    private final TextRender textRender = new TextRender();
    private final TextRender textHintRender = new TextRender();
    private TextRender.CaretData startCaret = new TextRender.CaretData();
    private TextRender.CaretData endCaret = new TextRender.CaretData();

    private CaretBlink caretBlink = new CaretBlink();
    private boolean showCaret;

    private float viewOffsetX;
    private float viewDimensionX;
    private float totalDimensionX;

    private float viewOffsetY;
    private float viewDimensionY;
    private float totalDimensionY;

    private int keyCopy = KeyCode.KEY_C;
    private int keyPaste = KeyCode.KEY_V;
    private int keyCut = KeyCode.KEY_X;
    private int keySelectAll = KeyCode.KEY_A;
    private int keyClearSelection = KeyCode.KEY_ESCAPE;
    private int keyBackspace = KeyCode.KEY_BACKSPACE;
    private int keyDelete = KeyCode.KEY_DELETE;
    private int keyMenu  = KeyCode.KEY_MENU;

    public TextField() {
        textRender.setFont(font);
        textHintRender.setFont(font);
        textRender.setTextSize(textSize);
        textHintRender.setTextSize(textSize);
    }

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);
        UXAttrs attrs = getAttrs();

        setText(attrs.getAttributeString("text", getText()));
        setTextHint(attrs.getAttributeString("text-hint", getTextHint()));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();

        setFont(attrs.getFont("font", info, getFont()));
        setTextSize(attrs.getSize("text-size", info, getTextSize()));
        setTextColor(attrs.getColor("text-color", info, getTextColor()));
        setTextHintColor(attrs.getColor("text-hint-color", info, getTextHintColor()));

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
    public void onLayout(float width, float height) {
        super.onLayout(width, height);
        updateTextSize();
    }

    private void updateTextSize() {
        viewDimensionX = getInWidth();
        viewDimensionY = getInHeight();
        totalDimensionX = Math.max(viewDimensionX, getTextWidth());
        totalDimensionY = Math.max(viewDimensionY, getTextHeight());
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

        if (getText() == null || getText().length() == 0 && getTextHint() != null) {
            if (getFont() != null && getTextSize() > 0 && Color.getAlpha(getTextHintColor()) > 0) {
                float xpos = xOff(x, x + width, Math.min(getTextHintWidth(), width));
                float ypos = yOff(y, y + height, Math.min(getTextHintHeight(), height));

                context.setTransform2D(getTransform());
                context.setColor(getTextHintColor());
                context.setTextFont(getFont());
                context.setTextSize(getTextSize());
                context.setTextBlur(0);
                textHintRender.drawText(context, x, y, width, height, horizontalAlign);
            }
        } else {
            if (getFont() != null && getTextSize() > 0 && Color.getAlpha(getTextColor()) > 0) {
                if (totalDimensionX > viewDimensionX + 0.01f || totalDimensionY > viewDimensionY + 0.01f) {
                    context.pushClip(getBackgroundShape());
                }
                x -= viewOffsetX;
                y -= viewOffsetY;
                float xpos = getTextWidth() < width ? xOff(x, x + width, getTextWidth()) : x;
                float ypos = getTextHeight() < height ? yOff(y, y + height, getTextHeight()) : y;

                context.setTransform2D(getTransform());
                context.setColor(getTextColor());
                context.setTextFont(getFont());
                context.setTextSize(getTextSize());
                context.setTextBlur(0);

                float lineH = getLineHeight();
                var first = startCaret.offset <= endCaret.offset ? startCaret : endCaret;
                var second = startCaret.offset <= endCaret.offset ? endCaret : startCaret;
                float firstX = textRender.getCaretHorizontalOffset(first, horizontalAlign) + xpos;
                float secondX = textRender.getCaretHorizontalOffset(second, horizontalAlign) + xpos;

                context.setColor(Color.aqua);
                if (first.line == second.line) {
                    context.drawRect(firstX, ypos + first.line * lineH, secondX - firstX, lineH, true);
                } else {
                    context.drawRect(firstX, ypos + first.line * lineH, totalDimensionX - (firstX - x), lineH, true);
                    context.drawRect(x, y + second.line * lineH, secondX - x, lineH, true);
                    if (first.line + 1 < second.line) {
                        context.drawRect(x, y + (first.line + 1) * lineH, totalDimensionX, (second.line - first.line - 1) * lineH, true);
                    }
                }

                context.setColor(getTextColor());
                int start = Math.max(0, (int) Math.floor((viewOffsetY - getInY()) / lineH) - 1);
                int end = start + Math.max(0, (int) Math.ceil(getHeight() / lineH) + 2);
                textRender.drawText(context, xpos, ypos, width * 9999, height * 9999, horizontalAlign, start, end);

                if (showCaret) {
                    float caretX = textRender.getCaretHorizontalOffset(endCaret, horizontalAlign) + xpos;
                    context.setColor(getTextColor());
                    context.setStroker(new BasicStroke(2));
                    context.drawLine(caretX, ypos + endCaret.line * lineH, caretX, ypos + (endCaret.line + 1) * lineH);
                }
                if (totalDimensionX > viewDimensionX + 0.01f || totalDimensionY > viewDimensionY + 0.01f) {
                    context.popClip();
                }
            }
        }
    }

    @Override
    public void fireScroll(ScrollEvent event) {
        super.fireScroll(event);
        if (!event.isConsumed()) {
            slideVertical(- event.getDeltaY() * 10);
        }
    }

    @Override
    public void firePointer(PointerEvent event) {
        super.firePointer(event);
        if (!event.isConsumed() && getFont() != null) {
            float x = getInX();
            float y = getInY();
            float width = getInWidth();
            float height = getInHeight();
            x -= viewOffsetX;
            y -= viewOffsetY;
            float xpos = getTextWidth() < width ? xOff(x, x + width, getTextWidth()) : x;
            float ypos = getTextHeight() < height ? yOff(y, y + height, getTextHeight()) : y;

            Vector2 point = screenToLocal(event.getX(), event.getY());
            if (event.getType() == PointerEvent.PRESSED) {
                textRender.getCaret(point.x, point.y, xpos, ypos, horizontalAlign, startCaret);
                endCaret.set(startCaret);
                setCaretVisible();
            } else if (event.getType() == PointerEvent.DRAGGED) {
                textRender.getCaret(point.x, point.y, xpos, ypos, horizontalAlign, endCaret);
                setCaretVisible();
                slideToCaret(Application.getLoopTime() * 10f);
            }
            invalidate(false);
        }
    }

    @Override
    public void fireKey(KeyEvent event) {
        super.fireKey(event);
        if (event.isConsumed()) {
            return;
        }

        var first = startCaret.offset <= endCaret.offset ? startCaret : endCaret;
        var second = startCaret.offset <= endCaret.offset ? endCaret : startCaret;

        if (event.getType() == KeyEvent.TYPED) {
            editText(first, second, new String(Character.toChars(event.getKeycode())));
        }

        if (event.getKeycode() != KeyCode.KEY_UNKNOWN &&
                (event.getType() == KeyEvent.PRESSED || event.getType() == KeyEvent.REPEATED)) {

            if (event.getKeycode() == KeyCode.KEY_ENTER) {
                editText(first, second, "\n");
            } else if (event.getKeycode() == KeyCode.KEY_TAB) {
                editText(first, second, "\t");
            } else if (event.getKeycode() == keyBackspace) {
                actionDeleteBackwards();
            } else if (event.getKeycode() == keyDelete) {
                actionDeleteFowards();
            } else if (event.isCtrlDown() && event.getKeycode() == keyPaste) {
                actionPaste();
            } else if (event.isCtrlDown() && event.getKeycode() == keyCopy) {
                actionCopy();
            } else if (event.isCtrlDown() && event.getKeycode() == keyCut) {
                actionCut();
            } else if (event.isCtrlDown() && event.getKeycode() == keySelectAll) {
                actionSelectAll();
            } else if (event.getKeycode() == keyClearSelection) {
                actionClearSelection();
            } else if (event.getKeycode() == keyMenu) {
                actionShowContextMenu();
            } else if (event.getKeycode() == KeyCode.KEY_LEFT) {
                textRender.moveCaretBackwards(endCaret);
                if (!event.isShiftDown()) startCaret.set(endCaret);
            } else if (event.getKeycode() == KeyCode.KEY_RIGHT) {
                textRender.moveCaretFoward(endCaret);
                if (!event.isShiftDown()) startCaret.set(endCaret);
            } else if (event.getKeycode() == KeyCode.KEY_DOWN) {
                textRender.moveCaretVertical(endCaret, horizontalAlign, 1);
                if (!event.isShiftDown()) startCaret.set(endCaret);
            } else if (event.getKeycode() == KeyCode.KEY_UP) {
                textRender.moveCaretVertical(endCaret, horizontalAlign, -1);
                if (!event.isShiftDown()) startCaret.set(endCaret);
            } else if (event.getKeycode() == KeyCode.KEY_PAGE_DOWN) {
                textRender.moveCaretVertical(endCaret, horizontalAlign, (int) (viewDimensionY / getLineHeight()));
                if (!event.isShiftDown()) startCaret.set(endCaret);
            } else if (event.getKeycode() == KeyCode.KEY_PAGE_UP) {
                textRender.moveCaretVertical(endCaret, horizontalAlign, (int) -(viewDimensionY / getLineHeight()));
                if (!event.isShiftDown()) startCaret.set(endCaret);
            } else if (event.getKeycode() == KeyCode.KEY_HOME) {
                textRender.moveCaretBackwardsLine(endCaret);
                if (!event.isShiftDown()) startCaret.set(endCaret);
            } else if (event.getKeycode() == KeyCode.KEY_END) {
                textRender.moveCaretFowardsLine(endCaret);
                if (!event.isShiftDown()) startCaret.set(endCaret);
            } else {
                return;
            }
            setCaretVisible();
            slideToCaret(1);
        }
    }

    @Override
    public void fireFocus(FocusEvent event) {
        super.fireFocus(event);
        if (!isFocused()) {
            actionClearSelection();
            setCaretHidden();
        }
    }

    private void editText(TextRender.CaretData first, TextRender.CaretData second, String text) {
        textRender.editText(first, second, text, endCaret);
        startCaret.set(endCaret);
        invalidateTextSize();
        setCaretVisible();
        slideToCaret(1);
    }

    private void actionShowContextMenu() {
        float x = getInX();
        float y = getInY();
        float width = getInWidth();
        float height = getInHeight();
        float px = x - viewOffsetX;
        float py = y - viewOffsetY;
        float xpos = getTextWidth() < width ? xOff(x, x + width, getTextWidth()) : px;
        float ypos = getTextHeight() < height ? yOff(y, y + height, getTextHeight()) : py;

        float caretX = textRender.getCaretHorizontalOffset(endCaret, horizontalAlign) + xpos;
        float caretY = endCaret.line * getLineHeight() + ypos;
        var pos = localToScreen(Math.min(x + width, Math.max(x, caretX)), Math.min(y + height, Math.max(y, caretY)));
        showContextMenu(pos.x, pos.y);
    }

    private void actionClearSelection() {
        startCaret.set(endCaret);
        invalidate(false);
    }

    private void actionSelectAll() {
        textRender.moveCaretBegin(startCaret);
        textRender.moveCaretEnd(endCaret);
        invalidate(false);
    }

    private void actionDeleteBackwards() {
        var first = startCaret.offset <= endCaret.offset ? startCaret : endCaret;
        var second = startCaret.offset <= endCaret.offset ? endCaret : startCaret;
        if (first.offset == second.offset) {
            textRender.moveCaretBackwards(first);
        }
        editText(first, second, "");
    }

    private void actionDeleteFowards() {
        var first = startCaret.offset <= endCaret.offset ? startCaret : endCaret;
        var second = startCaret.offset <= endCaret.offset ? endCaret : startCaret;
        if (first.offset == second.offset) {
            textRender.moveCaretFoward(second);
        }
        editText(first, second, "");
    }

    private void actionCut() {
        var first = startCaret.offset <= endCaret.offset ? startCaret : endCaret;
        var second = startCaret.offset <= endCaret.offset ? endCaret : startCaret;
        String str = textRender.getText(first, second);
        if (str != null && !str.isEmpty()) {
            getActivity().getWindow().setClipboard(str);
        }
        editText(first, second, "");
    }

    private void actionCopy() {
        var first = startCaret.offset <= endCaret.offset ? startCaret : endCaret;
        var second = startCaret.offset <= endCaret.offset ? endCaret : startCaret;
        String str = textRender.getText(first, second);
        if (str != null && !str.isEmpty()) {
            getActivity().getWindow().setClipboard(str);
        }
    }

    private void actionPaste() {
        var first = startCaret.offset <= endCaret.offset ? startCaret : endCaret;
        var second = startCaret.offset <= endCaret.offset ? endCaret : startCaret;
        String str = getActivity().getWindow().getClipboard();
        if (str != null && !str.isEmpty()) {
            editText(first, second, str);
            Application.runVsync(() -> slideToCaret(1));
        }
    }

    private void setCaretVisible() {
        showCaret = true;
        invalidate(false);
        caretBlink.setLoops(1000);
        caretBlink.setDuration(1.0f);
        caretBlink.play(getActivity(), 0);
    }

    private void setCaretHidden() {
        showCaret = false;
        invalidate(false);
        caretBlink.stop(false);
    }

    private void blinkCaret() {
        showCaret = isFocused() && !showCaret;
        invalidate(false);
        caretBlink.play(getActivity(), 0);
    }

    public int getKeyCopy() {
        return keyCopy;
    }

    public void setKeyCopy(int keyCopy) {
        this.keyCopy = keyCopy;
    }

    public int getKeyPaste() {
        return keyPaste;
    }

    public void setKeyPaste(int keyPaste) {
        this.keyPaste = keyPaste;
    }

    public int getKeyCut() {
        return keyCut;
    }

    public void setKeyCut(int keyCut) {
        this.keyCut = keyCut;
    }

    public int getKeySelectAll() {
        return keySelectAll;
    }

    public void setKeySelectAll(int keySelectAll) {
        this.keySelectAll = keySelectAll;
    }

    public int getKeyClearSelection() {
        return keyClearSelection;
    }

    public void setKeyClearSelection(int keyClearSelection) {
        this.keyClearSelection = keyClearSelection;
    }

    public int getKeyBackspace() {
        return keyBackspace;
    }

    public void setKeyBackspace(int keyBackspace) {
        this.keyBackspace = keyBackspace;
    }

    public int getKeyDelete() {
        return keyDelete;
    }

    public void setKeyDelete(int keyDelete) {
        this.keyDelete = keyDelete;
    }

    public int getKeyMenu() {
        return keyMenu;
    }

    public void setKeyMenu(int keyMenu) {
        this.keyMenu = keyMenu;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (!Objects.equals(this.text, text)) {
            this.text = text;
            textRender.setText(text);
            invalidate(isWrapContent());
            invalidateTextSize();
        }
    }

    public String getTextHint() {
        return textHint;
    }

    public void setTextHint(String textHint) {
        if (!Objects.equals(this.textHint, textHint)) {
            this.textHint = textHint;
            textHintRender.setText(textHint);
            invalidate(isWrapContent());
            invalidateTextHintSize();
        }
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        if (this.font != font) {
            this.font = font;
            textRender.setFont(font);
            textHintRender.setFont(font);
            invalidate(isWrapContent());
            invalidateTextSize();
            invalidateTextHintSize();
        }
    }

    protected float getLineHeight() {
        return font == null ? textSize : font.getHeight(textSize);
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        if (this.textSize != textSize) {
            this.textSize = textSize;
            textRender.setTextSize(textSize);
            textHintRender.setTextSize(textSize);
            invalidate(isWrapContent());
            invalidateTextSize();
            invalidateTextHintSize();
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

    public int getTextHintColor() {
        return textHintColor;
    }

    public void setTextHintColor(int textHintColor) {
        if (this.textHintColor != textHintColor) {
            this.textHintColor = textHintColor;
            invalidate(false);
        }
    }

    private void invalidateTextSize() {
        invalidate(true);
        invalidTextSize = true;
        updateTextSize();
    }

    private void invalidateTextHintSize() {
        invalidTextHintSize = true;
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

    public float getViewOffsetX() {
        return Math.max(0, Math.min(viewOffsetX, totalDimensionX - viewDimensionX));
    }

    public void setViewOffsetX(float viewOffsetX) {
        viewOffsetX = Math.max(0, Math.min(viewOffsetX, totalDimensionX - viewDimensionX));

        if (this.viewOffsetX != viewOffsetX) {
            float old = this.viewOffsetX;
            this.viewOffsetX = viewOffsetX;
            invalidate(true);
            /*fireViewOffsetXListener(old);
            if (horizontalBar != null) {
                horizontalBar.setViewOffsetListener(null);
                horizontalBar.setViewOffset(this.viewOffsetX);
            }*/
        }
    }

    public float getViewOffsetY() {
        return Math.max(0, Math.min(viewOffsetY, totalDimensionY - viewDimensionY));
    }

    public void setViewOffsetY(float viewOffsetY) {
        viewOffsetY = Math.max(0, Math.min(viewOffsetY, totalDimensionY - viewDimensionY));

        if (this.viewOffsetY != viewOffsetY) {
            float old = this.viewOffsetY;
            this.viewOffsetY = viewOffsetY;
            invalidate(true);
            /*fireViewOffsetYListener(old);
            if (verticalBar != null) {
                verticalBar.setViewOffsetListener(null);
                verticalBar.setViewOffset(this.viewOffsetY);
            }*/
        }
    }

    public void slide(float offsetX, float offsetY) {
        slideHorizontal(offsetX);
        slideVertical(offsetY);
    }

    public void slideTo(float offsetX, float offsetY) {
        slideHorizontalTo(offsetX);
        slideVerticalTo(offsetY);
    }

    public void slideHorizontalTo(float offsetX) {
        offsetX = Math.max(0, Math.min(offsetX, totalDimensionX - viewDimensionX));

        float old = viewOffsetX;
        if (offsetX != old /*&& filterSlideX(offsetX)*/) {
            setViewOffsetX(offsetX);
            //fireSlideX();
        }
    }

    public void slideHorizontal(float offsetX) {
        slideHorizontalTo(getViewOffsetX() + offsetX);
    }

    public void slideVerticalTo(float offsetY) {
        offsetY = Math.max(0, Math.min(offsetY, totalDimensionY - viewDimensionY));

        float old = viewOffsetY;
        if (offsetY != old /*&& filterSlideY(offsetY)*/) {
            setViewOffsetY(offsetY);
            //fireSlideY();
        }
    }

    public void slideVertical(float offsetY) {
        slideVerticalTo(getViewOffsetY() + offsetY);
    }

    public void slideToCaret(float speed) {
        float lineH = getLineHeight();
        float caretX = textRender.getCaretHorizontalOffset(endCaret, horizontalAlign);
        float caretYMin = endCaret.line * lineH;
        float caretYMax = (endCaret.line + 1) * lineH;

        float targetX = viewOffsetX;
        if (caretX < viewOffsetX) {
            targetX = caretX;
        } else if (caretX > viewOffsetX + viewDimensionX) {
            targetX = caretX - viewDimensionX;
        }

        float targetY = viewOffsetY;
        if (caretYMin < viewOffsetY) {
            targetY = caretYMin;
        } else if (caretYMax > viewOffsetY + viewDimensionY) {
            targetY = caretYMax - viewDimensionY;
        }
        slideTo(targetX * speed + viewOffsetX * (1 - speed), targetY * speed + viewOffsetY * (1 - speed));
    }

    protected float getTextWidth() {
        if (invalidTextSize) {
            invalidTextSize = false;
            textWidth = textRender.getTextWidth();
        }
        return textWidth;
    }

    protected float getTextHeight() {
        return textRender.getTextHeight();
    }

    protected float getTextHintWidth() {
        if (invalidTextHintSize) {
            invalidTextHintSize = false;
            textHintWidth = textHintRender.getTextWidth();
        }
        return textHintWidth;
    }

    protected float getTextHintHeight() {
        return textHintRender.getTextHeight();
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

    private class CaretBlink extends NormalizedAnimation {
        @Override
        public Activity getSource() {
            return getActivity();
        }

        @Override
        protected void compute(float t) {
            if (t >= 0.5f) {
                blinkCaret();
            }
        }
    }
}
