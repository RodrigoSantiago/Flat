package flat.events;

import flat.uxml.Controller;

import java.lang.reflect.Method;

public interface ScrollListener {
    void handle(ScrollEvent event);

    final class AutoScrollListener implements ScrollListener {
        private Controller object;
        private Method method;

        public AutoScrollListener(Controller object, Method method) {
            this.object = object;
            this.method = method;
        }

        @Override
        public void handle(ScrollEvent event) {
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
