package flat.widget;

import flat.animations.ActivityTransition;
import flat.backend.SVG;
import flat.backend.WL;
import flat.backend.WLEnums;
import flat.graphics.SmartContext;
import flat.graphics.context.Context;
import flat.graphics.cursor.Cursor;
import flat.graphics.image.PixelMap;
import flat.widget.window.*;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class Window {

    private Context context;
    private boolean disposed;
    long windowId;
    long svgId;

    // Application
    private ActivityFactory factory;
    private Activity activity;
    private ActivityTransition transition;
    private final ArrayList<Activity> transitionsTarget = new ArrayList<>();
    private final ArrayList<ActivityTransition> transitions = new ArrayList<>();

    // Events
    private ArrayList<EventData> events = new ArrayList<>();
    private ArrayList<EventData> eventsCp = new ArrayList<>();

    private PointerData mouse;
    private float outMouseX, outMouseY;
    private ArrayList<PointerData> pointersData;

    private ArrayList<FutureTask<?>> runSync = new ArrayList<>();
    private ArrayList<FutureTask<?>> runSyncCp = new ArrayList<>();

    // Cache Values
    private String title;
    private float dpi;
    private Cursor cursor;
    private Cursor currentCursor;
    private int minWidth, minHeight, maxWidth, maxHeight;

    Window(ActivityFactory factory, int width, int height, int multiSamples, boolean transparent) {
        windowId = WL.WindowCreate(width, height, multiSamples, transparent);
        if (windowId == 0) {
            throw new RuntimeException("Invalid context creation");
        }
        svgId = SVG.Create();
        if (svgId == 0) {
            WL.WindowDestroy(windowId);
            throw new RuntimeException("Invalid context creation");
        }

        context = new Context(this, windowId, svgId);

        outMouseX = (float) WL.GetCursorX(windowId);
        outMouseY = (float) WL.GetCursorY(windowId);

        WL.SetInputMode(windowId, WLEnums.STICKY_KEYS, 1);
        WL.SetInputMode(windowId, WLEnums.STICKY_MOUSE_BUTTONS, 1);

        this.factory = factory;
    }

    void checkDisposed() {
        if (disposed) {
            throw new RuntimeException("Window is disposed.");
        }
    }

    void dispose() {
        if (!disposed) {
            context.dispose();

            SVG.Destroy(svgId);
            WL.WindowDestroy(windowId);

            disposed = true;
        }
    }

    boolean loop(long loopTime) {
        if (activity == null) {
            activity = factory.build(context);
            show();
        }

        Application.assignWindow(this);
        Application.assignVsync();

        SmartContext smartContext = context.getSmartContext();
        boolean draw = false;

        // Synchronization
        processSyncCalls();

        // Transitions
        if (transition == null && transitions.size() > 0) {
            transition = transitions.remove(0);
            transition.start(activity, transitionsTarget.remove(0));
        }

        if (transition != null) {
            transition.handle(loopTime);

            if (transition.isPlaying()) {
                draw = transition.draw(smartContext);

            } else {
                transition.end();
                transition = null;
            }
        }

        if (transition == null && activity != null) {
            // Activity

            processEvents(activity);

            activity.animate(loopTime);

            activity.layout(getClientWidth(), getClientHeight(), getDpi());

            draw = activity.draw(smartContext);
        }

        // Cursor
        if (cursor != currentCursor) {
            currentCursor = cursor;
            WL.SetCursor(windowId, currentCursor.getInternalCursor());
        }

        // GL Draw
        if (draw) {
            smartContext.softFlush();
            WL.SwapBuffers(windowId);
            return true;
        }
        return false;
    }

    void addEvent(EventData eventData) {
        events.add(eventData);
        // TODO - LOOP ON SIZE EVENTS
    }

    void addEvent(EventData eventData, float mouseX, float mouseY) {
        outMouseX = mouseX;
        outMouseY = mouseY;
        events.add(eventData);
    }

    void processEvents(Activity activity) {
        ArrayList<EventData> swap = eventsCp;
        eventsCp = events;
        events = swap;

        // DPI Change Listener
        if (dpi != (float) Math.ceil(WL.GetDpi(windowId))) {
            dpi = (float) Math.ceil(WL.GetDpi(windowId));
            eventsCp.add(EventData.getSize(getWidth(), getHeight()));
        }

        // Mouse Outside Move Listener
        float currentX = (float) WL.GetCursorX(windowId);
        float currentY = (float) WL.GetCursorY(windowId);
        if (outMouseX != currentX || outMouseY != currentY) {
            outMouseX = currentX;
            outMouseY = currentY;
            eventsCp.add(EventData.getMouseMove(outMouseX, outMouseY));
        }

        for (EventData eData : eventsCp) {
            EventData.consume(this, eData);
        }
        eventsCp.clear();
    }

    void releaseEvents(Activity activity) {
        // TODO - FORCE MOUSE RELEASING to remove widget pointers memory leak!
    }

    void processSyncCalls() {
        synchronized (Application.class) {
            ArrayList<FutureTask<?>> swap = runSyncCp;
            runSyncCp = runSync;
            runSync = swap;
        }
        for (FutureTask<?> run : runSyncCp) {
            run.run();
        }
        runSyncCp.clear();
    }

    public void runSync(FutureTask<?> task) {
        synchronized (Application.class) {
            runSync.add(task);
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

    public void setActivity(Activity next) {
        setActivity(next, new ActivityTransition());
    }

    public void setActivity(Activity next, ActivityTransition activityTransition) {
        transitionsTarget.add(next);
        transitions.add(activityTransition);
    }

    public boolean isClosed() {
        return disposed || WL.IsClosed(windowId);
    }

    public boolean isTransparent() {
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

        WL.SetResizable(resizable);
    }

    public boolean isResizable() {
        return WL.IsResizable(windowId);
    }

    public void setDecorated(boolean decorated) {
        checkDisposed();

        WL.SetDecorated(decorated);
    }

    public boolean isDecorated() {
        return WL.IsDecorated(windowId);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        checkDisposed();

        WL.SetTitle(this.title = title);
    }

    public void setIcon(PixelMap icons) {
        checkDisposed();


    }

    public void setCursor(Cursor cursor) {
        checkDisposed();

        if (cursor == null) {
            cursor = Cursor.ARROW;
        }
        this.cursor = cursor;
    }

    public Cursor getCursor() {
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

    public PointerData getPointer() {
        if (pointersData == null) {
            pointersData = new ArrayList<>();
            pointersData.add(new PointerData(-1));
        }
        return pointersData.get(0);
    }

    public PointerData getPointer(int pointerId) {
        return getPointer();
    }

    public int getX() {
        checkDisposed();

        return WL.GetX(windowId);
    }

    public void setX(int x) {
        checkDisposed();

        WL.SetPosition(windowId, x, WL.GetY(windowId));
    }

    public int getY() {
        checkDisposed();

        return WL.GetY(windowId);
    }

    public void setY(int y) {
        checkDisposed();

        WL.SetPosition(windowId, WL.GetX(windowId), y);
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
        return WL.GetClientHeight(windowId);
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

        return dpi;
    }

    public int getWidth() {
        checkDisposed();

        return WL.GetWidth(windowId);
    }

    public void setWidth(int width) {
        checkDisposed();

        WL.SetSize(windowId, width, WL.GetHeight(windowId));
    }

    public int getHeight() {
        checkDisposed();

        return WL.GetHeight(windowId);
    }

    public void setHeight(int height) {
        checkDisposed();

        WL.SetSize(windowId, WL.GetWidth(windowId), height);
    }

    public void setSize(int width, int height) {
        checkDisposed();

        WL.SetSize(windowId, width, height);
    }

    public int getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(int minWidth) {
        checkDisposed();

        WL.SetSizeLimits(windowId, this.minWidth = minWidth, minHeight, maxWidth, maxHeight);
    }

    public int getMinHeight() {
        return minHeight;
    }

    public void setMinHeight(int minHeight) {
        checkDisposed();

        WL.SetSizeLimits(windowId, minWidth, this.minHeight = minHeight, maxWidth, maxHeight);
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        checkDisposed();

        WL.SetSizeLimits(windowId, minWidth, minHeight, this.maxWidth = maxWidth, maxHeight);
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        checkDisposed();

        WL.SetSizeLimits(windowId, minWidth, minHeight, maxWidth, this.maxHeight = maxHeight);
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

    boolean requestClose() {
        System.out.println("CLOSED");
        return true;
    }
}
