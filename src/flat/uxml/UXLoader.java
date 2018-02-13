package flat.uxml;

import flat.widget.*;
import flat.widget.image.ImageView;
import flat.widget.layout.*;
import flat.widget.text.*;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import flat.uxml.data.*;

public final class UXLoader {
    private static HashMap<String, UXWidgetFactory> builders = new HashMap<>();
    public static void install(String name, UXWidgetFactory widgetFactory) {
        builders.put(name, widgetFactory);
    }

    static {
        UXLoader.install("Box", Box::new);
        UXLoader.install("VBox", VBox::new);
        UXLoader.install("HBox", HBox::new);
        UXLoader.install("Button", Button::new);
        UXLoader.install("Label", Label::new);
        UXLoader.install("ImageView", ImageView::new);
    }

    private DimensionStream dimensionStream;
    private Dimension dimension;
    private StringBundle stringBundle;

    private float fontScale = 1f;
    private Controller controller;
    private ArrayList<String> logger = new ArrayList<>();

    public UXLoader(DimensionStream dimensionStream, Dimension dimension) {
        this(dimensionStream, dimension, null, null);
    }

    public UXLoader(DimensionStream dimensionStream, Dimension dimension, StringBundle stringBundle, Controller controller) {
        setDimensionStream(dimensionStream);
        setDimension(dimension);
        setStringBundle(stringBundle);
        setController(controller);
    }

    public DimensionStream getDimensionStream() {
        return dimensionStream;
    }

    public UXLoader setDimensionStream(DimensionStream dimensionStream) {
        this.dimensionStream = dimensionStream;
        return this;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public UXLoader setDimension(Dimension dimension) {
        this.dimension = dimension;
        return this;
    }

    public StringBundle getStringBundle() {
        return stringBundle;
    }

    public UXLoader setStringBundle(StringBundle stringBundle) {
        this.stringBundle = stringBundle;
        return this;
    }

    public Object getController() {
        return controller;
    }

    public UXLoader setController(Controller controller) {
        this.controller = controller;
        return this;
    }

    public void setFontScale(float fontScale) {
        this.fontScale = fontScale;
    }

    public float getFontScale() {
        return fontScale;
    }

    public void log(String log) {
        logger.add(log);
    }

    public List<String> getLog() {
        return logger;
    }

    public Widget load() throws Exception {
        InputStream inputStream = dimensionStream.getStream(dimension);
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputStream);
            doc.getDocumentElement().normalize();

            Widget uxml = null;
            NodeList nList = doc.getChildNodes();
            for (int i = 0; i < nList.getLength(); i++) {
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    if (uxml != null) {
                        log("Unexpected root node : " + node.getNodeName());
                    } else {
                        uxml = recursive(controller, node);
                        if (uxml == null) {
                            log("Unexpected root node : " + node.getNodeName());
                        }
                    }
                }
            }

            return uxml;
        } finally {
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    private Widget recursive(Controller controller, Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            //Name
            String name = node.getNodeName();

            //Factory
            UXWidgetFactory widgetFactory = builders.get(name);

            if (widgetFactory != null) {
                Widget widget = widgetFactory.build();

                //Attributes
                NamedNodeMap nnm = node.getAttributes();
                UXAttributes atts = new UXAttributes(this);
                for (int i = 0; i < nnm.getLength(); i++) {
                    Node item = nnm.item(i);
                    String value = item.getNodeValue();
                    if (value == null) {
                        value = "true";
                    } else if (value.startsWith("$") && stringBundle != null) {
                        value = stringBundle.get(value.substring(1));
                    } else if (value.startsWith("\\$")) {
                        value = value.substring(1);
                    }
                    atts.set(item.getNodeName(), value);
                }
                widget.applyAttributes(controller, atts);

                //Children
                NodeList nList = node.getChildNodes();
                UXChildren children = new UXChildren(this);
                for (int i = 0; i < nList.getLength(); i++) {
                    Widget child = recursive(controller, nList.item(i));
                    if (child != null) {
                        children.add(child);
                    }
                }
                widget.applyChildren(children);

                atts.logUnusedAttributes();
                children.logUnusedChildren();
                return widget;
            } else {
                log("Widget not found : " + name);
            }
        }
        return null;
    }

    public boolean hasLog() {
        return logger.size() > 0;
    }

    public void printLog() {
        System.err.println("Imperfect UXML decode : " + dimensionStream.getName());
        for (String log : logger) System.err.println("    " + log);
    }
}