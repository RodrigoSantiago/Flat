package flat.window;

import flat.backend.SVG;
import flat.backend.WL;
import flat.backend.WLEnums;
import flat.exception.FlatException;
import flat.graphics.context.Context;
import flat.graphics.cursor.Cursor;
import flat.graphics.image.ImageData;
import flat.graphics.image.ImageTexture;
import flat.uxml.UXListener;
import flat.window.event.EventData;
import flat.window.event.EventDataPointer;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class Window {

    private final Context context;
    private final long windowId;
    private final long svgId;
    private boolean disposed;
    private boolean closed;

    private boolean shift;
    private boolean ctrl;
    private boolean alt;
    private boolean spr;

    // Application
    private Activity activity;

    // Events
    private ArrayList<EventData> events = new ArrayList<>();
    private ArrayList<EventData> eventsCp = new ArrayList<>();

    private EventDataPointer mouse;
    private float outMouseX, outMouseY;
    private ArrayList<EventDataPointer> pointersData;

    private ArrayList<FutureTask<?>> runSync = new ArrayList<>();
    private ArrayList<FutureTask<?>> runSyncCp = new ArrayList<>();

    // Cache Values
    private String title;
    private float dpi;
    private Cursor cursor;
    private Cursor currentCursor;
    private int multiSample, minWidth, minHeight, maxWidth, maxHeight;

    private float loopTime;
    private boolean assigned;
    private boolean started;
    private boolean releaseEventDelayed, eventConsume;
    private boolean bufferInvalid;

    static Window create(WindowSettings settings) {
        if (settings.getWidth() <= 0 || settings.getHeight() <= 0) {
            throw new FlatException("Invalid application settings (Negative Screen Size)");
        }
        if (settings.getMultiSamples() < 0) {
            throw new FlatException("Invalid application settings (Negative Multi Samples)");
        }
        return new Window(settings);
    }

    Window(WindowSettings settings) {
        windowId = WL.WindowCreate(settings.getWidth(), settings.getHeight(), settings.getMultiSamples(), settings.isTransparent());
        if (windowId == 0) {
            throw new FlatException("Invalid context creation");
        }
        svgId = SVG.Create();
        if (svgId == 0) {
            WL.WindowDestroy(windowId);
            throw new FlatException("Invalid context creation");
        }

        outMouseX = (float) WL.GetCursorX(windowId);
        outMouseY = (float) WL.GetCursorY(windowId);

        this.multiSample = settings.getMultiSamples();
        this.context = Context.create(this, svgId);
        this.activity = Activity.create(this, settings);
    }

    private void checkDisposed() {
        if (disposed) {
            throw new FlatException("The Window is disposed");
        }
    }

    void dispose() {
        if (!disposed) {
            context.dispose();
            SVG.Destroy(svgId);
            WL.Close(windowId);
            WL.WindowDestroy(windowId);

            disposed = true;
        }
    }

    void loop(float loopTime) {
        this.loopTime = loopTime;

        context.getGraphics().resetState();

        processStartup();

        activity.refreshScene();

        processSyncCalls();

        processEvents();

        activity.animate(loopTime);

        activity.layout(getClientWidth(), getClientHeight());

        if (activity.draw(context.getGraphics())) {
            bufferInvalid = true;
            context.endFrame();
        }

        // Cursor
        var pointer = getPointer();
        if (pointer.getPressed() != null) {
            setCursor(pointer.getPressed().getCurrentCursor());
        } else if (pointer.getHover() != null) {
            setCursor(pointer.getHover().getCurrentCursor());
        } else {
            setCursor(Cursor.UNSET);
        }
        if (cursor != currentCursor) {
            if (currentCursor == Cursor.NONE) {
                WL.SetInputMode(windowId, WLEnums.CURSOR, WLEnums.CURSOR_NORMAL);
            }
            currentCursor = cursor;
            if (currentCursor == Cursor.NONE) {
                WL.SetInputMode(windowId, WLEnums.CURSOR, WLEnums.CURSOR_HIDDEN);

            } else if (currentCursor == Cursor.UNSET) {
                WL.SetCursor(windowId, Cursor.ARROW.getInternalCursor());

            } else {
                WL.SetCursor(windowId, currentCursor.getInternalCursor());
            }
        }
    }

    boolean isBufferInvalided() {
        return bufferInvalid;
    }

    void unsetBufferInvalided() {
        bufferInvalid = false;
    }

    void addEvent(EventData eventData) {
        if (!closed && !modal) {
            events.add(eventData);
        }
    }

    void addEvent(EventData eventData, float mouseX, float mouseY) {
        if (!closed) {
            outMouseX = mouseX;
            outMouseY = mouseY;
            events.add(eventData);
        }
    }

    public void releaseEvents() {
        if (eventConsume) {
            releaseEventDelayed = true;
        } else {
            getPointer().reset(outMouseX, outMouseY);
            events.clear();
            eventsCp.clear();
        }
    }

    void processStartup() {
        if (!started) {
            setPosition((getMonitorWidth() - getWidth()) / 2, (getMonitorHeight() - getHeight()) / 2);
            show();
            started = true;
            activity.initialize();
            activity.show();
        }
    }

    void processEvents() {

        // DPI Change Listener
        if (dpi != getDpi()) {
            dpi = getDpi();
            addEvent(EventData.getSize(getWidth(), getHeight()));
        }

        // Mouse Outside Move Listener
        float currentX = (float) WL.GetCursorX(windowId);
        float currentY = (float) WL.GetCursorY(windowId);
        if (outMouseX != currentX || outMouseY != currentY) {
            outMouseX = currentX;
            outMouseY = currentY;
            addEvent(EventData.getMouseMove(outMouseX, outMouseY), currentX, currentY);
        }

        ArrayList<EventData> swap = eventsCp;
        eventsCp = events;
        events = swap;

        eventConsume = true;
        for (EventData eData : eventsCp) {
            try {
                eData.handle(this);
            } catch (Exception e) {
                Application.handleException(e);
            }

            if (releaseEventDelayed) {
                break;
            }
        }
        eventConsume = false;
        eventsCp.clear();

        if (releaseEventDelayed) {
            releaseEvents();
            releaseEventDelayed = false;
        }
    }

    void processSyncCalls() {
        synchronized (Application.class) {
            ArrayList<FutureTask<?>> swap = runSyncCp;
            runSyncCp = runSync;
            runSync = swap;
        }
        for (FutureTask<?> run : runSyncCp) {
            try {
                run.run();
                run.get();
            } catch (Exception e) {
                Application.handleException(e);
            }
        }
        runSyncCp.clear();
    }

    long getWindowId() {
        return windowId;
    }

    long getSvgId() {
        return svgId;
    }

    public boolean isStarted() {
        return started;
    }

    public void runSync(FutureTask<?> task) {
        checkDisposed();

        synchronized (Application.class) {
            runSync.add(task);
            WL.PostEmptyEvent();
        }
    }

    public <T> Future<T> runSync(Callable<T> task) {
        FutureTask<T> fTask = new FutureTask<>(task);
        runSync(fTask);
        return fTask;
    }

    public <T> Future<T> runSync(Runnable task) {
        FutureTask<T> fTask = new FutureTask<>(task, null);
        runSync(fTask);
        return fTask;
    }

    public Context getContext() {
        return context;
    }

    public Activity getActivity() {
        return activity;
    }

    public boolean isClosed() {
        return disposed || closed || WL.IsClosed(windowId);
    }

    void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }

    public boolean isAssigned() {
        return assigned;
    }

    public boolean isTransparent() {
        checkDisposed();

        return WL.IsTransparent(windowId);
    }

    public void setFullscreen(boolean fullscreen) {
        checkDisposed();

        WL.SetFullscreen(windowId, fullscreen);
    }

    public boolean isFullscreen() {
        checkDisposed();

        return WL.IsFullscreen(windowId);
    }

    public void setResizable(boolean resizable) {
        checkDisposed();

        WL.SetResizable(windowId, resizable);
    }

    public boolean isResizable() {
        checkDisposed();

        return WL.IsResizable(windowId);
    }

    public void setDecorated(boolean decorated) {
        checkDisposed();

        WL.SetDecorated(windowId, decorated);
    }

    public boolean isDecorated() {
        checkDisposed();

        return WL.IsDecorated(windowId);
    }

    public String getTitle() {
        checkDisposed();

        return title;
    }

    public void setTitle(String title) {
        checkDisposed();

        WL.SetTitle(windowId, this.title = title);
    }
    
    public void setIcon(ImageTexture icon) {
        setIcon(icon.readImageData());
    }
    
    public void setIcon(ImageData icon) {
        checkDisposed();
        
        WL.SetIcon(windowId, icon.getData(), icon.getWidth(), icon.getHeight());
    }

    public void setCursor(Cursor cursor) {
        checkDisposed();

        if (cursor == null) {
            cursor = Cursor.ARROW;
        }
        this.cursor = cursor;
    }

    public Cursor getCursor() {
        checkDisposed();

        return cursor;
    }

    public float getPointerX() {
        return getPointer().getX();
    }

    public float getPointerY() {
        return getPointer().getY();
    }

    public float getPointerX(int pointerId) {
        return 0;
    }

    public float getPointerY(int pointerId) {
        return 0;
    }

    public EventDataPointer getPointer() {
        checkDisposed();

        if (pointersData == null) {
            pointersData = new ArrayList<>();
            pointersData.add(new EventDataPointer(this, -1));
        }
        return pointersData.get(0);
    }

    public int getPositionX() {
        checkDisposed();

        return WL.GetX(windowId);
    }

    public int getPositionY() {
        checkDisposed();

        return WL.GetY(windowId);
    }

    public void setPosition(int x, int y) {
        checkDisposed();

        WL.SetPosition(windowId, x, y);
    }

    public int getClientWidth() {
        checkDisposed();

        return WL.GetClientWidth(windowId);
    }

    public int getClientHeight() {
        checkDisposed();

        return WL.GetClientHeight(windowId);
    }

    public int getMonitorWidth() {
        checkDisposed();

        return WL.GetMonitorWidth(windowId);
    }

    public int getMonitorHeight() {
        checkDisposed();

        return WL.GetMonitorHeight(windowId);
    }

    public float getPhysicalWidth() {
        checkDisposed();

        return (float) WL.GetPhysicalWidth(windowId);
    }

    public float getPhysicalHeight() {
        checkDisposed();

        return (float) WL.GetPhysicalHeight(windowId);
    }

    public float getDpi() {
        checkDisposed();

        return (float) WL.GetDpi(windowId);
    }

    public int getWidth() {
        checkDisposed();

        return WL.GetWidth(windowId);
    }

    public int getHeight() {
        checkDisposed();

        return WL.GetHeight(windowId);
    }

    public int getMultiSample() {
        return multiSample;
    }

    public void setSize(int width, int height) {
        checkDisposed();

        WL.SetSize(windowId, width, height);
    }

    public int getMinWidth() {
        checkDisposed();

        return minWidth;
    }

    public int getMinHeight() {
        checkDisposed();

        return minHeight;
    }

    public int getMaxWidth() {
        checkDisposed();

        return maxWidth;
    }

    public int getMaxHeight() {
        checkDisposed();

        return maxHeight;
    }

    public void setSizeLimits(int minWidth, int minHeight, int maxWidth, int maxHeight) {
        checkDisposed();

        this.minWidth = minWidth;
        this.minHeight = minHeight;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        WL.SetSizeLimits(windowId, minWidth, minHeight, maxWidth, maxHeight);
    }

    public void show() {
        checkDisposed();

        WL.Show(windowId);
    }

    public void hide() {
        checkDisposed();

        WL.Hide(windowId);
    }

    public void maximize() {
        checkDisposed();

        WL.Maximize(windowId);
    }

    public void minimize() {
        checkDisposed();

        WL.Minimize(windowId);
    }

    public void restore() {
        checkDisposed();

        WL.Restore(windowId);
    }

    public void focus() {
        checkDisposed();

        WL.Focus(windowId);
    }
    
    public boolean requestClose() {
        return onRequestClose(false);
    }

    boolean onRequestClose(boolean system) {
        if (activity == null || closed) {
            return true;

        } else if (activity.closeRequest(system)) {
            close();
            return true;

        }
        return false;
    }

    void close() {
        releaseEvents();
        activity.close();
        closed = true;
    }

    public void setMods(boolean shift, boolean ctrl, boolean alt, boolean spr) {
        this.shift = shift;
        this.ctrl = ctrl;
        this.alt = alt;
        this.spr = spr;
    }

    public boolean isShiftDown() {
        return shift;
    }

    public boolean isCtrlDown() {
        return ctrl;
    }

    public boolean isAltDown() {
        return alt;
    }

    public boolean isSprDown() {
        return spr;
    }

    private boolean modal;

    private static String createFilter(String[] filters) {
        StringBuilder filter = null;
        for (var str : filters) {
            if (str.matches("[a-zA-Z0-9_ \\-]+(,[a-zA-Z0-9_ \\-]+)*")) {
                if (filter == null) {
                    filter = new StringBuilder(str);
                } else {
                    filter.append(";").append(str);
                }
            }
        }
        return filter == null ? "" : filter.toString();
    }

    private static String createFolder(File file) {
        String folder;
        if (file == null || !file.exists() || !file.isDirectory()) {
            return "";
        } else {
            return file.getAbsolutePath();
        }
    }

    public boolean isBlockedByModal() {
        return modal;
    }

    public void showOpenFileDialog(UXListener<String> listener, File initialFolder, String... filters) {
        if (modal) return;
        modal = true;

        String filter = createFilter(filters);
        String folder = createFolder(initialFolder);
        WL.ShowOpenFile(windowId, filter, folder, (window, path) -> {
            modal = false;
            if (!disposed) {
                runSync(() -> UXListener.safeHandle(listener, path));
            }
        });
    }

    public void showOpenMultipleFilesDialog(UXListener<String[]> listener, File initialFolder, String... filters) {
        if (modal) return;
        modal = true;

        String filter = createFilter(filters);
        String folder = createFolder(initialFolder);
        WL.ShowOpenMultipleFiles(windowId, filter, folder, (window, path) -> {
            modal = false;
            if (!disposed) {
                String[] split = path == null ? null : path.split(",");
                runSync(() -> UXListener.safeHandle(listener, split));
            }
        });
    }

    public void showSaveFileDialog(UXListener<String> listener, File initialFolder, String... filters) {
        if (modal) return;
        modal = true;

        String filter = createFilter(filters);
        String folder = createFolder(initialFolder);
        WL.ShowSaveFile(windowId, filter, folder, (window, path) -> {
            modal = false;
            if (!disposed) {
                runSync(() -> UXListener.safeHandle(listener, path));
            }
        });
    }

    public void showOpenFolderDialog(UXListener<String> listener, File initialFolder) {
        if (modal) return;
        modal = true;

        String folder = createFolder(initialFolder);
        WL.ShowOpenFolder(windowId, folder, (window, path) -> {
            modal = false;
            if (!disposed) {
                runSync(() -> UXListener.safeHandle(listener, path));
            }
        });
    }

}
