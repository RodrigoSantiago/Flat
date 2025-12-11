package flat.widget.text.styled;

import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.math.Mathf;
import flat.widget.text.content.Caret;
import flat.widget.text.content.CaretControl;
import flat.widget.text.content.TextContentController;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class TextStyledContent implements TextContentController {
    
    private float fontHeight = 16f;
    private float fontWidth = 16f;
    
    private TextStyleBundle bundle;
    private ArrayList<TextStyledLine> cache = new ArrayList<>();
    private TreeList<TextStyledLine> lines = new TreeList<>();
    
    ByteBuffer temp;
    byte[] tBytes = new byte[32];
    
    public TextStyledContent() {
        lines.add(createLine(16));
        temp = ByteBuffer.allocateDirect(32);
    }
    
    public float getFontWidth() {
        return fontWidth;
    }
    
    public void setFontWidth(float fontWidth) {
        this.fontWidth = fontWidth;
    }
    
    public float getFontHeight() {
        return fontHeight;
    }
    
    public void setFontHeight(float fontHeight) {
        this.fontHeight = fontHeight;
    }
    
    public int getLineCount() {
        return lines.size();
    }
    
    public void createLinesFromInput(InputStream is) {
        ArrayList<TextStyledLine> localLines = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String strLine;
            while ((strLine = reader.readLine()) != null) {
                var bytes = strLine.getBytes(StandardCharsets.UTF_8);
                var line = createLine(bytes.length);
                line.putChar(bytes, 0, bytes.length);
                localLines.add(line);
            }
        } catch (Exception ignored) {
        }
        lines.clear();
        if (localLines.isEmpty()) {
            lines.add(createLine(16));
        } else {
            lines.addAll(localLines);
        }
    }
    
    public void createLinesFromString(String str) {
        lines.clear();
        lines.add(createLine(16));
        editText(str, 0, 0, 0, 0, null);
    }
    
    public void getCaret(float px, float py, Caret caret) {
        int lineId = Mathf.floorInt(py / fontHeight);
        lineId = Math.max(0, Math.min(lines.size() - 1, lineId));
        
        var line = lines.get(lineId);
        int targetChars = Mathf.round(px / fontWidth);
        int offset = 0;
        int chars = 0;
        while (chars < targetChars && offset < line.getSize()) {
            offset = CaretControl.getNextCharIndex(offset, line.getTextBytes(), line.getSize());
            chars++;
        }
        caret.setLine(lineId);
        caret.setChars(chars);
        caret.setOffset(offset);
        caret.setLineOffset(offset);
    }
    
    public void editText(String text, int line1, int start, int line2, int end, Caret caret) {
        TextStyledLine firstLine = lines.get(line1);
        TextStyledLine lastLine;
        if (line1 != line2) {
            var removed = lines.removeFromRange(line1 + 1, line2 + 1);
            lastLine = copyOfLine(removed.get(removed.size() - 1), end);
            for (TextStyledLine line : removed) {
                removeLine(line);
            }
        } else {
            lastLine = copyOfLine(firstLine, end);
        }
        firstLine.setColor(0);
        
        ArrayList<TextStyledLine> newLines = new ArrayList<>();
        TextStyledLine curLine = firstLine;
        curLine.setLength(start);
        
        int prev = 0;
        byte[] textChars = text.getBytes(StandardCharsets.UTF_8);
        byte ph = '\0';
        for (int i = 0; i < textChars.length; i++) {
            byte ch = textChars[i];
            if (ch == '\n') {
                curLine.putChar(textChars, prev, ph == '\r' ? i - 1 : i);
                prev = i + 1;
                curLine = createLine(16);
                newLines.add(curLine);
            }
            ph = ch;
        }
        curLine.putChar(textChars, prev, textChars.length);
        
        int cEnd = curLine.getSize();
        curLine.putLine(lastLine);
        removeLine(lastLine);
        
        lines.addAll(line1 + 1, newLines);
        if (caret != null) {
            int chars = CaretControl.countChars(curLine.getTextBytes(), 0, cEnd);
            caret.setLine(line1 + newLines.size());
            caret.setChars(chars);
            caret.setOffset(cEnd);
            caret.setLineOffset(cEnd);
        }
    }
    
    private void removeLine(TextStyledLine line) {
        cache.add(line);
    }
    
    private TextStyledLine createLine(int capacity) {
        if (cache.isEmpty()) {
            return new TextStyledLine(this, capacity);
        } else {
            return cache.remove(cache.size() - 1).recycle(null, 0);
        }
    }
    
    private TextStyledLine copyOfLine(TextStyledLine src, int offset) {
        if (cache.isEmpty()) {
            return new TextStyledLine(this, src.getCapacity()).recycle(src, offset);
        } else {
            return cache.remove(cache.size() - 1).recycle(src, offset);
        }
    }
    
    private int[] localStyles;
    
    public void setBundle(TextStyleBundle bundle) {
        this.bundle = new TextStyleBundle(bundle);
        localStyles = bundle.getLocalStyles();
        for (var line : lines) {
            line.invalidate();
        }
    }
    
    TextStyleBundle getBundle() {
        return bundle;
    }
    
    public void drawText(Graphics graphics, float x, float y, float width, float height) {
        int s = Math.max(0, -Mathf.ceilInt(y / fontHeight) - 1);
        int e = Math.min(lines.size(), s + Mathf.ceilInt(height / fontHeight) + 2);
        for (int i = s; i < e; i++) {
            var line = lines.get(i);
            var buffer = line.getRenderByteBuffer();
            int size = line.getSize();
            int capacity = line.getCapacity();
            if (Color.getAlpha(line.getColor()) > 0) {
                graphics.setColor(line.getColor());
                graphics.drawRect(x, y + i * fontHeight, width, fontHeight, true);
            }
            graphics.drawTextStyled(x, y + i * fontHeight, buffer, 0, size, line.getCapacity(), localStyles);
        }
    }
    
    public void drawLineNumbers(Graphics graphics, float yOff, float x, float y, float width, float height,
                         float lineTextSize, int lineTextColor, int selLineTextColor, int selLine) {
        int s = Math.max(0, Mathf.ceilInt(yOff / fontHeight) - 1);
        int e = Math.min(lines.size(), s + Mathf.ceilInt(height / fontHeight) + 2);
        for (int i = s; i < e; i++) {
            float yPos = (fontHeight - lineTextSize) / 2f;
            int len = toStr(i + 1, tBytes);
            temp.position(0);
            temp.put(tBytes, 0, len);
            graphics.setColor(selLine == i ? selLineTextColor : lineTextColor);
            graphics.drawTextSlice(x, y + i * fontHeight + yPos -yOff, width, fontHeight, temp, 0, len);
        }
    }
    
    public int getWidth(float y, float height) {
        int s = Math.max(0, -Mathf.ceilInt(y / fontHeight) - 1);
        int e = Math.min(lines.size(), s + Mathf.ceilInt(height / fontHeight) + 2);
        int max = 1;
        for (int i = s; i < e; i++) {
            max = Math.max(max, lines.get(i).getCharsCount());
        }
        return max;
    }
    
    public Iterator<String> iterateLines(int start, int end) {
        int size = lines.size();
        start = Math.max(0, Math.min(start, size));
        end = Math.max(0, Math.min(end, size));
        int finalStart = start;
        int finalEnd = end;
        
        
        ListIterator<TextStyledLine> it = lines.listIterator(start);
        
        if (end >= start) {
            return new Iterator<>() {
                int pos = finalStart;
                
                @Override
                public boolean hasNext() {
                    return pos < finalEnd && it.hasNext();
                }
                
                @Override
                public String next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    pos++;
                    return it.next().getString();
                }
            };
        } else {
            return new Iterator<>() {
                int pos = finalStart;
                
                @Override
                public boolean hasNext() {
                    return pos >= finalEnd && it.hasPrevious();
                }
                
                @Override
                public String next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    pos--;
                    return it.previous().getString();
                }
            };
        }
    }
    
    @Override
    public void moveCaretBegin(Caret caret) {
        caret.setLine(0);
        caret.setChars(0);
        caret.setOffset(0);
        caret.setLineOffset(0);
    }
    
    @Override
    public void moveCaretEnd(Caret caret) {
        var line = lines.get(lines.size() - 1);
        caret.setLine(lines.size() - 1);
        caret.setChars(line.getCharsCount());
        caret.setOffset(line.getSize());
        caret.setLineOffset(line.getSize());
    }
    
    @Override
    public void moveCaretWordForward(Caret caret) {
        var line = lines.get(caret.getLine());
        int index = caret.getLineOffset();
        int chars = caret.getChars();
        int prevIndex = index;
        while (index < line.getSize()) {
            index = CaretControl.getNextCharIndex(index, line.getTextBytes(), line.getSize());
            String cp = line.getSubString(prevIndex, index);
            if (cp.isEmpty() || (!Character.isLetterOrDigit(cp.codePointAt(0)) && !"_#".contains(cp))) {
                break;
            }
            prevIndex = index;
            chars++;
        }
        
        caret.setChars(chars);
        caret.setOffset(prevIndex);
        caret.setLineOffset(prevIndex);
    }
    
    @Override
    public void moveCaretWordBackwards(Caret caret) {
        var line = lines.get(caret.getLine());
        int index = caret.getLineOffset();
        int chars = caret.getChars();
        int prevIndex = index;
        while (index > 0) {
            index = CaretControl.getPrevCharIndex(index, line.getTextBytes(), line.getSize());
            String cp = line.getSubString(index, prevIndex);
            if (cp.isEmpty() || (!Character.isLetterOrDigit(cp.codePointAt(0)) && !"_#".contains(cp))) {
                break;
            }
            prevIndex = index;
            chars--;
        }
        
        caret.setChars(chars);
        caret.setOffset(prevIndex);
        caret.setLineOffset(prevIndex);
    }
    
    @Override
    public void moveCaretBackwards(Caret caret) {
        if (caret.getOffset() == 0) {
            if (caret.getLine() > 0) {
                caret.setLine(caret.getLine() - 1);
                var line = lines.get(caret.getLine());
                caret.setChars(line.getCharsCount());
                caret.setOffset(line.getSize());
                caret.setLineOffset(line.getSize());
            }
        } else {
            var line = lines.get(caret.getLine());
            int index = CaretControl.getPrevCharIndex(caret.getLineOffset(), line.getTextBytes(), line.getSize());
            caret.setChars(caret.getChars() - 1);
            caret.setOffset(index);
            caret.setLineOffset(index);
        }
    }
    
    @Override
    public void moveCaretForward(Caret caret) {
        var line = lines.get(caret.getLine());
        if (caret.getLineOffset() == line.getSize()) {
            if (caret.getLine() < lines.size() - 1) {
                caret.setLine(caret.getLine() + 1);
                caret.setChars(0);
                caret.setOffset(0);
                caret.setLineOffset(0);
            }
        } else {
            int index = CaretControl.getNextCharIndex(caret.getLineOffset(), line.getTextBytes(), line.getSize());
            caret.setChars(caret.getChars() + 1);
            caret.setOffset(index);
            caret.setLineOffset(index);
        }
    }
    
    @Override
    public void moveCaretVertical(Caret caret, int jumpLines) {
        if (jumpLines == 0) return;
        
        int nextLine = Math.max(0, Math.min(lines.size() - 1, caret.getLine() + jumpLines));
        
        if (caret.getLine() != nextLine) {
            caret.setLine(nextLine);
            
            var line = lines.get(caret.getLine());
            int targetChars = caret.getChars();
            int index = 0;
            int chars = 0;
            while (chars < targetChars && index < line.getSize()) {
                index = CaretControl.getNextCharIndex(index, line.getTextBytes(), line.getSize());
                chars++;
            }
            caret.setChars(chars);
            caret.setOffset(index);
            caret.setLineOffset(index);
        }
    }
    
    @Override
    public void moveCaretLineBegin(Caret caret) {
        caret.setChars(0);
        caret.setOffset(0);
        caret.setLineOffset(0);
    }
    
    @Override
    public void moveCaretLineEnd(Caret caret) {
        var line = lines.get(caret.getLine());
        caret.setChars(line.getCharsCount());
        caret.setOffset(line.getSize());
        caret.setLineOffset(line.getSize());
    }
    
    @Override
    public String getText(Caret first, Caret second) {
        if (first.getLine() == second.getLine()) {
            return lines.get(first.getLine()).getSubString(first.getLineOffset(), second.getLineOffset());
        }
        
        StringBuilder sb = new StringBuilder();
        int index = first.getLine();
        var it = lines.listIterator(first.getLine());
        while (it.hasNext()) {
            var nextLine = it.next();
            if (index == first.getLine()) {
                sb.append(nextLine.getSubString(first.getLineOffset(), nextLine.getSize())).append("\n");
            } else if (index == second.getLine()) {
                sb.append(nextLine.getSubString(0, second.getLineOffset()));
                break;
            } else {
                sb.append(nextLine.getString()).append("\n");
            }
            index++;
        }
        return sb.toString();
    }
    
    public boolean isCaretLastOfLine(Caret endCaret) {
        return lines.get(endCaret.getLine()).getSize() == endCaret.getLineOffset();
    }
    
    public int getTotalLines() {
        return lines.size();
    }
    
    public void setLineColor(int line, int color) {
        if (line >= 0 && line < lines.size()) {
            lines.get(line).setColor(color);
        }
    }
    
    public void clearLinesColor() {
        for (var line : lines) {
            line.setColor(0);
        }
    }
    
    private static int toStr(int value, byte[] bytes) {
        if (value < 10) {
            bytes[0] = (byte) ('0' + value);
            return 1;
        }
        
        int v = value;
        
        int temp = v;
        int count = 0;
        while (temp > 0) {
            temp /= 10;
            count++;
        }
        
        int pos = count - 1;
        
        while (v > 0) {
            int digit = v % 10;
            bytes[pos--] = (byte) ('0' + digit);
            v /= 10;
        }
        
        return count;
    }
}
