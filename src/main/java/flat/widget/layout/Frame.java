package flat.widget.layout;

import flat.animations.StateInfo;
import flat.graphics.Graphics;
import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.widget.Group;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;
import flat.window.Activity;
import flat.window.Application;

import java.util.List;

public class Frame extends Group {

    private VerticalAlign verticalAlign = VerticalAlign.MIDDLE;
    private HorizontalAlign horizontalAlign = HorizontalAlign.CENTER;

    private Controller controller;

    public void build(String uxmlStream) {
        build(new ResourceStream(uxmlStream), null);
    }

    public void build(String uxmlStream, Controller controller) {
        build(new ResourceStream(uxmlStream), controller);
    }

    public void build(ResourceStream uxmlStream) {
        build(uxmlStream, null);
    }

    public void build(ResourceStream uxmlStream, Controller controller) {
        build(UXNode.parse(uxmlStream).instance(controller).build(getCurrentTheme()), controller);
    }

    public void build(Widget root) {
        build(root, null);
    }

    public void build(Widget root, Controller controller) {
        removeAll();
        if (root != null) {
            add(root);
        }
        setController(controller);
    }

    public void setController(Controller controller) {
        if (this.controller != controller) {
            Controller old = this.controller;
            this.controller = controller;
            if (old != null) {
                old.setActivity(null);
            }
            if (this.controller != null) {
                this.controller.setActivity(getActivity());
            }
        }
    }

    public Controller getController() {
        return controller;
    }

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        for (var child : children) {
            add(child.getWidget());
        }
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();
        setHorizontalAlign(attrs.getConstant("horizontal-align", info, getHorizontalAlign()));
        setVerticalAlign(attrs.getConstant("vertical-align", info, getVerticalAlign()));
    }

    @Override
    public void onMeasure() {
        performMeasureStack();
    }

    @Override
    public void onLayout(float width, float height) {
        setLayout(width, height);
        performLayoutConstraints(getInWidth(), getInHeight(), getInX(), getInY(), verticalAlign, horizontalAlign);
    }

    @Override
    protected void onActivityChange(Activity prev, Activity current, TaskList tasks) {
        super.onActivityChange(prev, current, tasks);
        if ((prev == null) != (current == null) && controller != null) {
            tasks.add(() -> {
                if (controller != null) {
                    controller.setActivity(this.getActivity());
                }
            });
        }
    }

    @Override
    public void add(Widget child) {
        super.add(child);
    }

    @Override
    public void add(Widget... children) {
        super.add(children);
    }

    @Override
    public void add(List<Widget> children) {
        super.add(children);
    }

    @Override
    public void onDraw(Graphics graphics) {
        super.onDraw(graphics);

        if (controller != null && controller.isListening()) {
            try {
                controller.onDraw(graphics);
            } catch (Exception e) {
                Application.handleException(e);
            }
        }
    }

    public VerticalAlign getVerticalAlign() {
        return verticalAlign;
    }

    public void setVerticalAlign(VerticalAlign verticalAlign) {
        if (verticalAlign == null) verticalAlign = VerticalAlign.MIDDLE;

        if (this.verticalAlign != verticalAlign) {
            this.verticalAlign = verticalAlign;
            invalidate(true);
        }
    }

    public HorizontalAlign getHorizontalAlign() {
        return horizontalAlign;
    }

    public void setHorizontalAlign(HorizontalAlign horizontalAlign) {
        if (horizontalAlign == null) horizontalAlign = HorizontalAlign.CENTER;

        if (this.horizontalAlign != horizontalAlign) {
            this.horizontalAlign = horizontalAlign;
            invalidate(true);
        }
    }
}
