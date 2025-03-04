/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class flat_backend_WL */

#ifndef _Included_flat_backend_WL
#define _Included_flat_backend_WL
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     flat_backend_WL
 * Method:    Init
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_flat_backend_WL_Init
  (JNIEnv *, jclass);

/*
 * Class:     flat_backend_WL
 * Method:    Finish
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_Finish
  (JNIEnv *, jclass);

/*
 * Class:     flat_backend_WL
 * Method:    WindowCreate
 * Signature: (IIIZ)J
 */
JNIEXPORT jlong JNICALL Java_flat_backend_WL_WindowCreate
        (JNIEnv *, jclass, jint, jint, jint, jboolean);

/*
 * Class:     flat_backend_WL
 * Method:    WindowAssign
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_WindowAssign
        (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    WindowDestroy
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_WindowDestroy
        (JNIEnv *, jclass, jlong);


/*
 * Class:     flat_backend_WL
 * Method:    SwapBuffers
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SwapBuffers
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    HandleEvents
 * Signature: (D)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_HandleEvents
  (JNIEnv *, jclass, jdouble);

/*
 * Class:     flat_backend_WL
 * Method:    SetVsync
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetVsync
  (JNIEnv *, jclass, jint);

/*
 * Class:     flat_backend_WL
 * Method:    PostEmptyEvent
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_PostEmptyEvent
        (JNIEnv *, jclass);

/*
 * Class:     flat_backend_WL
 * Method:    SetFullscreen
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetFullscreen
  (JNIEnv *, jclass, jlong, jboolean);

/*
 * Class:     flat_backend_WL
 * Method:    IsFullscreen
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_flat_backend_WL_IsFullscreen
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    SetResizable
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetResizable
  (JNIEnv *, jclass, jlong, jboolean);

/*
 * Class:     flat_backend_WL
 * Method:    IsResizable
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_flat_backend_WL_IsResizable
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    SetDecorated
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetDecorated
  (JNIEnv *, jclass, jlong, jboolean);

/*
 * Class:     flat_backend_WL
 * Method:    IsDecorated
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_flat_backend_WL_IsDecorated
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    IsTransparent
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_flat_backend_WL_IsTransparent
        (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    SetTitle
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetTitle
  (JNIEnv *, jclass, jlong, jstring);

/*
 * Class:     flat_backend_WL
 * Method:    SetIcon
 * Signature: (J[BII)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetIcon
  (JNIEnv *, jclass, jlong, jbyteArray, jint, jint);

/*
 * Class:     flat_backend_WL
 * Method:    SetPosition
 * Signature: (JII)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetPosition
  (JNIEnv *, jclass, jlong, jint, jint);

/*
 * Class:     flat_backend_WL
 * Method:    GetX
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_flat_backend_WL_GetX
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    GetY
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_flat_backend_WL_GetY
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    SetSize
 * Signature: (JII)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetSize
  (JNIEnv *, jclass, jlong, jint, jint);

/*
 * Class:     flat_backend_WL
 * Method:    GetWidth
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_flat_backend_WL_GetWidth
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    GetHeight
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_flat_backend_WL_GetHeight
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    GetClientWidth
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_flat_backend_WL_GetClientWidth
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    GetClientHeight
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_flat_backend_WL_GetClientHeight
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    GetPhysicalWidth
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_flat_backend_WL_GetPhysicalWidth
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    GetPhysicalHeight
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_flat_backend_WL_GetPhysicalHeight
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    GetDpi
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_flat_backend_WL_GetDpi
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    SetSizeLimits
 * Signature: (JIIII)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetSizeLimits
  (JNIEnv *, jclass, jlong, jint, jint, jint, jint);

/*
 * Class:     flat_backend_WL
 * Method:    Show
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_Show
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    Hide
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_Hide
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    Close
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_Close
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    Maximize
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_Maximize
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    Minimize
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_Minimize
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    Restore
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_Restore
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    Focus
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_Focus
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    IsShown
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_flat_backend_WL_IsShown
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    IsClosed
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_flat_backend_WL_IsClosed
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    IsMaximized
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_flat_backend_WL_IsMaximized
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    IsMinimized
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_flat_backend_WL_IsMinimized
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    GetInputMode
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_flat_backend_WL_GetInputMode
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     flat_backend_WL
 * Method:    SetInputMode
 * Signature: (JII)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetInputMode
  (JNIEnv *, jclass, jlong, jint, jint);

/*
 * Class:     flat_backend_WL
 * Method:    SetClipboardString
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetClipboardString
  (JNIEnv *, jclass, jlong, jstring);

/*
 * Class:     flat_backend_WL
 * Method:    GetClipboardString
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_flat_backend_WL_GetClipboardString
        (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    GetKeyName
 * Signature: (II)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_flat_backend_WL_GetKeyName
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     flat_backend_WL
 * Method:    GetKey
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_flat_backend_WL_GetKey
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     flat_backend_WL
 * Method:    GetMouseButton
 * Signature: (JI)I
 */
JNIEXPORT jint JNICALL Java_flat_backend_WL_GetMouseButton
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     flat_backend_WL
 * Method:    GetCursorX
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_flat_backend_WL_GetCursorX
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    GetCursorY
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_flat_backend_WL_GetCursorY
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    SetCursorPos
 * Signature: (JDD)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetCursorPos
  (JNIEnv *, jclass, jlong, jdouble, jdouble);

/*
 * Class:     flat_backend_WL
 * Method:    CreateCursor
 * Signature: ([BIIII)J
 */
JNIEXPORT jlong JNICALL Java_flat_backend_WL_CreateCursor
  (JNIEnv *, jclass, jbyteArray, jint, jint, jint, jint);

