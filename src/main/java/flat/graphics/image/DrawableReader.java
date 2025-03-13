package flat.graphics.image;

import flat.backend.SVG;
import flat.exception.FlatException;
import flat.graphics.context.enums.PixelFormat;
import flat.graphics.image.svg.SvgBuilder;
import flat.graphics.image.svg.SvgRoot;
import flat.resources.ResourceStream;
import flat.uxml.node.UXNodeElement;
import flat.uxml.node.UXNodeParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DrawableReader {

    public static Drawable parse(ResourceStream stream) {
        Object cache = stream.getCache();
        if (cache != null) {
            if (cache instanceof Drawable) {
                return (Drawable) cache;
            } else {
                stream.clearCache();
            }
        }
        if (stream.getResourceName().toLowerCase().endsWith(".svg")) {
            try {
                LineMap lineMap = loadLineMap(stream);
                stream.putCache(lineMap);
                return lineMap;
            } catch (IOException e) {
                throw new FlatException(e);
            }
        } else {
            try {
                PixelMap pixelMap = loadPixelMap(stream);
                stream.putCache(pixelMap);
                return pixelMap;
            } catch (IOException e) {
                throw new FlatException(e);
            }
        }
    }

    public static PixelMap loadPixelMap(ResourceStream stream) throws IOException {
        byte[] data = stream.readData();
        if (stream.getStream() == null || data == null) {
            throw new FlatException("Invalid image" + stream.getResourceName());
        }

        int[] imageData = new int[3];
        byte[] readImage = SVG.ReadImage(data, imageData);
        if (readImage == null) {
            throw new FlatException("Invalid image format " + stream.getResourceName());
        }
        return new PixelMap(readImage, imageData[0], imageData[1], PixelFormat.RGBA);
    }


    public static LineMap loadLineMap(ResourceStream stream) throws IOException {
        byte[] data = stream.readData();
        if (stream.getStream() == null || data == null) {
            throw new FlatException("Invalid image" + stream.getResourceName());
        }

        String xml = new String(data, StandardCharsets.UTF_8);
        UXNodeParser parser = new UXNodeParser(xml);
        parser.parse();

        UXNodeElement root = parser.getRootElement();
        if (root == null) {
            throw new FlatException("Invalid image format " + stream.getResourceName());
        }

        SvgBuilder builder = new SvgBuilder(root);
        SvgRoot svg = builder.build();
        if (svg == null) {
            throw new FlatException("Invalid image format " + stream.getResourceName());
        }

        return new LineMap(svg);
    }
}
