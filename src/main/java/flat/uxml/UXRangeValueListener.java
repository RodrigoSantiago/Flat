package flat.uxml;

import flat.window.Application;

public interface UXRangeValueListener<T> {
    public static <T> void safeHandle(UXRangeValueListener<T> listener, T valueStart, T valueEnd) {
        if (listener != null) {
            try {
                listener.handle(valueStart, valueEnd);
            } catch (Exception e) {
                Application.handleException(e);
            }
        }
    }

    void handle(T valueStart, T valueEnd);
}
