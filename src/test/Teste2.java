package test;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;

public class Teste2 {
    public static void test() {
        Rectangle rec = new Rectangle(0, 0, 100, 100);
        Rectangle rec2 = new Rectangle(25, 25, 50, 50);
        Rectangle rec3 = new Rectangle(300, 0, 100, 100);
        Rectangle rec4 = new Rectangle(325, 25, 25, 50);
        Rectangle rec5 = new Rectangle(351, 25, 24, 50);

        Area area = new Area(rec);
        Area area2 = new Area(rec2);
        Area area3 = new Area(rec3);
        Area area4 = new Area(rec4);
        Area area5 = new Area(rec5);

        area.add(area3);
        area.subtract(area2);
        area.subtract(area4);
        area.subtract(area5);

        PathIterator it = area.getPathIterator(null);
        float[] coords = new float[6];
        while (!it.isDone()) {
            switch (it.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO:
                    System.out.println("move : " + coords[0] + "," + coords[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    System.out.println("line : " + coords[0] + "," + coords[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    System.out.println("quad : " + coords[0] + "," + coords[1] + "," + coords[2] + "," + coords[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    System.out.println("cubic : " + coords[0] + "," + coords[1] + "," + coords[2] + "," + coords[3] + "," + coords[4] + "," + coords[5]);
                    break;
                case PathIterator.SEG_CLOSE:
                    System.out.println("close");
                    break;
            }
            it.next();
        }
    }
}
