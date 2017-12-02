package flat.screen;

import flat.backend.GL;
import flat.backend.SVG;
import flat.backend.WL;

public abstract class Application {

    static {
        WL.load();
        GL.load();
        SVG.load();
    }

    Window window;
    int vsync = 1, samples = 0;
    boolean decorated = true, resizable = true, fullscreen = false;

    private static Application application;

    public abstract void start(Window window);

    @Deprecated
    public void setParams(int vsync, int samples, boolean decorated, boolean resizable, boolean fullscreen) {
        this.vsync = vsync;
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
        application.window.launch();
    }

    public static Application getApplication() {
        return application;
    }

    public Window getWindow() {
        return window;
    }

}
