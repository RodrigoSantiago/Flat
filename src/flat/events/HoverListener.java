package flat.events;

import java.lang.reflect.Method;

public interface HoverListener {
    void handle(HoverEvent event);

    final class AutoHoverListener implements HoverListener {
        private final Object object;
        private final Method method;

        public AutoHoverListener(Object object, Method method) {
            this.object = object;
            this.method = method;
        }

        @Override
        public void handle(HoverEvent event) {
            try {
                method.invoke(object, event);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
