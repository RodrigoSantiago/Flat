package flat.events;

import java.lang.reflect.Method;

public interface FocusListener {
    void handle(FocusEvent event);

    final class AutoFocusListener implements FocusListener {
        private final Object object;
        private final Method method;

        public AutoFocusListener(Object object, Method method) {
            this.object = object;
            this.method = method;
        }

        @Override
        public void handle(FocusEvent event) {
            try {
                method.invoke(object, event);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
