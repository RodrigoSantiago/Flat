package flat.model;

import flat.math.Matrix4;

public class Bone {
    final String name;
    final Bone[] children;
    final Matrix4 defaultTransform;

    public Bone(String name, Bone[] children, Matrix4 defaultTransform) {
        this.name = name;
        this.children = children;
        this.defaultTransform = defaultTransform;
    }
}
