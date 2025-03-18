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
    private int color = Color.white;
    private float lineWidth;
    private int lineColor;
    private float lineFilledWidth;
    private int lineFilledColor;
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
        setColor(attrs.getColor("color", info, getColor()));
        setLineColor(attrs.getColor("line-color", info, getLineColor()));
        setLineFilledColor(attrs.getColor("line-filled-color", info, getLineFilledColor()));
        setLineWidth(attrs.getSize("line-width", info, getLineWidth()));
        setLineFilledWidth(attrs.getSize("line-filled-width", info, getLineFilledWidth()));
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

        float x1, y1, x2, y2;
        float hx1, hy1, hx2, hy2;
        float handleSize = totalDimension == 0 ? 1 : Math.max(minRange, Math.min(1, viewDimension / totalDimension));
        float moveOffset = totalDimension == 0 ? 0 : Math.max(0, Math.min(1 - handleSize, viewOffset / totalDimension));

        float lineW = Math.min(width, Math.min(height, getLineWidth()));
        float lineFilledW = Math.min(width, Math.min(height, getLineFilledWidth()));
        float lineH = getLineCap() == LineCap.BUTT ? 0 : lineW * 0.5f;
        boolean empty;
        boolean emptyH;
        if (hor) {
            x1 = x + lineH;
            x2 = x + width - lineH;
            y1 = y + height * 0.5f;
            y2 = y + height * 0.5f;
            empty = Math.abs(x1 - x2) < 0.001f;

            hx1 = Mathf.lerp(x1, x2, moveOffset);
            hx2 = Mathf.lerp(x1, x2, moveOffset + handleSize);
            hy1 = y + height * 0.5f;
            hy2 = y + height * 0.5f;
            emptyH = Math.abs(hx1 - hx2) < 0.001f;
        } else {
            x1 = x + width * 0.5f;
            x2 = x + width * 0.5f;
            y1 = y + lineH;
            y2 = y + height - lineH;
            empty = Math.abs(y1 - y2) < 0.001f;

            hx1 =  x + width * 0.5f;
            hx2 =  x + width * 0.5f;
            hy1 = Mathf.lerp(y1, y2, moveOffset);
            hy2 = Mathf.lerp(y1, y2, moveOffset + handleSize);
            emptyH = Math.abs(hy1 - hy2) < 0.001f;
        }

        graphics.setTransform2D(getTransform());
        if (Color.getAlpha(getLineColor()) > 0) {
            graphics.setColor(getLineColor());
            graphics.setStroke(new BasicStroke(lineW, getLineCap().ordinal(), 0));
            if (empty) {
                if (getLineCap() == LineCap.ROUND) {
                    graphics.drawEllipse(x1 - lineW * 0.5f, y1 - lineW * 0.5f, lineW, lineW, true);
                } else if (getLineCap() == LineCap.SQUARE) {
                    graphics.drawRect(x1 - lineW * 0.5f, y1 - lineW * 0.5f, lineW, lineW, true);
                }
            } else {
                graphics.drawLine(x1, y1, x2, y2);
            }
        }

        if (Color.getAlpha(getLineFilledColor()) > 0) {
            graphics.setColor(getLineFilledColor());
            graphics.setStroke(new BasicStroke(lineFilledW, getLineCap().ordinal(), 0));
            if (emptyH) {
                if (getLineCap() == LineCap.ROUND) {
                    graphics.drawEllipse(hx1 - lineFilledW * 0.5f, hy1 - lineFilledW * 0.5f, lineFilledW, lineFilledW, true);
                } else if (getLineCap() == LineCap.SQUARE) {
                    graphics.drawRect(hx1 - lineFilledW * 0.5f, hy1 - lineFilledW * 0.5f, lineFilledW, lineFilledW, true);
                }
            } else {
                graphics.drawLine(hx1, hy1, hx2, hy2);
            }
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

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        if (this.color != color) {
            this.color = color;
            invalidate(false);
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

    public float getLineFilledWidth() {
        return lineFilledWidth;
    }

    public void setLineFilledWidth(float lineFilledWidth) {
        if (this.lineFilledWidth != lineFilledWidth) {
            this.lineFilledWidth = lineFilledWidth;
            invalidate(false);
        }
    }

    public int getLineFilledColor() {
        return lineFilledColor;
    }

    public void setLineFilledColor(int lineFilledColor) {
        if (this.lineFilledColor != lineFilledColor) {
            this.lineFilledColor = lineFilledColor;
            invalidate(true);
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
