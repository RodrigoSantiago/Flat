package flat.graphics.image.svg;

import flat.graphics.context.Paint;
import flat.graphics.context.enums.LineCap;
import flat.graphics.context.enums.LineJoin;
import flat.graphics.context.paints.ColorPaint;
import flat.math.Affine;
import flat.math.shapes.Path;
import flat.math.shapes.Rectangle;
import flat.math.shapes.Shape;
import flat.resources.Parser;
import flat.uxml.node.UXNodeAttribute;
import flat.uxml.node.UXNodeElement;
import flat.uxml.sheet.UXSheetParser;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueSizeList;

public class SvgBuilder {

    private final UXNodeElement root;
    private UXSheetParser innerParser;

    public SvgBuilder(UXNodeElement root) {
        this.root = root;
    }

    public SvgRoot build() {
        SvgNode child = loadRecursive(null, root);
        if (child instanceof SvgRoot svgRoot) {
            return svgRoot;
        }
        return null;
    }

    private Paint readFill(UXNodeElement node) {
        UXNodeAttribute fillNode = node.getAttributes().get("fill");
        int col = fillNode == null ? 0 : fillNode.getValue().asColor(null);
        return col != 0 ? new ColorPaint(col) : null;
    }

    private Paint readStroke(UXNodeElement node) {
        UXNodeAttribute fillNode = node.getAttributes().get("stroke");
        int col = fillNode == null ? 0 : fillNode.getValue().asColor(null);
        return col != 0 ? new ColorPaint(col) : null;
    }

    private float readSize(UXNodeElement node, String name) {
        UXNodeAttribute fillNode = node.getAttributes().get(name);
        return fillNode == null ? 0 : fillNode.getValue().asSize(null, 160);
    }

    private float readStrokeWidth(UXNodeElement node) {
        UXNodeAttribute fillNode = node.getAttributes().get("stroke-width");
        return fillNode == null ? 0 : fillNode.getValue().asSize(null, 160);
    }

    private float readStrokeMiter(UXNodeElement node) {
        UXNodeAttribute fillNode = node.getAttributes().get("stroke-miterlimit");
        return fillNode == null ? -1 : fillNode.getValue().asNumber(null);
    }

    private int readStrokeCap(UXNodeElement node) {
        UXNodeAttribute strokeCap = node.getAttributes().get("stroke-cap");
        if (strokeCap != null) {
            return switch (strokeCap.getValue().asString(null).toLowerCase()) {
                case "round" -> LineCap.ROUND.ordinal();
                case "square" -> LineCap.SQUARE.ordinal();
                case "butt" -> LineCap.BUTT.ordinal();
                default -> -1;
            };
        }
        return -1;
    }

