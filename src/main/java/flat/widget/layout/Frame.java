package flat.widget.layout;

import flat.animations.StateInfo;
import flat.graphics.SmartContext;
import flat.resources.ResourceStream;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.uxml.UXNode;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;
import flat.window.Activity;
import flat.window.Application;

public class Frame extends Parent {

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
        this.controller = controller;

        removeAll();
        if (root != null) {
            add(root);
        }
    }

    protected void setController(Controller controller) {
        this.controller = controller;
    }

    public Controller getController() {
        return controller;
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
        performLayoutConstraints(width, height, verticalAlign, horizontalAlign);
    }

    @Override
    protected void onActivityChange(Activity prev, Activity current) {
        super.onActivityChange(prev, current);
        if (prev == null && current != null) {
            if (controller != null) {
                try {
                    controller.setActivity(getActivity());
                    controller.onShow();
                } catch (Exception e) {
                    Application.handleException(e);
                }
            }
        } else if (current == null && prev != null) {
            if (controller != null) {
                try {
                    controller.onHide();
                } catch (Exception e) {
                    Application.handleException(e);
                }
            }
        }
    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);

        if (controller != null) {
            try {
                controller.onDraw(context);
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
