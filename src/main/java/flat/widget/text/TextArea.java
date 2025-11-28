package flat.widget.text;

import flat.animations.StateInfo;
import flat.events.*;
import flat.graphics.Graphics;
import flat.graphics.symbols.Font;
import flat.math.Vector2;
import flat.math.stroke.BasicStroke;
import flat.uxml.*;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;
import flat.widget.text.content.Caret;
import flat.widget.text.area.TextBox;
import flat.widget.text.content.SelectionPos;
import flat.widget.value.HorizontalScrollBar;
import flat.widget.value.VerticalScrollBar;
import flat.window.Application;

import java.util.Objects;

public class TextArea extends TextEditor {

    private UXValueListener<String> textChangeListener;
    private UXListener<TextEvent> textChangeFilter;
    private UXListener<TextEvent> textInputFilter;
    private UXListener<TextEvent> textTypeListener;
    private String text;
    private boolean invalidTextString;
    
    private int textColor = 0x000000FF;
    private boolean editable = true;
    private boolean multiLineEnabled = true;
    private boolean lineWrapEnabled = true;

    private VerticalAlign verticalAlign = VerticalAlign.TOP;
    private HorizontalAlign horizontalAlign = HorizontalAlign.LEFT;

    private boolean invalidTextSize;
    private float textWidth;
    private float textHintWidth;

    private int maxCharacters = 0;
    
