package flat.graphics.image;

import flat.resources.ResourceStream;

public class DrawableReader {
    public static Drawable parse(ResourceStream stream) {
        Object cache = stream.getCache();
        if (cache != null) {
            if (cache instanceof Drawable) {
                return (Drawable) cache;
            } else {
                stream.clearCache();
            }
        }
        if (stream.getResourceName().toLowerCase().endsWith(".svg")) {
            return LineMap.parse(stream);
        } else {
            return PixelMap.parse(stream);
        }
    }
}
