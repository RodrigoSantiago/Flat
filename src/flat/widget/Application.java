package flat.widget;

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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

import static flat.backend.SVGEnuns.*;

public final class Application {

    static {
        System.loadLibrary("flat");
    }

    private static Thread thread;
    private static Context context;
    private static Activity activity;

    private static HashMap<Thread, Context> contexts = new HashMap<>();

    private static int gThreadCount;
    private static final Object key = new Object();

    private static ArrayList<FutureTask<?>> runSync = new ArrayList<>();
    private static ArrayList<FutureTask<?>> runSyncCp = new ArrayList<>();

    private static ArrayList<Animation> anims = new ArrayList<>();

    private static ArrayList<EventData> events = new ArrayList<>();
    private static ArrayList<EventData> eventsCp = new ArrayList<>();
    private static Cursor cursor = Cursor.ARROW;
    private static Cursor currentCursor = null;

    private static PointerData mouse;
    private static float mouseX, mouseY, outMouseX, outMouseY;
    private static ArrayList<PointerData> pointersData = new ArrayList<>();

    private static long loopTime;
    private static float dpi;
    private static int vsync;

    private Application() {
    }

    public static void init(Settings settings) {
        if (settings.activityClass == null) {
            throw new RuntimeException("Invalide appliction settings (Null start activity class)");
        }
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

        synchronized (Application.class) {
            contexts.put(thread, context);
        }

        try {
            mouseX = (float) WL.GetCursorX();
            mouseY = (float) WL.GetCursorY();
            dpi = (float) WL.GetDpi();
            WL.SetInputMode(WLEnuns.STICKY_KEYS, 1);
            WL.SetInputMode(WLEnuns.STICKY_MOUSE_BUTTONS, 1); // --- Ocorreu Antes dessa linha {porem em thread diferente}
            WL.SetMouseButtonCallback((button, action, mods) -> events.add(MouseBtnData.get(button + 1, action, mods)));
            WL.SetCursorPosCallback((x, y) -> events.add(MouseMoveData.get(outMouseX = (float) x, outMouseY = (float) y)));
            WL.SetScrollCallback((x, y) -> events.add(MouseScrollData.get(x, y)));
            WL.SetDropCallback(names -> events.add(MouseDropData.get(names)));
            WL.SetKeyCallback((key, scancode, action, mods) -> events.add(KeyData.get(key, scancode, action, mods)));
            WL.SetCharModsCallback((codepoint, mods) -> events.add(CharModsData.get(codepoint, mods)));
            WL.SetWindowSizeCallback((width, height) -> events.add(SizeData.get(width, height)));

            activity = (Activity) settings.activityClass.getConstructor().newInstance();
            activity.invalidate(true);

            if (settings.start != null) {
                settings.start.run();
            } else {
                show();
            }

            launch();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            synchronized (Application.class) {
                contexts.remove(thread);
            }
            context.dispose();
            context = null;
            SVG.Destroy(svgId);
            WL.ContextAssign(0);

            synchronized (key) {
                while (gThreadCount > 0) {
                    try {
                        processSyncCalls();
                        key.wait();
                    } catch (Exception e) {
                        break;
                    }
                }
            }

            WL.Finish();
        }

    }

    public static Context getContext() {
        if (Thread.currentThread() != thread) {
            throw new RuntimeException("The context is not current");
        }
        return context;
    }

    public static Context getCurrentContext() {
        if  (Thread.currentThread() == thread) {
            return context;
        }
        synchronized (Application.class) {
            return contexts.get(Thread.currentThread());
        }
    }

    public static Thread createGraphicalThread(GraphicTask task) {
        long id = WL.ContextCreate(0);
        if (id == 0) {
            throw new RuntimeException("Invalide context creation");
        }
        return new Thread(() -> {
            synchronized (key) {
                gThreadCount++;
            }

            WL.ContextAssign(id);

            long svgId = SVG.Create();
            if (svgId == 0) {
                runSync(() -> WL.ContextDestroy(id));
                throw new RuntimeException("Invalide context creation");
            }

            Thread thread = Thread.currentThread();
            Context context = new Context(id, svgId);
            context.init();

            synchronized (Application.class) {
                contexts.put(thread, context);
            }

            try {
                task.run(context);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                synchronized (Application.class) {
                    contexts.remove(thread);
                }
                context.dispose();
                SVG.Destroy(svgId);

                synchronized (key) {
                    gThreadCount--;
                    runSync(() -> WL.ContextDestroy(id));
                }
            }
        });
    }

