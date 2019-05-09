package flat.events;

import flat.uxml.Controller;

import java.lang.reflect.Method;

public interface PointerListener {
    void handle(PointerEvent event);

    final class AutoPointerListener implements PointerListener {
        private Controller object;
        private Method method;

        public AutoPointerListener(Controller object, Method method) {
            this.object = object;
            this.method = method;
        }

        @Override
        public void handle(PointerEvent event) {
            try {
                if (object != null) {
                    if (object.isListening()) {
                        method.invoke(object, event);
                    } else {
                        object = null;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
