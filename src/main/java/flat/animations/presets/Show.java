package flat.animations.presets;

import flat.animations.Interpolation;
import flat.animations.NormalizedAnimation;
import flat.widget.Widget;
import flat.window.Activity;

public class Show extends NormalizedAnimation {

    private Widget widget;
    private float dimension;
    private float _dimension;

    public Show(Widget widget) {
        this.widget = widget;
    }

    public void setDimension(float dimension) {
        this.dimension = dimension;
    }

    @Override
    protected void evaluate() {
        super.evaluate();
        if (isStopped()) {
            _dimension = dimension;
        }
    }

    @Override
    protected void compute(float t) {
        if (t == 0) {

        }

        float nDimension = Interpolation.mix(0, _dimension, t);
        widget.setPrefWidth(nDimension);
        widget.setMaxWidth(nDimension);
        widget.setMinWidth(nDimension);
    }

    @Override
    public Activity getSource() {
        return widget.getActivity();
    }
}
