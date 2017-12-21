package flat.screen;

import flat.backend.GL;
import flat.graphics.context.Context;
import flat.graphics.context.Frame;
import flat.graphics.context.Render;
import flat.graphics.context.Texture2D;
import flat.graphics.context.enuns.PixelFormat;
import flat.graphics.smart.SmartContext;
import flat.math.Affine;
import flat.widget.Scene;
import flat.widget.Widget;

public class Activity {
    private Scene scene;
    private boolean invalided, layoutInvalidaded;
    private float width;
    private float height;

    Texture2D back;
    Render render;
    volatile boolean finished = false;

    public Activity() {
        scene = new Scene(this);

        Application.createGraphicalThread((context) -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            back = new Texture2D();
            back.begin(0);
            back.setSize(100, 100, PixelFormat.RGBA);
            back.setData(0, new int[]{-1, -1, -1, -1}, 0, 0, 0, 2, 2);
            back.generatMipmapLevels();
            back.end();

            render = new Render();
            render.begin();
            render.setSize(100, 100, PixelFormat.DEPTH32_STENCIL8);
            render.end();

            Frame frame = new Frame(context);
            frame.begin();
            frame.attach(Frame.DEPTH_STENCIL, render);
            frame.attach(0, back, 0);
            context.setViewPort(0, 0, 100, 100);
            context.setClearColor(0xFFFFFFFF);
            context.clear(true, true, true);
            frame.end();

            context.finish();
            context.hardFlush();

            Application.runSync(() -> {
                finished = true;
                Activity.this.invalidate(true);
            });
        }).start();
    }

    public void onSave() {

    }

    public void onLoad() {

    }

    public void onLayout(float width, float height) {
        this.width = width;
        this.height = height;
        scene.onMeasure();
        scene.onLayout(0, 0, width, height);
    }

    public void onDraw(SmartContext context) {
        context.setView(0, 0, (int) getWidth(), (int) getHeight());
        scene.onDraw(context);
        if (finished) {
            context.setTransform2D(new Affine());
            context.drawImage(back, 0, 0, 1, 1, 100, 100, 200, 200, null);
        }
    }

    public Scene getScene() {
        return scene;
    }

    final boolean draw() {
        if (invalided) {
            invalided = false;
            return true;
        } else {
            return false;
        }
    }

    final boolean layout() {
        if (layoutInvalidaded) {
            layoutInvalidaded = false;
            return true;
        } else {
            return false;
        }
    }

    public final void invalidate(boolean layout) {
        invalided = true;
        if (layout) {
            layoutInvalidaded = true;
        }
    }

    public Widget findById(String id) {
        return scene.findById(id);
    }

    public Widget findByPosition(float x, float y) {
        Widget child = scene.findByPosition(x , y);
        return child == null ? scene : child;
    }

    public Widget findFocused() {
        Widget child = scene.findFocused();
        return child == null ? scene : child;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
