package flat.uxml;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

public class ControllerValueListener<T> implements UXValueListener<T> {
    private WeakReference<Controller> controller;
    private Method method;
    private boolean simple;
    private boolean direct;

    ControllerValueListener(Controller controller, Method method, boolean direct) {
        this.controller = new WeakReference<>(controller);
        this.method = method;
        this.direct = direct;
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
                } else if (direct) {
                    method.invoke(obj, change.getValue());
                } else {
                    method.invoke(obj, change);
                }
            } else if (obj == null) {
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
