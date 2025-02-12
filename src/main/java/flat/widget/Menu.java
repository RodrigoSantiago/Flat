package flat.widget;

import flat.animations.StateInfo;
import flat.uxml.UXAttrs;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.Visibility;
import flat.window.Activity;

import java.util.ArrayList;

public class Menu extends Scene {

    private HorizontalAlign horizontalAlign = HorizontalAlign.CENTER;
    private ArrayList<Widget> orderedList = new ArrayList<>();
    private float targetX, targetY;
    private float[] tempSize;

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
        setHorizontalAlign(attrs.getConstant("horizontal-align", info, getHorizontalAlign()));
    }

    @Override
    public void onMeasure() {
        onMeasureVertical();
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

            if (child == childMenu) continue;

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

    @Override
    public void onLayout(float width, float height) {
        onLayoutVertical(width, height);
        setPosition(targetX, targetY);
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
            if (child.getVisibility() == Visibility.GONE || child == childMenu) continue;

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
            if (child.getVisibility() == Visibility.GONE || child.getMeasureHeight() == MATCH_PARENT || child == childMenu) continue;

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
            if (child.getVisibility() == Visibility.GONE || child.getMeasureHeight() != MATCH_PARENT || child == childMenu) continue;
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
                if (child.getVisibility() == Visibility.GONE || child.getMeasureHeight() != MATCH_PARENT || child == childMenu) continue;

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
            if (child.getVisibility() == Visibility.GONE || child.getMeasureHeight() != MATCH_PARENT || child == childMenu) continue;
            float childMin = totalMinimum == 0 ? 0 : (child.getLayoutMinHeight() / totalMinimum) * minSpace;

            float childWidth = Math.min(Math.min(child.getMeasureWidth(), child.getLayoutMaxWidth()), lWidth);
            float childHeight = childMin + tempSize[j++];
            child.onLayout(childWidth, childHeight);
        }

        // Set Positions
        float totalHeight = 0;
        for (Widget child : orderedList) {
            if (child.getVisibility() == Visibility.GONE || child == childMenu) continue;
            totalHeight += child.getLayoutHeight();
        }

        float xPos = 0;
        float yPos = getPaddingTop() + getMarginTop();

        for (int k = 0; k < orderedList.size(); k++) {
            Widget child = orderedList.get(k);
            if (child.getVisibility() == Visibility.GONE || child == childMenu) continue;

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

    private Menu parentMenu;
    private Menu childMenu;

    protected void showSubMenu(Menu menu, float x, float y) {
        if (childMenu != null) {
            hideSubMenu();
        }
        if (attachAndAddChild(menu)) {
            childMenu = menu;
            childMenu.parentMenu = this;
        }
    }

    protected void hideSubMenu() {
        if (childMenu != null) {
            childMenu.parentMenu = null;
            childMenu.hideSubMenu();
            remove(childMenu);
        }
        childMenu = null;
    }

    public void show(Menu menu, float x, float y) {
        if (menu != null) {
            menu.showSubMenu(this, x, y);

        } else {
            Activity activity = getCurrentActivity();
            if (activity == null) return;

            Scene scene = activity.getScene();
            if (scene == null) return;

            if (getParent() != scene) {
                scene.add(this);
                targetX = x;
                targetY = y;
            }
        }
    }

    public void hide() {
        if (parentMenu != null) {
            if (parentMenu.childMenu == this) {
                parentMenu.hideSubMenu();
            }
            parentMenu = null;

        } else if (getParent() instanceof Scene scene) {
            scene.remove(this);

        }
    }
}
