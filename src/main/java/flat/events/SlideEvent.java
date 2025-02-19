package flat.events;

import flat.widget.Widget;

public class SlideEvent extends Event {

    public static final EventType SLIDE = new EventType();

    private float viewOffsetDimension;

    public SlideEvent(Widget source, float viewOffsetDimension) {
        super(source, SLIDE);
        this.viewOffsetDimension = viewOffsetDimension;
    }

    public float getViewOffsetDimension() {
        return viewOffsetDimension;
    }

    @Override
    public String toString() {
        return "SlideEvent [SLIDE]";
    }
}
