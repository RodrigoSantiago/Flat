package flat.widget.value;

import flat.events.PointerEvent;
import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.widget.Group;
import flat.widget.Widget;
import flat.widget.enums.Direction;
import flat.widget.enums.Visibility;

public abstract class Splitter extends Widget {

    private Widget target;
    private boolean inverse;

    private boolean grabSplit;
    private float grabV;
    private float grabP;

    public abstract Direction getDirection();

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);

        UXAttrs attrs = getAttrs();

        String targetId = attrs.getAttributeString("target-id", null);
        if (targetId != null) {
            Group group = getGroup();
            if (group != null) {
                Widget widget = group.findById(targetId);
                if (widget != null) {
                    setTarget(widget);
                }
            }
        }
        setInverse(attrs.getAttributeBool("inverse", isInverse()));
    }
    
    @Override
    public void pointer(PointerEvent event) {
        super.pointer(event);
        if (target == null) return;
        if (!event.isConsumed() && event.getPointerID() == 1 && event.getType() == PointerEvent.PRESSED) {
            grabSplit = true;
            if (getDirection() == Direction.HORIZONTAL) {
                grabP = target.getWidth();
                grabV = event.getX();
            } else {
                grabP = target.getHeight();
                grabV = event.getY();
            }
        }
        if (event.getPointerID() == 1 && event.getType() == PointerEvent.RELEASED) {
            grabSplit = false;
        }
        if (grabSplit && event.getType() == PointerEvent.DRAGGED) {
            float m = isInverse() ? -1 : 1;
            if (getDirection() == Direction.HORIZONTAL) {
                this.target.setFollowStyleProperty("width", false);
                target.setPrefWidth(Math.max(1, grabP + (event.getX() - grabV) * m));
            } else {
                this.target.setFollowStyleProperty("height", false);
                target.setPrefHeight(Math.max(1, grabP + (event.getY() - grabV) * m));
            }
        }
    }
    
    @Override
    public void setVisibility(Visibility visibility) {
        if (visibility != getVisibility()) {
            super.setVisibility(visibility);
            if (target == null) return;
            if (getVisibility() == Visibility.GONE) {
                if (getDirection() == Direction.HORIZONTAL) {
                    this.target.setFollowStyleProperty("width", true);
                } else {
                    this.target.setFollowStyleProperty("height", true);
                }
            }
        }
    }
    
    public void setTarget(Widget target) {
        if (this.target != target) {
            grabSplit = false;
            this.target = target;
        }
    }

    public Widget getTarget() {
        return target;
    }

    public void setInverse(boolean inverse) {
        this.inverse = inverse;
    }

    public boolean isInverse() {
        return inverse;
    }
}
