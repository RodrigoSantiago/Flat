package flat.animations.presets;

import flat.animations.Interpolation;
import flat.animations.NormalizedAnimation;
import flat.widget.Widget;
import flat.window.Activity;

public class Hide extends NormalizedAnimation {

    private Widget widget;
    private float _dimension;

    public Hide(Widget widget) {
        this.widget = widget;
    }

    @Override
    protected void evaluate() {
        super.evaluate();
        if (isStopped()) {
            _dimension = widget.getInHeight();
        }
    }

    @Override
    protected void compute(float t) {
        float nDimension = Interpolation.mix(1, 0, t);
        widget.setPrefHeight(_dimension * nDimension);
        widget.setMaxHeight(_dimension * nDimension);
        widget.setMinHeight(_dimension * nDimension);
        widget.setTranslateY(-_dimension * (1 - nDimension));
        //widget.setOffsetHeight(_dimension - (_dimension * nDimension));
        if (t == 1) {
            if (widget.getParent() != null) {
                widget.getParent().remove(widget);
            }
        }
    }

    @Override
    public Activity getSource() {
        return widget.getActivity();
    }
}
