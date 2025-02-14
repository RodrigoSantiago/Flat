package flat.widget.stages;

import flat.widget.Group;

public abstract class Stage extends Group {

    public boolean isShown() {
        return false;
    }

    public boolean isModal() {
        return false;
    }

    public void hide() {

    }
}