/*
 * Class:     flat_backend_WL
 * Method:    CreateStandardCursor
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_flat_backend_WL_CreateStandardCursor
  (JNIEnv *, jclass, jint);

/*
 * Class:     flat_backend_WL
 * Method:    DestroyCursor
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_DestroyCursor
  (JNIEnv *, jclass, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    SetCursor
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetCursor
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     flat_backend_WL
 * Method:    JoystickPresent
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_flat_backend_WL_JoystickPresent
  (JNIEnv *, jclass, jint);

/*
 * Class:     flat_backend_WL
 * Method:    GetJoystickAxesCount
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_flat_backend_WL_GetJoystickAxesCount
  (JNIEnv *, jclass, jint);

/*
 * Class:     flat_backend_WL
 * Method:    GetJoystickAxes
 * Signature: (I[F)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_GetJoystickAxes
  (JNIEnv *, jclass, jint, jfloatArray);

/*
 * Class:     flat_backend_WL
 * Method:    GetJoystickButtonsCount
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_flat_backend_WL_GetJoystickButtonsCount
  (JNIEnv *, jclass, jint);

/*
 * Class:     flat_backend_WL
 * Method:    GetJoystickButtons
 * Signature: (I[I)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_GetJoystickButtons
  (JNIEnv *, jclass, jint, jintArray);

/*
 * Class:     flat_backend_WL
 * Method:    GetJoystickName
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_flat_backend_WL_GetJoystickName
  (JNIEnv *, jclass, jint);

/*
 * Class:     flat_backend_WL
 * Method:    SetWindowPosCallback
 * Signature: (Lflat/backend/WLEnums/WindowPosCallback;)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetWindowPosCallback
  (JNIEnv *, jclass, jobject);

/*
 * Class:     flat_backend_WL
 * Method:    SetWindowSizeCallback
 * Signature: (Lflat/backend/WLEnums/WindowSizeCallback;)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetWindowSizeCallback
  (JNIEnv *, jclass, jobject);

/*
 * Class:     flat_backend_WL
 * Method:    SetWindowCloseCallback
 * Signature: (Lflat/backend/WLEnums/WindowCloseCallback;)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetWindowCloseCallback
  (JNIEnv *, jclass, jobject);

/*
 * Class:     flat_backend_WL
 * Method:    SetWindowRefreshCallback
 * Signature: (Lflat/backend/WLEnums/WindowRefreshCallback;)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetWindowRefreshCallback
  (JNIEnv *, jclass, jobject);

/*
 * Class:     flat_backend_WL
 * Method:    SetWindowFocusCallback
 * Signature: (Lflat/backend/WLEnums/WindowFocusCallback;)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetWindowFocusCallback
  (JNIEnv *, jclass, jobject);

/*
 * Class:     flat_backend_WL
 * Method:    SetWindowIconifyCallback
 * Signature: (Lflat/backend/WLEnums/WindowIconifyCallback;)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetWindowIconifyCallback
  (JNIEnv *, jclass, jobject);

/*
 * Class:     flat_backend_WL
 * Method:    SetFramebufferSizeCallback
 * Signature: (Lflat/backend/WLEnums/WindowBufferSizeCallback;)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetFramebufferSizeCallback
  (JNIEnv *, jclass, jobject);

/*
 * Class:     flat_backend_WL
 * Method:    SetKeyCallback
 * Signature: (Lflat/backend/WLEnums/KeyCallback;)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetKeyCallback
  (JNIEnv *, jclass, jobject);

/*
 * Class:     flat_backend_WL
 * Method:    SetCharCallback
 * Signature: (Lflat/backend/WLEnums/CharCallback;)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetCharCallback
  (JNIEnv *, jclass, jobject);

/*
 * Class:     flat_backend_WL
 * Method:    SetCharModsCallback
 * Signature: (Lflat/backend/WLEnums/CharModsCallback;)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetCharModsCallback
  (JNIEnv *, jclass, jobject);

/*
 * Class:     flat_backend_WL
 * Method:    SetMouseButtonCallback
 * Signature: (Lflat/backend/WLEnums/MouseButtonCallback;)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetMouseButtonCallback
  (JNIEnv *, jclass, jobject);

/*
 * Class:     flat_backend_WL
 * Method:    SetCursorPosCallback
 * Signature: (Lflat/backend/WLEnums/CursorPosCallback;)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetCursorPosCallback
  (JNIEnv *, jclass, jobject);

/*
 * Class:     flat_backend_WL
 * Method:    SetCursorEnterCallback
 * Signature: (Lflat/backend/WLEnums/CursorEnterCallback;)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetCursorEnterCallback
  (JNIEnv *, jclass, jobject);

/*
 * Class:     flat_backend_WL
 * Method:    SetScrollCallback
 * Signature: (Lflat/backend/WLEnums/ScrollCallback;)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetScrollCallback
  (JNIEnv *, jclass, jobject);

/*
 * Class:     flat_backend_WL
 * Method:    SetDropCallback
 * Signature: (Lflat/backend/WLEnums/DropCallback;)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetDropCallback
  (JNIEnv *, jclass, jobject);

/*
 * Class:     flat_backend_WL
 * Method:    SetJoystickCallback
 * Signature: (Lflat/backend/WLEnums/JoyCallback;)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetJoystickCallback
  (JNIEnv *, jclass, jobject);

/*
 * Class:     flat_backend_WL
 * Method:    SetErrorCallback
 * Signature: (Lflat/backend/WLEnums/ErrorCallback;)V
 */
JNIEXPORT void JNICALL Java_flat_backend_WL_SetErrorCallback
        (JNIEnv *, jclass, jobject);
#ifdef __cplusplus
}
#endif
#endif
