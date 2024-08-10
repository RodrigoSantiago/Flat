package flat.graphics.text;

public enum FontWeight {
    NORMAL(400),
    MEDIUM(500),
    SEMI_BOLD(600),
    BOLD(700),
    LIGHT(300),
    EXTRA_BOLD(800),
    EXTRA_LIGHT(200),
    BLACK(900),
    THIN(100);

    private final int weight;

    FontWeight(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    public static FontWeight parse(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    public static FontWeight parse(int weight) {
        for (var e : values()) {
            if (e.weight == weight) return e;
        }
        return null;
    }
}
