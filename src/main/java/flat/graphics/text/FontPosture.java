package flat.graphics.text;

public enum FontPosture {
    ITALIC, REGULAR;

    public static FontPosture parse(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}
