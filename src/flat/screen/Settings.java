package flat.screen;

public class Settings {

    private Class<?> activityClass;

    public Runnable start;
    public int multsamples;
    public int width;
    public int height;
    public boolean transparent;

    public <T extends Activity> Settings(Class<T> activityClass) {
        this(activityClass, null);
    }

    public <T extends Activity> Settings(Class<T> activityClass, Runnable start) {
        this(activityClass, start, 0);
    }

    public <T extends Activity> Settings(Class<T> activityClass, Runnable start, int multsamples) {
        this(activityClass, start, multsamples, 800, 600);
    }

    public <T extends Activity> Settings(Class<T> activityClass, Runnable start, int multsamples, int width, int height) {
        this(activityClass, start, multsamples, width, height, false);
    }

    public <T extends Activity> Settings(Class<T> activityClass, Runnable start, int multsamples, int width, int height, boolean transparent) {
        this.activityClass = activityClass;
        this.start = start;
        this.multsamples = multsamples;
        this.width = width;
        this.height = height;
        this.transparent = transparent;
    }

    public <T extends Activity> void setActivityClass(Class<T> activityClass) {
        this.activityClass = activityClass;
    }

    public Class<?> getActivityClass() {
        return activityClass;
    }
}
