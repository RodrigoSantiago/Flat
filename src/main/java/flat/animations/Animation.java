package flat.animations;

public interface Animation {

    // TODO - Animation source must be assigned to the same active that the animation si running

    boolean isPlaying();

    void handle(float milis);

}
