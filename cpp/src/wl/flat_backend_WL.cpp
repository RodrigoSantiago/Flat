//
// Created by Rodrigo Santiago on 27/11/2017
//

#include "flat_backend_WL.h"
#include <glad/glad.h>
#include <glfw/glfw3.h>
#include <vector>
#include <cstring>
#include <functional>
#include <unordered_map>
#include <iostream>

static GLFWwindow * window = nullptr;

static long int x;
static long int y;
static long int width;
static long int height;
static long int minw = -1;
static long int minh = -1;
static long int maxw = -1;
static long int maxh = -1;
static bool transparent = false;
static std::string title = "";

static JNIEnv* sjEnv;

template<class T>
class jLambda;

template<class R, class ...Args> class jLambda<R(Args...)> {
public:
    jobject obj;
    jmethodID method;

    jLambda(jobject jobj, jmethodID jmth) : obj(nullptr), method (nullptr) {
        set(jobj, jmth);
    }

    jLambda(std::nullptr_t) : obj(nullptr), method (nullptr) {
        set(nullptr, nullptr);
    }

    jLambda(const jLambda<R(Args...)>& other) : obj(nullptr), method (nullptr) {
        set(other.obj, other.method);
    }

    jLambda<R(Args...)>& operator=(std::nullptr_t) {
        set(nullptr, nullptr);
        return *this;
    }

    jLambda<R(Args...)>& operator=(jLambda<R(Args...)> other) {
        set(other.obj, other.method);
        return *this;
    }

    void run(Args... args) {
        sjEnv->CallVoidMethod(obj, method, args...);
    }

    R bRun(Args... args) {
        return sjEnv->CallBooleanMethod(obj, method, args...);
    }

    void set(jobject jobj, jmethodID jmth) {
        if (obj != nullptr) {
            sjEnv->DeleteGlobalRef(obj);
        }
        if (jobj != nullptr) {
            obj = sjEnv->NewGlobalRef(jobj);
            obj = sjEnv->NewGlobalRef(jobj);
        } else {
            obj = nullptr;
        }
        method = jmth;
    }

    ~jLambda() {
        set(nullptr, nullptr);
    }

    operator bool() const {
        return obj != nullptr;
    }
};

class localRefGuard {
public:
    jobject obj;
    localRefGuard(jobject o) : obj(o) {
    }

    ~localRefGuard() {
        sjEnv->DeleteLocalRef(obj);
    }
};

static jLambda<void(jint, jint)> sWindowPosCallback = nullptr;
static jLambda<void(jint, jint)> sWindowSizeCallback = nullptr;
static jLambda<bool()> sWindowCloseCallback = nullptr;
static jLambda<void()> sWindowRefreshCallback = nullptr;
static jLambda<void(jint)> sWindowFocusCallback = nullptr;
static jLambda<void(jint)> sWindowIconifyCallback = nullptr;
static jLambda<void(jint, jint)> sFramebufferSizeCallback = nullptr;
static jLambda<void(jint, jint, jint, jint)> sKeyCallback = nullptr;
static jLambda<void(jint)> sCharCallback = nullptr;
static jLambda<void(jint, jint)> sCharModsCallback = nullptr;
static jLambda<void(jint, jint, jint)> sMouseButtonCallback = nullptr;
static jLambda<void(jdouble, jdouble)> sCursorPosCallback = nullptr;
static jLambda<void(jint)> sCursorEnterCallback = nullptr;
static jLambda<void(jdouble, jdouble)> sScrollCallback = nullptr;
static jLambda<void(jobjectArray)> sDropCallback = nullptr;
static jLambda<void(jint, jint)> sJoystickCallback = nullptr;

namespace {
    void WindowPosCallback(GLFWwindow *a, int b, int c) {
        if (sWindowPosCallback) sWindowPosCallback.run(b, c);
    }

    void WindowSizeCallback(GLFWwindow *a, int b, int c) {
        if (sWindowSizeCallback) sWindowSizeCallback.run(b, c);
    }

    void WindowCloseCallback(GLFWwindow *a) {
        if (sWindowCloseCallback) {
            if (sWindowCloseCallback.bRun()) {
                glfwSetWindowShouldClose(a, false);
            } else {
                glfwHideWindow(a);
            }
        } else {
            glfwHideWindow(a);
        }
    }

