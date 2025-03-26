package flat.graphics.context;

import flat.window.Application;

class DisposeTask implements Runnable {
    private final Runnable task;
    private boolean done;

    DisposeTask(Runnable task) {
        this.task = task;
    }

    @Override
    public void run() {
        Application.runOnContextSync(() -> {
            if (!done) {
                done = true;
                if (Application.getCurrentContext() != null) {
                    task.run();
                }
            }
        });
    }
}
