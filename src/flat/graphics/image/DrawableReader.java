package flat.graphics.image;

import flat.graphics.context.Paint;
import flat.graphics.context.Texture2D;
import flat.graphics.context.enuns.*;
import flat.math.shapes.Path;
import flat.math.shapes.Rectangle;
import flat.math.shapes.Shape;
import flat.math.shapes.Stroke;
import flat.math.stroke.BasicStroke;
import flat.resources.Parser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class DrawableReader {

    public static PixelMap loadPixelMap(InputStream is) throws IOException {
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
        return new PixelMap(texture, 0, 0, pimg.getWidth(), pimg.getHeight());
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
            fillPaint = Paint.color(Parser.color(fillNode.getNodeValue()));
        }
        Node strokeNode = map.getNamedItem("stroke");
        if (strokeNode != null) {
            strokePaint = Paint.color(Parser.color(strokeNode.getNodeValue()));
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

    static void disposeImage(PixelMap image) {

    }
}
