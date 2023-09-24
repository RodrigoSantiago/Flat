package flat.widget.layout;

import flat.uxml.Controller;
import flat.uxml.UXChildren;
import flat.uxml.UXStyleAttrs;
import flat.widget.Gadget;
import flat.widget.Widget;

public final class Cell implements Gadget {

    Widget widget;

    private int c, r, cSpan, rSpan;

    public Cell() {
        cSpan = 1;
        rSpan = 1;
    }

    public Cell(Widget widget, int c, int r, int cSpan, int rSpan) {
        this.widget = widget;
        this.c = c;
        this.r = r;
        this.cSpan = cSpan;
        this.rSpan = rSpan;
    }

    @Override
    public void applyAttributes(UXStyleAttrs style, Controller controller) {
        if (style == null) return;

        c = (int) style.asNumber("column", getColumn());
        r = (int) style.asNumber("row", getRow());
        cSpan = (int) style.asNumber("colspan", getColSpan());
        rSpan = (int) style.asNumber("rowspan", getRowSpan());
    }

    @Override
    public void applyChildren(UXChildren children) {
        Gadget child;
        if ((child = children.next()) != null) {
            widget = child.getWidget();
        }
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public Widget getWidget() {
        return widget;
    }

    public int getColumn() {
        return c;
    }

    public int getRow() {
        return r;
    }

    public int getColSpan() {
        return cSpan;
    }

    public void setColumnSpan(int cSpan) {
        this.cSpan = cSpan;
    }

    public int getRowSpan() {
        return rSpan;
    }

    public void setRowSpan(int rSpan) {
        this.rSpan = rSpan;
    }
}
