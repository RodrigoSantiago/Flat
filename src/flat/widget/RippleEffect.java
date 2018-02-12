package flat.widget;

import flat.animations.Animation;
import flat.animations.Interpolation;
import flat.graphics.SmartContext;
import flat.math.operations.Area;
import flat.math.shapes.Circle;
import flat.math.shapes.RoundRectangle;
import flat.uxml.data.Dimension;

class RippleEffect {

    private Widget widget;
    private RippleAnimation animation = new RippleAnimation();

    private Circle circle = new Circle();
    private RoundRectangle bg = new RoundRectangle();
    private Area rectArea = new Area(bg);
    private Area rippleArea = new Area();

    public RippleEffect(Widget widget) {
        this.widget = widget;
        animation.setDuration(1000);
        animation.setInterpolation(Interpolation.quadOut);
    }

    public boolean isVisible() {
        return animation.isPlaying();
    }

    public void drawRipple(SmartContext context, RoundRectangle background, int color) {
        if (!background.equals(bg)) {
            bg.set(background);
            rectArea = new Area(bg);
        }
        rippleArea.set(circle).intersect(rectArea);
        float alpha = (((color & 0xFF) / 255f) * (1 - animation.getPosition()));
        context.setColor((color & 0xFFFFFF00) | ((int)(alpha * 255)));
        context.drawShape(rippleArea, true);
    }

    public void fire(float x, float y) {
        circle.set(x, y, Dimension.dpPx(8));
        animation.stop();
        animation.setDelta(1);
        animation.play();
    }

    public void release() {
        animation.setDelta(2);
    }

    private class RippleAnimation extends Animation {

        @Override
        public void compute(float t) {
            float w = widget.getWidth() - widget.getMarginLeft() - widget.getMarginRight();
            float h = widget.getHeight() - widget.getMarginTop() - widget.getMarginBottom();
            circle.radius = mix(Dimension.dpPx(8), Math.min(Dimension.dpPx(300), (float) Math.sqrt(w * w + h * h)), t);
            RippleEffect.this.widget.invalidate(false);
        }
    }
}
