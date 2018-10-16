package flat.animations;

public interface StateInfo {
    int ENABLED = 0;
    int FOCUSED = 1;
    int ACTIVATED = 2;
    int HOVERED = 3;
    int PRESSED = 4;
    int DRAGGED = 5;
    int ERROR = 6;
    int DISABLED = 7;

    float get(int index);
}
