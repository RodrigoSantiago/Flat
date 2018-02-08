package flat.uxml.data;

public class Dimension {
    public enum Size {small, normal, large, xlarge}

    public enum Density {ldpi, mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi}

    public final Size size;
    public final Density density;
    public final float width, height, dpi;

    public Dimension(float width, float height, float dpi) {
        this.width = width;
        this.height = height;
        this.dpi = dpi;
        size = Size.small;
        density = Density.ldpi;
    }

    public static float DP(float px) {
        return px;
    }
}
