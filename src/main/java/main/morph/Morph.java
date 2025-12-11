package main.morph;

import flat.backend.SVG;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.graphics.image.ImageData;
import flat.math.Affine;
import flat.math.Mathf;
import flat.math.Vector2;
import flat.math.shapes.*;

import java.util.ArrayList;
import java.util.Collections;

public class Morph {
    ImageData img;
    boolean[] pass;
    int width;
    int height;
    
    ArrayList<PLine> lines = new ArrayList<>();
    
    public Morph(ImageData img) {
        this.img = img;
        width = img.getWidth();
        height = img.getHeight();
    }
    
    public ArrayList<Path> toPaths() {
        for (int xx = 0; xx < width; xx++) {
            for (int yy = 0; yy < height; yy++) {
                buildPixelLine(xx, yy);
            }
        }
        
        Path a = new Path();
        a.moveTo(11, 16);
        a.lineTo(11.5f, 16.5f);
        a.moveTo(11, 17);
        a.lineTo(11.5f, 17.5f);
        for (var line : lines) {
            if (line.x1 == 11 && line.y1 == 16 && line.x2 == 11 && line.y2 == 17) {
                a.moveTo(line.x1, line.y1);
                a.lineTo(line.x2, line.y2);
            }
            if ((line.x1 == 11 && line.y1 == 16) || (line.x2 == 11 && line.y2 == 17) ||
                        (line.x2 == 11 && line.y2 == 16) || (line.x1 == 11 && line.y1 == 17)) {
                a.moveTo(line.x1, line.y1);
                a.lineTo(line.x2, line.y2);
            }
        }
        //if (true) return new ArrayList<>(List.of(a));
        
        ArrayList<ArrayList<PLine>> polygons = new ArrayList<>();
        while (!lines.isEmpty()) {
            ArrayList<PLine> polygon = new ArrayList<>();
            PLine startLine = lines.remove(0);
            PLine curLine = startLine;
            polygon.add(curLine);
            while (polygon.size() < 3 || !curLine.isLink(startLine)) {
                boolean found = false;
                for (int i = 0; i < lines.size(); i++) {
                    var line = lines.get(i);
                    if (line.isNoLink(curLine)) continue;
                    if (curLine.isLink(line)) {
                        curLine = lines.remove(i);
                        polygon.add(curLine);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    break;
                }
            }
            polygons.add(polygon);
        }
        
        ArrayList<Path> paths = new ArrayList<>();
        for (var polygon : polygons) {
            Path path = new Path();
            PLine prev = polygon.get(0);
            for (int i = 1; i < polygon.size(); i++) {
                var line = polygon.get(i);
                if ((line.x1 == prev.x1 && line.y1 == prev.y1) ||
                            (line.x2 == prev.x1 && line.y2 == prev.y1)) {
                    if (i == 1) path.moveTo(prev.x2, prev.y2);
                    path.lineTo(prev.x1, prev.y1);
                } else {
                    if (i == 1) path.moveTo(prev.x1, prev.y1);
                    path.lineTo(prev.x2, prev.y2);
                }
                prev = line;
            }
            path.closePath();
            paths.add(path);
        }
        
        return paths;
    }
    
    private int getColor(int x, int y) {
        if (x < 0 || x>= width || y < 0 || y >= height) return 0;
        int coord = (x + y * width) * 4;
        int r = img.getData()[coord] & 0xFF;
        int g = img.getData()[coord + 1] & 0xFF;
        int b = img.getData()[coord + 2] & 0xFF;
        int a = img.getData()[coord + 3] & 0xFF;
        return a;//Color.rgbaToColor(r, g, b, a);
    }
    
    private void buildPixelLine(int x, int y) {
        int col = getColor(x, y);
        if (col == 0) return;
        
        int colTop   = getColor(x, y - 1);
        int colLeft  = getColor(x - 1, y);
        int colRight = getColor(x + 1, y);
        int colBot   = getColor(x, y + 1);
        int colTopLeft  = getColor(x - 1, y - 1);
        int colTopRight = getColor(x + 1, y - 1);
        int colBotLeft  = getColor(x - 1, y + 1);
        int colBotRight = getColor(x + 1, y + 1);
        
        PLine lineTop = null;
        PLine lineLeft = null;
        PLine lineRight = null;
        PLine lineBot = null;
        if (colTop == 0) {
            lines.add(lineTop = new PLine(x, y, x + 1, y));
        }
        if (colLeft == 0) {
            lines.add(lineLeft = new PLine(x, y, x, y + 1));
        }
        if (colRight == 0) {
            lines.add(lineRight = new PLine(x + 1, y, x + 1, y + 1));
        }
        if (colBot == 0) {
            lines.add(lineBot = new PLine(x, y + 1, x + 1, y + 1));
        }
        if (colTopLeft != 0) {
            if (colTop == 0 && colLeft == 0) {
                lineTop.noLink(lineLeft);
            }
        }
        if (colTopRight != 0) {
            if (colTop == 0 && colRight == 0) {
                lineTop.noLink(lineRight);
            }
        }
        if (colBotLeft != 0) {
            if (colBot == 0 && colLeft == 0) {
                lineBot.noLink(lineLeft);
            }
        }
        if (colBotRight != 0) {
            if (colBot == 0 && colRight == 0) {
                lineBot.noLink(lineRight);
            }
        }
    }
    
    private static class PLine {
        protected int x1;
        protected int y1;
        protected int x2;
        protected int y2;
        private ArrayList<PLine> noLink;
        
        public PLine(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
        
        public void noLink(PLine other) {
            if (noLink == null) {
                noLink = new ArrayList<>(2);
            }
            if (!noLink.contains(other)) {
                noLink.add(other);
                other.noLink(this);
            }
        }
        
        public boolean isNoLink(PLine line) {
            return noLink != null && noLink.contains(line);
        }
        
        public PLine hasNolinkTouch(PLine line) {
            if (noLink != null) {
                for (var l : noLink) {
                    if (l.isLink(line.x1, line.y1, line.x2, line.y2)) return l;
                }
            }
            return null;
        }
        
        public boolean isLink(PLine line) {
            if ((line.x1 == x1 && line.y1 == y1) ||
                        (line.x2 == x1 && line.y2 == y1) ||
                        (line.x1 == x2 && line.y1 == y2) ||
                        (line.x2 == x2 && line.y2 == y2)) {
                if (line.noLink != null) {
                    var touch = line.hasNolinkTouch(this);
                    if (touch != null) {
                        if (Math.abs(x1 - x2) == Math.abs(line.x1 - line.x2) &&
                                    Math.abs(y1 - y2) == Math.abs(line.y1 - line.y2)) {
                            return false;
                        }
                    }
                }
                return true;
            }
            return false;
        }
        
        public boolean isLink(int lx1, int ly1, int lx2, int ly2) {
            return (lx1 == x1 && ly1 == y1) ||
                           (lx2 == x1 && ly2 == y1) ||
                           (lx1 == x2 && ly1 == y2) ||
                           (lx2 == x2 && ly2 == y2);
        }
        
        @Override
        public String toString() {
            return x1 + ", " + y1 + " -- " + x2 + ", " + y2;
        }
    }
    
    
    public static void generateTrainingDataset(Graphics graphics, float t) {
        // move   0, to right, to bottom, to top right, to bottom right
        Vector2[][] movePair = {
                {new Vector2(0, 0), new Vector2(0, 0)},
                {new Vector2(-1, 0), new Vector2(1, 0)},
                {new Vector2(0, -1), new Vector2(0, 1)},
                {new Vector2(-1, -1), new Vector2(1, 1)},
                {new Vector2(-1,  1), new Vector2(1, -1)},
        };
        // rotate 0 45 90 -45 -90
        float[] angles = {0, 45, 90, -45, -90, 180};
        // scale  1 - 1, 1 - 2, 1 - 0.25
        float[] scale = {1, 2, 0.25f};
        // fill   fill, outline, fill + outline, double fill, double fill + outline
        int[][] fillPair = {
                {1, 1, 0}, {0, 0, 1}, {1, 1, 2}, { 1, 2, 0}, {1, 2, 3}
        };
        // shape  rect, ellipse, star, L, cross, pacman, line, spline, triangle, heart
        Shape[] shapes = {
                new Rectangle(-1, -1, 2, 2),
                new Ellipse(-1, -1, 2, 2),
                makeStar(),
                makeL(),
                makeCross(),
                makePac(),
                makeTriangle(),
                makeHeart()
        };
        
        ArrayList<Vector2>[] polygons = new ArrayList[shapes.length];
        for (int i = 0; i < polygons.length; i++) {
            polygons[i] = circlePath(toPoints(shapes[i]));
        }
        
        ArrayList<Vector2> target = new ArrayList<>();
        for (int i = 0; i < polygons[0].size(); i++) {
            target.add(new Vector2());
        }
        graphics.setTransform2D(null);
        graphics.setColor(Color.red);
        for (int i = 0; i < polygons.length; i++) {
            graphics.setTransform2D(new Affine().translate(64 * i + 128, 128).scale(32, 32));
            morphTo(polygons[i], polygons[(i + 5) % polygons.length], target, t);
            for (int j = 0; j < polygons[i].size(); j++) {
                graphics.drawCircle(target.get(j).x, target.get(j).y, 0.05f, true);
            }
            graphics.drawShape(toShape(target), true);
        }
    }
    private static float t;
    
    private static void offsetFor(ArrayList<Vector2> src, ArrayList<Vector2> dst) {
        int minOffset = 0;
        float minLoss = Float.MAX_VALUE;
        for (int offSet = 0; offSet < src.size(); offSet++) {
            float totalDist = 0;
            for (int i = 0; i < src.size(); i++) {
                var pt1 = src.get((i + offSet) % src.size());
                var pt2 = dst.get((i + offSet) % src.size());
                totalDist += pt1.distance(pt2);
            }
            float avr = totalDist / src.size();
            float loss = 0;
            for (int i = 0; i < src.size(); i++) {
                var pt1 = src.get((i + offSet) % src.size());
                var pt2 = dst.get((i + offSet) % src.size());
                loss += Math.abs(pt1.distance(pt2) - avr);
            }
            if (loss <= minLoss) {
                minLoss = loss;
                minOffset = offSet;
            }
        }
        for (int i = 0; i < minOffset; i++) {
            src.add(src.remove(0));
        }
    }
    
    private static void morphToA(ArrayList<Vector2> src, ArrayList<Vector2> dst, ArrayList<Vector2> target, float t) {
        for (int i = 0; i < src.size(); i++) {
            var p1 = src.get(i);
            float md = Float.MAX_VALUE;
            int p = i;
            for (int j = 0; j < dst.size(); j++) {
                float d = dst.get(j).distance(p1);
                if (d < md) {
                    md = d;
                    p = j;
                }
            }
            target.get(i).x = Mathf.lerp(src.get(i).x, dst.get(p).x, t);
            target.get(i).y = Mathf.lerp(src.get(i).y, dst.get(p).y, t);
        }
    }
    
    private static void morphTo(ArrayList<Vector2> src, ArrayList<Vector2> dst, ArrayList<Vector2> target, float t) {
        for (int i = 0; i < src.size(); i++) {
            target.get(i).x = Mathf.lerp(src.get(i).x, dst.get(i).x, t);
            target.get(i).y = Mathf.lerp(src.get(i).y, dst.get(i).y, t);
        }
    }
    
    private static ArrayList<Vector2> circlePath(ArrayList<Vector2> polygon) {
        ArrayList<Vector2> circle = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            float angle = 2f * Mathf.PI * i / 64f;
            circle.add(new Vector2(Mathf.cos(angle) * 3, Mathf.sin(angle) * 3));
        }
        
        ArrayList<Vector2> projectedPoints = new ArrayList<>();
        
        for (Vector2 p : circle) {
            Vector2 dir = new Vector2(p).normalize();
            Vector2 closest = null;
            float minDist = Float.MAX_VALUE;
            
            for (int i = 0; i < polygon.size(); i++) {
                Vector2 a = polygon.get(i);
                Vector2 b = polygon.get((i + 1) % polygon.size());
                Vector2 inter = col(p.x, p.y, -p.x, -p.y, a.x, a.y, b.x, b.y);
                if (inter != null) {
                    float dist = p.distance(inter);
                    if (dist < minDist) {
                        minDist = dist;
                        closest = inter;
                    }
                }
            }
            projectedPoints.add(closest != null ? closest : p);
        }
        return projectedPoints;
    }
    
    public static Vector2 col(float x1, float y1, float x2, float y2,
                                        float x3, float y3, float x4, float y4) {
        float denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (denom == 0) {
            return null; // Linhas paralelas ou coincidentes
        }
        
        float px = ((x1 * y2 - y1 * x2) * (x3 - x4) -
                            (x1 - x2) * (x3 * y4 - y3 * x4)) / denom;
        float py = ((x1 * y2 - y1 * x2) * (y3 - y4) -
                            (y1 - y2) * (x3 * y4 - y3 * x4)) / denom;
        
        // Verifica se o ponto está dentro dos segmentos
        if (entre(px, x1, x2) && entre(py, y1, y2) &&
            entre(px, x3, x4) && entre(py, y3, y4)) {
            return new Vector2(px, py);
        }
        
        return null;
    }
    
    // Verifica se c está entre a e b
    private static boolean entre(float c, float a, float b) {
        return c >= Math.min(a, b) - 0.0001f && c <= Math.max(a, b) + 0.0001f;
    }
    
    private static Path makeStar() {
        float outerRadius = 1f;
        float innerRadius = outerRadius * Mathf.sin(Mathf.toRadians(18)) / Mathf.sin(Mathf.toRadians(54));
        
        Path star = new Path();
        for (int i = 0; i < 10; i++) {
            float r = (i % 2 == 0) ? outerRadius : innerRadius;
            float x = r * Mathf.cos(Mathf.toRadians(-90 + i * 36));
            float y = r * Mathf.sin(Mathf.toRadians(-90 + i * 36));
            
            if (i == 0) {
                star.moveTo(x, y);
            } else {
                star.lineTo(x, y);
            }
        }
        star.closePath();
        return star;
    }
    
    private static Path makeL() {
        Path path = new Path();
        path.moveTo(-1, -1);
        path.lineTo(-0.5f, -1);
        path.lineTo(-0.5f, 0.5f);
        path.lineTo(1.0f, 0.5f);
        path.lineTo(1.0f, 1.0f);
        path.lineTo(-1.0f, 1.0f);
        path.closePath();
        path = new Path(path.pathIterator(new Affine().translate(0.5f, -0.5f)));
        return path;
    }
    
    private static Path makeCross() {
        Path path = new Path();
        path.moveTo(-0.1f, -1.0f);
        path.lineTo( 0.1f, -1.0f);
        path.lineTo( 0.1f, -0.1f);
        path.lineTo( 1.0f, -0.1f);
        path.lineTo( 1.0f,  0.1f);
        path.lineTo( 0.1f,  0.1f);
        path.lineTo( 0.1f,  1.0f);
        path.lineTo(-0.1f,  1.0f);
        path.lineTo(-0.1f,  0.1f);
        path.lineTo(-1.0f,  0.1f);
        path.lineTo(-1.0f, -0.1f);
        path.lineTo(-0.1f, -0.1f);
        path.closePath();
        return path;
    }
    
    private static Path makePac() {
        var shape = new Arc(-1, -1, 2, 2, 45, 270, Arc.Type.PIE);
        return new Path(shape.pathIterator(new Affine().translate(0.2f, -0.01f)));
    }
    
    private static Path makeTriangle() {
        Path path = new Path();
        path.moveTo(-1, -0.5f);
        path.lineTo(1f, -0.5f);
        path.lineTo(-1f, 1.0f);
        path.closePath();
        return path;
    }
    
    private static Path makeHeart() {
        float x = 0;
        float y = 0;
        float size = 0.8f;
        float sizey = 1.0f;
        
        Path path = new Path();
        path.moveTo(x, y + sizey);
        path.curveTo(
                x - size * 2, y + sizey * 0.5f,
                x - size, y - sizey,
                x, y - sizey * 0.3f
        );
        path.curveTo(
                x + size, y - sizey,
                x + size * 2, y + sizey * 0.5f,
                x, y + sizey
        );
        path.closePath();
        return path;
    }
    
    private static void splitBigger(ArrayList<Vector2> ptrs) {
        float d = -1;
        int id = 0;
        for (int i = 0; i < ptrs.size(); i++) {
            var pt1 = ptrs.get(i);
            var pt2 = ptrs.get((i + 1) % ptrs.size());
            float dist = pt1.distance(pt2);
            if (d < 0 || dist > d) {
                d = dist;
                id = i;
            }
        }
        var pt1 = ptrs.get(id);
        var pt2 = ptrs.get((id + 1) % ptrs.size());
        Vector2 result = new Vector2(pt1).add(pt2).mul(0.5f);
        ptrs.add(id + 1, result);
    }
    
    private static Path toShape(ArrayList<Vector2> points) {
        Path path = new Path();
        for (int i = 0; i < points.size(); i++) {
            if (i == 0) {
                path.moveTo(points.get(i).x, points.get(i).y);
            } else {
                path.lineTo(points.get(i).x, points.get(i).y);
            }
        }
        path.closePath();
        return path;
    }
    
    private static ArrayList<Vector2> toPoints(Shape path) {
        ArrayList<Vector2> points = new ArrayList<>();
        float[] data = new float[6];
        var pi = path.pathIterator(null);
        float px = 0, py = 0;
        while (!pi.isDone()) {
            switch (pi.currentSegment(data)) {
                case PathIterator.SEG_MOVETO:
                    points.add(new Vector2(px = data[0], py = data[1]));
                    break;
                case PathIterator.SEG_LINETO:
                    points.add(new Vector2(px = data[0], py = data[1]));
                    break;
                case PathIterator.SEG_QUADTO:
                    for (int i = 0; i < 10; i++) {
                        points.add(QuadCurve.getPointAt(px, py, data[0], data[1], data[2], data[3], ((i + 1) / 10f)));
                    }
                    px = data[2];
                    py = data[3];
                    break;
                case PathIterator.SEG_CUBICTO:
                    for (int i = 0; i < 10; i++) {
                        points.add(CubicCurve.getPointAt(px, py, data[0], data[1], data[2], data[3], data[4], data[5], ((i + 1) / 10f)));
                    }
                    px = data[4];
                    py = data[5];
                    break;
                case PathIterator.SEG_CLOSE:
                    break;
            }
            pi.next();
        }
        return points;
    }
    
    public static boolean getOrientation(ArrayList<Vector2> points) {
        float sum = 0;
        int n = points.size();
        
        for (int i = 0; i < n; i++) {
            Vector2 current = points.get(i);
            Vector2 next = points.get((i + 1) % n); // wrap around
            sum += (next.x - current.x) * (next.y + current.y);
        }
        
        return sum > 0;
    }
}
