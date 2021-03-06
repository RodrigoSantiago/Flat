package flat.graphics.image;

import flat.graphics.SmartContext;

public interface Drawable {

    // TODO - ADD Default DPI for scaling system

    boolean isDynamic();

    float getWidth();

    float getHeight();

    void draw(SmartContext context, float x, float y, float width, float height, float frame);

    void draw(SmartContext context, float x, float y, float frame);

}