    static void launch() {
        while (!WL.IsClosed()) {
            loopTime = System.currentTimeMillis();

            processEvents();

            processAnimations();

            processLayout();

            processDraws();

            processSyncCalls();

            if (cursor != currentCursor) {
                currentCursor = cursor;
                WL.SetCursor(currentCursor.getInternalCursor());
            }
        }
    }

    static void processEvents() {
        WL.HandleEvents();
        ArrayList<EventData> swap = eventsCp;
        eventsCp = events;
        events = swap;

        if (dpi != (float) Math.ceil(WL.GetDpi())) {
            dpi = (float) Math.ceil(WL.GetDpi());
            eventsCp.add(SizeData.get(Application.getWidth(), Application.getHeight()));
        }

        if  (outMouseX != WL.GetCursorX() || outMouseY != WL.GetCursorY()) {
            eventsCp.add(MouseMoveData.get(outMouseX = (float) WL.GetCursorX(), outMouseY = (float) WL.GetCursorY()));
        }

        for (EventData eData : eventsCp) {
            // Mouse Button
            if (eData.type == 1) {
                MouseBtnData event = (MouseBtnData) eData;
                PointerData pointer = getPointer(event.btn, -1, null);
                Widget widget = activity.findByPosition(mouseX, mouseY, false);

                // Pressed
                if (event.action == WLEnuns.PRESS) {
                    if (mouse == null) {
                        mouse = pointer;
                        mouse.pressed = widget;
                        mouse.pressed.firePointer(new PointerEvent(mouse.pressed, PointerEvent.PRESSED, event.btn, mouseX, mouseY));
                        mouse.pressed.setPressed(true);
                        mouse.pressed.fireRipple(mouseX, mouseY);
                    } else {
                        mouse.pressed.firePointer(new PointerEvent(mouse.pressed, PointerEvent.PRESSED, event.btn, mouseX, mouseY));
                    }
                }
                // Released
                else if (event.action == WLEnuns.RELEASE) {
                    if (mouse == pointer) {
                        if (mouse.dragStarted) {
                            DragEvent dragEvent = new DragEvent(widget, DragEvent.DROPPED, mouse.dragData, mouseX, mouseY);
                            widget.fireDrag(dragEvent);
                            mouse.dragData = dragEvent.getData();
                            if (dragEvent.isDragCompleted()) {
                                mouse.dragged.fireDrag(new DragEvent(mouse.dragged, DragEvent.DONE, mouse.dragData, mouseX, mouseY));
                            }
                        }
                        mouse.pressed.firePointer(new PointerEvent(mouse.pressed, PointerEvent.RELEASED, event.btn, mouseX, mouseY));
                        if (mouse.dragged != null) {
                            mouse.dragged.setDragged(false);
                        }
                        mouse.pressed.setPressed(false);
                        mouse.pressed.releaseRipple();
                        mouse.reset();
                        mouse = null;

                        mouseMove(mouseX, mouseY);

                    } else if (mouse != null) {
                        mouse.pressed.firePointer(new PointerEvent(mouse.pressed, PointerEvent.RELEASED, event.btn, mouseX, mouseY));
                    }
                }
                MouseBtnData.release(event);
            }
            // Mouse Move
            else if (eData.type == 2) {
                MouseMoveData event = (MouseMoveData) eData;
                mouseMove(event.x, event.y);
                MouseMoveData.release(event);
            }
            // Mouse Scroll
            else if (eData.type == 3) {
                MouseScrollData event = (MouseScrollData) eData;
                Widget widget = activity.findByPosition(mouseX, mouseY, false);
                widget.fireScroll(new ScrollEvent(widget, ScrollEvent.SCROLL, event.x, event.y));

                MouseScrollData.release(event);
            }
            // Mouse Drop (system)
            else if (eData.type == 4) {
                MouseDropData event = (MouseDropData) eData;
                Widget widget = activity.findByPosition(mouseX, mouseY, false);
                widget.fireDrag(new DragEvent(widget, DragEvent.DROPPED, event.paths, mouseX, mouseY));

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

                if (activity != null) {
                    activity.invalidate(true);
                }

                SizeData.release(event);
            }
        }
        eventsCp.clear();
    }

