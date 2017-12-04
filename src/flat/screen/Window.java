package flat.screen;

import flat.animations.Animation;
import flat.backend.*;
import flat.events.DragEvent;
import flat.events.KeyEvent;
import flat.events.PointerEvent;
import flat.graphics.Context;
import flat.graphics.image.Image;
import flat.widget.Widget;

import java.util.ArrayList;
import java.util.List;

public final class Window {

    private final Application app;
    private final Thread thread;
    private Activity activity;

    private static ArrayList<Runnable> runSync = new ArrayList<>();
    private static ArrayList<Runnable> runSyncCp = new ArrayList<>();

    private ArrayList<Animation> animSync = new ArrayList<>();
    private ArrayList<Animation> animSyncCp = new ArrayList<>();

    private ArrayList<EventData> events = new ArrayList<>();
    private ArrayList<EventData> eventsCp = new ArrayList<>();

    private Context context;
    private long loopTime;

    protected Window(Application app) {
        this.app = app;
        if (!WL.Init(80, 80, 800, 600, app.samples, app.resizable, app.decorated)) {
            throw new RuntimeException("Cannot create a window");
        }
        if (!SVG.Init(SVGEnuns.SVG_ANTIALIAS | SVGEnuns.SVG_STENCIL_STROKES)) {
            throw new RuntimeException("Cannot create a graphic context window");
        }

        this.thread = Thread.currentThread();
        this.context = Context.getContext();

        mouseX = (float) WL.GetCursorX();
        mouseY = (float) WL.GetCursorY();
        WL.SetInputMode(WLEnuns.STICKY_KEYS, 1);
        WL.SetInputMode(WLEnuns.STICKY_MOUSE_BUTTONS, 1);
        WL.SetMouseButtonCallback((button, action, mods) -> events.add(MouseBtnData.get(button, action, mods)));
        WL.SetCursorPosCallback((x, y) -> events.add(MouseMoveData.get(outMouseX = (float) x, outMouseY = (float) y)));
        WL.SetScrollCallback((x, y) -> events.add(MouseScrollData.get(x, y)));
        WL.SetDropCallback(names -> events.add(MouseDropData.get(names)));
        WL.SetKeyCallback((key, scancode, action, mods) -> events.add(KeyData.get(key, scancode, action, mods)));
        WL.SetCharModsCallback((codepoint, mods) -> events.add(CharModsData.get(codepoint, mods)));
        WL.SetWindowSizeCallback((width, height) -> {
            context.resize(width, height);
            if (activity != null) activity.invalidate(true);
        });
    }

    public Context getContext() {
        return context;
    }

    public Thread getThread() {
        return thread;
    }

    protected void launch() {
        app.start(this);
        while (!WL.IsClosed()) {
            loopTime = System.currentTimeMillis();

            // Events
            processEvents();

            // Animation
            processAnimations();

            // Layout
            processLayout();

            // Draw
            processDraws();

            // Sync Calls
            processSyncCalls();
        }
        SVG.Finish();
        WL.Finish();
    }

