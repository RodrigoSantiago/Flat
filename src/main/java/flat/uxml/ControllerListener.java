package flat.uxml;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

public class ControllerListener<T> implements UXListener<T> {
    private WeakReference<Controller> controller;
    private Method method;
    private boolean simple;

    ControllerListener(Controller controller, Method method) {
        this.controller = new WeakReference<>(controller);
        this.method = method;
        simple = (method.getParameterCount() == 0);
    }

    @Override
    public void handle(T event) {
        if (controller == null) {
            return;
        }

        try {
            var obj = controller.get();
            if (obj != null && obj.isListening()) {
                if (simple) {
                    method.invoke(obj);
                } else {
                    method.invoke(obj, event);
                }
            }  else if (obj == null) {
                controller = null;
                method = null;
            }
        } catch (Exception e) {
            controller = null;
            method = null;
            throw new RuntimeException(e);
        }
    }
}
