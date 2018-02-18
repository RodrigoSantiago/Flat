//
// Created by Rodrigo on 30/11/2017
//

#include "flat_backend_SVG.h"
#include <glad/glad.h>
#include <iostream>
#include <unordered_map>

#define NANOVG_GL3_IMPLEMENTATION
#include "nanovg/nanovg.h"
#include "nanovg/nanovg_gl.h"

class Ctx {
public:
    NVGcontext* ctx;
    NVGglyphPosition gpos[2048];
    Ctx(NVGcontext* nvg) : ctx(nvg) {
    }
};

//---------------------------
//         Context
//---------------------------
JNIEXPORT jlong JNICALL Java_flat_backend_SVG_Create(JNIEnv * jEnv, jclass jClass, jint flags) {
    return (jlong) new Ctx(nvgCreateGL3(flags));
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_Destroy(JNIEnv * jEnv, jclass jClass, jlong context) {
    nvgDeleteGL3(((Ctx*)context)->ctx);
    delete ((Ctx*)context);
}

//---------------------------
//          Frame
//---------------------------
JNIEXPORT void JNICALL Java_flat_backend_SVG_BeginFrame(JNIEnv * jEnv, jclass jClass, jlong context, jint width, jint height, jfloat pixelRatio) {
    nvgBeginFrame(((Ctx*)context)->ctx, width, height, pixelRatio);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_CancelFrame(JNIEnv * jEnv, jclass jClass, jlong context) {
    nvgCancelFrame(((Ctx*)context)->ctx);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_EndFrame(JNIEnv * jEnv, jclass jClass, jlong context) {
    nvgEndFrame(((Ctx*)context)->ctx);
}

//---------------------------
//     Composite operation
//---------------------------
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetGlobalCompositeOperation(JNIEnv * jEnv, jclass jClass, jlong context, jint operation) {
    nvgGlobalCompositeOperation(((Ctx*)context)->ctx, operation);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetGlobalCompositeBlendFunction(JNIEnv * jEnv, jclass jClass, jlong context, jint srcRGB, jint dstRGB, jint srcAlpha, jint dstAlpha) {
    nvgGlobalCompositeBlendFuncSeparate(((Ctx*)context)->ctx, srcRGB, dstRGB, srcAlpha, dstAlpha);
}

//---------------------------
//      State Handling
//---------------------------
JNIEXPORT void JNICALL Java_flat_backend_SVG_StateSave(JNIEnv * jEnv, jclass jClass, jlong context) {
    nvgSave(((Ctx*)context)->ctx);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_StateRestore(JNIEnv * jEnv, jclass jClass, jlong context) {
    nvgRestore(((Ctx*)context)->ctx);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_StateReset(JNIEnv * jEnv, jclass jClass, jlong context) {
    nvgReset(((Ctx*)context)->ctx);
}

//---------------------------
//      Render styles
//---------------------------
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetShapeAntiAlias(JNIEnv * jEnv, jclass jClass, jlong context, jboolean enable) {
    nvgShapeAntiAlias(((Ctx*)context)->ctx, enable);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetPaintColor(JNIEnv * jEnv, jclass jClass, jlong context, jint color) {
    NVGcolor p = nvgRGBA(reinterpret_cast<unsigned char *>(&color)[3],
                             reinterpret_cast<unsigned char *>(&color)[2],
                             reinterpret_cast<unsigned char *>(&color)[1],
                             reinterpret_cast<unsigned char *>(&color)[0]);
    nvgStrokeColor(((Ctx*)context)->ctx, p);
    nvgFillColor(((Ctx*)context)->ctx, p);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetPaintLinearGradient(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jint count, jfloatArray stops, jintArray colors, jint cycleMethod) {
    NVGpaint p = nvgLinearGradient(((Ctx*)context)->ctx, x1, y1, x2, y2, nvgRGBAf(0,0,0,1), nvgRGBAf(1, 1, 1, 1));
    p.fillType = 1;
    p.stopsCount = count;
    p.cycleMethod = cycleMethod;

    void * pointer = jEnv->GetPrimitiveArrayCritical(stops, 0);
    jfloat* fdata = reinterpret_cast<jfloat*>(pointer);
    for  (int i = 0; i < count; i++) {
        p.stops[i] = fdata[i];
    }
    jEnv->ReleasePrimitiveArrayCritical(stops, pointer, 0);

    pointer = jEnv->GetPrimitiveArrayCritical(colors, 0);
    jint* idata = reinterpret_cast<jint*>(pointer);
    for  (int i = 0; i < count; i++) {
        p.colors[i] = nvgRGBA(reinterpret_cast<unsigned char *>(&idata[i])[3],
                              reinterpret_cast<unsigned char *>(&idata[i])[2],
                              reinterpret_cast<unsigned char *>(&idata[i])[1],
                              reinterpret_cast<unsigned char *>(&idata[i])[0]);
    }
    jEnv->ReleasePrimitiveArrayCritical(colors, pointer, 0);
    nvgStrokePaint(((Ctx*)context)->ctx, p);
    nvgFillPaint(((Ctx*)context)->ctx, p);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetPaintRadialGradient(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x1, jfloat y1, jfloat radiusIn, jfloat radiusOut, jint count, jfloatArray stops, jintArray colors, jint cycleMethod) {
    NVGpaint p = nvgRadialGradient(((Ctx*)context)->ctx, x1, y1, radiusIn, radiusOut, nvgRGBAf(0,0,0,1), nvgRGBAf(1, 1, 1, 1));
    p.fillType = 1;
    p.stopsCount = count;
    p.cycleMethod = cycleMethod;

    void * pointer = jEnv->GetPrimitiveArrayCritical(stops, 0);
    jfloat* fdata = reinterpret_cast<jfloat*>(pointer);
    for  (int i = 0; i < count; i++) {
        p.stops[i] = fdata[i];
    }
    jEnv->ReleasePrimitiveArrayCritical(stops, pointer, 0);

    pointer = jEnv->GetPrimitiveArrayCritical(colors, 0);
    jint* idata = reinterpret_cast<jint*>(pointer);
    for  (int i = 0; i < count; i++) {
        p.colors[i] = nvgRGBA(reinterpret_cast<unsigned char *>(&idata[i])[3],
                              reinterpret_cast<unsigned char *>(&idata[i])[2],
                              reinterpret_cast<unsigned char *>(&idata[i])[1],
                              reinterpret_cast<unsigned char *>(&idata[i])[0]);
    }
    jEnv->ReleasePrimitiveArrayCritical(colors, pointer, 0);
    nvgStrokePaint(((Ctx*)context)->ctx, p);
    nvgFillPaint(((Ctx*)context)->ctx, p);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetPaintBoxShadow(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y, jfloat width, jfloat height, jfloat corners, jfloat blur, jfloat alpha) {
    NVGpaint p = nvgBoxGradient(((Ctx*)context)->ctx, x, y, width, height, corners, blur, nvgRGBAf(0,0,0, alpha), nvgRGBAf(0, 0, 0, 0));
    nvgStrokePaint(((Ctx*)context)->ctx, p);
    nvgFillPaint(((Ctx*)context)->ctx, p);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetPaintImage(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y, jfloat width, jfloat height, jint textureID) {
    NVGpaint p = nvgImagePattern(((Ctx*)context)->ctx, x, y, width, height, 0, -(textureID + 1), 1);
    p.fillType = 2;
    nvgStrokePaint(((Ctx*)context)->ctx, p);
    nvgFillPaint(((Ctx*)context)->ctx, p);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetMiterLimit(JNIEnv * jEnv, jclass jClass, jlong context, jfloat limit) {
    nvgMiterLimit(((Ctx*)context)->ctx, limit);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetStrokeWidth(JNIEnv * jEnv, jclass jClass, jlong context, jfloat size) {
    nvgStrokeWidth(((Ctx*)context)->ctx, size);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetLineCap(JNIEnv * jEnv, jclass jClass, jlong context, jint cap) {
    nvgLineCap(((Ctx*)context)->ctx, cap);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetLineJoin(JNIEnv * jEnv, jclass jClass, jlong context, jint join) {
    nvgLineJoin(((Ctx*)context)->ctx, join);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetGlobalAlpha(JNIEnv * jEnv, jclass jClass, jlong context, jfloat alpha) {
    nvgGlobalAlpha(((Ctx*)context)->ctx, alpha);
}

//---------------------------
//         Transforms
//---------------------------
JNIEXPORT void JNICALL Java_flat_backend_SVG_TransformIdentity(JNIEnv * jEnv, jclass jClass, jlong context) {
    nvgResetTransform(((Ctx*)context)->ctx);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_TransformSet(JNIEnv * jEnv, jclass jClass, jlong context, jfloat m00, jfloat m01, jfloat m02, jfloat m10, jfloat m11, jfloat m12) {
    nvgResetTransform(((Ctx*)context)->ctx);
    nvgTransform(((Ctx*)context)->ctx, m00, m01, m02, m10, m11, m12);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_TransformGet(JNIEnv * jEnv, jclass jClass, jlong context, jfloatArray data6) {
    void * pointer = jEnv->GetPrimitiveArrayCritical(data6, 0);
    nvgCurrentTransform(((Ctx*)context)->ctx, (float *) pointer);
    jEnv->ReleasePrimitiveArrayCritical(data6, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_TransformTranslate(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y) {
    nvgTranslate(((Ctx*)context)->ctx, x, y);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_TransformRotate(JNIEnv * jEnv, jclass jClass, jlong context, jfloat angle) {
    nvgRotate(((Ctx*)context)->ctx, angle);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_TransformSkewX(JNIEnv * jEnv, jclass jClass, jlong context, jfloat angle) {
    nvgSkewX(((Ctx*)context)->ctx, angle);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_TransformSkewY(JNIEnv * jEnv, jclass jClass, jlong context, jfloat angle) {
    nvgSkewY(((Ctx*)context)->ctx, angle);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_TransformScale(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y) {
    nvgScale(((Ctx*)context)->ctx, x, y);
}

//---------------------------
//          Scissoring
//---------------------------
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetScissor(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y, jfloat width, jfloat height) {
    nvgScissor(((Ctx*)context)->ctx, x, y, width, height);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetIntersectScissor(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y, jfloat width, jfloat height) {
    nvgIntersectScissor(((Ctx*)context)->ctx, x, y, width, height);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_ResetScissor(JNIEnv * jEnv, jclass jClass, jlong context) {
    nvgResetScissor(((Ctx*)context)->ctx);
}

//---------------------------
//           Paths
//---------------------------
JNIEXPORT void JNICALL Java_flat_backend_SVG_BeginPath(JNIEnv * jEnv, jclass jClass, jlong context) {
    nvgBeginPath(((Ctx*)context)->ctx);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_MoveTo(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y) {
    nvgMoveTo(((Ctx*)context)->ctx, x, y);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_LineTo(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y) {
    nvgLineTo(((Ctx*)context)->ctx, x, y);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_BezierTo(JNIEnv * jEnv, jclass jClass, jlong context, jfloat c1x, jfloat c1y, jfloat c2x, jfloat c2y, jfloat x, jfloat y) {
    nvgBezierTo(((Ctx*)context)->ctx, c1x, c1y, c2x, c2y, x, y);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_QuadTo(JNIEnv * jEnv, jclass jClass, jlong context, jfloat cx, jfloat cy, jfloat x, jfloat y) {
    nvgQuadTo(((Ctx*)context)->ctx, cx, cy, x, y);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_ArcTo(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat radius) {
    nvgArcTo(((Ctx*)context)->ctx, x1, y1, x2, y2, radius);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_ClosePath(JNIEnv * jEnv, jclass jClass, jlong context) {
    nvgClosePath(((Ctx*)context)->ctx);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_PathWinding(JNIEnv * jEnv, jclass jClass, jlong context, jint dir) {
    nvgPathWinding(((Ctx*)context)->ctx, dir);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_Arc(JNIEnv * jEnv, jclass jClass, jlong context, jfloat cx, jfloat cy, jfloat radius, jfloat a0, jfloat a1, jint dir) {
    nvgArc(((Ctx*)context)->ctx, cx, cy, radius, a0, a1, dir);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_Rect(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y, jfloat width, jfloat height) {
    nvgRect(((Ctx*)context)->ctx, x, y, width, height);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_RoundedRect(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y, jfloat width, jfloat height, jfloat c1, jfloat c2, jfloat c3, jfloat c4) {
    nvgRoundedRectVarying(((Ctx*)context)->ctx, x, y, width, height, c1, c2, c3, c4);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_Ellipse(JNIEnv * jEnv, jclass jClass, jlong context, jfloat cx, jfloat cy, jfloat rx, jfloat ry) {
    nvgEllipse(((Ctx*)context)->ctx, cx, cy, rx, ry);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_Circle(JNIEnv * jEnv, jclass jClass, jlong context, jfloat cx, jfloat cy, jfloat radius) {
    nvgCircle(((Ctx*)context)->ctx, cx, cy, radius);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_Fill(JNIEnv * jEnv, jclass jClass, jlong context) {
    nvgFill(((Ctx*)context)->ctx);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_Stroke(JNIEnv * jEnv, jclass jClass, jlong context) {
    nvgStroke(((Ctx*)context)->ctx);
}

JNIEXPORT void JNICALL Java_flat_backend_SVG_RoundedRectShadow(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y, jfloat width, jfloat height, jfloat c1, jfloat c2, jfloat c3, jfloat c4, jfloat blur) {
    NVGpaint p = nvgBoxGradient(((Ctx*)context)->ctx, x, y, width, height, c1, blur, nvgRGBAf(0,0,0,1), nvgRGBAf(0,0,0,0));
    nvgFillPaint(((Ctx*)context)->ctx, p);
    nvgRect(((Ctx*)context)->ctx, x + blur, y + blur, width - blur * 2, height - blur * 2);
}
//---------------------------
//           Text
//---------------------------
JNIEXPORT jint JNICALL Java_flat_backend_SVG_FontCreate(JNIEnv * jEnv, jclass jClass, jlong context, jstring name, jbyteArray data) {
    const char * n = jEnv->GetStringUTFChars(name, 0);
    jint size = jEnv->GetArrayLength(data);
    void * pointer = jEnv->GetPrimitiveArrayCritical(data, 0);
    unsigned char * copy = (unsigned char *)malloc(sizeof(unsigned char) * size);
    memcpy(copy, pointer, sizeof(unsigned char) * size);
    jint id = nvgCreateFontMem(((Ctx*)context)->ctx, n, copy, size, 1);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
    jEnv->ReleaseStringUTFChars(name, n);
    return id;
}
JNIEXPORT jint JNICALL Java_flat_backend_SVG_FontFind(JNIEnv * jEnv, jclass jClass, jlong context, jstring name) {
    const char * n = jEnv->GetStringUTFChars(name, 0);
    jint id  = nvgFindFont(((Ctx*)context)->ctx, n);
    jEnv->ReleaseStringUTFChars(name, n);
    return id;
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_FontDestroy(JNIEnv * jEnv, jclass jClass, jlong context, jint id) {
    // todo - implement
}
JNIEXPORT jboolean JNICALL Java_flat_backend_SVG_FontAddFallbackFont(JNIEnv * jEnv, jclass jClass, jlong context, jint baseFont, jint fallbackFont) {
    return nvgAddFallbackFontId(((Ctx*)context)->ctx, baseFont, fallbackFont);
}

JNIEXPORT void JNICALL Java_flat_backend_SVG_TextSetFont(JNIEnv * jEnv, jclass jClass, jlong context, jint font) {
    nvgFontFaceId(((Ctx*)context)->ctx, font);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_TextSetSize(JNIEnv * jEnv, jclass jClass, jlong context, jfloat size) {
    nvgFontSize(((Ctx*)context)->ctx, size);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_TextSetBlur(JNIEnv * jEnv, jclass jClass, jlong context, jfloat blur) {
    nvgFontBlur(((Ctx*)context)->ctx, blur);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_TextSetLetterSpacing(JNIEnv * jEnv, jclass jClass, jlong context, jfloat spacing) {
    nvgTextLetterSpacing(((Ctx*)context)->ctx, spacing);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_TextSetLineHeight(JNIEnv * jEnv, jclass jClass, jlong context, jfloat lineHeight) {
    nvgTextLineHeight(((Ctx*)context)->ctx, lineHeight);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_TextSetAlign(JNIEnv * jEnv, jclass jClass, jlong context, jint align) {
    nvgTextAlign(((Ctx*)context)->ctx, align);
}

JNIEXPORT jfloat JNICALL Java_flat_backend_SVG_DrawText(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y, jstring string) {
    const char * data = jEnv->GetStringUTFChars(string, 0);
    jfloat size = nvgText(((Ctx*)context)->ctx, x, y, data, nullptr);
    jEnv->ReleaseStringUTFChars(string, data);
    return size;
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_DrawTextBox(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y, jfloat breakRowWidth, jstring string) {
    const char * data = jEnv->GetStringUTFChars(string, 0);
    nvgTextBox(((Ctx*)context)->ctx, x, y, breakRowWidth, data, nullptr);
    jEnv->ReleaseStringUTFChars(string, data);
}
JNIEXPORT jfloat JNICALL Java_flat_backend_SVG_DrawTextBounds(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y, jstring string, jfloatArray bounds4) {
    const char * data = jEnv->GetStringUTFChars(string, 0);
    float boundsData[4];
    jfloat size = nvgTextBounds(((Ctx*)context)->ctx, x, y, data, nullptr, boundsData);
    jEnv->ReleaseStringUTFChars(string, data);
    jEnv->SetFloatArrayRegion(bounds4, 0, 4, boundsData);
    return size;
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_DrawTextBoxBounds(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y, jfloat breakRowWidth, jstring string, jfloatArray bounds4) {
    const char * data = jEnv->GetStringUTFChars(string, 0);
    float boundsData[4];
    nvgTextBoxBounds(((Ctx*)context)->ctx, x, y, breakRowWidth, data, nullptr, boundsData);
    jEnv->ReleaseStringUTFChars(string, data);
    jEnv->SetFloatArrayRegion(bounds4, 0, 4, boundsData);
}

JNIEXPORT jfloat JNICALL Java_flat_backend_SVG_DrawTextBuffer(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y, jobject string, jint offset, jint length) {
    const char * data = (const char *) jEnv->GetDirectBufferAddress(string);
    jfloat size = nvgText(((Ctx*)context)->ctx, x, y, data + offset * 2, data + length * 2);
    return size;
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_DrawTextBoxBuffer(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y, jfloat breakRowWidth, jobject string, jint offset, jint length) {
    const char * data = (const char *) jEnv->GetDirectBufferAddress(string);
    nvgTextBox(((Ctx*)context)->ctx, x, y, breakRowWidth, data + offset * 2, data + length * 2);
}
JNIEXPORT jfloat JNICALL Java_flat_backend_SVG_DrawTextBoundsBuffer(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y, jobject string, jint offset, jint length, jfloatArray bounds4) {
    const char * data = (const char *) jEnv->GetDirectBufferAddress(string);
    float boundsData[4];
    jfloat size = nvgTextBounds(((Ctx*)context)->ctx, x, y, data + offset * 2, data + length * 2, boundsData);

    jEnv->SetFloatArrayRegion(bounds4, 0, 4, boundsData);
    return size;
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_DrawTextBoxBoundsBuffer(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y, jfloat breakRowWidth, jobject string, jint offset, jint length, jfloatArray bounds4) {
    const char * data = (const char *) jEnv->GetDirectBufferAddress(string);
    float boundsData[4];
    nvgTextBoxBounds(((Ctx*)context)->ctx, x, y, breakRowWidth, data + offset * 2, data + length * 2, boundsData);

    jEnv->SetFloatArrayRegion(bounds4, 0, 4, boundsData);
}

JNIEXPORT jfloat JNICALL Java_flat_backend_SVG_TextGetWidth(JNIEnv * jEnv, jclass jClass, jlong context, jstring string) {
    const char * data = jEnv->GetStringUTFChars(string, 0);
    NVGglyphPosition* gpos = ((Ctx*)context)->gpos;
    int size = nvgTextGlyphPositions(((Ctx*)context)->ctx, 0, 0, data, nullptr, gpos, std::min(jEnv->GetStringLength(string), (jint) 2048));
    float width = size == 0 ? 0 : gpos[size - 1].maxx - gpos[0].minx;
    jEnv->ReleaseStringUTFChars(string, data);
    return width;
}

JNIEXPORT jfloat JNICALL Java_flat_backend_SVG_TextGetWidthBuffer(JNIEnv * jEnv, jclass jClass, jlong context, jobject string, jint offset, jint length) {
    const char * data = (const char *) jEnv->GetDirectBufferAddress(string);
    NVGglyphPosition* gpos = ((Ctx*)context)->gpos;
    int size = nvgTextGlyphPositions(((Ctx*)context)->ctx, 0, 0, data + offset * 2, data + length * 2, ((Ctx*)context)->gpos, std::min(length, (jint) 2048));
    float width = size == 0 ? 0 : gpos[size - 1].maxx - gpos[0].minx;
    return width;
}

JNIEXPORT jint JNICALL Java_flat_backend_SVG_TextGetLastGlyph(JNIEnv * jEnv, jclass jClass, jlong context, jstring string, jfloat breakRowWidth) {
    const char * data = jEnv->GetStringUTFChars(string, 0);
    NVGglyphPosition* gpos = ((Ctx*)context)->gpos;
    int size = nvgTextGlyphPositions(((Ctx*)context)->ctx, 0, 0, data, nullptr, gpos, std::min(jEnv->GetStringLength(string), (jint) 2048));
    if (size == 0) {
        jEnv->ReleaseStringUTFChars(string, data);
        return 0;
    }

    int pos = -1;
    float start = gpos[0].minx;
    for (int i = 0; i < size; i++) {
        if (gpos[i].maxx - start > breakRowWidth) {
            break;
        }
        pos = i;
    }
    jEnv->ReleaseStringUTFChars(string, data);
    return pos;
}

JNIEXPORT jint JNICALL Java_flat_backend_SVG_TextGetLastGlyphBuffer(JNIEnv * jEnv, jclass jClass, jlong context, jobject string, jint offset, jint length, jfloat breakRowWidth) {
    const char * data = (const char *) jEnv->GetDirectBufferAddress(string);
    NVGglyphPosition* gpos = ((Ctx*)context)->gpos;
    int size = nvgTextGlyphPositions(((Ctx*)context)->ctx, 0, 0, data + offset * 2, data + length * 2, gpos, std::min(length, (jint) 2048));
    if (size == 0) return 0;

    int pos = -1;
    float start = gpos[0].minx;
    for (int i = 0; i < size; i++) {
        if (gpos[i].maxx - start > breakRowWidth) {
            break;
        }
        pos = i;
    }
    return pos;
}

JNIEXPORT jfloat JNICALL Java_flat_backend_SVG_TextMetricsGetAscender(JNIEnv * jEnv, jclass jClass, jlong context) {
    float ascender, descender, lineh;
    nvgTextMetrics(((Ctx*)context)->ctx, &ascender, &descender, &lineh);
    return ascender;
}
JNIEXPORT jfloat JNICALL Java_flat_backend_SVG_TextMetricsGetDescender(JNIEnv * jEnv, jclass jClass, jlong context) {
    float ascender, descender, lineh;
    nvgTextMetrics(((Ctx*)context)->ctx, &ascender, &descender, &lineh);
    return descender;
}
JNIEXPORT jfloat JNICALL Java_flat_backend_SVG_TextMetricsGetLineHeight(JNIEnv * jEnv, jclass jClass, jlong context) {
    float ascender, descender, lineh;
    nvgTextMetrics(((Ctx*)context)->ctx, &ascender, &descender, &lineh);
    return lineh;
}

