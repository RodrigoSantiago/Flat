package flat.widget;

import flat.window.Activity;

public class WidgetSupport {
    public static void setParent(Widget widget, Parent parent) {
        widget.setParent(parent);
    }

    public static void setGroup(Widget widget, Group group) {
        widget.onGroupChangeLocal(widget.getGroup(), group);
    }

    public static void setActivity(Widget widget, Activity activity) {
        widget.onActivityChangeLocal(widget.getActivity(), activity);
    }
}
