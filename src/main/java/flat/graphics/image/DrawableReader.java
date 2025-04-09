package flat.graphics.image;

import flat.resources.ResourceStream;

public class DrawableReader {
    public static Drawable parse(ResourceStream stream) {
        return parse(stream, true);
    }

    public static Drawable parse(ResourceStream stream, boolean suppressException) {
        Object cache = stream.getCache();
        if (cache != null) {
            if (cache instanceof Exception) {
                return null;
            } else if (cache instanceof Drawable) {
                return (Drawable) cache;
            } else {
                stream.clearCache();
            }
        }
        if (stream.getResourceName().toLowerCase().endsWith(".svg")) {
            if (suppressException) {
                try {
                    return LineMap.parse(stream);
                } catch (Exception e) {
                    return null;
                }
            }
            return LineMap.parse(stream);
        } else {
            if (suppressException) {
                try {
                    return PixelMap.parse(stream);
                } catch (Exception e) {
                    return null;
                }
            }
            return PixelMap.parse(stream);
        }
    }
}
