package flat.graphics.image;

import flat.graphics.SmartContext;
import flat.widget.enums.ImageFilter;

public interface Drawable {

    boolean isDynamic();

    float getWidth();

    float getHeight();

    void draw(SmartContext context, float x, float y, float width, float height, float frame, ImageFilter filter);

    void draw(SmartContext context, float x, float y, float frame, ImageFilter filter);

}
