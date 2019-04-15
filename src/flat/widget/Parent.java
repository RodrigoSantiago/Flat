package flat.widget;

import flat.graphics.text.Align;
import flat.widget.enuns.Visibility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Parent extends Widget {

    public Parent() {
        children = new ArrayList<>();
        unmodifiableChildren = Collections.unmodifiableList(children);
    }

    @Override
    protected ArrayList<Widget> getChildren() {
        return super.getChildren();
    }

    // add child - children list unaltered
    protected void attachChildren(Widget child) {
        child.setParent(this);
    }

    // remove child - children list unaltered
    protected void detachChildren(Widget child) {
        if (child.parent == this) {
            child.setParent(null);
        }
    }

    protected void add(Widget child) {
        child.setParent(this);
        children.add(child);
        invalidateChildrenOrder();
        invalidate(true);
    }

    protected void add(Widget... children) {
        for (Widget child : children) {
            add(child);
        }
    }

    public void remove(Widget widget) {
        children.remove(widget);
        if (widget.parent == this) {
            widget.setParent(null);
        }
        invalidateChildrenOrder();
        invalidate(true);
    }

    protected static void layoutHelperBox(List<? extends Widget> children, float x, float y, float w, float h) {
        for (Widget child : children) {
            if (child.getVisibility() == Visibility.Gone) continue;

            child.onLayout(w, h);
            child.setPosition(x, y);
        }
    }

    protected static void layoutHelperHorizontal(List<? extends Widget> children, float x, float y, float w, float h,
                                                 Align.Vertical valign) {

        float definedSpace = 0, definedMinSpace = 0;
        for (Widget child : children) {
            if (child.getVisibility() == Visibility.Gone) continue;

            float mes = child.getMeasureWidth();
            float min = child.getLayoutMinWidth();
            if (mes != MATCH_PARENT) definedSpace += mes;
            if (min != MATCH_PARENT) definedMinSpace += min;
        }
        if (definedMinSpace >= w) {
            // Divide Space
            for (Widget child : children) {
                if (child.getVisibility() == Visibility.Gone) continue;

                float min = child.getLayoutMinWidth();
                if (min == MATCH_PARENT) min = 0;

                child.onLayout(min * w / definedMinSpace, h);
            }
        } else if (definedSpace >= w) {
            // Divide Space
            for (Widget child : children) {
                if (child.getVisibility() == Visibility.Gone) continue;

                float mes = child.getMeasureWidth();
                float min = child.getLayoutMinWidth();
                if (mes == MATCH_PARENT) mes = 0;
                if (min == MATCH_PARENT) min = 0;

                child.onLayout(min + Math.max(0, mes - min) / (definedSpace - definedMinSpace) * (w - definedMinSpace), h);
            }
        } else {
            // Defined Space
            int match_parent_count = 0;
            float undefinedMinSpace = 0;
            float reamingW = w;
            for (Widget child : children) {
                if (child.getVisibility() == Visibility.Gone) continue;

                if (child.getMeasureWidth() == MATCH_PARENT) {
                    match_parent_count++;
                    float min = child.getLayoutMinWidth();
                    if (min != MATCH_PARENT) undefinedMinSpace += min;
                } else {
                    child.onLayout(child.getMeasureWidth(), h);
                    reamingW -= child.getWidth();
                }
            }
            // Undefined Space
            for (Widget child : children) {
                if (child.getVisibility() == Visibility.Gone) continue;

                if (child.getMeasureWidth() == MATCH_PARENT) {
                    float min = child.getLayoutMinWidth();
                    if (min == MATCH_PARENT) min = 0;

                    child.onLayout(min + (reamingW - undefinedMinSpace) / match_parent_count, h);
                    undefinedMinSpace -= min;
                    reamingW -= child.getWidth();
                    match_parent_count--;
                }
            }
        }
        float xoff = x;
        for (Widget child : children) {
            child.setPosition(xoff, yOff(child.getHeight(), y, y + h, valign));
            xoff += child.getWidth();
        }
    }

    protected static void layoutHelperVertical(List<? extends Widget> children, float x, float y, float w, float h,
                                               Align.Horizontal halign) {

        float definedSpace = 0, definedMinSpace = 0;
        for (Widget child : children) {
            if (child.getVisibility() == Visibility.Gone) continue;

            float mes = child.getMeasureHeight();
            float min = child.getLayoutMinHeight();
            if (mes != MATCH_PARENT) definedSpace += mes;
            else if (min != MATCH_PARENT) definedSpace += min;
            if (min != MATCH_PARENT) definedMinSpace += min;
        }
        if (definedMinSpace > h) {
            // Divide Space
            for (Widget child : children) {
                if (child.getVisibility() == Visibility.Gone) continue;

                float min = child.getLayoutMinHeight();
                if (min == MATCH_PARENT) min = 0;

                child.onLayout(w, min * h / definedMinSpace);
            }
        } else if (definedSpace > h) {
            // Divide Space
            for (Widget child : children) {
                if (child.getVisibility() == Visibility.Gone) continue;

                float mes = child.getMeasureHeight();
                float min = child.getLayoutMinHeight();
                if (mes == MATCH_PARENT) mes = 0;
                if (min == MATCH_PARENT) min = 0;

                child.onLayout(w, min + Math.max(0, mes - min) / (definedSpace - definedMinSpace) * (h - definedMinSpace));
            }
        } else {
            // Defined Space
            int match_parent_count = 0;
            float undefinedMinSpace = 0;
            float reamingH = h;
            for (Widget child : children) {
                if (child.getVisibility() == Visibility.Gone) continue;

                if (child.getMeasureHeight() == MATCH_PARENT) {
                    match_parent_count++;
                    float min = child.getLayoutMinHeight();
                    if (min != MATCH_PARENT) undefinedMinSpace += min;
                } else {
                    child.onLayout(w, child.getMeasureHeight());
                    reamingH -= child.getHeight();
                }
            }
            // Undefined Space
            for (Widget child : children) {
                if (child.getVisibility() == Visibility.Gone) continue;

                if (child.getMeasureHeight() == MATCH_PARENT) {
                    float min = child.getLayoutMinHeight();
                    if (min == MATCH_PARENT) min = 0;

                    child.onLayout(w, min + (reamingH - undefinedMinSpace) / match_parent_count);
                    undefinedMinSpace -= min;
                    reamingH -= child.getHeight();
                    match_parent_count--;
                }
            }
        }
        float yoff = y;
        for (Widget child : children) {
            child.setPosition(xOff(child.getWidth(), x, x + w, halign), yoff);
            yoff += child.getHeight();
        }
    }

    private static float xOff(float childWidth, float sx, float ex, Align.Horizontal halign) {
        float start = sx;
        float end = ex;
        if (end < start) return (start + end) / 2f;
        if (halign == Align.Horizontal.RIGHT) return end - childWidth;
        if (halign == Align.Horizontal.CENTER) return (start + end - childWidth) / 2f;
        return start;
    }

    private static float yOff(float childHeight, float sy, float ey, Align.Vertical valign) {
        float start = sy;
        float end = ey;
        if (end < start) return (start + end) / 2f;
        if (valign == Align.Vertical.BOTTOM || valign == Align.Vertical.BASELINE) return end - childHeight;
        if (valign == Align.Vertical.MIDDLE) return (start + end - childHeight) / 2f;
        return start;
    }
}