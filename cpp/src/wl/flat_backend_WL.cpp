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

static JavaVM *jvm;
static bool loadGlad = false;

JNIEnv* getJNIEnv() {
    JNIEnv* env;
    int getEnvStat = jvm->GetEnv((void **)&env, JNI_VERSION_9);
    if (getEnvStat == JNI_EDETACHED) {
        jvm->AttachCurrentThread((void **) &env, NULL);
    }
    return env;
}

template<class T>
class jLambda;

template<class R, class ...Args> class jLambda<R(Args...)> {
public:
    jobject obj;
    jmethodID method;

    jLambda() : obj(nullptr), method (nullptr) {

    }

    jLambda(jobject jobj, jmethodID jmth) : obj(nullptr), method (nullptr) {
        set(jobj, jmth);
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
        getJNIEnv()->CallVoidMethod(obj, method, args...);
    }

    R bRun(Args... args) {
        return getJNIEnv()->CallBooleanMethod(obj, method, args...);
    }

    void set(jobject jobj, jmethodID jmth) {
        JNIEnv* env = getJNIEnv();

        if (obj != nullptr) {
            env->DeleteGlobalRef(obj);
        }
        if (jobj != nullptr) {
            obj = env->NewGlobalRef(jobj);
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

static jLambda<void(jlong, jint, jint)> sWindowPosCallback;
static jLambda<void(jlong, jint, jint)> sWindowSizeCallback;
static jLambda<bool(jlong)> sWindowCloseCallback;
static jLambda<void(jlong)> sWindowRefreshCallback;
static jLambda<void(jlong, jint)> sWindowFocusCallback;
static jLambda<void(jlong, jint)> sWindowIconifyCallback;
static jLambda<void(jlong, jint, jint)> sFramebufferSizeCallback;
static jLambda<void(jlong, jint, jint, jint, jint)> sKeyCallback;
static jLambda<void(jlong, jint)> sCharCallback;
static jLambda<void(jlong, jint, jint)> sCharModsCallback;
static jLambda<void(jlong, jint, jint, jint)> sMouseButtonCallback;
static jLambda<void(jlong, jdouble, jdouble)> sCursorPosCallback;
static jLambda<void(jlong, jint)> sCursorEnterCallback;
static jLambda<void(jlong, jdouble, jdouble)> sScrollCallback;
static jLambda<void(jlong, jobjectArray)> sDropCallback;
static jLambda<void(jlong, jint, jint)> sJoystickCallback;
static jLambda<void(jstring)> sErrorCallback;

namespace {
    void WindowPosCallback(GLFWwindow *a, int b, int c) {
        if (sWindowPosCallback) sWindowPosCallback.run((jlong) a, b, c);
    }

    void WindowSizeCallback(GLFWwindow *a, int b, int c) {
        if (sWindowSizeCallback) sWindowSizeCallback.run((jlong) a, b, c);
    }

    void WindowCloseCallback(GLFWwindow *a) {
        if (sWindowCloseCallback) {
            if (sWindowCloseCallback.bRun((jlong) a)) {
                glfwSetWindowShouldClose(a, true);
                glfwHideWindow(a);
            } else {
                glfwSetWindowShouldClose(a, false);
            }
        } else {
            glfwSetWindowShouldClose(a, true);
            glfwHideWindow(a);
        }
    }

    void WindowRefreshCallback(GLFWwindow *a) {
        if (sWindowRefreshCallback) sWindowRefreshCallback.run((jlong) a);
    }

    void WindowFocusCallback(GLFWwindow *a, int b) {
        if (sWindowFocusCallback) sWindowFocusCallback.run((jlong) a, b);
    }

    void WindowIconifyCallback(GLFWwindow *a, int b) {
        if (sWindowIconifyCallback) sWindowIconifyCallback.run((jlong) a, b);
    }

    void FramebufferSizeCallback(GLFWwindow *a, int b, int c) {
        if (sFramebufferSizeCallback) sFramebufferSizeCallback.run((jlong) a, b, c);
    }

    void KeyCallback(GLFWwindow *a, int b, int c, int d, int e) {
        if (sKeyCallback) sKeyCallback.run((jlong) a, b, c, d, e);
    }

    void CharCallback(GLFWwindow *a, unsigned int b) {
        if (sCharCallback) sCharCallback.run((jlong) a, (jint)b);
    }

    void CharModsCallback(GLFWwindow *a, unsigned int b, int c) {
        if (sCharModsCallback) sCharModsCallback.run((jlong) a, (jint)b, c);
    }

    void MouseButtonCallback(GLFWwindow *a, int b, int c, int d) {
        if (sMouseButtonCallback) sMouseButtonCallback.run((jlong) a, b, c, d);
    }

    void CursorPosCallback(GLFWwindow *a, double b, double c) {
        if (sCursorPosCallback) sCursorPosCallback.run((jlong) a, b, c);
    }

    void CursorEnterCallback(GLFWwindow *a, int b) {
        if (sCursorEnterCallback) sCursorEnterCallback.run((jlong) a, b);
    }

    void ScrollCallback(GLFWwindow *a, double b, double c) {
        if (sScrollCallback) sScrollCallback.run((jlong) a, b, c);
    }

    void DropCallback(GLFWwindow *a, int b, const char **c) {
        if (sDropCallback) {
            JNIEnv* env = getJNIEnv();
            jobjectArray arr = env->NewObjectArray(b, env->FindClass("java/lang/String"), nullptr);
            for (int i = 0; i < b; i++) {
                jstring str = env->NewStringUTF(c[i]);
                env->SetObjectArrayElement(arr, i, str);
                env->DeleteLocalRef(str);
            }
            sDropCallback.run((jlong) a, arr);
            env->DeleteLocalRef(arr);
        }
    }

    void JoystickCallback(int a, int b) {
        if (sJoystickCallback) sJoystickCallback.run((jlong) a, a, b);
    }

    void ErrorCallback(int a, const char* description) {
        if (sErrorCallback)sErrorCallback.run(getJNIEnv()->NewStringUTF(description));
    }
}

GLFWmonitor * findMonitor(GLFWwindow* window) {
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

JNIEXPORT jlong JNICALL Java_flat_backend_WL_Init(JNIEnv * jEnv, jclass) {
    jEnv->GetJavaVM(&jvm);
    loadGlad = false;
    return glfwInit();
}

JNIEXPORT void JNICALL Java_flat_backend_WL_Finish(JNIEnv * jEnv, jclass jClass) {
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
    sErrorCallback = nullptr;
    glfwTerminate();
}

JNIEXPORT jlong JNICALL Java_flat_backend_WL_WindowCreate(JNIEnv * jEnv, jclass, jint width, jint height, jint samples, jboolean transparent) {
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
    glfwWindowHint(GLFW_SAMPLES, samples);
    glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, transparent ? GLFW_TRUE : GLFW_FALSE);
    GLFWwindow *window = glfwCreateWindow(width, height, "", nullptr, nullptr);
    if (window == nullptr) {
        return 0;
    }

    glfwMakeContextCurrent(window);

    if (!loadGlad && !gladLoadGLLoader((GLADloadproc) glfwGetProcAddress)) {
        glfwDestroyWindow(window);
        return 0;
    }

    loadGlad = true;

    glfwSetInputMode(window, GLFW_STICKY_KEYS, 1);
    glfwSetInputMode(window, GLFW_STICKY_MOUSE_BUTTONS, 1);

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
    glfwSetErrorCallback(ErrorCallback);
    return (jlong) window;
}

JNIEXPORT void JNICALL Java_flat_backend_WL_WindowAssign(JNIEnv * jEnv, jclass jClass, jlong win) {
    glfwMakeContextCurrent((GLFWwindow*) win);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_WindowDestroy(JNIEnv * jEnv, jclass jClass, jlong win) {
    glfwDestroyWindow((GLFWwindow*) win);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SwapBuffers(JNIEnv * jEnv, jclass jClass, jlong win) {
    glfwSwapBuffers((GLFWwindow *) win);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_HandleEvents(JNIEnv * jEnv, jclass jClass, jdouble wait) {
    if (wait > 0) {
        glfwWaitEventsTimeout(wait);
    } else {
        glfwPollEvents();
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetVsync(JNIEnv * jEnv, jclass jClass, jint vsync) {
    glfwSwapInterval(vsync);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_PostEmptyEvent(JNIEnv * jEnv, jclass jClass) {
    glfwPostEmptyEvent();
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetFullscreen(JNIEnv * jEnv, jclass jClass, jlong win, jboolean fullscreen) {
    GLFWwindow* window = (GLFWwindow*) win;
    GLFWmonitor *monitor = glfwGetWindowMonitor(window);
    if (fullscreen) {
        if (monitor == nullptr) {
            monitor = glfwGetPrimaryMonitor();
            const GLFWvidmode *videomode = glfwGetVideoMode(monitor);
            glfwSetWindowMonitor(window, monitor, 0, 0, videomode->width, videomode->height, videomode->refreshRate);
        }
    } else {
        if (monitor != nullptr) {
            const GLFWvidmode *videomode = glfwGetVideoMode(monitor);
            glfwSetWindowMonitor(window, nullptr, 0, 0, videomode->width, videomode->height, videomode->refreshRate);
        }
    }
}

JNIEXPORT jboolean JNICALL Java_flat_backend_WL_IsFullscreen(JNIEnv * jEnv, jclass jClass, jlong win) {
    return glfwGetWindowMonitor((GLFWwindow*) win) != nullptr;
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetResizable(JNIEnv * jEnv, jclass jClass, jlong win, jboolean resizable) {
    glfwSetWindowAttrib((GLFWwindow*) win, GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE);
}

JNIEXPORT jboolean JNICALL Java_flat_backend_WL_IsResizable(JNIEnv * jEnv, jclass jClass, jlong win) {
    return glfwGetWindowAttrib((GLFWwindow*) win, GLFW_RESIZABLE) == GLFW_TRUE;
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetDecorated(JNIEnv * jEnv, jclass jClass, jlong win, jboolean decorated) {
    glfwSetWindowAttrib((GLFWwindow*) win, GLFW_DECORATED, decorated ? GLFW_TRUE : GLFW_FALSE);
}

JNIEXPORT jboolean JNICALL Java_flat_backend_WL_IsDecorated(JNIEnv * jEnv, jclass jClass, jlong win) {
    return glfwGetWindowAttrib((GLFWwindow*) win, GLFW_DECORATED) == GLFW_TRUE;
}

JNIEXPORT jboolean JNICALL Java_flat_backend_WL_IsTransparent(JNIEnv * jEnv, jclass jClass, jlong win) {
    return glfwGetWindowAttrib((GLFWwindow*) win, GLFW_TRANSPARENT_FRAMEBUFFER) == GLFW_TRUE;
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetTitle(JNIEnv * jEnv, jclass jClass, jlong win, jstring title) {
    jboolean isCopy;
    const char *sTitle = jEnv->GetStringUTFChars(title, &isCopy);
    glfwSetWindowTitle((GLFWwindow*) win, sTitle);
    jEnv->ReleaseStringUTFChars(title, sTitle);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetIcon(JNIEnv * jEnv, jclass jClass, jlong win, jbyteArray image, jint width, jint height) {
    void* pointer = jEnv->GetPrimitiveArrayCritical(image, 0);
    GLFWimage img{width, height, (unsigned char *) pointer};
    glfwSetWindowIcon((GLFWwindow*) win, 1, &img);
    jEnv->ReleasePrimitiveArrayCritical(image, pointer, 0);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetPosition(JNIEnv * jEnv, jclass jClass, jlong win, jint x, jint y) {
    glfwSetWindowPos((GLFWwindow*) win, x, y);
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetX(JNIEnv * jEnv, jclass jClass, jlong win) {
    int x, y;
    glfwGetWindowPos((GLFWwindow*) win, &x, &y);
    return x;
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetY(JNIEnv * jEnv, jclass jClass, jlong win) {
    int x, y;
    glfwGetWindowPos((GLFWwindow*) win, &x, &y);
    return y;
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetSize(JNIEnv * jEnv, jclass jClass, jlong win, jint width, jint height) {
    glfwSetWindowSize((GLFWwindow*) win, width, height);
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetWidth(JNIEnv * jEnv, jclass jClass, jlong win) {
    int w, h;
    glfwGetWindowSize((GLFWwindow*) win, &w, &h);
    return w;
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetHeight(JNIEnv * jEnv, jclass jClass, jlong win) {
    int w, h;
    glfwGetWindowSize((GLFWwindow*) win, &w, &h);
    return h;
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetClientWidth(JNIEnv * jEnv, jclass jClass, jlong win) {
    int w, h;
    glfwGetFramebufferSize((GLFWwindow*) win, &w, &h);
    return w;
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetClientHeight(JNIEnv * jEnv, jclass jClass, jlong win) {
    int w, h;
    glfwGetFramebufferSize((GLFWwindow*) win, &w, &h);
    return h;
}

JNIEXPORT jdouble JNICALL Java_flat_backend_WL_GetPhysicalWidth(JNIEnv * jEnv, jclass jClass, jlong win) {
    GLFWwindow* window = (GLFWwindow*) win;

    int pw, ph, ww, wh;
    GLFWmonitor* monitor = findMonitor(window);
    const GLFWvidmode* mode = glfwGetVideoMode(monitor);
    glfwGetMonitorPhysicalSize(monitor, &pw, &ph);
    glfwGetWindowSize(window, &ww, &wh);

    return (ww / (double) mode->width) * pw;
}

JNIEXPORT jdouble JNICALL Java_flat_backend_WL_GetPhysicalHeight(JNIEnv * jEnv, jclass jClass, jlong win) {
    GLFWwindow* window = (GLFWwindow*) win;

    int pw, ph, ww, wh;
    GLFWmonitor* monitor = findMonitor(window);
    const GLFWvidmode* mode = glfwGetVideoMode(monitor);
    glfwGetMonitorPhysicalSize(monitor, &pw, &ph);
    glfwGetWindowSize(window, &ww, &wh);

    return (wh / (double) mode->height) * ph;
}

JNIEXPORT jdouble JNICALL Java_flat_backend_WL_GetDpi(JNIEnv * jEnv, jclass jClass, jlong win) {
    GLFWwindow* window = (GLFWwindow*) win;

    GLFWmonitor* monitor = findMonitor(window);
    const GLFWvidmode* mode = glfwGetVideoMode(monitor);
    int w, h;
    glfwGetMonitorPhysicalSize(monitor, &w, &h);
    return mode->width / (w / 25.4);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetSizeLimits(JNIEnv * jEnv, jclass jClass, jlong win, jint minWidth, jint minHeight, jint maxWidth, jint maxHeight) {
    glfwSetWindowSizeLimits((GLFWwindow*) win, minWidth, minHeight, maxWidth, maxHeight);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_Show(JNIEnv * jEnv, jclass jClass, jlong win) {
    glfwShowWindow((GLFWwindow*) win);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_Hide(JNIEnv * jEnv, jclass jClass, jlong win) {
    glfwHideWindow((GLFWwindow*) win);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_Close(JNIEnv * jEnv, jclass jClass, jlong win) {
    glfwSetWindowShouldClose((GLFWwindow*) win, true);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_Maximize(JNIEnv * jEnv, jclass jClass, jlong win) {
    glfwMaximizeWindow((GLFWwindow*) win);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_Minimize(JNIEnv * jEnv, jclass jClass, jlong win) {
    glfwIconifyWindow((GLFWwindow*) win);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_Restore(JNIEnv * jEnv, jclass jClass, jlong win) {
    glfwRestoreWindow((GLFWwindow*) win);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_Focus(JNIEnv * jEnv, jclass jClass, jlong win) {
    glfwFocusWindow((GLFWwindow*) win);
}

JNIEXPORT jboolean JNICALL Java_flat_backend_WL_IsShown(JNIEnv * jEnv, jclass jClass, jlong win) {
    return glfwGetWindowAttrib((GLFWwindow*) win, GLFW_VISIBLE) == GLFW_TRUE;
}

JNIEXPORT jboolean JNICALL Java_flat_backend_WL_IsClosed(JNIEnv * jEnv, jclass jClass, jlong win) {
    return glfwWindowShouldClose((GLFWwindow*) win);
}

JNIEXPORT jboolean JNICALL Java_flat_backend_WL_IsMaximized(JNIEnv * jEnv, jclass jClass, jlong win) {
    return glfwGetWindowAttrib((GLFWwindow*) win, GLFW_MAXIMIZED) == GLFW_TRUE;
}

JNIEXPORT jboolean JNICALL Java_flat_backend_WL_IsMinimized(JNIEnv * jEnv, jclass jClass, jlong win) {
    return glfwGetWindowAttrib((GLFWwindow*) win, GLFW_ICONIFIED) == GLFW_TRUE;
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetInputMode(JNIEnv * jEnv, jclass jClass, jlong win, jint mode) {
    return glfwGetInputMode((GLFWwindow*) win, mode);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetInputMode(JNIEnv * jEnv, jclass jClass, jlong win, jint mode, jint value) {
    glfwSetInputMode((GLFWwindow*) win, mode, value);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetClipboardString(JNIEnv * jEnv, jclass jClass, jlong win, jstring clipboard) {
    jboolean isCopy;
    const char *sClipboard = jEnv->GetStringUTFChars(clipboard, &isCopy);
    glfwSetClipboardString((GLFWwindow*) win, sClipboard);
    jEnv->ReleaseStringUTFChars(clipboard, sClipboard);
}

JNIEXPORT jstring JNICALL Java_flat_backend_WL_GetClipboardString(JNIEnv * jEnv, jclass jClass, jlong win) {
    const char* clipboard = glfwGetClipboardString((GLFWwindow*) win);
    if (clipboard == NULL) {
        return NULL;
    }
    return jEnv->NewStringUTF(clipboard);
}

JNIEXPORT jstring JNICALL Java_flat_backend_WL_GetKeyName(JNIEnv * jEnv, jclass jClass, jint key, jint scancode) {
    const char* name = glfwGetKeyName(key, scancode);
    if (name == NULL) {
        return NULL;
    }
    return jEnv->NewStringUTF(name);
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetKey(JNIEnv * jEnv, jclass jClass, jlong win, jint key) {
    return glfwGetKey((GLFWwindow*) win, key);
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetMouseButton(JNIEnv * jEnv, jclass jClass, jlong win, jint button) {
    return glfwGetMouseButton((GLFWwindow*) win, button);
}

JNIEXPORT jdouble JNICALL Java_flat_backend_WL_GetCursorX(JNIEnv * jEnv, jclass jClass, jlong win) {
    double x, y;
    glfwGetCursorPos((GLFWwindow*) win, &x, &y);
    return x;
}

JNIEXPORT jdouble JNICALL Java_flat_backend_WL_GetCursorY(JNIEnv * jEnv, jclass jClass, jlong win) {
    double x, y;
    glfwGetCursorPos((GLFWwindow*) win, &x, &y);
    return y;
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetCursorPos(JNIEnv * jEnv, jclass jClass, jlong win, jdouble xpos, jdouble ypos) {
    glfwSetCursorPos((GLFWwindow*) win, xpos, ypos);
}

JNIEXPORT jlong JNICALL Java_flat_backend_WL_CreateCursor(JNIEnv * jEnv, jclass jClass, jbyteArray image, jint width, jint height, jint xhot, jint yhot) {
    void* pointer = jEnv->GetPrimitiveArrayCritical(image, 0);
    GLFWimage img{width, height, (unsigned char *) pointer};
    jlong cursorId = (jlong) glfwCreateCursor(&img, xhot, yhot);
    jEnv->ReleasePrimitiveArrayCritical(image, pointer, 0);
    return cursorId;
}

JNIEXPORT jlong JNICALL Java_flat_backend_WL_CreateStandardCursor(JNIEnv * jEnv, jclass jClass, jint shape) {
    return (jlong) glfwCreateStandardCursor(shape);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_DestroyCursor(JNIEnv * jEnv, jclass jClass, jlong cursor) {
    glfwDestroyCursor((GLFWcursor *) cursor);
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetCursor(JNIEnv * jEnv, jclass jClass, jlong win, jlong cursor) {
    glfwSetCursor((GLFWwindow*) win, (GLFWcursor *) cursor);
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_JoystickPresent(JNIEnv * jEnv, jclass jClass, jint joy) {
    return glfwJoystickPresent(joy);
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetJoystickAxesCount(JNIEnv * jEnv, jclass jClass, jint joy) {
    int count;
    glfwGetJoystickAxes(joy, &count);
    return count;
}

JNIEXPORT void JNICALL Java_flat_backend_WL_GetJoystickAxes(JNIEnv * jEnv, jclass jClass, jint joy, jfloatArray axes) {
    int count;
    const float *iaxes = glfwGetJoystickAxes(joy, &count);
    jEnv->SetFloatArrayRegion(axes, 0, count, iaxes);
}

JNIEXPORT jint JNICALL Java_flat_backend_WL_GetJoystickButtonsCount(JNIEnv * jEnv, jclass jClass, jint joy) {
    int count;
    glfwGetJoystickButtons(joy, &count);
    return count;
}

JNIEXPORT void JNICALL Java_flat_backend_WL_GetJoystickButtons(JNIEnv * jEnv, jclass jClass, jint joy, jintArray buttons) {
    int count;
    const unsigned char *ibtns = glfwGetJoystickButtons(joy, &count);
    for (int i = 0; i < count; i++) {
        jint btn = ibtns[i];
        jEnv->SetIntArrayRegion(buttons, i, 1, &btn);
    }
}

JNIEXPORT jstring JNICALL Java_flat_backend_WL_GetJoystickName(JNIEnv * jEnv, jclass jClass, jint joy) {
    return jEnv->NewStringUTF(glfwGetJoystickName(joy));
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetWindowPosCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    if (callback == nullptr) {
        sWindowPosCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(II)V");
        sWindowPosCallback = jLambda<void(jlong,jint,jint)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetWindowSizeCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    if (callback == nullptr) {
        sWindowSizeCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(JII)V");
        sWindowSizeCallback = jLambda<void(jlong,jint,jint)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetWindowCloseCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    if (callback == nullptr) {
        sWindowCloseCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(J)Z");
        sWindowCloseCallback = jLambda<bool(jlong)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetWindowRefreshCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    if (callback == nullptr) {
        sWindowRefreshCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(J)V");
        sWindowRefreshCallback = jLambda<void(jlong)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetWindowFocusCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    if (callback == nullptr) {
        sWindowFocusCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(JI)V");
        sWindowFocusCallback = jLambda<void(jlong,jint)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetWindowIconifyCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    if (callback == nullptr) {
        sWindowIconifyCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(JI)V");
        sWindowIconifyCallback = jLambda<void(jlong,jint)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetFramebufferSizeCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    if (callback == nullptr) {
       sFramebufferSizeCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(JII)V");
        sFramebufferSizeCallback = jLambda<void(jlong,jint,jint)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetKeyCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    if (callback == nullptr) {
        sKeyCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(JIIII)V");
        sKeyCallback = jLambda<void(jlong,jint, jint, jint, jint)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetCharCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    if (callback == nullptr) {
        sCharCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(JI)V");
        sCharCallback = jLambda<void(jlong,jint)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetCharModsCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    if (callback == nullptr) {
        sCharModsCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(JII)V");
        sCharModsCallback = jLambda<void(jlong,jint,jint)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetMouseButtonCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    if (callback == nullptr) {
        sMouseButtonCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(JIII)V");
        sMouseButtonCallback = jLambda<void(jlong,jint,jint,jint)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetCursorPosCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    if (callback == nullptr) {
        sCursorPosCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(JDD)V");
        sCursorPosCallback = jLambda<void(jlong,jdouble,jdouble)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetCursorEnterCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    if (callback == nullptr) {
        sCursorEnterCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(JI)V");
        sCursorEnterCallback = jLambda<void(jlong,jint)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetScrollCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    if (callback == nullptr) {
        sScrollCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(JDD)V");
        sScrollCallback = jLambda<void(jlong,jdouble, jdouble)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetDropCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    if (callback == nullptr) {
        sDropCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(J[Ljava/lang/String;)V");
        sDropCallback = jLambda<void(jlong,jobjectArray)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetJoystickCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    if (callback == nullptr) {
        sJoystickCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(JII)V");
        sJoystickCallback = jLambda<void(jlong,jint,jint)>(callback, mid);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_WL_SetErrorCallback(JNIEnv * jEnv, jclass jClass, jobject callback) {
    if (callback == nullptr) {
        sErrorCallback = nullptr;
    } else {
        jclass cls = jEnv->GetObjectClass(callback);
        jmethodID mid = jEnv->GetMethodID(cls, "handle", "(Ljava/lang/String;)V");
        sErrorCallback = jLambda<void(jstring)>(callback, mid);
    }
}