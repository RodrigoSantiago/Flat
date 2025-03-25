package flat.graphics.image;

import flat.exception.FlatException;
import flat.graphics.Graphics;
import flat.graphics.Surface;
import flat.graphics.context.Paint;
import flat.graphics.context.enums.PixelFormat;
import flat.graphics.image.svg.SvgBuilder;
import flat.graphics.image.svg.SvgRoot;
import flat.graphics.image.svg.SvgShape;
import flat.math.Affine;
import flat.math.shapes.Path;
import flat.math.shapes.Rectangle;
import flat.math.shapes.Shape;
import flat.math.shapes.Stroke;
import flat.resources.ResourceStream;
import flat.uxml.node.UXNodeElement;
import flat.uxml.node.UXNodeParser;
import flat.widget.enums.ImageFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class LineMap implements Drawable {

    public static LineMap parse(ResourceStream stream) {
        Object cache = stream.getCache();
        if (cache != null) {
            if (cache instanceof Exception) {
                return null;
            } else if (cache instanceof Drawable) {
                return (LineMap) cache;
            } else {
                stream.clearCache();
            }
        }
        try {
            LineMap lineMap = loadLineMap(stream);
            stream.putCache(lineMap);
            return lineMap;
        } catch (Exception e) {
            stream.putCache(e);
            throw new FlatException(e);
        }
    }

    private static LineMap loadLineMap(ResourceStream stream) throws IOException {
        byte[] data = stream.readData();
        if (data == null) {
            throw new FlatException("Invalid image " + stream.getResourceName());
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

    private final SvgRoot root;
    private final Rectangle view;
    private final boolean needClipping;
    private PixelMap pixelMap;
    private boolean optimize;

    public LineMap(SvgRoot root) {
        this.root = root;
        this.view = root.getView();
        needClipping = !root.getView().contains(root.getBoundingBox());
    }

    public void optimize() {
        optimize = true;
    }

    private void bake(Graphics graphics) {
        float d = getWidth() / getHeight();
        int w, h;
        if (d > 1) {
            w = view.width > 4096 ? 4096 : (int) view.width;
            h = (int) (w / d);
        } else {
            h = view.height > 4096 ? 4096 : (int) view.height;
            w = (int) (h * d);
        }

        var transform = graphics.getTransform2D();

        Surface surface = new Surface(graphics.getContext(), w, h, 8, PixelFormat.RGBA);
        graphics.setSurface(surface);
        graphics.clear(0, 0, 0x0);
        graphics.setTransform2D(null);
        graphics.setAntialiasEnabled(true);
        drawSvg(graphics, 0, 0, w, h, 0xFFFFFFFF, false);
        graphics.setSurface(null);

        pixelMap = surface.createPixelMap();

        graphics.setTransform2D(transform);
    }

    @Override
    public float getWidth() {
        return view.width;
    }

    @Override
    public float getHeight() {
        return view.height;
    }

    @Override
    public void draw(Graphics graphics, float x, float y, float width, float height, int color, ImageFilter filter) {
        if (graphics.discardDraw(x, y, width, height)) return;
        if (root.getAllShapes().isEmpty()) return;

        if (optimize && pixelMap == null) {
            bake(graphics);
        }

        if (pixelMap != null) {
            pixelMap.draw(graphics, x, y, width, height, color, filter);
        } else {
            drawSvg(graphics, x, y, width, height, color, true);
        }
    }

    @Override
    public void draw(Graphics graphics, float x, float y, float frame, ImageFilter filter) {
        drawSvg(graphics, x, y, getWidth(), getHeight(), 0xFFFFFFFF, true);
    }

    private void drawSvg(Graphics graphics, float x, float y, float width, float height, int color, boolean optimize) {
        Affine affine = graphics.getTransform2D();
        Affine base = graphics.getTransform2D()
                .translate(x, y)
                .scale(width / view.width, height / view.height);

        graphics.setTransform2D(base);
        if (needClipping) {
            graphics.pushClip(view);
        }

        Paint paint = graphics.getPaint();
        Stroke stroke = graphics.getStroke();
        for (SvgShape svgPath : root.getAllShapes()) {

            Affine local = svgPath.getTransform();
            if (local != null) {
                graphics.setTransform2D(local.preMul(base));
            }

            Shape shape = svgPath.getShape();

            if (svgPath.getFillPaint() != null) {
                graphics.setPaint(svgPath.getFillPaint().multiply(color));
                if (shape instanceof Path p) {
                    graphics.drawPath(p, true, optimize);
                } else {
                    graphics.drawShape(shape, true);
                }
            }

            if (svgPath.getStrokePaint() != null && svgPath.getStroke() != null) {
                graphics.setPaint(svgPath.getStrokePaint().multiply(color));
                graphics.setStroke(svgPath.getStroke());
                graphics.drawShape(shape, false);
            }

            if (svgPath.getFillPaint() == null && svgPath.getStrokePaint() == null) {
                graphics.setColor(color);
                if (shape instanceof Path p) {
                    graphics.drawPath(p, true, optimize);
                } else {
                    graphics.drawShape(shape, true);
                }
            }

            if (local != null) {
                graphics.setTransform2D(base);
            }
        }
        if (needClipping) {
            graphics.popClip();
        }
        graphics.setTransform2D(affine);
    }
}
