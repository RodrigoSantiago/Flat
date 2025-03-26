package flat.graphics.text;

public enum FontStyle {
    SANS, SERIF, MONO, CURSIVE, FANTASY;

    public static FontStyle parse(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}
