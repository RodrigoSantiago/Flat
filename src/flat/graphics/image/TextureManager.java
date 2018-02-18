package flat.graphics.image;

import flat.graphics.context.Texture2D;
import flat.graphics.context.enuns.MagFilter;
import flat.graphics.context.enuns.MinFilter;
import flat.graphics.context.enuns.PixelFormat;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.InputStream;

public class TextureManager {

    public static ImageRaster createImage(InputStream is) throws IOException {
        BufferedImage oimg = ImageIO.read(is);
        BufferedImage pimg = new BufferedImage(oimg.getWidth(), oimg.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g = pimg.createGraphics();
        g.drawImage(oimg, 0, 0, null);
        g.dispose();

        int[] data = ((DataBufferInt) pimg.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < data.length; i++) {
            final int argb = data[i];
            data[i] =
                    ((argb & 0xff) << 16) |
                    (((argb >> 8) & 0xff) << 8) |
                    (((argb >> 16) & 0xff)) |
                    (((argb >> 24) & 0xff) << 24);
        }
        Texture2D texture = new Texture2D();
        texture.begin(0);
        texture.setSize(pimg.getWidth(), pimg.getHeight(), PixelFormat.RGBA);
        texture.setData(0, data, 0, 0, 0, pimg.getWidth(), pimg.getHeight());
        texture.setLevels(0);
        texture.generatMipmapLevels();
        texture.setScaleFilters(MagFilter.NEAREST, MinFilter.NEAREST);
        texture.end();
        return new ImageRaster(texture, 0, 0, pimg.getWidth(), pimg.getHeight());
    }

    public static ImageVector createVector(InputStream is) {
        return null;
    }

    static void disposeImage(ImageRaster image) {

    }
}
