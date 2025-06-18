package flat.graphics.symbols;

import flat.exception.FlatException;
import flat.graphics.context.Glyph;
import flat.graphics.image.Drawable;
import flat.graphics.image.ImageVector;
import flat.math.shapes.Path;
import flat.math.shapes.Rectangle;

import java.util.HashMap;

public class IconBundle {

    private final String name;
    private final Font font;
    private final HashMap<String, Integer> iconsName;
    private final HashMap<Integer, Drawable> drawables = new HashMap<>();

    public IconBundle(String name, Font font, String iconsNameList) {
        this.name = name;
        this.font = font;
        try {
            HashMap<String, Integer> map = new HashMap<>();
            String[] lines = iconsNameList.split("\n");
            for (var line : lines) {
                String[] nameValue = line.split(" ");
                String iconName = nameValue[0].trim();
                Integer value = Integer.parseInt(nameValue[1].trim(), 16);
                map.put(iconName, value);
            }
            iconsName = map;
        } catch (Exception e) {
            throw new FlatException("Invalid icons name list");
        }
    }

    public IconBundle(String name, Font font, HashMap<String, Integer> iconsName) {
        this.name = name;
        this.font = font;
        this.iconsName = new HashMap<>(iconsName);
    }

    public String getName() {
        return name;
    }

    public Font getFont() {
        return font;
    }

    public Drawable getIcon(int unicode) {
        Drawable drawable = drawables.get(unicode);
        if (drawable == null && !drawables.containsKey(unicode)) {
            Path path = font.getGlyphPath(unicode);
            Glyph glyph = font.getGlyphData(unicode);
            if (path != null) {
                var bounds = path.bounds();
                float a = font.getAscent(font.getSize()) + font.getDescent(font.getSize());

                Rectangle view = new Rectangle(
                        -(a - bounds.width) * 0.5f + bounds.x,
                        -(a - bounds.height) * 0.5f + bounds.y, a, a);
                drawable = new ImageVector(view, path);
            }
            drawables.put(unicode, drawable);
        }
        return drawable;
    }

    public Drawable getIcon(String text) {
        Integer unicode = iconsName.get(text);
        if (unicode == null && !text.isEmpty()) {
            unicode = text.codePointAt(0);
        }
        if (unicode != null) {
            return getIcon(unicode);
        }
        return null;
    }
}
