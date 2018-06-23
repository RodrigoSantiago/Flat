package flat.widget.selection;

import flat.animations.Animation;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.math.operations.Area;
import flat.math.shapes.Ellipse;
import flat.math.shapes.Path;
import flat.math.shapes.Shape;
import flat.uxml.Controller;
import flat.uxml.UXAttributes;
import flat.resources.Dimension;

import java.util.Objects;

public class RadioButton extends ToogleWidget {

    private static Shape defIcon = new Ellipse(6,6,12,12);
    private static Shape defBg = new Path(new Area(new Ellipse(0, 0, 24, 24)).subtract(new Area(new Ellipse(2,2,20,20))));

    private Shape icon;
    private int onColor, offColor;

    private AnimShowHide animation = new AnimShowHide();
    private Shape bgPath, icPath;
    private float bgSize;
    private int bgColor;

    @Override
    public void applyAttributes(Controller controller, UXAttributes attributes) {
        super.applyAttributes(controller, attributes);

        float dp24 = Dimension.dpPx(24);
        setPrefSize(attributes.asNumber("width", dp24), (attributes.asNumber("height", dp24)));
        setOnColor(attributes.asColor("onColor", 0x000000FF));
        setOffColor(attributes.asColor("offColor", 0x808080FF));
        setIcon(attributes.asShape("icon", defIcon));

        bgColor = getOffColor();
        bgPath = defBg;
    }

    @Override
    public void setSelected(boolean selected) {
        if (this.selected != selected) {
            this.selected = selected;
            animation.show = selected;
            float p = 0;
            if (animation.isPlaying()) {
                p = 1 - animation.getPosition();
            }
            animation.stop();
            animation.play(p);
            invalidate(false);
        }
    }

    public Shape getIcon() {
        return icon;
    }

    public void setIcon(Shape icon) {
        if (!Objects.equals(icon, this.icon)) {
            if (icon == null) {
                this.icon = null;
                icPath = new Path();
            } else {
                this.icon = icon;
                icPath = new Path(new Area(icon));
            }
            invalidate(false);
        }
    }

    public int getOnColor() {
        return onColor;
    }

    public void setOnColor(int onColor) {
        if (this.onColor != onColor) {
            this.onColor = onColor;
            invalidate(false);
        }
    }

    public int getOffColor() {
        return offColor;
    }

    public void setOffColor(int offColor) {
        if (this.offColor != offColor) {
            this.offColor = offColor;
            invalidate(false);
        }
    }

    @Override
    public void onDraw(SmartContext context) {
        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        context.setTransform2D(getTransformView().translate(x, y).scale(width / 24f, height / 24f));
        context.setColor(bgColor);
        context.drawShape(bgPath, true);
        if (bgSize > 0) {
            float iw = (width) / 2f * (1f - bgSize);
            float ih = (height) / 2f * (1f - bgSize);
            float x2 = width - iw;
            float y2 = height - ih;
            context.setTransform2D(getTransformView().translate(x + iw, y + ih).scale((x2 - iw) / 24f, (y2 - ih) / 24f));
            context.drawShape(icPath, true);
        }

        if (isRippleEffectEnabled() && getRipple().isVisible()) {
            context.setTransform2D(getTransformView());
            getRipple().drawRipple(context, null, getRippleColor());
            context.setTransform2D(null);
        }
    }

    @Override
    public void fireRipple(float x, float y) {
        if (isRippleEffectEnabled()) {
            getRipple().fire(getInX() + getInWidth() / 2f, getInY() + getInHeight() / 2f);
        }
    }

    @Override
    public void firePointer(PointerEvent pointerEvent) {
        if (pointerEvent.getType() == PointerEvent.RELEASED) {
            setSelected(!isSelected());
        }
        super.firePointer(pointerEvent);
    }

    private class AnimShowHide extends Animation {
        public boolean show;
        private boolean _show;

        AnimShowHide() {
            setDuration(150);
        }

        @Override
        protected void evaluate() {
            super.evaluate();
            if (isStopped()) {
                _show = show;
            }
        }

        @Override
        protected void compute(float t) {
            if (_show) {
                bgSize = t;
                bgColor = mixColor(getOnColor(), getOffColor(), t);
            } else {
                bgSize = 1f - t;
                bgColor = mixColor(getOffColor(), getOnColor(), t);
            }
            invalidate(false);
        }
    }
}