    void WindowRefreshCallback(GLFWwindow *a) {
        if (sWindowRefreshCallback) sWindowRefreshCallback.run();
    }

    void WindowFocusCallback(GLFWwindow *a, int b) {
        if (sWindowFocusCallback) sWindowFocusCallback.run(b);
    }

    void WindowIconifyCallback(GLFWwindow *a, int b) {
        if (sWindowIconifyCallback) sWindowIconifyCallback.run(b);
    }

    void FramebufferSizeCallback(GLFWwindow *a, int b, int c) {
        if (sFramebufferSizeCallback) sFramebufferSizeCallback.run(b, c);
    }

    void KeyCallback(GLFWwindow *a, int b, int c, int d, int e) {
        if (sKeyCallback) sKeyCallback.run(b, c, d, e);
    }

    void CharCallback(GLFWwindow *a, unsigned int b) {
        if (sCharCallback) sCharCallback.run(b);
    }

    void CharModsCallback(GLFWwindow *a, unsigned int b, int c) {
        if (sCharModsCallback) sCharModsCallback.run(b, c);
    }

    void MouseButtonCallback(GLFWwindow *a, int b, int c, int d) {
        if (sMouseButtonCallback) sMouseButtonCallback.run(b, c, d);
    }

    void CursorPosCallback(GLFWwindow *a, double b, double c) {
        if (sCursorPosCallback) sCursorPosCallback.run(b, c);
    }

    void CursorEnterCallback(GLFWwindow *a, int b) {
        if (sCursorEnterCallback) sCursorEnterCallback.run(b);
    }

    void ScrollCallback(GLFWwindow *a, double b, double c) {
        if (sScrollCallback) sScrollCallback.run(b, c);
    }

    void DropCallback(GLFWwindow *a, int b, const char **c) {
        if (sDropCallback) {
            jobjectArray arr = sjEnv->NewObjectArray(b, sjEnv->FindClass("java/lang/String"), nullptr);
            for (int i = 0; i < b; i++) {
                jstring str = sjEnv->NewStringUTF(c[i]);
                sjEnv->SetObjectArrayElement(arr, i, str);
                sjEnv->DeleteLocalRef(str);
            }
            sDropCallback.run(arr);
            sjEnv->DeleteLocalRef(arr);
        }
    }

    void JoystickCallback(int a, int b) {
        if (sJoystickCallback) sJoystickCallback.run(a, b);
    }
}

GLFWmonitor * findMonitor() {
    int count;
    GLFWmonitor** monitors = glfwGetMonitors(&count);
    int x, y;
    glfwGetWindowPos(window, &x, &y);
    for (int i = 0; i < count; i++) {
        int x1, y1, x2, y2;
        glfwGetMonitorPos(monitors[i], &x1, &y1);
        const GLFWvidmode* mode = glfwGetVideoMode(monitors[i]);
        x2 = x1 + mode->width;
        y2 = y1 + mode->height;
        if (x >= x1 && x <= x2 && y >= y1 && y <= y2) {
            return monitors[i];
        }
    }
    return glfwGetPrimaryMonitor();
}

