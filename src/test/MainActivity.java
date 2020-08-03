package test;

import flat.Flat;
import flat.animations.presets.Hide;
import flat.animations.presets.Show;
import flat.animations.property.Property;
import flat.events.ActionEvent;
import flat.graphics.SmartContext;
import flat.math.Affine;
import flat.math.shapes.Path;
import flat.math.shapes.PathIterator;
import flat.math.shapes.Shape;
import flat.math.stroke.BasicStroke;
import flat.math.stroke.Dasher;
import flat.resources.Dimension;
import flat.resources.ResourcesManager;
import flat.uxml.UXTheme;
import flat.widget.Activity;
import flat.resources.ResourceStream;
import flat.widget.layout.LinearBox;
import flat.widget.text.Button;

public class MainActivity extends Activity {

    @Flat public LinearBox box;
    @Flat public Button btn1;

    public MainActivity(int n) {
        setTheme(new UXTheme(ResourcesManager.getInput("themes/material.uxss")));
        setSceneStream(new ResourceStream("screen_test"));
    }

    float mx, my;
    @Override
    public void onLoad() {
        getScene().setPointerListener(event -> {
            mx = event.getX();
            my = event.getY();
            //if (event.getType() == PointerEvent.PRESSED)
                invalidate(true);
                f += 1;
        });
    }

    @Override
    public void onDraw(SmartContext context) {
        context.setAntialiasEnabled(true);
        context.setView(0, 0, (int) getWidth(), (int) getHeight());
        context.clear(0xFFFFFFFF, 1, 1);

        //context.getContext().svgBegin();
        context.clearClip();
        /*context.setClip(new Circle(100, 100, 100));

        context.setColor(0xFF000080);
        Path p = new Path() {
            @Override
            public boolean isOptimized() {
                return true;
            }
        };
        p.moveTo(50, 50);
        p.lineTo(100, 0);
        p.lineTo(100, 100);
        p.lineTo(0, 100);
        p.lineTo(0, 0);
        p.closePath();
        p.transform(new Affine().translate(mx, my));
        p.moveTo(125, 175);
        p.lineTo(175, 175);
        p.lineTo(175, 125);
        p.lineTo(125, 125);
        p.closePath();
        context.setStroker(new BasicStroke(10));
        context.drawShape(p, false);
        context.setColor(0xFFFF0080);
        context.drawShape(p, true);
        // super.onDraw(context);
        context.setStroker(new BasicStroke(20));
        context.drawRect(mx, my, 100, 100, false);
        context.setStroker(new BasicStroke(20));
        context.drawRect(mx+50, my+50, 100, 100, false);
        /*context.setColor(0x0000FF80);
        context.drawRect(mx, my, 100, 100, true);*/
        Path star = new Path(Path.WIND_NON_ZERO);
        /*star.moveTo(100, 100);
        star.lineTo(200, 100);
        star.lineTo(100, 200);
        star.lineTo(150, 50);
        star.lineTo(200, 200);*/
        star.moveTo(100, 100);
        star.lineTo(200, 100);
        star.lineTo(200, 200);
        star.lineTo(100, 200);
        star.closePath();
        star.transform(new Affine().translate(200, 0));

        Path p = new Path();
        p.moveTo(100, 100);
        p.curveTo(mx, my, 200, 100,  100, 200);
        /*test.stroke.BasicStroke bs = new test.stroke.BasicStroke(test.stroke.BasicStroke.TYPE_CENTERED, 30,
                test.stroke.BasicStroke.CAP_BUTT,
                test.stroke.BasicStroke.JOIN_MITER, 10);*/

        BasicStroke bs = new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f,
                new float[]{10,10}, 0);
        Shape shape = bs.createStrokedShape(star);
        context.setColor(0xFF000080);
        context.setStroker(new BasicStroke(1));
        context.setTransform2D(new Affine().translate(-120, 0));
        context.drawShapeOptimized(shape, true);

        context.setStroker(bs);
        context.setTransform2D(null);
        context.drawShapeOptimized(star, false);
        //context.drawShapeOptimized(star, true);
        /*Dasher d = new Dasher(star.pathIterator(null), new float[]{10,50,10}, f);
        context.drawShapeOptimized(d.path, false);
        float[] data = new float[6];
        PathIterator pi = d.path.pathIterator(null);
        while (!pi.isDone()) {
            switch (pi.currentSegment(data)) {
                case PathIterator.SEG_MOVETO:
                    context.drawCircle(data[0], data[1], 5, true);
                    break;
                case PathIterator.SEG_LINETO:
                    context.drawCircle(data[0], data[1], 5, true);
                    break;
                case PathIterator.SEG_QUADTO:
                    context.drawCircle(data[0], data[1], 5, false);
                    context.drawCircle(data[2], data[3], 5, true);
                    break;
                case PathIterator.SEG_CUBICTO:
                    context.drawCircle(data[0], data[1], 5, false);
                    context.drawCircle(data[2], data[3], 5, false);
                    context.drawCircle(data[4], data[5], 5, true);
                    break;
                case PathIterator.SEG_CLOSE:
                    break;
            }
            pi.next();
        }*/
        invalidate(true);
    }
    float f = 0.1f;

    public static float[] splitBezier(float Ax, float Ay, float Bx, float By, float Cx, float Cy, float Dx, float Dy) {
        float Ex = (Ax + Bx) / 2f;
        float Fx = (Bx + Cx) / 2f;
        float Gx = (Cx + Dx) / 2f;
        float Hx = (Ex + Fx) / 2f;
        float Jx = (Fx + Gx) / 2f;
        float Kx = (Hx + Jx) / 2f;

        float Ey = (Ay + By) / 2f;
        float Fy = (By + Cy) / 2f;
        float Gy = (Cy + Dy) / 2f;
        float Hy = (Ey + Fy) / 2f;
        float Jy = (Fy + Gy) / 2f;
        float Ky = (Hy + Jy) / 2f;
        // A,E,H,K and K,J,G,D.
        return new float[]{Ax, Ay, Ex, Ey, Hx, Hy, Kx, Ky,
                Kx, Ky, Jx, Jy, Gx, Gy, Dx, Dy};
    }


    @Flat
    public void onAction(ActionEvent event) {
        Hide hide = new Hide(event.getSource());
        event.getSource().setClickable(false);
        hide.setDuration(1000);
        hide.play(this);
    }

    @Flat
    public void onAdd(ActionEvent event) {
        Button btn = new Button();
        btn.setStyle(getTheme().getStyle("button"));
        btn.applyStyle();
        btn.setText("ola");
        btn.setActionListener(this::onAction);
        box.add(btn);

        Show show = new Show(btn);
        show.setDimension(Dimension.dpPx(64));
        show.setDuration(1000);
        show.play(this);

        Property p = new Property<>(box::setPrefWidth, 0f, 100f);
    }
}