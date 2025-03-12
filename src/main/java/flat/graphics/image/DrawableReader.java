package flat.graphics.image;

import flat.backend.SVG;
import flat.exception.FlatException;
import flat.graphics.context.Paint;
import flat.graphics.context.enums.LineCap;
import flat.graphics.context.enums.LineJoin;
import flat.graphics.context.enums.PixelFormat;
import flat.graphics.context.paints.ColorPaint;
import flat.math.shapes.Path;
import flat.math.shapes.Rectangle;
import flat.math.shapes.Shape;
import flat.math.shapes.Stroke;
import flat.math.stroke.BasicStroke;
import flat.resources.Parser;
import flat.resources.ResourceStream;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

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
                LineMap lineMap = loadLineMap(stream.getStream());
                stream.putCache(lineMap);
                return lineMap;
            } catch (Exception e) {
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
        if (stream.getStream() == null) {
            throw new FlatException("Invalid image format " + stream.getResourceName());
        }
        byte[] data = stream.readData();
        if (data == null) {
            throw new FlatException("Invalid image format " + stream.getResourceName());
        }
        int[] imageData = new int[3];
        byte[] readImage = SVG.ReadImage(data, imageData);
        if (readImage == null) {
            throw new FlatException("Invalid image format " + stream.getResourceName());
        }
        return new PixelMap(readImage, imageData[0], imageData[1], PixelFormat.RGBA);
    }


    public static LineMap loadLineMap(InputStream is) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(is);
        doc.getDocumentElement().normalize();

        Rectangle view = null;
        Rectangle buildView = null;
        ArrayList<LineMap.SVGPath> svgPaths = new ArrayList<>();

        NodeList nList = doc.getChildNodes();
        for (int i = 0; i < nList.getLength(); i++) {
            Node node = nList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getNodeName().equals("svg")) {
                    NamedNodeMap map = node.getAttributes();
                    Node vNode = map.getNamedItem("viewBox");
                    if (vNode != null) {
                        String[] vals = vNode.getNodeValue().split(" ");
                        view = new Rectangle(
                                Float.parseFloat(vals[0]), Float.parseFloat(vals[1]),
                                Float.parseFloat(vals[2]), Float.parseFloat(vals[3]));
                    } else {
                        buildView = new Rectangle(0, 0, 0, 0);
                    }

                    NodeList cList = node.getChildNodes();
                    for (int j = 0; j < cList.getLength(); j++) {
                        Node child = cList.item(j);
                        if (child.getNodeType() == Node.ELEMENT_NODE) {
                            if (child.getNodeName().equals("path")) {
                                LineMap.SVGPath svgPath = loadPath(child);
                                if (svgPath != null) {
                                    svgPaths.add(svgPath);
                                    if (buildView != null) {
                                        buildView.add(svgPath.shape.bounds());
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }

        return new LineMap(buildView == null ? view : buildView, svgPaths.toArray(new LineMap.SVGPath[0]));
    }

    private static LineMap.SVGPath loadPath(Node node) {
        String id = null;
        Shape shape = null;
        Paint fillPaint = null;
        Paint strokePaint = null;
        Stroke stroke = null;

        NamedNodeMap map = node.getAttributes();
        Node dNode = map.getNamedItem("d");
        if (dNode != null) {
            Path path = Parser.svg(dNode.getNodeValue(), 0);
            path.optimize();
            shape = path;
        } else {
            return null;
        }
        Node idNode = map.getNamedItem("id");
        if (idNode != null) {
            id = idNode.getNodeValue();
        }
        Node fillNode = map.getNamedItem("fill");
        if (fillNode != null) {
            fillPaint = new ColorPaint(Parser.color(fillNode.getNodeValue()));
        }
        Node strokeNode = map.getNamedItem("stroke");
        if (strokeNode != null) {
            strokePaint = new ColorPaint(Parser.color(strokeNode.getNodeValue()));
        }
        Node strokeWidth = map.getNamedItem("stroke-width");
        Node strokeCap = map.getNamedItem("stroke-cap");
        Node strokeJoin = map.getNamedItem("stroke-linejoin");
        Node strokeMitter = map.getNamedItem("stroke-miterlimit");
        if (strokeWidth != null) {
            float w = 1, m = 10;
            int c = LineCap.BUTT.ordinal(), j = LineJoin.BEVEL.ordinal();

            w = Float.parseFloat(strokeWidth.getNodeValue());
            if (strokeMitter != null) {
                m = Float.parseFloat(strokeMitter.getNodeValue());
            }
            if (strokeCap != null) {
                switch (strokeCap.getNodeValue()) {
                    case "round":
                        c = LineCap.ROUND.ordinal();
                        break;
                    case "square":
                        c = LineCap.SQUARE.ordinal();
                        break;
                    default:
                        c = LineCap.BUTT.ordinal();
                }
            }
            if (strokeJoin != null) {
                switch (strokeJoin.getNodeValue()) {
                    case "arcs":
                    case "mitter":
                    case "mitter-clip":
                        j = LineJoin.MITER.ordinal();
                        break;
                    case "round":
                        j = LineJoin.ROUND.ordinal();
                        break;
                    default:
                        j = LineJoin.BEVEL.ordinal();
                }
            }
            stroke = new BasicStroke(w, c, j, m);
        }

        return new LineMap.SVGPath(id, shape, stroke, fillPaint, strokePaint, true, false);
    }
}
