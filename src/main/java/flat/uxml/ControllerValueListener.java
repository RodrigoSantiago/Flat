package flat.uxml;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

public class ControllerValueListener<T> implements UXValueListener<T> {
    private WeakReference<Controller> controller;
    private Method method;
    private boolean simple;

    ControllerValueListener(Controller controller, Method method) {
        this.controller = new WeakReference<>(controller);
        this.method = method;
        this.simple = method.getParameterCount() == 0;
    }

    @Override
    public void handle(ValueChange<T> change) {
        if (controller == null) {
            return;
        }

        try {
            var obj = controller.get();
            if (obj != null && obj.isListening()) {
                if (simple) {
                    method.invoke(obj);
                } else {
                    method.invoke(obj, change);
                }
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
