package flat.widget.value;

import flat.animations.StateInfo;
import flat.events.PointerEvent;
import flat.events.SlideEvent;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.math.Mathf;
import flat.math.Vector2;
import flat.math.stroke.BasicStroke;
import flat.uxml.*;
import flat.widget.Widget;
import flat.widget.enums.Direction;
import flat.widget.enums.LineCap;

public class ScrollBar extends Widget {

    private UXListener<SlideEvent> slideListener;
    private UXListener<SlideEvent> slideFilter;
    private UXValueListener<Float> viewOffsetListener;
    private float viewOffset;
    private float viewDimension;
    private float totalDimension;

    private float minRange;
    private float lineWidth = 4;
    private int lineColor = Color.black;
    private LineCap lineCap = LineCap.BUTT;

    private float grabOffset;

    ScrollBar() {
    }

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);

        UXAttrs attrs = getAttrs();
        setTotalDimension(attrs.getAttributeSize("total-dimension", getTotalDimension()));
        setViewDimension(attrs.getAttributeSize("view-dimension", getViewDimension()));
        setViewOffset(attrs.getAttributeSize("view-offset", getViewOffset()));
        setSlideListener(attrs.getAttributeListener("on-slide", SlideEvent.class, controller));
        setSlideFilter(attrs.getAttributeListener("on-slide-filter", SlideEvent.class, controller));
        setViewOffsetListener(attrs.getAttributeValueListener("on-view-offset-change", Float.class, controller));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();
        setMinRange(attrs.getSize("min-range", info, getMinRange()));
        setLineColor(attrs.getColor("line-color", info, getLineColor()));
        setLineWidth(attrs.getSize("line-width", info, getLineWidth()));
        setLineCap(attrs.getConstant("line-cap", info, getLineCap()));
    }

    @Override
    public void onDraw(Graphics graphics) {
        if (discardDraw(graphics)) return;

        drawBackground(graphics);
        drawRipple(graphics);

        boolean hor = getDirection() == Direction.HORIZONTAL;

        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        if (width <= 0 || height <= 0) return;

        float hx1, hy1, hx2, hy2;
        float minR = Math.min(0.5f, minRange / (hor ? width : height));
        float handleSize = totalDimension == 0 ? 1 : Math.max(minR, Math.min(1, viewDimension / totalDimension));
        float handleSpace = 1 - handleSize;
        float handleMove = totalDimension == 0 ? 0 : Math.max(0, Math.min(1, viewOffset / (totalDimension - viewDimension)));

        float lineW = Math.min(width, Math.min(height, getLineWidth()));
        float lineH = getLineCap() == LineCap.BUTT ? 0 : lineW * 0.5f;
        if (hor) {
            hx1 = Mathf.lerp(x, x + width, handleMove * handleSpace) + lineH;
            hx2 = Mathf.lerp(x, x + width, handleMove * handleSpace + handleSize) - lineH;
            if (hx2 < hx1) {
                hx1 = (hx2 + hx1) * 0.5f;
                hx2 = hx1;
            }
            hy1 = y + height * 0.5f;
            hy2 = y + height * 0.5f;
        } else {
            hx1 =  x + width * 0.5f;
            hx2 =  x + width * 0.5f;
            hy1 = Mathf.lerp(y, y + height, handleMove * handleSpace) + lineH;
            hy2 = Mathf.lerp(y, y + height, handleMove * handleSpace + handleSize) - lineH;
            if (hy2 < hy1) {
                hy1 = (hy2 + hy1) * 0.5f;
                hy2 = hy1;
            }
        }

        graphics.setTransform2D(getTransform());
        if (Color.getAlpha(getLineColor()) > 0) {
            graphics.setColor(getLineColor());
            graphics.setStroke(new BasicStroke(lineW, getLineCap().ordinal(), 0));
            graphics.drawLine(hx1, hy1, hx2, hy2);
        }
    }

    @Override
    public void pointer(PointerEvent event) {
        super.pointer(event);
        if (event.isConsumed() || event.getPointerID() != 1) {
            return;
        }

        Vector2 point = new Vector2(event.getX(), event.getY());
        screenToLocal(point);

        boolean hor = getDirection() == Direction.HORIZONTAL;

        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        float hStart, hSize, pos, start, size;
        if (hor) {
            pos = point.x;
            start = x;
            size = width;
        } else {
            pos = point.y;
            start = y;
            size = height;
        }

        float minR = Math.min(0.5f, minRange / size);

        float handleSize = totalDimension == 0 ? 1 : Math.max(minR, Math.min(1, viewDimension / totalDimension));
        float moveOffset = totalDimension == 0 ? 0 : Math.max(0, 1 - handleSize);
        float handleMove = totalDimension == 0 ? 0 : Math.max(0, Math.min(1, viewOffset / (totalDimension - viewDimension)));
        float handleSpace = 1 - handleSize;

        if (event.getType() == PointerEvent.PRESSED) {
            event.consume();
            grabOffset = Math.max(0, Math.min(1, (((pos - start) / size) - handleMove * handleSpace) / handleSize));

            float py2 = (((pos - start) / size - handleSize * grabOffset) / moveOffset) * (totalDimension - viewDimension);
            slideTo(py2);

        } else if (event.getType() == PointerEvent.DRAGGED) {
            event.consume();
            float py2 = (((pos - start) / size - handleSize * grabOffset) / moveOffset) * (totalDimension - viewDimension);
            slideTo(py2);

        } else if (event.getType() == PointerEvent.RELEASED) {
            event.consume();
            grabOffset = 0;
        }
    }

    public Direction getDirection() {
        return null;
    }

    public float getViewOffset() {
        return viewOffset;
    }

    public void setViewOffset(float viewOffset) {
        viewOffset = Math.max(0, Math.min(viewOffset, totalDimension - viewDimension));

        if (this.viewOffset != viewOffset) {
            float old = this.viewOffset;
            this.viewOffset = viewOffset;
            invalidate(false);
            fireViewOffsetListener(old);
        }
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        if (this.lineWidth != lineWidth) {
            this.lineWidth = lineWidth;
            invalidate(false);
        }
    }

    public int getLineColor() {
        return lineColor;
    }

    public void setLineColor(int lineColor) {
        if (this.lineColor != lineColor) {
            this.lineColor = lineColor;
            invalidate(false);
        }
    }

    public LineCap getLineCap() {
        return lineCap;
    }

    public void setLineCap(LineCap lineCap) {
        if (lineCap == null) lineCap = LineCap.BUTT;

        if (this.lineCap != lineCap) {
            this.lineCap = lineCap;
            invalidate(false);
        }
    }

    public float getTotalDimension() {
        return totalDimension;
    }

    public void setTotalDimension(float totalDimension) {
        if (this.totalDimension != totalDimension) {
            this.totalDimension = totalDimension;
            invalidate(false);

            if (viewOffset > totalDimension - viewDimension) {
                setViewOffset(totalDimension - viewDimension);
            }
        }
    }

    public float getMinRange() {
        return minRange;
    }

    public void setMinRange(float minRange) {
        if (this.minRange != minRange) {
            this.minRange = minRange;
            invalidate(false);
        }
    }

    public float getViewDimension() {
        return viewDimension;
    }

    public void setViewDimension(float viewDimension) {
        if (this.viewDimension != viewDimension) {
            this.viewDimension = viewDimension;
            invalidate(false);

            if (viewOffset > totalDimension - viewDimension) {
                setViewOffset(totalDimension - viewDimension);
            }
        }
    }

    public void slideTo(float dimeionsOffset) {
        dimeionsOffset = Math.max(0, Math.min(dimeionsOffset, totalDimension - viewDimension));

        float old = getViewOffset();
        if (dimeionsOffset != old && filterSlide(dimeionsOffset)) {
            setViewOffset(dimeionsOffset);
            fireSlide();
        }
    }

    public void slide(float dimeionsOffset) {
        slideTo(getViewOffset() + dimeionsOffset);
    }

    public UXListener<SlideEvent> getSlideFilter() {
        return slideFilter;
    }

    public void setSlideFilter(UXListener<SlideEvent> slideFilter) {
        this.slideFilter = slideFilter;
    }

    private boolean filterSlide(float viewOffset) {
        if (slideFilter != null) {
            var event = new SlideEvent(this, SlideEvent.FILTER, viewOffset);
            UXListener.safeHandle(slideFilter, event);
            return !event.isConsumed();
        }
        return true;
    }

    public UXListener<SlideEvent> getSlideListener() {
        return slideListener;
    }

    public void setSlideListener(UXListener<SlideEvent> slideListener) {
        this.slideListener = slideListener;
    }

    private void fireSlide() {
        if (slideListener != null) {
            UXListener.safeHandle(slideListener, new SlideEvent(this, SlideEvent.SLIDE, getViewOffset()));
        }
    }

    public void setViewOffsetListener(UXValueListener<Float> viewOffsetListener) {
        this.viewOffsetListener = viewOffsetListener;
    }

    public UXValueListener<Float> getViewOffsetListener() {
        return viewOffsetListener;
    }

    private void fireViewOffsetListener(float old) {
        if (viewOffsetListener != null && old != viewOffset) {
            UXValueListener.safeHandle(viewOffsetListener, new ValueChange<>(this, old, viewOffset));
        }
    }
}
