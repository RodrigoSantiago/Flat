package flat.graphics.context.fonts;

import flat.graphics.symbols.FontStyle;
import flat.window.Application;
import flat.window.SystemType;

import java.util.ArrayList;
import java.util.HashMap;

public class SystemFonts {
    public static HashMap<String, ArrayList<FontDetail>> listSystemFontFamilies() {
        if (Application.getSystemType() == SystemType.WINDOWS) {
            return WindowsSystemFonts.listSystemFontFamilies();
        } else if (Application.getSystemType() == SystemType.UNIX) {
            return UnixSystemFonts.listSystemFontFamilies();
        } else {
            return new HashMap<>();
        }
    }

    public static FontStyle guessStyle(String name) {
        String lower = name.toLowerCase();
        if (lower.contains("script") || lower.contains("cursive") || lower.contains("calligraphy")) {
            return FontStyle.CURSIVE;
        }
        if (lower.contains("icon") || lower.contains("emoji") || lower.contains("symbol")) {
            return FontStyle.FANTASY;
        }
        if (lower.contains("mono")) {
            return FontStyle.MONO;
        }
        if (lower.contains("sans")) {
            return FontStyle.SANS;
        }
        if (lower.contains("serif")) {
            return FontStyle.SERIF;
        }
        return FontStyle.SANS;
    }
}
