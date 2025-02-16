package flat.uxml;

import flat.Flat;
import flat.graphics.SmartContext;
import flat.window.Activity;

import java.lang.reflect.*;

public class Controller {

    private Activity activity;

    public Controller(Activity activity) {
        this.activity = activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Activity getActivity() {
        return activity;
    }

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

    public void onShow() {

    }

    public void onDraw(SmartContext context) {

    }

    public void onHide() {

    }

    public boolean onCloseRequest(boolean systemRequest) {

        return true;
    }

}
