package flat.widget.text.area;

class Line {
    int start;
    int length;
    float width;

    public Line(int start, int length) {
        this.start = start;
        this.length = length;
    }

    public void reset(int start, int length) {
        this.start = start;
        this.length = length;
        this.width = 0;
    }
}
