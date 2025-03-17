package flat.graphics.context.paints;

import flat.backend.SVG;
import flat.graphics.Color;
import flat.graphics.context.Paint;

public class ColorPaint extends Paint {
    private final int color;

    public ColorPaint(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    @Override
    protected void setInternal(long svgId) {
        SVG.SetPaintColor(svgId, color);
    }

    @Override
    public Paint multiply(int color) {
        return color == -1 ? this : new ColorPaint(Color.multiply(this.color, color));
    }
}
