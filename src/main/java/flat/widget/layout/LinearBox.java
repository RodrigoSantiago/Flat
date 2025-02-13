package flat.widget.layout;

import flat.animations.StateInfo;
import flat.widget.enums.*;
import flat.uxml.UXAttrs;
import flat.widget.Widget;

import java.util.ArrayList;

public class LinearBox extends Box {

    private Direction direction = Direction.HORIZONTAL;
    private VerticalAlign verticalAlign = VerticalAlign.MIDDLE;
    private HorizontalAlign horizontalAlign = HorizontalAlign.CENTER;

    private final ArrayList<Widget> orderedList = new ArrayList<>();

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        if (direction == null) direction = Direction.HORIZONTAL;

        if (this.direction != direction) {
            this.direction = direction;
            invalidate(true);
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

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();
        setDirection(attrs.getConstant("direction", info, getDirection()));
        setHorizontalAlign(attrs.getConstant("horizontal-align", info, getHorizontalAlign()));
        setVerticalAlign(attrs.getConstant("vertical-align", info, getVerticalAlign()));
    }

    @Override
    public void onMeasure() {
        if (direction == Direction.VERTICAL || direction == Direction.IVERTICAL) {
            performMeasureVertical();
        } else {
            performMeasureHorizontal();
        }
    }

    @Override
    public void onLayout(float width, float height) {
        if (direction == Direction.VERTICAL || direction == Direction.IVERTICAL) {
            performLayoutVertical(width, height, orderedList, verticalAlign, horizontalAlign, direction);
        } else {
            performLayoutHorizontal(width, height, orderedList, verticalAlign, horizontalAlign, direction);
        }
    }
    
    @Override
    protected boolean attachChild(Widget child) {
        if (super.attachChild(child)) {
            orderedList.add(child);
            return true;
        }
        return false;
    }

    @Override
    protected boolean detachChild(Widget child) {
        if (super.detachChild(child)) {
            orderedList.remove(child);
            return true;
        }
        return false;
    }
}
