package flat.widget.text;

import flat.graphics.SmartContext;
import flat.graphics.context.Font;
import flat.math.Affine;
import flat.math.Vector2;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TextRender {

    private Font font;
    private ByteBuffer buffer;
    private ArrayList<Line> lines;
    private int byteSize;
    private float width;

    public void setText(String text) {
        if (lines != null) {
            lines.clear();
        }

        if (text == null || text.length() == 0) {
            byteSize = 0;
            return;
        }

        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        if (buffer == null) {
            buffer = ByteBuffer.allocateDirect(bytes.length);
        } else if (buffer.capacity() < bytes.length) {
            int newCapacity = Integer.highestOneBit(bytes.length);
            if (newCapacity < bytes.length) {
                newCapacity <<= 1;
            }
            buffer = ByteBuffer.allocateDirect(newCapacity);
        }
        buffer.position(0);
        buffer.put(bytes);
        byteSize = bytes.length;

        int prev = 0;
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i] == '\n') {
                if (lines == null) {
                    lines = new ArrayList<>();
                }
                lines.add(new Line(prev, i - prev));
                prev = i + 1;
            }
        }

        if (lines != null) {
            lines.add(new Line(prev, bytes.length - prev));
        }
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public float getTextWidth(float textSize) {
        if (byteSize == 0 || font == null) {
            return 0;
        }
        if (lines == null || lines.isEmpty()) {
            return font.getWidth(buffer, 0, byteSize, textSize, 1);
        }
        width = 0;
        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            if (line.length == 0) {
                line.width = 0;
            } else {
                line.width = font.getWidth(buffer, line.start, line.length, textSize, 1);
            }
            width = Math.max(width, line.width);
        }
        return width;
    }

    public float getTextHeight(float textSize) {
        if (font == null) {
            return textSize * (lines == null || lines.size() == 0 ? 1 : lines.size());
        }
        return font.getHeight(textSize) * (lines == null || lines.size() == 0 ? 1 : lines.size());
    }

    public void drawText(SmartContext context, float textSize, float x, float y, float width, float height, HorizontalAlign align) {
        if (byteSize == 0 || font == null) {
            return;
        }
        if (lines == null || lines.isEmpty()) {
            context.drawTextSlice(x, y, width, height, buffer, 0, byteSize);
            return;
        }
        float lineHeight = font.getHeight(textSize);
        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            if (line.length == 0) {
                continue;
            }

            float xpos = x;
            float ypos = y + (i * lineHeight);
            float wd = width;
            float hg = height - (i * lineHeight);
            if (align == HorizontalAlign.RIGHT) {
                float off = Math.max(0, this.width - line.width);
                xpos = x + off;
                wd -= off;
            } else if (align == HorizontalAlign.CENTER) {
                float off = Math.max(0, this.width - line.width) * 0.5f;
                xpos = x + off;
                wd -= off;
            }
            if (wd > 0 && hg > 0) {
                context.drawTextSlice(xpos, ypos, wd, hg, buffer, line.start, line.length);
            }
        }
    }

    private static class Line {
        int start;
        int length;
        float width;

        public Line(int start, int length) {
            this.start = start;
            this.length = length;
        }
    }
}
