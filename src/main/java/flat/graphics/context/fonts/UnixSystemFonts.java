package flat.graphics.context.fonts;

import java.io.File;
import java.util.*;

public class UnixSystemFonts {

    static HashMap<String, ArrayList<FontDetail>> listSystemFontFamilies() {
        String[] fontDirectories = {
                "/usr/share/fonts",
                "/usr/local/share/fonts",
                System.getProperty("user.home") + "/.fonts",
                "/usr/X11R6/lib/X11/fonts"
        };
        List<File> ttfFonts = new ArrayList<>();
        for (String dir : fontDirectories) {
            File fontDir = new File(dir);
            if (fontDir.exists() && fontDir.isDirectory()) {
                File[] ttfFiles = fontDir.listFiles((dir1, name) -> name.toLowerCase().endsWith(".ttf"));
                if (ttfFiles != null) {
                    ttfFonts.addAll(Arrays.asList(ttfFiles));
                }
            }
        }
        ttfFonts.sort(Comparator.comparing(File::getName));
        return new HashMap<>();
    }
}
