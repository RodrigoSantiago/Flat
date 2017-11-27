package flat.acess;


public class WL {
    static {
        System.loadLibrary("wl");
    }

    public static void load() {
        System.out.println("Window Layer Library loaded");
    }

    public static native boolean Init();
    public static native boolean Loop();
    public static native void Terminate();

    public static native long Create(int x, int y, int width, int height, boolean resizable, boolean decorated);
    public static native void Destroy(long id);
    public static native void SetVsync(long id,int vsync);
    public static native void SetFullscreen(long id, boolean fullscreen);
    public static native boolean IsFullscreen(long id);
    public static native boolean IsResizable(long id);
    public static native boolean IsDecorated(long id);
    public static native void SetTitle(long id, String title);
    public static native String GetTitle(long id);
    public static native void SetIcon(long id, int[] image, int width, int height);
    public static native void SetPosition(long id, int x, int y);
    public static native int GetX(long id);
    public static native int GetY(long id);
    public static native void SetSize(long id, int width, int height);
    public static native int GetWidth(long id);
    public static native int GetHeight(long id);

    public static native void SetSizeLimits(long id, int minWidth, int minHeight, int maxWidth, int maxHeight);
    public static native int GetMinWidth(long id);
    public static native int GetMinHeight(long id);
    public static native int GetMaxWidth(long id);
    public static native int GetMaxHeight(long id);

    public static native void Show(long id);
    public static native void Hide(long id);
    public static native void Maximize(long id);
    public static native void Minimize(long id);
    public static native void Restore(long id);
    public static native void Focus(long id);

    public static native boolean IsShown(long id);
    public static native boolean IsMaximized(long id);
    public static native boolean IsMinimized(long id);

    public static native int GetInputMode(long id, int mode);
    public static native void SetInputMode(long id, int mode, int value);

    public static native String GetKeyName(int key, int scancode);
    public static native int GetKey(long id, int key);
    public static native int GetMouseButton(long id, int button);

    public static native double GetCursorX(long id);
    public static native double GetCursorY(long id);
    public static native void SetCursorPos(long id, double xpos, double ypos);

    public static native long CreateCursor(int[] image, int length, int xhot, int yhot);
    public static native long CreateStandardCursor(int shape);
    public static native void DestroyCursor(long cursor);
    public static native void SetCursor(long id, long cursor);

    public static native int JoystickPresent(int joy);
    public static native int GetJoystickAxesCount(int joy);
    public static native void GetJoystickAxes(int joy, float[] axes);
    public static native int GetJoystickButtonsCount(int joy);
    public static native void GetJoystickButtons(int joy, int[] buttons);
    public static native String GetJoystickName(int joy);

    public static native void SetWindowBeforeEventsCallback(long id, WindowCallback callback);
    public static native void SetWindowAfterEventsCallback(long id, WindowCallback callback);

    public static native void SetWindowPosCallback(long id, WindowPosCallback callback);
    public static native void SetWindowSizeCallback(long id, WindowSizeCallback callback);
    public static native void SetWindowCloseCallback(long id, WindowCloseCallback callback);
    public static native void SetWindowRefreshCallback(long id, WindowRefreshCallback callback);
    public static native void SetWindowFocusCallback(long id, WindowFocusCallback callback);
    public static native void SetWindowIconifyCallback(long id, WindowIconifyCallback callback);
    public static native void SetFramebufferSizeCallback(long id, WindowBufferSizeCallback callback);

    public static native void SetKeyCallback(long id, KeyCallback callback);
    public static native void SetCharCallback(long id, CharCallback callback);
    public static native void SetCharModsCallback(long id, CharModsCallback callback);
    public static native void SetMouseButtonCallback(long id, MouseButtonCallback callback);
    public static native void SetCursorPosCallback(long id, CursorPosCallback callback);
    public static native void SetCursorEnterCallback(long id, CursorEnterCallback callback);
    public static native void SetScrollCallback(long id, ScrollCallback callback);
    public static native void SetDropCallback(long id, DropCallback callback);

    public static native void SetJoystickCallback(JoyCallback callback);

    public interface WindowCallback {
        void handle(long id);
    }
    public interface WindowPosCallback {
        void handle(long id, int x, int y);
    }
    public interface WindowSizeCallback {
        void handle(long id, int x, int y);
    }
    public interface WindowCloseCallback {
        boolean handle(long id);
    }
    public interface WindowRefreshCallback {
        void handle(long id);
    }
    public interface WindowFocusCallback {
        void handle(long id, boolean focus);
    }
    public interface WindowIconifyCallback {
        void handle(long id, boolean minimized);
    }
    public interface WindowBufferSizeCallback {
        void handle(long id, int width, int height);
    }

    public interface MouseButtonCallback {
        void handle(long id, int button, int action, int mods);
    }
    public interface CursorPosCallback {
        void handle(long id, double x, double y);
    }
    public interface CursorEnterCallback {
        void handle(long id, boolean entered);
    }
    public interface KeyCallback {
        void handle(long id, int key, int scancode, int action, int mods);
    }
    public interface CharCallback {
        void handle(long id, int codepoint);
    }
    public interface CharModsCallback {
        void handle(long id, int codepoint, int mods);
    }
    public interface DropCallback {
        void handle(long id, String[] names);
    }
    public interface ScrollCallback {
        void handle(long id, double x, double y);
    }

    public interface JoyCallback {
        void handle(int joy, boolean connected);
    }
}