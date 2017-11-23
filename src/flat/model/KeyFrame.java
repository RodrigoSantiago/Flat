package flat.model;

import flat.math.Matrix4;

public class KeyFrame {
    final float timeline;
    final Matrix4[] transforms;

    public KeyFrame(float timeline, Matrix4[] transforms) {
        this.timeline = timeline;
        this.transforms = transforms;
    }
}