    private final TextBox textBox = new TextBox();
    public TextArea() {
        textBox.setAlign(horizontalAlign);
        textBox.setFont(getTextFont());
        textBox.setTextSize(getTextSize());
        textContent = textBox;
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
        setLineWrapEnabled(attrs.getAttributeBool("line-wrap-enabled", isLineWrapEnabled()));
        setText(attrs.getAttributeString("text", getText()));
        setEditable(attrs.getAttributeBool("editable", isEditable()));
        setTextChangeListener(attrs.getAttributeValueListener("on-text-change", String.class, controller, getTextChangeListener()));
        setTextTypeListener(attrs.getAttributeListener("on-text-type", TextEvent.class, controller, getTextTypeListener()));
        setTextChangeFilter(attrs.getAttributeListener("on-text-change-filter", TextEvent.class, controller, getTextChangeFilter()));
        setTextInputFilter(attrs.getAttributeListener("on-text-input-filter", TextEvent.class, controller, getTextInputFilter()));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();
        
        setTextColor(attrs.getColor("text-color", info, getTextColor()));
        setHidden(attrs.getBool("hidden", info, isHidden()));

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
            mWidth = Math.max(getNaturalTextWidth() + extraWidth, getLayoutMinWidth());
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

    protected float getNaturalTextWidth() {
        return textBox.getNaturalWidth();
    }

    @Override
    public Vector2 onLayoutTotalDimension(float width, float height) {
        if (isLineWrapReallyEnabled()) {
            return new Vector2(getInWidth(), getTextHeight());
        } else {
            return new Vector2(getTextWidth(), getTextHeight());
        }
    }

    @Override
    public void setLayoutScrollOffset(float xx, float yy) {

    }

    private void breakLines() {
        if (isLineWrapReallyEnabled()) {
            int linesBefore = textBox.getTotalLines();
            textBox.breakLines(getViewDimensionX());
            if (linesBefore != textBox.getTotalLines()) {
                textBox.getCaret(startCaret);
                textBox.getCaret(endCaret);
            }
        } else if (textBox.isLineWrapped()) {
            textBox.breakLines(textBox.getNaturalWidth() + 1);
            textBox.getCaret(startCaret);
            textBox.getCaret(endCaret);
        }
    }

    @Override
    public void setLayout(float layoutWidth, float layoutHeight) {
        super.setLayout(layoutWidth, layoutHeight);
        if (getActivity() != null && isLineWrapReallyEnabled() && textBox.isBreakLines(getViewDimensionX())) {
            getActivity().runLater(() -> {
                textBox.breakLines(getViewDimensionX());
                textBox.getCaret(startCaret);
                textBox.getCaret(endCaret);
                invalidate(true);
            });
        }
    }

    protected boolean isLineWrapReallyEnabled() {
        return isLineWrapEnabled() && isMultiLineEnabled() && !isHidden();
    }

    protected Caret getStartCaret() {
        return startCaret;
    }

    protected Caret getEndCaret() {
        return endCaret;
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

    protected void onDrawText(Graphics graphics, float x, float y, float width, float height) {
        if (getOutWidth() <= 0 || getOutHeight() <= 0 || getTextFont() == null || getTextSize() <= 0) {
            return;
        }

        graphics.setTransform2D(getTransform());
        x -= getViewOffsetX();
        y -= getViewOffsetY();
        float xpos = getTextWidth() < width ? xOff(x, x + width, getTextWidth()) : x;
        float ypos = getTextHeight() < height ? yOff(y, y + height, getTextHeight()) : y;

        graphics.setTransform2D(getTransform());
        graphics.setColor(getTextColor());
        graphics.setTextFont(getTextFont());
        graphics.setTextSize(getTextSize());
        graphics.setTextBlur(0);

        Caret first = getFirstCaret();
        Caret second = getSecondCaret();

        float lineH = getLineHeight();
        float firstX = textBox.getCaretHorizontalOffset(first, horizontalAlign) + xpos;
        float secondX = textBox.getCaretHorizontalOffset(second, horizontalAlign) + xpos;

        // Text Selection
        graphics.setColor(getTextSelectedColor());
        if (first.getLine() == second.getLine()) {
            graphics.drawRect(firstX, ypos + first.getLine() * lineH, secondX - firstX, lineH, true);
        } else {
            graphics.drawRect(firstX, ypos + first.getLine() * lineH, getTotalDimensionX() - (firstX - x), lineH, true);
            graphics.drawRect(x, y + second.getLine() * lineH, secondX - x, lineH, true);
            if (first.getLine() + 1 < second.getLine()) {
                graphics.drawRect(
                        x, y + (first.getLine() + 1) * lineH
                        , getTotalDimensionX(), (second.getLine() - first.getLine() - 1) * lineH, true);
            }
        }

        // Text
        if (!isTextEmpty()) {
            graphics.setColor(getTextColor());
            int start = Math.max(0, (int) Math.floor((getViewOffsetY() - getInY()) / lineH) - 1);
            int end = start + Math.max(0, (int) Math.ceil(getHeight() / lineH) + 2);
            textBox.drawText(graphics, xpos, ypos, width * 9999, height * 9999, horizontalAlign, start, end);
        }

        // Caret
        if (isShowCaret() && isEditable()) {
            float caretX = textBox.getCaretHorizontalOffset(endCaret, horizontalAlign);
            if (caretX == 0) {
                caretX += xpos + 1;
            } else if (textBox.isCaretLastOfLine(endCaret)) {
                caretX += xpos - 1;
            } else {
                caretX += xpos;
            }
            graphics.setColor(getCaretColor());
            graphics.setStroke(new BasicStroke(2));
            graphics.drawLine(
                    caretX, ypos + endCaret.getLine() * lineH,
                    caretX, ypos + (endCaret.getLine() + 1) * lineH);
        }
    }

    @Override
    public void pointer(PointerEvent event) {
        super.pointer(event);
        Vector2 point = screenToLocal(event.getX(), event.getY());
        textPointer(event, point);
    }
    
    public boolean isEditable() {
        return editable;
    }
    
    public void setEditable(boolean editable) {
        this.editable = editable;
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
    
    @Override
    public Vector2 getContextMenuTextPosition() {
        float x = getInX();
        float y = getInY();
        float width = getInWidth();
        float height = getInHeight();
        float px = x - getViewOffsetX();
        float py = y - getViewOffsetY();
        float xpos = getTextWidth() < width ? xOff(x, x + width, getTextWidth()) : px;
        float ypos = getTextHeight() < height ? yOff(y, y + height, getTextHeight()) : py;
        
        float caretX = textBox.getCaretHorizontalOffset(endCaret, horizontalAlign) + xpos;
        float caretY = endCaret.getLine() * getLineHeight() + ypos;
        return localToScreen(Math.min(x + width, Math.max(x, caretX)), Math.min(y + height, Math.max(y, caretY)));
    }
    
    @Override
    public void showContextMenu() {
        var pos = getContextMenuTextPosition();
        showContextMenu(pos.x, pos.y);
    }

    public String getText() {
        if (invalidTextString) {
            setLocalText(textBox.getText());
        }
        return text;
    }

    private void setLocalText(String text) {
        this.text = text;
        invalidTextString = false;
    }

    private boolean hidden;

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        if (this.hidden != hidden) {
            this.hidden = hidden;
            textBox.setHidden(hidden);
            invalidateTextSize();
            invalidate(true);
            textBox.moveCaret(startCaret, 0);
            textBox.moveCaret(endCaret, 0);
        }
    }

    public void setText(String text) {
        if (Objects.equals(getText(), text)) return;
        if (!isMultiLineEnabled() && text != null) {
            text = text.replaceAll("[\\n\\r]", "");
            if (Objects.equals(getText(), text)) return;
        }

        // Input Filter
        var event = filterInputText(0, textBox.getTotalBytes(), text);
        if (event != null) {
            if (event.isConsumed() || event.getText() == null) {
                return;
            } else {
                text = event.getText();
            }
        }

        if (textChangeFilter != null || textChangeListener != null) {

            // Change Filter
            var textEvent = filterText(0, textBox.getTotalBytes(), text);
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
                if (!textBox.setText(text)) {
                    setLocalText(textBox.getText());
                }
            } else {
                textBox.setText(text);
            }

            textBox.moveCaretBegin(startCaret);
            endCaret.set(startCaret);
            breakLines();

            invalidateTextSize();

            // Change Listener
            fireTextChange(old);
        } else {
            setLocalText(text);

            if (text != null && maxCharacters > 0) {
                if (!textBox.setText(text)) {
                    invalidateTextString();
                }
            } else {
                textBox.setText(text);
            }

            textBox.moveCaretBegin(startCaret);
            endCaret.set(startCaret);
            breakLines();

            invalidateTextSize();
        }
    }
    
    protected void setTextSilently(String text) {
        if (Objects.equals(getText(), text)) return;
        if (!isMultiLineEnabled() && text != null) {
            text = text.replaceAll("[\\n\\r]", "");
            if (Objects.equals(getText(), text)) return;
        }
        setLocalText(text);
        
        if (text != null && maxCharacters > 0) {
            if (!textBox.setText(text)) {
                invalidateTextString();
            }
        } else {
            textBox.setText(text);
        }
        
        textBox.moveCaretBegin(startCaret);
        endCaret.set(startCaret);
        breakLines();
        invalidateTextSize();
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

        if (textChangeFilter != null || textChangeListener != null || textTypeListener != null || hasLocalFilter()) {
            String old = getText();

            if (!textBox.editText(first, second, input, caret)) {
                return;
            }

            // Change Filter
            String text = textBox.getText();
            String newTxt = localFilter(text);
            
            var textEvent = filterText(0, textBox.getTotalBytes(), newTxt);
            if (textEvent != null) {
                if (textEvent.isConsumed()) {
                    textBox.setText(old);
                    return;
                }
                newTxt = textEvent.getText();
            }
            if (!Objects.equals(newTxt, text)) {
                int before = textBox.getTotalCharacters();
                
                text = newTxt;
                if (!textBox.setText(text)) {
                    text = textBox.getText();
                }
                
                int after = textBox.getTotalCharacters();
                if (after < before) {
                    textBox.moveCaret(caret, 0);
                } else if (after > before) {
                    textBox.moveCaret(caret, after - before);
                }
            }
            setLocalText(text);

            startCaret.set(caret);
            endCaret.set(caret);
            breakLines();

            invalidateTextSize();
            setCaretVisible();
            slideToCaretLater(1);

            fireTextChange(old);
            fireTextType();
        } else {
            if (!textBox.editText(first, second, input, caret)) {
                return;
            }

            startCaret.set(caret);
            endCaret.set(caret);
            breakLines();

            invalidateTextString();
            invalidateTextSize();
            setCaretVisible();
            slideToCaretLater(1);
        }
    }
    
    protected boolean hasLocalFilter() {
        return false;
    }
    
    protected String localFilter(String value) {
        return value;
    }

    public int getMaxCharacters() {
        return maxCharacters;
    }

    public void setMaxCharacters(int maxCharacters) {
        if (this.maxCharacters != maxCharacters) {
            this.maxCharacters = maxCharacters;
            textBox.setMaxCharacters(maxCharacters);
            if (maxCharacters > 0 && maxCharacters < textBox.getTotalCharacters()) {
                if (textChangeListener != null || textChangeFilter != null) {
                    String old = getText();
                    if (textBox.trim(maxCharacters)) {
                        setLocalText(textBox.getText());
                    }

                    textBox.moveCaret(startCaret, 0);
                    textBox.moveCaret(endCaret, 0);

                    invalidateTextSize();
                    fireTextChange(old);
                } else {
                    if (textBox.trim(maxCharacters)) {
                        invalidateTextString();
                    }

                    textBox.moveCaret(startCaret, 0);
                    textBox.moveCaret(endCaret, 0);

                    invalidateTextSize();
                }
            }
        }
    }

    public void setTextFont(Font textFont) {
        if (textFont == null) textFont = Font.getDefault();
        
        if (this.getTextFont() != textFont) {
            super.setTextFont(textFont);
            textBox.setFont(textFont);
            invalidate(isWrapContent());
            invalidateTextSize();
        }
    }

    protected float getLineHeight() {
        return getTextFont().getHeight(getTextSize());
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

    @Override
    public void setTextSize(float textSize) {
        if (this.getTextSize() != textSize) {
            super.setTextSize(textSize);
            textBox.setTextSize(textSize);
            invalidate(isWrapContent());
            invalidateTextSize();
        }
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

    public boolean isLineWrapEnabled() {
        return lineWrapEnabled;
    }

    public void setLineWrapEnabled(boolean lineWrapEnabled) {
        if (this.lineWrapEnabled != lineWrapEnabled) {
            this.lineWrapEnabled = lineWrapEnabled;
            invalidate(true);
        }
    }

    protected void invalidateTextSize() {
        invalidate(true);
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
            textBox.setAlign(horizontalAlign);
            invalidate(false);
        }
    }

    @Override
    public void slideToCaret(float speed) {
        float lineH = getLineHeight();
        float caretX = textBox.getCaretHorizontalOffset(endCaret, horizontalAlign);
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
        if (textBox.getTotalLines() <= 1) {
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
    
    @Override
    public Caret getCaretFromPosition(float mx, float my) {
        Vector2 point = new Vector2(mx, my);
        point.x += getViewOffsetX();
        point.y += getViewOffsetY();
        float x = getVisibleTextX();
        float y = getVisibleTextY();
        float width = getVisibleTextWidth();
        float height = getVisibleTextHeight();
        float xpos = isHorizontalDimensionScroll() ? x : xOff(x, x + width, getTextWidth());
        float ypos = isVerticalDimensionScroll() ? y : yOff(y, y + height, getTextHeight());
        
        Caret caret = new Caret();
        textBox.getCaret(point.x, point.y, xpos, ypos, horizontalAlign, caret);
        return caret;
    }
    
    protected int getLastCaretPosition() {
        return textBox.getTotalBytes();
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

    public UXListener<TextEvent> getTextTypeListener() {
        return textTypeListener;
    }

    public void setTextTypeListener(UXListener<TextEvent> textTypeListener) {
        this.textTypeListener = textTypeListener;
    }

    protected void fireTextType() {
        if (textTypeListener != null) {
            UXListener.safeHandle(textTypeListener, new TextEvent(this, TextEvent.TYPE, 0, textBox.getTotalBytes(), text));
        }
    }

    public boolean isTextEmpty() {
        return textBox.getTotalCharacters() == 0;
    }

    protected float getTextWidth() {
        if (invalidTextSize) {
            invalidTextSize = false;
            textWidth = textBox.getTextWidth();
        }
        return textWidth;
    }

    protected float getTextHeight() {
        return textBox.getTextHeight();
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
}
