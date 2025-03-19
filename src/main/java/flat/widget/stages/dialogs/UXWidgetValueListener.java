package flat.widget.stages.dialogs;

import flat.widget.Widget;
import flat.window.Application;

public interface UXWidgetValueListener<W extends Widget, T> {

    public static <W extends Widget, T> void safeHandle(UXWidgetValueListener<W, T> listener, W widget, T value) {
        if (listener != null) {
            try {
                listener.handle(widget, value);
            } catch (Exception e) {
                Application.handleException(e);
            }
        }
    }

    void handle(W widget, T value);
}