JNIEXPORT jlong JNICALL Java_flat_backend_WL_Init(JNIEnv * jEnv, jclass, jint width, jint height, jint samples, jboolean transparent) {
    sjEnv = jEnv;

    if (glfwInit()) {
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_SAMPLES, samples);
        glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, (::transparent = transparent) ? GLFW_TRUE : GLFW_FALSE);
        window = glfwCreateWindow(width, height, "", nullptr, nullptr);

        if (window == nullptr) {
            glfwTerminate();
            return 0;
        }

        glfwMakeContextCurrent(window);
        if (!gladLoadGLLoader((GLADloadproc) glfwGetProcAddress)) {
            glfwTerminate();
            return 0;
        }

        glfwSetWindowPosCallback(window, WindowPosCallback);
        glfwSetWindowSizeCallback(window, WindowSizeCallback);
        glfwSetWindowCloseCallback(window, WindowCloseCallback);
        glfwSetWindowRefreshCallback(window, WindowRefreshCallback);
        glfwSetWindowFocusCallback(window, WindowFocusCallback);
        glfwSetWindowIconifyCallback(window, WindowIconifyCallback);
        glfwSetFramebufferSizeCallback(window, FramebufferSizeCallback);
        glfwSetKeyCallback(window, KeyCallback);
        glfwSetCharCallback(window, CharCallback);
        glfwSetCharModsCallback(window, CharModsCallback);
        glfwSetMouseButtonCallback(window, MouseButtonCallback);
        glfwSetCursorPosCallback(window, CursorPosCallback);
        glfwSetCursorEnterCallback(window, CursorEnterCallback);
        glfwSetScrollCallback(window, ScrollCallback);
        glfwSetDropCallback(window, DropCallback);
        glfwSetJoystickCallback(JoystickCallback);
        return (jlong)window;
    } else {
        return 0;
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_Finish(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    sWindowPosCallback = nullptr;
    sWindowSizeCallback = nullptr;
    sWindowCloseCallback = nullptr;
    sWindowRefreshCallback = nullptr;
    sWindowFocusCallback = nullptr;
    sWindowIconifyCallback = nullptr;
    sFramebufferSizeCallback = nullptr;
    sKeyCallback = nullptr;
    sCharCallback = nullptr;
    sCharModsCallback = nullptr;
    sMouseButtonCallback = nullptr;
    sCursorPosCallback = nullptr;
    sCursorEnterCallback = nullptr;
    sScrollCallback = nullptr;
    sDropCallback = nullptr;
    sJoystickCallback = nullptr;

    glfwDestroyWindow(window);
    glfwTerminate();
}

JNIEXPORT jlong JNICALL Java_flat_backend_WL_ContextCreate(JNIEnv * jEnv, jclass jClass, jint samples) {
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
    glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);
    glfwWindowHint(GLFW_SAMPLES, samples);
    GLFWwindow* win = glfwCreateWindow(128, 128, "", nullptr, window);
    return (jlong)win;
}

