package flat.widget.text;

import flat.animations.StateInfo;
import flat.events.KeyCode;
import flat.events.KeyEvent;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.graphics.context.Font;
import flat.graphics.image.Drawable;
import flat.graphics.text.Align;
import flat.math.Affine;
import flat.math.Vector2;
import flat.math.shapes.Shape;
import flat.resources.Resource;
import flat.uxml.Controller;
import flat.uxml.UXStyle;
import flat.uxml.UXStyleAttrs;
import flat.widget.Widget;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

public class TextField extends Widget {

    private byte[] text;
    private String string;
    private int size, capacity;

    int cursorPos, cursorStart, selStart, selEnd; //UTF-8 Positions
    int cursorLine, cursorStartLine, selStartLine, selEndLine;
    float cursorMoveAdvance = -1;

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

    // TODO - SCROLL LIMIT AFTER TEXT CHANGE
    private float scrollX, scrollY;
    private boolean invalidTextSize, invalidScroll;
    private float textWidth, textHeight;
    private float wrapWidth;

    // TODO - REMOVER ESSE BUFFER BOSTA
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

        setLabelText(style.asString("label-text", getLabelText()));
        setHelpText(style.asString("help-text", getHelpText()));
        setSingleLine(style.asBool("single-line", isSingleLine()));
        setWrapText(style.asBool("wrap-text", isWrapText()));

        setMinLines((int) style.asNumber("min-lines", getMinLines()));
        setMaxLines((int) style.asNumber("max-lines", getMaxLines()));

        setText(style.asString("text", getText()));
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
    public void invalidate(boolean layout) {
        super.invalidate(layout);

        if (layout) {
            invalidScroll = true;
        }
    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);

        Affine transform = context.getTransform2D();
        Shape shape = backgroundClip(context);

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

        updateScroll();

        Span span;
        spanManager.setWidth(w);
        for (spanManager.setPos(0); spanManager.hasNext();) {
            span = spanManager.next();
            float xpos = x - scrollX;
            float ypos = y + span.id * lineHeight - scrollY;
            if (ypos + lineHeight < y) continue;
            if (ypos > y + h) break;

            toBuffer(span.start, span.len);

            if (selStart != selEnd || selStartLine != selEndLine) {
                boolean ss = (selStartLine == span.id);
                boolean se = (selEndLine == span.id);
                context.setColor(selectionColor);
                if (ss && se) {
                    float offs = font.getWidth(buffer, 0, selStart - span.start, textSize, 1);
                    float offe = font.getWidth(buffer, 0, selEnd - span.start, textSize, 1);
                    context.drawRect(xpos + offs, ypos, offe - offs, lineHeight, true);
                } else if (ss) {
                    float offs = font.getWidth(buffer, 0, selStart - span.start, textSize, 1);
                    context.drawRect(xpos + offs, ypos, w + scrollX - offs, lineHeight, true);
                } else if (se) {
                    float offe = font.getWidth(buffer, 0, selEnd - span.start, textSize, 1);
                    context.drawRect(xpos, ypos, offe, lineHeight, true);
                } else if (selStartLine < span.id && selEndLine > span.id) {
                    context.drawRect(xpos, ypos, w + scrollX, lineHeight, true);
                }
            }

            context.setColor(textColor);
            context.drawText(xpos + span.x, ypos, buffer, 0, span.len);

            if ((isFocused() || isPressed()) && cursorLine == span.id) {
                float off = font.getWidth(buffer, 0, cursorPos - span.start, textSize, 1);
                context.setColor(textColor);
                context.drawRect((float) Math.floor(xpos + span.x + off), ypos,
                        Math.min(1, Math.round(lineHeight / 16f)), lineHeight, true);
            }
        }

