package flat.events;

import flat.uxml.Controller;

import java.lang.reflect.Method;

public interface HoverListener {
    void handle(HoverEvent event);

    final class AutoHoverListener implements HoverListener {
        private Controller object;
        private Method method;

        public AutoHoverListener(Controller object, Method method) {
            this.object = object;
            this.method = method;
        }

        @Override
        public void handle(HoverEvent event) {
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