    protected void processEvents() {
        WL.HandleEvents();
        ArrayList<EventData> swap = eventsCp;
        eventsCp = events;
        events = swap;

        if  (outMouseX != WL.GetCursorX() || outMouseY != WL.GetCursorY()) {
            eventsCp.add(MouseMoveData.get(outMouseX = (float) WL.GetCursorX(), outMouseY = (float) WL.GetCursorY()));
        }

        for (EventData eData : eventsCp) {
            // Mouse Button
            if (eData.type == 1) {
                MouseBtnData event = (MouseBtnData) eData;
                PointerData pointer = getPointer(event.btn, -1, null);
                Widget widget = activity.findByPosition(mouseX, mouseY);

                // Pressed
                if (event.action == WLEnuns.PRESS) {
                    if (mouse == null) {
                        mouse = pointer;
                        mouse.pressed = widget;
                    }
                    mouse.pressed.firePointer(new PointerEvent(mouse.pressed, PointerEvent.PRESSED, event.btn, mouseX, mouseY));
                }
                // Released
                else if (event.action == WLEnuns.RELEASE) {
                    if (mouse == pointer) {
                        if (mouse.dragStarted) {
                            DragEvent dragEvent = new DragEvent(widget, DragEvent.DROPPED, mouse.dragData, mouseX, mouseY);
                            widget.fireDrag(dragEvent);
                            mouse.dragData = dragEvent.getData();
                            if (dragEvent.isDragCompleted()) {
                                mouse.dragged.fireDrag(new DragEvent(mouse.pressed, DragEvent.DONE, mouse.dragData, mouseX, mouseY));
                            }
                        }
                        mouse.reset();
                    } else if (mouse != null) {
                        mouse.pressed.firePointer(new PointerEvent(mouse.pressed, PointerEvent.RELEASED, event.btn, mouseX, mouseY));
                    }
                    mouse = null;
                }
                MouseBtnData.release(event);

            }
            // Mouse Move
            else if (eData.type == 2) {
                MouseMoveData event = (MouseMoveData) eData;
                Widget widget = activity.findByPosition(mouseX = event.x, mouseY = event.y);

                // Move
                if (mouse == null) {
                    PointerData pointer = getPointer(-1, -1, null);
                    if (pointer.hover == null) pointer.hover = widget;
                    if (pointer.hover != widget) {
                        pointer.hover.firePointer(new PointerEvent(pointer.hover, PointerEvent.EXITED, 0, mouseX, mouseY));
                        widget.firePointer(new PointerEvent(widget, PointerEvent.ENTERED, 0, mouseX, mouseY));
                    }
                    widget.firePointer(new PointerEvent(widget, PointerEvent.MOVED, 0, mouseX, mouseY));
                    pointer.hover = widget;
                }
                // Drag
                else {
                    DragEvent dragEvent;
                    if (mouse.dragged == null) {
                        mouse.dragged = widget;
                        mouse.hover = widget;
                        widget.fireDrag(dragEvent = new DragEvent(widget, DragEvent.STARTED, mouse.dragData, mouseX, mouseY));
                        mouse.dragData = dragEvent.getData();
                        mouse.dragStarted = dragEvent.isStarted();
                    }
                    if (mouse.dragStarted) {
                        if (mouse.hover != widget) {
                            mouse.hover.fireDrag(dragEvent = new DragEvent(mouse.hover, DragEvent.EXITED, mouse.dragData, mouseX, mouseY));
                            mouse.dragData = dragEvent.getData();
                            widget.fireDrag(dragEvent = new DragEvent(widget, DragEvent.ENTERED, mouse.dragData, mouseX, mouseY));
                            mouse.dragData = dragEvent.getData();
                        }
                        widget.fireDrag(dragEvent = new DragEvent(widget, DragEvent.OVER, mouse.dragData, mouseX, mouseY));
                        mouse.dragData = dragEvent.getData();
                        mouse.hover = widget;
                    }

                    widget.firePointer(new PointerEvent(mouse.dragged, PointerEvent.DRAGGED, mouse.mouseButton, mouseX, mouseY));
                }
                MouseMoveData.release(event);

            }
            // Mouse Scroll
            else if (eData.type == 3) {
                MouseScrollData event = (MouseScrollData) eData;
                Widget widget = activity.findByPosition(mouseX, mouseY);
                widget.firePointer(new PointerEvent(widget, PointerEvent.SCROLL, 0, event.x, event.y));
                MouseScrollData.release(event);

            }
            // Mouse Drop (system)
            else if (eData.type == 4) {
                MouseDropData event = (MouseDropData) eData;
                Widget widget = activity.findByPosition(mouseX, mouseY);
                widget.fireDrag(new DragEvent(widget, DragEvent.DROPPED, event.paths, mouseX, mouseY));
                MouseDropData.release(event);

            }
            // Key
            else if (eData.type == 5) {
                KeyData event = (KeyData) eData;
                Widget widget = activity.findFocused();
                int eventType = (event.action == WLEnuns.PRESS) ? KeyEvent.PRESSED :
                        (event.action == WLEnuns.RELEASE) ? KeyEvent.RELEASED : KeyEvent.REPEATED;

                boolean shift = (event.mods & (WLEnuns.MOD_SHIFT)) != 0;
                boolean control = (event.mods & (WLEnuns.MOD_CONTROL)) != 0;
                boolean alt = (event.mods & (WLEnuns.MOD_ALT)) != 0;
                boolean meta = (event.mods & (WLEnuns.MOD_SUPER)) != 0;

                widget.fireKey(new KeyEvent(widget, eventType, shift, control, alt, meta, "", event.key));
                KeyData.release(event);
            }
            // Char Typed
            else if (eData.type == 7) {
                CharModsData event = (CharModsData) eData;
                Widget widget = activity.findFocused();

                boolean shift = (event.mods & (WLEnuns.MOD_SHIFT)) != 0;
                boolean control = (event.mods & (WLEnuns.MOD_CONTROL)) != 0;
                boolean alt = (event.mods & (WLEnuns.MOD_ALT)) != 0;
                boolean meta = (event.mods & (WLEnuns.MOD_SUPER)) != 0;

                String value = new String(Character.toChars(event.codepoint));
                widget.fireKey(new KeyEvent(widget, KeyEvent.TYPED, shift, control, alt, meta, value, -1));

                CharModsData.release(event);
            }
        }
        eventsCp.clear();
    }

