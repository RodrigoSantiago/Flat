package flat.widget.text;

import flat.events.KeyCode;
import flat.events.KeyEvent;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.graphics.context.Font;
import flat.graphics.text.Align;
import flat.graphics.text.FontStyle;
import flat.math.Vector2;
import flat.math.Vector3;
import flat.math.shapes.Rectangle;
import flat.math.shapes.Shape;
import flat.uxml.*;
import flat.widget.Widget;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

// TODO - Mult-texture font { +Unicodes }
public class TextArea extends Widget {

    private byte[] text;
    private String string;
    private int size, capacity;

    private int cursorPos, cursorStart, selStart, selEnd; //UTF-8 Positions
    private float cMove = -1;

    private Font font = Font.getDefault();
    private float textSize;
    private int textColor;
    private int selectionColor;
    private int cursorColor;

    private long timer = 0;

    private UXListener<PointerEvent> actPointerListener;

    private float scrollX, scrollY;
    private boolean invalidTextSize;
    private float textWidth, textHeight;

    private ByteBuffer buffer;
    private SpanManager spanManager;
    private ArrayList<Style> styles;

    public TextArea() {
        size = 0;
        capacity = 64;
        text = new byte[capacity];
        buffer = ByteBuffer.allocateDirect(capacity);
        styles = new ArrayList<>();
        styles.add(new Style(Font.findFont(FontStyle.CURSIVE), 24, 0xFF0000FF));
        styles.add(new Style(Font.findFont(FontStyle.CURSIVE), 32, 0x000000FF));
        spanManager = new SpanManager();
    }

    @Override
    public void applyAttributes(UXTheme theme, Controller controller, UXBuilder builder) {
        super.applyAttributes(theme, controller, builder);

        /*setText(theme.asString("text", getText()));

        Method handle = theme.linkListener("on-act-pointer", PointerEvent.class, controller);
        if (handle != null) {
            setActPointerListener(new PointerListener.AutoPointerListener(controller, handle));
        }*/
    }

    @Override
    public void applyStyle() {
        super.applyStyle();
        /*UXStyle style = getAttrs();
        if (style == null) return;

        StateInfo info = getStateInfo();

        if (info.get(StateInfo.FOCUSED) != 0) {
            invalidate(false);
        }

        setFont(style.asFont("font", info, getFont()));
        setTextSize(style.asSize("text-size", info, getTextSize()));
        setTextColor(style.asColor("text-color", info, getTextColor()));
        setSelectionColor(style.asColor("selection-color", info, getSelectionColor()));
        setCursorColor(style.asColor("cursor-color", info, getCursorColor()));*/
    }

