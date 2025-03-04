package flat.graphics.image;

import flat.graphics.Graphics;
import flat.widget.enums.ImageFilter;

public interface Drawable {

    boolean isDynamic();

    float getWidth();

    float getHeight();

    void draw(Graphics context, float x, float y, float width, float height, int color, ImageFilter filter);

    void draw(Graphics context, float x, float y, float frame, ImageFilter filter);

}
