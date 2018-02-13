package flat.events;

import java.lang.reflect.Method;

public interface PointerListener {
    void handle(PointerEvent event);

    final class AutoPointerListener implements PointerListener {
        private final Object object;
        private final Method method;

        public AutoPointerListener(Object object, Method method) {
            this.object = object;
            this.method = method;
        }

        @Override
        public void handle(PointerEvent event) {
            try {
                method.invoke(object, event);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
