package flat.widget.text;

import flat.animations.NormalizedAnimation;
import flat.animations.StateInfo;
import flat.events.*;
import flat.graphics.Color;
import flat.graphics.SmartContext;
import flat.graphics.context.Font;
import flat.math.Vector2;
import flat.math.stroke.BasicStroke;
import flat.uxml.*;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;
import flat.widget.layout.Scrollable;
import flat.widget.text.data.Caret;
import flat.widget.text.data.TextRender;
import flat.widget.value.HorizontalScrollBar;
import flat.widget.value.VerticalScrollBar;
import flat.window.Activity;
import flat.window.Application;

import java.util.Objects;

public class TextField extends Scrollable {

    private UXValueListener<String> textChangeListener;
    private UXListener<TextEvent> textChangeFilter;
    private UXListener<TextEvent> textInputFilter;
    private String text;
    private String textHint;

    private Font textFont = Font.getDefault();
    private float textSize = 16f;
    private int textColor = 0x000000FF;
    private int textHintColor = 0x000000FF;
    private int textSelectedColor = 0x00000080;
    private float caretBlinkDuration = 0.5f; // todo - implement

    private VerticalAlign verticalAlign = VerticalAlign.TOP;
    private HorizontalAlign horizontalAlign = HorizontalAlign.LEFT;

    private boolean invalidTextSize;
    private boolean invalidTextHintSize;
    private float textWidth;
    private float textHintWidth;
    private final TextRender textRender = new TextRender();
    private final TextRender textHintRender = new TextRender();
    private final Caret startCaret = new Caret();
    private final Caret endCaret = new Caret();

    private final CaretBlink caretBlink = new CaretBlink();
    private boolean showCaret;

    private int keyCopy = KeyCode.KEY_C;
    private int keyPaste = KeyCode.KEY_V;
    private int keyCut = KeyCode.KEY_X;
    private int keySelectAll = KeyCode.KEY_A;
    private int keyClearSelection = KeyCode.KEY_ESCAPE;
    private int keyBackspace = KeyCode.KEY_BACKSPACE;
    private int keyDelete = KeyCode.KEY_DELETE;
    private int keyMenu  = KeyCode.KEY_MENU;

    private int maxCharacters = 0;

    public TextField() {
        textRender.setFont(textFont);
        textHintRender.setFont(textFont);
        textRender.setTextSize(textSize);
        textHintRender.setTextSize(textSize);
    }

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        UXAttrs attrs = getAttrs();
        String hBarId = attrs.getAttributeString("horizontal-bar-id", null);
        String vBarId = attrs.getAttributeString("vertical-bar-id", null);

