package flat.resources;

import flat.graphics.image.Drawable;

public interface Resource {

    String path();
    Object getCache();
    void putCache(Object object);

    Drawable getDrawable();
}
