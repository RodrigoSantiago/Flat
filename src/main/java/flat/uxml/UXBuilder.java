package flat.uxml;

import flat.uxml.value.UXValue;
import flat.widget.image.Canvas;
import flat.widget.layout.*;
import flat.widget.stages.Divider;
import flat.widget.stages.Menu;
import flat.widget.Scene;
import flat.widget.Widget;
import flat.widget.image.ImageView;
import flat.widget.selection.Checkbox;
import flat.widget.selection.RadioButton;
import flat.widget.selection.RadioGroup;
import flat.widget.selection.SwitchToggle;
import flat.widget.stages.MenuItem;
import flat.widget.structure.*;
import flat.widget.text.*;
import flat.widget.value.*;

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
        UXBuilder.install("Text", Text::new);
        UXBuilder.install("Button", Button::new);
        UXBuilder.install("Checkbox", Checkbox::new);
        UXBuilder.install("RadioButton", RadioButton::new);
        UXBuilder.install("RadioGroup", RadioGroup::new);
        UXBuilder.install("SwitchToggle", SwitchToggle::new);
        UXBuilder.install("ImageView", ImageView::new);
        UXBuilder.install("VerticalScrollBar", VerticalScrollBar::new);
        UXBuilder.install("HorizontalScrollBar", HorizontalScrollBar::new);
        UXBuilder.install("ScrollBox", ScrollBox::new);
        UXBuilder.install("Menu", Menu::new);
        UXBuilder.install("Frame", Frame::new);
        UXBuilder.install("TabView", TabView::new);
        UXBuilder.install("Tab", Tab::new);
        UXBuilder.install("ListView", ListView::new);
        UXBuilder.install("Chip", Chip::new);
        UXBuilder.install("Slider", Slider::new);
        UXBuilder.install("RangedSlider", RangedSlider::new);
        UXBuilder.install("ProgressBar", ProgressBar::new);
        UXBuilder.install("ProgressCircle", ProgressCircle::new);
        UXBuilder.install("MenuItem", MenuItem::new);
        UXBuilder.install("Divider", Divider::new);
        UXBuilder.install("TextField", TextField::new);
        UXBuilder.install("TextInputField", TextInputField::new);
        UXBuilder.install("TextDropDown", TextDropDown::new);
        UXBuilder.install("TextArea", TextArea::new);
        UXBuilder.install("NumberInputField", NumberInputField::new);
        UXBuilder.install("DecimalInputField", DecimalInputField::new);
        UXBuilder.install("ToolBar", ToolBar::new);
        UXBuilder.install("ToolItem", ToolItem::new);
        UXBuilder.install("Grid", Grid::new);
        UXBuilder.install("Drawer", Drawer::new);
        UXBuilder.install("TreeView", TreeView::new);
        UXBuilder.install("HorizontalSplitter", HorizontalSplitter::new);
        UXBuilder.install("VerticalSplitter", VerticalSplitter::new);
        UXBuilder.install("Canvas", Canvas::new);
        UXBuilder.install("ClipBox", ClipBox::new);
        UXBuilder.install("TextStyledEditor", TextStyledEditor::new);
        // RichText text + multiple fonts + images + Emoji icons
        // MonoText text + multiple monospaced fonts
        // RawText text + monospaced infinity reading efficiency
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
    
    public Widget build(UXListener<Widget> assignTo) {
        Widget child = buildRecursive(root);
        if (child != null) {
            UXListener.safeHandle(assignTo, child);
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
            String id = getId(node);
            widget.setId(id);

            // Attributes
            widget.setAttributes(node.getValues(), node.getStyles());

            // Children
            UXChildren children = new UXChildren(this);
            for (var uxChild : node.getChildren()) {
                Widget child = buildRecursive(uxChild);
                if (child != null) {
                    if (child instanceof Menu menu) {
                        children.setMenu(menu);
                    } else {
                        children.add(child, uxChild.getValues());
                    }
                }
            }
            widget.applyChildren(children);

            // Link
            widgets.add(new KeyValue(id, widget));

            return widget;
        }
        return null;
    }

    private String getId(UXNode node) {
        UXValue uxValue = node.getValues().get(UXHash.getHash("id"));
        if (uxValue != null) {
            return uxValue.asString(null);
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

        public KeyValue(String id, Widget widget) {
            this.id = id;
            this.widget = widget;
        }
    }
}