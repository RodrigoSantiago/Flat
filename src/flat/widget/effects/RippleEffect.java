package flat.widget.effects;

import flat.animations.Animation;
import flat.animations.Interpolation;
import flat.animations.PropertyAnimation;
import flat.graphics.SmartContext;
import flat.graphics.context.Paint;
import flat.math.shapes.Circle;
import flat.math.shapes.Shape;
import flat.resources.Dimension;
import flat.widget.Widget;

public class RippleEffect {

    private static final float[] stops = new float[]{0, 1};
    private final int[] colors = new int[]{0, 0};

    private float size;

    private final Widget widget;
    private final Circle ripple = new Circle();
    private final RippleAnimation animation = new RippleAnimation();

    public RippleEffect(Widget widget) {
        this.widget = widget;
        animation.setDuration(1000);
        animation.setInterpolation(Interpolation.quadOut);
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public boolean isVisible() {
        return animation.isPlaying();
    }

    public void drawRipple(SmartContext context, Shape clip, int color) {
        float a = (((color & 0xFF) / 255f) * (1 - animation.getT()));
        colors[0] = (color & 0xFFFFFF00) | ((int) (a * 255));
        colors[1] = (color & 0xFFFFFF00);

        context.setPaint(Paint.radial(ripple.x, ripple.y, ripple.radius, ripple.radius,
                stops, colors, Paint.CycleMethod.CLAMP, context.getTransform2D()));

        context.drawShape(clip == null ? ripple : clip, true);
    }

    public void fire(float x, float y) {
        ripple.set(x, y, 1);
        animation.stop();
        animation.setDelta(1);
        animation.play();
    }

    public void release() {
        animation.setDelta(2);
    }

    private class RippleAnimation extends PropertyAnimation {
        @Override
        public void compute(float t) {
            float w = widget.getWidth() - widget.getMarginLeft() - widget.getMarginRight();
            float h = widget.getHeight() - widget.getMarginTop() - widget.getMarginBottom();
            float s = size <= 0 ? (float) Math.min(Dimension.dpPx(300), Math.sqrt(w * w + h * h)) : size;
            ripple.radius = Interpolation.mix(s / 10f,  s, t);

            widget.invalidate(false);
        }
    }
}
