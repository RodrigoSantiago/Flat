package flat.widget.selection;

import flat.animations.Animation;
import flat.events.PointerEvent;
import flat.graphics.SmartContext;
import flat.uxml.Controller;
import flat.uxml.UXAttributes;
import flat.resources.Dimension;

public class Switch extends ToogleWidget {

    private float size;

    private int onColor, offColor;
    private int onBackColor, offBackColor;

    private int icColor, bgColor;
    private float icPosition;
    private AnimShowHide animation = new AnimShowHide();

    // todo - progress indicator

    @Override
    public void applyAttributes(Controller controller, UXAttributes attributes) {
        super.applyAttributes(controller, attributes);

        float dp36 = Dimension.dpPx(36);
        float dp20 = Dimension.dpPx(20);
        float dp14 = Dimension.dpPx(14);

        setElevation(attributes.asSize("elevation", 1));
        setPrefSize(attributes.asNumber("width", dp36), (attributes.asNumber("height", dp14)));
        setSize(attributes.asNumber("size", dp20));

        setBackgroundCorners(
                attributes.asNumber("backgroundCornerTop", dp14 / 2f),
                attributes.asNumber("backgroundCornerRight", dp14 / 2f),
                attributes.asNumber("backgroundCornerBottom", dp14 / 2f),
                attributes.asNumber("backgroundCornerLeft", dp14 / 2f));

        setOnColor(attributes.asColor("onColor", 0x6200EEFF));
        setOffColor(attributes.asColor("offColor", 0xFFFFFFFF));
        setOnBackColor(attributes.asColor("onBackColor", 0x6200ee89));
        setOffBackColor(attributes.asColor("offBackColor", 0x808080FF));
        icColor = getOffColor();
        bgColor = getOffBackColor();
    }

    @Override
    public void onDraw(SmartContext context) {
        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();
        context.setTransform2D(getTransformView());


        float x1 = (x + height / 2) * (1 - icPosition) + (x + width - height / 2) * icPosition;
        float y1 = y + height / 2;
        float r = size / 2;

        context.setTransform2D(getTransformView());
        context.setColor(bgColor);
        context.drawRoundRect(x,y,width,height,
                getBackgroundCornerTop(), getBackgroundCornerRight(), getBackgroundCornerBottom(), getBackgroundCornerLeft(),
                true);

        context.setTransform2D(getTransformView().preTranslate(0, Math.max(0, getElevation())));
        context.setColor(0x000000FF);
        context.drawRoundRectShadow(x1 - r, y1 - r, r + r, r + r, r, r, r, r, getElevation() * 2, 0.28f);

        context.setTransform2D(getTransformView());
        context.setColor(icColor);
        context.drawCircle(x1, y1, r, true);

        if (isRippleEffectEnabled() && getRipple().isVisible()) {
            context.setTransform2D(getTransformView().translate(x1, y1));
            getRipple().drawRipple(context, null, getRippleColor());
            context.setTransform2D(null);
        }
    }

    @Override
    public void fireRipple(float x, float y) {
        if (isRippleEffectEnabled()) {
            getRipple().fire(0, 0);
        }
    }

    @Override
    public void firePointer(PointerEvent pointerEvent) {
        if (pointerEvent.getType() == PointerEvent.RELEASED) {
            toogle();
        }
        super.firePointer(pointerEvent);
    }

    @Override
    public void onSelected(boolean selected) {
        super.onSelected(selected);
        animation.show = selected;
        float p = 0;
        if (animation.isPlaying()) {
            p = 1 - animation.getPosition();
        }
        animation.stop();
        animation.play(p);
        invalidate(false);
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        if (this.size != size) {
            this.size = size;
            getRipple().setSize(size);
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

    public int getOnBackColor() {
        return onBackColor;
    }

    public void setOnBackColor(int onBackColor) {
        if (this.onBackColor != onBackColor) {
            this.onBackColor = onBackColor;
            invalidate(false);
        }
    }

    public int getOffBackColor() {
        return offBackColor;
    }

    public void setOffBackColor(int offBackColor) {
        if (this.offBackColor != offBackColor) {
            this.offBackColor = offBackColor;
            invalidate(false);
        }
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
                icPosition = t;
                icColor = mixColor(getOffColor(), getOnColor(), t);
                bgColor = mixColor(getOffBackColor(), getOnBackColor(), t);
            } else {
                icPosition = 1f - t;
                icColor = mixColor(getOnColor(), getOffColor(), t);
                bgColor = mixColor(getOnBackColor(), getOffBackColor(), t);
            }
            invalidate(false);
        }
    }
}
