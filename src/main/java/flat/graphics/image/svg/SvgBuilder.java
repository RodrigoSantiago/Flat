package flat.graphics.image.svg;

import flat.graphics.context.Paint;
import flat.graphics.context.enums.LineCap;
import flat.graphics.context.enums.LineJoin;
import flat.graphics.context.paints.ColorPaint;
import flat.math.Affine;
import flat.math.shapes.*;
import flat.resources.Parser;
import flat.uxml.node.UXNodeAttribute;
import flat.uxml.node.UXNodeElement;
import flat.uxml.sheet.UXSheetParser;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueSizeList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private String readId(UXNodeElement node) {
        UXNodeAttribute fillNode = node.getAttributes().get("id");
        return fillNode == null ? null : fillNode.getValue().asString(null);
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
        return fillNode == null ? 0 : fillNode.getValue().asSize(null);
    }

    private float readStrokeWidth(UXNodeElement node) {
        UXNodeAttribute fillNode = node.getAttributes().get("stroke-width");
        return fillNode == null ? 0 : fillNode.getValue().asSize(null);
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
    
    UXNodeAttribute empty = new UXNodeAttribute("", new UXValue());
    
    private Shape readPath(UXNodeElement node) {
        UXNodeAttribute pathNode = node.getAttributes().get("d");
        String val = pathNode == null ? "" : pathNode.getValue().asString(null);
        if (!val.isEmpty()) {
            Path path = Parser.svg(val, 0);
            if (path != null && path.length() < 3000) {
                path.optimize();
            }
            return path;
        }
        return null;
    }
    
    private Shape readCircle(UXNodeElement node) {
        float cx = node.getAttributes().getOrDefault("cx", empty).getValue().asSize(null);
        float cy = node.getAttributes().getOrDefault("cy", empty).getValue().asSize(null);
        float r  = node.getAttributes().getOrDefault("r",  empty).getValue().asSize(null);
        
        if (r <= 0) return null;
        return new Circle(cx, cy, r);
    }
    
    private Shape readRect(UXNodeElement node) {
        float x = node.getAttributes().getOrDefault("x", empty).getValue().asSize(null);
        float y = node.getAttributes().getOrDefault("y", empty).getValue().asSize(null);
        float w = node.getAttributes().getOrDefault("width", empty).getValue().asSize(null);
        float h = node.getAttributes().getOrDefault("height", empty).getValue().asSize(null);
        
        if (w <= 0 || h <= 0) return null;
        return new Rectangle(x, y, w, h);
    }
    
    private Shape readEllipse(UXNodeElement node) {
        float cx = node.getAttributes().getOrDefault("cx", empty).getValue().asSize(null);
        float cy = node.getAttributes().getOrDefault("cy", empty).getValue().asSize(null);
        float rx = node.getAttributes().getOrDefault("rx", empty).getValue().asSize(null);
        float ry = node.getAttributes().getOrDefault("ry", empty).getValue().asSize(null);
        
        if (rx <= 0 || ry <= 0) return null;
        
        float x = cx - rx;
        float y = cy - ry;
        float w = rx * 2;
        float h = ry * 2;
        
        return new Ellipse(x, y, w, h);
    }
    
    private Shape readLine(UXNodeElement node) {
        float x1 = node.getAttributes().getOrDefault("x1", empty).getValue().asSize(null);
        float y1 = node.getAttributes().getOrDefault("y1", empty).getValue().asSize(null);
        float x2 = node.getAttributes().getOrDefault("x2", empty).getValue().asSize(null);
        float y2 = node.getAttributes().getOrDefault("y2", empty).getValue().asSize(null);
        
        return new Line(x1, y1, x2, y2);
    }
    
    private Shape readPolyline(UXNodeElement node) {
        UXNodeAttribute pointsNode = node.getAttributes().get("points");
        if (pointsNode == null) return null;
        
        String val = pointsNode.getValue().asString(null);
        if (val == null || val.isEmpty()) return null;
        
        Path p = new Path();
        String[] tokens = val.trim().split("[ ,]+");
        
        for (int i = 0; i + 1 < tokens.length; i += 2) {
            float x = Float.parseFloat(tokens[i]);
            float y = Float.parseFloat(tokens[i + 1]);
            if (i == 0) p.moveTo(x, y);
            else p.lineTo(x, y);
        }
        
        return p;
    }
    
    private Shape readPolygon(UXNodeElement node) {
        UXNodeAttribute pointsNode = node.getAttributes().get("points");
        if (pointsNode == null) return null;
        
        String val = pointsNode.getValue().asString(null);
        if (val == null || val.isEmpty()) return null;
        
        Path p = new Path();
        String[] tokens = val.trim().split("[ ,]+");
        
        for (int i = 0; i + 1 < tokens.length; i += 2) {
            float x = Float.parseFloat(tokens[i]);
            float y = Float.parseFloat(tokens[i + 1]);
            if (i == 0) p.moveTo(x, y);
            else p.lineTo(x, y);
        }
        
        p.closePath();
        return p;
    }
    
    private SvgNode genNode(SvgNode parent, UXNodeElement node) {
        readStyle(node);

        if (parent == null && "svg".equals(node.getName())) {
            return new SvgRoot(readId(node), readSize(node, "width"), readSize(node, "height"), readViewBox(node));

        } else if ("path".equals(node.getName())) {
            return new SvgShape(parent, readId(node), readTransform(node),
                    readPath(node), readFill(node), readStroke(node),
                    readStrokeWidth(node), readStrokeMiter(node), readStrokeCap(node), readStrokeJoin(node));

        } else if ("circle".equals(node.getName())) {
            return new SvgShape(parent, readId(node), readTransform(node),
                    readCircle(node), readFill(node), readStroke(node),
                    readStrokeWidth(node), readStrokeMiter(node), readStrokeCap(node), readStrokeJoin(node));
            
        } else if ("rect".equals(node.getName())) {
            return new SvgShape(parent, readId(node), readTransform(node),
                    readRect(node), readFill(node), readStroke(node),
                    readStrokeWidth(node), readStrokeMiter(node), readStrokeCap(node), readStrokeJoin(node));
            
        } else if ("ellipse".equals(node.getName())) {
            return new SvgShape(parent, readId(node), readTransform(node),
                    readEllipse(node), readFill(node), readStroke(node),
                    readStrokeWidth(node), readStrokeMiter(node), readStrokeCap(node), readStrokeJoin(node));
            
        } else if ("line".equals(node.getName())) {
            return new SvgShape(parent, readId(node), readTransform(node),
                    readLine(node), readFill(node), readStroke(node),
                    readStrokeWidth(node), readStrokeMiter(node), readStrokeCap(node), readStrokeJoin(node));
            
        } else if ("polygon".equals(node.getName())) {
            return new SvgShape(parent, readId(node), readTransform(node),
                    readPolygon(node), readFill(node), readStroke(node),
                    readStrokeWidth(node), readStrokeMiter(node), readStrokeCap(node), readStrokeJoin(node));
            
        } else if ("polyline".equals(node.getName())) {
            return new SvgShape(parent, readId(node), readTransform(node),
                    readPolyline(node), readFill(node), readStroke(node),
                    readStrokeWidth(node), readStrokeMiter(node), readStrokeCap(node), readStrokeJoin(node));
            
        } else if ("g".equals(node.getName())) {
            return new SvgGroup(parent, readId(node), readTransform(node),
                    readFill(node), readStroke(node),
                    readStrokeWidth(node), readStrokeMiter(node), readStrokeCap(node), readStrokeJoin(node));

        }
        return null;
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
        String val = pathNode == null ? "" : pathNode.getValue().asString(null);
        if (val.isEmpty()) {
            return null;
        }
        try {
            Pattern pattern = Pattern.compile("[a-zA-Z_]+\\([^)]*\\)");
            Matcher matcher = pattern.matcher(val);

            Affine transform = new Affine();
            while (matcher.find()) {
                String value = matcher.group();
                if (innerParser == null) {
                    innerParser = new UXSheetParser(value);
                } else {
                    innerParser.reset(value);
                }
                UXValue uxValue = innerParser.parseXmlAttribute().getSource(null);
                if (uxValue instanceof UXValueSizeList list) {
                    if ("translate".equalsIgnoreCase(list.getName())) {
                        float[] numbers = list.asSizeList(null);
                        if (numbers.length == 2) {
                            transform.translate(numbers[0], numbers[1]);
                        }
                    } else if ("scale".equalsIgnoreCase(list.getName())) {
                        float[] numbers = list.asSizeList(null);
                        if (numbers.length == 2) {
                            transform.scale(numbers[0], numbers[1]);
                        }
                        if (numbers.length == 1) {
                            transform.scale(numbers[0], numbers[0]);
                        }
                    } else if ("rotate".equalsIgnoreCase(list.getName())) {
                        float[] numbers = list.asSizeList(null);
                        if (numbers.length == 1) {
                            transform.rotate(numbers[0]);
                        }
                    } else if ("skewX".equalsIgnoreCase(list.getName())) {
                        float[] numbers = list.asSizeList(null);
                        if (numbers.length == 1) {
                            transform.shear(numbers[0], 0);
                        }
                    } else if ("skewY".equalsIgnoreCase(list.getName())) {
                        float[] numbers = list.asSizeList(null);
                        if (numbers.length == 1) {
                            transform.shear(0, numbers[0]);
                        }
                    } else if ("matrix".equalsIgnoreCase(list.getName())) {
                        float[] numbers = list.asSizeList(null);
                        if (numbers.length == 6) {
                            transform = new Affine(numbers[0], numbers[1], numbers[2], numbers[3], numbers[4], numbers[5]);
                        }
                    }
                }
            }
            return transform;
        } catch (Exception ignored) {
        }
        return null;
    }
}

