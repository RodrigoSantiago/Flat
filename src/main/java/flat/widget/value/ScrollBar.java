package flat.widget.value;

import flat.animations.StateInfo;
import flat.events.ActionEvent;
import flat.events.PointerEvent;
import flat.graphics.Color;
import flat.graphics.SmartContext;
import flat.math.Vector2;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.uxml.UXListener;
import flat.widget.Widget;
import flat.widget.enums.Direction;

public class ScrollBar extends Widget {

    private UXListener<ActionEvent> scrollOffsetListener;
    private float viewOffset;
    private float viewDimension;
    private float totalDimension;
    private float minSize;
    private int color = Color.white;
    private Direction direction = Direction.VERTICAL;

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);

        UXAttrs attrs = getAttrs();
        setTotalDimension(attrs.getAttributeSize("total-dimension", getTotalDimension()));
        setViewDimension(attrs.getAttributeSize("view-dimension", getViewDimension()));
        setViewOffset(attrs.getAttributeSize("view-offset", getViewOffset()));
        setDirection(attrs.getAttributeConstant("direction", getDirection()));
    }

    @Override
    public void applyStyle() {
        super.applyStyle();

        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();
        setMinSize(attrs.getSize("min-size", info, getMinSize()));
        setColor(attrs.getColor("color", info, getColor()));
    }

    @Override
    public void onDraw(SmartContext context) {
        backgroundDraw(context);

        boolean hor = direction == Direction.HORIZONTAL || direction == Direction.IHORIZONTAL;
        boolean inverse = direction == Direction.IHORIZONTAL || direction == Direction.IVERTICAL;

        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        float w, h, x1, y1, xc, yc;
        float handleSize = totalDimension == 0 ? 1 : Math.max(minSize, Math.min(1, viewDimension / totalDimension));
        float moveOffset = totalDimension == 0 ? 0 : Math.max(0, Math.min(1 - handleSize, viewOffset / totalDimension));
        if (inverse) {
            moveOffset = (1 - handleSize) - moveOffset;
        }

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

        context.setTransform2D(getTransform());
        context.setColor(color);
        context.drawRoundRect(x1, y1, w, h,
                getRadiusTop(), getRadiusRight(), getRadiusBottom(), getRadiusLeft(), true);

    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        if (direction == null) direction = Direction.VERTICAL;

        if (this.direction != direction) {
            this.direction = direction;
            invalidate(true);
        }
    }

    public float getViewOffset() {
        return viewOffset;
    }

    public void setViewOffset(float viewOffset) {
        if (this.viewOffset != viewOffset) {
            this.viewOffset = viewOffset;
            invalidate(false);
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
        }
    }

    public float getMinSize() {
        return minSize;
    }

    public void setMinSize(float minSize) {
        if (minSize < 0) minSize = 0;
        if (minSize > 1) minSize = 1;

        if (this.minSize != minSize) {
            this.minSize = minSize;
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
        }
    }

    public void scrollTo(float dimeionsOffset) {
        if (dimeionsOffset > totalDimension - viewDimension) dimeionsOffset = totalDimension - viewDimension;
        if (dimeionsOffset < 0) dimeionsOffset = 0;
        setViewOffset(dimeionsOffset);
        fireScrollOffset();
    }

    public void scroll(float dimeionsOffset) {
        scrollTo(getViewOffset() + dimeionsOffset);
    }

    public void fireScrollOffset() {
        if (scrollOffsetListener != null) {
            scrollOffsetListener.handle(new ActionEvent(this));
        }
    }

    public UXListener<ActionEvent> getScrollOffsetListener() {
        return scrollOffsetListener;
    }

    public void setScrollOffsetListener(UXListener<ActionEvent> scrollListener) {
        this.scrollOffsetListener = scrollListener;
    }

    @Override
    public void firePointer(PointerEvent pointerEvent) {
        super.firePointer(pointerEvent);
        if (pointerEvent.isConsumed()) {
            return;
        }

        Vector2 point = new Vector2(pointerEvent.getX(), pointerEvent.getY());
        screenToLocal(point);

        boolean hor = direction == Direction.HORIZONTAL || direction == Direction.IHORIZONTAL;
        boolean inverse = direction == Direction.IHORIZONTAL || direction == Direction.IVERTICAL;

        final float x = getInX();
        final float y = getInY();
        final float width = getInWidth();
        final float height = getInHeight();

        float hStart, hSize, pos, start, size;

        float handleSize = totalDimension == 0 ? 1 :
                Math.max(minSize, Math.min(1, viewDimension / totalDimension));
        float moveOffset = totalDimension == 0 ? 0 :
                Math.max(0, Math.min(1 - handleSize, viewOffset / totalDimension));

        if (hor) {
            pos = inverse ? -(point.x - (x + width) * 0.5f) + (x + width) * 0.5f : point.x;
            start = x;
            size = width;
            hStart = x + width * moveOffset;
            hSize = width * handleSize;
        } else {
            pos = inverse ? -(point.y - (y + height) * 0.5f) + (y + height) * 0.5f : point.y;
            start = y;
            size = height;
            hStart = y + height * moveOffset;
            hSize = height * handleSize;
        }

        if (pointerEvent.getType() == PointerEvent.PRESSED) {
            grabOffset = Math.max(0, Math.min(1, (pos - hStart) / hSize));
            float target = (pos / size - handleSize * grabOffset) * totalDimension;
            scrollTo(target);

        } else if (pointerEvent.getType() == PointerEvent.DRAGGED) {
            float target = ((pos - start) / size - handleSize * grabOffset) * totalDimension;
            scrollTo(target);

        } else if (pointerEvent.getType() == PointerEvent.RELEASED) {
            grabOffset = 0;
        }
    }

    private float grabOffset;
}
