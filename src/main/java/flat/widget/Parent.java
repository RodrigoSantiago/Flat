package flat.widget;

import flat.uxml.TaskList;
import flat.widget.enums.*;
import flat.widget.stages.Dialog;
import flat.widget.stages.Menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Parent extends Widget {

    private float[] tempSize;

    @Override
    protected ArrayList<Widget> getChildren() {
        return super.getChildren();
    }

    @Override
    public void onMeasure() {
        super.onMeasure();
    }

    public boolean onLayoutSingleChild(Widget child) {
        return false;
    }

    protected boolean attachChild(Widget child) {
        if (child == this
                || this.isChildOf(child)
                || (child.getActivity() != null && child.getActivity().getScene() == child)
                || (child instanceof Stage && (getActivity() == null || getActivity().getScene() != this))
        ) {
            return false;
        }
        if (child.getParent() != null && !child.getParent().detachChild(child)) {
            return false;
        }
        children.add(child);
        child.invalidateTransform();
        invalidateChildrenOrder(null);
        invalidate(true);
        return true;
    }

    protected final boolean attachAndAddChild(Widget child, TaskList tasks) {
        if (attachChild(child)) {
            child.setParent(this, tasks);
            return true;
        }
        return false;
    }

    protected boolean detachChild(Widget child) {
        children.remove(child);
        child.invalidateTransform();
        invalidateChildrenOrder(null);
        invalidate(true);
        return true;
    }

    protected final boolean detachAndRemoveChild(Widget widget, TaskList tasks) {
        if (detachChild(widget)) {
            widget.setParent(null, tasks);
            return true;
        }
        return false;
    }

    protected void add(Widget child) {
        TaskList tasks = new TaskList();
        attachAndAddChild(child, tasks);
        tasks.run();
    }

    protected void add(Widget... children) {
        for (Widget child : children) {
            add(child);
        }
    }

    protected void add(List<Widget> children) {
        for (Widget child : children) {
            add(child);
        }
    }

    public void remove(Widget widget) {
        TaskList tasks = new TaskList();
        detachAndRemoveChild(widget, tasks);
        tasks.run();
    }

    public void removeAll() {
        List<Widget> children = new ArrayList<>(getChildren());
        for (Widget child : children) {
            remove(child);
        }
    }

    // -- Layout Helpers -- //
    protected final void performMeasureStack() {
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
                mWidth = Math.max(getDefWidth(child), mWidth);
            }
            if (wrapHeight) {
                mHeight = Math.max(getDefHeight(child), mHeight);
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

    protected final void performMeasureVertical(List<Widget> childList) {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();
        float mWidth = 0;
        float mHeight = 0;
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;

        for (Widget child : childList) {
            if (child.getVisibility() == Visibility.GONE) continue;

            child.onMeasure();
            if (wrapWidth) {
                mWidth = Math.max(getDefWidth(child), mWidth);
            }
            if (wrapHeight) {
                mHeight += getDefHeight(child);
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

    protected final void performMeasureHorizontal(List<Widget> childList) {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();
        float mWidth = 0;
        float mHeight = 0;
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;
        for (Widget child : childList) {
            if (child.getVisibility() == Visibility.GONE) continue;

            child.onMeasure();
            if (wrapWidth) {
                mWidth += getDefWidth(child);
            }
            if (wrapHeight) {
                mHeight = Math.max(getDefHeight(child), mHeight);
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

    protected final void performLayoutFree(float lWidth, float lHeight) {
        for (Widget child : getChildrenIterable()) {
            if (child.getVisibility() == Visibility.GONE) continue;

            float childWidth = Math.min(getDefWidth(child), lWidth);
            float childHeight = Math.min(getDefHeight(child), lHeight);
            child.onLayout(childWidth, childHeight);
        }
    }

    protected final void performLayoutConstraints(float lWidth, float lHeight, float lx, float ly
            , VerticalAlign verticalAlign, HorizontalAlign horizontalAlign) {

        for (Widget child : getChildrenIterable()) {
            if (child.getVisibility() == Visibility.GONE) continue;

            float childWidth = Math.min(getDefWidth(child), lWidth);
            float childHeight = Math.min(getDefHeight(child), lHeight);
            child.onLayout(childWidth, childHeight);

            float xPos = off(lx, lx + lWidth, child.getLayoutWidth(), horizontalAlign);
            float yPos = off(ly, ly + lHeight, child.getLayoutHeight(), verticalAlign);
            child.setLayoutPosition(xPos, yPos);
        }
    }

    protected final void performSingleLayoutFree(float lWidth, float lHeight, Widget child) {
        float childWidth = Math.min(getDefWidth(child), lWidth);
        float childHeight = Math.min(getDefHeight(child), lHeight);
        child.onLayout(childWidth, childHeight);
    }

    protected final void performSingleLayoutConstraints(float lWidth, float lHeight, float lx, float ly, Widget child
            , VerticalAlign verticalAlign, HorizontalAlign horizontalAlign) {
        float childWidth = Math.min(getDefWidth(child), lWidth);
        float childHeight = Math.min(getDefHeight(child), lHeight);
        child.onLayout(childWidth, childHeight);

        float xPos = off(lx, lx + lWidth, child.getLayoutWidth(), horizontalAlign);
        float yPos = off(ly, ly + lHeight, child.getLayoutHeight(), verticalAlign);
        child.setLayoutPosition(xPos, yPos);
    }

    protected final void performLayoutVertical(float lWidth, float lHeight, float lx, float ly, List<Widget> orderedList
            , VerticalAlign verticalAlign, HorizontalAlign horizontalAlign, Direction direction) {

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

            float childWidth = Math.min(getDefWidth(child), lWidth);
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

            float childWidth = Math.min(getDefWidth(child), lWidth);
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
        float yPos = off(ly, ly + lHeight, totalHeight, verticalAlign);

        if (direction == Direction.VERTICAL) {
            for (int k = 0; k < orderedList.size(); k++) {
                Widget child = orderedList.get(k);
                if (child.getVisibility() == Visibility.GONE) continue;

                xPos = off(lx, lx + lWidth, child.getLayoutWidth(), horizontalAlign);

                child.setLayoutPosition(xPos, yPos);
                yPos += child.getLayoutHeight();
            }
        } else {
            for (int k = orderedList.size() - 1; k >= 0; k--) {
                Widget child = orderedList.get(k);
                if (child.getVisibility() == Visibility.GONE) continue;

                xPos = off(lx, lx + lWidth, child.getLayoutWidth(), horizontalAlign);

                child.setLayoutPosition(xPos, yPos);
                yPos += child.getLayoutHeight();
            }
        }
    }

    protected final void performLayoutHorizontal(float lWidth, float lHeight, float lx, float ly, List<Widget> orderedList
            , VerticalAlign verticalAlign, HorizontalAlign horizontalAlign, Direction direction) {
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
            float childHeight = Math.min(getDefHeight(child), lHeight);
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
            float childHeight = Math.min(getDefHeight(child), lHeight);
            child.onLayout(childWidth, childHeight);
        }

        // Set Positions
        float totalWidth = 0;
        for (Widget child : orderedList) {
            if (child.getVisibility() == Visibility.GONE) continue;
            totalWidth += child.getLayoutWidth();
        }

        float xPos = off(lx, lx + lWidth, totalWidth, horizontalAlign);
        float yPos = 0;

        if (direction == Direction.HORIZONTAL) {
            for (int k = 0; k < orderedList.size(); k++) {
                Widget child = orderedList.get(k);
                if (child.getVisibility() == Visibility.GONE) continue;
                yPos = off(ly, ly + lHeight, child.getLayoutHeight(), verticalAlign);

                child.setLayoutPosition(xPos, yPos);
                xPos += child.getLayoutWidth();
            }
        } else {
            for (int k = orderedList.size() - 1; k >= 0; k--) {
                Widget child = orderedList.get(k);
                if (child.getVisibility() == Visibility.GONE) continue;
                yPos = off(ly, ly + lHeight, child.getLayoutHeight(), verticalAlign);

                child.setLayoutPosition(xPos, yPos);
                xPos += child.getLayoutWidth();
            }
        }
    }

    protected final float performLayoutVerticalScrollable(float lWidth, float lHeight, float lx, float ly, List<Widget> orderedList
            , VerticalAlign verticalAlign, HorizontalAlign horizontalAlign, float verticalOffset) {

        boolean scroll = false;
        float localDimensionY = 0;

        for (Widget child : getChildrenIterable()) {
            if (child.getVisibility() == Visibility.GONE) continue;

            if (child.getMeasureHeight() != MATCH_PARENT) {
                localDimensionY += child.getMeasureHeight();
            }
        }
        if (localDimensionY > lHeight) {
            if (verticalOffset > localDimensionY - lHeight) verticalOffset = localDimensionY - lHeight;
            if (verticalOffset < 0) verticalOffset = 0;
            scroll = true;
            lHeight = localDimensionY;
        } else {
            verticalOffset = 0;
        }

        //-----------------

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

            float childWidth = Math.min(getDefWidth(child), lWidth);
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

            float childWidth = Math.min(getDefWidth(child), lWidth);
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
        float yPos = scroll ? ly - verticalOffset : off(ly, ly + lHeight, totalHeight, verticalAlign);

        for (int k = 0; k < orderedList.size(); k++) {
            Widget child = orderedList.get(k);
            if (child.getVisibility() == Visibility.GONE) continue;
            xPos = off(lx, lx + lWidth, child.getLayoutWidth(), horizontalAlign);

            child.setLayoutPosition(xPos, yPos);
            yPos += child.getLayoutHeight();
        }
        return Math.max(lHeight, localDimensionY);
    }

    protected final float performLayoutHorizontalScrollable(float lWidth, float lHeight, float lx, float ly, List<Widget> orderedList
            , VerticalAlign verticalAlign, HorizontalAlign horizontalAlign, float verticalOffset) {

        boolean scroll = false;
        float localDimensionX = 0;

        for (Widget child : getChildrenIterable()) {
            if (child.getVisibility() == Visibility.GONE) continue;

            if (child.getMeasureWidth() != MATCH_PARENT) {
                localDimensionX += child.getMeasureWidth();
            }
        }
        if (localDimensionX > lWidth) {
            if (verticalOffset > localDimensionX - lWidth) verticalOffset = localDimensionX - lWidth;
            if (verticalOffset < 0) verticalOffset = 0;
            scroll = true;
            lWidth = localDimensionX;
        } else {
            verticalOffset = 0;
        }

        //-----------------
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
            float childHeight = Math.min(getDefHeight(child), lHeight);
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
            float childHeight = Math.min(getDefHeight(child), lHeight);
            child.onLayout(childWidth, childHeight);
        }

        // Set Positions
        float totalWidth = 0;
        for (Widget child : orderedList) {
            if (child.getVisibility() == Visibility.GONE) continue;
            totalWidth += child.getLayoutWidth();
        }

        float xPos = scroll ? lx - verticalOffset : off(lx, lx + lWidth, totalWidth, horizontalAlign);
        float yPos = 0;

        for (int k = 0; k < orderedList.size(); k++) {
            Widget child = orderedList.get(k);
            if (child.getVisibility() == Visibility.GONE) continue;
            yPos = off(ly, ly + lHeight, child.getLayoutHeight(), verticalAlign);

            child.setLayoutPosition(xPos, yPos);
            xPos += child.getLayoutWidth();
        }
        return Math.max(lWidth, localDimensionX);
    }

    protected final float getDefWidth(Widget child) {
        return Math.min(child.getMeasureWidth(), child.getLayoutMaxWidth());
    }

    protected final float getDefHeight(Widget child) {
        return Math.min(child.getMeasureHeight(), child.getLayoutMaxHeight());
    }

    protected final float off(float start, float end, float size, VerticalAlign verticalAlign) {
        if (verticalAlign == VerticalAlign.BOTTOM) return end - size;
        if (verticalAlign == VerticalAlign.MIDDLE) return (start + end - size) / 2f;
        return start;
    }

    protected final float off(float start, float end, float size, HorizontalAlign horizontalAlign) {
        if (horizontalAlign == HorizontalAlign.RIGHT) return end - size;
        if (horizontalAlign == HorizontalAlign.CENTER) return (start + end - size) / 2f;
        return start;
    }
}