package flat.graphics.image.svg;

import flat.uxml.node.UXNodeAttribute;

import java.util.HashMap;

public class SvgChild {
    private final SvgNode child;
    private final HashMap<String, UXNodeAttribute> attributes;

    public SvgChild(SvgNode child, HashMap<String, UXNodeAttribute> attributes) {
        this.child = child;
        this.attributes = attributes;
    }

    public SvgNode getChild() {
        return child;
    }

    public HashMap<String, UXNodeAttribute> getAttributes() {
        return attributes;
    }
}
