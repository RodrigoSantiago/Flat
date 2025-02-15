package flat.widget;

import flat.window.Activity;

public abstract class Stage extends Group {

    public boolean isShown() {
        return false;
    }

    public boolean isModal() {
        return false;
    }

    public void hide() {

    }

    protected void setToShow(Activity activity) {
        activity.getScene().addStage(this);
    }
}
