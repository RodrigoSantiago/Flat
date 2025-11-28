package flat.widget.text.area;

import flat.graphics.Graphics;
import flat.graphics.symbols.Font;
import flat.widget.enums.HorizontalAlign;
import flat.widget.text.content.Caret;
import flat.widget.text.content.CaretControl;
import flat.widget.text.content.TextContentController;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class TextBox implements TextContentController {

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

    private boolean vectorRender;
    private boolean vectorRenderFill = true;

    private boolean hidden;
    private String hiddenChars;
    private HorizontalAlign align;
    
    public void setAlign(HorizontalAlign align) {
        this.align = align;
    }
    
    public HorizontalAlign getAlign() {
        return align;
    }
    
    public String getHiddenChars() {
        if (hiddenChars == null || hiddenChars.length() != getTotalCharacters()) {
            hiddenChars = "*".repeat(getTotalCharacters());
        }
        return hiddenChars;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
        recreateLines(false);
    }

    public void setFont(Font font) {
        this.font = font;
        recreateLines(false);
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
        recreateLines(false);
    }

    public void setVectorRender(boolean vectorRender) {
        this.vectorRender = vectorRender;
    }

    public boolean isVectorRender() {
        return vectorRender;
    }

    public void setVectorRenderFill(boolean vectorRenderFill) {
        this.vectorRenderFill = vectorRenderFill;
    }

    public boolean isVectorRenderFill() {
        return vectorRenderFill;
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
        byte[] bytes = new byte[caretEnd.getOffset() - caretStart.getOffset()];
        System.arraycopy(textBytes, caretStart.getOffset(), bytes, 0, bytes.length);
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
            totalCharacters = CaretControl.countChars(newTextBytes, 0, len);
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
        int start = caretStart.getOffset();
        int length = caretEnd.getOffset() - start;
        int end = start + length;

        if (length == 0 && replaceBytes.length == 0) {
            return false;
        }

        int newCharacters = CaretControl.countChars(replaceBytes, 0, replaceBytes.length);
        int oldCharacters = CaretControl.countChars(textBytes, start, end);

        if (maxCharacters > 0 && totalCharacters - oldCharacters + newCharacters > maxCharacters) {
            int l = findLength(replaceBytes, 0, replaceBytes.length, maxCharacters - (totalCharacters - oldCharacters));
            if (l == 0) {
                return false;
            } else {
                replaceBytes = Arrays.copyOf(replaceBytes, l);
                newCharacters = CaretControl.countChars(replaceBytes, 0, replaceBytes.length);
            }
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
        if (!hidden) {
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
        if (byteSize == 0 || font == null || hidden) {
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
        if (byteSize == 0 || font == null || hidden) {
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
            if (hidden) {
                return font.getWidth("*", textSize, 1) * getTotalCharacters();
            } else {
                return font.getWidth(buffer, 0, byteSize, textSize, 1);
            }

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

    public void drawText(Graphics graphics, float x, float y, float width, float height, HorizontalAlign align) {
        drawText(graphics, x, y, width, height, align, 0, lineCount);
    }

    public void drawText(Graphics graphics, float x, float y, float width, float height, HorizontalAlign align, int startLine, int endLine) {
        if (byteSize == 0 || font == null) {
            return;
        }

        float localWidth = getTextWidth();

        if (lineCount == 1) {
            if (hidden) {
                drawTextSlice(graphics, x, y, width, height, getHiddenChars());
            } else {
                drawTextSlice(graphics, x, y, width, height, buffer, 0, byteSize);
            }
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
                drawTextSlice(graphics, xpos, ypos, wd, hg, buffer, line.start, line.length);
            }
        }
    }

    private void drawTextSlice(Graphics graphics, float x, float y, float maxWidth, float maxHeight, String string) {
        if (vectorRender) {
            graphics.drawTextVector(x, y, maxWidth, maxHeight, string, vectorRenderFill);
        } else {
            graphics.drawTextSlice(x, y, maxWidth, maxHeight, string);
        }
    }

    private void drawTextSlice(Graphics graphics, float x, float y, float maxWidth, float maxHeight, Buffer text, int offset, int length) {
        if (vectorRender) {
            byte[] subArray = new byte[length];
            System.arraycopy(textBytes, offset, subArray, 0, length);
            graphics.drawTextVector(x, y, maxWidth, maxHeight, new String(subArray, StandardCharsets.UTF_8), vectorRenderFill);
        } else {
            graphics.drawTextSlice(x, y, maxWidth, maxHeight, buffer, offset, length);
        }
    }

    private String getLocalString(int offset, int length) {
        byte[] subArray = new byte[length];
        System.arraycopy(textBytes, offset, subArray, 0, length);
        return new String(subArray, StandardCharsets.UTF_8);
    }

    public void getCaret(Caret caretPos) {
        caretPos.setLine(0);
        updateCaret(caretPos, caretPos.getOffset());
    }

    public void getCaret(float px, float py, float x, float y, HorizontalAlign align, Caret caretPos) {
        if (byteSize == 0 || font == null) {
            caretPos.setLineChar(0);
            caretPos.setLine(0);
            caretPos.setOffset(0);
            caretPos.setWidth(0);
            return;
        }

        float localWidth = getTextWidth();

        if (lineCount == 1) {
            Font.CaretData caret;
            if (hidden) {
                caret = font.getCaretOffset(getHiddenChars(), textSize, 1, px - x, true);
            } else {
                caret = font.getCaretOffset(buffer, 0, byteSize, textSize, 1, px - x, true);
            }
            caretPos.setLineChar(caret.getIndex());
            caretPos.setLine(0);
            caretPos.setOffset(caret.getIndex());
            caretPos.setWidth(caret.getWidth());
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
        caretPos.setLineChar(caret.getIndex());
        caretPos.setLine(lineIndex);
        caretPos.setOffset(line.start + caret.getIndex());
        caretPos.setWidth(caret.getWidth());
    }

    public float getCaretHorizontalOffset(Caret caret, HorizontalAlign align) {
        if (byteSize == 0 || font == null) {
            return 0;
        }

        float localWidth = getTextWidth();

        float w = lineCount == 1 ? localWidth : lines.get(caret.getLine()).width;

        float xpos = 0;
        if (align == HorizontalAlign.RIGHT) {
            xpos = Math.max(0, localWidth - w);
        } else if (align == HorizontalAlign.CENTER) {
            xpos = Math.max(0, localWidth - w) * 0.5f;
        }

        return caret.getWidth() + xpos;
    }

    private void updateCaret(Caret caret, int offset) {
        if (byteSize == 0 || font == null) {
            caret.setLineChar(0);
            caret.setLine(0);
            caret.setOffset(0);
            caret.setWidth(0);
            return;
        }

        float localWidth = getTextWidth();

        if (lineCount == 1) {
            if (hidden) {
                float width = font.getWidth("*", textSize, 1);
                caret.setLineChar(offset);
                caret.setLine(0);
                caret.setOffset(offset);
                caret.setWidth(width * offset);
            } else {
                Font.CaretData newCaret = font.getCaretOffset(buffer, 0, offset, textSize, 1, 9999, true);
                caret.setLineChar(newCaret.getIndex());
                caret.setLine(0);
                caret.setOffset(newCaret.getIndex());
                caret.setWidth(newCaret.getWidth());
            }
            return;
        }

        int startLine = Math.max(0, caret.getLine() + Math.min(0, offset - caret.getOffset()));
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
        caret.setLineChar(newCaret.getIndex());
        caret.setLine(lineIndex);
        caret.setOffset(line.start + newCaret.getIndex());
        caret.setWidth(newCaret.getWidth());
    }
    
    @Override
    public void moveCaretBegin(Caret caret) {
        if (caret.getOffset() == 0) return;

        int offset = 0;
        updateCaret(caret, offset);
    }
    
    @Override
    public void moveCaretEnd(Caret caret) {
        if (caret.getOffset() == byteSize) return;

        int offset = byteSize;
        updateCaret(caret, offset);
    }
    
    @Override
    public void moveCaretWordForward(Caret caret) {
        int len = lineCount == 1 ? byteSize : lines.get(caret.getLine()).start + lines.get(caret.getLine()).length;
        int offset = caret.getOffset();
        int prevIndex = offset;
        while (offset < len) {
            offset = CaretControl.getNextCharIndex(offset, textBytes, byteSize);
            String cp = new String(textBytes, prevIndex, offset - prevIndex);
            if (cp.isEmpty() || (!Character.isLetterOrDigit(cp.codePointAt(0)) && !"_#".contains(cp))) {
                break;
            }
            prevIndex = offset;
        }
        updateCaret(caret, prevIndex);
    }
    
    @Override
    public void moveCaretWordBackwards(Caret caret) {
        var line = lineCount == 1 ? 0 : lines.get(caret.getLine()).start;
        int offset = caret.getOffset();
        int prevIndex = offset;
        while (offset > line) {
            offset = CaretControl.getPrevCharIndex(offset, textBytes, byteSize);
            String cp = new String(textBytes, offset, prevIndex - offset);
            if (cp.isEmpty() || (!Character.isLetterOrDigit(cp.codePointAt(0)) && !"_#".contains(cp))) {
                break;
            }
            prevIndex = offset;
        }
        updateCaret(caret, prevIndex);
    }
    
    @Override
    public void moveCaretBackwards(Caret caret) {
        if (caret.getOffset() == 0) return;

        int offset = CaretControl.getPrevCharIndex(caret.getOffset(), textBytes, byteSize);
        updateCaret(caret, offset);
    }
    
    @Override
    public void moveCaretForward(Caret caret) {
        if (caret.getOffset() == byteSize) return;

        int offset = CaretControl.getNextCharIndex(caret.getOffset(), textBytes, byteSize);
        updateCaret(caret, offset);
    }
    
    @Override
    public void moveCaretVertical(Caret caret, int lines) {
        float px = getCaretHorizontalOffset(caret, align);
        float py = (caret.getLine() + 0.5f + lines) * (font == null ? textSize : font.getHeight(textSize));
        getCaret(px, py, 0, 0, align, caret);
    }

    @Override
    public void moveCaretLineBegin(Caret caret) {
        if (caret.getOffset() == 0) return;

        int offset = lineCount == 1 ? 0 : lines.get(caret.getLine()).start;
        updateCaret(caret, offset);
    }

    public void moveCaret(Caret caret, int by) {
        int offset = Math.min(byteSize, Math.max(0, caret.getOffset() + by));
        updateCaret(caret, offset);
    }
    
    @Override
    public void moveCaretLineEnd(Caret caret) {
        if (caret.getOffset() == byteSize) return;

        int offset;
        if (lineCount == 1) {
            offset = byteSize;
        } else {
            var line = lines.get(caret.getLine());
            offset = line.start + line.length;
        }
        updateCaret(caret, offset);
    }

    public boolean isCaretLastOfLine(Caret caret) {
        if (lineCount <= 1) return caret.getOffset() >= byteSize;
        if (caret.getLine() >= lineCount) return true;
        return caret.getLineChar() >= lines.get(caret.getLine()).length;
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

    public int getCharCount(String str) {
        byte[] newTextBytes = str.getBytes(StandardCharsets.UTF_8);
        int len = newTextBytes.length;
        return CaretControl.countChars(newTextBytes, 0, len);
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

}
