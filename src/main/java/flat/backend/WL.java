package flat.backend;

import flat.graphics.image.ImageData;

public class WL {

    //---------------------------
    //         Context
    //---------------------------
    public static native boolean Init();
    public static native void Finish();
    public static native long WindowCreate(int width, int height, int samples, boolean transparent);
    public static native void WindowAssign(long window);
    public static native void WindowDestroy(long window);

    //---------------------------
    //         Events
    //---------------------------
    public static native void SwapBuffers(long window);
    public static native void HandleEvents(double wait);
    public static native void SetVsync(int vsync);
    public static native void PostEmptyEvent();

    //---------------------------
    //       Properties
    //---------------------------
    public static native void SetFullscreen(long window, boolean fullscreen);
    public static native boolean IsFullscreen(long window);
    public static native void SetResizable(long window, boolean resizable);
    public static native boolean IsResizable(long window);
    public static native void SetDecorated(long window, boolean decorated);
    public static native boolean IsDecorated(long window);
    public static native boolean IsTransparent(long window);
    public static native void SetTitle(long window, String title);
    public static native void SetIcon(long window, byte[] image, int width, int height);
    public static native void SetPosition(long window, int x, int y);
    public static native int GetX(long window);
    public static native int GetY(long window);
    public static native void SetSize(long window, int width, int height);
    public static native int GetWidth(long window);
    public static native int GetHeight(long window);
    public static native int GetClientWidth(long window);
    public static native int GetClientHeight(long window);
    public static native int GetMonitorWidth(long window);
    public static native int GetMonitorHeight(long window);
    public static native double GetPhysicalWidth(long window);
    public static native double GetPhysicalHeight(long window);
    public static native double GetDpi(long window);

    public static native void SetSizeLimits(long window, int minWidth, int minHeight, int maxWidth, int maxHeight);

    public static native void Show(long window);
    public static native void Hide(long window);
    public static native void Close(long window);
    public static native void Maximize(long window);
    public static native void Minimize(long window);
    public static native void Restore(long window);
    public static native void Focus(long window);

    public static native boolean IsShown(long window);
    public static native boolean IsClosed(long window);
    public static native boolean IsMaximized(long window);
    public static native boolean IsMinimized(long window);

    //---------------------------
    //          Input
    //---------------------------
    public static native int GetInputMode(long window, int mode);
    public static native void SetInputMode(long window, int mode, int value);
    public static native void SetClipboardString(long window, String clipboard);
    public static native String GetClipboardString(long window);
    public static native void SetClipboardImage(long window, ImageData imageData);
    public static native ImageData GetClipboardImage(long window);

    public static native String GetKeyName(int key, int scancode);
    public static native int GetKey(long window, int key);
    public static native int GetMouseButton(long window, int button);

    public static native double GetCursorX(long window);
    public static native double GetCursorY(long window);
    public static native void SetCursorPos(long window, double xpos, double ypos);

    public static native long CreateCursor(byte[] image, int width, int height, int xhot, int yhot);
    public static native long CreateStandardCursor(int shape);
    public static native void DestroyCursor(long cursor);
    public static native void SetCursor(long window, long cursor);

    public static native int JoystickPresent(int joy);
    public static native int GetJoystickAxesCount(int joy);
    public static native void GetJoystickAxes(int joy, float[] axes);
    public static native int GetJoystickButtonsCount(int joy);
    public static native void GetJoystickButtons(int joy, int[] buttons);
    public static native String GetJoystickName(int joy);

    //---------------------------
    //       Callbacks
    //---------------------------
    public static native void SetWindowPosCallback(WLEnums.WindowPosCallback callback);
    public static native void SetWindowSizeCallback(WLEnums.WindowSizeCallback callback);
    public static native void SetWindowCloseCallback(WLEnums.WindowCloseCallback callback);
    public static native void SetWindowRefreshCallback(WLEnums.WindowRefreshCallback callback);
    public static native void SetWindowFocusCallback(WLEnums.WindowFocusCallback callback);
    public static native void SetWindowIconifyCallback(WLEnums.WindowIconifyCallback callback);
    public static native void SetFramebufferSizeCallback(WLEnums.WindowBufferSizeCallback callback);

    public static native void SetKeyCallback(WLEnums.KeyCallback callback);
    public static native void SetCharCallback(WLEnums.CharCallback callback);
    public static native void SetCharModsCallback(WLEnums.CharModsCallback callback);
    public static native void SetMouseButtonCallback(WLEnums.MouseButtonCallback callback);
    public static native void SetCursorPosCallback(WLEnums.CursorPosCallback callback);
    public static native void SetCursorEnterCallback(WLEnums.CursorEnterCallback callback);
    public static native void SetScrollCallback(WLEnums.ScrollCallback callback);
    public static native void SetDropCallback(WLEnums.DropCallback callback);

    public static native void SetJoystickCallback(WLEnums.JoyCallback callback);
    public static native void SetErrorCallback(WLEnums.ErrorCallback callback);

    //---------------------------
    //       Dialogs
    //---------------------------
    public static native void ShowOpenFile(long window, String fileFilters, String initialFolder, WLEnums.DialogCallback callback);
    public static native void ShowOpenMultipleFiles(long window, String fileFilters, String initialFolder, WLEnums.DialogCallback callback);
    public static native void ShowSaveFile(long window, String fileFilters, String initialFolder, WLEnums.DialogCallback callback);
    public static native void ShowOpenFolder(long window, String initialFolder, WLEnums.DialogCallback callback);
}