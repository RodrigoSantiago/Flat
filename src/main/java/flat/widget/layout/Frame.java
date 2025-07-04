package flat.widget.layout;

import flat.animations.StateInfo;
import flat.events.DrawEvent;
import flat.events.KeyEvent;
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

    private VerticalAlign verticalAlign = VerticalAlign.TOP;
    private HorizontalAlign horizontalAlign = HorizontalAlign.LEFT;

    private Controller controller;

    public Frame build(String uxmlStream) {
        return build(new ResourceStream(uxmlStream), null);
    }

    public Frame build(String uxmlStream, Controller controller) {
        return build(new ResourceStream(uxmlStream), controller);
    }

    public Frame build(ResourceStream uxmlStream) {
        return build(uxmlStream, null);
    }

    public Frame build(ResourceStream uxmlStream, Controller controller) {
        UXNode.parse(uxmlStream).instance(controller).build(this::add);
        setController(controller);
        return this;
    }

    public Frame build(Widget root) {
        return build(root, null);
    }

    public Frame build(Widget root, Controller controller) {
        removeAll();
        if (root != null) {
            add(root);
        }
        setController(controller);
        return this;
    }

    public void setController(Controller controller) {
        if (this.controller != controller) {
            Controller old = this.controller;
            this.controller = controller;
            if (old != null) {
                old.setActivity(null);
            }
            if (this.controller != null) {
                this.controller.setRoot(this);
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
        fireLayout();
    }

    @Override
    protected void onActivityChange(Activity prev, Activity current, TaskList tasks) {
        super.onActivityChange(prev, current, tasks);
        if ((prev == null) != (current == null) && controller != null) {
            tasks.add(() -> {
                if (controller != null) {
                    controller.setRoot(this);
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
        if (discardDraw(graphics)) return;

        drawBackground(graphics);
        drawRipple(graphics);
        drawChildren(graphics);

        if (controller != null && controller.isListening()) {
            try {
                controller.onDraw(new DrawEvent(this, graphics));
            } catch (Exception e) {
                Application.handleException(e);
            }
        }
    }

    public VerticalAlign getVerticalAlign() {
        return verticalAlign;
    }

    public void setVerticalAlign(VerticalAlign verticalAlign) {
        if (verticalAlign == null) verticalAlign = VerticalAlign.TOP;

        if (this.verticalAlign != verticalAlign) {
            this.verticalAlign = verticalAlign;
            invalidate(true);
        }
    }

    public HorizontalAlign getHorizontalAlign() {
        return horizontalAlign;
    }

    public void setHorizontalAlign(HorizontalAlign horizontalAlign) {
        if (horizontalAlign == null) horizontalAlign = HorizontalAlign.LEFT;

        if (this.horizontalAlign != horizontalAlign) {
            this.horizontalAlign = horizontalAlign;
            invalidate(true);
        }
    }

    @Override
    public void key(KeyEvent event) {
        super.key(event);
        if (controller != null && controller.isListening()) {
            try {
                if (event.getType() == KeyEvent.FILTER) {
                    controller.onKeyFilter(event);
                } else {
                    controller.onKey(event);
                }
            } catch (Exception e) {
                Application.handleException(e);
            }
        }
    }
}
