package flat.graphics.symbols;

import flat.exception.FlatException;
import flat.graphics.context.fonts.FontDetail;
import flat.graphics.context.fonts.SystemFonts;
import flat.window.Application;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FontManager {

    private static Font DefaultFont;
    private static final ArrayList<Font> fonts = new ArrayList<>();
    private static final ArrayList<Font> defaultFonts = new ArrayList<>();
    private static final HashMap<String, ArrayList<Font>> fontFamilies = new HashMap<>();

    public static Font findFont(String family) {
        return findFont(family, null, null, null);
    }

    public static Font findFont(FontWeight weight) {
        return findFont(null, weight);
    }

    public static Font findFont(FontPosture posture) {
        return findFont(null, null, posture);
    }

    public static Font findFont(FontStyle style) {
        return findFont(null, null, null, style);
    }

    public static Font findFont(String family, FontWeight weight) {
        return findFont(family, weight, null, null);
    }

    public static Font findFont(String family, FontWeight weight, FontPosture posture) {
        return findFont(family, weight, posture, null);
    }

    public static Font findFont(String family, FontWeight weight, FontPosture posture, FontStyle style) {
        readDefaultFonts();

        ArrayList<Font> fontFamily;
        if (family == null) {
            fontFamily = defaultFonts;
        } else {
            fontFamily = fontFamilies.get(family);
            if (fontFamily == null) {
                fontFamily = defaultFonts;
            }
        }
        if (weight == null) weight = FontWeight.NORMAL;
        if (posture == null) posture = FontPosture.REGULAR;
        if (style == null) style = FontStyle.SANS;

        Font closer = fontFamily.get(0);
        for (Font font : fontFamily) {
            if (getFontDifference(weight, posture, style,
                    closer.getWeight(), closer.getPosture(), closer.getStyle(),
                    font.getWeight(), font.getPosture(), font.getStyle()) == 2) {
                closer = font;
            }

        }
        return closer;
    }

    private static int getFontDifference(
            FontWeight weightT, FontPosture postureT, FontStyle styleT,
            FontWeight weightA, FontPosture postureA, FontStyle styleA,
            FontWeight weightB, FontPosture postureB, FontStyle styleB) {

        if (styleA != styleB) {
            if (styleA == styleT) return 1;
            if (styleB == styleT) return 2;
            if (styleA.ordinal() < styleB.ordinal()) return 1;
            return 2;
        }

        if (postureA != postureB) {
            if (postureA == postureT) return 1;
            if (postureB == postureT) return 2;
            if (postureA.ordinal() < postureB.ordinal()) return 1;
            return 2;
        }

        if (weightA != weightB) {
            if (weightA == weightT) return 1;
            if (weightB == weightT) return 2;
            int wA = Math.abs(weightA.getWeight() - weightT.getWeight());
            int wB = Math.abs(weightA.getWeight() - weightT.getWeight());
            if (wA != wB) return wA < wB ? 1 : 2;
            if (weightA.ordinal() < weightB.ordinal()) return 1;
            return 2;
        }

        return 1;
    }

    public static void install(Font font) {
        font.checkDisposed();

        if (fonts.contains(font)) return;

        fonts.add(font);

        var family = fontFamilies.get(font.getFamily());
        if (family == null) {
            family = new ArrayList<>();
            fontFamilies.put(font.getFamily(), family);
        }
        family.add(font);
    }

    static void uninstall(Font font) {
        if (defaultFonts.contains(font)) {
            throw new FlatException("A default font cannot be disposed");
        }

        var list = fontFamilies.get(font.getFamily());
        if (list != null) {
            list.remove(font);
        }
        fonts.remove(font);
    }

    public static void installSystemFontFamily(String fontFamily) {
        readDefaultFonts();

        ArrayList<FontDetail> list = SystemFonts.listSystemFontFamilies().get(fontFamily);
        if (list == null) {
            return;
        }

        for (var detail : list) {
            ArrayList<Font> fontList = fontFamilies.get(fontFamily);
            if (fontList == null) {
                fontList = new ArrayList<>();
                fontFamilies.put(fontFamily, fontList);
            } else {
                Font instFont = findFont(fontFamily, detail.getWeight(), detail.getPosture(), detail.getStyle());
                if (instFont.getFamily().equals(fontFamily) &&
                        instFont.getWeight() == detail.getWeight() &&
                        instFont.getPosture() == detail.getPosture() &&
                        instFont.getStyle() == detail.getStyle()) {
                    continue;
                }
            }

            Font font = createSystemFont(detail);
            if (font != null) {
                install(font);
            }
        }
    }

    public static ArrayList<FontDetail> listSystemFonts() {
        readDefaultFonts();

        ArrayList<FontDetail> list = new ArrayList<>();
        for (var entry : SystemFonts.listSystemFontFamilies().values()) {
            list.addAll(entry);
        }
        return list;
    }

    public static Font createSystemFont(FontDetail fontDetail) {
        readDefaultFonts();

        try {
            byte[] data = Files.readAllBytes(fontDetail.getFile().toPath());
            return new Font(fontDetail.getFamily(), fontDetail.getWeight(), fontDetail.getPosture(), fontDetail.getStyle(), data);
        } catch (IOException e) {
            return null;
        }
    }

    private static void readDefaultFonts() {
        if (DefaultFont != null) {
            return;
        }
        var res = Application.getResourcesManager();

        var sans = new Font("Roboto", FontWeight.NORMAL, FontPosture.REGULAR, FontStyle.SANS
                , res.getData("default/fonts/Roboto-Regular.ttf"));
        install(sans);
        var bold = new Font("Roboto", FontWeight.BOLD, FontPosture.REGULAR, FontStyle.SANS
                , res.getData("default/fonts/Roboto-Bold.ttf"));
        install(bold);
        var italic = new Font("Roboto", FontWeight.NORMAL, FontPosture.ITALIC, FontStyle.SANS
                , res.getData("default/fonts/Roboto-Italic.ttf"));
        install(italic);
        var bolditalic = new Font("Roboto", FontWeight.BOLD, FontPosture.ITALIC, FontStyle.SANS
                , res.getData("default/fonts/Roboto-BoldItalic.ttf"));
        install(bolditalic);
        var serif = new Font("Roboto", FontWeight.NORMAL, FontPosture.REGULAR, FontStyle.SERIF
                , res.getData("default/fonts/RobotoSerif-Regular.ttf"));
        install(serif);
        var mono = new Font("Roboto", FontWeight.NORMAL, FontPosture.REGULAR, FontStyle.MONO
                , res.getData("default/fonts/RobotoMono-Regular.ttf"));
        install(mono);
        var cursive = new Font("DancingScript", FontWeight.NORMAL, FontPosture.REGULAR, FontStyle.CURSIVE
                , res.getData("default/fonts/DancingScript-Regular.ttf"));
        install(cursive);

        DefaultFont = sans;
        defaultFonts.addAll(List.of(sans, bold, italic, bolditalic, serif, mono, cursive));
    }

    public static Font getDefault() {
        readDefaultFonts();

        return DefaultFont;
    }
}
