package flat.graphics.context.fonts;

import flat.graphics.text.FontPosture;
import flat.graphics.text.FontStyle;
import flat.graphics.text.FontWeight;

import java.io.File;
import java.util.*;

class WindowsSystemFonts {

    private static String fontNameRegex =
            "[a-zA-Z0-9_ ]+-(bold|bolditalic|extralight|extralightitalic|italic|light|lightitalic|medium|mediumitalic|regular|semibold|semibolditalic|thin|thinitalic|black|blackitalic)";

    private static int count;
    private static long lastTime;
    private static HashMap<String, ArrayList<FontDetail>> cacheFonts;

    static HashMap<String, ArrayList<FontDetail>> listSystemFontFamilies() {
        String path = System.getenv("WINDIR");

        File fontsDir = new File(path, "Fonts");
        if (!fontsDir.exists()) {
            return new HashMap<>();
        }

        File[] children = fontsDir.listFiles();
        if (children == null) {
            return new HashMap<>();
        }

        if (cacheFonts != null) {
            if (count != children.length) {
                cacheFonts = null;
            } else {
                long max = 0;
                for (File child : children) {
                    long lm = child.lastModified();
                    if (max < lm) {
                        max = lm;
                    }
                }
                if (max != lastTime) {
                    cacheFonts = null;
                }
            }
        }
        if (cacheFonts != null) {
            return cacheFonts;
        }

        Arrays.sort(children, (f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));

        HashMap<String, SystemFont> nameList = new HashMap<>();
        ArrayList<SystemFont> allFamilies = new ArrayList<>();

        for (File child : children) {
            if (!child.getName().endsWith(".ttf") /*&& !child.getName().endsWith(".ttc")*/) {
                continue;
            }

            String originalName = child.getName().endsWith(".ttf") ?
                    child.getName().substring(0, child.getName().lastIndexOf(".ttf")) :
                    child.getName().substring(0, child.getName().lastIndexOf(".ttc"));
            String fName = originalName.toLowerCase();

            boolean sameFamily = false;
            for (var systemFont : allFamilies) {
                String family = systemFont.family;
                if (alone.contains(fName)) {
                    break;
                }
                if (fName.startsWith(family)) {
                    systemFont.add(new SystemFontVariant(child));
                    sameFamily = true;
                    break;
                }
                if ((family.length() > 5 && fName.startsWith(family.substring(0, family.length() - 1)))) {
                    systemFont.family = family.substring(0, family.length() - 1);
                    systemFont.add(new SystemFontVariant(child));
                    sameFamily = true;
                    break;
                }
                if ((family.length() > 5 && fName.startsWith(family.substring(0, family.length() - 2)))) {
                    systemFont.family = family.substring(0, family.length() - 2);
                    systemFont.add(new SystemFontVariant(child));
                    sameFamily = true;
                    break;
                }
                if (family.contains("-")
                        && fName.startsWith(family.substring(0, family.indexOf("-")))
                        && family.matches(fontNameRegex)) {
                    systemFont.family = family.substring(0, family.indexOf("-"));
                    systemFont.add(new SystemFontVariant(child));
                    sameFamily = true;
                    break;
                }
            }
            if (!sameFamily) {
                var sf = new SystemFont(fName, originalName);
                sf.add(new SystemFontVariant(child));
                allFamilies.add(sf);
            }
        }
        for (var a : allFamilies) {
            String microsoftName = families.get(a.family);
            if (microsoftName != null) {
                a.name = microsoftName;
            } else {
                a.name = a.originalName;
            }
            FontStyle style = styles.get(a.name);
            if (style != null) {
                a.style = style;
            } else {
                a.style = a.family.contains("mono") ? FontStyle.MONO : FontStyle.SANS;
            }
            for (var variant : a.variants) {
                variant.updateData(a);
            }
        }

        cacheFonts = new HashMap<>();
        for (var family : allFamilies) {
            ArrayList<FontDetail> fonts = new ArrayList<>();
            for (var variant : family.variants) {
                fonts.add(new FontDetail(variant.file, family.name, variant.posture, variant.weight, variant.style));
            }
            cacheFonts.put(family.name, fonts);
        }
        return cacheFonts;
    }

    private static class SystemFont {
        String originalName;
        String name;
        String family;
        FontStyle style;
        ArrayList<SystemFontVariant> variants = new ArrayList<>();

        public SystemFont(String family, String originalName) {
            this.originalName = originalName;
            this.family = family;
        }

        public void add(SystemFontVariant variant) {
            variants.add(variant);
        }
    }

    private static class SystemFontVariant {
        FontWeight weight = FontWeight.NORMAL;
        FontPosture posture = FontPosture.REGULAR;
        FontStyle style;
        File file;
        String name;

        public SystemFontVariant(File file) {
            this.file = file;
            name = file.getName().endsWith(".ttf") ?
                    file.getName().substring(0, file.getName().lastIndexOf(".ttf")).toLowerCase() :
                    file.getName().substring(0, file.getName().lastIndexOf(".ttc")).toLowerCase();
        }

