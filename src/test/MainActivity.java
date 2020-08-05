package test;

import flat.Flat;
import flat.animations.presets.Hide;
import flat.animations.presets.Show;
import flat.animations.property.Property;
import flat.events.ActionEvent;
import flat.graphics.SmartContext;
import flat.math.Affine;
import flat.math.shapes.Circle;
import flat.math.shapes.Path;
import flat.math.shapes.Shape;
import flat.math.stroke.BasicStroke;
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
            if (event.getPointerID() == 1) f += 0.2f;
            else f -= 1;
            invalidate(true);
        });
    }

    @Override
    public void onDraw(SmartContext context) {
        super.onDraw(context);
        /*context.setAntialiasEnabled(true);
        context.setView(0, 0, (int) getWidth(), (int) getHeight());
        context.clear(0xFFFFFFFF, 1, 1);

        //context.getContext().svgBegin();
        context.clearClip();
        context.intersectClip(new Circle(200, 200, 100));

        context.setColor(0x000000FF);
        context.drawText(100, 100, "Ola Mundão");
        context.setColor(0x0000FF64);
        context.drawCircle(200, 200, 100, true);

        Path star = new Path(Path.WIND_EVEN_ODD);
        star.moveTo(100, 100);
        star.lineTo(200, 100);
        star.lineTo(100, 200);
        star.lineTo(150, 50);
        star.lineTo(200, 200);
        star.closePath();
        star.transform(new Affine().translate(-150, -100));
        star.transform(new Affine().translate(mx, my));

        Path p = new Path();
        p.moveTo(100, 100);
        p.curveTo(mx, my, 200, 100,  100, 200);

        BasicStroke bs = new BasicStroke(8, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10.0f,
                new float[]{30,50,30}, f);
        Shape shape = bs.createStrokedShape(star);
        context.setColor(0xFF000080);
        context.setStroker(new BasicStroke(1));
        //context.drawShapeOptimized(shape, true);
        context.drawShape(star, true);

        context.setStroker(bs);
        context.drawShape(star, false);
        invalidate(true);*/
    }
    float f = 0.1f;

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