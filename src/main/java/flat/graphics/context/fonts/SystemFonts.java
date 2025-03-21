package flat.graphics.context.fonts;

import flat.window.Application;
import flat.window.SystemType;

import java.util.ArrayList;
import java.util.HashMap;

public class SystemFonts {
    public static HashMap<String, ArrayList<FontDetail>> listSystemFontFamilies() {
        if (Application.getSystemType() == SystemType.WINDOWS) {
            return WindowsSystemFonts.listSystemFontFamilies();
        } else {
            return new HashMap<>();
        }
    }
}
