package flat.uxml;

import flat.resources.Dimension;
import flat.resources.DimensionStream;
import flat.resources.StringBundle;
import flat.widget.Gadget;
import flat.widget.Menu;
import flat.widget.Scene;
import flat.widget.Widget;
import flat.widget.bars.OverflowMenu;
import flat.widget.bars.ToolBar;
import flat.widget.bars.ToolItem;
import flat.widget.dialogs.DropdownMenu;
import flat.widget.dialogs.MenuItem;
import flat.widget.image.Canvas;
import flat.widget.image.ImageView;
import flat.widget.layout.*;
import flat.widget.selection.*;
import flat.widget.text.*;
import flat.widget.value.ProgressBar;
import flat.widget.value.ProgressCircle;
import flat.widget.value.ScrollBar;
import flat.widget.value.Slider;

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
import java.util.Objects;

public final class UXLoader {

    private static HashMap<String, UXGadgetFactory> factories = new HashMap<>();

    public static void install(String name, UXGadgetFactory gadgetFactory) {
        factories.put(name, gadgetFactory);
    }

    static {
        UXLoader.install("Scene", Scene::new);
        UXLoader.install("Box", Box::new);
        UXLoader.install("LinearBox", LinearBox::new);
        UXLoader.install("Divider", Divider::new);
        UXLoader.install("Button", Button::new);
        UXLoader.install("ToggleButton", ToggleButton::new);
        UXLoader.install("ToggleGroup", RadioGroup::new);
        UXLoader.install("Label", Label::new);
        UXLoader.install("ImageView", ImageView::new);
        UXLoader.install("Canvas", Canvas::new);
        UXLoader.install("CheckBox", CheckBox::new);
        UXLoader.install("CheckGroup", CheckGroup::new);
        UXLoader.install("RadioButton", RadioButton::new);
        UXLoader.install("RadioGroup", RadioGroup::new);
        UXLoader.install("Switch", Switch::new);
        UXLoader.install("Slider", Slider::new);
        UXLoader.install("ProgressBar", ProgressBar::new);
        UXLoader.install("ProgressCircle", ProgressCircle::new);
        UXLoader.install("ScrollBar", ScrollBar::new);
        UXLoader.install("ScrollBox", ScrollBox::new);
        UXLoader.install("Grid", Grid::new);
        UXLoader.install("Cell", Cell::new);
        UXLoader.install("Tab", Tab::new);
        UXLoader.install("Page", Page::new);
        UXLoader.install("TextField", TextField::new);
        UXLoader.install("TextArea", TextArea::new);
        UXLoader.install("Chip", Chip::new);
        UXLoader.install("Drawer", Drawer::new);
        UXLoader.install("Menu", Menu::new);
        UXLoader.install("MenuItem", MenuItem::new);
        UXLoader.install("DropdownMenu", DropdownMenu::new);
        UXLoader.install("ToolBar", ToolBar::new);
        UXLoader.install("ToolItem", ToolItem::new);
        UXLoader.install("OverflowMenu", OverflowMenu::new);
    }

    private DimensionStream dimensionStream;
    private Dimension dimension;
    private StringBundle stringBundle;
    private UXTheme theme;

    private Controller controller;

    private ArrayList<Pair<String, UXGadgetLinker>> linkers = new ArrayList<>();
    private HashMap<String, Gadget> targets = new HashMap<>();

    public UXLoader(DimensionStream dimensionStream, Dimension dimension) {
        this(dimensionStream, dimension, null);
    }

    public UXLoader(DimensionStream dimensionStream, Dimension dimension, UXTheme theme) {
        this(dimensionStream, dimension, theme, null, null);
    }

    public UXLoader(DimensionStream dimensionStream, Dimension dimension, UXTheme theme, StringBundle stringBundle, Controller controller) {
        setDimensionStream(dimensionStream);
        setDimension(dimension);
        setStringBundle(stringBundle);
        setController(controller);
        setTheme(theme);
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

    public UXTheme getTheme() {
        return theme;
    }

    public UXLoader setTheme(UXTheme theme) {
        this.theme = theme;
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

    void addLink(String id, UXGadgetLinker linker) {
        linkers.add(new Pair<>(id, linker));
    }

    void link() {
        for (Pair<String, UXGadgetLinker> linker : linkers) {
            linker.getValue().onLink(targets.get(linker.getKey()));
        }
        linkers.clear();
        targets.clear();
    }

    public Widget load() throws Exception {
        return load(false);
    }

    public Widget load(boolean createScene) throws Exception {
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
                    Gadget gadget = recursive(controller, node);
                    if (gadget != null) {
                        uxml = gadget.getWidget();
                        if (uxml != null) {
                            break;
                        }
                    }
                }
            }

            link();

            if (uxml == null) {
                return null;
            } else if (createScene && !(uxml instanceof Scene)) {
                Scene scene = new Scene();
                scene.add(uxml);
                return scene;
            } else {
                return uxml;
            }
        } finally {
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
        }
    }

    private Gadget recursive(Controller controller, Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            // Name
            String name = node.getNodeName();

            // Factory
            UXGadgetFactory gadgetFactory = factories.get(name);

            if (gadgetFactory != null) {
                Gadget gadget = gadgetFactory.build();

                // Attributes
                UXStyle style = null;
                HashMap<String, UXValue> values = new HashMap<>();

                NamedNodeMap nnm = node.getAttributes();
                for (int i = 0; i < nnm.getLength(); i++) {
                    Node item = nnm.item(i);
                    String att = item.getNodeName();
                    String value = item.getNodeValue();
                    if (value == null) {
                        value = "true";
                    } else if (value.startsWith("$") && stringBundle != null) {
                        value = stringBundle.get(value.substring(1));
                    } else if (value.startsWith("\\$")) {
                        value = value.substring(1);
                    }
                    if (att.equals("style")) {
                        style = theme.getStyle(value);
                    } else {
                        values.put(att, new UXValue(value, false));
                    }
                }

                // Style
                if (style == null) {
                    style = theme.getStyle(name.toLowerCase());
                }

                gadget.applyAttributes(new UXStyleAttrs("attributes", style, this, values), controller);

                // ID Link
                if (gadget.getId() != null) {
                    targets.put(gadget.getId(), gadget);
                }

                // Children
                NodeList nList = node.getChildNodes();
                UXChildren children = new UXChildren(this);
                for (int i = 0; i < nList.getLength(); i++) {
                    Node childNode = nList.item(i);
                    Gadget child = recursive(controller, childNode);
                    if (child != null) {
                        if (child instanceof Menu) {
                            children.addMenu((Menu) child.getWidget());
                        } else {
                            children.add(child);
                        }
                    }
                }
                gadget.applyChildren(children);
                return gadget;
            }
        }
        return null;
    }
    
    static class Pair<K, V> {
        private final K Key;
        private final V Value;
    
        public Pair(K key, V value) {
            Key = key;
            Value = value;
        }
    
        public K getKey() {
            return Key;
        }
    
        public V getValue() {
            return Value;
        }
    
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(Key, pair.Key) && Objects.equals(Value, pair.Value);
        }
    
        @Override
        public int hashCode() {
            return Objects.hash(Key, Value);
        }
    }
}