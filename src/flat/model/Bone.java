package flat.model;

import flat.math.Matrix4;

public class Bone {
    Bone parent;
    Bone[] children;

    Matrix4 defaultTransform;
}
