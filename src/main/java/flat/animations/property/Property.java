package flat.animations.property;

public class Property<T> {

    public final Function<T> function;
    public final T start;
    public final T end;

    public Property(Function<T> function, T start, T end) {
        this.function = function;
        this.start = start;
        this.end = end;
    }

    public interface Function<T> {
        void run(T obj);
    }
}
