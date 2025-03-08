package flat.widget.value;

import flat.animations.StateInfo;
import flat.events.PointerEvent;
import flat.events.SlideEvent;
import flat.graphics.Color;
import flat.graphics.Graphics;
import flat.math.Vector2;
import flat.uxml.*;
import flat.widget.Widget;
import flat.widget.enums.Direction;

public class ScrollBar extends Widget {

    private UXListener<SlideEvent> slideListener;
    private UXListener<SlideEvent> slideFilter;
    private UXValueListener<Float> viewOffsetListener;
    private float viewOffset;
    private float viewDimension;
    private float totalDimension;

    private float minRange;
    private int color = Color.white;

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
        setColor(attrs.getColor("color", info, getColor()));
    }

    @Override
    public void onDraw(Graphics graphics) {
        drawBackground(graphics);

        boolean hor = getDirection() == Direction.HORIZONTAL;

        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        if (width <= 0 || height <= 0) return;

        float w, h, x1, y1, xc, yc;
        float handleSize = totalDimension == 0 ? 1 : Math.max(minRange, Math.min(1, viewDimension / totalDimension));
        float moveOffset = totalDimension == 0 ? 0 : Math.max(0, Math.min(1 - handleSize, viewOffset / totalDimension));

        if (hor) {
            w = width * handleSize;
            h = height;

            x1 = x + width * moveOffset;
            y1 = y;
        } else {
            w = width;
            h = height * handleSize;

            x1 = x;
            y1 = y + height * moveOffset;
        }

        graphics.setTransform2D(getTransform());
        graphics.setColor(color);
        graphics.drawRoundRect(x1, y1, w, h, getRadiusTop(), getRadiusRight(), getRadiusBottom(), getRadiusLeft(), true);

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

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        if (this.color != color) {
            this.color = color;
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
        if (minRange < 0) minRange = 0;
        if (minRange > 1) minRange = 1;

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

        float handleSize = totalDimension == 0 ? 1 :
                Math.max(minRange, Math.min(1, viewDimension / totalDimension));
        float moveOffset = totalDimension == 0 ? 0 :
                Math.max(0, Math.min(1 - handleSize, viewOffset / totalDimension));

        if (hor) {
            pos = point.x;
            start = x;
            size = width;
            hStart = x + width * moveOffset;
            hSize = width * handleSize;
        } else {
            pos = point.y;
            start = y;
            size = height;
            hStart = y + height * moveOffset;
            hSize = height * handleSize;
        }

        if (event.getType() == PointerEvent.PRESSED) {
            grabOffset = Math.max(0, Math.min(1, (pos + ((float) 0) - hStart) / hSize));
            float target = ((pos - start) / size - handleSize * grabOffset) * totalDimension;
            slideTo(target);

        } else if (event.getType() == PointerEvent.DRAGGED) {
            float target = ((pos - start) / size - handleSize * grabOffset) * totalDimension;
            slideTo(target);

        } else if (event.getType() == PointerEvent.RELEASED) {
            grabOffset = 0;
        }
    }
}
