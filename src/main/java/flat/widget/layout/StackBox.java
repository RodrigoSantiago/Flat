package flat.widget.layout;

import flat.animations.StateInfo;
import flat.graphics.text.Align;
import flat.uxml.UXAttrs;
import flat.widget.Widget;
import flat.widget.enums.Visibility;

public class StackBox extends Box {

    private Align.Vertical verticalAlign = Align.Vertical.MIDDLE;
    private Align.Horizontal horizontalAlign = Align.Horizontal.CENTER;

    public Align.Vertical getVerticalAlign() {
        return verticalAlign;
    }

    public void setVerticalAlign(Align.Vertical verticalAlign) {
        if (this.verticalAlign != verticalAlign) {
            this.verticalAlign = verticalAlign;
            invalidate(true);
        }
    }

    public Align.Horizontal getHorizontalAlign() {
        return horizontalAlign;
    }

    public void setHorizontalAlign(Align.Horizontal horizontalAlign) {
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
            if (horizontalAlign == Align.Horizontal.LEFT) {
                xPos = getPaddingLeft() + getMarginLeft();
            } else if (horizontalAlign == Align.Horizontal.RIGHT) {
                xPos = lWidth - child.getWidth() - (getPaddingRight() + getMarginRight());
            } else if (horizontalAlign == Align.Horizontal.CENTER) {
                xPos = (getPaddingLeft() + getMarginLeft() + (lWidth - child.getWidth() - (getPaddingRight() + getMarginRight()))) * 0.5f;
            }

            float yPos = 0;
            if (verticalAlign == Align.Vertical.TOP) {
                yPos = getPaddingTop() + getMarginTop();
            } else if (verticalAlign == Align.Vertical.BOTTOM || verticalAlign == Align.Vertical.BASELINE) {
                yPos = lHeight - child.getHeight() - (getPaddingBottom() + getMarginBottom());
            } else if (verticalAlign == Align.Vertical.MIDDLE) {
                yPos = (getPaddingTop() + getMarginTop() + (lHeight - child.getHeight() - (getPaddingBottom() + getMarginBottom()))) * 0.5f;
            }
            child.setPosition(xPos, yPos);
        }
    }
}
