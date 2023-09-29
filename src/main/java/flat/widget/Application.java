package flat.widget;

import flat.backend.WL;
import flat.resources.ResourcesManager;
import flat.widget.window.*;
import sun.misc.Signal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public final class Application {

    static {
        try {
            InputStream in = ResourcesManager.getInput("flat.dll");
            byte[] buffer = new byte[1024];
            int read = -1;
            File temp = File.createTempFile("flat.dll", "");
            FileOutputStream fos = new FileOutputStream(temp);
            while ((read = in.read(buffer)) != -1) {
                fos.write(buffer, 0, read);
            }
            fos.close();
            in.close();
            System.load(temp.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            System.loadLibrary("flat");
        }
    }

    private static Window assignedWindow;
    private static ArrayList<Window> windowsAdd = new ArrayList<>();
    private static ArrayList<Window> windowsRemove = new ArrayList<>();
    private static ArrayList<Window> windows = new ArrayList<>();
    private static HashMap<Long, Window> windowsMap = new HashMap<>();

    private static int vsync;
    private static int currentVsync;

    private Application() {

    }

    private static void init(Settings settings) {
        if (settings.width <= 0 || settings.height <= 0) {
            throw new RuntimeException("Invalid application settings (Negative Screen Size)");
        }
        if (settings.multiSamples < 0) {
            throw new RuntimeException("Invalid application settings (Negative Multi Samples)");
        }
        if (settings.vsync < 0) {
            throw new RuntimeException("Invalid application settings (Negative Vsync)");
        }
        ResourcesManager.setResources(settings.resources);

        if (!WL.Init()) {
            throw new RuntimeException("Invalid context creation");
        }

        WL.SetErrorCallback((error) -> {
            System.err.println(error);
        });
        WL.SetMouseButtonCallback((window, button, action, mods) -> {
            Window win = windowsMap.get(window);
            if (win != null) {
                win.addEvent(EventData.getMouseButton(button + 1, action, mods));
            }
        });
        WL.SetCursorPosCallback((window, x, y) -> {
            Window win = windowsMap.get(window);
            if (win != null) {
                win.addEvent(EventData.getMouseMove((float) x, (float) y), (float) x, (float) y);
            }
        });
        WL.SetScrollCallback((window, x, y) -> {
            Window win = windowsMap.get(window);
            if (win != null) {
                win.addEvent(EventData.getMouseScroll((float)x, (float)y));
            }
        });
        WL.SetDropCallback((window, names) -> {
            Window win = windowsMap.get(window);
            if (win != null) {
                win.addEvent(EventData.getMouseDrop(names));
            }
        });
        WL.SetKeyCallback((window, key, scancode, action, mods) -> {
            Window win = windowsMap.get(window);
            if (win != null) {
                win.addEvent(EventData.getKey(key, scancode, action, mods));
            }
        });
        WL.SetCharModsCallback((window, codepoint, mods) -> {
            Window win = windowsMap.get(window);
            if (win != null) {
                win.addEvent(EventData.getCharMode(codepoint, mods));
            }
        });
        WL.SetWindowSizeCallback((window, width, height) -> {
            Window win = windowsMap.get(window);
            if (win != null) {
                win.addEvent(EventData.getSize(width, height));
            }
        });
        WL.SetWindowCloseCallback((window) -> {
            Window win = windowsMap.get(window);
            if (win != null) {
                return win.requestClose();
            } else {
                return true;
            }
        });
    }

    private static void finish() {
        WL.Finish();
    }

    public static void launch(Settings settings) {
        Signal.handle(new Signal("INT"), signal -> {
            for (Window window : windows) {
                window.dispose();
            }
            finish();
            System.exit(0);
        });

        try {
            init(settings);

            createWindow(settings.factory, settings.width, settings.height, settings.multiSamples, settings.transparent);

            loop();
        } catch (Exception e) {
            throw new RuntimeException(e);

        } finally {
            finish();
        }
    }

    public static Window createWindow(ActivityFactory factory, int width, int height, int multiSamples, boolean transparent) {
        Window window = new Window(factory, width, height, multiSamples, transparent);
        windowsAdd.add(window);
        windowsRemove.remove(window);
        return window;
    }

    public static void removeWindow(Window window) {
        windowsAdd.remove(window);
        windowsRemove.add(window);
    }

    static void assignWindow(Window window) {
        if (assignedWindow != window) {
            assignedWindow = window;
            WL.WindowAssign(window.windowId);
        }
    }

    static void loop() {
        long then = System.currentTimeMillis();
        long loopTime, now;

        while (windows.size() > 0 || windowsAdd.size() > 0) {

            for (Window window : windows) {
                if (window.isClosed()) {
                    removeWindow(window);
                }
            }

            for (Window window : windowsAdd) {
                windows.add(window);
                windowsMap.put(window.windowId, window);
            }
            windowsAdd.clear();

            for (Window window : windowsRemove) {
                windows.remove(window);
                windowsMap.remove(window.windowId, window);
                window.dispose();
            }
            windowsRemove.clear();

            if (windows.isEmpty()) {
                return;
            }

            now = System.currentTimeMillis();
            loopTime = now - then;
            then = now;

            boolean anyDraw = false;

            currentVsync = -1;
            for (Window window : windows) {
                anyDraw = anyDraw | window.loop(loopTime);
            }

            WL.HandleEvents(0);
        }
    }

    public static void setVsync(int vsync) {
        Application.vsync = vsync;
    }

    public static int getVsync() {
        return vsync;
    }

    static void assignVsync() {
        if (currentVsync != vsync) {
            currentVsync = vsync;
            WL.SetVsync(vsync);
        }
    }

    public static class Settings {

        public ActivityFactory factory;
        public File resources;
        public int multiSamples;
        public int width;
        public int height;
        public boolean transparent;
        public int vsync;

        public Settings(ActivityFactory factory) {
            this(factory, null);
        }

        public Settings(ActivityFactory factory, File resources) {
            this(factory, resources, 800, 600);
        }

        public Settings(ActivityFactory factory, File resources, int width, int height) {
            this(factory, resources, width, height, 0);
        }

        public Settings(ActivityFactory factory, File resources, int width, int height, int vsync) {
            this(factory, resources, width, height, vsync, 1);
        }

        public Settings(ActivityFactory factory, File resources, int width, int height, int vsync, int multiSamples) {
            this(factory, resources, width, height, vsync, multiSamples, false);
        }

        public Settings(ActivityFactory factory, File resources, int width, int height, int vsync, int multiSamples, boolean transparent) {
            this.factory = factory;
            this.resources = resources;
            this.width = width;
            this.height = height;
            this.vsync = vsync;
            this.multiSamples = multiSamples;
            this.transparent = transparent;
        }
    }
}
