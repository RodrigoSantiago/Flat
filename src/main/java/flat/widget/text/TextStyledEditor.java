package flat.widget.text;

import flat.animations.StateInfo;
import flat.events.PointerEvent;
import flat.events.StyledTextEvent;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.graphics.symbols.Font;
import flat.graphics.symbols.FontManager;
import flat.graphics.symbols.FontStyle;
import flat.math.Vector2;
import flat.math.shapes.Rectangle;
import flat.math.stroke.BasicStroke;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.uxml.UXChildren;
import flat.uxml.UXListener;
import flat.widget.text.content.Caret;
import flat.widget.text.content.SelectionRange;
import flat.widget.text.styled.TextStyledContent;
import flat.widget.text.styled.TextStyle;
import flat.widget.text.styled.TextStyleBundle;
import flat.widget.value.HorizontalScrollBar;
import flat.widget.value.VerticalScrollBar;

import java.io.InputStream;
import java.util.Iterator;

public class TextStyledEditor extends TextEditor {
    
    private final TextStyledContent content;
    private UXListener<StyledTextEvent> textTypeListener;
    private UXListener<StyledTextEvent> textInputFilter;
    
    private int textCurrentLineColor;
    private boolean editable = true;
    TextStyledEditorLines plugin;
    
    public TextStyledEditor() {
        content = new TextStyledContent();
        content.setBundle(new TextStyleBundle(new TextStyle(0x000000FF, false, false)));
        setTextFont(getTextFont());
        content.setFontHeight(getTextSize());
        content.setFontWidth(getTextFont().getWidth("M", getTextSize(), 1));
        textContent = content;
    }
    
    void setLinePlugin(TextStyledEditorLines plugin) {
        this.plugin = plugin;
    }
    
    TextStyledEditorLines getLinePlugin() {
        return this.plugin;
    }
    
