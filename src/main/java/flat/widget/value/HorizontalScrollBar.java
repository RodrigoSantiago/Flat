package flat.widget.value;

import flat.widget.enums.Direction;

public class HorizontalScrollBar extends ScrollBar {
    @Override
    public Direction getDirection() {
        return Direction.HORIZONTAL;
    }
}
