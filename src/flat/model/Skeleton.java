package flat.model;

public class Skeleton {
    final Bone root;
    final Bone[] indexedList;

    public Skeleton(Bone root, Bone[] indexedList) {
        this.root = root;
        this.indexedList = indexedList;
    }
}
