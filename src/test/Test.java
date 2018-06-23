package test;

import flat.math.operations.Area;
import flat.math.shapes.Path;
import flat.math.shapes.PathIterator;
import flat.math.shapes.Rectangle;
import flat.math.shapes.Shape;

public class Test {
    public static void main(String... args) {
        new Area(new Rectangle(0, 0, 100, 100));
    }

    public static Path rec(Path p, int cx, int cy, int w, int h, boolean rigth) {
        if (rigth) {
            p.moveTo(cx - w / 2f, cy + h / 2f);
            p.lineTo(cx + w / 2f, cy + h / 2f);
            p.lineTo(cx + w / 2f, cy - h / 2f);
            p.lineTo(cx - w / 2f, cy - h / 2f);
            p.closePath();
        } else {
            p.moveTo(cx - w / 2f, cy - h / 2f);
            p.lineTo(cx + w / 2f, cy - h / 2f);
            p.lineTo(cx + w / 2f, cy + h / 2f);
            p.lineTo(cx - w / 2f, cy + h / 2f);
            p.closePath();
        }
        return p;
    }

    public static void printAreas(Shape shape) {
        PathIterator it = shape.pathIterator(null);
        while (!it.isDone()) {
            System.out.println(polygonArea(it));
            it.next();
        }
    }

    public static float polygonArea(PathIterator iterator) {
        float sx = 0, sy = 0, x = 0, y = 0;
        float[] coords = new float[6];
        float area = 0;
        while (!iterator.isDone()) {
            switch (iterator.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO :
                    sx = x = coords[0];
                    sy = y = coords[1];
                    break;
                case PathIterator.SEG_LINETO :
                    area += (x + coords[0]) * (y - coords[1]);
                    x = coords[0];
                    y = coords[1];
                    break;
                case PathIterator.SEG_CLOSE :
                    area += (x + sx) * (y - sy);
                    return area / 2;
            }
            iterator.next();
        }
        return Float.NaN;
    }

}
