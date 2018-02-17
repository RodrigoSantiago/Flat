package flat.graphics.image;

import flat.graphics.SmartContext;

public interface Image {

    boolean isDynamic();

    float getWidth();

    float getHeight();

    void draw(SmartContext context, float x, float y, float width, float height, float frame);

}