    @Override
    public void onDraw(SmartContext context) {
        backgroundDraw(getBackgroundColor(), getBorderColor(), getRippleColor(), context);

        float x = getTextInX();
        float y = getTextInY();
        float w = getTextInWidth();
        float h = getTextInHeight();
        context.setTransform2D(getTransform());
        context.setTextVerticalAlign(Align.Vertical.TOP);
        context.setTextHorizontalAlign(Align.Horizontal.LEFT);

        Shape shape = backgroundClip(context);
        context.intersectClip(new Rectangle(x, y, w, h));

        float xpos = x - scrollX;
        float ypos = y - scrollY;
        for (int i = 0; i < spanManager.lines.size(); i++) {
            Line line = spanManager.lines.get(i);
            float lineHeight = line.height;

            if (ypos + lineHeight < 0) {
                ypos += lineHeight;
                continue;
            }
            if (ypos > h) {
                break;
            }

            Span span = line.sSpan;
            do {
                Style style = span.style;

                toBuffer(span.start, span.len);
                float tw = style.font.getWidth(buffer, 0, span.len, style.size, 1);
                float th = style.font.getHeight(style.size);

                // Background (HighLightColor)
                //context.setColor(0x000000FF);
                //context.drawRect(xpos, ypos + (lineHeight - th), tw, th, false);

                if (selStart != selEnd) {
                    context.setColor(selectionColor);
                    if (selStart <= span.start) {
                        if (selEnd > span.end) {
                            context.drawRect(xpos, ypos, span.n ? w - xpos : tw, lineHeight, true);
                        } else {
                            float sw = style.font.getWidth(buffer, 0, selEnd - span.start, style.size, 1);
                            context.drawRect(xpos, ypos, sw, lineHeight, true);
                        }
                    } else if (selStart < span.end) {
                        float ssw = style.font.getWidth(buffer, 0, selStart - span.start, style.size, 1);

                        if (selEnd > span.end) {
                            if (span.n) {
                                context.drawRect(xpos + ssw, ypos, w - xpos - ssw, lineHeight, true);
                            } else {
                                context.drawRect(xpos + ssw, ypos, tw - ssw, lineHeight, true);
                            }
                        } else {
                            float sew = style.font.getWidth(buffer, 0, selEnd - span.start, style.size, 1);
                            context.drawRect(xpos + ssw, ypos, sew - ssw, lineHeight, true);
                        }
                    }
                }

                context.setColor(style.color);
                context.setTextSize(style.size);
                context.setTextFont(style.font);
                context.drawText(xpos, ypos + (lineHeight - th), buffer, 0, span.len);


                if ((isFocused() || isPressed()) && cursorPos >= span.start &&
                        (cursorPos < span.end ||
                                (i == spanManager.lines.size() - 1 && span == line.eSpan && cursorPos <= span.end))) {

                    float off = style.font.getWidth(buffer, 0, cursorPos - span.start, style.size, 1);
                    float cursorX = (float) Math.floor(xpos + off);
                    float cursorW = Math.max(2, Math.round(lineHeight / 16f));
                    float cursorW2 = Math.abs(cursorX - (x + w)) < cursorW ? cursorW : 0;

                    context.setColor(cursorColor);
                    context.drawRect(cursorX - cursorW2, ypos + (lineHeight - th), cursorW, th, true);
                }

                xpos += tw;
                span = span.next;
            } while (span != null && span != line.eSpan.next);
            xpos = x - scrollX;
            ypos += lineHeight;
        }

        context.setTransform2D(null);
        context.setClip(shape);
    }

