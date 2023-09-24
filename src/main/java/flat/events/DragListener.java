package flat.events;

import flat.uxml.Controller;

import java.lang.reflect.Method;

public interface DragListener {
    void handle(DragEvent event);

    final class AutoDragListener implements DragListener {
        private Controller object;
        private Method method;

        public AutoDragListener(Controller object, Method method) {
            this.object = object;
            this.method = method;
        }

        @Override
        public void handle(DragEvent event) {
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
