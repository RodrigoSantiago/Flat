package flat.widget.text;

import flat.animations.Interpolation;
import flat.animations.StateInfo;
import flat.events.*;
import flat.graphics.SmartContext;
import flat.graphics.context.Font;
import flat.graphics.cursor.Cursor;
import flat.graphics.image.Drawable;
import flat.graphics.text.Align;
import flat.math.Vector2;
import flat.math.shapes.Rectangle;
import flat.math.shapes.Shape;
import flat.resources.Resource;
import flat.uxml.Controller;
import flat.uxml.UXStyle;
import flat.uxml.UXStyleAttrs;
import flat.widget.Application;
import flat.widget.Widget;
import flat.widget.effects.RippleEffect;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

public class TextField extends Widget {

    private byte[] text;
    private String string;
    private int size, capacity;

    private int cursorPos, cursorStart, selStart, selEnd; //UTF-8 Positions
    private int cursorLine, cursorStartLine, selStartLine, selEndLine;
    private float cursorMoveAdvance = -1;

    private Font font = Font.DEFAULT;
    private float textSize;
    private int textColor;
    private int selectionColor;
    private int cursorColor;
    private boolean borderDiscret;

    private boolean singleLine = true;
    private boolean wrapText;
    private int minLines = 1, maxLines = Integer.MAX_VALUE;

    private String labelText;
    private Font labelFont = Font.DEFAULT;
    private float labelTextSize;
    private int labelTextColor;
    private float highLabelTextSize;
    private int highLabelTextColor;
    private boolean highLabelFixed;

    private String placeholder;
    private int placeholderTextColor;

    private String helpText;
    private Font helpFont = Font.DEFAULT;
    private float helpTextSize;
    private int helpTextColor;

    private Drawable leadingIcon;
    private float leadingSpacing;

    private Drawable actIcon;
    private float actSpacing;

    private RippleEffect actRipple;
    private int actRippleColor;
    private boolean actRippleEnabled;

    private long timer = 0;
    private int actPress = -1;

    private ActionListener actionListener;

    private float scrollX, scrollY;
    private boolean invalidTextSize, invalidScroll;
    private float textWidth, textHeight;
    private float wrapWidth, wrapHeight;

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
        setPlaceholder(style.asString("placeholder", getPlaceholder()));

