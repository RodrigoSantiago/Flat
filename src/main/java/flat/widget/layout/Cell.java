package flat.widget.layout;

import flat.widget.Widget;

public class Cell {
    final Widget widget;
    final int x, y, w, h;

    public Cell(Widget widget, int x, int y, int w, int h) {
        this.widget = widget;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public Widget getWidget() {
        return widget;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }
}
