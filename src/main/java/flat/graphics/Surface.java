package flat.graphics;

import flat.backend.GL;
import flat.backend.GLEnums;
import flat.graphics.context.*;
import flat.graphics.context.enums.BlitMask;
import flat.graphics.context.enums.MagFilter;
import flat.graphics.context.enums.PixelFormat;
import flat.graphics.image.PixelMap;

public class Surface {
    private final int width;
    private final int height;
    private final int multiSamples;
    private final PixelFormat format;
    private Context context;

    Frame frame;
    Frame frameTransfer;
    Render render;
    TextureMultisample2D textureMultisamples;
    Texture2D texture;
    boolean disposed;

    public Surface(Context context, int width, int height) {
        this(context, width, height, 8);
    }

    public Surface(Context context, int width, int height, int multiSamples) {
        this(context, width, height, multiSamples, PixelFormat.RGBA);
    }

    public Surface(Context context, int width, int height, int multiSamples, PixelFormat format) {
        this.context = context;
        this.width = width;
        this.height = height;
        this.multiSamples = multiSamples;
        this.format = format;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getMultiSamples() {
        return multiSamples;
    }

    public PixelFormat getFormat() {
        return format;
    }

    public PixelMap createPixelMap() {
        if (frame == null) {
            return null;
        }

        byte[] imageData = new byte[width * height * format.getPixelBytes()];

        if (multiSamples > 0) {
            if (frameTransfer == null) {
                frameTransfer = new Frame(context);
                if (texture == null) {
                    texture = new Texture2D(context);
                    texture.begin(0);
                    texture.setSize(width, height, format);
                    texture.end();
                }
                Frame before = context.getBoundFrame();
                frameTransfer.begin();
                frameTransfer.attach(0, texture);
                frameTransfer.end();
                if (before != null) {
                    before.begin();
                }
            }
            context.blitFrameNow(frame, frameTransfer,
                    0, 0, width, height,
                    0, 0, width, height, BlitMask.Color, MagFilter.LINEAR);
        }

        texture.begin(0);
        texture.getData(0, imageData, 0);
        texture.end();
        return new PixelMap(imageData, width, height, PixelFormat.RGBA);
    }

    void bind(Context context) {
        if (frame == null) {
            frame = new Frame(context);
        }

        if (multiSamples > 0 && textureMultisamples == null) {
            textureMultisamples = new TextureMultisample2D(context);
            textureMultisamples.begin(0);
            textureMultisamples.setSize(width, height, multiSamples, format);
            textureMultisamples.end();
        }
        if (multiSamples <= 0 && texture == null) {
            texture = new Texture2D(context);
            texture.begin(0);
            texture.setSize(width, height, format);
            texture.end();
        }
        if (render == null) {
            render = new Render(context);
            render.begin();
            render.setSize(width, height, multiSamples, PixelFormat.DEPTH24_STENCIL8);
            render.end();
        }

        frame.begin();
        if (multiSamples > 0) {
            frame.attach(0, textureMultisamples);
        } else {
            frame.attach(0, texture);
        }
        frame.attach(Frame.DEPTH_STENCIL, render);
    }

    void unbind() {
        frame.end();
    }
}