    protected void processAnimations() {
        long time = System.currentTimeMillis();

        ArrayList<Animation> animSwap = animSyncCp;
        animSyncCp = animSync;
        animSync = animSwap;

        for (Animation anim : animSyncCp) {
            anim.handle(time);
        }
        animSyncCp.clear();
    }

    protected void processLayout() {
        if (activity.layout()) {
            activity.onLayout(getClientWidth(), getClientHeight());
        }
    }

    protected void processDraws() {
        if (activity.draw()) {
            activity.onDraw(context);

            context.setModeClear();
            WL.SwapBuffers();
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

    protected void processSyncCalls() {
        synchronized (Window.class) {
            ArrayList<Runnable> swap = runSyncCp;
            runSyncCp = runSync;
            runSync = swap;
        }
        for (Runnable run : runSyncCp) {
            run.run();
        }
        runSyncCp.clear();
    }

    public static void runSync(Runnable task) {
        synchronized (Window.class) {
            runSync.add(task);
        }
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
        this.activity.invalidate(true);
    }

    public void setFullscreen(boolean fullscreen) {
        WL.SetFullscreen(fullscreen);
    }

    public boolean isFullscreen() {
        return WL.IsFullscreen();
    }

    public boolean isResisable() {
        return WL.IsResizable();
    }

    public boolean isDecorated() {
        return WL.IsDecorated();
    }

    public String getTitle() {
        return WL.GetTitle();
    }

    public void setTitle(String title) {
        WL.SetTitle(title);
    }

    public void setIcon(Image icons) {

    }

    public int getX() {
        return WL.GetX();
    }

    public void setX(int x) {
        WL.SetPosition(x, WL.GetY());
    }

    public int getY() {
        return WL.GetY();
    }

    public void setY(int y) {
        WL.SetPosition(WL.GetX(), y);
    }

    public void setPosition(int x, int y) {
        WL.SetPosition(x, y);
    }

    public int getClientWidth() {
        return WL.GetClientWidth();
    }

    public int getClientHeight() {
        return WL.GetClientHeight();
    }

    public int getWidth() {
        return WL.GetWidth();
    }

    public void setWidth(int width) {
        WL.SetSize(width, WL.GetHeight());
    }

    public int getHeight() {
        return WL.GetHeight();
    }

    public void setHeight(int height) {
        WL.SetSize(WL.GetWidth(), height);
    }

    public void setSize(int width, int height) {
        WL.SetSize(width, height);
    }

    public int getMinWidth() {
        return WL.GetMinWidth();
    }

    public void setMinWidth(int minWidth) {
        WL.SetSizeLimits(minWidth, WL.GetMinHeight(), WL.GetMaxWidth(), WL.GetMaxHeight());
    }

    public int getMinHeight() {
        return WL.GetMinHeight();
    }

    public void setMinHeight(int minHeight) {
        WL.SetSizeLimits(WL.GetMinWidth(), minHeight, WL.GetMaxWidth(), WL.GetMaxHeight());
    }

    public int getMaxWidth() {
        return WL.GetMaxWidth();
    }

    public void setMaxWidth(int maxWidth) {
        WL.SetSizeLimits(WL.GetMinWidth(), WL.GetMinHeight(), maxWidth, WL.GetMaxHeight());
    }

    public int getMaxHeight() {
        return WL.GetMaxHeight();
    }

    public void setMaxHeight(int maxHeight) {
        WL.SetSizeLimits(WL.GetMinWidth(), WL.GetMinHeight(), WL.GetMaxWidth(), maxHeight);
    }

    public void setSizeLimits(int minWidth, int minHeight, int maxWidth, int maxHeight) {
        WL.SetSizeLimits(minWidth, minHeight, maxWidth, maxHeight);
    }

    public void show() {
        WL.Show();
    }

    public void hide() {
        WL.Hide();
    }

    public void maximize() {
        WL.Maximize();
    }

    public void minimize() {
        WL.Minimize();
    }

    public void restore() {
        WL.Restore();
    }

    public void focus() {
        WL.Focus();
    }

    public void invalidate() {

    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    private static class EventData {
        int type;
    }

    private static class MouseBtnData extends EventData {
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

        public int btn, action, mods;

        public void set(int btn, int action, int mods) {
            this.type = 1;
            this.btn = btn;
            this.action = action;
            this.mods = mods;
        }
    }

    private static class MouseMoveData extends EventData {
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

        public float x, y;

        public void set(float x, float y) {
            this.type = 2;
            this.x = x;
            this.y = y;
        }
    }

    private static class MouseScrollData extends EventData {
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

        public float x, y;

        public void set(float x, float y) {
            this.type = 3;
            this.x = x;
            this.y = y;
        }
    }

    private static class MouseDropData extends EventData {
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

        public String[] paths;

        public void set(String[] paths) {
            this.type = 4;
            this.paths = paths;
        }
    }

    private static class KeyData extends EventData {
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

        public int key, scancode, action, mods;

        public void set(int key, int scancode, int action, int mods) {
            this.type = 5;
            this.key = key;
            this.scancode = scancode;
            this.action = action;
            this.mods = mods;
        }
    }

    private static class CharModsData extends EventData {
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

        public int codepoint, mods;

        public void set(int codepoint, int mods) {
            this.type = 7;
            this.codepoint = codepoint;
            this.mods = mods;
        }
    }

    public class PointerData {
        final int mouseButton, touchId;

        Widget pressed, dragged, hover;

        boolean dragStarted;
        Object dragData;

        private PointerData(int mouseButton, int touchId) {
            this.mouseButton = mouseButton;
            this.touchId = touchId;
        }

        private void reset() {
            pressed = dragged = hover = null;
            dragStarted = false;
            dragData = null;
        }
    }

    private PointerData mouse;
    private float mouseX, mouseY, outMouseX, outMouseY;
    private ArrayList<PointerData> pointersData = new ArrayList<>();

    private PointerData getPointer(int mb, int pid, List<PointerData> points) {
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
}
