//
// Created by Rodrigo on 30/11/2017
//

#include "flat_backend_SVG.h"
#include <glad/glad.h>
#define NANOVG_GL3_IMPLEMENTATION
#include "nanovg.h"
#include "nanovg_gl.h"

static NVGcontext* ctx;

//---------------------------
//         Context
//---------------------------
JNIEXPORT jboolean JNICALL Java_flat_backend_SVG_Init(JNIEnv * jEnv, jclass jClass, jint flags) {
    ctx = nvgCreateGL3(flags);
    return ctx != nullptr;
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_Finish(JNIEnv * jEnv, jclass jClass) {
    nvgDeleteGL3(ctx);
}

//---------------------------
//          Frame
//---------------------------
JNIEXPORT void JNICALL Java_flat_backend_SVG_BeginFrame(JNIEnv * jEnv, jclass jClass, jint width, jint height, jfloat pixelRatio) {
    nvgBeginFrame(ctx, width, height, pixelRatio);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_CancelFrame(JNIEnv * jEnv, jclass jClass) {
    nvgCancelFrame(ctx);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_EndFrame(JNIEnv * jEnv, jclass jClass) {
    nvgEndFrame(ctx);
}

//---------------------------
//     Composite operation
//---------------------------
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetGlobalCompositeOperation(JNIEnv * jEnv, jclass jClass, jint operation) {
    nvgGlobalCompositeOperation(ctx, operation);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetGlobalCompositeBlendFunction(JNIEnv * jEnv, jclass jClass, jint srcRGB, jint dstRGB, jint srcAlpha, jint dstAlpha) {
    nvgGlobalCompositeBlendFuncSeparate(ctx, srcRGB, dstRGB, srcAlpha, dstAlpha);
}

//---------------------------
//      State Handling
//---------------------------
JNIEXPORT void JNICALL Java_flat_backend_SVG_StateSave(JNIEnv * jEnv, jclass jClass) {
    nvgSave(ctx);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_StateRestore(JNIEnv * jEnv, jclass jClass) {
    nvgRestore(ctx);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_StateReset(JNIEnv * jEnv, jclass jClass) {
    nvgReset(ctx);
}

//---------------------------
//      Render styles
//---------------------------
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetShapeAntiAlias(JNIEnv * jEnv, jclass jClass, jint enable) {
    nvgShapeAntiAlias(ctx, enable);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetStrokeColor(JNIEnv * jEnv, jclass jClass, jint color) {
    nvgStrokeColor(ctx,
                   nvgRGBA(reinterpret_cast<unsigned char *>(&color)[0],
                           reinterpret_cast<unsigned char *>(&color)[1],
                           reinterpret_cast<unsigned char *>(&color)[2],
                           reinterpret_cast<unsigned char *>(&color)[3]));
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetFillColor(JNIEnv * jEnv, jclass jClass, jint color) {
    nvgFillColor(ctx,
                   nvgRGBA(reinterpret_cast<unsigned char *>(&color)[0],
                           reinterpret_cast<unsigned char *>(&color)[1],
                           reinterpret_cast<unsigned char *>(&color)[2],
                           reinterpret_cast<unsigned char *>(&color)[3]));
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetMiterLimit(JNIEnv * jEnv, jclass jClass, jfloat limit) {
    nvgMiterLimit(ctx, limit);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetStrokeWidth(JNIEnv * jEnv, jclass jClass, jfloat size) {
    nvgStrokeWidth(ctx, size);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetLineCap(JNIEnv * jEnv, jclass jClass, jint cap) {
    nvgLineCap(ctx, cap);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetLineJoin(JNIEnv * jEnv, jclass jClass, jint join) {
    nvgLineJoin(ctx, join);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetGlobalAlpha(JNIEnv * jEnv, jclass jClass, jfloat alpha) {
    nvgGlobalAlpha(ctx, alpha);
}

//---------------------------
//         Transforms
//---------------------------
JNIEXPORT void JNICALL Java_flat_backend_SVG_TransformIdentity(JNIEnv * jEnv, jclass jClass) {
    nvgResetTransform(ctx);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_TransformSet(JNIEnv * jEnv, jclass jClass, jfloat m00, jfloat m01, jfloat m02, jfloat m10, jfloat m11, jfloat m12) {
    nvgTransform(ctx, m00, m01, m02, m10, m11, m12);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_TransformTranslate(JNIEnv * jEnv, jclass jClass, jfloat x, jfloat y) {
    nvgTranslate(ctx, x, y);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_TransformRotate(JNIEnv * jEnv, jclass jClass, jfloat angle) {
    nvgRotate(ctx, angle);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_TransformSkewX(JNIEnv * jEnv, jclass jClass, jfloat angle) {
    nvgSkewX(ctx, angle);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_TransformSkewY(JNIEnv * jEnv, jclass jClass, jfloat angle) {
    nvgSkewY(ctx, angle);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_TransformScale(JNIEnv * jEnv, jclass jClass, jfloat x, jfloat y) {
    nvgScale(ctx, x, y);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_TransformGetCurrent(JNIEnv * jEnv, jclass jClass, jfloatArray data6) {
    void * pointer = jEnv->GetPrimitiveArrayCritical(data6, 0);
    nvgCurrentTransform(ctx, (float *) pointer);
    jEnv->ReleasePrimitiveArrayCritical(data6, pointer, 0);
}

//---------------------------
//          Scissoring
//---------------------------
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetScissor(JNIEnv * jEnv, jclass jClass, jfloat x, jfloat y, jfloat width, jfloat height) {
    nvgScissor(ctx, x, y, width, height);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetIntersectScissor(JNIEnv * jEnv, jclass jClass, jfloat x, jfloat y, jfloat width, jfloat height) {
    nvgIntersectScissor(ctx, x, y, width, height);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_ResetScissor(JNIEnv * jEnv, jclass jClass) {
    nvgResetScissor(ctx);
}

//---------------------------
//           Paths
//---------------------------
JNIEXPORT void JNICALL Java_flat_backend_SVG_BeginPath(JNIEnv * jEnv, jclass jClass) {
    nvgBeginPath(ctx);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_MoveTo(JNIEnv * jEnv, jclass jClass, jfloat x, jfloat y) {
    nvgMoveTo(ctx, x, y);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_LineTo(JNIEnv * jEnv, jclass jClass, jfloat x, jfloat y) {
    nvgLineTo(ctx, x, y);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_BezierTo(JNIEnv * jEnv, jclass jClass, jfloat c1x, jfloat c1y, jfloat c2x, jfloat c2y, jfloat x, jfloat y) {
    nvgBezierTo(ctx, c1x, c1y, c2x, c2y, x, y);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_QuadTo(JNIEnv * jEnv, jclass jClass, jfloat cx, jfloat cy, jfloat x, jfloat y) {
    nvgQuadTo(ctx, cx, cy, x, y);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_ArcTo(JNIEnv * jEnv, jclass jClass, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat radius) {
    nvgArcTo(ctx, x1, y1, x2, y2, radius);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_ClosePath(JNIEnv * jEnv, jclass jClass) {
    nvgClosePath(ctx);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_PathWinding(JNIEnv * jEnv, jclass jClass, jint dir) {
    nvgPathWinding(ctx, dir);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_Arc(JNIEnv * jEnv, jclass jClass, jfloat cx, jfloat cy, jfloat radius, jfloat a0, jfloat a1, jint dir) {
    nvgArc(ctx, cx, cy, radius, a0, a1, dir);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_Rect(JNIEnv * jEnv, jclass jClass, jfloat x, jfloat y, jfloat width, jfloat height) {
    nvgRect(ctx, x, y, width, height);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_RoundedRect(JNIEnv * jEnv, jclass jClass, jfloat x, jfloat y, jfloat width, jfloat height, jfloat radTopLeft, jfloat radTopRight, jfloat radBottomRight, jfloat radBottomLeft) {
    nvgRoundedRectVarying(ctx, x, y, width, height, radTopLeft, radTopRight, radBottomRight, radBottomLeft);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_Ellipse(JNIEnv * jEnv, jclass jClass, jfloat cx, jfloat cy, jfloat rx, jfloat ry) {
    nvgEllipse(ctx, cx, cy, rx, ry);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_Circle(JNIEnv * jEnv, jclass jClass, jfloat cx, jfloat cy, jfloat radius) {
    nvgCircle(ctx, cx, cy, radius);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_Fill(JNIEnv * jEnv, jclass jClass) {
    nvgFill(ctx);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_Stroke(JNIEnv * jEnv, jclass jClass) {
    nvgStroke(ctx);
}

//---------------------------
//           Text
//---------------------------
JNIEXPORT jint JNICALL Java_flat_backend_SVG_FontCreate(JNIEnv * jEnv, jclass jClass, jstring name, jbyteArray data) {
    const char * n = jEnv->GetStringUTFChars(name, 0);
    jint size = jEnv->GetArrayLength(data);
    void * pointer = jEnv->GetPrimitiveArrayCritical(data, 0);
    nvgCreateFontMem(ctx, n, (unsigned char *) pointer, size, 0);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
    jEnv->ReleaseStringUTFChars(name, n);
}
JNIEXPORT jint JNICALL Java_flat_backend_SVG_FontFind(JNIEnv * jEnv, jclass jClass, jstring name) {
    const char * n = jEnv->GetStringUTFChars(name, 0);
    jint id  = nvgFindFont(ctx, n);
    jEnv->ReleaseStringUTFChars(name, n);
    return id;
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_FontDestroy(JNIEnv * jEnv, jclass jClass, jint id) {
    // todo - implement
}
JNIEXPORT jboolean JNICALL Java_flat_backend_SVG_FontAddFallbackFont(JNIEnv * jEnv, jclass jClass, jint baseFont, jint fallbackFont) {
    nvgAddFallbackFontId(ctx, baseFont, fallbackFont);
}

JNIEXPORT void JNICALL Java_flat_backend_SVG_TextSetFont(JNIEnv * jEnv, jclass jClass, jint font) {
    nvgFontFaceId(ctx, font);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_TextSetSize(JNIEnv * jEnv, jclass jClass, jfloat size) {
    nvgFontSize(ctx, size);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_TextSetBlur(JNIEnv * jEnv, jclass jClass, jfloat blur) {
    nvgFontBlur(ctx, blur);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_TextSetLetterSpacing(JNIEnv * jEnv, jclass jClass, jfloat spacing) {
    nvgTextLetterSpacing(ctx, spacing);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_TextSetLineHeight(JNIEnv * jEnv, jclass jClass, jfloat lineHeight) {
    nvgTextLineHeight(ctx, lineHeight);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_TextSetAlign(JNIEnv * jEnv, jclass jClass, jint align) {
    nvgTextAlign(ctx, align);
}

JNIEXPORT jfloat JNICALL Java_flat_backend_SVG_DrawText(JNIEnv * jEnv, jclass jClass, jfloat x, jfloat y, jstring jstring) {
    const char * data = jEnv->GetStringUTFChars(jstring, 0);
    jfloat size = nvgText(ctx, x, y, data, nullptr);
    jEnv->ReleaseStringUTFChars(jstring, data);
    return size;
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_DrawTextBox(JNIEnv * jEnv, jclass jClass, jfloat x, jfloat y, jfloat breakRowWidth, jstring jstring) {
    const char * data = jEnv->GetStringUTFChars(jstring, 0);
    nvgTextBox(ctx, x, y, breakRowWidth, data, nullptr);
    jEnv->ReleaseStringUTFChars(jstring, data);
}
JNIEXPORT jfloat JNICALL Java_flat_backend_SVG_DrawTextBounds(JNIEnv * jEnv, jclass jClass, jfloat x, jfloat y, jstring jstring, jfloatArray bounds4) {
    const char * data = jEnv->GetStringUTFChars(jstring, 0);
    float boundsData[4];
    jfloat size = nvgTextBounds(ctx, x, y, data, nullptr, boundsData);
    jEnv->ReleaseStringUTFChars(jstring, data);
    jEnv->SetFloatArrayRegion(bounds4, 0, 4, boundsData);
    return size;
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_DrawTextBoxBounds(JNIEnv * jEnv, jclass jClass, jfloat x, jfloat y, jfloat breakRowWidth, jstring jstring, jfloatArray bounds4) {
    const char * data = jEnv->GetStringUTFChars(jstring, 0);
    float boundsData[4];
    nvgTextBoxBounds(ctx, x, y, breakRowWidth, data, nullptr, boundsData);
    jEnv->ReleaseStringUTFChars(jstring, data);
    jEnv->SetFloatArrayRegion(bounds4, 0, 4, boundsData);
}
JNIEXPORT jfloat JNICALL Java_flat_backend_SVG_TextMetricsGetAscender(JNIEnv * jEnv, jclass jClass) {
    float ascender, descender, lineh;
    nvgTextMetrics(ctx, &ascender, &descender, &lineh);
    return ascender;
}
JNIEXPORT jfloat JNICALL Java_flat_backend_SVG_TextMetricsGetDescender(JNIEnv * jEnv, jclass jClass) {
    float ascender, descender, lineh;
    nvgTextMetrics(ctx, &ascender, &descender, &lineh);
    return descender;
}
JNIEXPORT jfloat JNICALL Java_flat_backend_SVG_TextMetricsGetLineHeight(JNIEnv * jEnv, jclass jClass) {
    float ascender, descender, lineh;
    nvgTextMetrics(ctx, &ascender, &descender, &lineh);
    return lineh;
}

