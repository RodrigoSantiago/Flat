package flat.widget.stages.dialogs;

import flat.widget.Widget;
import flat.window.Application;

public interface UXWidgetRangeValueListener<W extends Widget, T> {
    public static <W extends Widget, T> void safeHandle(UXWidgetRangeValueListener<W, T> listener, W widget, T valueStart, T valueEnd) {
        if (listener != null) {
            try {
                listener.handle(widget, valueStart, valueEnd);
            } catch (Exception e) {
                Application.handleException(e);
            }
        }
    }

    void handle(W widget, T valueStart, T valueEnd);
}
