package flat.widget.text;

import flat.animations.StateInfo;
import flat.events.KeyCode;
import flat.events.KeyEvent;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.graphics.context.Font;
import flat.graphics.text.Align;
import flat.math.Vector2;
import flat.uxml.Controller;
import flat.uxml.UXStyleAttrs;
import flat.widget.Widget;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

// Todo - Melhorar performance e uso de meória
public class TextField extends Widget {

    private byte[] text;
    private String string;
    private int size, capacity;

    private int cursorPos, cursorStart, selStart, selEnd;    //UTF-8 Positions
    private float offsetLine;

    private Font font;
    private float textSize;
    private int textColor;

    private boolean invalidTextSize;
    private float textWidth, textHeight;

    private ByteBuffer buffer;

    public TextField() {
        text = new byte[capacity = 64];
        size = 0;
        buffer = ByteBuffer.allocateDirect(64);
    }

    @Override
    public void applyAttributes(UXStyleAttrs style, Controller controller) {
        super.applyAttributes(style, controller);

        setText(style.asString("text", getText()));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();
        super.applyStyle();
        if (getStyle() == null) return;

        StateInfo info = getStateInfo();

        setFont(getStyle().asFont("font", info, getFont()));
        setTextSize(getStyle().asSize("text-size", info, getTextSize()));
        setTextColor(getStyle().asColor("text-color", info, getTextColor()));
    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);
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
        int prev = 0;
        int line = 0;
        for (int i = 0; i < size;) {
            byte b = this.text[i];
            int next = findNext(i);
            i = next - 1;
            if (b == '\n' || next >= size) {
                int include = (b == '\n') ? 0 : 1;
                if (line >= offsetLine) {
                    toBuffer(prev, i - prev + include);

                    if (selStart != selEnd) {
                        boolean ss = selStart >= prev && selStart <= i + include;
                        boolean se = selEnd >= prev && selEnd <= i + include;

                        if (ss && se) {
                            float off1 = font.getWidth(buffer, 0, selStart - prev, textSize, 1);
                            float off2 = font.getWidth(buffer, 0, selEnd - prev, textSize, 1);
                            context.setColor(0xFF0000FF);
                            context.drawRect(x + off1, y, off2 - off1, lineHeight, true);
                        } else if (ss) {
                            float off = font.getWidth(buffer, 0, selStart - prev, textSize, 1);
                            context.setColor(0xFF0000FF);
                            context.drawRect(x + off, y, w - off, lineHeight, true);
                        } else if (se) {
                            float off = font.getWidth(buffer, 0, selEnd - prev, textSize, 1);
                            context.setColor(0xFF0000FF);
                            context.drawRect(x, y, off, lineHeight, true);
                        } else if (selStart < prev && selEnd > i + include) {
                            context.setColor(0xFF0000FF);
                            context.drawRect(x, y, w, lineHeight, true);
                        }
                    }

                    if (isFocused() && cursorPos >= prev && cursorPos <= i + include) {
                        float off = font.getWidth(buffer, 0, cursorPos - prev, textSize, 1);
                        context.setColor(textColor);
                        context.drawRect((float) Math.floor(x + off), y, 2, lineHeight, true);
                    }

                    context.setColor(textColor);
                    context.drawText(x, y, buffer, 0, i - prev + include);
                    y += lineHeight;
                }
                prev = next;
                line ++;
                if ((line - offsetLine) * lineHeight > h) {
                    break;
                }
            }
            i = next;
        }
    }

    @Override
    public void firePointer(PointerEvent pointerEvent) {
        super.firePointer(pointerEvent);

        if (!pointerEvent.isConsumed()) {
            if (pointerEvent.getType() == PointerEvent.PRESSED) {
                cursorPos = getPositionIndex(pointerEvent.getX(), pointerEvent.getY());
                cursorStart = cursorPos;
                selStart = cursorPos;
                selEnd = cursorPos;
                invalidate(true);
            }
            if (pointerEvent.getType() == PointerEvent.DRAGGED) {
                int p = getPositionIndex(pointerEvent.getX(), pointerEvent.getY());
                selStart = Math.min(p, cursorStart);
                selEnd = Math.max(p, cursorStart);
                cursorPos = p;
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
                    setCursorPos(findBack(cursorPos));
                }
                if (keyEvent.getKeycode() == KeyCode.KEY_RIGHT) {
                    setCursorPos(findNext(cursorPos));
                }

                if (keyEvent.getKeycode() == KeyCode.KEY_BACKSPACE) {
                    if (selStart != selEnd) {
                        replace(selStart, selEnd - selStart, "");
                        selStart = selEnd = cursorPos;
                    } else {
                        int back = findBack(selStart);
                        replace(back, selStart - back, "");
                        selStart = selEnd = cursorPos;
                    }
                }
                if (keyEvent.getKeycode() == KeyCode.KEY_DELETE) {
                    if (selStart != selEnd) {
                        replace(selStart, selEnd - selStart, "");
                        selStart = selEnd = cursorPos;
                    } else {
                        int next = findNext(selStart);
                        replace(selStart, next - selStart, "");
                        selStart = selEnd = cursorPos;
                    }
                }
                if (keyEvent.getKeycode() == KeyCode.KEY_ENTER) {
                    replace(selStart, selEnd - selStart, "\n");
                    selStart = selEnd = cursorPos;
                }
            }
            if (keyEvent.getType() == KeyEvent.TYPED) {
                if (keyEvent.getChar() != null) {
                    replace(selStart, selEnd - selStart, keyEvent.getChar());
                    selStart = selEnd = cursorPos;
                }
            }
        }
    }

    @Override
    public void onMeasure() {
        float mWidth = getPrefWidth();
        float mHeight = getPrefHeight();
        if (mWidth == WRAP_CONTENT) {
            updatePrefSize();
            mWidth = textWidth;
        }
        if (mHeight == WRAP_CONTENT) {
            updatePrefSize();
            mHeight = textHeight;
        }
        mWidth += getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        mHeight += getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();
        setMeasure(mWidth, mHeight);
    }

    void updatePrefSize() {
        if (invalidTextSize) {
            float lineHeight = font.getHeight(textSize);

            invalidTextSize = false;
            textWidth = 0;
            textHeight = lineHeight;
            int prev = 0;
            for (int i = 0; i < size;) {
                byte b = this.text[i];
                int next = findNext(i);
                i = next - 1;
                if (b == '\n' || next >= size) {
                    int include = (b == '\n') ? 0 : 1;
                    toBuffer(prev, i - prev + include);
                    float w = font.getWidth(buffer, 0, i - prev + include, textSize, 1);
                    if (w > textWidth) {
                        textWidth = w;
                    }
                    prev = next;
                    if (b == '\n') textHeight += lineHeight;
                }
                i = next;
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

    int findBack(int pos) {
        do {
            if (pos <= 0) return 0;
            pos--;
        } while ((text[pos] & 0xC0) == 0x80);
        return pos;
    }

    public void replace(int start, int length, String str) {
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

        ensureCapacity (size + offset);

        System.arraycopy(this.text, start + length, this.text, start + length + offset, size - (start + length));
        System.arraycopy(bytes, 0, this.text, start, bytes.length);
        size += offset;
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

        if (this.cursorPos != cursorPos) {
            this.cursorPos = cursorPos;
            selStart = cursorPos;
            selEnd = cursorPos;

            invalidate(false);
        }
    }

    public int getPositionIndex(float x, float y) {
        Vector2 point = new Vector2(x, y);
        screenToLocal(point);
        x = point.x - getPaddingLeft() - getMarginLeft();
        y = Math.max(0, point.y - getPaddingTop() - getMarginTop());

        float height = font.getHeight(textSize);

        int prev = 0;
        int line = 0;
        for (int i = 0; i < size; i++) {
            byte b = this.text[i];
            if ((b | 0b01111111) == 0b01111111) {             //0xxxxxxx
                if (b == '\n' || i == size - 1) {
                    int include = (b == '\n') ? 0 : 1;
                    toBuffer(prev, i - prev + include);
                    if ((line * height <= y && (line + 1) * height >= y) || i == size - 1) {
                        toBuffer(prev, i - prev);
                        return prev + font.getOffset(buffer, 0, i - prev + include, textSize, 1, x);
                    }
                    prev = i + 1;
                    line ++;
                }
            } else if ((b | 0b11011111) == 0b11011111) {      //110xxxxx
                i += 1;
            } else if ((b | 0b11101111) == 0b11101111) {     //1110xxxx
                i += 2;
            } else if ((b | 0b11110111) == 0b11110111) {     //11110xxx
                i += 3;
            }
        }
        return size; // assert
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
            invalidate(true);
        }
    }
}