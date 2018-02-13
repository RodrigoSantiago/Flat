package flat.events;

import java.lang.reflect.Method;

public interface DragListener {
    void handle(DragEvent event);

    final class AutoDragListener implements DragListener {
        private final Object object;
        private final Method method;

        public AutoDragListener(Object object, Method method) {
            this.object = object;
            this.method = method;
        }

        @Override
        public void handle(DragEvent event) {
            try {
                method.invoke(object, event);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
