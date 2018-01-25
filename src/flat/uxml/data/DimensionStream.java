package flat.uxml.data;

import java.io.InputStream;

public class DimensionStream {

    public final String name;

    public DimensionStream(String name) {
        this.name = name;
    }

    public InputStream getStream(Dimension dimension) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    }

    public boolean isCompatible(Dimension a, Dimension b) {
        return false;
    }
}
