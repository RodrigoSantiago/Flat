package flat.animations.presets;

import flat.animations.Interpolation;
import flat.animations.NormalizedAnimation;
import flat.widget.Widget;

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
            _dimension = widget.getWidth();
        }
    }

    @Override
    protected void compute(float t) {
        float nDimension = Interpolation.mix(_dimension, 0, t);
        widget.setPrefWidth(nDimension);
        widget.setMaxWidth(nDimension);
        widget.setMinWidth(nDimension);

        if (t == 1) {
            if (widget.getParent() != null) {
                widget.getParent().remove(widget);
            }
        }
    }

}
