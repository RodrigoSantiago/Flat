package flat.events;

import java.lang.reflect.Method;

public interface ActionListener {
    void handle(ActionEvent event);

    final class AutoActionListener implements ActionListener {
        private final Object object;
        private final Method method;

        public AutoActionListener(Object object, Method method) {
            this.object = object;
            this.method = method;
        }

        @Override
        public void handle(ActionEvent event) {
            try {
                method.invoke(object, event);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
