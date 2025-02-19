package flat.widget.effects;

import flat.animations.Interpolation;
import flat.animations.NormalizedAnimation;
import flat.graphics.SmartContext;
import flat.graphics.context.Paint;
import flat.math.shapes.Circle;
import flat.math.shapes.Shape;
import flat.window.Activity;
import flat.widget.Widget;

public class RippleEffect {

    private static final float[] stops = new float[]{0, 1};
    private final int[] colors = new int[]{0, 0};

    private final Widget widget;
    private final Circle ripple = new Circle();
    private final RippleAnimation animation = new RippleAnimation();

    public RippleEffect(Widget widget) {
        this.widget = widget;
        animation.setDuration(2);
        animation.setInterpolation(Interpolation.quadOut);
    }

    public float getSize() {
        return ripple.radius;
    }

    public void setSize(float size) {
        ripple.radius = size;
    }

    public void setPosition(float x, float y) {
        ripple.x = x;
        ripple.y = y;
    }

    public boolean isVisible() {
        return animation.isPlaying();
    }

    public void drawRipple(SmartContext context, Shape clip, int color) {
        float a = (((color & 0xFF) / 255f) * (1 - animation.getPosition()));
        colors[0] = (color & 0xFFFFFF00) | ((int) (a * 255));
        colors[1] = (color & 0xFFFFFF00);

        float min = Math.min(16, Math.max(4, ripple.radius * 0.1f));
        float max = Math.min(360, Math.max(8, ripple.radius));
        float pos = animation.getInterpolatedPosition();
        float radius = (min * (1 - pos)) + max * (pos);

        context.setPaint(Paint.radial(ripple.x, ripple.y, radius, radius, 0, 0,
                stops, colors, Paint.CycleMethod.CLAMP));
        if (clip == null) {
            context.drawCircle(ripple.x, ripple.y, radius, true);
        } else {
            context.drawShape(clip, true);
        }
    }

    public void fire(float x, float y) {
        if (animation.isStopped() || animation.getInterpolatedPosition() > 0.5f) {
            ripple.x = x;
            ripple.y = y;
            animation.stop();
            animation.setDelta(1);
            animation.play(widget.getActivity());
        }
    }

    public void release() {
        animation.setDelta(3);
    }

    public void stop() {
        animation.stop();
    }

    private class RippleAnimation extends NormalizedAnimation {
        @Override
        public void compute(float t) {
            widget.invalidate(false);
        }

        @Override
        public Activity getSource() {
            return widget.getActivity();
        }
    }
}
