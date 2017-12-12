package flat.screen;

public abstract class Application {

    static {
        System.loadLibrary("flat");
    }

    Window window;
    int samples = 0;
    boolean decorated = true, resizable = true, fullscreen = false;

    private static Application application;

    public abstract void start(Window window);

    @Deprecated
    public void setParams(int samples, boolean decorated, boolean resizable, boolean fullscreen) {
        this.samples = samples;
        this.decorated = decorated;
        this.resizable = resizable;
        this.fullscreen = fullscreen;
    }

    public static void launch(Application app) {
        if  (application != null) {
            throw new RuntimeException("The application is already defined");
        } else if (app == null) {
            throw new IllegalArgumentException("The application should not be null");
        }
        application = app;
        application.window = new Window(application);
        application.window.init();
        try {
            application.window.launch();
        } catch (Exception e) {
            e.printStackTrace();
        }
        application.window.finish();
    }

    public static Application getApplication() {
        return application;
    }

    public Window getWindow() {
        return window;
    }

}
