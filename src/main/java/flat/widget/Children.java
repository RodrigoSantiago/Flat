package flat.widget;

import java.util.Iterator;
import java.util.List;

public class Children<T extends Widget> implements Iterable<T> {

    private final List<T> children;
    private final boolean reverse;

    public Children(List<T> children) {
        this(children, false);
    }

    public Children(List<T> children, boolean reverse) {
        this.children = children;
        this.reverse = reverse;
    }

    public int size() {
        return children.size();
    }

    public Widget get(int pos) {
        return children.get(pos);
    }

    @Override
    public Iterator<T> iterator() {
        return new ChildrenIterator();
    }

    private class ChildrenIterator implements Iterator<T> {

        private int pos;

        ChildrenIterator() {
            if (reverse && children != null) {
                pos = children.size() - 1;
            }
        }

        @Override
        public boolean hasNext() {
            if (children == null) {
                return false;
            }
            return reverse ? pos >= 0 && children.size() > 0 : pos < children.size();
        }

        @Override
        public T next() {
            if (reverse) {
                if (pos >= children.size()) {
                    pos = children.size() - 1;
                }
                return children.get(pos--);
            } else {
                return children.get(pos++);
            }
        }
    }

}
