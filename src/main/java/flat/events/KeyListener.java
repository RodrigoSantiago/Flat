package flat.events;

import flat.uxml.Controller;

import java.lang.reflect.Method;

public interface KeyListener {
    void handle(KeyEvent event);

    final class AutoKeyListener implements KeyListener {
        private Controller object;
        private Method method;

        public AutoKeyListener(Controller object, Method method) {
            this.object = object;
            this.method = method;
        }

        @Override
        public void handle(KeyEvent event) {
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
