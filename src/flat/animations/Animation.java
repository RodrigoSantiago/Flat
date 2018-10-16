package flat.animations;

public interface Animation {

    boolean isPlaying();

    void handle(long milis);

}
