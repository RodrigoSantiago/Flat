package flat.uxml;

import flat.window.Application;

import java.util.ArrayList;

public class TaskList {
    private ArrayList<Runnable> tasks;

    public void add(Runnable task) {
        if (tasks == null) {
            tasks = new ArrayList<>();
        }
        tasks.add(task);
    }

    public void run() {
        if (tasks != null) {
            for (var task : tasks) {
                try {
                    task.run();
                } catch (Exception e) {
                    Application.handleException(e);
                }
            }
        }
    }
}
