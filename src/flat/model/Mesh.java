package flat.model;

public class Mesh {
    public final float[] vertex;
    public final int[] index;

    public Mesh(float[] vertex, int[] index) {
        this.vertex = vertex;
        this.index = index;
    }
}