    @Override
    public void firePointer(PointerEvent pointerEvent) {
        super.firePointer(pointerEvent);

        if (!pointerEvent.isConsumed()) {
            if (pointerEvent.getType() == PointerEvent.PRESSED) {
                int pos = getPositionIndex(pointerEvent.getX(), pointerEvent.getY(), true);
                timer = System.currentTimeMillis();
                setCursorPos(pos);
                cursorStart = cursorPos;
                invalidate(true);
            }
            if (pointerEvent.getType() == PointerEvent.DRAGGED) {
                int pos = getPositionIndex(pointerEvent.getX(), pointerEvent.getY(), true);
                if ((System.currentTimeMillis() - timer) > 100) {
                    timer = System.currentTimeMillis();
                }

                setCursorPos(pos);
                if (cursorPos < cursorStart) {
                    selStart = cursorPos;
                    selEnd = cursorStart;
                } else {
                    selStart = cursorStart;
                    selEnd = cursorPos;
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
                    int prev = findPrev(cursorPos);

                    setCursorPos(prev);
                }
                if (keyEvent.getKeycode() == KeyCode.KEY_RIGHT) {
                    int next = findNext(cursorPos);

                    setCursorPos(next);
                }
                if (keyEvent.getKeycode() == KeyCode.KEY_UP) {
                    Line lineUp = spanManager.findUp(cursorPos);
                    if (lineUp != null) {
                        updateCursorMove();

                        int off = 0;
                        float xpos = 0;
                        Span span = lineUp.sSpan;
                        do {
                            if (xpos <= cMove && (span.width + xpos > cMove || span.n || span.next == null)) {
                                toBuffer(span.start, span.len);
                                off = span.style.font.getOffset(buffer, 0, span.len, span.style.size, 1, cMove - xpos, true);
                                if (off == span.len && span.n) {
                                    off --;
                                }
                                off += span.start;
                                break;
                            } else {
                                xpos += span.width;
                            }
                            span = span.next;
                        } while (span != null && span != lineUp.eSpan.next);

                        float pw = cMove;
                        setCursorPos(off);
                        cMove = pw;
                    }
                }
                if (keyEvent.getKeycode() == KeyCode.KEY_DOWN) {
                    Line lineDown = spanManager.findDown(cursorPos);
                    if (lineDown != null) {
                        updateCursorMove();

                        int off = 0;
                        float xpos = 0;
                        Span span = lineDown.sSpan;
                        do {
                            if (xpos <= cMove && (span.width + xpos > cMove || span.n || span.next == null)) {
                                toBuffer(span.start, span.len);
                                off = span.style.font.getOffset(buffer, 0, span.len, span.style.size, 1, cMove - xpos, true);
                                if (off == span.len && span.n) {
                                    off --;
                                }
                                off += span.start;
                                break;
                            } else {
                                xpos += span.width;
                            }
                            span = span.next;
                        } while (span != null && span != lineDown.eSpan.next);

                        float pw = cMove;
                        setCursorPos(off);
                        cMove = pw;
                    }
                }

                if (keyEvent.getKeycode() == KeyCode.KEY_BACKSPACE) {
                    if (selStart != selEnd) {
                        replace(selStart, selEnd - selStart, "");
                        setCursorPos(cursorPos);
                    } else {
                        int back = findPrev(selStart);
                        replace(back, selStart - back, "");
                        setCursorPos(cursorPos);

                        // Enhaced Scroll - Fixer
                        cMove = -1;
                        updateCursorMove();
                        if (cMove <= scrollX) {
                            scrollX = cMove - Math.max(1, getTextInWidth() * 0.25f);
                        }
                    }
                }
                if (keyEvent.getKeycode() == KeyCode.KEY_DELETE) {
                    if (selStart != selEnd) {
                        replace(selStart, selEnd - selStart, "");
                        setCursorPos(cursorPos);
                    } else {
                        int next = findNext(selStart);
                        replace(selStart, next - selStart, "");
                        setCursorPos(cursorPos);
                    }
                }
                if (keyEvent.getKeycode() == KeyCode.KEY_ENTER) {
                    replace(selStart, selEnd - selStart, "\n");
                    setCursorPos(cursorPos);
                }
                if (keyEvent.getKeycode() == KeyCode.KEY_HOME) {
                    Line line = spanManager.find(cursorPos);
                    setCursorPos(line.sSpan.start);
                }
                if (keyEvent.getKeycode() == KeyCode.KEY_END) {
                    Line line = spanManager.find(cursorPos);
                    setCursorPos(line.eSpan.n ? line.eSpan.end - 1 : line.eSpan.end);
                }
            }
            if (keyEvent.getType() == KeyEvent.TYPED) {
                if (keyEvent.getChar() != null) {
                    replace(selStart, selEnd - selStart, keyEvent.getChar());
                    setCursorPos(cursorPos);
                }
            }
        }
    }

    @Override
    public void onMeasure() {
        float mWidth = getPrefWidth();
        float mHeight = getPrefHeight();

        updatePrefSize();

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
        updateScroll();
    }

    float getTextInY() {
        return getInY();
    }

    float getTextInX() {
        return getInX();
    }

    float getTextInWidth() {
        return getInWidth();
    }

    float getTextInHeight() {
        return getInHeight();
    }

    void updatePrefSize() {
        if (invalidTextSize) {
            invalidTextSize = false;
            textWidth = 0;
            textHeight = 0;

            for (int i = 0; i < spanManager.lines.size(); i++) {
                Line line = spanManager.lines.get(i);
                textHeight += line.height;
                textWidth = Math.max(textWidth, line.width);
            }
        }
    }

    void updateScroll() {
        float px = scrollX;
        float py = scrollY;

        float w = getTextInWidth();
        float h = getTextInHeight();
        if (textWidth - scrollX < w) {
            scrollX = textWidth - w;
        }
        if (textHeight - scrollY < h) {
            scrollY = textHeight - h;
        }
        scrollX = Math.max(0, scrollX);
        scrollY = Math.max(0, scrollY);

        if (px != scrollX || py != scrollY) {
            invalidate(true);
        }
    }

