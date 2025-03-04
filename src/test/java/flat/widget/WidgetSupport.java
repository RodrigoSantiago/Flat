package flat.widget;

import flat.window.Activity;

public class WidgetSupport {
    public static void setActivity(Widget widget, Activity activity) {
        widget.onActivityChangeLocal(widget.getActivity(), activity);
    }
}
