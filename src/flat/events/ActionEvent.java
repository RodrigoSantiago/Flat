package flat.events;

import flat.widget.Widget;

public class ActionEvent extends Event {
    public ActionEvent(Widget source, int type) {
        super(source, type);
    }

    @Override
    public ActionEvent recycle(Widget source) {
        super.recycle(source);
        return this;
    }
}
