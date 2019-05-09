package flat.events;

import flat.uxml.Controller;

import java.lang.reflect.Method;

public interface DrawListener {
    void handle(DrawEvent event);

    final class AutoDrawListener implements DrawListener {
        private Controller object;
        private Method method;

        public AutoDrawListener(Controller object, Method method) {
            this.object = object;
            this.method = method;
        }

        @Override
        public void handle(DrawEvent event) {
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
