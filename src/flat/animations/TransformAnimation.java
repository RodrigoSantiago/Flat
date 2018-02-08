package flat.animations;

import flat.widget.Widget;

public class TransformAnimation extends Animation {

    private float fTx, fTy, tTx, tTy;
    private float fSx, fSy, tSx, tSy;
    private float fR, tR;
    private boolean linearAngularMix;
    private Widget widget;

    private float _fTx, _fTy, _tTx, _tTy;
    private float _fSx, _fSy, _tSx, _tSy;
    private float _fR, _tR;
    private boolean _linearAngularMix;
    private Widget _widget;

    public TransformAnimation() {
        this(null);
    }

    public TransformAnimation(Widget widget) {
        fTx = tTx = fTy = tTy = Float.NaN;
        fSx = tSx = fSy = tSy = Float.NaN;
        fR = tR = Float.NaN;
        this.widget = widget;
    }

    public Widget getWidget() {
        return widget;
    }

    public void setWidget(Widget widget) {
        this.widget = widget;
    }

    public void ignoreTranslate() {
        fTx = tTx = fTy = tTy = Float.NaN;
    }

    public void ignoreScale() {
        fSx = tSx = fSy = tSy = Float.NaN;
    }

    public void ignoreRotation() {
        fR = tR = Float.NaN;
    }

    public boolean isLinearAngularMix() {
        return linearAngularMix;
    }

    public void setLinearAngularMix(boolean linearAngularMix) {
        this.linearAngularMix = linearAngularMix;
    }

    public TransformAnimation setFrom(float x, float y, float scaleX, float scaleY, float rotation) {
        fTx = x;
        fTy = y;
        fSx = scaleX;
        fSy = scaleY;
        if (rotation < 0 || rotation > 360) rotation = rotation % 360;
        fR = rotation;
        return this;
    }

    public TransformAnimation setTo(float x, float y, float scaleX, float scaleY, float rotation) {
        tTx = x;
        tTy = y;
        tSx = scaleX;
        tSy = scaleY;
        if (rotation < 0 || rotation > 360) rotation = rotation % 360;
        tR = rotation;
        return this;
    }

    public TransformAnimation setFromTrn(float x, float y) {
        fTx = x;
        fTy = y;
        return this;
    }

    public TransformAnimation setToTrn(float x, float y) {
        tTx = x;
        tTy = y;
        return this;
    }

    public TransformAnimation setFromScl(float x, float y) {
        fSx = x;
        fSy = y;
        return this;
    }

    public TransformAnimation setToScl(float x, float y) {
        tSx = x;
        tSy = y;
        return this;
    }

    public TransformAnimation setFromRot(float angle) {
        fR = angle;
        return this;
    }

    public TransformAnimation setToRot(float angle) {
        tR = angle;
        return this;
    }

    public void setFromTranslateX(float x) {
        fTx = x;
    }

    public float getFromTranslateX() {
        return fTx;
    }

    public void setFromTranslateY(float y) {
        fTy = y;
    }

    public float getFromTranslateY() {
        return fTy;
    }

    public void setToTranslateX(float x) {
        tTx = x;
    }

    public float getToTranslateX() {
        return tTx;
    }

    public void setToTranslateY(float y) {
        tTy = y;
    }

    public float getToTranslateY() {
        return tTy;
    }

    public void setFromScaleX(float x) {
        fSx = x;
    }

    public float getFromScaleX() {
        return fSx;
    }

    public void setFromScaleY(float y) {
        fSy = y;
    }

    public float getFromScaleY() {
        return fSy;
    }

    public void setToScaleX(float x) {
        tSx = x;
    }

    public float getToScaleX() {
        return tSx;
    }

    public void setToScaleY(float y) {
        tSy = y;
    }

    public float getToScaleY() {
        return tSy;
    }

    public void setFromRotate(float angle) {
        fR = angle;
    }

    public float getFromRotate() {
        return fR;
    }

    public void setToRotate(float angle) {
        tR = angle;
    }

    public float getToRotate() {
        return tR;
    }

    @Override
    protected void evaluate() {
        super.evaluate();
        if (isStopped()) {
            _fTx = fTx;
            _fTy = fTy;
            _tTx = tTx;
            _tTy = tTy;
            _fSx = fSx;
            _fSy = fSy;
            _tSx = tSx;
            _tSy = tSy;
            if (!linearAngularMix) {
                _fR = fR % 360;
                _tR = tR % 360;
            } else {
                _fR = fR;
                _tR = tR;
            }
            _linearAngularMix = linearAngularMix;
            _widget = widget;
        }
    }

    @Override
    protected void compute(float t) {
        if (_widget != null) {
            if (!Float.isNaN(_fTx) && !Float.isNaN(_tTx)) {
                widget.setTranslateX(mix(_fTx, _tTx, t));
            }
            if (!Float.isNaN(_fTy) && !Float.isNaN(_tTy)) {
                widget.setTranslateY(mix(_fTy, _tTy, t));
            }
            if (!Float.isNaN(_fSx) && !Float.isNaN(_tSx)) {
                widget.setScaleX(mix(_fSx, _tSx, t));
            }
            if (!Float.isNaN(_fSy) && !Float.isNaN(_tSy)) {
                widget.setScaleY(mix(_fSy, _tSy, t));
            }
            if (!Float.isNaN(_fR) && !Float.isNaN(_tR)) {
                widget.setRotate(_linearAngularMix ? mix(_fR, tR, t) : angularMix(_fR, _tR, t));
            }
        }
    }
}
