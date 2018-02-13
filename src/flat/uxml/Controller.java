package flat.uxml;

import flat.Flat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

public class Controller {
    private HashMap<String, Field> flatFields;

    public void assign(String name, Object object) {
        if (flatFields == null) {
            flatFields = new HashMap<>();
            Field[] fields = getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(Flat.class)
                        && !Modifier.isPrivate(field.getModifiers())
                        && !Modifier.isStatic(field.getModifiers())
                        && !Modifier.isFinal(field.getModifiers())) {
                    flatFields.put(field.getName(), field);
                }
            }
        }
        Field field = flatFields.get(name);
        if (field != null) {
            try {
                field.set(this, object);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
