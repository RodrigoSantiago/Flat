package flat.uxml;

import flat.uxml.value.UXValue;
import flat.widget.layout.*;
import flat.widget.stages.Menu;
import flat.widget.Scene;
import flat.widget.Widget;
import flat.widget.image.ImageView;
import flat.widget.selection.Checkbox;
import flat.widget.selection.RadioButton;
import flat.widget.selection.RadioGroup;
import flat.widget.selection.SwitchToggle;
import flat.widget.text.Button;
import flat.widget.text.Label;
import flat.widget.value.ScrollBar;

import java.util.ArrayList;
import java.util.HashMap;

public class UXBuilder {

    private static final HashMap<String, UXWidgetFactory> factories = new HashMap<>();

    private final UXNode root;
    private final Controller controller;
    private final ArrayList<KeyValue> widgets = new ArrayList<>();

    public static void install(String name, UXWidgetFactory WidgetFactory) {
        factories.put(name, WidgetFactory);
    }

    static {
        UXBuilder.install("Scene", Scene::new);
        UXBuilder.install("Panel", Panel::new);
        UXBuilder.install("LinearBox", LinearBox::new);
        UXBuilder.install("StackBox", StackBox::new);
        UXBuilder.install("Label", Label::new);
        UXBuilder.install("Button", Button::new);
        UXBuilder.install("Checkbox", Checkbox::new);
        UXBuilder.install("RadioButton", RadioButton::new);
        UXBuilder.install("RadioGroup", RadioGroup::new);
        UXBuilder.install("Switch", SwitchToggle::new);
        UXBuilder.install("ImageView", ImageView::new);
        UXBuilder.install("ScrollBar", ScrollBar::new);
        UXBuilder.install("ScrollBox", ScrollBox::new);
        UXBuilder.install("Menu", Menu::new);
        UXBuilder.install("Frame", Frame::new);
        /*UXBuilder.install("Divider", Divider::new);
        UXBuilder.install("ToggleButton", ToggleButton::new);
        UXBuilder.install("ToggleGroup", RadioGroup::new);
        UXBuilder.install("Canvas", Canvas::new);
        UXBuilder.install("Slider", Slider::new);
        UXBuilder.install("ProgressBar", ProgressBar::new);
        UXBuilder.install("ProgressCircle", ProgressCircle::new);
        UXBuilder.install("Grid", Grid::new);
        UXBuilder.install("Cell", Cell::new);
        UXBuilder.install("Tab", Tab::new);
        UXBuilder.install("Page", Page::new);
        UXBuilder.install("TextField", TextField::new);
        UXBuilder.install("TextArea", TextArea::new);
        UXBuilder.install("Chip", Chip::new);
        UXBuilder.install("Drawer", Drawer::new);
        UXBuilder.install("MenuItem", MenuItem::new);
        UXBuilder.install("DropdownMenu", DropdownMenu::new);
        UXBuilder.install("ToolBar", ToolBar::new);
        UXBuilder.install("ToolItem", ToolItem::new);
        UXBuilder.install("OverflowMenu", OverflowMenu::new);*/
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

    public Widget build(UXTheme theme) {
        Widget child = buildRecursive(root);
        if (child != null) {
            child.setTheme(theme);
            assignWidgets();
        }

        return child;
    }

    public Scene buildScene(UXTheme theme) {
        Widget child = root == null ? null : buildRecursive(root);

        Scene scene;
        if (child instanceof Scene) {
            scene = (Scene) child;

        } else {
            scene = new Scene();
            if (child != null) {
                scene.add(child);
            }
        }

        scene.setTheme(theme);
        assignWidgets();

        return scene;
    }

    private Widget buildRecursive(UXNode node) {
        // Factory
        UXWidgetFactory widgetFactory = factories.get(node.getName());

        if (widgetFactory != null) {
            Widget widget = widgetFactory.build();

            // Attributes
            widget.setAttributes(node.getValues(), node.getStyle());

            // Children
            UXChildren children = new UXChildren(this);
            for (var uxChild : node.getChildren()) {
                Widget child = buildRecursive(uxChild);
                if (child != null) {
                    if (child instanceof Menu menu) {
                        children.addMenu(menu);
                    } else {
                        children.add(child);
                    }
                }
            }
            widget.applyChildren(children);

            // Link
            widgets.add(new KeyValue(node, widget));

            return widget;
        }
        return null;
    }

    private void assignWidgets() {
        for (var keyValue : widgets) {
            if (keyValue.id != null) {
                keyValue.widget.setId(keyValue.id);
                if (controller != null) {
                    controller.assign(keyValue.id, keyValue.widget);
                }
            }
        }
        for (var keyValue : widgets) {
            keyValue.widget.applyAttributes(controller);
        }
        widgets.clear();
    }

    private static class KeyValue {
        public String id;
        public Widget widget;

        public KeyValue(UXNode node, Widget widget) {
            UXValue uxValue = node.getValues().get(UXHash.getHash("id"));
            if (uxValue != null) {
                this.id = uxValue.asString(null);
            }
            this.widget = widget;
        }
    }
}