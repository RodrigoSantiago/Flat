package flat.model;

import flat.math.Matrix4;

public class ModelAnim {

    String name;
    Matrix4 scale;
    Skeleton skeleton;
    KeyFrame[] keyFrames;

    public Matrix4 getBonePosition(float time, int boneIndex) {
        return null;
    }
}
