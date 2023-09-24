package flat.events;

import flat.uxml.Controller;

import java.lang.reflect.Method;

public interface FocusListener {
    void handle(FocusEvent event);

    final class AutoFocusListener implements FocusListener {
        private Controller object;
        private Method method;

        public AutoFocusListener(Controller object, Method method) {
            this.object = object;
            this.method = method;
        }

        @Override
        public void handle(FocusEvent event) {
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
