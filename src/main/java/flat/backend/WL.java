package flat.backend;

public class WL {

    //---------------------------
    //         Context
    //---------------------------
    public static native long Init(int width, int height, int samples, boolean transparent);
    public static native void Finish();

    public static native long ContextCreate(int samples);
    public static native void ContextAssign(long context);
    public static native void ContextDestroy(long context);

    //---------------------------
    //         Events
    //---------------------------
    public static native void SwapBuffers();
    public static native void HandleEvents();

    //---------------------------
    //       Properties
    //---------------------------
    public static native void SetVsync(int vsync);
    public static native boolean IsTransparent();
    public static native void SetFullscreen(boolean fullscreen);
    public static native boolean IsFullscreen();
    public static native void SetResizable(boolean resizable);
    public static native boolean IsResizable();
    public static native void SetDecorated(boolean decorated);
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
    public static native double GetPhysicalWidth();
    public static native double GetPhysicalHeight();
    public static native double GetDpi();

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
    public static native void SetWindowPosCallback(WLEnuns.WindowPosCallback callback);
    public static native void SetWindowSizeCallback(WLEnuns.WindowSizeCallback callback);
    public static native void SetWindowCloseCallback(WLEnuns.WindowCloseCallback callback);
    public static native void SetWindowRefreshCallback(WLEnuns.WindowRefreshCallback callback);
    public static native void SetWindowFocusCallback(WLEnuns.WindowFocusCallback callback);
    public static native void SetWindowIconifyCallback(WLEnuns.WindowIconifyCallback callback);
    public static native void SetFramebufferSizeCallback(WLEnuns.WindowBufferSizeCallback callback);

    public static native void SetKeyCallback(WLEnuns.KeyCallback callback);
    public static native void SetCharCallback(WLEnuns.CharCallback callback);
    public static native void SetCharModsCallback(WLEnuns.CharModsCallback callback);
    public static native void SetMouseButtonCallback(WLEnuns.MouseButtonCallback callback);
    public static native void SetCursorPosCallback(WLEnuns.CursorPosCallback callback);
    public static native void SetCursorEnterCallback(WLEnuns.CursorEnterCallback callback);
    public static native void SetScrollCallback(WLEnuns.ScrollCallback callback);
    public static native void SetDropCallback(WLEnuns.DropCallback callback);

    public static native void SetJoystickCallback(WLEnuns.JoyCallback callback);
}