package flat.graphics;

import flat.graphics.context.*;
import flat.graphics.context.enums.BlitMask;
import flat.graphics.context.enums.LayerTarget;
import flat.graphics.context.enums.MagFilter;
import flat.graphics.context.enums.PixelFormat;
import flat.graphics.image.PixelMap;

public class Surface {
    private final int width;
    private final int height;
    private final int multiSamples;
    private final PixelFormat format;
    private Context context;

    FrameBuffer frameBuffer;
    FrameBuffer frameBufferTransfer;
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
        if (frameBuffer == null) {
            return null;
        }

        byte[] imageData = new byte[width * height * format.getPixelBytes()];

        if (multiSamples > 0) {
            if (frameBufferTransfer == null) {
                frameBufferTransfer = new FrameBuffer(context);
                if (texture == null) {
                    texture = new Texture2D(context, width, height, format);
                }
                frameBufferTransfer.attach(LayerTarget.COLOR_0, texture, 0);
            }
            context.blitFrames(frameBuffer, frameBufferTransfer,
                    0, 0, width, height,
                    0, 0, width, height, BlitMask.Color, MagFilter.LINEAR);
        }

        texture.getData(0, imageData, 0);
        int pixelBytes = PixelFormat.RGBA.getPixelBytes();
        int rowSize = width * pixelBytes;
        byte[] tempRow = new byte[rowSize];
        for (int y = 0; y < height / 2; y++) {
            int topRowStart = y * rowSize;
            int bottomRowStart = (height - y - 1) * rowSize;
            System.arraycopy(imageData, topRowStart, tempRow, 0, rowSize);
            System.arraycopy(imageData, bottomRowStart, imageData, topRowStart, rowSize);
            System.arraycopy(tempRow, 0, imageData, bottomRowStart, rowSize);
        }
        return new PixelMap(imageData, width, height, PixelFormat.RGBA);
    }

    void begin(Context context) {
        if (frameBuffer == null) {
            frameBuffer = new FrameBuffer(context);
        }
        if (multiSamples > 0 && textureMultisamples == null) {
            textureMultisamples = new TextureMultisample2D(context, width, height, multiSamples, format);
        }
        if (multiSamples <= 0 && texture == null) {
            texture = new Texture2D(context, width, height, format);
        }
        if (render == null) {
            render = new Render(context, width, height, multiSamples, PixelFormat.DEPTH24_STENCIL8);
        }

        context.setFrameBuffer(frameBuffer);
        if (multiSamples > 0) {
            frameBuffer.attach(LayerTarget.COLOR_0, textureMultisamples, 0);
        } else {
            frameBuffer.attach(LayerTarget.COLOR_0, texture, 0);
        }
        frameBuffer.attach(LayerTarget.DEPTH_STENCIL, render);
    }
}
