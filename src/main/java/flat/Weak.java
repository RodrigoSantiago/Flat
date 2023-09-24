package flat;

import java.lang.ref.WeakReference;

public class Weak<T> extends WeakReference<T> {

    public Weak(T referent) {
        super(referent);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj instanceof Weak) {
            return get() == ((Weak) obj).get();
        } else {
            return get() == obj;
        }
    }
}
