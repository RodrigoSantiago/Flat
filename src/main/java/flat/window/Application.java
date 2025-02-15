package flat.window;

import flat.backend.FlatLibrary;
import flat.backend.WL;
import flat.exception.FlatException;
import flat.graphics.context.Context;
import flat.resources.ResourcesManager;
import flat.window.event.EventData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Application {

    private static Window assignedWindow;
    private static ArrayList<Window> windowsAdd;
    private static ArrayList<Window> windowsRemove;
    private static ArrayList<Window> windows;
    private static HashMap<Long, Window> windowsMap;
    private static final ArrayList<Runnable> vsyncRun = new ArrayList<>();
    private static final ArrayList<Runnable> vsyncRunTemp = new ArrayList<>();

    private static int vsync;
    private static int currentVsync;

    private static ResourcesManager resources;
    private static boolean loopActive;
    private static long lastLoopTime;

    public static void init() {
        init(new ResourcesManager());
    }

    public static void init(ResourcesManager resourcesManager) {
        resources = resourcesManager;

        try {
            FlatLibrary.load(resources.getFlatLibraryFile());
        } catch (Throwable e) {
            throw new FlatException("Failed to load Flat Library");
        }

        if (!WL.Init()) {
            throw new FlatException("Invalid context creation");
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (loopActive) {
                loopActive = false;
                finish();
            }
        }));

        loopActive = true;
        assignedWindow = null;
        windowsAdd = new ArrayList<>();
        windowsRemove = new ArrayList<>();
        windows = new ArrayList<>();
        windowsMap = new HashMap<>();

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
    }

    private static void finish() {
        WL.Finish();
    }

    public static void launch(WindowSettings settings) {
        try {
            createWindow(settings);

            lastLoopTime = System.currentTimeMillis();

            while (loopActive) {
                refresh();
            }

        } finally {
            loopActive = false;
            finish();
        }
    }

    public static Window createWindow(WindowSettings settings) {
        Window window = Window.create(settings);
        windowsAdd.add(window);

        return window;
    }

    public static ResourcesManager getResourcesManager() {
        return resources;
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
            if (!window.isClosed()) {
                windows.add(window);
                windowsMap.put(window.getWindowId(), window);
            }
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

        long now = System.nanoTime();
        float loopTime = (now - lastLoopTime) / 1_000_000_000.0f;
        lastLoopTime = now;

        boolean anyAnimation = false;
        for (Window window : windows) {
            assignWindow(window);

            try {
                anyAnimation = window.loop(loopTime) || anyAnimation;
            } catch (Exception e) {
                Application.handleException(e);
                window.close();
            }
        }
        return anyAnimation;
    }

    private static void refresh() {
        boolean anyAnimation = iterateWindows();
        runVsyncTasks();

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

    private static void runVsyncTasks() {
        synchronized (vsyncRun) {
            vsyncRunTemp.addAll(vsyncRun);
            vsyncRun.clear();
        }
        for (var task : vsyncRunTemp) {
            try {
                task.run();
            } catch (Exception e) {
                Application.handleException(e);
            }
        }
        vsyncRunTemp.clear();
    }

    public static void setVsync(int vsync) {
        Application.vsync = vsync;
    }

    public static int getVsync() {
        return vsync;
    }

    public static void runVsync(Runnable task) {
        synchronized (vsyncRun) {
            vsyncRun.add(task);
        }
    }

    public static List<Window> getAssignedWindows() {
        return new ArrayList<>(windows);
    }

    public static void handleException(Exception e) {
        e.printStackTrace();
    }

}
