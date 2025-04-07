package flat.graphics.symbols;

public enum FontPosture {
    REGULAR, ITALIC; // OBLIQUE

    public static FontPosture parse(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}