    private static void mouseMove(float x, float y) {
        Widget widget = activity.findByPosition(mouseX = x, mouseY = y, false);

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
            }
            widget.fireHover(new HoverEvent(widget, HoverEvent.MOVED, widget, x, y));
            pointer.hover = widget;
        }
        // Drag
        else {
            DragEvent dragEvent;
            if (mouse.dragged == null) {
                mouse.dragged = mouse.pressed;
                mouse.hover = widget;

                mouse.dragged.fireDrag(dragEvent = new DragEvent(mouse.dragged, DragEvent.STARTED, mouse.dragData, x, y));
                mouse.dragged.setDragged(true);

                mouse.dragData = dragEvent.getData();
                mouse.dragStarted = dragEvent.isStarted();
            }
            if (mouse.dragStarted) {
                if (mouse.hover != widget) {
                    if (!widget.isChildOf(mouse.hover)) {
                        mouse.hover.fireDrag(dragEvent = new DragEvent(mouse.hover, DragEvent.EXITED, widget, mouse.dragData, x, y));
                        mouse.dragData = dragEvent.getData();
                    }
                    if (!mouse.hover.isChildOf(widget)) {
                        widget.fireDrag(dragEvent = new DragEvent(widget, DragEvent.ENTERED, mouse.hover, mouse.dragData, x, y));
                        mouse.dragData = dragEvent.getData();
                    }
                }
                widget.fireDrag(dragEvent = new DragEvent(widget, DragEvent.OVER, mouse.dragData, x, y));
                mouse.dragData = dragEvent.getData();

                mouse.hover = widget;
            }

            mouse.dragged.firePointer(new PointerEvent(mouse.dragged, PointerEvent.DRAGGED, mouse.mouseButton, x, y));
        }
    }

    static void processAnimations() {
        for (int i = 0; i < anims.size(); i++) {
            Animation anim = anims.get(i);
            if (anim.isPlaying()) {
                anim.handle(loopTime);
            }
            if (!anim.isPlaying()) {
                anims.remove(i--);
            }
        }
    }

    static void processLayout() {
        if (activity.layout()) {
            activity.onLayout(getClientWidth(), getClientHeight(), (float) getDpi());
        }
    }

    static void processDraws() {
        if (activity.draw()) {
            SmartContext smartContext = context.getSmartContext();
            activity.onDraw(smartContext);

            smartContext.softFlush();
            WL.SwapBuffers();
            GL.Finish();

            if (vsync == 0) {
                long time = System.currentTimeMillis() - loopTime;
                if (time < 15) {
                    try {
                        Thread.sleep(15 - time);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        } else {
            long time = System.currentTimeMillis() - loopTime;
            if (time < 15) {
                try {
                    Thread.sleep(15 - time);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    static void processSyncCalls() {
        synchronized (key) {
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
        synchronized (key) {
            runSync.add(task);
            key.notifyAll();
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

    public static void runAnimation(Animation animation) {
        if (!anims.contains(animation)) {
            anims.add(animation);
        }
    }

    public static Activity getActivity() {
        return activity;
    }

    public static void setActivity(Activity activity) {
        Application.activity = activity;
        Application.activity.invalidate(true);
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

    public static double getDpi() {
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
            if (entity.touchId == touchId && entity.mouseButton == mb) {
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
        final int mouseButton, touchId;

        Widget pressed, dragged, hover;

        boolean dragStarted;
        Object dragData;

        PointerData(int mouseButton, int touchId) {
            this.mouseButton = mouseButton;
            this.touchId = touchId;
        }

        void reset() {
            pressed = dragged = hover = null;
            dragStarted = false;
            dragData = null;
        }
    }

    public interface GraphicTask {
        void run(Context context);
    }

    public static class Settings {

        public final File resources;
        public Class<?> activityClass;
        public Runnable start;
        public int multsamples;
        public int width;
        public int height;
        public boolean transparent;
        public int vsync;

        public <T extends Activity> Settings(File resources) {
            this(resources, null, null);
        }

        public <T extends Activity> Settings(File resources, Class<T> activityClass) {
            this(resources, activityClass, null);
        }

        public <T extends Activity> Settings(File resources, Class<T> activityClass, Runnable start) {
            this(resources, activityClass, start, 0);
        }

        public <T extends Activity> Settings(File resources, Class<T> activityClass, Runnable start, int multsamples) {
            this(resources, activityClass, start, multsamples, 800, 600);
        }

        public <T extends Activity> Settings(File resources, Class<T> activityClass, Runnable start, int multsamples, int width, int height) {
            this(resources, activityClass, start, multsamples, width, height, false);
        }

        public <T extends Activity> Settings(File resources, Class<T> activityClass, Runnable start, int multsamples, int width, int height, boolean transparent) {
            this.resources = resources;
            this.activityClass = activityClass;
            this.start = start;
            this.multsamples = multsamples;
            this.width = width;
            this.height = height;
            this.transparent = transparent;
        }
    }
}
