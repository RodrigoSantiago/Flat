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
import java.util.concurrent.atomic.AtomicBoolean;

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
    private static float loopTime;

    private static long autoFrameLimit = 0;
    private static final long autoFrameFPS = 1_000_000_000L / 120L;

    private static boolean finalized;
    private static SystemType systemType;

    public static void init() {
        init(new ResourcesManager());
    }

    public static void init(ResourcesManager resourcesManager) {
        resources = resourcesManager;
        finalized = false;

        try {
            FlatLibrary.load(resources.getFlatLibraryFile());
        } catch (Throwable e) {
            e.printStackTrace();
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
        if (!finalized) {
            finalized = true;
            WL.Finish();
        }
    }

    public static void launch(WindowSettings settings) {
        try {
            createWindow(settings);

            lastLoopTime = System.nanoTime();

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
        if (assignedWindow != null) {
            WL.WindowAssign(assignedWindow.getWindowId());
        } else {
            window.setAssigned(true);
            assignedWindow = window;
        }

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
            if (assignedWindow != null) {
                assignedWindow.setAssigned(false);
            }
            assignedWindow = window;
            assignedWindow.setAssigned(true);
            WL.WindowAssign(window.getWindowId());
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

            assignWindow(window);
            window.dispose();
            if (assignedWindow == window) {
                assignedWindow = null;
            }
        }
        windowsRemove.clear();
    }

    private static void iterateWindows() {
        updateWindowList();

        long now = System.nanoTime();
        loopTime = (now - lastLoopTime) / 1_000_000_000.0f;
        lastLoopTime = now;

        for (Window window : windows) {
            if (!window.isBlockedByModal()) {
                assignWindow(window);
                try {
                    window.loop(loopTime);
                } catch (Exception e) {
                    Application.handleException(e);
                    window.close();
                }
            }
        }
    }

    private static void refresh() {
        iterateWindows();

        if (assignedWindow != null) {
            boolean noSync = updateContext();
            runVsyncTasks(noSync);
        } else {
            loopActive = false;
        }
    }

    private static void refreshResize() {
        iterateWindows();

        if (assignedWindow != null) {
            updateContext();
        } else {
            loopActive = false;
        }
    }

    private static boolean updateContext() {
        int lastWindows = -1;
        for (int i = 0; i < windows.size(); i++) {
            Window window = windows.get(i);
            if (window.isBufferInvalided()) {
                lastWindows = i;
            }
        }

        for (int i = 0; i < windows.size(); i++) {
            Window window = windows.get(i);
            if (window.isBufferInvalided()) {
                assignWindow(window);
                if (i != lastWindows) {
                    if (window.getVsync() != 0) {
                        window.setVsync(0);
                        WL.SetVsync(0);
                    }
                } else {
                    if (window.getVsync() != getVsync()) {
                        window.setVsync(getVsync());
                        WL.SetVsync(getVsync());
                    }
                }
                WL.SwapBuffers(window.getWindowId());
            }
        }
        for (int i = 0; i < windows.size(); i++) {
            Window window = windows.get(i);
            window.unsetBufferInvalided();
        }
        return lastWindows == -1;
    }

    private static void runVsyncTasks(boolean noSync) {
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

        if (getVsync() == 0 || noSync) {
            long now = System.nanoTime();
            if (autoFrameLimit == 0) {
                autoFrameLimit = now;
            }
            long off = now - autoFrameLimit;

            long sleepTime = autoFrameFPS - off;
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime / 1_000_000);
                } catch (InterruptedException ignored) {
                }
            }
        }
        autoFrameLimit = System.nanoTime();

        WL.HandleEvents(0);
    }

    public static Context getCurrentContext() {
        if (assignedWindow != null) {
            return assignedWindow.getContext();
        }
        return null;
    }

    public static void setVsync(int vsync) {
        Application.vsync = vsync;
    }

    public static int getVsync() {
        return vsync;
    }

    public static void runOnContextSync(Runnable task) {
        synchronized (vsyncRun) {
            vsyncRun.add(task);
        }
    }

    public static float getLoopTime() {
        return loopTime;
    }

    public static List<Window> getAssignedWindows() {
        return new ArrayList<>(windows);
    }

    public static void handleException(Exception e) {
        e.printStackTrace();
    }

    public static SystemType getSystemType() {
        if (systemType == null) {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                systemType = SystemType.WINDOWS;
            } else if (os.contains("nix") || os.contains("nux")) {
                systemType = SystemType.UNIX;
            } else if (os.contains("mac")) {
                systemType = SystemType.MAC;
            } else {
                systemType = SystemType.OTHER;
            }
        }
        return systemType;
    }
}