    private Rectangle readViewBox(UXNodeElement node) {
        UXNodeAttribute fillNode = node.getAttributes().get("viewBox");
        String text = fillNode == null ? "" : fillNode.getValue().asString(null);
        if (!text.isEmpty()) {
            String[] split = text.split(" ");
            if (split.length != 4) return null;
            try {
                float x = Float.parseFloat(split[0]);
                float y = Float.parseFloat(split[1]);
                float w = Float.parseFloat(split[2]);
                float h = Float.parseFloat(split[3]);
                return new Rectangle(x, y, w, h);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private int readStrokeJoin(UXNodeElement node) {
        UXNodeAttribute strokeJoin = node.getAttributes().get("stroke-linejoin");
        if (strokeJoin != null) {
            return switch (strokeJoin.getValue().asString(null).toLowerCase()) {
                case "arcs", "mitter", "mitter-clip" -> LineJoin.MITER.ordinal();
                case "round" -> LineJoin.ROUND.ordinal();
                case "bevel" -> LineJoin.BEVEL.ordinal();
                default -> -1;
            };
        }
        return -1;
    }

    private Shape readPath(UXNodeElement node) {
        UXNodeAttribute pathNode = node.getAttributes().get("d");
        String val = pathNode == null ? "" : pathNode.getValue().asString(null);
        if (!val.isEmpty()) {
            Path path = Parser.svg(val, 0);
            if (path.length() < 3000) {
                path.optimize();
            }
            return path;
        }
        return null;
    }

    private SvgNode genNode(SvgNode parent, UXNodeElement node) {
        readStyle(node);

        if (parent == null && "svg".equals(node.getName())) {
            return new SvgRoot(null, readSize(node, "width"), readSize(node, "height"), readViewBox(node));

        } else if ("path".equals(node.getName())) {
            return new SvgShape(parent, null, readTransform(node),
                    readPath(node), readFill(node), readStroke(node),
                    readStrokeWidth(node), readStrokeMiter(node), readStrokeCap(node), readStrokeJoin(node));

        } else if ("g".equals(node.getName())) {
            return new SvgGroup(parent, null, readTransform(node),
                    readFill(node), readStroke(node),
                    readStrokeWidth(node), readStrokeMiter(node), readStrokeCap(node), readStrokeJoin(node));

        }
        return null;
    }

    private SvgNode loadRecursive(SvgNode parent, UXNodeElement node) {
        // Factory
        SvgNode svgNode = genNode(parent, node);
        if (svgNode == null) {
            return null;
        }

        // Children
        SvgChildren children = new SvgChildren();
        for (var svgChild : node.getChildren()) {
            SvgNode child = loadRecursive(svgNode, svgChild);
            if (child != null) {
                children.add(child, svgChild.getAttributes());
            }
        }
        svgNode.applyChildren(children);

        // Link
        // svgNode.add(new UXBuilder.KeyValue(id, widget));

        return svgNode;
    }

    private void readStyle(UXNodeElement node) {
        UXNodeAttribute pathNode = node.getAttributes().get("style");
        String val = pathNode == null ? "" : pathNode.getValue().asString(null);
        if (!val.isEmpty()) {
            try {
                String[] styles = val.split(";");
                for (var style : styles) {
                    String[] pair = style.split(":");
                    String name = pair[0].trim();
                    String value = pair[1].trim();
                    if (innerParser == null) {
                        innerParser = new UXSheetParser(value);
                    } else {
                        innerParser.reset(value);
                    }
                    UXValue uxValue = innerParser.parseXmlAttribute();
                    node.getAttributes().put(name, new UXNodeAttribute(name, uxValue));
                }
            } catch (Exception ignored) {
            }
        }
    }

    private Affine readTransform(UXNodeElement node) {
        UXNodeAttribute pathNode = node.getAttributes().get("transform");
        if (pathNode != null && pathNode.getValue() != null) {
            UXValue val = pathNode.getValue().getSource(null);
            if (val instanceof UXValueSizeList list) {
                if ("translate".equalsIgnoreCase(list.getName())) {
                    float[] numbers = list.asSizeList(null, 160);
                    if (numbers.length == 2) {
                        return new Affine().translate(numbers[0], numbers[1]);
                    }
                } else if ("scale".equalsIgnoreCase(list.getName())) {
                    float[] numbers = list.asSizeList(null, 160);
                    if (numbers.length == 2) {
                        return new Affine().scale(numbers[0], numbers[1]);
                    }
                } else if ("rotate".equalsIgnoreCase(list.getName())) {
                    float[] numbers = list.asSizeList(null, 160);
                    if (numbers.length == 1) {
                        return new Affine().rotate(numbers[0]);
                    }
                } else if ("skewX".equalsIgnoreCase(list.getName())) {
                    float[] numbers = list.asSizeList(null, 160);
                    if (numbers.length == 1) {
                        return new Affine().shear(numbers[0], 0);
                    }
                } else if ("skewY".equalsIgnoreCase(list.getName())) {
                    float[] numbers = list.asSizeList(null, 160);
                    if (numbers.length == 1) {
                        return new Affine().shear(0, numbers[0]);
                    }
                } else if ("matrix".equalsIgnoreCase(list.getName())) {
                    float[] numbers = list.asSizeList(null, 160);
                    if (numbers.length == 6) {
                        return new Affine(numbers[0], numbers[1], numbers[2], numbers[3], numbers[4], numbers[5]);
                    }
                }
            }
        }
        return null;
    }
}
