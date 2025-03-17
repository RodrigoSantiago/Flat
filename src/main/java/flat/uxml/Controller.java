package flat.uxml;

import flat.Flat;
import flat.graphics.Graphics;
import flat.window.Activity;
import flat.window.Application;

import java.lang.reflect.*;

public class Controller {

    private Activity activity;
    private boolean listening;

    public final void setActivity(Activity activity) {
        if (this.activity != activity) {
            Activity old = this.activity;
            this.activity = activity;
            if (old == null) {
                try {
                    onShow();
                } catch (Exception e) {
                    Application.handleException(e);
                }
            } else if (activity == null) {
                try {
                    onHide();
                } catch (Exception e) {
                    Application.handleException(e);
                }
            }
        }
    }

    public Activity getActivity() {
        return activity;
    }

    private Method findMethod(String name, Class<?> argument) {
        try {
            Method method = argument == null ? getClass().getMethod(name) : getClass().getMethod(name, argument);
            method.setAccessible(true);
            if (method.isAnnotationPresent(Flat.class)
                    && Modifier.isPublic(method.getModifiers())
                    && !Modifier.isStatic(method.getModifiers())) {
                return method;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public <T> UXListener<T> getListenerMethod(String name, Class<T> argument) {
        Method method = findMethod(name, argument);
        if (method == null) {
            method = findMethod(name, null);
        }
        if (method != null) {
            return new ControllerListener<>(this, method);
        }
        return null;
    }

    public <T> UXValueListener<T> getValueListenerMethod(String name, Class<T> argument) {
        try {
            Method method = getClass().getMethod(name, ValueChange.class);
            method.setAccessible(true);
            if (method.isAnnotationPresent(Flat.class)
                    && Modifier.isPublic(method.getModifiers())
                    && !Modifier.isStatic(method.getModifiers())) {

                Type[] genParamTypes = method.getGenericParameterTypes();
                if (genParamTypes.length == 1 && genParamTypes[0] instanceof ParameterizedType paramType) {
                    Type actualTypeArgument = paramType.getActualTypeArguments()[0];

                    if (actualTypeArgument.equals(argument)) {
                        return new ControllerValueListener<>(this, method);
                    }
                }
            }
            method = findMethod(name, null);
            if (method != null) {
                return new ControllerValueListener<>(this, method);
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
        return activity != null;
    }

    public void onShow() {

    }

    public void onDraw(Graphics graphics) {

    }

    public void onHide() {

    }

    public boolean onCloseRequest(boolean systemRequest) {

        return true;
    }

}
