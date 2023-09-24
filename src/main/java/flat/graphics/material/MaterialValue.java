package flat.graphics.material;

import java.io.*;

public class MaterialValue implements Serializable {
    public final String name;
    public final Serializable value;

    public MaterialValue(String name, Serializable value) {
        this.name = name;
        this.value = value;
    }

    protected MaterialValue copy() {
        return new MaterialValue(name, copy(value));
    }

    private static Serializable copy(final Serializable obj) {
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        Object copy = null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            out = new ObjectOutputStream(baos);
            out.writeObject(obj);
            out.flush();

            byte data[] = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            in = new ObjectInputStream(bais);
            copy = in.readObject();
        } catch (Exception ignored) {
        } finally {
            if (out != null) try {
                out.close();
            } catch (IOException ignored) {
            }
            if (in != null) try {
                in.close();
            } catch (IOException ignored) {
            }
        }

        return (Serializable) copy;
    }
}
