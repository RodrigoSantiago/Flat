package flat.widget.layout;

import flat.uxml.*;
import flat.uxml.value.UXValue;
import flat.widget.Gadget;
import flat.widget.Widget;

import java.util.HashMap;

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
    public void setAttributes(HashMap<Integer, UXValue> attributes, String style) {
        // TODO
    }

    @Override
    public void applyAttributes(UXTheme theme, Controller controller, UXBuilder builder) {
        if (theme == null) return;

        /*c = (int) theme.asNumber("column", getColumn());
        r = (int) theme.asNumber("row", getRow());
        cSpan = (int) theme.asNumber("colspan", getColSpan());
        rSpan = (int) theme.asNumber("rowspan", getRowSpan());*/
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
