package flat.widget.text;

import flat.animations.StateInfo;
import flat.events.KeyCode;
import flat.events.KeyEvent;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.graphics.context.Font;
import flat.graphics.image.Drawable;
import flat.graphics.text.Align;
import flat.math.Vector2;
import flat.resources.Resource;
import flat.uxml.Controller;
import flat.uxml.UXStyle;
import flat.uxml.UXStyleAttrs;
import flat.widget.Widget;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class TextField extends Widget {

    private byte[] text;
    private String string;
    private int size, capacity;

    int cursorPos, cursorStart, selStart, selEnd; //UTF-8 Positions (+ 0.1 fixer)
    int cursorLine, cursorStartLine, selStartLine, selEndLine;

    private Font font;
    private float textSize;
    private int textColor;
    private int selectionColor;

    private boolean singleLine = true;
    private boolean wrapText;
    private int minLines = 1, maxLines = Integer.MAX_VALUE;

    private String labelText;
    private Font labelFont;
    private float labelTextSize;
    private int labelTextColor;
    private float highLabelTextSize;
    private int highLabelTextColor;

    private String helpText;
    private Font helpFont;
    private float helpTextSize;
    private int helpTextColor;

    private Drawable leadingIcon;
    private Drawable activationIcon;
    private float leadingSpacing;
    private float activationSpacing;

    private int lines;
    private float scrollX, scrollY;
    private float offsetLine;
    private boolean invalidTextSize;
    private float textWidth, textHeight;
    private float wrapWidth;

    private ByteBuffer buffer;

    private SpanManager spanManager = new SpanManager();

    public TextField() {
        size = 0;
        capacity = 64;
        text = new byte[capacity];
        buffer = ByteBuffer.allocateDirect(capacity);
    }

    @Override
    public void applyAttributes(UXStyleAttrs style, Controller controller) {
        super.applyAttributes(style, controller);

        setText(style.asString("text", getText()));
        setLabelText(style.asString("label-text", getLabelText()));
        setHelpText(style.asString("help-text", getHelpText()));
        setSingleLine(style.asBool("single-line", isSingleLine()));
        setWrapText(style.asBool("wrap-text", isWrapText()));

        setMinLines((int) style.asNumber("min-lines", getMinLines()));
        setMaxLines((int) style.asNumber("max-lines", getMaxLines()));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();
        UXStyle style = getStyle();
        if (style == null) return;

        StateInfo info = getStateInfo();

        setFont(style.asFont("font", info, getFont()));
        setTextSize(style.asSize("text-size", info, getTextSize()));
        setTextColor(style.asColor("text-color", info, getTextColor()));
        setSelectionColor(style.asColor("selection-color", info, getSelectionColor()));

        setLabelFont(style.asFont("label-font", info, getLabelFont()));
        setLabelTextSize(style.asSize("label-text-size", info, getLabelTextSize()));
        setLabelTextColor(style.asColor("label-text-color", info, getLabelTextColor()));
        setHighLabelTextSize(style.asSize("high-label-text-size", info, getHighLabelTextSize()));
        setHighLabelTextColor(style.asColor("high-label-text-color", info, getHighLabelTextColor()));

        setHelpFont(style.asFont("help-font", info, getHelpFont()));
        setHelpTextSize(style.asSize("help-text-size", info, getHelpTextSize()));
        setHelpTextColor(style.asColor("help-text-color", info, getHelpTextColor()));

        Resource res = getStyle().asResource("leading-icon", info);
        if (res != null) {
            Drawable drawable = res.getDrawable();
            if (drawable != null) {
                setLeadingIcon(drawable);
            }
        }
        res = getStyle().asResource("activation-icon", info);
        if (res != null) {
            Drawable drawable = res.getDrawable();
            if (drawable != null) {
                setActivationIcon(drawable);
            }
        }
        setLeadingSpacing(style.asSize("leading-spacing", info, getLeadingSpacing()));
        setActivationSpacing(style.asSize("activation-spacing", info, getActivationSpacing()));
    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);

        //Affine transform = context.getTransform2D();
        //Shape shape = backgroundClip(context);

        float x = getInX();
        float y = getInY();
        float w = getInWidth();
        float h = getInHeight();
        context.setTransform2D(getTransform());
        context.setTextFont(font);
        context.setTextSize(textSize);
        context.setColor(textColor);
        context.setTextVerticalAlign(Align.Vertical.TOP);
        context.setTextHorizontalAlign(Align.Horizontal.LEFT);

        float lineHeight = font.getHeight(textSize);

        context.setColor(textColor);

        Span span;
        spanManager.setWidth(w);
        for (spanManager.setPos(0); spanManager.hasNext();) {
            span = spanManager.next();

            toBuffer(span.start, span.len);

            if (selStart != selEnd || selStartLine != selEndLine) {
                boolean ss = (selStartLine == span.id);
                boolean se = (selEndLine == span.id);
                context.setColor(selectionColor);
                if (ss && se) {
                    float offs = font.getWidth(buffer, 0, selStart - span.start, textSize, 1);
                    float offe = font.getWidth(buffer, 0, selEnd - span.start, textSize, 1);
                    context.drawRect(x + offs, y, offe - offs, lineHeight, true);
                } else if (ss) {
                    float offs = font.getWidth(buffer, 0, selStart - span.start, textSize, 1);
                    context.drawRect(x + offs, y, w - offs, lineHeight, true);
                } else if (se) {
                    float offe = font.getWidth(buffer, 0, selEnd - span.start, textSize, 1);
                    context.drawRect(x, y, offe, lineHeight, true);
                } else if (selStartLine < span.id && selEndLine > span.id) {
                    context.drawRect(x, y, w, lineHeight, true);
                }
            }

            context.setColor(textColor);
            context.drawText(x + span.x, y, buffer, 0, span.len);

            if ((isFocused() || isPressed()) && cursorLine == span.id) {
                float off = font.getWidth(buffer, 0, cursorPos - span.start, textSize, 1);
                context.setColor(textColor);
                context.drawRect((float) Math.floor(x + span.x + off), y, lineHeight / 16f, lineHeight, true);
            }

            if (span.line) {
                y += lineHeight;
            }

            if (y > h) {
                break;
            }
        }

        //context.setTransform2D(transform);
        //context.setClip(shape);
    }

    @Override
    public void firePointer(PointerEvent pointerEvent) {
        super.firePointer(pointerEvent);

        if (!pointerEvent.isConsumed()) {
            if (pointerEvent.getType() == PointerEvent.PRESSED) {
                cursorPos = getPositionIndex(pointerEvent.getX(), pointerEvent.getY(), true);
                cursorStart = cursorPos;
                cursorStartLine = cursorLine;
                selStart = cursorPos;
                selStartLine = cursorLine;
                selEnd = cursorPos;
                selEndLine = cursorLine;
                invalidate(true);
            }
            if (pointerEvent.getType() == PointerEvent.DRAGGED) {
                cursorPos = getPositionIndex(pointerEvent.getX(), pointerEvent.getY(), true);
                if (cursorPos < cursorStart) {
                    selStart = cursorPos;
                    selStartLine = cursorLine;
                    selEnd = cursorStart;
                    selEndLine = cursorStartLine;
                } else {
                    selStart = cursorStart;
                    selStartLine = cursorStartLine;
                    selEnd = cursorPos;
                    selEndLine = cursorLine;
                }

                invalidate(true);
            }
        }
    }

    @Override
    public void fireKey(KeyEvent keyEvent) {
        super.fireKey(keyEvent);

        if (!keyEvent.isConsumed()) {
            if (keyEvent.getType() == KeyEvent.PRESSED || keyEvent.getType() == KeyEvent.REPEATED) {
                if (keyEvent.getKeycode() == KeyCode.KEY_LEFT) {
                    setCursorPos(findPrev(cursorPos));
                }
                if (keyEvent.getKeycode() == KeyCode.KEY_RIGHT) {
                    setCursorPos(findNext(cursorPos));
                }

                if (keyEvent.getKeycode() == KeyCode.KEY_BACKSPACE) {
                    if (selStart != selEnd) {
                        replace(selStart, selEnd - selStart, "");
                        selStart = selEnd = cursorPos;
                        selStartLine = selEndLine = cursorLine;
                    } else {
                        int back = findPrev(selStart);
                        replace(back, selStart - back, "");
                        selStart = selEnd = cursorPos;
                        selStartLine = selEndLine = cursorLine;
                    }
                }
                if (keyEvent.getKeycode() == KeyCode.KEY_DELETE) {
                    if (selStart != selEnd) {
                        replace(selStart, selEnd - selStart, "");
                        selStart = selEnd = cursorPos;
                        selStartLine = selEndLine = cursorLine;
                    } else {
                        int next = findNext(selStart);
                        replace(selStart, next - selStart, "");
                        selStart = selEnd = cursorPos;
                        selStartLine = selEndLine = cursorLine;
                    }
                }
                if (keyEvent.getKeycode() == KeyCode.KEY_ENTER && !singleLine) {
                    replace(selStart, selEnd - selStart, "\n");
                    selStart = selEnd = cursorPos;
                    selStartLine = selEndLine = cursorLine;
                }
            }
            if (keyEvent.getType() == KeyEvent.TYPED) {
                if (keyEvent.getChar() != null) {
                    replace(selStart, selEnd - selStart, keyEvent.getChar());
                    selStart = selEnd = cursorPos;
                    selStartLine = selEndLine = cursorLine;
                }
            }
        }
    }

    @Override
    public void onMeasure() {
        float mWidth = getPrefWidth();
        float mHeight = getPrefHeight();

        if (mWidth == WRAP_CONTENT || mHeight == WRAP_CONTENT) {
            updatePrefSize(mWidth);
        }
        if (mWidth == WRAP_CONTENT) {
            mWidth = textWidth;
        }
        if (mHeight == WRAP_CONTENT) {
            mHeight = textHeight;
        }
        mWidth += getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        mHeight += getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();
        setMeasure(mWidth, mHeight);
    }

    @Override
    public void onLayout(float width, float height) {
        super.onLayout(width, height);
        if (getWidth() != wrapWidth) {
            wrapWidth = getWidth();
            if (!singleLine && wrapText) {
                invalidTextSize = true;
                invalidate(true);
            }
        }
    }

    void updatePrefSize(float width) {
        if (invalidTextSize) {

            invalidTextSize = false;
            textWidth = 0;
            textHeight = 0;

            float w = (!singleLine && wrapText) ? wrapWidth: width == 0 ? MATCH_PARENT : width;
            float lineHeight = font.getHeight(textSize);

            SpanManager sm = spanManager.getWidth() == w ? spanManager : new SpanManager(false);
            sm.setWidth(w);

            Span span = null;
            for (sm.setPos(0); sm.hasNext(); ) {
                span = sm.next();
                if (span.width > textWidth) {
                    textWidth = span.width;
                }
            }
            textHeight = ((span == null ? 0 : span.id) + 1) * lineHeight;
        }
    }

    void ensureCapacity(int size) {
        if (capacity < size) {
            while (capacity < size) {
                capacity *= 2;
            }
            byte[] text = new byte[capacity];
            System.arraycopy(this.text, 0, text, 0, this.size);
            this.text = text;
        }
    }

    void toBuffer(int pos, int len) {
        if (buffer.capacity() < len) {
            int cap = buffer.capacity();
            while (cap < len) cap *= 2;
            buffer = ByteBuffer.allocateDirect(cap);
        }

        buffer.position(0);
        buffer.put(text, pos, len);
    }

    int findNext(int pos) {
        do {
            if (pos >= size - 1) return size;
            pos++;
        } while ((text[pos] & 0xC0) == 0x80);
        return pos;
    }

    int findPrev(int pos) {
        do {
            if (pos <= 0) return 0;
            pos--;
        } while ((text[pos] & 0xC0) == 0x80);
        return pos;
    }

    public void replace(int start, int length, String str) {
        if (singleLine) {
            str = str.replaceAll("\n", "");
        }

        start = Math.min(size, Math.max(0, start));
        length = Math.min(size - start, Math.max(0, length));

        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        int offset = bytes.length - length;

        if (offset == 0) {
            boolean equal = true;
            for (int i = 0; i < bytes.length; i++) {
                if (this.text[i + start] != bytes[i]) {
                    equal = false;
                    break;
                }
            }
            if (equal) return;
        }

        spanManager.setWidth(getInWidth());
        Span span = spanManager.find(start);

        ensureCapacity (size + offset);
        System.arraycopy(this.text, start + length, this.text, start + length + offset, size - (start + length));
        System.arraycopy(bytes, 0, this.text, start, bytes.length);
        size += offset;

        spanManager.invalidate(span == null ? 0 : span.id);

        if (cursorPos >= start && cursorPos < start + length) {
            cursorPos = start + bytes.length;
            Span cLine = spanManager.findFoward(cursorLine, cursorPos);
            cursorLine = cLine == null ? 0 : cLine.id;
        } else if (cursorPos >= start + length) {
            cursorPos += offset;
            Span cLine = spanManager.findFoward(cursorLine, cursorPos);
            cursorLine = cLine == null ? 0 : cLine.id;
        }

        if (selStart >= start && selStart < start + length) {
            selStart = start + bytes.length;
        } else if (selStart >= start + length) {
            selStart += offset;
        }
        if (selEnd >= start && selEnd < start + length) {
            selEnd = start + bytes.length;
        } else if (selEnd >= start + length) {
            selEnd += offset;
        }

        if (selStart == cursorPos) {
            selStartLine = cursorLine;
        } else {
            Span cLine = spanManager.find(selStart);
            selStartLine = cLine == null ? cursorLine : cLine.id;
        }
        if (selEnd == cursorPos) {
            selEndLine = cursorLine;
        } else {
            Span cLine = spanManager.find(selEnd);
            selEndLine = cLine == null ? cursorLine : cLine.id;
        }

        string = null;
        invalidTextSize = true;
        invalidate(true);
    }

    public int getCursorPos() {
        return cursorPos;
    }

    public void setCursorPos(int cursorPos) {
        if (cursorPos < 0) cursorPos = 0;
        if (cursorPos > size) cursorPos = size;

        if (this.cursorPos != cursorPos || selStart != cursorPos || selEnd != cursorPos) {
            boolean less = cursorPos < this.cursorPos;
            this.cursorPos = cursorPos;

            Span cLine = less ? spanManager.findBackward(cursorLine, cursorPos) : spanManager.findFoward(cursorLine, cursorPos);
            cursorLine = cLine == null ? 0 : cLine.id;

            selStart = cursorPos;
            selEnd = cursorPos;

            invalidate(false);
        }
    }

    public int getPositionIndex(float x, float y) {
        return getPositionIndex(x, y, false);
    }

    int getPositionIndex(float x, float y, boolean sCursorLine) {
        Vector2 point = new Vector2(x, y);
        screenToLocal(point);
        x = point.x - getPaddingLeft() - getMarginLeft();
        y = Math.max(0, point.y - getPaddingTop() - getMarginTop());

        int iLine = 0;
        int index = -1;
        float line = 0;
        float lineHeight = font.getHeight(textSize);

        Span span;
        for (spanManager.setPos(0); spanManager.hasNext();) {
            span = spanManager.next();
            if ((line <= y && line + lineHeight >= y) || (!spanManager.hasNext() && index == -1)) {
                int len = span.len;
                if (len > 0 && span.start + len - 1 < size && text[span.start + len - 1] == '\n') {
                    len --;
                }

                toBuffer(span.start, len);
                int off = font.getOffset(buffer, 0, len, textSize, 1, x - span.x, true);
                if (off == 0) {
                    if (index > -1) {
                        break;
                    } else {
                        iLine = span.id;
                        index = span.start;
                    }
                } else if (off < len) {
                    iLine = span.id;
                    index = span.start + off;
                    break;
                } else {
                    iLine = span.id;
                    index = span.start + len;
                }
            } else if (index > -1) {
                break;
            }

            if (span.line) {
                line += lineHeight;
            }
        }
        if (sCursorLine) {
            cursorLine = iLine;
        }
        return index > -1 ? index : size;
    }

    public String getText() {
        if (string == null) {
            string = new String(text, 0, size, StandardCharsets.UTF_8);
        }
        return string;
    }

    public void setText(String text) {
        replace(0, size, text);
        invalidate(true);
    }

    public boolean isSingleLine() {
        return singleLine;
    }

    public void setSingleLine(boolean singleLine) {
        if (this.singleLine != singleLine) {
            this.singleLine = singleLine;
            int cPos = cursorPos;
            if (singleLine) {
                replace(0, size, getText());
            }
            setCursorPos(cPos);

            invalidTextSize = true;
            invalidate(true);
        }
    }

    public boolean isWrapText() {
        return wrapText;
    }

    public void setWrapText(boolean wrapText) {
        if (this.wrapText != wrapText) {
            this.wrapText = wrapText;
            invalidTextSize = true;
            invalidate(true);
        }
    }

    public int getMaxLines() {
        return maxLines;
    }

    public void setMaxLines(int maxLines) {
        if (maxLines < 1) maxLines = 1;

        if (this.maxLines != maxLines) {
            this.maxLines = maxLines;

            if (this.maxLines < this.minLines) {
                this.minLines = this.maxLines;
            }

            invalidTextSize = true;
            invalidate(true);
        }
    }

    public int getMinLines() {
        return minLines;
    }

    public void setMinLines(int minLines) {
        if (minLines < 1) minLines = 1;

        if (this.minLines != minLines) {
            this.minLines = minLines;

            if (this.minLines > this.maxLines) {
                this.maxLines = this.minLines;
            }

            invalidTextSize = true;
            invalidate(true);
        }
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        if (this.font != font) {
            this.font = font;
            invalidTextSize = true;
            invalidate(true);
        }
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        if (this.textSize != textSize) {
            this.textSize = textSize;
            invalidTextSize = true;
            invalidate(true);
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

    public int getSelectionColor() {
        return selectionColor;
    }

    public void setSelectionColor(int selectionColor) {
        if (this.selectionColor != selectionColor) {
            this.selectionColor = selectionColor;
            invalidate(false);
        }
    }

    public Font getLabelFont() {
        return labelFont;
    }

    public void setLabelFont(Font labelFont) {
        if (this.labelFont != labelFont) {
            this.labelFont = labelFont;
            invalidate(true);
        }
    }

    public String getLabelText() {
        return labelText;
    }

    public void setLabelText(String labelText) {
        if (!Objects.equals(this.labelText, labelText)) {
            this.labelText = labelText;
            invalidate(false);
        }
    }

    public float getLabelTextSize() {
        return labelTextSize;
    }

    public void setLabelTextSize(float labelTextSize) {
        if (this.labelTextSize != labelTextSize) {
            this.labelTextSize = labelTextSize;
            invalidate(true);
        }
    }

    public int getLabelTextColor() {
        return labelTextColor;
    }

    public void setLabelTextColor(int labelTextColor) {
        if (this.labelTextColor != labelTextColor) {
            this.labelTextColor = labelTextColor;
            invalidate(false);
        }
    }

    public float getHighLabelTextSize() {
        return highLabelTextSize;
    }

    public void setHighLabelTextSize(float highLabelTextSize) {
        if (this.highLabelTextSize != highLabelTextSize) {
            this.highLabelTextSize = highLabelTextSize;
            invalidate(true);
        }
    }

    public int getHighLabelTextColor() {
        return highLabelTextColor;
    }

    public void setHighLabelTextColor(int highLabelTextColor) {
        if (this.highLabelTextColor != highLabelTextColor) {
            this.highLabelTextColor = highLabelTextColor;
            invalidate(false);
        }
    }

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        if (!Objects.equals(this.helpText, helpText)) {
            this.helpText = helpText;
            invalidate(true);
        }
    }

    public Font getHelpFont() {
        return helpFont;
    }

    public void setHelpFont(Font helpFont) {
        if (this.helpFont != helpFont) {
            this.helpFont = helpFont;
            invalidate(true);
        }
    }

    public float getHelpTextSize() {
        return helpTextSize;
    }

    public void setHelpTextSize(float helpTextSize) {
        if (this.helpTextSize != helpTextSize) {
            this.helpTextSize = helpTextSize;
            invalidate(true);
        }
    }

    public int getHelpTextColor() {
        return helpTextColor;
    }

    public void setHelpTextColor(int helpTextColor) {
        if (this.helpTextColor != helpTextColor) {
            this.helpTextColor = helpTextColor;
            invalidate(false);
        }
    }

    public Drawable getLeadingIcon() {
        return leadingIcon;
    }

    public void setLeadingIcon(Drawable leadingIcon) {
        if (this.leadingIcon != leadingIcon) {
            this.leadingIcon = leadingIcon;
            invalidate(true);
        }
    }

    public Drawable getActivationIcon() {
        return activationIcon;
    }

    public void setActivationIcon(Drawable activationIcon) {
        if (this.activationIcon != activationIcon) {
            this.activationIcon = activationIcon;
            invalidate(true);
        }
    }

    public float getLeadingSpacing() {
        return leadingSpacing;
    }

    public void setLeadingSpacing(float leadingSpacing) {
        if (this.leadingSpacing != leadingSpacing) {
            this.leadingSpacing = leadingSpacing;
            invalidate(true);
        }
    }

    public float getActivationSpacing() {
        return activationSpacing;
    }

    public void setActivationSpacing(float activationSpacing) {
        if (this.activationSpacing != activationSpacing) {
            this.activationSpacing = activationSpacing;
            invalidate(true);
        }
    }

    class Span {
        int id;
        int start, end, len;
        float width, x;
        boolean line;
        boolean midline;

        Span(int id, int start, int len, boolean line, float x, float width) {
            this.id = id;
            this.start = start;
            this.len = len;
            this.end = start + len;
            this.line = line;
            this.x = x;
            this.width = width;
        }

        @Override
        public String toString() {
            return id + "[" + start + "," + end + "]" + new String(text, start, len, StandardCharsets.UTF_8);
        }
    }

    class SpanManager {

        // Index do cursor atual
        int cur;

        // Index do proximo cursor
        int next;

        // Index do proximo cursor no caso de warp em texto sem espaco
        int wnext = -1;

        // Index da ultima quebra de linha
        int prev;

        // Ultimo Span criado
        Span span;

        // Largura atual em linha
        float width;
        float moveX;

        // Linhas Encontradas
        int count;

        boolean cache;
        ArrayList<Span> spans = new ArrayList<>();

        SpanManager() {
            this(true);
        }

        SpanManager(boolean cache) {
            this.cache = cache;
        }

        public float getWidth() {
            return width;
        }

        public void setWidth(float width) {
            if (this.width != width) {
                this.width = width;
                if (!singleLine && wrapText) {
                    invalidate(0);
                }
            }
        }
        Span get() {
            Span oSpan = null;

            if (singleLine) {
                cur = next;
                if (cur < size) {
                    oSpan = new Span(count++, 0, size, true, 0, font.getWidth(buffer, 0, size, textSize, 1));
                    next = size;
                }
            } else if (!wrapText) {
                cur = next;
                for ( ;cur < size; ) {
                    byte b = text[cur];
                    next = findNext(cur);
                    if (b == '\n' || next >= size) {
                        int len = prev - next;
                        toBuffer(prev, len);
                        oSpan = new Span(count++, prev, len, true, 0, font.getWidth(buffer, 0, len, textSize, 1));
                        prev = next;
                        break;
                    }
                    cur = next;
                }
            } else {
                if (wnext == -1) {
                    cur = next;
                }
                for (; cur < size; ) {
                    char b = (char) text[cur];
                    if (wnext == -1) {
                        next = findNext(cur);
                    }
                    if (b == '_' || b == '\n' || next >= size) {
                        int len = next - prev;
                        toBuffer(prev, len);

                        int cOff = font.getOffset(buffer, 0, len, textSize, 1, width, false);
                        if (len > 0 && cOff == 0) {
                            cOff ++;
                        }

                        float tw = font.getWidth(buffer, 0, cOff, textSize, 1);

                        if (moveX > 0 && moveX + tw > width) {
                            moveX = 0;
                            if (span != null) {
                                span.line = true;
                            }
                        }

                        boolean line = (cOff < len || b == '\n' || next >= size);
                        if (span != null && !span.line) {
                            span.len += cOff;
                            span.end += cOff;
                            span.width += tw;
                            span.line = line;
                            if (!span.line) {
                                oSpan = span;
                            }
                        } else {
                            oSpan = new Span(count++, prev, cOff, line, moveX, tw);
                            oSpan.midline = cOff < len || wnext != -1;
                        }

                        prev += cOff;
                        moveX += tw;
                        if (cOff < len) {
                            moveX = 0;
                            wnext = cur + len;
                        } else {
                            wnext = -1;
                            if (b == '\n') {
                                moveX = 0;
                            }
                        }
                        break;
                    }
                    cur = next;
                }
            }
            return oSpan;
        }

        public Span get(int index) {
            Span oSpan = null;
            if (count < index) {
                while (count < index && hasNext()) {
                    oSpan = next();
                }
            } else {
                int pos = count;
                setPos(index + 1);
                setPos(pos);
                oSpan = spans.get(index);
            }
            return oSpan;
        }

        public Span findFoward(int spanID, int position) {
            setPos(spanID);
            while (hasNext()) {
                Span s = next();
                if (s.start <= position && s.end > position) {
                    return s;
                }
            }

            setPos(0);
            while (hasNext()) {
                Span s = next();
                if (s.start <= position && s.end > position) {
                    return s;
                }
                if (count >= spanID) {
                    return null;
                }
            }
            return null; // assert
        }

        public Span findBackward(int spanID, int position) {
            setPos(spanID);
            while (spanID > 0) {
                Span s = get(spanID--);
                if (s != null && s.start <= position && s.end > position) {
                    return s;
                }
            }

            setPos(spanID);
            while (hasNext()) {
                Span s = next();
                if (s.start <= position && s.end > position) {
                    return s;
                }
            }
            return null;
        }

        public Span find(int position) {
            Span s = get(cursorLine);
            if (s != null && s.start <= position && s.end > position) {
                return s;
            }

            setPos(0);
            while (hasNext()) {
                s = next();
                if (s.start <= position && s.end > position) {
                    return s;
                }
            }
            return spans.get(spans.size() - 1);
        }

        public void invalidate(int line) {
            line -= 1;
            line = Math.min(spans.size() - 1, Math.max(0, line));

            Span lSpan = null;

            if (line >= 0) {
                lSpan = spans.get(line);
                while (lSpan.midline) {
                    if (line > 0) {
                        lSpan = spans.get(--line);
                    } else {
                        break;
                    }
                }
                spans.subList(line, spans.size()).clear();
            }

            System.out.println("Invalidado : " + line);
            if (line <= 0) {
                count = 0;
                prev = 0;
                next = 0;
                cur = 0;
                moveX = 0;
                wnext = -1;
                span = null;
            } else {
                count = line;
                prev = lSpan.start;
                next = lSpan.start;
                cur = lSpan.start;
                moveX = 0;
                wnext = -1;
                span = null;
                span = get();
            }
        }

        public void setPos(int count) {
            if (count < this.count) {
                this.count = count;
            } else {
                while (hasNext() && this.count < count) {
                    next();
                }
            }
        }

        public boolean hasNext() {
            return count < spans.size() || count == 0 || cur < size || (cur == size && text[size - 1] == '\n');
        }

        public Span next() {
            int c = count;

            if (count < spans.size()) {
                //System.out.println("Reusar "+c+"("+(spans.get(count).id)+"){"+spans.get(count)+"}");
                return spans.get(count++);
            }

            if (cur == size) {
                Span oSpan = new Span(count++, cur, 0, false, 0, 0);
                if (cache) spans.add(oSpan);

                cur = size + 1;

                //System.out.println("Novo* "+(c)+"("+(oSpan.id)+"){"+oSpan+"}");
                return oSpan;
            }

            if (count == 0) {
                span = get();
            } else if (count == spans.size()) {
                count ++;
            }

            Span oSpan = span;

            do {
                span = get();
                if (oSpan.line && span == null) {
                    span = get();
                }
            } while (!oSpan.line);

            if (cache) spans.add(oSpan);

            //System.out.println("Novo "+(c)+"("+(oSpan.id)+"){"+oSpan+"}");
            return oSpan;
        }
    }
}