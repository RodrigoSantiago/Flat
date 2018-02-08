package flat.animations;

import flat.widget.Widget;

public class ElevationAnimation extends Animation {

    private Widget widget;
    private float fromElevation;
    private float toElevation;

    private Widget _widget;
    private float _fromElevation;
    private float _toElevation;

    public ElevationAnimation() {
        this(null);
    }

    public ElevationAnimation(Widget widget) {
        this.widget = widget;
    }

    public ElevationAnimation set(float from, float to) {
        setFromElevation(from);
        setToElevation(to);
        return this;
    }

    public Widget getWidget() {
        return widget;
    }

    public void setWidget(Widget widget) {
        this.widget = widget;
    }

    public float getFromElevation() {
        return fromElevation;
    }

    public void setFromElevation(float fromElevation) {
        this.fromElevation = fromElevation;
    }

    public float getToElevation() {
        return toElevation;
    }

    public void setToElevation(float toElevation) {
        this.toElevation = toElevation;
    }

    @Override
    protected void evaluate() {
        super.evaluate();
        if (isStopped()) {
            _widget = widget;
            _fromElevation = fromElevation;
            _toElevation = toElevation;
        }
    }

    @Override
    protected void compute(float t) {
        if (_widget != null) {
            _widget.setElevation(mix(_fromElevation, _toElevation, t));
        }
    }
}