        @Override
        public String toString() {
            return name + " = " + weight + ", " + posture;
        }

        private void updateData(SystemFont parent) {
            this.style = parent.style;
            if (name.startsWith(parent.family)) {
                String rest = name.substring(parent.family.length());
                if (rest.startsWith("-")) {
                    if (rest.contains("italic")) posture = FontPosture.ITALIC;
                    if (rest.contains("bold")) weight = FontWeight.BOLD;
                    if (rest.contains("light")) weight = FontWeight.LIGHT;
                    if (rest.contains("semibold")) weight = FontWeight.SEMI_BOLD;
                    if (rest.contains("extrabold")) weight = FontWeight.EXTRA_BOLD;
                    if (rest.contains("extralight")) weight = FontWeight.EXTRA_LIGHT;
                    if (rest.contains("black")) weight = FontWeight.BLACK;
                    if (rest.contains("thin")) weight = FontWeight.THIN;
                    if (rest.contains("medium")) weight = FontWeight.MEDIUM;
                } else {
                    if (rest.contains("i")) posture = FontPosture.ITALIC;
                    if (rest.contains("ob")) posture = FontPosture.ITALIC;

                    if (rest.contains("bl") || rest.contains("blk")) {
                        weight = FontWeight.BLACK;
                    } else {
                        if (rest.contains("b") && !rest.contains("ob")) weight = FontWeight.BOLD;
                        if (rest.contains("l")) weight = FontWeight.LIGHT;
                    }

                    if (rest.contains("m")) weight = FontWeight.MEDIUM;
                    if (rest.contains("sb")) weight = FontWeight.SEMI_BOLD;
                    if (rest.contains("sl")) weight = FontWeight.LIGHT;
                    if (rest.contains("xb")) weight = FontWeight.EXTRA_BOLD;
                    if (rest.contains("ul") || rest.contains("hl")) weight = FontWeight.EXTRA_LIGHT;
                    if (rest.contains("c")) weight = FontWeight.THIN;
                    if (rest.contains("z")) {
                        posture = FontPosture.ITALIC;
                        weight = FontWeight.BOLD;
                    }
                }
            }
        }
    }
    
    static HashMap<String, FontStyle> styles = new HashMap<>();
    static HashMap<String, String> families = new HashMap<>();

    static List<String> alone = List.of("seguiemj", "seguisym", "seguihis", "segoesc", "segoepr", "segoescb", "segoeprb");

    static {
        nameByFile();
        styleByName();
    }

    private static void nameByFile() {
        families.put("arial", "Arial");
        families.put("ariblk", "Arial Black");
        families.put("bahnschrift", "Bahnschrift");
        families.put("calibri", "Calibri");
        families.put("cambria", "Cambria");
        families.put("cambria math", "Cambria Math");
        families.put("candara", "Candara");
        families.put("comic", "Comic Sans MS");
        families.put("consola", "Consolas");
        families.put("cascadiacode", "Cascadia Code");
        families.put("cascadiamono", "Cascadia Mono");
        families.put("constan", "Constantia");
        families.put("corbel", "Corbel");
        families.put("cour", "Courier New");
        families.put("ebrima", "Ebrima");
        families.put("framd", "Franklin Gothic Medium");
        families.put("gabriola", "Gabriola");
        families.put("gadugi", "Gadugi");
        families.put("georgia", "Georgia");
        families.put("holomdl2", "HoloLens MDL2 Assets");
        families.put("impact", "Impact");
        families.put("inkfree", "Ink Free");
        families.put("javatext", "Javanese Text");
        families.put("leelawui", "Leelawadee UI");
        families.put("leelauib", "Leelawadee UI");
        families.put("leeluisl", "Leelawadee UI");
        families.put("lucon", "Lucida Console");
        families.put("l_10646", "Lucida Sans Unicode");
        families.put("malgun", "Malgun Gothic");
        families.put("marlett", "Marlett");
        families.put("himalaya", "Microsoft Himalaya");
        families.put("msjh", "Microsoft JhengHei");
        families.put("ntailu", "Microsoft New Tai Lue");
        families.put("phagspa", "Microsoft PhagsPa");
        families.put("micross", "Microsoft Sans Serif");
        families.put("taile", "Microsoft Tai Le");
        families.put("msyh", "Microsoft YaHei");
        families.put("msyi", "Microsoft Yi Baiti");
        families.put("mingliub", "MingLiU-ExtB");
        families.put("monbaiti", "Mongolian Baiti");
        families.put("msgothic", "MS Gothic");
        families.put("mvboli", "MV Boli");
        families.put("mmrtext", "Myanmar Text");
        families.put("nirmalas", "Nirmala UI");
        families.put("pala", "Palatino Linotype");
        families.put("segoe", "Segoe");
        families.put("segmdl2", "Segoe MDL2 Assets");
        families.put("segoepr", "Segoe Print");
        families.put("segoeprb", "Segoe Print Bold");
        families.put("segoesc", "Segoe Script");
        families.put("segoescb", "Segoe Script Bold");
        families.put("segoeui", "Segoe UI");
        families.put("segui", "Segoe UI");
        families.put("seguihis", "Segoe UI Historic");
        families.put("seguiemj", "Segoe UI Emoji");
        families.put("seguisym", "Segoe UI Symbol");
        families.put("simsun", "SimSun");
        families.put("sitka", "Sitka");
        families.put("sylfaen", "Sylfaen");
        families.put("symbol", "Symbol");
        families.put("tahoma", "Tahoma");
        families.put("times", "Times New Roman");
        families.put("trebuc", "Trebuchet MS");
        families.put("verdana", "Verdana");
        families.put("webdings", "Webdings");
        families.put("wingding", "Wingdings");
        families.put("yugoth", "Yu Gothic");
    }
    
