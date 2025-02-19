package flat.widget.value;

import flat.widget.enums.Direction;

public class VerticalScrollBar extends ScrollBar {
    @Override
    public Direction getDirection() {
        return Direction.VERTICAL;
    }
}