        context.setTransform2D(transform);
        context.setClip(shape);
    }

    long timer = 0;
    @Override
    public void firePointer(PointerEvent pointerEvent) {
        super.firePointer(pointerEvent);

        if (!pointerEvent.isConsumed()) {
            if (pointerEvent.getType() == PointerEvent.PRESSED) {
                int pos = getPositionIndex(pointerEvent.getX(), pointerEvent.getY(), true);
                timer = System.currentTimeMillis();
                setCursorPos(pos, cursorLine);
                cursorStart = cursorPos;
                cursorStartLine = cursorLine;
                invalidate(true);
            }
            if (pointerEvent.getType() == PointerEvent.DRAGGED) {
                int pos = getPositionIndex(pointerEvent.getX(), pointerEvent.getY(), true);
                if ((System.currentTimeMillis() - timer) > 100) {
                    timer = System.currentTimeMillis();
                }

                setCursorPos(pos, cursorLine);
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
                    int line = cursorLine;
                    int prev = findPrev(cursorPos);

                    if (!singleLine && wrapText) {
                        Span span = spanManager.get(cursorLine);
                        Span prevSpan = spanManager.get(cursorLine - 1);

                        if (span != null && prev < span.start) {
                            if (prevSpan != null && (prevSpan.len == 0 || text[prevSpan.end - 1] != '\n')) {
                                setCursorPos(cursorPos, prevSpan.id);
                            } else {
                                setCursorPos(prev);
                            }
                        } else {
                            setCursorPos(prev, line);
                        }
                    } else {
                        setCursorPos(prev);
                    }
                }
                if (keyEvent.getKeycode() == KeyCode.KEY_RIGHT) {
                    int line = cursorLine;
                    int next = findNext(cursorPos);

                    if (!singleLine && wrapText) {
                        Span span = spanManager.get(cursorLine);
                        Span nextSpan = spanManager.get(cursorLine + 1);

                        if (span != null && next >= span.end) {
                            if (nextSpan != null && next > span.end && (span.len == 0 || text[span.end - 1] != '\n')) {
                                setCursorPos(cursorPos, nextSpan.id);
                            } else {
                                if (next == span.end && (span.len == 0 || text[span.end - 1] != '\n')) {
                                    setCursorPos(next, line);
                                } else {
                                    setCursorPos(next);
                                }
                            }
                        } else {
                            setCursorPos(next, line);
                        }
                    } else {
                        setCursorPos(next);
                    }
                }
                if (keyEvent.getKeycode() == KeyCode.KEY_UP) {
                    if (cursorLine > 0) {
                        Span spanLine = spanManager.get(cursorLine);
                        Span span = spanManager.get(cursorLine - 1);
                        if (spanLine != null && span != null) {
                            toBuffer(spanLine.start, spanLine.len);
                            float w = cursorMoveAdvance;
                            if (w == -1) {
                                w = font.getWidth(buffer, 0, cursorPos - spanLine.start, textSize, 1);
                            }
                            toBuffer(span.start, span.len);
                            int off = font.getOffset(buffer, 0, span.len, textSize, 1, w, true);
                            setCursorPos(span.start + off, span.id);
                            cursorMoveAdvance = w;
                        }
                    }
                }
                if (keyEvent.getKeycode() == KeyCode.KEY_DOWN) {
                    Span spanLine = spanManager.get(cursorLine);
                    Span span = spanManager.get(cursorLine + 1);
                    if (spanLine != null && span != null) {
                        toBuffer(spanLine.start, spanLine.len);
                        float w = cursorMoveAdvance;
                        if (w == -1) {
                            w = font.getWidth(buffer, 0, cursorPos - spanLine.start, textSize, 1);
                        }
                        toBuffer(span.start, span.len);
                        int off = font.getOffset(buffer, 0, span.len, textSize, 1, w, true);
                        setCursorPos(span.start + off, span.id);
                        cursorMoveAdvance = w;
                    }
                }

                if (keyEvent.getKeycode() == KeyCode.KEY_BACKSPACE) {
                    if (selStart != selEnd) {
                        replace(selStart, selEnd - selStart, "");
                        setCursorPos(cursorPos, cursorLine);
                    } else {
                        int back = findPrev(selStart);
                        replace(back, selStart - back, "");
                        setCursorPos(cursorPos, cursorLine);

                        updateScroll();
                        Span spanLine = spanManager.get(cursorLine);
                        if (spanLine != null) {
                            toBuffer(spanLine.start, spanLine.len);
                            float w = font.getWidth(buffer, 0, cursorPos - spanLine.start, textSize, 1);
                            if (w == scrollX) {
                                scrollX = Math.min(w, Math.max(0, scrollX - (textSize * 4)));
                            }
                        }
                    }
                }
                if (keyEvent.getKeycode() == KeyCode.KEY_DELETE) {
                    if (selStart != selEnd) {
                        replace(selStart, selEnd - selStart, "");
                        setCursorPos(cursorPos, cursorLine);
                    } else {
                        int next = findNext(selStart);
                        replace(selStart, next - selStart, "");
                        setCursorPos(cursorPos, cursorLine);
                    }
                }
                if (keyEvent.getKeycode() == KeyCode.KEY_ENTER && !singleLine) {
                    replace(selStart, selEnd - selStart, "\n");
                    setCursorPos(cursorPos, cursorLine);
                }
                if (keyEvent.getKeycode() == KeyCode.KEY_HOME) {
                    Span spanLine = spanManager.get(cursorLine);
                    setCursorPos(spanLine.start, spanLine.id);
                }
                if (keyEvent.getKeycode() == KeyCode.KEY_END) {
                    Span spanLine = spanManager.get(cursorLine);
                    if (spanLine != null) {
                        setCursorPos(spanLine.end, spanLine.id);
                    }
                }
            }
            if (keyEvent.getType() == KeyEvent.TYPED) {
                if (keyEvent.getChar() != null) {
                    replace(selStart, selEnd - selStart, keyEvent.getChar());
                    setCursorPos(cursorPos, cursorLine);
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
            textHeight = Math.min(Math.max((span == null ? 0 : span.id) + 1, minLines), maxLines) * lineHeight;
        }
    }

    void updateScroll() {
        if (invalidScroll) {
            float bx = scrollX, by = scrollY;
            Span line = spanManager.get(cursorLine);
            if (line == null || (!singleLine && wrapText)) {
                scrollX = 0;
            } else {
                float w = getInWidth();

                toBuffer(line.start, line.len);
                float tw = font.getWidth(buffer, 0, cursorPos - line.start, textSize, 1);

                if (tw - scrollX > w) {
                    scrollX = tw - w;
                } else if (tw - scrollX < 0) {
                    scrollX = tw;
                }
            }
            if (line == null || singleLine) {
                scrollY = 0;
            } else {
                float h = getInHeight();
                float th = font.getHeight(textSize);
                if ((cursorLine + 1) * th - scrollY > h) {
                    scrollY = (cursorLine + 1) * th - h;
                } else if (cursorLine * th - scrollY < 0) {
                    scrollY = cursorLine * th;
                }
            }
            if (scrollX != bx || scrollY != by) {
                invalidate(false);
            }
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

        // Offset
        if (cursorPos >= start && cursorPos < start + length) {
            cursorPos = start + bytes.length;
        } else if (cursorPos >= start + length) {
            cursorPos += offset;
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

        // Lines
        Span cLine = spanManager.find(cursorPos);
        cursorLine = cLine == null ? 0 : cLine.id;

        if (selStart == cursorPos) {
            selStartLine = cursorLine;
        } else {
            cLine = spanManager.find(selStart);
            selStartLine = cLine == null ? cursorLine : cLine.id;
        }
        if (selEnd == cursorPos) {
            selEndLine = cursorLine;
        } else {
            cLine = spanManager.find(selEnd);
            selEndLine = cLine == null ? cursorLine : cLine.id;
        }

        string = null;
        invalidTextSize = true;
        invalidScroll = true;
        invalidate(true);
    }

    public int getCursorPos() {
        return cursorPos;
    }

    public int getLinePos() {
        return cursorLine;
    }

    public void setCursorPos(int cursorPos) {
        if (cursorPos < 0) cursorPos = 0;
        if (cursorPos > size) cursorPos = size;
        if (cursorPos > 0 && cursorPos < size) {
            cursorPos = findPrev(findNext(cursorPos));
        }

        if (this.cursorPos != cursorPos || selStart != cursorPos || selEnd != cursorPos) {
            this.cursorPos = cursorPos;
            this.cursorMoveAdvance = -1;

            Span line = spanManager.find(cursorPos);
            cursorLine = line == null ? 0 : line.id;

            selStart = cursorPos;
            selStartLine = cursorLine;
            selEnd = cursorPos;
            selEndLine = cursorLine;

            invalidScroll = true;
            invalidate(false);
        }
    }

    public void setCursorPos(int cursorPos, int preferedLine) {
        if (cursorPos < 0) cursorPos = 0;
        if (cursorPos > size) cursorPos = size;
        if (cursorPos > 0 && cursorPos < size) {
            cursorPos = findPrev(findNext(cursorPos));
        }

        if (this.cursorPos != cursorPos || preferedLine != cursorLine
                || selStart != cursorPos || selEnd != cursorPos
                || selStartLine != preferedLine || selEndLine != preferedLine) {

            this.cursorPos = cursorPos;
            this.cursorMoveAdvance = -1;

            Span line = spanManager.get(preferedLine);
            if (line != null && cursorPos >= line.start && cursorPos <= line.end) {
                cursorLine = line.id;
            } else {
                line = spanManager.find(cursorPos);
                cursorLine = line == null ? 0 : line.id;
            }

            selStart = cursorPos;
            selStartLine = cursorLine;
            selEnd = cursorPos;
            selEndLine = cursorLine;

            invalidScroll = true;
            invalidate(false);
        }
    }

    public int getPositionIndex(float x, float y) {
        return getPositionIndex(x, y, false);
    }

    int getPositionIndex(float px, float py, boolean sCursorLine) {
        updateScroll();

        Vector2 point = new Vector2(px, py);
        screenToLocal(point);
        px = point.x - getInX() + scrollX;
        py = point.y;

        boolean s = (System.currentTimeMillis() - timer) > 100;
        boolean fConsume = false;

        float lineHeight = font.getHeight(textSize);
        float h = getInHeight();
        float x = getInX();
        float y = getInY();

        int i = size;
        Span span = null;
        for (spanManager.setPos(0); spanManager.hasNext();) {
            span = spanManager.next();
            float ypos = y + span.id * lineHeight - scrollY;

            if (!s && sCursorLine && Math.round(ypos) < Math.round(y)) continue;
            boolean first = !fConsume;
            fConsume = true;

            boolean last;
            if (!s && sCursorLine && ypos >= y + h - lineHeight) {
                last = true;
            } else {
                last = !spanManager.hasNext();
            }

            if ((first && py < ypos)
                || (ypos <= py && ypos + lineHeight > py)
                || (last && py > ypos)) {

                toBuffer(span.start, span.len);
                int index = font.getOffset(buffer, 0, span.len, textSize, 1, px, true);
                if (index == span.len) {
                    if (span.line) {
                        if (span.len > 0 && text[span.start + span.len - 1] == '\n') {
                            i = span.start + index - 1;
                        } else {
                            i = span.start + index;
                        }
                        break;
                    }
                } else {
                    i = span.start + index;
                    break;
                }
            }

            if (last) break;
        }
        if (sCursorLine) {
            cursorLine = span == null ? 0 : span.id;
        }
        return i;
    }

    public String getText() {
        if (string == null) {
            string = new String(text, 0, size, StandardCharsets.UTF_8);
        }
        return string;
    }

    public void setText(String text) {
        replace(0, size, text);
        setCursorPos(0, 0);
        invalidate(true);
    }

    public boolean isSingleLine() {
        return singleLine;
    }

    public void setSingleLine(boolean singleLine) {
        if (this.singleLine != singleLine) {
            int cPos = cursorPos;
            if (singleLine) {
                replace(0, size, getText().replaceAll("\n",""));
            }
            setCursorPos(cPos);

            this.singleLine = singleLine;
            spanManager.invalidate(0);
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

            // TODO - ALL TO CPP SIDE !
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
                        int len = next - prev;
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
                    if (b == ' ' || b == '\n' || next >= size) {
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

        public Span get(int line) {
            if (line < 0) return null;

            if (line >= spans.size()) {
                while (line >= spans.size() && hasNext()) {
                    next();
                }
            }
            return line >= spans.size() ? null : spans.get(line);
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

        // TODO - Performance Improvement
        public void inlineInvalidate(int line, int start, int end, int newLen) {
            // Only for SingleLine or Non-WrapTexts
            // Only for simple between text changes [no \n, no hit the line's edges]
            // Atualizar valores e linhas da linha
            // Update only aftewards loaded values [>= line]
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
                return spans.get(count++);
            }

            if (cur == size) {
                Span oSpan = new Span(count++, cur, 0, false, 0, 0);
                if (cache) spans.add(oSpan);

                cur = size + 1;

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

            return oSpan;
        }
    }
}