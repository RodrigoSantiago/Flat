package flat.widget.text.data;

import flat.graphics.Graphics;
import flat.graphics.context.Font;
import flat.widget.enums.HorizontalAlign;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class TextRender {

    private Font font;
    private float textSize;
    private byte[] textBytes = new byte[16];
    private ByteBuffer buffer;
    private ArrayList<Line> lines;
    private int byteSize;
    private int lineCount = 1;
    private int totalCharacters;
    private int maxCharacters;

    private boolean lineWrapped;
    private float width = -1;
    private float layoutWidth;
    private float naturalWidth;
    private int naturalLines;

    public void setFont(Font font) {
        this.font = font;
        recreateLines(false);
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
        recreateLines(false);
    }

    public void setMaxCharacters(int maxCharacters) {
        this.maxCharacters = maxCharacters;
    }

    public int getTotalCharacters() {
        return totalCharacters;
    }

    public int getTotalBytes() {
        return byteSize;
    }

    public int getTotalLines() {
        return lineCount;
    }

    private void ensureCapacity(int requiredCapacity) {
        if (requiredCapacity > textBytes.length) {
            int newCapacity = Integer.highestOneBit(requiredCapacity);
            if (newCapacity < requiredCapacity) {
                newCapacity <<= 1;
            }
            textBytes = Arrays.copyOf(textBytes, newCapacity);
        }
    }

    public String getText() {
        return new String(textBytes, 0, byteSize, StandardCharsets.UTF_8);
    }

    public String getText(Caret caretStart, Caret caretEnd) {
        byte[] bytes = new byte[caretEnd.offset - caretStart.offset];
        System.arraycopy(textBytes, caretStart.offset, bytes, 0, bytes.length);
        return new String(bytes);
    }

    public boolean setText(String text) {
        boolean complete = true;
        if (text == null || text.isEmpty()) {
            byteSize = 0;
            totalCharacters = 0;
        } else {
            byte[] newTextBytes = text.getBytes(StandardCharsets.UTF_8);
            int len = newTextBytes.length;
            totalCharacters = countChars(newTextBytes, 0, len);
            if (maxCharacters > 0 && totalCharacters > maxCharacters) {
                len = findLength(newTextBytes, 0, len, maxCharacters);
                totalCharacters = maxCharacters;
                complete = false;
            }

            ensureCapacity(len);
            System.arraycopy(newTextBytes, 0, textBytes, 0, len);
            byteSize = len;
        }

        recreateLines(true);
        return complete;
    }

    public boolean trim(int length) {
        if (totalCharacters > length) {
            byteSize = findLength(textBytes, 0, byteSize, length);
            totalCharacters = length;
            recreateLines(true);
            return true;
        }
        return false;
    }

    public boolean editText(Caret caretStart, Caret caretEnd, String replace, Caret newCaret) {
        byte[] replaceBytes = replace.getBytes(StandardCharsets.UTF_8);
        int start = caretStart.offset;
        int length = caretEnd.offset - start;
        int end = start + length;

        if (length == 0 && replaceBytes.length == 0) {
            return false;
        }

        int newCharacters = countChars(replaceBytes, 0, replaceBytes.length);
        int oldCharacters = countChars(textBytes, start, end);

        if (maxCharacters > 0 && totalCharacters - oldCharacters + newCharacters > maxCharacters) {
            return false;
        }

        int newSize = byteSize - length + replaceBytes.length;
        ensureCapacity(newSize);

        if (replaceBytes.length != length) {
            int remainingLength = byteSize - end;

            if (remainingLength > 0) {
                if (replaceBytes.length < length) {
                    for (int i = 0; i < remainingLength; i++) {
                        textBytes[start + replaceBytes.length + i] = textBytes[end + i];
                    }
                } else {
                    for (int i = remainingLength - 1; i >= 0; i--) {
                        textBytes[start + replaceBytes.length + i] = textBytes[end + i];
                    }
                }
            }
        }

        System.arraycopy(replaceBytes, 0, textBytes, start, replaceBytes.length);
        byteSize = newSize;
        totalCharacters = totalCharacters - oldCharacters + newCharacters;
        recreateLines(true);

        int offset = start + replaceBytes.length;
        updateCaret(newCaret, offset);

        return true;
    }

    private void recreateLines(boolean updateBuffer) {
        lineCount = 1;
        naturalWidth = 0;
        naturalLines = 1;
        lineWrapped = false;
        layoutWidth = 0;
        width = -1;

        if (byteSize == 0) {
            return;
        }

        if (updateBuffer) {
            if (buffer == null || buffer.capacity() < textBytes.length) {
                buffer = ByteBuffer.allocateDirect(textBytes.length);
            }

            buffer.position(0);
            buffer.put(textBytes, 0, byteSize);
        }

        int prev = 0;
        for (int i = 0; i < byteSize; i++) {
            if (textBytes[i] == '\n') {
                if (lines == null) {
                    lines = new ArrayList<>();
                    lines.add(new Line(prev, i - prev));

                } else if (lines.size() < lineCount) {
                    lines.add(new Line(prev, i - prev));

                } else {
                    lines.get(lineCount - 1).reset(prev, i - prev);
                }
                lineCount++;
                prev = i + 1;
            }
        }

        if (lines != null) {
            if (lines.size() < lineCount) {
                lines.add(new Line(prev, byteSize - prev));

            } else {
                lines.get(lineCount - 1).reset(prev, byteSize - prev);
            }
        }

        naturalWidth = getTextWidth();
        naturalLines = lineCount;
    }

    public boolean isLineWrapped() {
        return lineWrapped;
    }

    public boolean isBreakLines(float maxWidth) {
        if (byteSize == 0 || font == null) {
            return false;
        }

        maxWidth = Math.max(textSize, maxWidth);

        if (width != -1 && maxWidth == layoutWidth) {
            return false;
        }

        if (naturalWidth <= maxWidth + 0.01f) {
            return lineWrapped;
        }

        if (lineCount == 1) {
            return true;
        }

        return font.getLineWrap(buffer, 0, byteSize, textSize, 1, maxWidth) != lineCount;
    }

    public void breakLines(float maxWidth) {
        if (byteSize == 0 || font == null) {
            return;
        }

        if (width != -1 && maxWidth == layoutWidth) {
            return;
        }

        if (lineWrapped) {
            recreateLines(false);
        }

        maxWidth = Math.max(textSize, maxWidth);

        if (naturalWidth <= maxWidth + 0.01f) {
            return;
        }

        if (lineCount == 1) {
            if (lines == null) {
                lines = new ArrayList<>();
            }
            if (lines.isEmpty()) {
                Line line = new Line(0, byteSize);
                line.width = naturalWidth;
                lines.add(line);
            } else {
                Line line = lines.get(0);
                line.reset(0, byteSize);
                line.width = naturalWidth;
            }
        }

        layoutWidth = maxWidth;
        width = -1;

        for (int i = 0; i < lineCount; i++) {
            var line = lines.get(i);
            if (line.width > maxWidth + 0.01f) {
                var caret = font.getCaretOffsetSpace(buffer, line.start, line.length, textSize, 1, maxWidth);
                if (caret.index == 0 && line.length > 1) {
                    caret = new Font.CaretData(1, font.getWidth(buffer, line.start, 1, textSize, 1));
                }
                if (caret.index > 0 && caret.index < line.length) {
                    Line split;
                    if (lines.size() > lineCount) {
                        split = lines.remove(lines.size() - 1);
                    } else {
                        split = new Line(0, 0);
                    }
                    split.reset(line.start + caret.index, line.length - caret.index);
                    split.width = line.width - caret.width;
                    lines.add(i + 1, split);
                    lineCount++;

                    line.length = caret.index;
                    line.width = caret.width;
                    lineWrapped = true;
                }
            }
        }
    }

    private float calculateTextWidth() {
        if (byteSize == 0 || font == null) {
            return 0;

        } else if (lineCount == 1) {
            return font.getWidth(buffer, 0, byteSize, textSize, 1);

        } else {
            float mWidth = 0;
            for (int i = 0; i < lineCount; i++) {
                var line = lines.get(i);
                if (line.length == 0) {
                    line.width = 0;
                } else {
                    line.width = font.getWidth(buffer, line.start, line.length, textSize, 1);
                }
                mWidth = Math.max(mWidth, line.width);
            }
            return mWidth;
        }
    }

    public float getNaturalWidth() {
        return naturalWidth;
    }

    public float getTextWidth() {
        if (width == -1) {
            width = calculateTextWidth();
        }
        return width;
    }

    public float getTextHeight() {
        float height;
        if (font == null) {
            height = textSize;
        } else {
            height = font.getHeight(textSize);
        }
        return height * lineCount;
    }

    public void drawText(Graphics context, float x, float y, float width, float height, HorizontalAlign align) {
        drawText(context, x, y, width, height, align, 0, lineCount);
    }

    public void drawText(Graphics context, float x, float y, float width, float height, HorizontalAlign align, int startLine, int endLine) {
        if (byteSize == 0 || font == null) {
            return;
        }

        float localWidth = getTextWidth();

        if (lineCount == 1) {
            context.drawTextSlice(x, y, width, height, buffer, 0, byteSize);
            return;
        }

        float lineHeight = font.getHeight(textSize);
        for (int i = startLine; i < lineCount && i < endLine; i++) {
            var line = lines.get(i);
            if (line.length == 0) {
                continue;
            }

            float xpos = x;
            float ypos = y + (i * lineHeight);
            float wd = width;
            float hg = height - (i * lineHeight);
            if (align == HorizontalAlign.RIGHT) {
                float off = Math.max(0, localWidth - line.width);
                xpos = x + off;
                wd -= off;
            } else if (align == HorizontalAlign.CENTER) {
                float off = Math.max(0, localWidth - line.width) * 0.5f;
                xpos = x + off;
                wd -= off;
            }
            if (wd > 0 && hg > 0) {
                context.drawTextSlice(xpos, ypos, wd, hg, buffer, line.start, line.length);
            }
        }
    }

    public void getCaret(Caret caretPos) {
        caretPos.line = 0;
        updateCaret(caretPos, caretPos.offset);
    }

    public void getCaret(float px, float py, float x, float y, HorizontalAlign align, Caret caretPos) {
        if (byteSize == 0 || font == null) {
            caretPos.lineChar = 0;
            caretPos.line = 0;
            caretPos.offset = 0;
            caretPos.width = 0;
            return;
        }

        float localWidth = getTextWidth();

        if (lineCount == 1) {
            var caret = font.getCaretOffset(buffer, 0, byteSize, textSize, 1, px - x, true);
            caretPos.lineChar = caret.getIndex();
            caretPos.line = 0;
            caretPos.offset = caret.getIndex();
            caretPos.width = caret.getWidth();
            return;
        }

        float lineHeight = font.getHeight(textSize);
        int lineIndex = Math.min(lineCount - 1, Math.max(0, (int) Math.floor((py - y) / lineHeight)));
        var line = lines.get(lineIndex);

        float xpos = x;
        if (align == HorizontalAlign.RIGHT) {
            float off = Math.max(0, localWidth - line.width);
            xpos = x + off;
        } else if (align == HorizontalAlign.CENTER) {
            float off = Math.max(0, localWidth - line.width) * 0.5f;
            xpos = x + off;
        }

        var caret = font.getCaretOffset(buffer, line.start, line.length, textSize, 1, px - xpos, true);
        caretPos.lineChar = caret.getIndex();
        caretPos.line = lineIndex;
        caretPos.offset = line.start + caret.getIndex();
        caretPos.width = caret.getWidth();
    }

    public float getCaretHorizontalOffset(Caret caret, HorizontalAlign align) {
        if (byteSize == 0 || font == null) {
            return 0;
        }

        float localWidth = getTextWidth();

        float w = lineCount == 1 ? localWidth : lines.get(caret.line).width;

        float xpos = 0;
        if (align == HorizontalAlign.RIGHT) {
            xpos = Math.max(0, localWidth - w);
        } else if (align == HorizontalAlign.CENTER) {
            xpos = Math.max(0, localWidth - w) * 0.5f;
        }

        return caret.width + xpos;
    }

    private void updateCaret(Caret caret, int offset) {
        if (byteSize == 0 || font == null) {
            caret.lineChar = 0;
            caret.line = 0;
            caret.offset = 0;
            caret.width = 0;
            return;
        }

        float localWidth = getTextWidth();

        if (lineCount == 1) {
            var newCaret = font.getCaretOffset(buffer, 0, offset, textSize, 1, 9999, true);
            caret.lineChar = newCaret.getIndex();
            caret.line = 0;
            caret.offset = newCaret.getIndex();
            caret.width = newCaret.getWidth();
            return;
        }

        int startLine = Math.max(0, caret.line + Math.min(0, offset - caret.offset));
        int lineIndex = lineCount - 1;
        for (int i = startLine; i < lineCount; i++) {
            var line = lines.get(i);
            if (line.start + line.length >= offset) {
                lineIndex = i;
                break;
            }
        }
        var line = lines.get(lineIndex);
        var newCaret = font.getCaretOffset(buffer, line.start, offset - line.start, textSize, 1, 9999, true);
        caret.lineChar = newCaret.getIndex();
        caret.line = lineIndex;
        caret.offset = line.start + newCaret.getIndex();
        caret.width = newCaret.getWidth();
    }

    public void moveCaretBegin(Caret caret) {
        if (caret.offset == 0) return;

        int offset = 0;
        updateCaret(caret, offset);
    }

    public void moveCaretEnd(Caret caret) {
        if (caret.offset == byteSize) return;

        int offset = byteSize;
        updateCaret(caret, offset);
    }

    public void moveCaretBackwards(Caret caret) {
        if (caret.offset == 0) return;

        int offset = getPrevCharIndex(caret.offset);
        updateCaret(caret, offset);
    }

    public void moveCaretFoward(Caret caret) {
        if (caret.offset == byteSize) return;

        int offset = getNextCharIndex(caret.offset);
        updateCaret(caret, offset);
    }

    public void moveCaretVertical(Caret caret, HorizontalAlign align, int lines) {
        float px = getCaretHorizontalOffset(caret, align);
        float py = (caret.line + 0.5f + lines) * (font == null ? textSize : font.getHeight(textSize));
        getCaret(px, py, 0, 0, align, caret);
    }

    public void moveCaretBackwardsLine(Caret caret) {
        if (caret.offset == 0) return;

        int offset = lineCount == 1 ? 0 : lines.get(caret.line).start;
        updateCaret(caret, offset);
    }

    public void moveCaret(Caret caret, int by) {
        int offset = Math.min(byteSize, Math.max(0, caret.offset + by));
        updateCaret(caret, offset);
    }

    public void moveCaretFowardsLine(Caret caret) {
        if (caret.offset == byteSize) return;

        int offset;
        if (lineCount == 1) {
            offset = byteSize;
        } else {
            var line = lines.get(caret.line);
            offset = line.start + line.length;
        }
        updateCaret(caret, offset);
    }

    public boolean isCaretLastOfLine(Caret caret) {
        if (lineCount <= 1) return caret.offset >= byteSize;
        if (caret.line >= lineCount) return true;
        return caret.lineChar >= lines.get(caret.line).length;
    }

    private int findLength(byte[] arr, int off, int end, int max) {
        int count = 0;
        int nextIndex = off;
        while (nextIndex < end) {
            if (count == max) {
                break;
            }
            count++;
            int byteValue = arr[nextIndex] & 0xFF;

            if (byteValue <= 0x7F) {
                nextIndex++;
            } else if ((byteValue & 0xC0) == 0xC0) {
                int byteCount = getUtf8ByteCount(byteValue);
                nextIndex += byteCount;
            }
        }
        return Math.min(nextIndex, end);
    }

    private int countChars(byte[] arr, int off, int end) {
        temp[0] = 0;
        temp[1] = off;
        int count = 0;
        while (temp[1] < end) {
            count++;
            readNextUnicode(end, arr, temp);
        }
        return count;
    }

    private int getNextCharIndex(int currentIndex) {
        if (currentIndex < 0 || currentIndex >= byteSize) {
            throw new IndexOutOfBoundsException("Invalid currentIndex");
        }
        temp[0] = 0;
        temp[1] = currentIndex;
        readNextUnicode(byteSize, textBytes, temp);
        return temp[1];
    }

    private int getPrevCharIndex(int currentIndex) {
        if (currentIndex <= 0 || currentIndex > byteSize) {
            throw new IndexOutOfBoundsException("Invalid currentIndex");
        }

        int pos = currentIndex;
        for (int i = 0; i < 7; i++) {
            pos = readPrevChar(pos);
        }

        int err = 10;
        temp[0] = 0;
        temp[1] = pos;
        while (temp[1] < currentIndex && err-- > 0) {
            pos = temp[1];
            readNextUnicode(byteSize, textBytes, temp);
        }
        return pos;
    }

    private int getUtf8ByteCount(int byteValue) {
        if ((byteValue & 0xE0) == 0xC0) {
            return 2;
        } else if ((byteValue & 0xF0) == 0xE0) {
            return 3;
        } else if ((byteValue & 0xF8) == 0xF0) {
            return 4;
        }
        return 1;
    }

    int[] temp = new int[2];

    private int readPrevChar(int currentIndex) {
        int prevIndex = currentIndex - 1;

        while (prevIndex > 0) {
            int byteValue = textBytes[prevIndex] & 0xFF;

            if ((byteValue & 0xC0) != 0x80) {
                return prevIndex;
            }

            prevIndex--;
        }

        return 0;
    }

    private void readNextUnicode(int length, byte[] array, int[] outPut) {
        // Usual Char
        readNextChar(length, array, outPut);
        int pc = outPut[0];
        int pi = outPut[1];

        if (!((pc >= 0x1F000 && pc <= 0x1FAFF) || (pc >= 0x200D && pc <= 0x3300))) {
            return;
        }

        // Emoji

        boolean waitNext = false;
        int pos = 1;
        while (readNextChar(length, array, outPut)) {
            int chr = outPut[0];
            if (chr == 0x200D) {
                waitNext = true;
            } else if (waitNext || chr == 0x1F3FB || chr == 0x1F3FC || chr == 0x1F3FD || chr == 0x1F3FE || chr == 0x1F3FF) {
                waitNext = false;
                pos++;
                if (pos == 6) {
                    break;
                }
            } else if (pos == 1 && (pc >= 0x1f1e6 && pc <= 0x1f1ff) && (chr >= 0x1f1e6 && chr <= 0x1f1ff)) {
                break;
            } else if (chr != 0xFE0E && chr != 0xFE0F) {
                outPut[0] = pc;
                outPut[1] = pi;
                break;
            }
            pc = outPut[0];
            pi = outPut[1];
        }
    }

    private boolean readNextChar(int length, byte[] array, int[] outPut) {
        int position = outPut[1];
        int nextPosition = position;
        if (position < 0 || position >= length) return false;

        int firstByte = array[position] & 0xFF;
        int codePoint;

        if ((firstByte & 0x80) == 0) {
            codePoint = firstByte;
            nextPosition += 1;
        } else if ((firstByte & 0xE0) == 0xC0 && position + 1 < length) {
            int secondByte = array[position + 1] & 0xFF;
            codePoint = ((firstByte & 0x1F) << 6) | (secondByte & 0x3F);
            nextPosition += 2;
        } else if ((firstByte & 0xF0) == 0xE0 && position + 2 < length) {
            int secondByte = array[position + 1] & 0xFF;
            int thirdByte = array[position + 2] & 0xFF;
            codePoint = ((firstByte & 0x0F) << 12) | ((secondByte & 0x3F) << 6) | (thirdByte & 0x3F);
            nextPosition += 3;
        } else if ((firstByte & 0xF8) == 0xF0 && position + 3 < length) {
            int secondByte = array[position + 1] & 0xFF;
            int thirdByte = array[position + 2] & 0xFF;
            int fourthByte = array[position + 3] & 0xFF;
            codePoint = ((firstByte & 0x07) << 18) | ((secondByte & 0x3F) << 12) |
                    ((thirdByte & 0x3F) << 6) | (fourthByte & 0x3F);
            nextPosition += 4;
        } else {
            codePoint = 0xFFFD;
            nextPosition += 1;
        }
        outPut[0] = codePoint;
        outPut[1] = nextPosition;
        return nextPosition < length;
    }

}
