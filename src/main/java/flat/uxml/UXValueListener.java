package flat.uxml;

import flat.window.Application;

public interface UXValueListener<T> {

    public static <T> void safeHandle(UXValueListener<T> listener, ValueChange<T> event) {
        if (listener != null) {
            try {
                listener.handle(event);
            } catch (Exception e) {
                Application.handleException(e);
            }
        }
    }

    void handle(ValueChange<T> event);
}
