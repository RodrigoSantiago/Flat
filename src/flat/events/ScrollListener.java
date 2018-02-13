package flat.events;

import java.lang.reflect.Method;

public interface ScrollListener {
    void handle(ScrollEvent event);

    final class AutoScrollListener implements ScrollListener {
        private final Object object;
        private final Method method;

        public AutoScrollListener(Object object, Method method) {
            this.object = object;
            this.method = method;
        }

        @Override
        public void handle(ScrollEvent event) {
            try {
                method.invoke(object, event);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
