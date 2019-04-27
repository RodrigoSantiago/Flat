package flat.events;

import java.lang.reflect.Method;

public interface DrawListener {
    void handle(DrawEvent event);

    final class AutoDrawListener implements DrawListener {
        private final Object object;
        private final Method method;

        public AutoDrawListener(Object object, Method method) {
            this.object = object;
            this.method = method;
        }

        @Override
        public void handle(DrawEvent event) {
            try {
                method.invoke(object, event);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
