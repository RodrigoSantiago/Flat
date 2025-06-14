package flat.uxml;

import flat.Flat;
import flat.events.KeyEvent;
import flat.graphics.Graphics;
import flat.window.Activity;
import flat.window.Application;
import flat.window.Window;

import java.lang.reflect.*;

public class Controller {

    private Activity activity;
    private boolean listening;
    private boolean firstLoad;

    public final void setActivity(Activity activity) {
        if (this.activity != activity) {
            Activity old = this.activity;
            if (old == null) {
                this.activity = activity;
                if (!firstLoad) {
                    firstLoad = true;
                    try {
                        onLoad();
                    } catch (Exception e) {
                        Application.handleException(e);
                    }
                }
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
                this.activity = null;
            } else {
                this.activity = activity;
            }
        }
    }

    public Activity getActivity() {
        return activity;
    }

    public Window getWindow() {
        return activity != null ? activity.getWindow() : null;
    }

    public Graphics getGraphics() {
        return activity != null ? activity.getContext().getGraphics() : null;
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

    public void onLoad() {

    }

    public void onShow() {

    }

    public void onDraw(Graphics graphics) {

    }

    public void onHide() {

    }

    public void onKey(KeyEvent keyEvent) {

    }

    public void onKeyFilter(KeyEvent keyEvent) {

    }

    public boolean onCloseRequest(boolean systemRequest) {

        return true;
    }

}
