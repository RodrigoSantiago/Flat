package flat.backend;

public class WL {

    //---------------------------
    //         Context2D
    //---------------------------
    public static native boolean Init(int x, int y, int width, int height, int samples, boolean resizable, boolean decorated);
    public static native void Finish();

    //---------------------------
    //         Events
    //---------------------------
    public static native boolean SwapBuffers();
    public static native boolean HandleEvents();

    //---------------------------
    //       Properties
    //---------------------------
    public static native void SetVsync(int vsync);
    public static native void SetMultsample(int samples);
    public static native void SetFullscreen(boolean fullscreen);
    public static native boolean IsFullscreen();
    public static native boolean IsResizable();
    public static native boolean IsDecorated();
    public static native void SetTitle(String title);
    public static native String GetTitle();
    public static native void SetIcon(byte[] image, int width, int height);
    public static native void SetPosition(int x, int y);
    public static native int GetX();
    public static native int GetY();
    public static native void SetSize(int width, int height);
    public static native int GetWidth();
    public static native int GetHeight();
    public static native int GetClientWidth();
    public static native int GetClientHeight();

    public static native void SetSizeLimits(int minWidth, int minHeight, int maxWidth, int maxHeight);
    public static native int GetMinWidth();
    public static native int GetMinHeight();
    public static native int GetMaxWidth();
    public static native int GetMaxHeight();

    public static native void Show();
    public static native void Hide();
    public static native void Close();
    public static native void Maximize();
    public static native void Minimize();
    public static native void Restore();
    public static native void Focus();

    public static native boolean IsShown();
    public static native boolean IsClosed();
    public static native boolean IsMaximized();
    public static native boolean IsMinimized();

    //---------------------------
    //          Input
    //---------------------------
    public static native int GetInputMode(int mode);
    public static native void SetInputMode(int mode, int value);

    public static native String GetKeyName(int key, int scancode);
    public static native int GetKey(int key);
    public static native int GetMouseButton(int button);

    public static native double GetCursorX();
    public static native double GetCursorY();
    public static native void SetCursorPos(double xpos, double ypos);

    public static native long CreateCursor(byte[] image, int width, int height, int xhot, int yhot);
    public static native long CreateStandardCursor(int shape);
    public static native void DestroyCursor(long cursor);
    public static native void SetCursor(long cursor);

    public static native int JoystickPresent(int joy);
    public static native int GetJoystickAxesCount(int joy);
    public static native void GetJoystickAxes(int joy, float[] axes);
    public static native int GetJoystickButtonsCount(int joy);
    public static native void GetJoystickButtons(int joy, int[] buttons);
    public static native String GetJoystickName(int joy);

    //---------------------------
    //       Callbacks
    //---------------------------
    public static native void SetWindowPosCallback(WindowPosCallback callback);
    public static native void SetWindowSizeCallback(WindowSizeCallback callback);
    public static native void SetWindowCloseCallback(WindowCloseCallback callback);
    public static native void SetWindowRefreshCallback(WindowRefreshCallback callback);
    public static native void SetWindowFocusCallback(WindowFocusCallback callback);
    public static native void SetWindowIconifyCallback(WindowIconifyCallback callback);
    public static native void SetFramebufferSizeCallback(WindowBufferSizeCallback callback);

    public static native void SetKeyCallback(KeyCallback callback);
    public static native void SetCharCallback(CharCallback callback);
    public static native void SetCharModsCallback(CharModsCallback callback);
    public static native void SetMouseButtonCallback(MouseButtonCallback callback);
    public static native void SetCursorPosCallback(CursorPosCallback callback);
    public static native void SetCursorEnterCallback(CursorEnterCallback callback);
    public static native void SetScrollCallback(ScrollCallback callback);
    public static native void SetDropCallback(DropCallback callback);

    public static native void SetJoystickCallback(JoyCallback callback);

    public interface WindowPosCallback {
        void handle(int x, int y);
    }
    public interface WindowSizeCallback {
        void handle(int width, int height);
    }
    public interface WindowCloseCallback {
        boolean handle();
    }
    public interface WindowRefreshCallback {
        void handle();
    }
    public interface WindowFocusCallback {
        void handle(boolean focus);
    }
    public interface WindowIconifyCallback {
        void handle(boolean minimized);
    }
    public interface WindowBufferSizeCallback {
        void handle(int width, int height);
    }

    public interface MouseButtonCallback {
        void handle(int button, int action, int mods);
    }
    public interface CursorPosCallback {
        void handle(double x, double y);
    }
    public interface CursorEnterCallback {
        void handle(boolean entered);
    }
    public interface KeyCallback {
        void handle(int key, int scancode, int action, int mods);
    }
    public interface CharCallback {
        void handle(int codepoint);
    }
    public interface CharModsCallback {
        void handle(int codepoint, int mods);
    }
    public interface DropCallback {
        void handle(String[] names);
    }
    public interface ScrollCallback {
        void handle(double x, double y);
    }

    public interface JoyCallback {
        void handle(int joy, boolean connected);
    }
}