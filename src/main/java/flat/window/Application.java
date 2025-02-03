package flat.window;

import flat.backend.FlatLibrary;
import flat.backend.WL;
import flat.exception.FlatException;
import flat.graphics.context.Context;
import flat.resources.ResourcesManager;
import flat.window.event.EventData;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Application {

    private static Window assignedWindow;
    private static ArrayList<Window> windowsAdd;
    private static ArrayList<Window> windowsRemove;
    private static ArrayList<Window> windows ;
    private static HashMap<Long, Window> windowsMap;

    private static int vsync;
    private static int currentVsync;

    private static ResourcesManager resources;
    private static boolean loopActive;
    private static long lastLoopTime;

    private static void init(Settings settings) {
        resources = settings.createResources();

        try {
            FlatLibrary.load(resources.getFlatLibraryFile());
        } catch (Throwable e) {
            throw new FlatException("Failed to load Flat Library");
        }

        if (!WL.Init()) {
            throw new FlatException("Invalid context creation");
        }

        assignedWindow = null;
        windowsAdd = new ArrayList<>();
        windowsRemove = new ArrayList<>();
        windows = new ArrayList<>();
        windowsMap = new HashMap<>();

        currentVsync = -1;
        setVsync(settings.getVsync());

        WL.SetErrorCallback((error) -> {
            System.err.println(error);
        });
        WL.SetMouseButtonCallback((window, button, action, mods) -> {
            Window win = windowsMap.get(window);
            if (win != null) {
                win.addEvent(EventData.getMouseButton(button + 1, action));
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
            if ((win == null || win.isStarted()) && loopActive) {
                refreshResize();
            }
        });
        WL.SetWindowCloseCallback((window) -> {
            Window win = windowsMap.get(window);
            return win == null || win.onRequestClose(true);
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (loopActive) {
                loopActive = false;
                finish();
            }
        }));
    }

    private static void finish() {
        WL.Finish();
    }

    public static void launch(Settings settings) {
        init(settings);

        try {
            loopActive = true;
            lastLoopTime = System.currentTimeMillis();
            assignWindow(settings.createWindow());

            while (loopActive) {
                refresh();
            }
        } finally {
            loopActive = false;
            finish();
        }
    }

    public static ResourcesManager getResourcesManager() {
        return resources;
    }

    static Context createContext(Window window) {
        windowsAdd.add(window);
        return Context.create(window, window.getWindowId(), window.getSvgId());
    }

    private static void removeWindow(Window window) {
        windowsRemove.add(window);
    }

    private static void assignWindow(Window window) {
        if (assignedWindow != window) {
            assignedWindow = window;
            WL.WindowAssign(window.getWindowId());
        }
        if (currentVsync != vsync) {
            currentVsync = vsync;
            WL.SetVsync(Math.max(0, vsync));
        }
    }

    private static void updateWindowList() {
        for (Window window : windows) {
            if (window.isClosed()) {
                removeWindow(window);
            }
        }

        for (Window window : windowsAdd) {
            windows.add(window);
            windowsMap.put(window.getWindowId(), window);
        }
        windowsAdd.clear();

        for (Window window : windowsRemove) {
            windows.remove(window);
            windowsMap.remove(window.getWindowId(), window);

            if (assignedWindow == window) {
                assignedWindow = null;
            }
            window.dispose();
        }
        windowsRemove.clear();
    }

    private static boolean iterateWindows() {
        updateWindowList();

        long now = System.currentTimeMillis();
        float loopTime = (now - lastLoopTime) / 1000f;
        lastLoopTime = now;

        boolean anyAnimation = false;
        for (Window window : windows) {
            assignWindow(window);

            anyAnimation = window.loop(loopTime) || anyAnimation;
        }
        return anyAnimation;
    }

    private static void refresh() {
        boolean anyAnimation = iterateWindows();

        if (assignedWindow != null) {
            WL.HandleEvents(anyAnimation || vsync > 0 ? 0 : 0.25f);
        } else {
            loopActive = false;
        }
    }

    private static void refreshResize() {
        boolean anyAnimation = iterateWindows();

        if (assignedWindow == null) {
            loopActive = false;
        }
    }

    public static void setVsync(int vsync) {
        Application.vsync = vsync;
    }

    public static int getVsync() {
        return vsync;
    }

    public static List<Window> getAssignedWindows() {
        return new ArrayList<>(windows);
    }

    public static class Settings {

        private final File resourceFile;

        private final ActivityFactory factory;
        private final int multiSamples;
        private final int width;
        private final int height;
        private final boolean transparent;
        private final int vsync;

        public Settings(ActivityFactory factory, int width, int height) {
            this(null, factory, width, height, 0, 2, false);
        }

        public Settings(ActivityFactory factory, int width, int height, int vsync, int multiSamples, boolean transparent) {
            this(null, factory, width, height, vsync, multiSamples, transparent);
        }

        public Settings(File resources, ActivityFactory factory, int width, int height) {
            this(resources, factory, width, height, 0, 2, false);
        }

        public Settings(File resources, ActivityFactory factory, int width, int height, int vsync, int multiSamples, boolean transparent) {
            this.resourceFile = resources;
            this.factory = factory;
            this.width = width;
            this.height = height;
            this.vsync = vsync;
            this.multiSamples = multiSamples;
            this.transparent = transparent;
        }

        public int getMultiSamples() {
            return multiSamples;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public boolean isTransparent() {
            return transparent;
        }

        public int getVsync() {
            return vsync;
        }

        ResourcesManager createResources() {
            return new ResourcesManager(resourceFile);
        }

        Window createWindow() {
            return new Window(factory, width, height, multiSamples, transparent);
        }
    }
}