        Widget widget;
        while ((widget = children.next()) != null ) {
            if (hBarId != null && hBarId.equals(widget.getId()) && widget instanceof HorizontalScrollBar bar) {
                setHorizontalBar(bar);
            } else if (vBarId != null && vBarId.equals(widget.getId()) && widget instanceof VerticalScrollBar bar) {
                setVerticalBar(bar);
            }
        }
    }

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);
        UXAttrs attrs = getAttrs();

        setMaxCharacters((int) attrs.getAttributeNumber("max-characters", getMaxCharacters()));
        setText(attrs.getAttributeString("text", getText()));
        setTextHint(attrs.getAttributeString("text-hint", getTextHint()));
        setTextChangeListener(attrs.getAttributeValueListener("on-text-change", String.class, controller));
        setTextChangeFilter(attrs.getAttributeListener("on-text-change-filter", TextEvent.class, controller));
        setTextInputFilter(attrs.getAttributeListener("on-text-input-filter", TextEvent.class, controller));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();

        setTextFont(attrs.getFont("text-font", info, getTextFont()));
        setTextSize(attrs.getSize("text-size", info, getTextSize()));
        setTextColor(attrs.getColor("text-color", info, getTextColor()));
        setTextSelectedColor(attrs.getColor("text-selected-color", info, getTextSelectedColor()));
        setTextHintColor(attrs.getColor("text-hint-color", info, getTextHintColor()));

        setVerticalAlign(attrs.getConstant("vertical-align", info, getVerticalAlign()));
        setHorizontalAlign(attrs.getConstant("horizontal-align", info, getHorizontalAlign()));
    }

    @Override
    public void onMeasure() {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        if (getHorizontalBar() != null) {
            getHorizontalBar().onMeasure();
        }
        if (getVerticalBar() != null) {
            getVerticalBar().onMeasure();
        }

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
    public Vector2 onLayoutLocalDimension(float width, float height) {
        return new Vector2(getTextWidth(), getTextHeight());
    }

    @Override
    public void setLayoutScrollOffset(float xx, float yy) {

    }

    @Override
    public void onLayout(float width, float height) {
        super.onLayout(width, height);
    }

    private Caret getFirstCaret() {
        return startCaret.getOffset() <= endCaret.getOffset() ? startCaret : endCaret;
    }

    private Caret getSecondCaret() {
        return startCaret.getOffset() <= endCaret.getOffset() ? endCaret : startCaret;
    }

    @Override
    public void onDraw(SmartContext context) {
        drawBackground(context);
        drawRipple(context);

        float x = getInX();
        float y = getInY();
        float width = getInWidth();
        float height = getInHeight();

        if (getOutWidth() <= 0 || getOutHeight() <= 0) return;

        if (getText() == null || getText().length() == 0 && getTextHint() != null) {
            if (getTextFont() == null || getTextSize() <= 0 || Color.getAlpha(getTextHintColor()) == 0) {
                return;
            }
            float xpos = getTextHintWidth() < width ? xOff(x, x + width, getTextHintWidth()) : x;
            float ypos = getTextHintHeight() < height ? yOff(y, y + height, getTextHintHeight()) : y;

            context.setTransform2D(getTransform());
            context.setColor(getTextHintColor());
            context.setTextFont(getTextFont());
            context.setTextSize(getTextSize());
            context.setTextBlur(0);

            float lineH = getLineHeight();
            context.setColor(getTextHintColor());
            int start = Math.max(0, (int) Math.floor((getViewOffsetY() - getInY()) / lineH) - 1);
            int end = start + Math.max(0, (int) Math.ceil(getHeight() / lineH) + 2);
            textHintRender.drawText(context, xpos, ypos, width * 9999, height * 9999, horizontalAlign, start, end);

            if (showCaret) {
                float caretX = textHintRender.getCaretHorizontalOffset(endCaret, horizontalAlign) + xpos;
                context.setColor(getTextColor());
                context.setStroker(new BasicStroke(2));
                context.drawLine(
                        caretX, ypos + endCaret.getLine() * lineH
                        , caretX, ypos + (endCaret.getLine() + 1) * lineH);
            }

        } else {
            if (getTextFont() == null || getTextSize() <= 0) {
                return;
            }
            if (isHorizontalDimensionScroll() || isVerticalDimensionScroll()) {
                context.pushClip(getBackgroundShape());
            }
            x -= getViewOffsetX();
            y -= getViewOffsetY();
            float xpos = getTextWidth() < width ? xOff(x, x + width, getTextWidth()) : x;
            float ypos = getTextHeight() < height ? yOff(y, y + height, getTextHeight()) : y;

            context.setTransform2D(getTransform());
            context.setColor(getTextColor());
            context.setTextFont(getTextFont());
            context.setTextSize(getTextSize());
            context.setTextBlur(0);

            Caret first = getFirstCaret();
            Caret second = getSecondCaret();

            float lineH = getLineHeight();
            float firstX = textRender.getCaretHorizontalOffset(first, horizontalAlign) + xpos;
            float secondX = textRender.getCaretHorizontalOffset(second, horizontalAlign) + xpos;

            // Text Selection
            context.setColor(textSelectedColor);
            if (first.getLine() == second.getLine()) {
                context.drawRect(firstX, ypos + first.getLine() * lineH, secondX - firstX, lineH, true);
            } else {
                context.drawRect(firstX, ypos + first.getLine() * lineH, getTotalDimensionX() - (firstX - x), lineH, true);
                context.drawRect(x, y + second.getLine() * lineH, secondX - x, lineH, true);
                if (first.getLine() + 1 < second.getLine()) {
                    context.drawRect(
                            x, y + (first.getLine() + 1) * lineH
                            , getTotalDimensionX(), (second.getLine() - first.getLine() - 1) * lineH, true);
                }
            }

            // Text
            context.setColor(getTextColor());
            int start = Math.max(0, (int) Math.floor((getViewOffsetY() - getInY()) / lineH) - 1);
            int end = start + Math.max(0, (int) Math.ceil(getHeight() / lineH) + 2);
            textRender.drawText(context, xpos, ypos, width * 9999, height * 9999, horizontalAlign, start, end);

            // Caret
            if (showCaret) {
                float caretX = textRender.getCaretHorizontalOffset(endCaret, horizontalAlign) + xpos;
                context.setColor(getTextColor());
                context.setStroker(new BasicStroke(2));
                context.drawLine(
                        caretX, ypos + endCaret.getLine() * lineH
                        , caretX, ypos + (endCaret.getLine() + 1) * lineH);
            }
            if (isHorizontalDimensionScroll() || isVerticalDimensionScroll()) {
                context.popClip();
            }
        }

        if (getHorizontalBar() != null && isHorizontalVisible()) {
            getHorizontalBar().onDraw(context);
        }

        if (getVerticalBar() != null && isVerticalVisible()) {
            getVerticalBar().onDraw(context);
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
        if (!event.isConsumed() && event.getSource() == this && event.getPointerID() == 1 && getTextFont() != null) {
            float x = getInX();
            float y = getInY();
            float width = getInWidth();
            float height = getInHeight();
            x -= getViewOffsetX();
            y -= getViewOffsetY();
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

        var first = getFirstCaret();
        var second = getSecondCaret();

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
                actionDeleteBackwards(first, second);
            } else if (event.getKeycode() == keyDelete) {
                actionDeleteFowards(first, second);
            } else if (event.isCtrlDown() && event.getKeycode() == keyPaste) {
                actionPaste(first, second);
            } else if (event.isCtrlDown() && event.getKeycode() == keyCopy) {
                actionCopy(first, second);
            } else if (event.isCtrlDown() && event.getKeycode() == keyCut) {
                actionCut(first, second);
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
                textRender.moveCaretVertical(endCaret, horizontalAlign, (int) (getViewDimensionY() / getLineHeight()));
                if (!event.isShiftDown()) startCaret.set(endCaret);
            } else if (event.getKeycode() == KeyCode.KEY_PAGE_UP) {
                textRender.moveCaretVertical(endCaret, horizontalAlign, (int) -(getViewDimensionY() / getLineHeight()));
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

    protected void editText(Caret first, Caret second, String text) {
        var event = filterInputText(first.getOffset(), second.getOffset(), text);
        if (event != null) {
            if (event.isConsumed() || event.getText() == null) {
                return;
            } else {
                text = event.getText();
            }
        }

        Caret caret = new Caret();
        caret.set(endCaret);
        if (!textRender.editText(first, second, text, caret)) {
            return;
        }

        String old = this.text;
        String txt = textRender.getText();
        if (textChangeFilter != null) {
            var textEvent = filterText(0, textRender.getTotalBytes(), txt);
            if (textEvent != null) {
                if (textEvent.isConsumed()) {
                    textRender.setText(old);
                    return;
                }
                String newTxt = textEvent.getText();
                if (!Objects.equals(newTxt, txt)) {
                    int before = textRender.getTotalCharacters();
                    textRender.setText(newTxt);
                    int after = textRender.getTotalCharacters();
                    if (after < before) {
                        textRender.moveCaret(caret, 0);
                    } else if (after > before) {
                        textRender.moveCaret(caret, after - before);
                    }
                    if (maxCharacters > 0) {
                        txt = textRender.getText();
                    } else {
                        txt = newTxt;
                    }
                }
            }
        }
        this.text = txt;

        startCaret.set(caret);
        endCaret.set(caret);

        invalidateTextSize();
        setCaretVisible();
        slideToCaret(1);

        fireTextChange(old);
    }

    private void actionShowContextMenu() {
        float x = getInX();
        float y = getInY();
        float width = getInWidth();
        float height = getInHeight();
        float px = x - getViewOffsetX();
        float py = y - getViewOffsetY();
        float xpos = getTextWidth() < width ? xOff(x, x + width, getTextWidth()) : px;
        float ypos = getTextHeight() < height ? yOff(y, y + height, getTextHeight()) : py;

        float caretX = textRender.getCaretHorizontalOffset(endCaret, horizontalAlign) + xpos;
        float caretY = endCaret.getLine() * getLineHeight() + ypos;
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

    private void actionDeleteBackwards(Caret first, Caret second) {
        if (first.getOffset() == second.getOffset()) {
            textRender.moveCaretBackwards(first);
        }
        editText(first, second, "");
    }

    private void actionDeleteFowards(Caret first, Caret second) {
        if (first.getOffset() == second.getOffset()) {
            textRender.moveCaretFoward(second);
        }
        editText(first, second, "");
    }

    private void actionCut(Caret first, Caret second) {
        String str = textRender.getText(first, second);
        if (str != null && !str.isEmpty()) {
            getActivity().getWindow().setClipboard(str);
        }
        editText(first, second, "");
    }

    private void actionCopy(Caret first, Caret second) {
        String str = textRender.getText(first, second);
        if (str != null && !str.isEmpty()) {
            getActivity().getWindow().setClipboard(str);
        }
    }

    private void actionPaste(Caret first, Caret second) {
        String str = getActivity().getWindow().getClipboard();
        if (str != null && !str.isEmpty()) {
            editText(first, second, str);
            Application.runVsync(() -> slideToCaret(1));
        }
    }

    private void setCaretVisible() {
        showCaret = true;
        invalidate(false);
        caretBlink.setLoops(-1);
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
            var event = filterInputText(0, textRender.getTotalBytes(), text);

            if (event != null) {
                if (event.isConsumed() || event.getText() == null) {
                    return;
                } else {
                    text = event.getText();
                }
            }

            String old = this.text;

            if (textChangeFilter != null) {
                var textEvent = filterText(0, textRender.getTotalBytes(), text);
                if (textEvent != null) {
                    if (textEvent.isConsumed()) {
                        return;
                    } else {
                        text = textEvent.getText();
                    }
                }
            }

            if (text != null && maxCharacters > 0) {
                textRender.setText(text);
                this.text = textRender.getText();
            } else {
                this.text = text;
                textRender.setText(text);
            }

            textRender.moveCaretBegin(startCaret);
            textRender.moveCaretBegin(endCaret);

            invalidate(isWrapContent());
            invalidateTextSize();

            fireTextChange(old);
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

    public int getMaxCharacters() {
        return maxCharacters;
    }

    public void setMaxCharacters(int maxCharacters) {
        if (this.maxCharacters != maxCharacters) {
            this.maxCharacters = maxCharacters;
            textRender.setMaxCharacters(maxCharacters);
            if (maxCharacters > 0 && maxCharacters < textRender.getTotalCharacters()) {
                textRender.trim(maxCharacters);

                String old = this.text;
                this.text = textRender.getText();

                textRender.moveCaretBegin(startCaret);
                textRender.moveCaretBegin(endCaret);

                invalidate(isWrapContent());
                invalidateTextSize();
                fireTextChange(old);
            }
        }
    }

    public Font getTextFont() {
        return textFont;
    }

    public void setTextFont(Font textFont) {
        if (this.textFont != textFont) {
            this.textFont = textFont;
            textRender.setFont(textFont);
            textHintRender.setFont(textFont);
            invalidate(isWrapContent());
            invalidateTextSize();
            invalidateTextHintSize();
        }
    }

    protected float getLineHeight() {
        return textFont == null ? textSize : textFont.getHeight(textSize);
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

    public int getTextSelectedColor() {
        return textSelectedColor;
    }

    public void setTextSelectedColor(int textSelectedColor) {
        if (this.textSelectedColor != textSelectedColor) {
            this.textSelectedColor = textSelectedColor;
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
        getTextWidth();
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

    public void slideToCaret(float speed) {
        float lineH = getLineHeight();
        float caretX = textRender.getCaretHorizontalOffset(endCaret, horizontalAlign);
        float caretYMin = endCaret.getLine() * lineH;
        float caretYMax = (endCaret.getLine() + 1) * lineH;
        float caretY = (caretYMax + caretYMin) * 0.5F;

        float targetX = getViewOffsetX();
        if (caretX < targetX) {
            targetX = caretX;
        } else if (caretX > targetX + getViewDimensionX()) {
            targetX = caretX - getViewDimensionX();
        }

        float targetY = getViewOffsetY();
        if (caretYMin < targetY) {
            targetY = caretYMin;
            if (caretYMax > targetY + getViewDimensionY()) {
                targetY = caretY;
            }
        } else if (caretYMax > targetY + getViewDimensionY()) {
            targetY = caretYMax - getViewDimensionY();
            if (caretYMin < targetY) {
                targetY = caretY;
            }
        }
        slideTo(targetX * speed + getViewOffsetX() * (1 - speed), targetY * speed + getViewOffsetY() * (1 - speed));
    }

    public UXListener<TextEvent> getTextChangeFilter() {
        return textChangeFilter;
    }

    public void setTextChangeFilter(UXListener<TextEvent> textChangeFilter) {
        this.textChangeFilter = textChangeFilter;
    }

    private TextEvent filterText(int start, int end, String text) {
        if (textChangeFilter != null) {
            TextEvent event = new TextEvent(this, TextEvent.FILTER, start, end, text);
            UXListener.safeHandle(textChangeFilter, event);
            return event;
        }
        return null;
    }

    public UXListener<TextEvent> getTextInputFilter() {
        return textInputFilter;
    }

    public void setTextInputFilter(UXListener<TextEvent> textInputFilter) {
        this.textInputFilter = textInputFilter;
    }

    private TextEvent filterInputText(int start, int end, String text) {
        if (textInputFilter != null) {
            TextEvent event = new TextEvent(this, TextEvent.FILTER, start, end, text);
            UXListener.safeHandle(textInputFilter, event);
            return event;
        }
        return null;
    }

    public UXValueListener<String> getTextChangeListener() {
        return textChangeListener;
    }

    public void setTextChangeListener(UXValueListener<String> textChangeListener) {
        this.textChangeListener = textChangeListener;
    }

    private void fireTextChange(String old) {
        if (textChangeListener != null && !Objects.equals(old, text)) {
            UXValueListener.safeHandle(textChangeListener, new ValueChange<>(this, old, text));
        }
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
