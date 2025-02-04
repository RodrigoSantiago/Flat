package flat.uxml;

import flat.Flat;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Controller {

    public <T> UXListener<T> getListenerMethod(String name, Class<T> argument) {
        try {
            Method method = getClass().getMethod(name, argument);
            method.setAccessible(true);
            if (method.isAnnotationPresent(Flat.class)
                    && Modifier.isPublic(method.getModifiers())
                    && !Modifier.isStatic(method.getModifiers())) {
                return new ControllerListener<>(this, method);
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    public void assign(String name, Object object) {
        try {
            Field field = getClass().getDeclaredField(name);
            field.setAccessible(true);
            if (field.isAnnotationPresent(Flat.class)
                    && !Modifier.isStatic(field.getModifiers())
                    && !Modifier.isFinal(field.getModifiers())) {
                field.set(this, object);
            }
        } catch (Exception ignored) {

        }
    }

    public boolean isListening() {
        return true;
    }

    private static class ControllerListener<T> implements UXListener<T> {
        private WeakReference<Controller> controller;
        private Method method;

        public ControllerListener(Controller controller, Method method) {
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
}
