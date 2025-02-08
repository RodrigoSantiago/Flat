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
    private ArrayList<Widget> orderedList;
    private float[] tempSize;

    public LinearBox() {
        orderedList = new ArrayList<>();
    }

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
            onMeasureVertical();
        } else {
            onMeasureHorizontal();
        }
    }

    private void onMeasureVertical() {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();
        float mWidth = 0;
        float mHeight = 0;
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;

        for (Widget child : getChildrenIterable()) {
            if (child.getVisibility() == Visibility.GONE) continue;

            child.onMeasure();
            if (wrapWidth) {
                if (child.getMeasureWidth() == MATCH_PARENT) {
                    float mW = Math.min(child.getMeasureWidth(), child.getLayoutMaxWidth());
                    if (mW > mWidth) {
                        mWidth = mW;
                    }
                } else if (child.getMeasureWidth() > mWidth) {
                    mWidth = child.getMeasureWidth();
                }
            }
            if (wrapHeight) {
                if (child.getMeasureHeight() == MATCH_PARENT) {
                    mHeight += Math.min(child.getMeasureHeight(), child.getLayoutMaxHeight());
                } else {
                    mHeight += child.getMeasureHeight();
                }
            }
        }
        if (wrapWidth) {
            mWidth = Math.max(mWidth + extraWidth, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            mHeight = Math.max(mHeight + extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    private void onMeasureHorizontal() {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();
        float mWidth = 0;
        float mHeight = 0;
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;
        for (Widget child : getChildrenIterable()) {
            if (child.getVisibility() == Visibility.GONE) continue;

            child.onMeasure();
            if (wrapWidth) {
                if (child.getMeasureWidth() == MATCH_PARENT) {
                    mWidth += Math.min(child.getMeasureWidth(), child.getLayoutMaxWidth());
                } else {
                    mWidth += child.getMeasureWidth();
                }
            }
            if (wrapHeight) {
                if (child.getMeasureHeight() == MATCH_PARENT) {
                    float mH = Math.min(child.getMeasureHeight(), child.getLayoutMaxHeight());
                    if (mH > mHeight) {
                        mHeight = mH;
                    }
                } else if (child.getMeasureHeight() > mHeight) {
                    mHeight = child.getMeasureHeight();
                }
            }
        }
        if (wrapWidth) {
            mWidth = Math.max(mWidth + extraWidth, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            mHeight = Math.max(mHeight + extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public void onLayout(float width, float height) {
        if (direction == Direction.VERTICAL || direction == Direction.IVERTICAL) {
            onLayoutVertical(width, height);
        } else {
            onLayoutHorizontal(width, height);
        }
    }

    private float getDefWidth(Widget widget) {
        return Math.min(widget.getLayoutMaxWidth(), widget.getMeasureWidth());
    }

    private float getDefHeight(Widget widget) {
        return Math.min(widget.getLayoutMaxHeight(), widget.getMeasureHeight());
    }

    private void onLayoutVertical(float width, float height) {
        setLayout(width, height);

        float lWidth = Math.max(0, getLayoutWidth()
                - getMarginLeft() - getMarginRight() - getPaddingLeft() - getPaddingRight());
        float lHeight = Math.max(0, getLayoutHeight()
                - getMarginTop() - getMarginBottom() - getPaddingTop() - getPaddingBottom());

        float totalMinimum = 0;
        float totalDefined = 0;
        int countMp = 0;
        float sumWeight = 0;
        for (Widget child : orderedList) {
            if (child.getVisibility() == Visibility.GONE) continue;

            if (child.getMeasureHeight() == MATCH_PARENT) {
                countMp += 1;
                sumWeight += child.getWeight();
            } else {
                totalDefined += Math.max(getDefHeight(child) - child.getLayoutMinHeight(), 0);
            }
            totalMinimum += child.getLayoutMinHeight();
        }

        float minSpace = Math.min(totalMinimum, lHeight);
        float defSpace = Math.min(totalDefined, lHeight - minSpace);

        // Set Defined Layout
        for (Widget child : orderedList) {
            if (child.getVisibility() == Visibility.GONE || child.getMeasureHeight() == MATCH_PARENT) continue;

            float childMin = child.getLayoutMinHeight();
            float childDef = Math.max(getDefHeight(child) - childMin, 0);

            float childHeightM = totalMinimum == 0 ? 0 : (childMin / totalMinimum) * minSpace;
            float childHeightD = totalDefined == 0 ? 0 : (childDef / totalDefined) * defSpace;

            float childWidth = Math.min(Math.min(child.getMeasureWidth(), child.getLayoutMaxWidth()), lWidth);
            float childHeight = childHeightM + childHeightD;
            child.onLayout(childWidth, childHeight);
        }

        if (countMp > 0 && (tempSize == null || tempSize.length < countMp)) {
            tempSize = new float[countMp];
        }

        int i = 0;
        for (Widget child : orderedList) {
            if (child.getVisibility() == Visibility.GONE || child.getMeasureHeight() != MATCH_PARENT) continue;
            tempSize[i++] = 0;
        }

        // Find the best fit size
        float totalSpaceLeft = Math.max(lHeight - minSpace - totalDefined, 0);
        while (totalSpaceLeft > 0) {

            int nOut = 0;
            float nWeight = 0;
            float spaceLeft = totalSpaceLeft;

            int iPos = 0;
            for (Widget child : orderedList) {
                if (child.getVisibility() == Visibility.GONE || child.getMeasureHeight() != MATCH_PARENT) continue;

                float childMin = totalMinimum == 0 ? 0 : (child.getLayoutMinHeight() / totalMinimum) * minSpace;
                if (childMin + tempSize[iPos] < child.getLayoutMaxHeight()) {
                    float part = (sumWeight == 0 ? 1f / countMp : child.getWeight() / sumWeight);
                    float size = part * spaceLeft;

                    if (childMin + tempSize[iPos] + size >= child.getLayoutMaxHeight()) {
                        totalSpaceLeft -= (child.getLayoutMaxHeight() - childMin) - tempSize[iPos];
                        tempSize[iPos] = (child.getLayoutMaxHeight() - childMin);

                    } else {
                        nOut += 1;
                        nWeight += child.getWeight();

                        tempSize[iPos] += size;
                        totalSpaceLeft -= size;
                    }
                }
                iPos++;
            }

            if (totalSpaceLeft >= 1f && nOut > 0) {
                countMp = nOut;
                sumWeight = nWeight;
            } else {
                break;
            }
        }

        // Set MatchParent Layout
        int j = 0;
        for (Widget child : orderedList) {
            if (child.getVisibility() == Visibility.GONE || child.getMeasureHeight() != MATCH_PARENT) continue;
            float childMin = totalMinimum == 0 ? 0 : (child.getLayoutMinHeight() / totalMinimum) * minSpace;

            float childWidth = Math.min(Math.min(child.getMeasureWidth(), child.getLayoutMaxWidth()), lWidth);
            float childHeight = childMin + tempSize[j++];
            child.onLayout(childWidth, childHeight);
        }

        // Set Positions
        float totalHeight = 0;
        for (Widget child : orderedList) {
            if (child.getVisibility() == Visibility.GONE) continue;
            totalHeight += child.getLayoutHeight();
        }

        float xPos = 0;
        float yPos = 0;
        if (verticalAlign == VerticalAlign.TOP) {
            yPos = getPaddingTop() + getMarginTop();
        } else if (verticalAlign == VerticalAlign.BOTTOM || verticalAlign == VerticalAlign.BASELINE) {
            yPos = getLayoutHeight() - getPaddingBottom() - getMarginBottom() - totalHeight;
        } else if (verticalAlign == VerticalAlign.MIDDLE) {
            yPos = (getPaddingTop() + getMarginTop() + (getLayoutHeight() - getPaddingBottom() - getMarginBottom() - totalHeight)) * 0.5f;
        }

        if (direction == Direction.VERTICAL) {
            for (int k = 0; k < orderedList.size(); k++) {
                Widget child = orderedList.get(k);
                if (child.getVisibility() == Visibility.GONE) continue;
                if (horizontalAlign == HorizontalAlign.LEFT) {
                    xPos = getPaddingLeft() + getMarginLeft();
                } else if (horizontalAlign == HorizontalAlign.RIGHT) {
                    xPos = getLayoutWidth() - child.getLayoutWidth() - (getPaddingRight() + getMarginRight());
                } else if (horizontalAlign == HorizontalAlign.CENTER) {
                    xPos = (getPaddingLeft() + getMarginLeft() + (getLayoutWidth() - child.getLayoutWidth() - (getPaddingRight() + getMarginRight()))) * 0.5f;
                }

                child.setPosition(xPos, yPos);
                yPos += child.getLayoutHeight();
            }
        } else {
            for (int k = orderedList.size() - 1; k >= 0; k--) {
                Widget child = orderedList.get(k);
                if (child.getVisibility() == Visibility.GONE) continue;
                if (horizontalAlign == HorizontalAlign.LEFT) {
                    xPos = getPaddingLeft() + getMarginLeft();
                } else if (horizontalAlign == HorizontalAlign.RIGHT) {
                    xPos = getLayoutWidth() - child.getLayoutWidth() - (getPaddingRight() + getMarginRight());
                } else if (horizontalAlign == HorizontalAlign.CENTER) {
                    xPos = (getPaddingLeft() + getMarginLeft() + (getLayoutWidth() - child.getLayoutWidth() - (getPaddingRight() + getMarginRight()))) * 0.5f;
                }

                child.setPosition(xPos, yPos);
                yPos += child.getLayoutHeight();
            }
        }
    }

    private void onLayoutHorizontal(float width, float height) {
        setLayout(width, height);

        float lWidth = Math.max(0, getLayoutWidth()
                - getMarginLeft() - getMarginRight() - getPaddingLeft() - getPaddingRight());
        float lHeight = Math.max(0, getLayoutHeight()
                - getMarginTop() - getMarginBottom() - getPaddingTop() - getPaddingBottom());

        float totalMinimum = 0;
        float totalDefined = 0;
        int countMp = 0;
        float sumWeight = 0;
        for (Widget child : orderedList) {
            if (child.getVisibility() == Visibility.GONE) continue;

            if (child.getMeasureWidth() == MATCH_PARENT) {
                countMp += 1;
                sumWeight += child.getWeight();
            } else {
                totalDefined += Math.max(getDefWidth(child) - child.getLayoutMinWidth(), 0);
            }
            totalMinimum += child.getLayoutMinWidth();
        }

        float minSpace = Math.min(totalMinimum, lWidth);
        float defSpace = Math.min(totalDefined, lWidth - minSpace);

        // Set Defined Layout
        for (Widget child : orderedList) {
            if (child.getVisibility() == Visibility.GONE || child.getMeasureWidth() == MATCH_PARENT) continue;

            float childMin = child.getLayoutMinWidth();
            float childDef = Math.max(getDefWidth(child) - childMin, 0);

            float childWidthM = totalMinimum == 0 ? 0 : (childMin / totalMinimum) * minSpace;
            float childWidthD = totalDefined == 0 ? 0 : (childDef / totalDefined) * defSpace;

            float childWidth = childWidthM + childWidthD;
            float childHeight = Math.min(Math.min(child.getMeasureHeight(), child.getLayoutMaxHeight()), lHeight);
            child.onLayout(childWidth, childHeight);
        }

        if (countMp > 0 && (tempSize == null || tempSize.length < countMp)) {
            tempSize = new float[countMp];
        }

        int i = 0;
        for (Widget child : orderedList) {
            if (child.getVisibility() == Visibility.GONE || child.getMeasureWidth() != MATCH_PARENT) continue;
            tempSize[i++] = 0;
        }

        // Find the best fit size
        float totalSpaceLeft = Math.max(lWidth - minSpace - totalDefined, 0);
        while (totalSpaceLeft > 0) {

            int nOut = 0;
            float nWeight = 0;
            float spaceLeft = totalSpaceLeft;

            int iPos = 0;
            for (Widget child : orderedList) {
                if (child.getVisibility() == Visibility.GONE || child.getMeasureWidth() != MATCH_PARENT) continue;

                float childMin = totalMinimum == 0 ? 0 : (child.getLayoutMinWidth() / totalMinimum) * minSpace;
                if (childMin + tempSize[iPos] < child.getLayoutMaxWidth()) {
                    float part = (sumWeight == 0 ? 1f / countMp : child.getWeight() / sumWeight);
                    float size = part * spaceLeft;

                    if (childMin + tempSize[iPos] + size >= child.getLayoutMaxWidth()) {
                        totalSpaceLeft -= (child.getLayoutMaxWidth() - childMin) - tempSize[iPos];
                        tempSize[iPos] = (child.getLayoutMaxWidth() - childMin);

                    } else {
                        nOut += 1;
                        nWeight += child.getWeight();

                        tempSize[iPos] += size;
                        totalSpaceLeft -= size;
                    }
                }
                iPos++;
            }

            if (totalSpaceLeft >= 1f && nOut > 0) {
                countMp = nOut;
                sumWeight = nWeight;
            } else {
                break;
            }
        }

        // Set MatchParent Layout
        int j = 0;
        for (Widget child : orderedList) {
            if (child.getVisibility() == Visibility.GONE || child.getMeasureWidth() != MATCH_PARENT) continue;
            float childMin = totalMinimum == 0 ? 0 : (child.getLayoutMinWidth() / totalMinimum) * minSpace;

            float childWidth = childMin + tempSize[j++];
            float childHeight = Math.min(Math.min(child.getMeasureHeight(), child.getLayoutMaxHeight()), lHeight);
            child.onLayout(childWidth, childHeight);
        }

        // Set Positions
        float totalWidth = 0;
        for (Widget child : orderedList) {
            if (child.getVisibility() == Visibility.GONE) continue;
            totalWidth += child.getLayoutWidth();
        }

        float xPos = 0;
        float yPos = 0;
        if (horizontalAlign == HorizontalAlign.LEFT) {
            xPos = getPaddingLeft() + getMarginLeft();
        } else if (horizontalAlign == HorizontalAlign.RIGHT) {
            xPos = getLayoutWidth() - getPaddingRight() - getMarginRight() - totalWidth;
        } else if (horizontalAlign == HorizontalAlign.CENTER) {
            xPos = (getPaddingLeft() + getMarginLeft() + (getLayoutWidth() - getPaddingRight() - getMarginRight() - totalWidth)) * 0.5f;
        }

        if (direction == Direction.HORIZONTAL) {
            for (int k = 0; k < orderedList.size(); k++) {
                Widget child = orderedList.get(k);
                if (child.getVisibility() == Visibility.GONE) continue;
                if (verticalAlign == VerticalAlign.TOP) {
                    yPos = getPaddingTop() + getMarginTop();
                } else if (verticalAlign == VerticalAlign.BOTTOM || verticalAlign == VerticalAlign.BASELINE) {
                    yPos = getLayoutHeight() - child.getLayoutHeight() - (getPaddingBottom() + getMarginBottom());
                } else if (verticalAlign == VerticalAlign.MIDDLE) {
                    yPos = (getPaddingTop() + getMarginTop() + (getLayoutHeight() - child.getLayoutHeight() - (getPaddingBottom() + getMarginBottom()))) * 0.5f;
                }

                child.setPosition(xPos, yPos);
                xPos += child.getLayoutWidth();
            }
        } else {
            for (int k = orderedList.size() - 1; k >= 0; k--) {
                Widget child = orderedList.get(k);
                if (child.getVisibility() == Visibility.GONE) continue;
                if (verticalAlign == VerticalAlign.TOP) {
                    yPos = getPaddingTop() + getMarginTop();
                } else if (verticalAlign == VerticalAlign.BOTTOM || verticalAlign == VerticalAlign.BASELINE) {
                    yPos = getLayoutHeight() - child.getLayoutHeight() - (getPaddingBottom() + getMarginBottom());
                } else if (verticalAlign == VerticalAlign.MIDDLE) {
                    yPos = (getPaddingTop() + getMarginTop() + (getLayoutHeight() - child.getLayoutHeight() - (getPaddingBottom() + getMarginBottom()))) * 0.5f;
                }

                child.setPosition(xPos, yPos);
                xPos += child.getLayoutWidth();
            }
        }
    }
    
    @Override
    public void add(Widget child) {
        orderedList.add(child);
        super.add(child);
    }

    @Override
    public void remove(Widget widget) {
        orderedList.remove(widget);
        super.remove(widget);
    }
}