    private static void styleByName() {
        styles.put("Arial".toLowerCase(), FontStyle.SANS);
        styles.put("Arial Black", FontStyle.SANS);
        styles.put("Bahnschrift", FontStyle.SANS);
        styles.put("Calibri", FontStyle.SANS);
        styles.put("Cambria", FontStyle.SERIF);
        styles.put("Cambria Math", FontStyle.SERIF);
        styles.put("Candara", FontStyle.SANS);
        styles.put("Cascadia Code Mono", FontStyle.MONO);
        styles.put("Comic Sans MS", FontStyle.CURSIVE);
        styles.put("Consolas", FontStyle.MONO);
        styles.put("Constantia", FontStyle.SERIF);
        styles.put("Corbel", FontStyle.SANS);
        styles.put("Courier New", FontStyle.MONO);
        styles.put("Ebrima", FontStyle.SANS);
        styles.put("Franklin Gothic Medium", FontStyle.SANS);
        styles.put("Gabriola", FontStyle.CURSIVE);
        styles.put("Gadugi", FontStyle.SANS);
        styles.put("Georgia", FontStyle.SERIF);
        styles.put("HoloLens MDL2 Assets", FontStyle.SANS);
        styles.put("Impact", FontStyle.SANS);
        styles.put("Ink Free", FontStyle.CURSIVE);
        styles.put("Javanese Text", FontStyle.SERIF);
        styles.put("Leelawadee UI", FontStyle.SANS);
        styles.put("Lucida Console", FontStyle.MONO);
        styles.put("Lucida Sans Unicode", FontStyle.SANS);
        styles.put("Malgun Gothic", FontStyle.SANS);
        styles.put("Marlett", FontStyle.SANS);
        styles.put("Microsoft Himalaya", FontStyle.SERIF);
        styles.put("Microsoft JhengHei", FontStyle.SANS);
        styles.put("Microsoft New Tai Lue", FontStyle.SANS);
        styles.put("Microsoft PhagsPa", FontStyle.SANS);
        styles.put("Microsoft Sans Serif", FontStyle.SANS);
        styles.put("Microsoft Tai Le", FontStyle.SANS);
        styles.put("Microsoft YaHei", FontStyle.SANS);
        styles.put("Microsoft Yi Baiti", FontStyle.SANS);
        styles.put("MingLiU-ExtB", FontStyle.SERIF);
        styles.put("Mongolian Baiti", FontStyle.SERIF);
        styles.put("MS Gothic", FontStyle.MONO);
        styles.put("MV Boli", FontStyle.SANS);
        styles.put("Myanmar Text", FontStyle.SANS);
        styles.put("Nirmala UI", FontStyle.SANS);
        styles.put("Palatino Linotype", FontStyle.SERIF);
        styles.put("Segoe MDL2 Assets", FontStyle.SANS);
        styles.put("Segoe Print", FontStyle.SANS);
        styles.put("Segoe Print Bold", FontStyle.SANS);
        styles.put("Segoe Script", FontStyle.CURSIVE);
        styles.put("Segoe Script Bold", FontStyle.CURSIVE);
        styles.put("Segoe UI", FontStyle.SANS);
        styles.put("Segoe UI Historic", FontStyle.CURSIVE);
        styles.put("Segoe UI Emoji", FontStyle.FANTASY);
        styles.put("Segoe UI Symbol", FontStyle.FANTASY);
        styles.put("SimSun", FontStyle.SERIF);
        styles.put("Sitka", FontStyle.SERIF);
        styles.put("Sylfaen", FontStyle.SERIF);
        styles.put("Symbol", FontStyle.SERIF);
        styles.put("Tahoma", FontStyle.SANS);
        styles.put("Times New Roman", FontStyle.SERIF);
        styles.put("Trebuchet MS", FontStyle.SANS);
        styles.put("Verdana", FontStyle.SANS);
        styles.put("Webdings", FontStyle.SANS);
        styles.put("Wingdings", FontStyle.SANS);
        styles.put("Yu Gothic", FontStyle.SANS);
    }
}
