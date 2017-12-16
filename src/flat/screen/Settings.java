package flat.screen;

public class Settings {

    private Class activityClass;

    public Start start;
    public int multsamples;
    public int width;
    public int height;
    public boolean decorated;
    public boolean resizable;

    public <T extends Activity> Settings(Class<T> activityClass) {
        this(activityClass, null);
    }

    public <T extends Activity> Settings(Class<T> activityClass, Start start) {
        this(activityClass, start, 1);
    }

    public <T extends Activity> Settings(Class<T> activityClass, Start start, int multsamples) {
        this(activityClass, start, multsamples, 800, 600);
    }

    public <T extends Activity> Settings(Class<T> activityClass, Start start, int multsamples, int width, int height) {
        this(activityClass, start, multsamples, width, height, true, true);
    }

    public <T extends Activity> Settings(Class<T> activityClass, Start start, int multsamples, int width, int height, boolean decorated, boolean resizable) {
        this.activityClass = activityClass;
        this.start = start;
        this.multsamples = multsamples;
        this.width = width;
        this.height = height;
        this.decorated = decorated;
        this.resizable = resizable;
    }

    public <T extends Activity> void setActivityClass(Class<T> activityClass) {
        this.activityClass = activityClass;
    }

    public Class getActivityClass() {
        return activityClass;
    }

    public interface Start {
        void start(Application application);
    }
}
