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

    protected void add(List<Widget> children) {
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

    public void removeAll() {
        List<Widget> children = getChildren();
        if (children != null) {
            int size;
            while ((size = children.size()) > 0) {
                remove(children.get(children.size() - 1));

                if (children.size() >= size) {
                    // UNEXPECTED ADDITION
                    break;
                }
            }
        }
    }

    protected void setChildrenLayout(float x, float y, float w, float h) {
        for (Widget child : getChildrenIterable()) {
            if (child.getVisibility() == Visibility.GONE) continue;

            child.onLayout(Math.min(child.getMeasureWidth(), w), Math.min(child.getMeasureHeight(), h));
            child.setPosition(x, y);
        }
    }

    protected void layoutHelperHorizontal(Children<?> children, float x, float y, float w, float h, Align.Vertical valign) {
        float defSpace = 0, defMinSpace = 0;
        for (Widget child : children) {
            if (child.getVisibility() == Visibility.GONE) continue;

            float mes = child.getMeasureWidth();
            float min = child.getTotalMinWidth();
            if (mes != MATCH_PARENT) defSpace += mes;
            if (min != MATCH_PARENT) defMinSpace += min;
        }
        if (defMinSpace >= w) {
            float mul = w / defMinSpace;

            for (Widget child : children) {
                if (child.getVisibility() == Visibility.GONE) continue;

                float min = child.getTotalMinWidth();
                if (min == MATCH_PARENT) min = 0;

                child.onLayout(min * mul, Math.min(child.getMeasureHeight(), h));
            }
        } else if (defSpace >= w) {
            float mul = (w - defMinSpace) / (defSpace - defMinSpace);

            for (Widget child : children) {
                if (child.getVisibility() == Visibility.GONE) continue;

                float mes = child.getMeasureWidth();
                float min = child.getTotalMinWidth();
                if (mes == MATCH_PARENT) mes = 0;
                if (min == MATCH_PARENT) min = 0;

                child.onLayout(min + Math.max(0, mes - min) * mul, Math.min(child.getMeasureHeight(), h));
            }
        } else {
            int undefCount = 0;
            float undefMinSpace = 0;
            float reamingW = w;

            // Defined Space
            for (Widget child : children) {
                if (child.getVisibility() == Visibility.GONE) continue;

                if (child.getMeasureWidth() == MATCH_PARENT) {
                    undefCount++;
                    float min = child.getTotalMinWidth();
                    if (min != MATCH_PARENT) undefMinSpace += min;
                } else {
                    child.onLayout(child.getMeasureWidth(), Math.min(child.getMeasureHeight(), h));
                    reamingW -= child.lWidth();
                }
            }

            // Undefined Space
            for (Widget child : children) {
                if (child.getVisibility() == Visibility.GONE) continue;

                if (child.getMeasureWidth() == MATCH_PARENT) {
                    float min = child.getTotalMinWidth();
                    if (min == MATCH_PARENT) min = 0;

                    child.onLayout(min + (reamingW - undefMinSpace) / undefCount, Math.min(child.getMeasureHeight(), h));
                    reamingW -= child.lWidth();
                    undefMinSpace -= min;
                    undefCount--;
                }
            }
        }

        for (Widget child : children) {
            child.setPosition(x, yOff(child.lHeight(), y, y + h, valign));
            x += child.lWidth();
        }
    }

    protected void layoutHelperVertical(Children<Widget> children, float x, float y, float w, float h, Align.Horizontal halign) {

        float defSpace = 0, defMinSpace = 0;
        for (Widget child : children) {
            if (child.getVisibility() == Visibility.GONE) continue;

            float mes = child.getMeasureHeight();
            float min = child.getTotalMinHeight();
            if (mes != MATCH_PARENT) defSpace += mes;
            if (min != MATCH_PARENT) defMinSpace += min;
        }
        if (defMinSpace >= h) {
            float mul = h / defMinSpace;

            for (Widget child : children) {
                if (child.getVisibility() == Visibility.GONE) continue;

                float min = child.getTotalMinHeight();
                if (min == MATCH_PARENT) min = 0;

                child.onLayout(Math.min(child.getMeasureWidth(), w), min * mul);
            }
        } else if (defSpace >= h) {
            float mul = (h - defMinSpace) / (defSpace - defMinSpace);

            for (Widget child : children) {
                if (child.getVisibility() == Visibility.GONE) continue;

                float mes = child.getMeasureHeight();
                float min = child.getTotalMinHeight();
                if (mes == MATCH_PARENT) mes = 0;
                if (min == MATCH_PARENT) min = 0;

                child.onLayout(Math.min(child.getMeasureWidth(), w), min + Math.max(0, mes - min) * mul);
            }
        } else {
            int undefCount = 0;
            float undefMinSpace = 0;
            float reamingH = h;

            // Defined Space
            for (Widget child : children) {
                if (child.getVisibility() == Visibility.GONE) continue;

                if (child.getMeasureHeight() == MATCH_PARENT) {
                    undefCount++;
                    float min = child.getTotalMinHeight();
                    if (min != MATCH_PARENT) undefMinSpace += min;
                } else {
                    child.onLayout(Math.min(child.getMeasureWidth(), w), child.getMeasureHeight());
                    reamingH -= child.lHeight();
                }
            }

            // Undefined Space
            for (Widget child : children) {
                if (child.getVisibility() == Visibility.GONE) continue;

                if (child.getMeasureHeight() == MATCH_PARENT) {
                    float min = child.getTotalMinHeight();
                    if (min == MATCH_PARENT) min = 0;

                    child.onLayout(Math.min(child.getMeasureWidth(), w), min + (reamingH - undefMinSpace) / undefCount);
                    reamingH -= child.lHeight();
                    undefMinSpace -= min;
                    undefCount--;
                }
            }
        }

        for (Widget child : children) {
            child.setPosition(xOff(child.lWidth(), x, x + w, halign), y);
            y += child.lHeight();
        }
    }

    protected static float xOff(float childWidth, float sx, float ex, Align.Horizontal halign) {
        float start = sx;
        float end = ex;
        if (end < start) return (start + end) / 2f;
        if (halign == Align.Horizontal.RIGHT) return end - childWidth;
        if (halign == Align.Horizontal.CENTER) return (start + end - childWidth) / 2f;
        return start;
    }

    protected static float yOff(float childHeight, float sy, float ey, Align.Vertical valign) {
        float start = sy;
        float end = ey;
        if (end < start) return (start + end) / 2f;
        if (valign == Align.Vertical.BOTTOM || valign == Align.Vertical.BASELINE) return end - childHeight;
        if (valign == Align.Vertical.MIDDLE) return (start + end - childHeight) / 2f;
        return start;
    }
}