        Method handle = style.asListener("on-action", ActionEvent.class, controller);
        if (handle != null) {
            setActionListener(new ActionListener.AutoActionListener(controller, handle));
        }
    }

    @Override
    public void applyStyle() {
        super.applyStyle();
        UXStyle style = getStyle();
        if (style == null) return;

        StateInfo info = getStateInfo();

        if (info.get(StateInfo.FOCUSED) != 0) {
            invalidate(false);
        }

        setFont(style.asFont("font", info, getFont()));
        setTextSize(style.asSize("text-size", info, getTextSize()));
        setTextColor(style.asColor("text-color", info, getTextColor()));
        setSelectionColor(style.asColor("selection-color", info, getSelectionColor()));
        setCursorColor(style.asColor("cursor-color", info, getCursorColor()));

        setLabelFont(style.asFont("label-font", info, getLabelFont()));
        setLabelTextSize(style.asSize("label-text-size", info, getLabelTextSize()));
        setLabelTextColor(style.asColor("label-text-color", info, getLabelTextColor()));
        setHighLabelTextSize(style.asSize("high-label-text-size", info, getHighLabelTextSize()));
        setHighLabelTextColor(style.asColor("high-label-text-color", info, getHighLabelTextColor()));
        setHighLabelFixed(style.asBool("high-label-fixed", info, getHighLabelFixed()));

        setPlaceholderTextColor(style.asColor("placeholder-text-color", info, getPlaceholderTextColor()));
        setBorderDiscret(style.asBool("border-discret", info, isBorderDiscret()));

        setHelpFont(style.asFont("help-font", info, getHelpFont()));
        setHelpTextSize(style.asSize("help-text-size", info, getHelpTextSize()));
        setHelpTextColor(style.asColor("help-text-color", info, getHelpTextColor()));

        setActRippleEnabled(style.asBool("act-ripple", info, isActRippleEnabled()));
        setActRippleColor(style.asColor("act-ripple-color", info, getActRippleColor()));

        Resource res = getStyle().asResource("leading-icon", info);
        if (res != null) {
            Drawable drawable = res.getDrawable();
            if (drawable != null) {
                setLeadingIcon(drawable);
            }
        }
        res = getStyle().asResource("act-icon", info);
        if (res != null) {
            Drawable drawable = res.getDrawable();
            if (drawable != null) {
                setActIcon(drawable);
            }
        }
        setLeadingSpacing(style.asSize("leading-spacing", info, getLeadingSpacing()));
        setActSpacing(style.asSize("act-spacing", info, getActSpacing()));
    }

    @Override
    public void onDraw(SmartContext context) {
        backgroundDraw(getBackgroundColor(), isBorderDiscret() ? 0 : getBorderColor(), getRippleColor(), context);

        float x = getTextInX();
        float y = getTextInY();
        float w = getTextInWidth();
        float h = getTextInHeight();
        context.setTransform2D(getTransform());
        context.setTextVerticalAlign(Align.Vertical.TOP);
        context.setTextHorizontalAlign(Align.Horizontal.LEFT);

        if (isBorderDiscret()) {
            context.setColor(getBorderColor());
            context.drawRect(getOutX(), getOutY() + getOutHeight() - getBorderWidth(), getOutWidth(), getBorderWidth(), true);
        }

        StateInfo info = getStateInfo();
        float focusTime = info.get(StateInfo.FOCUSED);
        if (labelText != null) {
            float hLabel = labelFont.getHeight(highLabelTextSize);
            if (size == 0 && focusTime < 1 && !highLabelFixed) {
                context.setTextFont(labelFont);
                context.setTextSize(Interpolation.mix(labelTextSize, highLabelTextSize, focusTime));
                context.setColor(Interpolation.mixColor(labelTextColor, highLabelTextColor, focusTime));
                context.drawText(x, y - Interpolation.mix(0, hLabel, focusTime), labelText);
            } else {
                context.setTextFont(labelFont);
                context.setTextSize(highLabelTextSize);
                context.setColor(highLabelTextColor);
                context.drawText(x, y - hLabel, labelText);
            }
        }

        if (helpText != null) {
            context.setTextFont(helpFont);
            context.setTextSize(helpTextSize);
            context.setColor(helpTextColor);
            context.drawText(x, getOutY() + getOutHeight(), helpText);
        }

        if (leadingIcon != null) {
            leadingIcon.draw(context, getInX(), getInY(), 0);
            context.setTransform2D(getTransform());
        }

        if (actIcon != null) {
            actIcon.draw(context, getTextInX() + getTextInWidth() + getActSpacing(), getInY(), 0);
            context.setTransform2D(getTransform());
            if (isActRippleEnabled() && getActRipple().isVisible()) {
                getActRipple().drawRipple(context, null, actRippleColor);
            }
        }

        context.setTextFont(font);
        context.setTextSize(textSize);
        context.setColor(textColor);

        Shape shape = backgroundClip(context);
        context.intersectClip(new Rectangle(x, y, w, h));

        if (size == 0 && placeholder != null && (focusTime >= 1 || highLabelFixed || labelText == null)) {
            context.setColor(placeholderTextColor);
            context.drawText(x, y, placeholder);
        }

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
            context.drawText(xpos, ypos, buffer, 0, span.len);

            if ((isFocused() || isPressed()) && cursorLine == span.id) {
                float off = font.getWidth(buffer, 0, cursorPos - span.start, textSize, 1);

                float cursorX = (float) Math.floor(xpos + off);
                float cursorW = Math.max(2, Math.round(lineHeight / 16f));
                float cursorW2 = Math.abs(cursorX - (x + w)) < cursorW ? cursorW : 0;

                context.setColor(cursorColor);
                context.drawRect(cursorX - cursorW2, ypos, cursorW, lineHeight, true);
            }
        }

        context.setTransform2D(null);
        context.setClip(shape);
    }

    @Override
    public void invalidate(boolean layout) {
        super.invalidate(layout);

        if (layout) {
            invalidScroll = true;
        }
    }

    @Override
    public void firePointer(PointerEvent pointerEvent) {
        super.firePointer(pointerEvent);

        if (!pointerEvent.isConsumed()) {
            if (getActIcon() != null) {
                float x1 = getTextInX() + getTextInWidth() + getActSpacing();
                float x2 = x1 + getActIcon().getWidth();
                float y1 = getInY();
                float y2 = y1 + getActIcon().getHeight();
                Vector2 point = new Vector2(pointerEvent.getX(), pointerEvent.getY());
                screenToLocal(point);
                if (point.x >= x1 && point.x <= x2 && point.y >= y1 && point.y <= y2) {
                    if (actPress == -1 && pointerEvent.getType() == PointerEvent.PRESSED) {
                        actPress = pointerEvent.getPointerID();
                        if (isActRippleEnabled()) {
                            getActRipple().setSize(getActIcon().getWidth());
                            getActRipple().fire(x1 + getActIcon().getWidth() / 2, y1 + getActIcon().getHeight() / 2);
                        }
                        fireAction(new ActionEvent(this));
                        return;
                    }
                }
            }
            if (actPress != -1 && actPress == pointerEvent.getPointerID()) {
                if (pointerEvent.getType() == PointerEvent.RELEASED) {
                    if (isActRippleEnabled()) {
                        getActRipple().release();
                    }
                    fireAction(new ActionEvent(this));
                    actPress = -1;
                    return;
                }
            }

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

    public void fireAction(ActionEvent actionEvent) {
        if (actionListener != null) {
            actionListener.handle(actionEvent);
        }
    }

    @Override
    public void fireHover(HoverEvent hoverEvent) {
        super.fireHover(hoverEvent);
        if (getActIcon() != null) {
            float x1 = getTextInX() + getTextInWidth() + getActSpacing();
            float x2 = x1 + getActIcon().getWidth();
            float y1 = getInY();
            float y2 = y1 + getActIcon().getHeight();
            Vector2 point = new Vector2(hoverEvent.getX(), hoverEvent.getY());
            screenToLocal(point);
            if (point.x >= x1 && point.x <= x2 && point.y >= y1 && point.y <= y2) {
                if (actPress == -1) {
                    Application.setCursor(Cursor.HAND);
                }
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

                        // Enhaced Scroll - Fixer
                        updateScroll();
                        Span spanLine = spanManager.get(cursorLine);
                        if (spanLine != null) {
                            toBuffer(spanLine.start, spanLine.len);
                            float w = font.getWidth(buffer, 0, cursorPos - spanLine.start, textSize, 1);
                            if (w == scrollX) {
                                scrollX = Math.min(w, Math.max(0, scrollX - Math.max(1, getTextInWidth() * 0.25f)));
                            }
                        }
                        invalidScroll = true;
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
        if (getTextInWidth() != wrapWidth) {
            wrapWidth = getTextInWidth();
            invalidTextSize = true;
            invalidScroll = true;
            invalidate(true);
        }
        if (getInHeight() != wrapHeight) {
            wrapHeight = getInHeight();
            invalidScroll = true;
            invalidate(true);
        }
    }

    float getTextInY() {
        return getInY();
    }

    float getTextInX() {
        float add = 0;
        if (getLeadingIcon() != null) {
            add = getLeadingIcon().getWidth() + getLeadingSpacing();
        }
        return getInX() + add;
    }

    float getTextInWidth() {
        float add = 0;
        if (getLeadingIcon() != null) {
            add = getLeadingIcon().getWidth() + getLeadingSpacing();
        }
        if (getActIcon() != null) {
            add += getActIcon().getWidth() + getActSpacing();
        }
        return getInWidth() - add;
    }

    float getTextInHeight() {
        return getInHeight();
    }

    void updatePrefSize(float width) {
        if (invalidTextSize) {

            invalidTextSize = false;
            textWidth = 0;
            textHeight = 0;

            float w = (!singleLine && wrapText) ? wrapWidth : width == 0 ? MATCH_PARENT : width;
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
                float w = getTextInWidth();

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
                float h = getTextInHeight();
                float th = font.getHeight(textSize);
                if ((cursorLine + 1) * th - scrollY > h) {
                    scrollY = (cursorLine + 1) * th - h;
                } else if (cursorLine * th - scrollY < 0) {
                    scrollY = cursorLine * th;
                }

                spanManager.setPos((int) ((scrollY) / th));
                while (spanManager.hasNext()) {
                    Span span = spanManager.next();
                    if (span.id * th - scrollY > h) {
                        break;
                    }
                    if (!spanManager.hasNext()) {
                        scrollY = (span.id + 1) * th - h;
                        scrollY = Math.max(scrollY, 0);
                    }
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

        spanManager.setWidth(getTextInWidth());
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

        float lineHeight = font.getHeight(textSize);

        boolean first = true;
        boolean last;

        int off = size;
        Span span = null;
        for (spanManager.setPos(0); spanManager.hasNext();) {
            span = spanManager.next();
            ypos = span.id * lineHeight;

            last = !spanManager.hasNext();

            if ((first && py < ypos)
                || (ypos <= py && ypos + lineHeight > py)
                || (last && py > ypos)) {

                toBuffer(span.start, span.len);
                int index = font.getOffset(buffer, 0, span.len, textSize, 1, px, true);
                if (index == span.len) {
                    if (span.line) {
                        if (span.len > 0 && text[span.start + span.len - 1] == '\n') {
                            off = span.start + index - 1;
                        } else {
                            off = span.start + index;
                        }
                        break;
                    }
                } else {
                    off = span.start + index;
                    break;
                }
            }

            first = false;
        }
        if (sCursorLine) {
            cursorLine = span == null ? 0 : span.id;
        }
        return off;
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

    public int getCursorColor() {
        return cursorColor;
    }

    public void setCursorColor(int cursorColor) {
        if (this.cursorColor != cursorColor) {
            this.cursorColor = cursorColor;
            invalidate(false);
        }
    }

    public boolean isBorderDiscret() {
        return borderDiscret;
    }

    public void setBorderDiscret(boolean borderDiscret) {
        if (this.borderDiscret != borderDiscret) {
            this.borderDiscret = borderDiscret;
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

    public boolean getHighLabelFixed() {
        return highLabelFixed;
    }

    public void setHighLabelFixed(boolean highLabelFixed) {
        if (this.highLabelFixed != highLabelFixed) {
            this.highLabelFixed = highLabelFixed;
            invalidate(false);
        }
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        if (!Objects.equals(this.placeholder, placeholder)) {
            this.placeholder = placeholder;
            invalidate(false);
        }
    }

    public int getPlaceholderTextColor() {
        return placeholderTextColor;
    }

    public void setPlaceholderTextColor(int placeholderTextColor) {
        if (this.placeholderTextColor != placeholderTextColor) {
            this.placeholderTextColor = placeholderTextColor;
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

    private RippleEffect getActRipple() {
        if (actRipple == null) {
            actRipple = new RippleEffect(this);
        }
        return actRipple;
    }

    public boolean isActRippleEnabled() {
        return actRippleEnabled;
    }

    public void setActRippleEnabled(boolean actRippleEnabled) {
        if (this.actRippleEnabled != actRippleEnabled) {
            this.actRippleEnabled = actRippleEnabled;
            invalidate(false);
        }
    }

    public int getActRippleColor() {
        return actRippleColor;
    }

    public void setActRippleColor(int actRippleColor) {
        if (this.actRippleColor != actRippleColor) {
            this.actRippleColor = actRippleColor;
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

    public Drawable getActIcon() {
        return actIcon;
    }

    public void setActIcon(Drawable actIcon) {
        if (this.actIcon != actIcon) {
            this.actIcon = actIcon;
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

    public float getActSpacing() {
        return actSpacing;
    }

    public void setActSpacing(float actSpacing) {
        if (this.actSpacing != actSpacing) {
            this.actSpacing = actSpacing;
            invalidate(true);
        }
    }

    public ActionListener getActionListener() {
        return actionListener;
    }

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    class Span {
        int id;
        int start, end, len;
        float width;
        boolean line;
        boolean midline;

        Span(int id, int start, int len, boolean line, float width) {
            this.id = id;
            this.start = start;
            this.len = len;
            this.end = start + len;
            this.line = line;
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
                    oSpan = new Span(count++, 0, size, true, font.getWidth(buffer, 0, size, textSize, 1));
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
                        oSpan = new Span(count++, prev, len, true, font.getWidth(buffer, 0, len, textSize, 1));
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
                            oSpan = new Span(count++, prev, cOff, line, tw);
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
                Span oSpan = new Span(count++, cur, 0, false, 0);
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