package flat.graphics.image.svg;

import java.util.ArrayList;

public class SvgNode {
    private final SvgNode parent;
    private final String id;

    public SvgNode(SvgNode parent, String id) {
        this.parent = parent;
        this.id = id;
    }

    void applyChildren(SvgChildren children) {

    }

    public SvgNode getParent() {
        return parent;
    }

    public String getId() {
        return id;
    }

    void getShapes(ArrayList<SvgShape> allShapes) {

    }
}
