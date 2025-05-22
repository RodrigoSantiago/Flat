package flat.widget.text;

public class Text extends Label {

    @Override
    public void onMeasure() {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        float mWidth;
        float mHeight;
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;

        if (wrapWidth) {
            mWidth = Math.max(getNaturalTextWidth() + extraWidth, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            mHeight = Math.max(getTextHeight() + extraHeight, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public void setLayout(float layoutWidth, float layoutHeight) {
        super.setLayout(layoutWidth, layoutHeight);
        if (getActivity() != null && textRender.isBreakLines(getInWidth())) {
            getActivity().runLater(() -> {
                textRender.breakLines(getInWidth());
                invalidate(true);
            });
        }
    }

    protected float getNaturalTextWidth() {
        return textRender.getNaturalWidth();
    }
}
