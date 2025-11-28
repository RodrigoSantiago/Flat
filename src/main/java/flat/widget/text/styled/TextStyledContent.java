package flat.widget.text.styled;

import flat.graphics.Graphics;
import flat.math.Mathf;
import flat.widget.text.content.Caret;
import flat.widget.text.content.CaretControl;
import flat.widget.text.content.TextContentController;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TextStyledContent implements TextContentController {
    
    private float fontHeight = 16f;
    private float fontWidth = 16f;
    
    private TextStyleBundle bundle;
    private ArrayList<TextStyledLine> cache = new ArrayList<>();
    private TreeList<TextStyledLine> lines = new TreeList<>();
    
    public TextStyledContent() {
        lines.add(createLine(16));
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
        lines.get(lineId);
        
        var line = lines.get(lineId);
        int targetOffset = Mathf.round(px / fontWidth);
        int count = 0;
        int offset = 0;
        while (offset < targetOffset && count < line.getSize()) {
            count = CaretControl.getNextCharIndex(count, line.getTextBytes(), line.getSize());
            offset++;
        }
        caret.setLine(lineId);
        caret.setOffset(offset);
        caret.setLineChar(count);
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
        
        ArrayList<TextStyledLine> newLines = new ArrayList<>();
        TextStyledLine curLine = firstLine;
        curLine.setLength(start);
        
        int prev = 0;
        byte[] textChars = text.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < textChars.length; i++) {
            byte ch = textChars[i];
            if (ch == '\n') {
                curLine.putChar(textChars, prev, i);
                prev = i + 1;
                curLine = createLine(16);
                newLines.add(curLine);
            }
        }
        curLine.putChar(textChars, prev, textChars.length);
        
        int cEnd = curLine.getSize();
        curLine.putLine(lastLine);
        removeLine(lastLine);
        
        lines.addAll(line1 + 1, newLines);
        if (caret != null) {
            caret.setLine(line1 + newLines.size());
            caret.setLineChar(cEnd);
            caret.setOffset(CaretControl.countChars(curLine.getTextBytes(), 0, cEnd));
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
            graphics.drawTextStyled(x, y + i * fontHeight, buffer, 0, size, line.getCapacity(), localStyles);
        }
    }
    
    public int getWidth(float y, float height) {
        int s = Math.max(0, -Mathf.ceilInt(y / fontHeight) - 1);
        int e = Math.min(lines.size(), s + Mathf.ceilInt(height / fontHeight) + 2);
        int max = 1;
        for (int i = s; i < e; i++) {
            max = Math.max(max, lines.get(i).getOffsetLength());
        }
        return max;
    }
    
    @Override
    public void moveCaretBegin(Caret caret) {
        caret.set(0, 0, 0);
    }
    
    @Override
    public void moveCaretEnd(Caret caret) {
        var line = lines.get(lines.size() - 1);
        caret.set(lines.size() - 1, line.getSize(), CaretControl.countChars(line.getTextBytes(), 0, line.getSize()));
    }
    
    @Override
    public void moveCaretWordForward(Caret caret) {
        var line = lines.get(caret.getLine());
        int index = caret.getLineChar();
        int offset = caret.getOffset();
        int prevIndex = index;
        while (index < line.getSize()) {
            index = CaretControl.getNextCharIndex(index, line.getTextBytes(), line.getSize());
            String cp = line.getSubString(prevIndex, index);
            if (cp.isEmpty() || (!Character.isLetterOrDigit(cp.codePointAt(0)) && !"_#".contains(cp))) {
                break;
            }
            prevIndex = index;
            offset++;
        }
        
        caret.setLineChar(prevIndex);
        caret.setOffset(offset);
    }
    
    @Override
    public void moveCaretWordBackwards(Caret caret) {
        var line = lines.get(caret.getLine());
        int index = caret.getLineChar();
        int offset = caret.getOffset();
        int prevIndex = index;
        while (index > 0) {
            index = CaretControl.getPrevCharIndex(index, line.getTextBytes(), line.getSize());
            String cp = line.getSubString(index, prevIndex);
            if (cp.isEmpty() || (!Character.isLetterOrDigit(cp.codePointAt(0)) && !"_#".contains(cp))) {
                break;
            }
            prevIndex = index;
            offset--;
        }
        
        caret.setLineChar(prevIndex);
        caret.setOffset(offset);
    }
    
    @Override
    public void moveCaretBackwards(Caret caret) {
        if (caret.getLineChar() == 0) {
            if (caret.getLine() > 0) {
                caret.setLine(caret.getLine() - 1);
                var line = lines.get(caret.getLine());
                caret.setLineChar(line.getSize());
                caret.setOffset(CaretControl.countChars(line.getTextBytes(), 0, line.getSize()));
            }
        } else {
            var line = lines.get(caret.getLine());
            int index = CaretControl.getPrevCharIndex(caret.getLineChar(), line.getTextBytes(), line.getSize());
            caret.setLineChar(index);
            caret.setOffset(caret.getOffset() - 1);
        }
    }
    
    @Override
    public void moveCaretForward(Caret caret) {
        var line = lines.get(caret.getLine());
        if (caret.getLineChar() == line.getSize()) {
            if (caret.getLine() < lines.size() - 1) {
                caret.setLine(caret.getLine() + 1);
                caret.setLineChar(0);
                caret.setOffset(0);
            }
        } else {
            int index = CaretControl.getNextCharIndex(caret.getLineChar(), line.getTextBytes(), line.getSize());
            caret.setLineChar(index);
            caret.setOffset(caret.getOffset() + 1);
        }
    }
    
    @Override
    public void moveCaretVertical(Caret caret, int jumpLines) {
        if (jumpLines == 0) return;
        
        var line = lines.get(caret.getLine());
        int nextLine = Math.max(0, Math.min(lines.size() - 1, caret.getLine() + jumpLines));
        
        if (caret.getLine() != nextLine) {
            int charCount = CaretControl.countChars(line.getTextBytes(), 0, caret.getLineChar());
            caret.setLine(nextLine);
            
            line = lines.get(caret.getLine());
            int targetOffset = caret.getOffset();
            int count = 0;
            int offset = 0;
            while (offset < targetOffset && count < line.getSize()) {
                count = CaretControl.getNextCharIndex(count, line.getTextBytes(), line.getSize());
                offset++;
            }
            caret.setOffset(offset);
            caret.setLineChar(count);
        }
    }
    
    @Override
    public void moveCaretLineBegin(Caret caret) {
        caret.setLineChar(0);
        caret.setOffset(0);
    }
    
    @Override
    public void moveCaretLineEnd(Caret caret) {
        var line = lines.get(caret.getLine());
        caret.setLineChar(line.getSize());
        caret.setOffset(CaretControl.countChars(line.getTextBytes(), 0, line.getSize()));
    }
    
    @Override
    public String getText(Caret first, Caret second) {
        if (first.getLine() == second.getLine()) {
            return lines.get(first.getLine()).getSubString(first.getLineChar(), second.getLineChar());
        }
        
        StringBuilder sb = new StringBuilder();
        int index = first.getLine();
        var it = lines.listIterator(first.getLine());
        while (it.hasNext()) {
            var nextLine = it.next();
            if (index == first.getLine()) {
                sb.append(nextLine.getSubString(first.getLineChar(), nextLine.getSize())).append("\n");
            } else if (index == second.getLine()) {
                sb.append(nextLine.getSubString(0, second.getLineChar()));
                break;
            } else {
                sb.append(nextLine.getString()).append("\n");
            }
            index++;
        }
        return sb.toString();
    }
    
    public boolean isCaretLastOfLine(Caret endCaret) {
        return lines.get(endCaret.getLine()).getSize() == endCaret.getLineChar();
    }
    
    public int getTotalLines() {
        return lines.size();
    }
}
