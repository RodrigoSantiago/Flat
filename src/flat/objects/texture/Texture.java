package flat.objects.texture;

import flat.image.ImageFormat;
import flat.objects.ContextObject;
import flat.screen.Context;

public class Texture extends ContextObject {
    private int width, height, page;
    private ImageFormat format;

    public Texture(Context context) {
        super(context);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    public void uploadData(byte[] data) {

    }

    public void uploadData(int[] data) {

    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getPageCount() {
        return page;
    }

    public ImageFormat getFormat() {
        return format;
    }

}
