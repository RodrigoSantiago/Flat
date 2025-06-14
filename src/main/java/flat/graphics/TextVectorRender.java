package flat.graphics;

import flat.graphics.context.Glyph;
import flat.graphics.symbols.Font;
import flat.math.Affine;
import flat.math.Mathf;
import flat.math.shapes.Path;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class TextVectorRender {

    private Font font;
    private float size = 16f;
    private HashMap<Integer, GlyphData> cache = new HashMap<>();

    public TextVectorRender(Font font) {
        this.font = font;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        if (this.font != font) {
            this.font = font;
            cache.clear();
        }
    }

    public void drawText(Graphics graphics, float x, float y, String text, float maxWidth, float maxHeight, boolean fill) {
        CodepointIterator it = new CodepointIteratorString(text);
        Affine affine = new Affine();
        float off = size / font.getSize();
        float px = x;
        float descent = font.getDescent(font.getSize());
        int prev = -1;
        while (it.next()) {
            int codePoint = it.get();
            GlyphData glyphData = getGlyphData(codePoint);
            affine.identity();
            if (graphics.isAntialiasEnabled()) {
                affine.translate(px, y);
            } else {
                affine.translate(Mathf.round(px), Mathf.round(y));
            }
            affine.scale(off, off);
            affine.translate(0, descent);
            graphics.drawPath(glyphData.path, fill, affine);
            px += (glyphData.glyph.getAdvance() + (prev == -1 ? 0 : font.getGlyphKerning(prev, codePoint))) * off;
            prev = codePoint;
            if (maxWidth > 0 && px > maxWidth) return;
        }
    }

    private GlyphData getGlyphData(int codePoint) {
        GlyphData glyphData = cache.get(codePoint);
        if (glyphData == null) {
            Glyph glyph = font.getGlyphData(codePoint);
            Path path = font.getGlyphPath(codePoint);
            glyphData = new GlyphData(glyph, path);
            cache.put(codePoint, glyphData);
        }
        return glyphData;
    }

    public interface CodepointIterator {
        int get();
        boolean next();
        boolean prev();
    }

    public static class CodepointIteratorString implements CodepointIterator{
        private final String string;
        private int pos = -1;

        public CodepointIteratorString(String string) {
            this.string = string;
        }

        public int get() {
            return string.codePointAt(pos);
        }

        public boolean next() {
            if (pos == -1) {
                pos = 0;
                if (pos < string.length()) {
                    return true;
                }
            } else if (pos < string.length()) {
                pos = string.offsetByCodePoints(pos, 1);
                if (pos < string.length()) {
                    return true;
                }
            }
            return false;
        }

        public boolean prev() {
            if (pos > 0) {
                pos = string.offsetByCodePoints(pos, -1);
                return true;
            }
            return false;
        }
    }

    private class GlyphData {
        private Glyph glyph;
        private Path path;

        public GlyphData(Glyph glyph, Path path) {
            this.glyph = glyph;
            this.path = path;
        }
    }
}