    void updateCursorMove() {
        if (cMove == -1) {
            Line line = spanManager.find(cursorPos);
            cMove = 0;
            Span span = line.sSpan;
            do {
                if (span.start <= cursorPos && (span.end > cursorPos || span.n || span.next == null)) {
                    toBuffer(span.start, span.len);
                    cMove += span.style.font.getWidth(buffer, 0, cursorPos - span.start, span.style.size, 1);
                    break;
                } else {
                    cMove += span.width;
                }
                span = span.next;
            } while (span != null && span != line.eSpan.next);
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
        if (start < 0) start = 0;
        if (start > size) start = size;
        if (start > 0 && start < size) {
            start = findPrev(findNext(start));
        }
        length = Math.min(size - start, Math.max(0, length));
        if (start + length > 0 && start + length < size) {
            length = findPrev(findNext(start + length)) - start;
        }


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

        ensureCapacity (size + offset);
        System.arraycopy(this.text, start + length, this.text, start + length + offset, size - (start + length));
        System.arraycopy(bytes, 0, this.text, start, bytes.length);
        size += offset;

        Span newBegin = null;
        Span newEnd = null;
        int sStart = start;
        for (int cur = start; cur < start + bytes.length;) {
            int next = findNext(cur);
            if (text[cur] == '\n') {
                Span span = new Span(sStart, next - sStart, true);
                span.style = styles.get(0);
                if (newBegin == null) newBegin = span;
                if (newEnd != null) newEnd.next = span;
                newEnd = span;
                sStart = next;
            }

            if (next >= start + bytes.length) {
                Span span = new Span(sStart, (start + bytes.length) - sStart, false);
                span.style = text[cur] == 'a' ? styles.get(1) : styles.get(0);
                if (newBegin == null) newBegin = span;
                if (newEnd != null) newEnd.next = span;
                newEnd = span;
            }
            cur = next;
        }

        spanManager.replace(start, length, bytes.length, newBegin, newEnd);

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

        Vector3 reversePosition = getReversePosition(cursorPos);
        scrollTo(reversePosition.x, reversePosition.y, reversePosition.z);

        cMove = -1;
        string = null;
        invalidTextSize = true;
        invalidate(true);
    }

    public void setStyle(int start, int length, Style style) {
        if (start < 0) start = 0;
        if (start > size) start = size;
        if (start > 0 && start < size) {
            start = findPrev(findNext(start));
        }
        length = Math.min(size - start, Math.max(0, length));
        if (start + length > 0 && start + length < size) {
            length = findPrev(findNext(start + length)) - start;
        }

        Span newBegin = null;
        Span newEnd = null;
        int sStart = start;
        for (int cur = start; cur < start + length;) {
            int next = findNext(cur);
            if (text[cur] == '\n') {
                Span span = new Span(sStart, next - sStart, true, style);
                if (newBegin == null) newBegin = span;
                if (newEnd != null) newEnd.next = span;
                newEnd = span;
                sStart = next;
            }

            if (next >= start + length) {
                Span span = new Span(sStart, (start + length) - sStart, false, style);
                if (newBegin == null) newBegin = span;
                if (newEnd != null) newEnd.next = span;
                newEnd = span;
            }
            cur = next;
        }

        spanManager.replace(start, length, length, newBegin, newEnd);

        cMove = -1;
        invalidTextSize = true;
        invalidate(true);
    }

    public int getCursorPos() {
        return cursorPos;
    }

    public void scrollTo(float x, float y) {
        scrollTo(x, y, y);
    }

    public void scrollTo(float x, float y1, float y2) {
        if (scrollX + getTextInWidth() < x) {
            scrollX = x - getTextInWidth();
        } else if (scrollX > x) {
            scrollX = x;
        }
        if (scrollY + getTextInHeight() < y2) {
            scrollY = y2 - getTextInHeight();
        } else if (scrollY > y1) {
            scrollY = y1;
        }
    }

    public void setCursorPos(int cursorPos) {
        if (cursorPos < 0) cursorPos = 0;
        if (cursorPos > size) cursorPos = size;
        if (cursorPos > 0 && cursorPos < size) {
            cursorPos = findPrev(findNext(cursorPos));
        }

        if (this.cursorPos != cursorPos || selStart != cursorPos || selEnd != cursorPos) {
            this.cursorPos = cursorPos;

            Vector3 reversePosition = getReversePosition(cursorPos);
            scrollTo(reversePosition.x, reversePosition.y, reversePosition.z);

            selStart = cursorPos;
            selEnd = cursorPos;

            cMove = -1;
            invalidate(false);
        }
    }

    public int getPositionIndex(float x, float y) {
        return getPositionIndex(x, y, false);
    }

    int getPositionIndex(float px, float py, boolean sCursorLine) {
        Vector2 point = new Vector2(px, py);
        screenToLocal(point);
        px = point.x - getTextInX();
        py = point.y - getTextInY();

        float w = getTextInWidth();
        float h = getTextInHeight();

        if (sCursorLine) {
            if ((System.currentTimeMillis() - timer) <= 100) {
                if (px < 0) px = 0;
                if (px > w) px = w;
                if (py < 0) py = 0;
                if (py > h) py = h - 1;
            }
        }

        px += scrollX;
        py += scrollY;
        float ypos = 0;

        int off = size;

        boolean first = true;
        boolean last;
        loop : for (int j = 0; j < spanManager.lines.size(); j++) {
            Line line = spanManager.lines.get(j);

            last = (j == spanManager.lines.size() - 1);

            if ((first && py < ypos)
                    || (ypos <= py && ypos + line.height > py)
                    || (last && py > ypos)) {

                float xpos = 0;
                Span span = line.sSpan;
                do {
                    if ((xpos <= px || span == line.sSpan) && (xpos + span.width > px || span == line.eSpan)) {
                        toBuffer(span.start, span.len);
                        off = span.style.font.getOffset(buffer, 0, span.len, span.style.size, 1, px - xpos, true) + span.start;
                        if (span.n && off == span.end) off--;
                        break loop;
                    }
                    xpos += span.width;
                    span = span.next;
                } while (span != line.eSpan.next);
            }

            first = false;

            ypos += line.height;
        }
        return off;
    }

    Vector3 getReversePosition(int cursor) {
        float x = 0;
        float y = 0;
        float y2 = 0;
        loop : for (int i = 0, len = spanManager.lines.size(); i < len; i++) {
            Line line = spanManager.lines.get(i);
            if (line.sSpan.start <= cursor && (line.eSpan.end > cursor || line.eSpan.next == null)) {
                Span span = line.sSpan;
                do {
                    if (cursor >= span.start && (cursor < span.end || span.n || span.next == null)) {
                        toBuffer(span.start, span.len);
                        x += span.style.font.getWidth(buffer, 0, cursor - span.start, span.style.size, 1);
                        y2 += y + line.height;
                        break loop;
                    } else {
                        x += span.width;
                    }
                    span = span.next;
                } while (span != null && span != line.eSpan.next);
                break;
            }
            y += line.height;
        }
        return new Vector3(x, y, y2);
    }

    public String getText() {
        if (string == null) {
            string = new String(text, 0, size, StandardCharsets.UTF_8);
        }
        return string;
    }

    public void setText(String text) {
        replace(0, size, text);
        setCursorPos(0);
        invalidate(true);
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

    public int getCursorColor() {
        return cursorColor;
    }

    public void setCursorColor(int cursorColor) {
        if (this.cursorColor != cursorColor) {
            this.cursorColor = cursorColor;
            invalidate(false);
        }
    }

    public UXListener<PointerEvent> getActPointerListener() {
        return actPointerListener;
    }

    public void setActPointerListener(UXListener<PointerEvent> actPointerListener) {
        this.actPointerListener = actPointerListener;
    }

    public static class Style {
        public final Font font;
        public final float size;
        public final int color;

        public Style(Font font, float size, int color) {
            this.font = font;
            this.size = size;
            this.color = color;
        }
    }

    class Line {
        float width;
        float height;
        Span sSpan;
        Span eSpan;

        Line(Span span) {
            this.sSpan = span;
            this.eSpan = span;
            width = span.width;
            height = span.style.font.getHeight(span.style.size);

            Span pSpan = span;
            Span nSpan = span.next;
            while (nSpan != null && !pSpan.n) {
                if (pSpan.style == nSpan.style) {
                    pSpan.end = nSpan.end;
                    pSpan.len = pSpan.end - pSpan.start;
                    pSpan.next = nSpan.next;
                    pSpan.n = nSpan.n;
                    nSpan = pSpan;
                } else {
                    toBuffer(pSpan.start, pSpan.len);
                    pSpan.width = pSpan.style.font.getWidth(buffer, 0, pSpan.len, pSpan.style.size, 1);
                    width += pSpan.width;
                    height = Math.max(height, pSpan.style.font.getHeight(pSpan.style.size));
                }
                eSpan = nSpan;
                pSpan = nSpan;
                nSpan = nSpan.next;
            }

            toBuffer(pSpan.start, pSpan.len);
            width += pSpan.style.font.getWidth(buffer, 0, pSpan.len, pSpan.style.size, 1);
            height = Math.max(height, pSpan.style.font.getHeight(pSpan.style.size));
        }

        @Override
        public String toString() {
            return super.toString() + "[" + sSpan.start + "," + eSpan.end + (eSpan.n ? "-N":"") + "]" +
                    new String(text, sSpan.start, eSpan.end - sSpan.start - (eSpan.n ? 1 : 0), StandardCharsets.UTF_8);
        }
    }

    class Span {
        Style style;

        int start, end, len;
        float width;
        boolean n;
        Span next;

        Span(int start, int len) {
            this(start, len, false);
        }

        Span(int start, int len, boolean n) {
            this(start, len, n, styles.get(0));
        }

        Span(int start, int len, boolean n, Style style) {
            this.start = start;
            this.len = len;
            this.end = start + len;
            this.n = n;
            this.style = style;
        }

        @Override
        public String toString() {
            return super.toString() + "[" + start + "," + end + (n ? "-N":"") + "]" + new String(text, start, len - (n ? 1 : 0), StandardCharsets.UTF_8);
        }
    }

    class SpanManager {

        ArrayList<Line> lines = new ArrayList<>();

        SpanManager() {
            Span span = new Span(0, 0);
            Line line = new Line(span);
            lines.add(line);
        }

        public Line findUp(int index) {
            Line upLine = null;
            for (Line line : lines) {
                if (line.sSpan.start <= index && (line.eSpan.end > index || line.eSpan.next == null)) {
                    return upLine;
                }
                upLine = line;
            }
            return null;
        }

        public Line findDown(int index) {
            Line prevLine = null;
            for (Line line : lines) {
                if (prevLine != null) return line;
                if (line.sSpan.start <= index && (line.eSpan.end > index || line.eSpan.next == null)) {
                    prevLine = line;
                }
            }
            return null;
        }

        public Line find(int index) {
            for (Line line : lines) {
                if (line.sSpan.start <= index && (line.eSpan.end > index || line.eSpan.next == null)) {
                    return line;
                }
            }
            return null; //assert
        }

        public void replace(int start, int len, int newLen, Span newBegin, Span newEnd) {
            int cOffSet = newLen - len;

            int isLine = 0;
            int ieLine = 0;
            Line sLine = null;
            Line eLine = null;

            Span preStart = null;
            Span posEnd = null;

            Span sStart = null;
            Span sEnd = null;

            // Encontrar Span Inicial e Final
            loop : for (int i = 0; i < lines.size(); i++) {
                Line line = lines.get(i);
                Span span = line.sSpan;
                do {
                    if (sStart == null  &&
                            ((span.start <= start && span.end > start) || span.next == null)) {
                        sLine = line;
                        isLine = i;
                        sStart = span;
                    }
                    if (sStart != null &&
                            ((span.start <= start + len && span.end > start + len) || span.next == null)) {
                        eLine = line;
                        ieLine = i;
                        sEnd = span;
                        posEnd = span.next;
                        break loop;
                    }
                    if (sStart == null) preStart = span;
                    span = span.next;
                } while (span != line.eSpan.next);
            }

            // Cortar spans
            if (sStart.start < start) {
                if (newBegin != null && sStart.style == newBegin.style) {
                    newBegin.start = sStart.start;
                    newBegin.len = newBegin.end - newBegin.start;
                } else {
                    Span cStart = new Span(sStart.start, start - sStart.start, false, sStart.style);
                    cStart.next = newBegin;
                    if (newBegin == null) newEnd = cStart;
                    newBegin = cStart;
                }
            }
            if (sEnd.end > start + len) {
                if (newEnd != null && sEnd.style == newEnd.style) {
                    newEnd.end = sEnd.end + cOffSet;
                    newEnd.len = newEnd.end - newEnd.start;
                    newEnd.n = sEnd.n;
                } else {
                    Span cEnd = new Span(start + newLen, (sEnd.end + cOffSet) - (start + newLen), sEnd.n, sEnd.style); // +offset
                    if (newBegin == null) newBegin = cEnd;
                    if (newEnd != null) newEnd.next = cEnd;
                    newEnd = cEnd;
                }
            }

            // Realinhar spans
            Span offset = posEnd;
            while (offset != null) {
                offset.start += cOffSet;
                offset.end += cOffSet;
                offset = offset.next;
            }

            // Buscar Linhas
            Span lineSearch;
            if (newBegin == null) {
                if (preStart == null) {
                    if (posEnd == null) {
                        lineSearch = new Span(0, 0);
                    } else {
                        lineSearch = posEnd;
                    }
                } else {
                    // LINE EXCEPTION
                    if (preStart.n) {
                        newBegin = new Span(preStart.end, 0);
                        preStart.next = newBegin;
                        newBegin.next = posEnd;

                        lineSearch = newBegin;
                    } else {
                        preStart.next = posEnd;

                        lineSearch = (sStart == sLine.sSpan ? lines.get(isLine - 1).sSpan : sLine.sSpan);
                    }
                }
            } else {
                if (preStart != null) {
                    preStart.next = newBegin;

                    if (preStart.n) {
                        lineSearch = newBegin;
                    } else {
                        lineSearch = (sStart == sLine.sSpan ? newBegin : sLine.sSpan);
                    }
                } else {
                    lineSearch = newBegin;
                }

                if (posEnd != null) {
                    newEnd.next = posEnd;
                }
            }

            ArrayList<Line> newLines = new ArrayList<>();
            while ((posEnd == null && lineSearch != null) ||
                    (posEnd != null && (lineSearch != null && lineSearch.end <= posEnd.start))) {
                Line line = new Line(lineSearch);
                lineSearch = line.eSpan.next;
                newLines.add(line);
            }
            List<Line> lineList = lines.subList(isLine, ieLine + 1);
            lineList.clear();
            lineList.addAll(newLines);

            /*System.out.println("Spans : ");
            offset = lines.get(0).sSpan;
            while (offset != null) {
                System.out.println(offset);
                offset = offset.next;
            }*/

            //System.out.println("Lines : ");
            Span begin = lines.get(0).sSpan;
            for (Line line : lines) {
                if (begin != line.sSpan) {
                    System.out.println("INICIO INCORRETO : [" + line + "] [" + line.sSpan + "]");
                    break;
                }
                while (begin != line.eSpan) {
                    if (begin.next != null && begin.end != begin.next.start){
                        System.out.println("SPANS NAO SE ENCONTRAM " + begin + " || " + begin.next);
                    }
                    begin = begin.next;
                }
                begin = begin.next;
                //System.out.println(line);
            }
            if (begin != null) {
                System.out.println("FINAL INCORRETO [" + begin + "]");
            }
        }
    }
}
