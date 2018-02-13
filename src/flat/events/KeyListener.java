package flat.events;

import java.lang.reflect.Method;

public interface KeyListener {
    void handle(KeyEvent event);

    final class AutoKeyListener implements KeyListener {
        private final Object object;
        private final Method method;

        public AutoKeyListener(Object object, Method method) {
            this.object = object;
            this.method = method;
        }

        @Override
        public void handle(KeyEvent event) {
            try {
                method.invoke(object, event);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