    @Override
    public void invalidate(boolean layout) {
        super.invalidate(layout);
        if (plugin != null) {
            plugin.invalidate(layout);
        }
    }
    
    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);
        UXAttrs attrs = getAttrs();
        
        setEditable(attrs.getAttributeBool("editable", isEditable()));
        setTextInputFilter(attrs.getAttributeListener("on-text-input-filter", StyledTextEvent.class, controller, getTextInputFilter()));
        setTextTypeListener(attrs.getAttributeListener("on-text-type", StyledTextEvent.class, controller, getTextTypeListener()));
        
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
    public void applyStyle() {
        super.applyStyle();
        
        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();
        
        setTextSize(attrs.getSize("text-size", info, getTextSize()));
        setTextCurrentLineColor(attrs.getColor("text-current-line-color", info, getTextCurrentLineColor()));
    }
    
    @Override
    public Vector2 onLayoutTotalDimension(float width, float height) {
        return new Vector2(getTextWidth(), getTextHeight());
    }
    
    private float getTextWidth() {
        return content.getFontWidth() * content.getWidth(getInY() - getViewOffsetY(), getInHeight());
    }
    
    private float getTextHeight() {
        return content.getFontHeight() * content.getLineCount();
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
    public void pointer(PointerEvent event) {
        super.pointer(event);
        Vector2 point = screenToLocal(event.getX(), event.getY());
        textPointer(event, point);
    }
    
    public void replaceSelection(String string) {
        var first = getFirstCaret();
        var second = getSecondCaret();
        editText(first, second, string);
    }
    
    @Override
    protected void editText(Caret start, Caret end, String text) {
        if (!isEditable()) return;
        
        // Input Filter
        var event = filterInputText(start.getLine(), start.getChars(), end.getLine(), end.getChars(), text);
        if (event != null) {
            if (event.isConsumed() || event.getText() == null) {
                return;
            } else {
                text = event.getText();
            }
        }
        
        content.editText(text, start.getLine(), start.getLineOffset(), end.getLine(), end.getLineOffset(), startCaret);
        endCaret.set(startCaret);
        setCaretVisible();
        slideToCaretLater(1);
        invalidate(true);
        
        fireTextType(start.getLine(), start.getChars(), end.getLine(), end.getChars(), text);
    }
    
    public int getLineCount() {
        return content.getLineCount();
    }
    
    public void setTextFromString(String text) {
        content.createLinesFromString(text);
        endCaret.set(startCaret);
    }
    
    public void setTextFromInput(InputStream is) {
        content.createLinesFromInput(is);
        content.moveCaretBegin(startCaret);
        endCaret.set(startCaret);
    }
    
    public String exportText() {
        var end = new Caret();
        content.moveCaretEnd(end);
        return content.getText(new Caret(), end);
    }
    
    public String exportText(int startLine, int endLine) {
        if (startLine > endLine) {
            int e = startLine;
            startLine = endLine;
            endLine = e;
        }
        var start = new Caret();
        textContent.moveCaretBegin(start);
        textContent.moveCaretVertical(start, startLine);
        var end = new Caret();
        textContent.moveCaretBegin(end);
        textContent.moveCaretVertical(end, endLine);
        textContent.moveCaretLineEnd(end);
        return content.getText(start, end);
    }
    
    public String exportText(int startLine, int startCharacter, int endLine, int endCharacter) {
        if (startLine > endLine) {
            int e = startLine;
            startLine = endLine;
            endLine = e;
        } else if (startLine == endLine && startCharacter > endCharacter) {
            int e = startCharacter;
            startCharacter = endCharacter;
            endCharacter = e;
        }
        var start = new Caret();
        textContent.moveCaretBegin(start);
        textContent.moveCaretVertical(start, startLine);
        textContent.moveCaretHorizontal(start, startCharacter);
        var end = new Caret();
        textContent.moveCaretBegin(end);
        textContent.moveCaretVertical(end, endLine);
        textContent.moveCaretHorizontal(start, endCharacter);
        return content.getText(start, end);
    }
    
    public String exportSelectedText() {
        return content.getText(getFirstCaret(), getSecondCaret());
    }
    
    public String exportLineText(int line) {
        if (line < 0 || line >= content.getLineCount()) return "";
        var start = new Caret();
        var end = new Caret();
        content.moveCaretVertical(start, line);
        end.set(start);
        content.moveCaretLineEnd(end);
        return content.getText(start, end);
    }
    
    public String exportWordAt(int line, int character) {
        if (line < 0 || line >= content.getLineCount()) return "";
        var start = new Caret();
        content.moveCaretVertical(start, line);
        for (int i = 0; i < character; i++) {
            if (textContent.isCaretLastOfLine(start)) {
                break;
            }
            textContent.moveCaretForward(start);
        }
        var end = new Caret();
        end.set(start);
        content.moveCaretWordBackwards(start);
        content.moveCaretForward(end);
        
        return content.getText(start, end);
    }
    
    public SelectionRange getSelectionRange() {
        var first = getFirstCaret();
        var second = getSecondCaret();
        return new SelectionRange(first.getLine(), first.getChars(), second.getLine(), second.getChars());
    }
    
    public String getCurrentLine() {
        var start = new Caret(endCaret);
        content.moveCaretLineBegin(start);
        var end = new Caret(endCaret);
        content.moveCaretLineEnd(end);
        return content.getText(start, end);
    }
    
    public String getCurrentLineSubstring() {
        var start = new Caret(endCaret);
        content.moveCaretLineBegin(start);
        return content.getText(start, endCaret);
    }
    
    public String getCurrentWord() {
        var start = new Caret(endCaret);
        content.moveCaretWordBackwards(start);
        var end = new Caret(endCaret);
        content.moveCaretWordForward(end);
        return content.getText(start, end);
    }
    
    public String getCurrentWordSubstring() {
        var start = new Caret(endCaret);
        content.moveCaretWordBackwards(start);
        return content.getText(start, endCaret);
    }
    
    public void setTextStyleBundle(TextStyleBundle bundle) {
        content.setBundle(bundle);
        invalidate(false);
    }
    
    public void setLineColor(int line, int color) {
        content.setLineColor(line, color);
    }
    
    public void clearLinesColor() {
        content.clearLinesColor();
    }
    
    public Iterable<String> iterateLines(int start, int end) {
        return () -> content.iterateLines(start, end);
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
        
        graphics.setTransform2D(getTransform());
        
        if (isHorizontalDimensionScroll() || isVerticalDimensionScroll()) {
            graphics.pushClip(getBackgroundShape());
        }
        
        x -= getViewOffsetX();
        y -= getViewOffsetY();
        
        Caret first = getFirstCaret();
        Caret second = getSecondCaret();
        
        float lineH = content.getFontHeight();
        float firstX = x + first.getChars() * content.getFontWidth();
        float secondX = x + second.getChars() * content.getFontWidth();
        
        // Current Line
        if (Color.getAlpha(getTextCurrentLineColor()) > 0) {
            graphics.setColor(getTextCurrentLineColor());
            graphics.drawRect(x, y + second.getLine() * lineH, getTotalDimensionX(), lineH, true);
        }
        
        // Text Selection
        graphics.setColor(getTextSelectedColor());
        if (first.getLine() == second.getLine()) {
            graphics.drawRect(firstX, y + first.getLine() * lineH, secondX - firstX, lineH, true);
        } else {
            graphics.drawRect(firstX, y + first.getLine() * lineH, getTotalDimensionX() - (firstX - x), lineH, true);
            graphics.drawRect(x, y + second.getLine() * lineH, secondX - x, lineH, true);
            if (first.getLine() + 1 < second.getLine()) {
                graphics.drawRect(
                        x, y + (first.getLine() + 1) * lineH
                        , getTotalDimensionX(), (second.getLine() - first.getLine() - 1) * lineH, true);
            }
        }
        
        // Text
        graphics.setTextFont(getTextFont());
        graphics.setTextSize(getTextSize());
        graphics.setColor(Color.black);
        content.drawText(graphics, x, y, width, getOutHeight());
        
        // Caret
        if (isShowCaret() && isEditable()) {
            float caretX = endCaret.getChars() * content.getFontWidth();
            if (caretX == 0) {
                caretX += x + 1;
            } else if (content.isCaretLastOfLine(endCaret)) {
                caretX += x - 1;
            } else {
                caretX += x;
            }
            graphics.setColor(getCaretColor());
            graphics.setStroke(new BasicStroke(2));
            graphics.drawLine(
                    caretX, y + endCaret.getLine() * lineH,
                    caretX, y + (endCaret.getLine() + 1) * lineH);
        }
        
        
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
    
    public void drawLines(Graphics graphics, TextStyledEditorLines lines, float lx, float ly, float lwidth, float lheight) {
        float yOff = getViewOffsetY();
        graphics.pushClip(lines.getBackgroundShape());
        content.drawLineNumbers(graphics, yOff, lx, ly, lwidth, lheight, lines.getLineNumberTextSize(),
                lines.getLineNumberColor(), lines.getSelectedLneNumberColor(), endCaret.getLine());
        graphics.popClip();
    }
    
    @Override
    public void setTextFont(Font textFont) {
        if (textFont == null || textFont.getStyle() != FontStyle.MONO) textFont = FontManager.findFont(FontStyle.MONO);
        
        if (this.getTextFont() != textFont) {
            super.setTextFont(textFont);
            content.setFontHeight(getTextSize());
            content.setFontWidth(textFont.getWidth("M", getTextSize(), 1));
        }
    }
    
    @Override
    public void setTextSize(float textSize) {
        if (this.getTextSize() != textSize) {
            super.setTextSize(textSize);
            content.setFontHeight(textSize);
            content.setFontWidth(getTextFont().getWidth("M", getTextSize(), 1));
        }
    }
    
    @Override
    public Vector2 getContextMenuTextPosition() {
        float x = getInX();
        float y = getInY();
        float width = getInWidth();
        float height = getInHeight();
        float px = x - getViewOffsetX();
        float py = y - getViewOffsetY();
        
        float caretX = endCaret.getChars() * content.getFontWidth() + px;
        float caretY = (endCaret.getLine() + 1) * content.getFontHeight() + py;
        return localToScreen(Math.min(x + width, Math.max(x, caretX)), Math.min(y + height, Math.max(y, caretY)));
    }
    
    @Override
    public void showContextMenu() {
        var pos = getContextMenuTextPosition();
        showContextMenu(pos.x, pos.y);
    }
    
    @Override
    public Caret getCaretFromPosition(float mx, float my) {
        Vector2 point = new Vector2(mx, my);
        point.x += getViewOffsetX();
        point.y += getViewOffsetY();
        point.x -= getInX();
        point.y -= getInY();
        
        Caret caret = new Caret();
        content.getCaret(point.x, point.y, caret);
        return caret;
    }
    
    @Override
    public void slideToCaretLater(float speed) {
        if (getActivity() != null) {
            getActivity().runLater(() -> slideToCaret(speed));
        }
    }
    
    @Override
    public void slideToCaret(float speed) {
        float lineH = content.getFontHeight();
        float caretX = content.getFontWidth() * endCaret.getChars();
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
        if (content.getTotalLines() <= 1) {
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
    
    public UXListener<StyledTextEvent> getTextInputFilter() {
        return textInputFilter;
    }
    
    public void setTextInputFilter(UXListener<StyledTextEvent> textInputFilter) {
        this.textInputFilter = textInputFilter;
    }
    
    private StyledTextEvent filterInputText(int startLine, int startChar, int endLine, int endChar, String text) {
        if (textInputFilter != null) {
            StyledTextEvent event = new StyledTextEvent(this, StyledTextEvent.FILTER, startLine, startChar, endLine, endChar, text);
            UXListener.safeHandle(textInputFilter, event);
            return event;
        }
        return null;
    }
    
    public UXListener<StyledTextEvent> getTextTypeListener() {
        return textTypeListener;
    }
    
    public void setTextTypeListener(UXListener<StyledTextEvent> textTypeListener) {
        this.textTypeListener = textTypeListener;
    }
    
    protected void fireTextType(int startLine, int startChar, int endLine, int endChar, String text) {
        if (textTypeListener != null) {
            StyledTextEvent event = new StyledTextEvent(this, StyledTextEvent.TYPE, startLine, startChar, endLine, endChar, text);
            UXListener.safeHandle(textTypeListener, event);
        }
    }
    
    public boolean isEditable() {
        return editable;
    }
    
    public void setEditable(boolean editable) {
        this.editable = editable;
    }
    
    public int getTextCurrentLineColor() {
        return textCurrentLineColor;
    }
    
    public void setTextCurrentLineColor(int textCurrentLineColor) {
        if (this.textCurrentLineColor != textCurrentLineColor) {
            this.textCurrentLineColor = textCurrentLineColor;
            invalidate(false);
        }
    }
}
