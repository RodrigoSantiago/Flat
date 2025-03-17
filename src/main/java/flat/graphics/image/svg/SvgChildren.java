package flat.graphics.image.svg;

import flat.uxml.node.UXNodeAttribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class SvgChildren implements Iterable<SvgChild> {

    private final ArrayList<SvgChild> children = new ArrayList<>();

    public void add(SvgNode child, HashMap<String, UXNodeAttribute> attributes) {
        children.add(new SvgChild(child, attributes));
    }

    public int getChildrenCount() {
        return children.size();
    }

    @Override
    public Iterator<SvgChild> iterator() {
        return new SvgChildIterator();
    }

    private class SvgChildIterator implements Iterator<SvgChild> {
        private int pos;

        @Override
        public boolean hasNext() {
            return pos < children.size();
        }

        @Override
        public SvgChild next() {
            return pos >= children.size() ? null : children.get(pos++);
        }
    }
}

