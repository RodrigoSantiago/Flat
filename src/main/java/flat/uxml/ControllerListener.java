package flat.uxml;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

public class ControllerListener<T> implements UXListener<T> {
    private WeakReference<Controller> controller;
    private Method method;

    ControllerListener(Controller controller, Method method) {
        this.controller = new WeakReference<>(controller);
        this.method = method;
    }

    @Override
    public void handle(T event) {
        if (controller == null) {
            return;
        }

        try {
            var obj = controller.get();
            if (obj != null && obj.isListening()) {
                method.invoke(obj, event);
            } else {
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
