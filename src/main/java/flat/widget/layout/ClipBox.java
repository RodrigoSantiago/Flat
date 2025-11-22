package flat.widget.layout;

import flat.graphics.Graphics;
import flat.math.Vector2;
import flat.math.shapes.Rectangle;
import flat.uxml.UXChildren;
import flat.widget.Parent;
import flat.widget.Widget;
import flat.widget.enums.Visibility;

import java.util.List;

public class ClipBox extends Parent {
    
    boolean clip = false;
    Vector2[] pt = {
            new Vector2(), new Vector2(), new Vector2(), new Vector2()
    };
    Rectangle bb = new Rectangle();
    Rectangle cbb = new Rectangle();
    
    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        for (var child : children) {
            add(child.getWidget());
        }
    }

    @Override
    public void onMeasure() {
        performMeasureStack();
    }

    @Override
    public void onLayout(float width, float height) {
        setLayout(width, height);
        performLayoutUnbounded(getInWidth(), getInHeight());
        validateClip();
        fireLayout();
    }

    @Override
    public boolean onLayoutSingleChild(Widget child) {
        if (getChildren().contains(child)) {
            child.onMeasure();
            performSingleLayoutUnbounded(getInWidth(), getInHeight(), child);
            validateClip();
            return true;
        }
        return false;
    }
    
    private void validateClip() {
        var affine = getTransform();
        var shape = getBackgroundShape();
        float rX = Math.max(getRadiusTop(), Math.max(getRadiusLeft(), Math.max(getRadiusRight(), getRadiusBottom())));
        if (rX > shape.width - rX || rX > shape.height - rX) {
            clip = true;
            return;
        }
        
        pt[0].set(shape.x + rX, shape.y + rX);
        pt[1].set(shape.x + shape.width - rX, shape.y + rX);
        pt[2].set(shape.x + shape.width - rX, shape.y + shape.width - rX);
        pt[3].set(shape.x + rX, shape.y + shape.height - rX);
        for (int i = 0; i < 4; i++) {
            affine.transform(pt[i]);
        }
        setBoundingBox(bb);
        
        clip = false;
        for (Widget child : getChildrenIterable()) {
            if (child.getVisibility() == Visibility.VISIBLE) {
                var ct = child.getTransform();
                pt[0].set(child.getOutX(), child.getOutY());
                pt[1].set(child.getOutX() + child.getOutWidth(), child.getOutY());
                pt[2].set(child.getOutX() + child.getOutWidth(), child.getOutY() + child.getOutWidth());
                pt[3].set(child.getOutX(), child.getOutY() + child.getOutWidth());
                for (int i = 0; i < 4; i++) {
                    ct.transform(pt[i]);
                }
                setBoundingBox(cbb);
                if (!bb.contains(cbb)) {
                    clip = true;
                    break;
                }
            }
        }
    }
    
    private void setBoundingBox(Rectangle rect) {
        float minx = pt[0].x;
        float maxx = pt[0].x;
        float miny = pt[0].y;
        float maxy = pt[0].y;
        for (int i = 1; i < 4; i++) {
            minx = Math.min(minx, pt[i].x);
            maxx = Math.max(maxx, pt[i].x);
            miny = Math.min(miny, pt[i].y);
            maxy = Math.max(maxy, pt[i].y);
        }
        rect.set(minx, miny, maxx - minx, maxy - miny);
    }

    @Override
    public void add(Widget child) {
        super.add(child);
    }

    @Override
    public void add(Widget... children) {
        super.add(children);
    }

    @Override
    public void add(List<Widget> children) {
        super.add(children);
    }
    
    @Override
    public void onDraw(Graphics graphics) {
        if (discardDraw(graphics)) return;
        
        drawBackground(graphics);
        drawRipple(graphics);
        
        if (getOutWidth() <= 0 || getOutHeight() <= 0) return;
        
        graphics.setTransform2D(getTransform());
        if (clip) {
            graphics.pushClip(getBackgroundShape());
        }
        for (Widget child : getChildrenIterable()) {
            if (child.getVisibility() == Visibility.VISIBLE) {
                child.onDraw(graphics);
            }
        }
        if (clip) {
            graphics.popClip();
        }
    }
}
