package flat.widget;

import flat.animations.ActivityTransition;
import flat.animations.Animation;
import flat.backend.*;
import flat.events.*;
import flat.graphics.context.Context;
import flat.graphics.SmartContext;
import flat.graphics.cursor.Cursor;
import flat.graphics.image.PixelMap;
import flat.resources.ResourcesManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public final class Application {

    static {
        System.loadLibrary("flat");
    }

    private static Thread thread;
    private static Context context;
    private static Activity activity;
    private static ActivityTransition transition;
    private static ArrayList<Activity> dialogs = new ArrayList<>();
    private static ArrayList<Runnable> activityChanges = new ArrayList<>();

    private static ArrayList<FutureTask<?>> runSync = new ArrayList<>();
    private static ArrayList<FutureTask<?>> runSyncCp = new ArrayList<>();

    private static ArrayList<Animation> anims = new ArrayList<>();
    private static ArrayList<Animation> animsCp = new ArrayList<>();

    private static ArrayList<EventData> events = new ArrayList<>();
    private static ArrayList<EventData> eventsCp = new ArrayList<>();
    private static Cursor cursor = Cursor.ARROW;
    private static Cursor currentCursor = null;

    private static PointerData mouse;
    private static float mx, my, outMouseX, outMouseY;
    private static ArrayList<PointerData> pointersData = new ArrayList<>();

    private static long loopTime;
    private static float dpi;
    private static int vsync;

    private Application() {
    }

    public static void init(Settings settings) {
        if (settings.width <= 0 || settings.height <= 0) {
            throw new RuntimeException("Invalide appliction settings (Negative screen size)");
        }
        if (settings.multsamples < 0) {
            throw new RuntimeException("Invalide appliction settings (Negative multsamples)");
        }
        if (settings.vsync < 0) {
            throw new RuntimeException("Invalide appliction settings (Negative vsync)");
        }
        ResourcesManager.setResources(settings.resources);

        long id = WL.Init(settings.width, settings.height, settings.multsamples, settings.transparent);
        if (id == 0) {
            throw new RuntimeException("Invalide context creation");
        }
        long svgId = SVG.Create();
        if (svgId == 0) {
            WL.Finish();
            throw new RuntimeException("Invalide context creation");
        }

        setVsync(settings.vsync);
        thread = Thread.currentThread();
        context = new Context(id, svgId);
        context.init();

        mx = (float) WL.GetCursorX();
        my = (float) WL.GetCursorY();
        dpi = (float) WL.GetDpi();

        WL.SetInputMode(WLEnuns.STICKY_KEYS, 1);
        WL.SetInputMode(WLEnuns.STICKY_MOUSE_BUTTONS, 1);
        WL.SetMouseButtonCallback((button, action, mods) -> events.add(MouseBtnData.get(button + 1, action, mods)));
        WL.SetCursorPosCallback((x, y) -> events.add(MouseMoveData.get(outMouseX = (float) x, outMouseY = (float) y)));
        WL.SetScrollCallback((x, y) -> events.add(MouseScrollData.get(x, y)));
        WL.SetDropCallback(names -> events.add(MouseDropData.get(names)));
        WL.SetKeyCallback((key, scancode, action, mods) -> events.add(KeyData.get(key, scancode, action, mods)));
        WL.SetCharModsCallback((codepoint, mods) -> events.add(CharModsData.get(codepoint, mods)));
        WL.SetWindowSizeCallback((width, height) -> {
            events.add(SizeData.get(width, height));
            doLoop();
        });
    }

    public static void launch(Activity startActivity) {
        dialogs.add(startActivity);
        activity = startActivity;

        try {
            show();

            loop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            long svgId = context.svgId;
            context.dispose();
            context = null;

            SVG.Destroy(svgId);

            // TODO - DELETE OPENGL LIVE OBJECTS {NOT FINALIZED}
            WL.Finish();
        }
    }

    public static Context getContext() {
        // assert - thread
        return context;
    }

    private static long t;

    static void loop() {
        t = System.currentTimeMillis();

        activity.onShow();
        activity.onStart();
        while (!WL.IsClosed()) {
            // Loop Wait
            if (!doLoop() || vsync == 0) {
                if (loopTime < 15) {
                    try {
                        Thread.sleep(15 - loopTime);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
    }

    static boolean doLoop() {
        // Timer
        long now = System.currentTimeMillis();
        loopTime = now - t;
        t = now;

        // Transitions
        while (transition == null && activityChanges.size() > 0) {
            activityChanges.remove(0).run();
        }

        SmartContext smartContext = context.getSmartContext();
        boolean draw = false;

        if (transition != null) {
            transition.handle(loopTime);

            draw = transition.draw(smartContext);

            if (!transition.isPlaying()) {
                Activity prev = transition.getPrev();
                if (prev != null) prev.onHide();
                Activity next = transition.getNext();
                if (next != null) next.onStart();

                transition = null;
            }
        }

        // Events
        WL.HandleEvents();

        if (transition == null && activity != null) {
            // Activity

            processEvents(activity);

            activity.animate(loopTime);

            activity.layout(getClientWidth(), getClientHeight(), getDpi());

            draw = activity.draw(smartContext);
        }

        // Syncronization
        processSyncCalls();

        // Cursor
        if (cursor != currentCursor) {
            currentCursor = cursor;
            WL.SetCursor(currentCursor.getInternalCursor());
        }

        // GL Draw
        if (draw) {
            smartContext.softFlush();
            WL.SwapBuffers();
            GL.Finish();
        }

        return draw;
    }

    static void processEvents(Activity activity) {
        ArrayList<EventData> swap = eventsCp;
        eventsCp = events;
        events = swap;

        // DPI - Listener
        if (dpi != (float) Math.ceil(WL.GetDpi())) {
            dpi = (float) Math.ceil(WL.GetDpi());
            eventsCp.add(SizeData.get(Application.getWidth(), Application.getHeight()));
        }

        // Mouse Listener
        if  (outMouseX != (float) WL.GetCursorX() || outMouseY != (float) WL.GetCursorY()) {
            eventsCp.add(MouseMoveData.get(outMouseX = (float) WL.GetCursorX(), outMouseY = (float) WL.GetCursorY()));
        }

        for (EventData eData : eventsCp) {
            // Mouse Button
            if (eData.type == 1) {
                MouseBtnData event = (MouseBtnData) eData;
                PointerData pt = getPointer(event.btn, -1, null);
                Widget widget = activity.findByPosition(mx, my, false);

                // Pressed
                if (event.action == WLEnuns.PRESS) {
                    if (mouse == null) {
                        mouse = pt;
                    }
                    pt.pressed = widget;
                    widget.firePointer(new PointerEvent(widget, PointerEvent.PRESSED, pt.btnId, mx, my));
                    widget.setPressed(true);
                    widget.fireRipple(mx, my);

                }
                // Released
                else if (event.action == WLEnuns.RELEASE) {
                    if (pt == mouse) {
                        if (pt.dragStarted) {
                            if (widget != pt.dragged) {
                                DragEvent dragEvent = new DragEvent(widget, DragEvent.DROPPED, pt.dragData, mx, my);
                                widget.fireDrag(dragEvent);
                                pt.dragData = dragEvent.getData();
                            }
                            pt.dragged.fireDrag(new DragEvent(pt.dragged, DragEvent.DONE, pt.dragData, mx, my));
                        }
                        pt.pressed.firePointer(new PointerEvent(pt.pressed, PointerEvent.RELEASED, pt.btnId, mx, my));
                        if (pt.dragged != null) {
                            pt.dragged.setDragged(false);
                        }
                        pt.pressed.setPressed(false);
                        pt.pressed.releaseRipple();
                        pt.reset();

                        mouse = null;
                        mouseMove(activity, mx, my);

                    } else if (pt.pressed != null) {
                        pt.pressed.firePointer(new PointerEvent(pt.pressed, PointerEvent.RELEASED, pt.btnId, mx, my));
                    }
                }

                MouseBtnData.release(event);
            }
            // Mouse Move
            else if (eData.type == 2) {
                MouseMoveData event = (MouseMoveData) eData;
                mouseMove(activity, event.x, event.y);

                MouseMoveData.release(event);
            }
            // Mouse Scroll
            else if (eData.type == 3) {
                MouseScrollData event = (MouseScrollData) eData;
                Widget widget = activity.findByPosition(mx, my, false);
                widget.fireScroll(new ScrollEvent(widget, ScrollEvent.SCROLL, event.x, event.y));

                MouseScrollData.release(event);
            }
            // Mouse Drop (system)
            else if (eData.type == 4) {
                MouseDropData event = (MouseDropData) eData;
                Widget widget = activity.findByPosition(mx, my, false);
                widget.fireDrag(new DragEvent(widget, DragEvent.DROPPED, event.paths, mx, my));

                MouseDropData.release(event);
            }
            // Key
            else if (eData.type == 5) {
                KeyData event = (KeyData) eData;
                Widget widget = activity.getFocus();
                if (widget != null) {
                    int eventType = (event.action == WLEnuns.PRESS) ? KeyEvent.PRESSED :
                            (event.action == WLEnuns.RELEASE) ? KeyEvent.RELEASED : KeyEvent.REPEATED;

                    boolean shift = (event.mods & (WLEnuns.MOD_SHIFT)) != 0;
                    boolean ctrl = (event.mods & (WLEnuns.MOD_CONTROL)) != 0;
                    boolean alt = (event.mods & (WLEnuns.MOD_ALT)) != 0;
                    boolean spr = (event.mods & (WLEnuns.MOD_SUPER)) != 0;

                    KeyEvent keyEvent = new KeyEvent(widget, eventType, shift, ctrl, alt, spr, "", event.key);
                    widget.fireKey(keyEvent);

                    if (!keyEvent.isConsumed()) {
                        activity.onKeyPress(keyEvent);
                    }
                }
                KeyData.release(event);
            }
            // Char Typed
            else if (eData.type == 7) {
                CharModsData event = (CharModsData) eData;
                Widget widget = activity.getFocus();
                if (widget != null) {

                    boolean shift = (event.mods & (WLEnuns.MOD_SHIFT)) != 0;
                    boolean ctrl = (event.mods & (WLEnuns.MOD_CONTROL)) != 0;
                    boolean alt = (event.mods & (WLEnuns.MOD_ALT)) != 0;
                    boolean spr = (event.mods & (WLEnuns.MOD_SUPER)) != 0;

                    String value = new String(Character.toChars(event.codepoint));
                    widget.fireKey(new KeyEvent(widget, KeyEvent.TYPED, shift, ctrl, alt, spr, value, -1));
                }

                CharModsData.release(event);
            }
            // Screen Size
            else if (eData.type == 8) {
                SizeData event = (SizeData) eData;

                activity.invalidate(true);

                SizeData.release(event);
            }
        }
        eventsCp.clear();
    }

    static void mouseMove(Activity activity, float x, float y) {
        Widget widget = activity.findByPosition(mx = x, my = y, false);

        // Move
        if (mouse == null) {
            PointerData pointer = getPointer(-1, -1, null);
            if (pointer.hover == null) pointer.hover = widget;
            if (pointer.hover != widget) {
                if (!widget.isChildOf(pointer.hover)) {
                    pointer.hover.fireHover(new HoverEvent(pointer.hover, HoverEvent.EXITED, widget, x, y));
                }
                if (!pointer.hover.isChildOf(widget)) {
                    widget.fireHover(new HoverEvent(widget, HoverEvent.ENTERED, pointer.hover, x, y));
                }
                Application.setCursor(widget.getShowCursor());
            }
            widget.fireHover(new HoverEvent(widget, HoverEvent.MOVED, widget, x, y));
            pointer.hover = widget;
        }
        // Drag
        else {
            if (mouse.dragged == null) {
                mouse.dragged = mouse.pressed;
                mouse.hover = widget;

                DragEvent event = new DragEvent(mouse.dragged, DragEvent.STARTED, mouse.dragData, x, y);
                mouse.dragged.fireDrag(event);
                mouse.dragged.setDragged(true);

                mouse.dragData = event.getData();
                mouse.dragStarted = event.isStarted();
            }
            if (mouse.dragStarted) {
                if (mouse.hover != widget) {
                    if (mouse.hover != mouse.dragged && !widget.isChildOf(mouse.hover)) {
                        DragEvent event = new DragEvent(mouse.hover, DragEvent.EXITED, widget, mouse.dragData, x, y);
                        mouse.hover.fireDrag(event);
                        mouse.dragData = event.getData();
                    }
                    if (widget != mouse.dragged && !mouse.hover.isChildOf(widget)) {
                        DragEvent event = new DragEvent(widget, DragEvent.ENTERED, mouse.hover, mouse.dragData, x, y);
                        widget.fireDrag(event);
                        mouse.dragData = event.getData();
                    }
                }

                if (widget != mouse.dragged) {
                    DragEvent event = new DragEvent(widget, DragEvent.OVER, mouse.dragData, x, y);
                    widget.fireDrag(event);
                    mouse.dragData = event.getData();
                }
            }

            mouse.hover = widget;
            mouse.dragged.firePointer(new PointerEvent(mouse.dragged, PointerEvent.DRAGGED, mouse.btnId, x, y));
        }
    }

    static void releaseEvents(Activity activity) {
        if (activity == null) {
            for (PointerData pt : pointersData) {
                pt.reset();
            }
            return;
        }

        for (PointerData pt : pointersData) {
            Widget widget = activity.findByPosition(mx, my, false);

            if (pt == mouse) {
                if (pt.dragStarted) {
                    if (widget != pt.dragged) {
                        DragEvent dragEvent = new DragEvent(widget, DragEvent.DROPPED, pt.dragData, mx, my);
                        widget.fireDrag(dragEvent);
                        pt.dragData = dragEvent.getData();
                    }
                    pt.dragged.fireDrag(new DragEvent(pt.dragged, DragEvent.DONE, pt.dragData, mx, my));
                }
                pt.pressed.firePointer(new PointerEvent(pt.pressed, PointerEvent.RELEASED, pt.btnId, mx, my));
                if (pt.dragged != null) {
                    pt.dragged.setDragged(false);
                }
                pt.pressed.setPressed(false);
                pt.pressed.releaseRipple();
                mouse = null;

            } else if (pt.pressed != null) {
                pt.pressed.firePointer(new PointerEvent(pt.pressed, PointerEvent.RELEASED, pt.btnId, mx, my));
            }
            pt.reset();
        }
    }

    static void processSyncCalls() {
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

    public static void runSync(FutureTask<?> task) {
        synchronized (Application.class) {
            runSync.add(task);
        }
    }

    public static <T> Future<T> runSync(Callable<T> task) {
        FutureTask<T> fTask = new FutureTask<>(task);
        runSync(fTask);
        return fTask;
    }

    public static <T> Future<T> runSync(Runnable task) {
        FutureTask<T> fTask = new FutureTask<>(task, null);
        runSync(fTask);
        return fTask;
    }

    public static Activity getActivity() {
        return activity;
    }

    public static void setActivity(final Activity next) {
        setActivity(next, new ActivityTransition());
    }

    public static void setActivity(final Activity next, final ActivityTransition activityTransition) {
        activityChanges.add(() -> {
            for (int i = dialogs.size() - 2; i >= 0; i--) {
                Activity act = dialogs.get(i);
                act.onHide();
            }
            dialogs.clear();
            dialogs.add(next);

            if (activity != null) activity.onPause();
            next.onShow();

            transition = activityTransition;
            transition.setActivities(activity, next);

            activity = next;
        });
    }

    public static void showDialog(final Activity next) {
        showDialog(next, new ActivityTransition());
    }

    public static void showDialog(final Activity next, final ActivityTransition activityTransition) {
        activityChanges.add(() -> {
            if (!dialogs.contains(next)) {
                dialogs.add(next);

                if (activity != null) {
                    releaseEvents(activity);
                    activity.onPause();
                }
                next.onShow();

                transition = activityTransition;
                transition.setActivities(activity, next);

                activity = next;
            }
        });
    }

    public static void hideDialog() {
        hideDialog(new ActivityTransition());
    }

    public static void hideDialog(final ActivityTransition activityTransition) {
        activityChanges.add(() -> {
            if (activity != null) {
                dialogs.remove(activity);
                final Activity next = dialogs.size() > 0 ? dialogs.get(dialogs.size() - 1) : null;

                releaseEvents(activity);
                activity.onPause();

                transition = activityTransition;
                transition.setActivities(activity, next);

                activity = next;
            }
        });
    }

    public static void setVsync(int vsync) {
        WL.SetVsync(Application.vsync = vsync);
    }

    public static int getVsync() {
        return Application.vsync;
    }

    public static boolean isTransparent() {
        return WL.IsTransparent();
    }

    public static void setFullscreen(boolean fullscreen) {
        WL.SetFullscreen(fullscreen);
    }

    public static boolean isFullscreen() {
        return WL.IsFullscreen();
    }

    public static void setResizable(boolean resizable) {
        WL.SetResizable(resizable);
    }

    public static boolean isResisable() {
        return WL.IsResizable();
    }

    public static void setDecorated(boolean decorated) {
        WL.SetDecorated(decorated);
    }

    public static boolean isDecorated() {
        return WL.IsDecorated();
    }

    public static String getTitle() {
        return WL.GetTitle();
    }

    public static void setTitle(String title) {
        WL.SetTitle(title);
    }

    public static void setIcon(PixelMap icons) {

    }

    public static void setCursor(Cursor cursor) {
        if (cursor == null) {
            cursor = Cursor.ARROW;
        }
        Application.cursor = cursor;
    }

    public static Cursor getCursor() {
        return Application.cursor;
    }

    public static int getX() {
        return WL.GetX();
    }

    public static void setX(int x) {
        WL.SetPosition(x, WL.GetY());
    }

    public static int getY() {
        return WL.GetY();
    }

    public static void setY(int y) {
        WL.SetPosition(WL.GetX(), y);
    }

    public static void setPosition(int x, int y) {
        WL.SetPosition(x, y);
    }

    public static int getClientWidth() {
        return WL.GetClientWidth();
    }

    public static int getClientHeight() {
        return WL.GetClientHeight();
    }

    public static float getPhysicalWidth() {
        return (float) WL.GetPhysicalWidth();
    }

    public static float getPhysicalHeight() {
        return (float) WL.GetPhysicalHeight();
    }

    public static float getDpi() {
        return dpi;
    }

    public static int getWidth() {
        return WL.GetWidth();
    }

    public static void setWidth(int width) {
        WL.SetSize(width, WL.GetHeight());
    }

    public static int getHeight() {
        return WL.GetHeight();
    }

    public static void setHeight(int height) {
        WL.SetSize(WL.GetWidth(), height);
    }

    public static void setSize(int width, int height) {
        WL.SetSize(width, height);
    }

    public static int getMinWidth() {
        return WL.GetMinWidth();
    }

    public static void setMinWidth(int minWidth) {
        WL.SetSizeLimits(minWidth, WL.GetMinHeight(), WL.GetMaxWidth(), WL.GetMaxHeight());
    }

    public static int getMinHeight() {
        return WL.GetMinHeight();
    }

    public static void setMinHeight(int minHeight) {
        WL.SetSizeLimits(WL.GetMinWidth(), minHeight, WL.GetMaxWidth(), WL.GetMaxHeight());
    }

    public static int getMaxWidth() {
        return WL.GetMaxWidth();
    }

    public static void setMaxWidth(int maxWidth) {
        WL.SetSizeLimits(WL.GetMinWidth(), WL.GetMinHeight(), maxWidth, WL.GetMaxHeight());
    }

    public static int getMaxHeight() {
        return WL.GetMaxHeight();
    }

    public static void setMaxHeight(int maxHeight) {
        WL.SetSizeLimits(WL.GetMinWidth(), WL.GetMinHeight(), WL.GetMaxWidth(), maxHeight);
    }

    public static void setSizeLimits(int minWidth, int minHeight, int maxWidth, int maxHeight) {
        WL.SetSizeLimits(minWidth, minHeight, maxWidth, maxHeight);
    }

    public static void show() {
        WL.Show();
    }

    public static void hide() {
        WL.Hide();
    }

    public static void maximize() {
        WL.Maximize();
    }

    public static void minimize() {
        WL.Minimize();
    }

    public static void restore() {
        WL.Restore();
    }

    public static void focus() {
        WL.Focus();
    }

    // Multouch

    static PointerData getPointer(int mb, int pid, List<PointerData> points) {
        int touchId = points != null ? points.get(pid).touchId : -1;
        for (PointerData entity : pointersData) {
            if (entity.touchId == touchId && entity.btnId == mb) {
                return entity;
            }
        }
        PointerData entity = new PointerData(mb, touchId);
        pointersData.add(entity);
        return entity;
    }

    static class EventData {
        int type;
    }

    static class MouseBtnData extends EventData {
        static ArrayList<MouseBtnData> list = new ArrayList<>();

        static MouseBtnData get(int btn, int action, int mods) {
            MouseBtnData data;
            if (list.size() > 0) data = list.remove(list.size() - 1);
            else data = new MouseBtnData();
            data.set(btn, action, mods);
            return data;
        }

        static void release(MouseBtnData data) {
            list.add(data);
        }

        int btn, action, mods;

        void set(int btn, int action, int mods) {
            this.type = 1;
            this.btn = btn;
            this.action = action;
            this.mods = mods;
        }
    }

    static class MouseMoveData extends EventData {
        static ArrayList<MouseMoveData> list = new ArrayList<>();

        static MouseMoveData get(double x, double y) {
            MouseMoveData data;
            if (list.size() > 0) data = list.remove(list.size() - 1);
            else data = new MouseMoveData();
            data.set((float) x, (float) y);
            return data;
        }

        static void release(MouseMoveData data) {
            list.add(data);
        }

        float x, y;

        void set(float x, float y) {
            this.type = 2;
            this.x = x;
            this.y = y;
        }
    }

    static class MouseScrollData extends EventData {
        static ArrayList<MouseScrollData> list = new ArrayList<>();

        static MouseScrollData get(double x, double y) {
            MouseScrollData data;
            if (list.size() > 0) data = list.remove(list.size() - 1);
            else data = new MouseScrollData();
            data.set((float) x, (float) y);
            return data;
        }

        static void release(MouseScrollData data) {
            list.add(data);
        }

        float x, y;

        void set(float x, float y) {
            this.type = 3;
            this.x = x;
            this.y = y;
        }
    }

    static class MouseDropData extends EventData {
        static ArrayList<MouseDropData> list = new ArrayList<>();

        static MouseDropData get(String[] paths) {
            MouseDropData data;
            if (list.size() > 0) data = list.remove(list.size() - 1);
            else data = new MouseDropData();
            data.set(paths);
            return data;
        }

        static void release(MouseDropData data) {
            list.add(data);
        }

        String[] paths;

        void set(String[] paths) {
            this.type = 4;
            this.paths = paths;
        }
    }

    static class KeyData extends EventData {
        static ArrayList<KeyData> list = new ArrayList<>();

        static KeyData get(int key, int scancode, int action, int mods) {
            KeyData data;
            if (list.size() > 0) data = list.remove(list.size() - 1);
            else data = new KeyData();
            data.set(key, scancode, action, mods);
            return data;
        }

        static void release(KeyData data) {
            list.add(data);
        }

        int key, scancode, action, mods;

        void set(int key, int scancode, int action, int mods) {
            this.type = 5;
            this.key = key;
            this.scancode = scancode;
            this.action = action;
            this.mods = mods;
        }
    }

    static class CharModsData extends EventData {
        static ArrayList<CharModsData> list = new ArrayList<>();

        static CharModsData get(int codepoint, int mods) {
            CharModsData data;
            if (list.size() > 0) data = list.remove(list.size() - 1);
            else data = new CharModsData();
            data.set(codepoint, mods);
            return data;
        }

        static void release(CharModsData data) {
            list.add(data);
        }

        int codepoint, mods;

        void set(int codepoint, int mods) {
            this.type = 7;
            this.codepoint = codepoint;
            this.mods = mods;
        }
    }

    static class SizeData extends EventData {
        static ArrayList<SizeData> list = new ArrayList<>();

        static SizeData get(int width, int height) {
            SizeData data;
            if (list.size() > 0) data = list.remove(list.size() - 1);
            else data = new SizeData();
            data.set(width, height);
            return data;
        }

        static void release(SizeData data) {
            list.add(data);
        }

        int width, height;

        void set(int width, int height) {
            this.type = 8;
            this.width = width;
            this.height = height;
        }
    }

    static class PointerData {
        final int btnId, touchId;

        Widget pressed, dragged, hover;

        boolean dragStarted;
        Object dragData;

        PointerData(int btnId, int touchId) {
            this.btnId = btnId;
            this.touchId = touchId;
        }

        void reset() {
            pressed = dragged = hover = null;
            dragStarted = false;
            dragData = null;
        }
    }

    public static class Settings {

        public final File resources;
        public Runnable start;
        public int multsamples;
        public int width;
        public int height;
        public boolean transparent;
        public int vsync;

        public <T extends Activity> Settings(File resources) {
            this(resources, null);
        }

        public <T extends Activity> Settings(File resources, Runnable start) {
            this(resources, start, 0);
        }

        public <T extends Activity> Settings(File resources, Runnable start, int multsamples) {
            this(resources, start, multsamples, 800, 600);
        }

        public <T extends Activity> Settings(File resources, Runnable start, int multsamples, int width, int height) {
            this(resources, start, multsamples, width, height, false);
        }

        public <T extends Activity> Settings(File resources, Runnable start, int multsamples, int width, int height, boolean transparent) {
            this.resources = resources;
            this.start = start;
            this.multsamples = multsamples;
            this.width = width;
            this.height = height;
            this.transparent = transparent;
        }
    }
}
