package flat.animations;

import flat.window.Activity;

public interface Animation {

    Activity getSource();

    boolean isPlaying();

    void handle(float milis);

}
