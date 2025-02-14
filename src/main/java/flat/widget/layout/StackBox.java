package flat.widget.layout;

import flat.animations.StateInfo;
import flat.uxml.UXAttrs;
import flat.uxml.UXChildren;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;

import java.util.List;

public class StackBox extends Parent {

    private VerticalAlign verticalAlign = VerticalAlign.MIDDLE;
    private HorizontalAlign horizontalAlign = HorizontalAlign.CENTER;

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        Widget widget;
        while ((widget = children.next()) != null ) {
            add(widget);
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
        performLayoutConstraints(width, height, verticalAlign, horizontalAlign);
    }

    @Override
    public boolean onLayoutSingleChild(Widget child) {
        if (getChildren().contains(child)) {
            performSingleLayoutConstraints(getLayoutWidth(), getLayoutHeight(), child, verticalAlign, horizontalAlign);
            return true;
        }
        return false;
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
