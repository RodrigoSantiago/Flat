package flat.uxml;

import flat.Flat;
import org.tinylog.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Controller {
    public void assign(String name, Object object) {
        try {
            Field field = getClass().getField(name);
            field.setAccessible(true);
            if (field.isAnnotationPresent(Flat.class)
                    && !Modifier.isPrivate(field.getModifiers())
                    && !Modifier.isStatic(field.getModifiers())
                    && !Modifier.isFinal(field.getModifiers())) {
                field.set(this, object);
            }
        } catch (NoSuchFieldException e) {
            Logger.info(e);

        } catch (IllegalAccessException e) {
            Logger.error(e);

        }
    }

    public boolean isListening() {
        return true;
    }
}
