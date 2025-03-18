package flat.widget.text;

import flat.animations.Animation;
import flat.animations.StateInfo;
import flat.events.*;
import flat.graphics.Graphics;
import flat.graphics.context.Font;
import flat.math.Vector2;
import flat.math.stroke.BasicStroke;
import flat.uxml.*;
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

public class TextArea extends Scrollable {

    private UXValueListener<String> textChangeListener;
    private UXListener<TextEvent> textChangeFilter;
    private UXListener<TextEvent> textInputFilter;
    private String text;
    private boolean invalidTextString;

    private Font textFont = Font.getDefault();
    private float textSize = 16f;
    private int textColor = 0x000000FF;
    private int textSelectedColor = 0x00000080;
    private int caretColor = 0x000000FF;
    private float caretBlinkDuration = 0.5f;
    private boolean editable = true;
    private boolean multiLineEnabled = true;

    private VerticalAlign verticalAlign = VerticalAlign.TOP;
    private HorizontalAlign horizontalAlign = HorizontalAlign.LEFT;

    private boolean invalidTextSize;
    private float textWidth;
    private float textHintWidth;
    private final TextRender textRender = new TextRender();
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

    private long lastPressedTime;
    private int clickCont;

    public TextArea() {
        textRender.setFont(textFont);
        textRender.setTextSize(textSize);
    }

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        for (var child : children) {
            if (child.getAttributeBool("horizontal-bar", false) &&
                    child.getWidget() instanceof HorizontalScrollBar bar) {
                setHorizontalBar(bar);
            } else if (child.getAttributeBool("vertical-bar", false) &&
                    child.getWidget() instanceof VerticalScrollBar bar) {
                setVerticalBar(bar);
            }
        }
    }

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);
        UXAttrs attrs = getAttrs();

        setMaxCharacters((int) attrs.getAttributeNumber("max-characters", getMaxCharacters()));
        setMultiLineEnabled(attrs.getAttributeBool("multiline-enabled", isMultiLineEnabled()));
        setText(attrs.getAttributeString("text", getText()));
        setEditable(attrs.getAttributeBool("editable", isEditable()));
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
        setCaretColor(attrs.getColor("caret-color", info, getCaretColor()));
        setCaretBlinkDuration(attrs.getNumber("caret-blink-duration", info, getCaretBlinkDuration()));

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
    public Vector2 onLayoutTotalDimension(float width, float height) {
        return new Vector2(getTextWidth(), getTextHeight());
    }

    @Override
    public void setLayoutScrollOffset(float xx, float yy) {

    }

    @Override
    public void onLayout(float width, float height) {
        super.onLayout(width, height);
    }

    protected Caret getStartCaret() {
        return startCaret;
    }

    protected Caret getEndCaret() {
        return endCaret;
    }

    protected Caret getFirstCaret() {
        return startCaret.getOffset() <= endCaret.getOffset() ? startCaret : endCaret;
    }

    protected Caret getSecondCaret() {
        return startCaret.getOffset() <= endCaret.getOffset() ? endCaret : startCaret;
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

        if (isHorizontalDimensionScroll() || isVerticalDimensionScroll()) {
            graphics.pushClip(getBackgroundShape());
        }

        onDrawText(graphics, x, y, width, height);

        if (isHorizontalDimensionScroll() || isVerticalDimensionScroll()) {
            graphics.popClip();
        }

        if (getHorizontalBar() != null && isHorizontalVisible()) {
            getHorizontalBar().onDraw(graphics);
        }

        if (getVerticalBar() != null && isVerticalVisible()) {
            getVerticalBar().onDraw(graphics);
        }
    }

    protected void onDrawText(Graphics context, float x, float y, float width, float height) {
        if (getOutWidth() <= 0 || getOutHeight() <= 0 || getTextFont() == null || getTextSize() <= 0) {
            return;
        }

        context.setTransform2D(getTransform());
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
        context.setColor(getTextSelectedColor());
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
        if (!isTextEmpty()) {
            context.setColor(getTextColor());
            int start = Math.max(0, (int) Math.floor((getViewOffsetY() - getInY()) / lineH) - 1);
            int end = start + Math.max(0, (int) Math.ceil(getHeight() / lineH) + 2);
            textRender.drawText(context, xpos, ypos, width * 9999, height * 9999, horizontalAlign, start, end);
        }

        // Caret
        if (showCaret && isEditable()) {
            float caretX = textRender.getCaretHorizontalOffset(endCaret, horizontalAlign);
            if (caretX == 0) {
                caretX += xpos + 1;
            } else if (textRender.isCaretLastOfLine(endCaret)) {
                caretX += xpos - 1;
            } else {
                caretX += xpos;
            }
            context.setColor(getCaretColor());
            context.setStroke(new BasicStroke(2));
            context.drawLine(
                    caretX, ypos + endCaret.getLine() * lineH,
                    caretX, ypos + (endCaret.getLine() + 1) * lineH);
        }
    }

    @Override
    public void scroll(ScrollEvent event) {
        super.scroll(event);
        if (!event.isConsumed() && isVerticalDimensionScroll()) {
            slideVertical(- event.getDeltaY() * 10);
            event.consume();
        }
    }

    @Override
    public void pointer(PointerEvent event) {
        super.pointer(event);
        Vector2 point = screenToLocal(event.getX(), event.getY());
        point.x += getViewOffsetX();
        point.y += getViewOffsetY();
        textPointer(event, point);
    }

    protected float getVisibleTextX() {
        return getInX();
    }

    protected float getVisibleTextY() {
        return getInY();
    }

    protected float getVisibleTextHeight() {
        return getInHeight();
    }

    protected float getVisibleTextWidth() {
        return getInWidth();
    }

    protected void textPointer(PointerEvent event, Vector2 point) {
        if (event.isConsumed() || event.getSource() != this || event.getPointerID() != 1 || getTextFont() == null) {
            return;
        }

        float x = getVisibleTextX();
        float y = getVisibleTextY();
        float width = getVisibleTextWidth();
        float height = getVisibleTextHeight();
        float xpos = isHorizontalDimensionScroll() ? x : xOff(x, x + width, getTextWidth());
        float ypos = isVerticalDimensionScroll() ? y : yOff(y, y + height, getTextHeight());

        if (event.getType() == PointerEvent.PRESSED) {
            long now = System.currentTimeMillis();
            if (now - lastPressedTime < 200) {
                clickCont++;
                if (clickCont == 1) {
                    textRender.getCaret(point.x, point.y, xpos, ypos, horizontalAlign, startCaret);
                    endCaret.set(startCaret);
                    textRender.moveCaretBackwardsLine(startCaret);
                    textRender.moveCaretFowardsLine(endCaret);
                } else if (clickCont > 1) {
                    textRender.moveCaretBegin(startCaret);
                    textRender.moveCaretEnd(endCaret);
                }
            } else {
                clickCont = 0;
                textRender.getCaret(point.x, point.y, xpos, ypos, horizontalAlign, startCaret);
                endCaret.set(startCaret);
            }
            setCaretVisible();
            lastPressedTime = now;
        } else if (event.getType() == PointerEvent.DRAGGED) {
            textRender.getCaret(point.x, point.y, xpos, ypos, horizontalAlign, endCaret);
            setCaretVisible();
            slideToCaret(Application.getLoopTime() * 10f);
        }
        invalidate(false);
    }

    @Override
    public void key(KeyEvent event) {
        super.key(event);
        if (event.isConsumed()) {
            return;
        }
        event.consume();

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
            } else {
                if (event.getKeycode() == KeyCode.KEY_LEFT) {
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
    }

    @Override
    public void focus(FocusEvent event) {
        super.focus(event);
        if (!isFocused()) {
            actionClearSelection();
            setCaretHidden();
        }
    }

    protected void actionShowContextMenu() {
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

    protected void actionClearSelection() {
        startCaret.set(endCaret);
        invalidate(false);
    }

    protected void actionSelectAll() {
        textRender.moveCaretBegin(startCaret);
        textRender.moveCaretEnd(endCaret);
        invalidate(false);
    }

    protected void actionDeleteBackwards(Caret first, Caret second) {
        if (first.getOffset() == second.getOffset()) {
            textRender.moveCaretBackwards(first);
        }
        editText(first, second, "");
    }

    protected void actionDeleteFowards(Caret first, Caret second) {
        if (first.getOffset() == second.getOffset()) {
            textRender.moveCaretFoward(second);
        }
        editText(first, second, "");
    }

    protected void actionCut(Caret first, Caret second) {
        String str = textRender.getText(first, second);
        if (str != null && !str.isEmpty()) {
            getActivity().getWindow().setClipboard(str);
        }
        editText(first, second, "");
    }

    protected void actionCopy(Caret first, Caret second) {
        String str = textRender.getText(first, second);
        if (str != null && !str.isEmpty()) {
            getActivity().getWindow().setClipboard(str);
        }
    }

    protected void actionPaste(Caret first, Caret second) {
        String str = getActivity().getWindow().getClipboard();
        if (str != null && !str.isEmpty()) {
            editText(first, second, str);
            slideToCaretLater(1);
        }
    }

    protected void setCaretVisible() {
        showCaret = true;
        invalidate(false);
        caretBlink.play();
    }

    protected void setCaretHidden() {
        showCaret = false;
        invalidate(false);
        caretBlink.stop();
    }

    protected void blinkCaret() {
        showCaret = isFocused() && !showCaret;
        invalidate(false);
    }

    protected boolean isShowCaret() {
        return showCaret;
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
        if (invalidTextString) {
            setLocalText(textRender.getText());
        }
        return text;
    }

    private void setLocalText(String text) {
        this.text = text;
        invalidTextString = false;
    }

    public void setText(String text) {
        if (Objects.equals(getText(), text)) return;
        if (!isMultiLineEnabled() && text != null) {
            text = text.replaceAll("[\\n\\r]", "");
            if (Objects.equals(getText(), text)) return;
        }

        // Input Filter
        var event = filterInputText(0, textRender.getTotalBytes(), text);
        if (event != null) {
            if (event.isConsumed() || event.getText() == null) {
                return;
            } else {
                text = event.getText();
            }
        }

        if (textChangeFilter != null || textChangeListener != null) {

            // Change Filter
            var textEvent = filterText(0, textRender.getTotalBytes(), text);
            if (textEvent != null) {
                if (textEvent.isConsumed()) {
                    return;
                } else {
                    text = textEvent.getText();
                }
            }

            String old = getText();
            setLocalText(text);

            if (text != null && maxCharacters > 0) {
                if (!textRender.setText(text)) {
                    setLocalText(textRender.getText());
                }
            } else {
                textRender.setText(text);
            }

            textRender.moveCaretBegin(startCaret);
            endCaret.set(startCaret);

            invalidateTextSize();

            // Change Listener
            fireTextChange(old);
        } else {
            setLocalText(text);

            if (text != null && maxCharacters > 0) {
                if (!textRender.setText(text)) {
                    invalidateTextString();
                }
            } else {
                textRender.setText(text);
            }

            textRender.moveCaretBegin(startCaret);
            endCaret.set(startCaret);

            invalidateTextSize();
        }
    }

    protected void editText(Caret first, Caret second, String input) {
        if (!isEditable()) return;
        if (!isMultiLineEnabled()) {
            input = input.replaceAll("[\\n\\r]", "");
        }

        // Input Filter
        var event = filterInputText(first.getOffset(), second.getOffset(), input);
        if (event != null) {
            if (event.isConsumed() || event.getText() == null) {
                return;
            } else {
                input = event.getText();
            }
        }

        Caret caret = new Caret();
        caret.set(endCaret);

        if (textChangeFilter != null || textChangeListener != null) {
            String old = getText();

            if (!textRender.editText(first, second, input, caret)) {
                return;
            }

            // Change Filter
            String text = textRender.getText();
            var textEvent = filterText(0, textRender.getTotalBytes(), text);
            if (textEvent != null) {
                if (textEvent.isConsumed()) {
                    textRender.setText(old);
                    return;
                }
                String newTxt = textEvent.getText();
                if (!Objects.equals(newTxt, text)) {
                    int before = textRender.getTotalCharacters();

                    text = newTxt;
                    if (!textRender.setText(text)) {
                        text = textRender.getText();
                    }

                    int after = textRender.getTotalCharacters();
                    if (after < before) {
                        textRender.moveCaret(caret, 0);
                    } else if (after > before) {
                        textRender.moveCaret(caret, after - before);
                    }
                }
            }
            setLocalText(text);

            startCaret.set(caret);
            endCaret.set(caret);

            invalidateTextSize();
            setCaretVisible();
            slideToCaretLater(1);

            fireTextChange(old);
        } else {
            if (!textRender.editText(first, second, input, caret)) {
                return;
            }

            startCaret.set(caret);
            endCaret.set(caret);

            invalidateTextString();
            invalidateTextSize();
            setCaretVisible();
            slideToCaretLater(1);
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
                if (textChangeListener != null || textChangeFilter != null) {
                    String old = getText();
                    if (textRender.trim(maxCharacters)) {
                        setLocalText(textRender.getText());
                    }

                    textRender.moveCaret(startCaret, 0);
                    textRender.moveCaret(endCaret, 0);

                    invalidateTextSize();
                    fireTextChange(old);
                } else {
                    if (textRender.trim(maxCharacters)) {
                        invalidateTextString();
                    }

                    textRender.moveCaret(startCaret, 0);
                    textRender.moveCaret(endCaret, 0);

                    invalidateTextSize();
                }
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
            invalidate(isWrapContent());
            invalidateTextSize();
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

    public int getTextSelectedColor() {
        return textSelectedColor;
    }

    public void setTextSelectedColor(int textSelectedColor) {
        if (this.textSelectedColor != textSelectedColor) {
            this.textSelectedColor = textSelectedColor;
            invalidate(false);
        }
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isMultiLineEnabled() {
        return multiLineEnabled;
    }

    public void setMultiLineEnabled(boolean multiLineEnabled) {
        if (this.multiLineEnabled != multiLineEnabled) {
            this.multiLineEnabled = multiLineEnabled;
            if (multiLineEnabled && !isTextEmpty()) {
                setText(getText());
            }
        }
    }

    public float getCaretBlinkDuration() {
        return caretBlinkDuration;
    }

    public void setCaretBlinkDuration(float caretBlinkDuration) {
        this.caretBlinkDuration = caretBlinkDuration;
    }

    public int getCaretColor() {
        return caretColor;
    }

    public void setCaretColor(int caretColor) {
        if (this.caretColor != caretColor) {
            this.caretColor = caretColor;
            invalidate(false);
        }
    }

    protected void invalidateTextSize() {
        invalidate(isWrapContent());
        invalidTextSize = true;
    }

    private void invalidateTextString() {
        invalidTextString = true;
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

    public void slideToCaretLater(float speed) {
        if (getActivity() != null) {
            getActivity().runLater(() -> slideToCaret(speed));
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
            targetX = caretX - 1;
        } else if (caretX > targetX + getViewDimensionX()) {
            targetX = caretX - getViewDimensionX() + 1;
        }

        float targetY = getViewOffsetY();
        if (textRender.getTotalLines() <= 1) {
            targetY = 0;
        } else if (caretYMin < targetY) {
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

    protected int getLastCaretPosition() {
        return textRender.getTotalBytes();
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

    public boolean isTextEmpty() {
        return textRender.getTotalCharacters() == 0;
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

    protected class CaretBlink implements Animation {

        private boolean playing;
        private float timer;

        public void play() {
            timer = 0;
            if (getActivity() != null) {
                playing = true;
                getActivity().addAnimation(this);
            }
        }

        public void stop() {
            playing = false;
        }

        @Override
        public Activity getSource() {
            return getActivity();
        }

        @Override
        public boolean isPlaying() {
            return playing;
        }

        @Override
        public void handle(float seconds) {
            timer += seconds;
            if (timer >= getCaretBlinkDuration()) {
                timer = 0;
                blinkCaret();
            }
        }
    }
}
