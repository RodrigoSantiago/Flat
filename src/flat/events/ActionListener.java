package flat.events;

import flat.uxml.Controller;

import java.lang.reflect.Method;

public interface ActionListener {
    void handle(ActionEvent event);

    final class AutoActionListener implements ActionListener {
        private Controller object;
        private Method method;

        public AutoActionListener(Controller object, Method method) {
            this.object = object;
            this.method = method;
        }

        @Override
        public void handle(ActionEvent event) {
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
