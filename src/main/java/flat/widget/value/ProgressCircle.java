package flat.widget.value;

import flat.graphics.SmartContext;
import flat.math.shapes.Arc;
import flat.math.stroke.BasicStroke;
import flat.widget.enums.ProgressLineMode;

public class ProgressCircle extends ProgressBar {

    private final Arc arc = new Arc();

    @Override
    public void onMeasure() {
        float extraWidth = getPaddingLeft() + getPaddingRight() + getMarginLeft() + getMarginRight();
        float extraHeight = getPaddingTop() + getPaddingBottom() + getMarginTop() + getMarginBottom();

        float mWidth;
        float mHeight;
        boolean wrapWidth = getLayoutPrefWidth() == WRAP_CONTENT;
        boolean wrapHeight = getLayoutPrefHeight() == WRAP_CONTENT;

        if (wrapWidth) {
            mWidth = Math.max(extraWidth + getLineWidth() * 2f, getLayoutMinWidth());
        } else {
            mWidth = Math.max(getLayoutPrefWidth(), getLayoutMinWidth());
        }
        if (wrapHeight) {
            mHeight = Math.max(extraHeight + getLineWidth() * 2f, getLayoutMinHeight());
        } else {
            mHeight = Math.max(getLayoutPrefHeight(), getLayoutMinHeight());
        }

        setMeasure(mWidth, mHeight);
    }

    @Override
    public void onDraw(SmartContext context) {
        drawBackground(context);
        drawRipple(context);

        float x = getInX();
        float y = getInY();
        float width = getInWidth();
        float height = getInHeight();

        float lineWidth = Math.min(getLineWidth(), Math.min(width, height));
        float lineRadius = lineWidth * 0.5f;

        x += lineRadius;
        y += lineRadius;
        width -= lineRadius * 2;
        height -= lineRadius * 2;

        if (width <= 0 || height <= 0) return;

        context.setTransform2D(getTransform());
        context.setStroker(new BasicStroke(lineWidth, getLineMode() == ProgressLineMode.REGULAR ? 0 : 1, 2));
        float baseAngle = 90;
        float radius = (float) Math.sqrt(width * width + height * height) * 0.5f;

        float angleOffset = getAngle(radius, lineRadius);

        if (getValue() < 0 && indeterminateAnimation.isPlaying()) {
            float t1 = indeterminateAnimation.getT1();
            float t2 = indeterminateAnimation.getT2();
            float t3 = indeterminateAnimation.getT3() * 120f % 360;

            if (getLineMode() == ProgressLineMode.REGULAR || getLineMode() == ProgressLineMode.ROUND) {
                context.setColor(getLineColor());
                context.drawEllipse(x, y, width, height, false);
            } else {
                context.setColor(getLineColor());
                float x2 = 360 * t2 + t3 + 360 * (t1 - t2);
                float x3 = 360 * t2 + t3 + 360;

                float secx1 = x2 + angleOffset * 4f;
                float secx2 = x3 - angleOffset * 4f;
                if (secx1 < secx2) {
                    arc.set(x, y, width, height, secx1, secx2 - secx1, Arc.Type.OPEN);
                    context.drawShape(arc, false);
                }
            }

            context.setColor(getLineFilledColor());
            arc.set(x, y, width, height, 360 * t2 + t3, 360 * (t1 - t2), Arc.Type.OPEN);
            context.drawShape(arc, false);
        } else {

            if (getLineMode() == ProgressLineMode.REGULAR || getLineMode() == ProgressLineMode.ROUND
                    || visibleValue <= 0 || visibleValue >= 1) {
                context.setColor(getLineColor());
                context.drawEllipse(x, y, width, height, false);
            } else {
                context.setColor(getLineColor());

                float x2 = baseAngle - 360 * visibleValue;
                float x3 = baseAngle - 360;
                float secx1 = x2 - angleOffset * 4f;
                float secx2 = x3 + angleOffset * 4f;
                if (secx1 > secx2) {
                    arc.set(x, y, width, height, secx1, secx2 - secx1, Arc.Type.OPEN);
                    context.drawShape(arc, false);
                }
            }

            context.setColor(getLineFilledColor());
            arc.set(x, y, width, height, baseAngle, -360 * visibleValue, Arc.Type.OPEN);
            context.drawShape(arc, false);

        }
    }

    private float getAngle(float radius, float distance) {
        float angleRadians = distance / radius;

        return (float) Math.toDegrees(angleRadians);
    }
}
