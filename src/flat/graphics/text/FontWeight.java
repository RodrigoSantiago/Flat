package flat.graphics.text;

public enum FontWeight {
    BLACK(900),
    EXTRA_BOLD(800),
    BOLD(700),
    SEMI_BOLD(600),
    MEDIUM(500),
    NORMAL(400),
    LIGHT(300),
    EXTRA_LIGHT(200),
    THIN(100);

    private final int weight;

    FontWeight(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}
