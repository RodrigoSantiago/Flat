package flat.uxml;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

public class ControllerListener<T> implements UXListener<T> {
    private WeakReference<Controller> controller;
    private Method method;
    private boolean simple;
    private String extra;

    ControllerListener(Controller controller, Method method, String extra) {
        this.controller = new WeakReference<>(controller);
        this.method = method;
        this.extra = extra;
        simple = extra == null ? (method.getParameterCount() == 0) : (method.getParameterCount() == 1);
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
                    if (extra != null) {
                        method.invoke(obj, extra);
                    } else {
                        method.invoke(obj);
                    }
                } else {
                    if (extra != null) {
                        method.invoke(obj, event, extra);
                    } else {
                        method.invoke(obj, event);
                    }
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
