package flat.uxml;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class UXBuilder {

    private static HashMap<String, UXGadgetFactory> factories = new HashMap<>();

    private UXNode root;
    private Controller controller;

    private ArrayList<Pair<String, UXGadgetLinker>> linkers = new ArrayList<>();
    private HashMap<String, Gadget> targets = new HashMap<>();

    public static void install(String name, UXGadgetFactory gadgetFactory) {
        factories.put(name, gadgetFactory);
    }

    public static void installDefaultWidgets() {
        UXBuilder.install("Scene", Scene::new);
        UXBuilder.install("Box", Box::new);
        UXBuilder.install("LinearBox", LinearBox::new);
        UXBuilder.install("Divider", Divider::new);
        UXBuilder.install("Button", Button::new);
        UXBuilder.install("ToggleButton", ToggleButton::new);
        UXBuilder.install("ToggleGroup", RadioGroup::new);
        UXBuilder.install("Label", Label::new);
        UXBuilder.install("ImageView", ImageView::new);
        UXBuilder.install("Canvas", Canvas::new);
        UXBuilder.install("CheckBox", CheckBox::new);
        UXBuilder.install("CheckGroup", CheckGroup::new);
        UXBuilder.install("RadioButton", RadioButton::new);
        UXBuilder.install("RadioGroup", RadioGroup::new);
        UXBuilder.install("Switch", Switch::new);
        UXBuilder.install("Slider", Slider::new);
        UXBuilder.install("ProgressBar", ProgressBar::new);
        UXBuilder.install("ProgressCircle", ProgressCircle::new);
        UXBuilder.install("ScrollBar", ScrollBar::new);
        UXBuilder.install("ScrollBox", ScrollBox::new);
        UXBuilder.install("Grid", Grid::new);
        UXBuilder.install("Cell", Cell::new);
        UXBuilder.install("Tab", Tab::new);
        UXBuilder.install("Page", Page::new);
        UXBuilder.install("TextField", TextField::new);
        UXBuilder.install("TextArea", TextArea::new);
        UXBuilder.install("Chip", Chip::new);
        UXBuilder.install("Drawer", Drawer::new);
        UXBuilder.install("Menu", Menu::new);
        UXBuilder.install("MenuItem", MenuItem::new);
        UXBuilder.install("DropdownMenu", DropdownMenu::new);
        UXBuilder.install("ToolBar", ToolBar::new);
        UXBuilder.install("ToolItem", ToolItem::new);
        UXBuilder.install("OverflowMenu", OverflowMenu::new);
    }

    public UXBuilder(UXNode root) {
        this(root, null);
    }

    public UXBuilder(UXNode root, Controller controller) {
        this.root = root;
        this.controller = controller;
    }

    public UXNode getRoot() {
        return root;
    }

    public Controller getController() {
        return controller;
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

    public Gadget build(UXTheme theme, boolean createScene) {
        Gadget child = buildRecursive(theme, root);
        link();

        if (child instanceof Scene) {
            return child;

        } else if (child instanceof Widget) {
            if (createScene) {
                Scene scene = new Scene();
                scene.add((Widget) child);
                return scene;
            }
            return child;

        } else if (child != null) {

            return child;
        } else {

            return new Scene();
        }
    }

    private Gadget buildRecursive(UXTheme theme, UXNode node) {
        // Factory
        UXGadgetFactory gadgetFactory = factories.get(node.getName());

        if (gadgetFactory != null) {
            Gadget gadget = gadgetFactory.build();
            gadget.setAttributes(node.getValues(), node.getStyle());
            gadget.applyAttributes(theme, controller, this);

            // ID Link
            if (gadget.getId() != null) {
                targets.put(gadget.getId(), gadget);
            }

            // Children
            UXChildren children = new UXChildren(this);
            for (var uxChild : node.getChildren()) {
                Gadget child = buildRecursive(theme, uxChild);
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
        return null;
    }

    static class Pair<K, V> {
        private final K key;
        private final V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair<?, ?> pair = (Pair<?, ?>) o;
            return Objects.equals(key, pair.key) && Objects.equals(value, pair.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }
    }
}