JNIEXPORT void JNICALL Java_flat_backend_WL_ContextAssign(JNIEnv * jEnv, jclass jClass, jlong win) {
    glfwMakeContextCurrent((GLFWwindow*) win);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_ContextDestroy(JNIEnv * jEnv, jclass jClass, jlong win) {
    glfwDestroyWindow((GLFWwindow*) win);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SwapBuffers(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    glfwSwapBuffers(window);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_HandleEvents(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    glfwPollEvents();
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetVsync(JNIEnv * jEnv, jclass jClass, jint vsync) {
    sjEnv = jEnv;

    glfwSwapInterval(vsync);
}

JNIEXPORT jboolean JNICALL Java_flat_backend_WL_IsTransparent(JNIEnv * jEnv, jclass jClass) {
    return ::transparent;
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetFullscreen(JNIEnv * jEnv, jclass jClass, jboolean fullscreen) {
    sjEnv = jEnv;

    GLFWmonitor *monitor = glfwGetWindowMonitor(window);
    if (fullscreen) {
        if (monitor == nullptr) {
            monitor = glfwGetPrimaryMonitor();
            const GLFWvidmode *videomode = glfwGetVideoMode(monitor);
            int x, y, width, height;
            glfwGetWindowPos(window, &x, &y);
            ::x = x;
            ::y = y;
            glfwSetWindowMonitor(window, monitor, 0, 0, videomode->width, videomode->height, videomode->refreshRate);
        }
    } else {
        if (monitor != nullptr) {
            const GLFWvidmode *videomode = glfwGetVideoMode(monitor);
            glfwSetWindowMonitor(window, nullptr, x, y, videomode->width, videomode->height, videomode->refreshRate);
        }
    }
}

JNIEXPORT jboolean JNICALL Java_flat_backend_WL_IsFullscreen(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    return glfwGetWindowMonitor(window) != nullptr;
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetResizable(JNIEnv * jEnv, jclass jClass, jboolean resizable) {
    glfwSetWindowAttrib(window, GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE);
}

JNIEXPORT jboolean JNICALL Java_flat_backend_WL_IsResizable(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    return glfwGetWindowAttrib(window, GLFW_RESIZABLE) == GLFW_TRUE;
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetDecorated(JNIEnv * jEnv, jclass jClass, jboolean decorated) {
    glfwSetWindowAttrib(window, GLFW_DECORATED, decorated ? GLFW_TRUE : GLFW_FALSE);
}

JNIEXPORT jboolean JNICALL Java_flat_backend_WL_IsDecorated(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    return glfwGetWindowAttrib(window, GLFW_DECORATED) == GLFW_TRUE;
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetTitle(JNIEnv * jEnv, jclass jClass, jstring title) {
    sjEnv = jEnv;

    jboolean isCopy;
    const char *sTitle = jEnv->GetStringUTFChars(title, &isCopy);
    ::title = sTitle;
    glfwSetWindowTitle(window, ::title.c_str());
    jEnv->ReleaseStringUTFChars(title, sTitle);
}

JNIEXPORT jstring JNICALL Java_flat_backend_WL_GetTitle(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    jstring jstr = jEnv->NewStringUTF(::title.c_str());
    localRefGuard guard(jstr);
    return jstr;
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetIcon(JNIEnv * jEnv, jclass jClass, jbyteArray image, jint width, jint height) {
    sjEnv = jEnv;

    void* pointer = jEnv->GetPrimitiveArrayCritical(image, 0);
    GLFWimage img{width, height, (unsigned char *) pointer};
    glfwSetWindowIcon(window, 1, &img);
    jEnv->ReleasePrimitiveArrayCritical(image, pointer, 0);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetPosition(JNIEnv * jEnv, jclass jClass, jint x, jint y) {
    sjEnv = jEnv;

    glfwSetWindowPos(window, x, y);
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetX(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    int x, y;
    glfwGetWindowPos(window, &x, &y);
    return x;
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetY(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    int x, y;
    glfwGetWindowPos(window, &x, &y);
    return y;
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetSize(JNIEnv * jEnv, jclass jClass, jint width, jint height) {
    sjEnv = jEnv;

    glfwSetWindowSize(window, width, height);
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetWidth(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    int w, h;
    glfwGetWindowSize(window, &w, &h);
    return w;
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetHeight(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    int w, h;
    glfwGetWindowSize(window, &w, &h);
    return h;
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetClientWidth(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    int w, h;
    glfwGetFramebufferSize(window, &w, &h);
    return w;
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetClientHeight(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    int w, h;
    glfwGetFramebufferSize(window, &w, &h);
    return h;
}

JNIEXPORT jdouble JNICALL Java_flat_backend_WL_GetPhysicalWidth(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    int pw, ph, ww, wh;
    GLFWmonitor* monitor = findMonitor();
    const GLFWvidmode* mode = glfwGetVideoMode(monitor);
    glfwGetMonitorPhysicalSize(monitor, &pw, &ph);
    glfwGetWindowSize(window, &ww, &wh);

    return (ww / (double) mode->width) * pw;
}

JNIEXPORT jdouble JNICALL Java_flat_backend_WL_GetPhysicalHeight(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    int pw, ph, ww, wh;
    GLFWmonitor* monitor = findMonitor();
    const GLFWvidmode* mode = glfwGetVideoMode(monitor);
    glfwGetMonitorPhysicalSize(monitor, &pw, &ph);
    glfwGetWindowSize(window, &ww, &wh);

    return (wh / (double) mode->height) * ph;
}

JNIEXPORT jdouble JNICALL Java_flat_backend_WL_GetDpi(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    GLFWmonitor* monitor = findMonitor();
    const GLFWvidmode* mode = glfwGetVideoMode(monitor);
    int w, h;
    glfwGetMonitorPhysicalSize(monitor, &w, &h);
    return mode->width / (w / 25.4);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetSizeLimits(JNIEnv * jEnv, jclass jClass, jint minWidth, jint minHeight, jint maxWidth, jint maxHeight) {
    sjEnv = jEnv;

    glfwSetWindowSizeLimits(window, minw = minWidth, minh = minHeight, minw = maxWidth, minh = maxHeight);
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetMinWidth(JNIEnv * jEnv, jclass jClass) {
    return ::minw;
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetMinHeight(JNIEnv * jEnv, jclass jClass) {
    return ::minh;
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetMaxWidth(JNIEnv * jEnv, jclass jClass) {
    return ::minw;
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetMaxHeight(JNIEnv * jEnv, jclass jClass) {
    return ::minh;
}

JNIEXPORT void JNICALL Java_flat_backend_WL_Show(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    glfwShowWindow(window);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_Hide(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    glfwHideWindow(window);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_Close(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    glfwSetWindowShouldClose(window, true);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_Maximize(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    glfwMaximizeWindow(window);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_Minimize(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    glfwIconifyWindow(window);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_Restore(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    glfwRestoreWindow(window);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_Focus(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    glfwFocusWindow(window);
}

JNIEXPORT jboolean JNICALL Java_flat_backend_WL_IsShown(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    return glfwGetWindowAttrib(window, GLFW_VISIBLE) == GLFW_TRUE;
}

JNIEXPORT jboolean JNICALL Java_flat_backend_WL_IsClosed(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    return glfwWindowShouldClose(window);
}

JNIEXPORT jboolean JNICALL Java_flat_backend_WL_IsMaximized(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    return glfwGetWindowAttrib(window, GLFW_MAXIMIZED) == GLFW_TRUE;
}

JNIEXPORT jboolean JNICALL Java_flat_backend_WL_IsMinimized(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    return glfwGetWindowAttrib(window, GLFW_ICONIFIED) == GLFW_TRUE;
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetInputMode(JNIEnv * jEnv, jclass jClass, jint mode) {
    sjEnv = jEnv;

    return glfwGetInputMode(window, mode);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetInputMode(JNIEnv * jEnv, jclass jClass, jint mode, jint value) {
    sjEnv = jEnv;

    glfwSetInputMode(window, mode, value);
}

JNIEXPORT jstring JNICALL Java_flat_backend_WL_GetKeyName(JNIEnv * jEnv, jclass jClass, jint key, jint scancode) {
    sjEnv = jEnv;

    jstring jstr = jEnv->NewStringUTF(glfwGetKeyName(key, scancode));
    localRefGuard guard(jstr);
    return jstr;
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetKey(JNIEnv * jEnv, jclass jClass, jint key) {
    sjEnv = jEnv;

    return glfwGetKey(window, key);
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetMouseButton(JNIEnv * jEnv, jclass jClass, jint button) {
    sjEnv = jEnv;

    return glfwGetMouseButton(window, button);
}

JNIEXPORT jdouble JNICALL Java_flat_backend_WL_GetCursorX(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    double x, y;
    glfwGetCursorPos(window, &x, &y);
    return x;
}

JNIEXPORT jdouble JNICALL Java_flat_backend_WL_GetCursorY(JNIEnv * jEnv, jclass jClass) {
    sjEnv = jEnv;

    double x, y;
    glfwGetCursorPos(window, &x, &y);
    return y;
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetCursorPos(JNIEnv * jEnv, jclass jClass, jdouble xpos, jdouble ypos) {
    sjEnv = jEnv;

    glfwSetCursorPos(window, xpos, ypos);
}

JNIEXPORT jlong JNICALL Java_flat_backend_WL_CreateCursor(JNIEnv * jEnv, jclass jClass, jbyteArray image, jint width, jint height, jint xhot, jint yhot) {
    sjEnv = jEnv;

    void* pointer = jEnv->GetPrimitiveArrayCritical(image, 0);
    GLFWimage img{width, height, (unsigned char *) pointer};
    jlong cursorId = (jlong) glfwCreateCursor(&img, xhot, yhot);
    jEnv->ReleasePrimitiveArrayCritical(image, pointer, 0);
    return cursorId;
}

JNIEXPORT jlong JNICALL Java_flat_backend_WL_CreateStandardCursor(JNIEnv * jEnv, jclass jClass, jint shape) {
    sjEnv = jEnv;

    return (jlong) glfwCreateStandardCursor(shape);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_DestroyCursor(JNIEnv * jEnv, jclass jClass, jlong cursor) {
    sjEnv = jEnv;

    glfwDestroyCursor((GLFWcursor *) cursor);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetCursor(JNIEnv * jEnv, jclass jClass, jlong cursor) {
    sjEnv = jEnv;

    glfwSetCursor(window, (GLFWcursor *) cursor);
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_JoystickPresent(JNIEnv * jEnv, jclass jClass, jint joy) {
    sjEnv = jEnv;

    return glfwJoystickPresent(joy);
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetJoystickAxesCount(JNIEnv * jEnv, jclass jClass, jint joy) {
    sjEnv = jEnv;

    int count;
    glfwGetJoystickAxes(joy, &count);
    return count;
}

JNIEXPORT void JNICALL Java_flat_backend_WL_GetJoystickAxes(JNIEnv * jEnv, jclass jClass, jint joy, jfloatArray axes) {
    sjEnv = jEnv;

    int count;
    const float *iaxes = glfwGetJoystickAxes(joy, &count);
    jEnv->SetFloatArrayRegion(axes, 0, count, iaxes);
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetJoystickButtonsCount(JNIEnv * jEnv, jclass jClass, jint joy) {
    sjEnv = jEnv;

    int count;
    glfwGetJoystickButtons(joy, &count);
    return count;
}

JNIEXPORT void JNICALL Java_flat_backend_WL_GetJoystickButtons(JNIEnv * jEnv, jclass jClass, jint joy, jintArray buttons) {
    sjEnv = jEnv;

    int count;
    const unsigned char *ibtns = glfwGetJoystickButtons(joy, &count);
    for (int i = 0; i < count; i++) {
        jint btn = ibtns[i];
        jEnv->SetIntArrayRegion(buttons, i, 1, &btn);
    }
}

JNIEXPORT jstring JNICALL Java_flat_backend_WL_GetJoystickName(JNIEnv * jEnv, jclass jClass, jint joy) {
    sjEnv = jEnv;

    jstring jstr = jEnv->NewStringUTF(glfwGetJoystickName(joy));
    localRefGuard guard(jstr);
    return jstr;
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetWindowPosCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    sjEnv = jEnv;

    if (callback == nullptr) {
        sWindowPosCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(II)V");
        sWindowPosCallback = jLambda<void(jint,jint)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetWindowSizeCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    sjEnv = jEnv;

    if (callback == nullptr) {
        sWindowSizeCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(II)V");
        sWindowSizeCallback = jLambda<void(jint,jint)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetWindowCloseCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    sjEnv = jEnv;

    if (callback == nullptr) {
        sWindowCloseCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "()Z");
        sWindowCloseCallback = jLambda<bool()>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetWindowRefreshCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    sjEnv = jEnv;

    if (callback == nullptr) {
        sWindowRefreshCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "()V");
        sWindowRefreshCallback = jLambda<void()>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetWindowFocusCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    sjEnv = jEnv;

    if (callback == nullptr) {
        sWindowFocusCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(I)V");
        sWindowFocusCallback = jLambda<void(jint)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetWindowIconifyCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    sjEnv = jEnv;

    if (callback == nullptr) {
        sWindowIconifyCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(I)V");
        sWindowIconifyCallback = jLambda<void(jint)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetFramebufferSizeCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    sjEnv = jEnv;

    if (callback == nullptr) {
       sFramebufferSizeCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(II)V");
        sFramebufferSizeCallback = jLambda<void(jint,jint)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetKeyCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    sjEnv = jEnv;

    if (callback == nullptr) {
        sKeyCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(IIII)V");
        sKeyCallback = jLambda<void(jint, jint, jint, jint)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetCharCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    sjEnv = jEnv;

    if (callback == nullptr) {
        sCharCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(I)V");
        sCharCallback = jLambda<void(jint)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetCharModsCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    sjEnv = jEnv;

    if (callback == nullptr) {
        sCharModsCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(II)V");
        sCharModsCallback = jLambda<void(jint,jint)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetMouseButtonCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    sjEnv = jEnv;

    if (callback == nullptr) {
        sMouseButtonCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(III)V");
        sMouseButtonCallback = jLambda<void(jint,jint,jint)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetCursorPosCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    sjEnv = jEnv;

    if (callback == nullptr) {
        sCursorPosCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(DD)V");
        sCursorPosCallback = jLambda<void(jdouble,jdouble)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetCursorEnterCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    sjEnv = jEnv;

    if (callback == nullptr) {
        sCursorEnterCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(I)V");
        sCursorEnterCallback = jLambda<void(jint)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetScrollCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    sjEnv = jEnv;

    if (callback == nullptr) {
        sScrollCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(DD)V");
        sScrollCallback = jLambda<void(jdouble, jdouble)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetDropCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    sjEnv = jEnv;

    if (callback == nullptr) {
        sDropCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "([Ljava/lang/String;)V");
        sDropCallback = jLambda<void(jobjectArray)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetJoystickCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    sjEnv = jEnv;

    if (callback == nullptr) {
        sJoystickCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(II)V");
        sJoystickCallback = jLambda<void(jint,jint)>(callback, mid);
    }
}