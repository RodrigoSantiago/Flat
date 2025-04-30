package flat.concurrent;

import flat.window.Application;
import flat.window.Window;

import java.util.concurrent.atomic.AtomicBoolean;

public class ProgressTask<T> implements Runnable {

    private final Window window;
    private final AsyncProcess<T> asyncProcess;
    private final AsyncHandle<T> onComplete;
    private final AsyncHandle<Exception> onFail;
    private final ProgressReport report = new ProgressReport();
    private final AtomicBoolean consumed = new AtomicBoolean();
    private final AtomicBoolean done = new AtomicBoolean();
    private final AtomicBoolean close = new AtomicBoolean();

    public ProgressTask(AsyncProcess<T> asyncProcess) {
        this(null, asyncProcess, null, null);
    }

    public ProgressTask(AsyncProcess<T> asyncProcess, AsyncHandle<T> onComplete) {
        this(null, asyncProcess, onComplete, null);
    }

    public ProgressTask(AsyncProcess<T> asyncProcess, AsyncHandle<T> onComplete, AsyncHandle<Exception> onFail) {
        this(null, asyncProcess, onComplete, onFail);
    }

    public ProgressTask(AsyncProcessVoid asyncProcess) {
        this(null, asyncProcess, null, null);
    }

    public ProgressTask(AsyncProcessVoid asyncProcess, AsyncHandle<T> onComplete) {
        this(null, asyncProcess, onComplete, null);
    }

    public ProgressTask(AsyncProcessVoid asyncProcess, AsyncHandle<T> onComplete, AsyncHandle<Exception> onFail) {
        this(null, asyncProcess, onComplete, onFail);
    }

    public ProgressTask(Window window, AsyncProcessVoid asyncProcess) {
        this(window, asyncProcess, null, null);
    }

    public ProgressTask(Window window, AsyncProcessVoid asyncProcess, AsyncHandle<T> onComplete) {
        this(window, asyncProcess, onComplete, null);
    }

    public ProgressTask(Window window, AsyncProcessVoid asyncProcess, AsyncHandle<T> onComplete, AsyncHandle<Exception> onFail) {
        this.window = window;
        this.asyncProcess = (report) -> {
            asyncProcess.run(report);
            return null;
        };
        this.onComplete = onComplete;
        this.onFail = onFail;
    }

    public ProgressTask(Window window, AsyncProcess<T> asyncProcess) {
        this(window, asyncProcess, null, null);
    }

    public ProgressTask(Window window, AsyncProcess<T> asyncProcess, AsyncHandle<T> onComplete) {
        this(window, asyncProcess, onComplete, null);
    }

    public ProgressTask(Window window, AsyncProcess<T> asyncProcess, AsyncHandle<T> onComplete, AsyncHandle<Exception> onFail) {
        this.window = window;
        this.asyncProcess = asyncProcess;
        this.onComplete = onComplete;
        this.onFail = onFail;
    }

    @Override
    public void run() {
        if (!consumed.compareAndSet(false, true)) {
            throw new RuntimeException("A progress task cannot be reused");
        }
        try {
            T result = asyncProcess.run(report);
            done.set(true);
            if (onComplete != null && !close.get()) {
                if (window != null) {
                    if (!window.isClosed()) window.runSync(() -> onComplete.handle(result));
                } else {
                    Application.runOnContextSync(() -> onComplete.handle(result));
                }
            }
        } catch (Exception e) {
            done.set(true);
            if (onFail != null && !close.get()) {
                if (window != null) {
                    if (!window.isClosed()) window.runSync(() -> onFail.handle(e));
                } else {
                    Application.runOnContextSync(() -> onFail.handle(e));
                }
            }
        }
    }

    public void requestCancel() {
        close.set(true);
        report.requestCancel();
    }

    public float getProgress() {
        return report.getProgress();
    }

    public boolean isDone() {
        return done.get();
    }
}
