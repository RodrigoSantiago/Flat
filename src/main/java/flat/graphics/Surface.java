package flat.graphics;

import flat.graphics.context.Context;
import flat.graphics.context.Frame;
import flat.graphics.context.Render;
import flat.graphics.context.Texture2D;
import flat.graphics.context.enuns.PixelFormat;

public class Surface {
    Frame frame;
    Texture2D texture;
    Render render;

    public Surface() {

    }

    public void setTexture(Texture2D texture) {
        this.texture = texture;
    }

    public Texture2D getTexture() {
        return texture;
    }

    void bind(Context context) {
        if (frame == null) {
            frame = new Frame(context);
        }
        frame.begin();

        frame.attach(0, texture);

        if (render == null) {
            render = new Render();
            render.begin();
            render.setSize(texture.getWidth(), texture.getHeight(), PixelFormat.DEPTH24_STENCIL8);
            render.end();
        } else if (render.getWidth() != texture.getWidth() || render.getHeight() != texture.getHeight()) {
            render.begin();
            render.setSize(texture.getWidth(), texture.getHeight(), PixelFormat.DEPTH24_STENCIL8);
            render.end();
        }
        frame.attach(Frame.DEPTH_STENCIL, render);
    }

    void unbind() {
        frame.end();
    }
}
