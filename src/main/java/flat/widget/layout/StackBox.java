package flat.widget.layout;

import flat.animations.StateInfo;
import flat.uxml.UXAttrs;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;
import flat.widget.enums.Visibility;

public class StackBox extends Box {

    private VerticalAlign verticalAlign = VerticalAlign.MIDDLE;
    private HorizontalAlign horizontalAlign = HorizontalAlign.CENTER;

    public VerticalAlign getVerticalAlign() {
        return verticalAlign;
    }

    public void setVerticalAlign(VerticalAlign verticalAlign) {
        if (this.verticalAlign != verticalAlign) {
            this.verticalAlign = verticalAlign;
            invalidate(true);
        }
    }

    public HorizontalAlign getHorizontalAlign() {
        return horizontalAlign;
    }

    public void setHorizontalAlign(HorizontalAlign horizontalAlign) {
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
        setHorizontalAlign(attrs.getConstant("horizontal-align", info, getHorizontalAlign()));
        setVerticalAlign(attrs.getConstant("vertical-align", info, getVerticalAlign()));
    }

    @Override
    public void onLayout(float width, float height) {
        setLayout(width, height);

        float lWidth = Math.max(0, getWidth()
                - getMarginLeft() - getMarginRight() - getPaddingLeft() - getPaddingRight());
        float lHeight = Math.max(0, getHeight()
                - getMarginTop() - getMarginBottom() - getPaddingTop() - getPaddingBottom());

        for (Widget child : getChildrenIterable()) {
            if (child.getVisibility() == Visibility.GONE) continue;

            float childWidth = Math.min(Math.min(child.getMeasureWidth(), child.getLayoutMaxWidth()), lWidth);
            float childHeight = Math.min(Math.min(child.getMeasureHeight(), child.getLayoutMaxHeight()), lHeight);
            child.onLayout(childWidth, childHeight);

            float xPos = 0;
            if (horizontalAlign == HorizontalAlign.LEFT) {
                xPos = getPaddingLeft() + getMarginLeft();
            } else if (horizontalAlign == HorizontalAlign.RIGHT) {
                xPos = getWidth() - child.getWidth() - (getPaddingRight() + getMarginRight());
            } else if (horizontalAlign == HorizontalAlign.CENTER) {
                xPos = (getPaddingLeft() + getMarginLeft() + (getWidth() - child.getWidth() - (getPaddingRight() + getMarginRight()))) * 0.5f;
            }

            float yPos = 0;
            if (verticalAlign == VerticalAlign.TOP) {
                yPos = getPaddingTop() + getMarginTop();
            } else if (verticalAlign == VerticalAlign.BOTTOM || verticalAlign == VerticalAlign.BASELINE) {
                yPos = getHeight() - child.getHeight() - (getPaddingBottom() + getMarginBottom());
            } else if (verticalAlign == VerticalAlign.MIDDLE) {
                yPos = (getPaddingTop() + getMarginTop() + (getHeight() - child.getHeight() - (getPaddingBottom() + getMarginBottom()))) * 0.5f;
            }
            child.setPosition(xPos, yPos);
        }
    }
}
