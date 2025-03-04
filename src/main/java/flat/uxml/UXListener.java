package flat.uxml;

import flat.window.Application;

public interface UXListener<T> {

    public static <T> void safeHandle(UXListener<T> listener, T event) {
        if (listener != null) {
            try {
                listener.handle(event);
            } catch (Exception e) {
                Application.handleException(e);
            }
        }
    }

    void handle(T event);
}
