package flat.application;

import java.io.File;

public class Settings {

    public final File resources;
    public Class<?> activityClass;
    public Runnable start;
    public int multsamples;
    public int width;
    public int height;
    public boolean transparent;
    public int vsync;

    public <T extends Activity> Settings(File resources) {
        this(resources, null, null);
    }

    public <T extends Activity> Settings(File resources, Class<T> activityClass) {
        this(resources, activityClass, null);
    }

    public <T extends Activity> Settings(File resources, Class<T> activityClass, Runnable start) {
        this(resources, activityClass, start, 0);
    }

    public <T extends Activity> Settings(File resources, Class<T> activityClass, Runnable start, int multsamples) {
        this(resources, activityClass, start, multsamples, 800, 600);
    }

    public <T extends Activity> Settings(File resources, Class<T> activityClass, Runnable start, int multsamples, int width, int height) {
        this(resources, activityClass, start, multsamples, width, height, false);
    }

    public <T extends Activity> Settings(File resources, Class<T> activityClass, Runnable start, int multsamples, int width, int height, boolean transparent) {
        this.resources = resources;
        this.activityClass = activityClass;
        this.start = start;
        this.multsamples = multsamples;
        this.width = width;
        this.height = height;
        this.transparent = transparent;
    }
}
