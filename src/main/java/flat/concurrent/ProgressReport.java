package flat.concurrent;

import java.util.concurrent.atomic.AtomicBoolean;

public class ProgressReport {
    private float progress;
    private final AtomicBoolean cancel = new AtomicBoolean();

    public synchronized float getProgress() {
        return progress;
    }

    public synchronized void setProgress(float progress) {
        this.progress = progress;
    }

    public boolean isRequestCancel() {
        return cancel.get();
    }

    public void requestCancel() {
        cancel.set(true);
    }
}
