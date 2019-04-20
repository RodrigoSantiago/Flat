//
// Created by Rodrigo on 30/11/2017
//

#include "flat_backend_SVG.h"
#include <glad/glad.h>
#include <iostream>
#include <unordered_map>

#include "flatvectors.h"

//---------------------------
//         Context
//---------------------------
JNIEXPORT jlong JNICALL Java_flat_backend_SVG_Create(JNIEnv * jEnv, jclass jClass) {
    return (jlong) fvCreate();
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_Destroy(JNIEnv * jEnv, jclass jClass, jlong context) {
    fvDestroy((fvContext*) context);
}

//---------------------------
//          Frame
//---------------------------
JNIEXPORT void JNICALL Java_flat_backend_SVG_BeginFrame(JNIEnv * jEnv, jclass jClass, jlong context, jint width, jint height) {
    fvBegin((fvContext*) context, width, height);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_EndFrame(JNIEnv * jEnv, jclass jClass, jlong context) {
    fvEnd((fvContext*) context);
}

JNIEXPORT void JNICALL Java_flat_backend_SVG_Flush(JNIEnv * jEnv, jclass jClass, jlong context) {
    fvFlush((fvContext*) context);
}

//---------------------------
//      Render styles
//---------------------------
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetAntiAlias(JNIEnv * jEnv, jclass jClass, jlong context, jboolean enabled) {
    fvAntiAlias((fvContext*) context, enabled == 1 ? 1 : 0);
}

JNIEXPORT void JNICALL Java_flat_backend_SVG_SetStroke(JNIEnv * jEnv, jclass jClass, jlong context, jfloat width, jint cap, jint join, jfloat mitter) {
    fvCap _cap = cap == 0 ? fvCap::CAP_BUTT : cap == 1 ? fvCap::CAP_SQUARE : fvCap::CAP_ROUND;
    fvJoin _join = join == 0 ? fvJoin::JOIN_BEVEL : join == 1 ? fvJoin::JOIN_MITER : fvJoin::JOIN_ROUND;
    fvSetStroker((fvContext *) context, {width, _cap, _join, mitter});
}

JNIEXPORT void JNICALL Java_flat_backend_SVG_SetPaintColor(JNIEnv * jEnv, jclass jClass, jlong context, jint color) {
    fvSetPaint((fvContext *) context, fvColorPaint(color));
}

JNIEXPORT void JNICALL Java_flat_backend_SVG_SetPaintLinearGradient(JNIEnv * jEnv, jclass jClass, jlong context, jfloatArray affine, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jint count, jfloatArray stops, jintArray colors, jint cycleMethod) {
    float* _stops = (float*) jEnv->GetPrimitiveArrayCritical(stops, 0);
    long* _colors = (long*) jEnv->GetPrimitiveArrayCritical(colors, 0);
    float* _affine = (float*) jEnv->GetPrimitiveArrayCritical(affine, 0);
    fvSetPaint((fvContext *) context, fvLinearGradientPaint(_affine, x1, y1, x2, y2, count, _stops, _colors, cycleMethod));
    jEnv->ReleasePrimitiveArrayCritical(affine, _affine, 0);
    jEnv->ReleasePrimitiveArrayCritical(colors, _colors, 0);
    jEnv->ReleasePrimitiveArrayCritical(stops, _stops, 0);
}

JNIEXPORT void JNICALL Java_flat_backend_SVG_SetPaintRadialGradient(JNIEnv * jEnv, jclass jClass, jlong context, jfloatArray affine, jfloat x1, jfloat y1, jfloat rIn, jfloat rOut, jint count, jfloatArray stops, jintArray colors, jint cycleMethod) {
    float* _stops = (float*) jEnv->GetPrimitiveArrayCritical(stops, 0);
    long* _colors = (long*) jEnv->GetPrimitiveArrayCritical(colors, 0);
    float* _affine = (float*) jEnv->GetPrimitiveArrayCritical(affine, 0);
    fvSetPaint((fvContext *) context, fvRadialGradientPaint(_affine, x1, y1, rIn, rOut, count, _stops, _colors, cycleMethod));
    jEnv->ReleasePrimitiveArrayCritical(affine, _affine, 0);
    jEnv->ReleasePrimitiveArrayCritical(colors, _colors, 0);
    jEnv->ReleasePrimitiveArrayCritical(stops, _stops, 0);
}

JNIEXPORT void JNICALL Java_flat_backend_SVG_SetPaintBoxGradient(JNIEnv * jEnv, jclass jClass, jlong context, jfloatArray affine, jfloat x, jfloat y, jfloat width, jfloat height, jfloat corners, jfloat blur, jint count, jfloatArray stops, jintArray colors, jint cycleMethod) {
    float* _stops = (float*) jEnv->GetPrimitiveArrayCritical(stops, 0);
    long* _colors = (long*) jEnv->GetPrimitiveArrayCritical(colors, 0);
    float* _affine = (float*) jEnv->GetPrimitiveArrayCritical(affine, 0);
    fvSetPaint((fvContext *) context, fvBoxGradientPaint(_affine, x, y, width, height, corners, blur, count, _stops, _colors, cycleMethod));
    jEnv->ReleasePrimitiveArrayCritical(affine, _affine, 0);
    jEnv->ReleasePrimitiveArrayCritical(colors, _colors, 0);
    jEnv->ReleasePrimitiveArrayCritical(stops, _stops, 0);
}

JNIEXPORT void JNICALL Java_flat_backend_SVG_SetPaintImage(JNIEnv * jEnv, jclass jClass, jlong context, jint imageID, jfloatArray affineImg, jint color) {
    float* _affineImg = (float*) jEnv->GetPrimitiveArrayCritical(affineImg, 0);
    fvSetPaint((fvContext *) context, fvImagePaint(imageID, _affineImg, color));
    jEnv->ReleasePrimitiveArrayCritical(affineImg, _affineImg, 0);
}

JNIEXPORT void JNICALL Java_flat_backend_SVG_SetPaintImageLinearGradient(JNIEnv * jEnv, jclass jClass, jlong context, jint imageID, jfloatArray affineImg, jfloatArray affine, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jint count, jfloatArray stops, jintArray colors, jint cycleMethod) {
    float* _stops = (float*) jEnv->GetPrimitiveArrayCritical(stops, 0);
    long* _colors = (long*) jEnv->GetPrimitiveArrayCritical(colors, 0);
    float* _affineImg = (float*) jEnv->GetPrimitiveArrayCritical(affineImg, 0);
    float* _affine = (float*) jEnv->GetPrimitiveArrayCritical(affine, 0);
    fvSetPaint((fvContext *) context, fvLinearGradientImagePaint(imageID, _affineImg, _affine, x1, y1, x2, y2, count, _stops, _colors, cycleMethod));
    jEnv->ReleasePrimitiveArrayCritical(affine, _affine, 0);
    jEnv->ReleasePrimitiveArrayCritical(affineImg, _affineImg, 0);
    jEnv->ReleasePrimitiveArrayCritical(colors, _colors, 0);
    jEnv->ReleasePrimitiveArrayCritical(stops, _stops, 0);
}

JNIEXPORT void JNICALL Java_flat_backend_SVG_SetPaintImageRadialGradient(JNIEnv * jEnv, jclass jClass, jlong context, jint imageID, jfloatArray affineImg, jfloatArray affine, jfloat x1, jfloat y1, jfloat rIn, jfloat rOut, jint count, jfloatArray stops, jintArray colors, jint cycleMethod) {
    float* _stops = (float*) jEnv->GetPrimitiveArrayCritical(stops, 0);
    long* _colors = (long*) jEnv->GetPrimitiveArrayCritical(colors, 0);
    float* _affineImg = (float*) jEnv->GetPrimitiveArrayCritical(affineImg, 0);
    float* _affine = (float*) jEnv->GetPrimitiveArrayCritical(affine, 0);
    fvSetPaint((fvContext *) context, fvRadialGradientImagePaint(imageID, _affineImg, _affine, x1, y1, rIn, rOut, count, _stops, _colors, cycleMethod));
    jEnv->ReleasePrimitiveArrayCritical(affine, _affine, 0);
    jEnv->ReleasePrimitiveArrayCritical(affineImg, _affineImg, 0);
    jEnv->ReleasePrimitiveArrayCritical(colors, _colors, 0);
    jEnv->ReleasePrimitiveArrayCritical(stops, _stops, 0);
}

JNIEXPORT void JNICALL Java_flat_backend_SVG_SetPaintImageBoxGradient(JNIEnv * jEnv, jclass jClass, jlong context, jint imageID, jfloatArray affineImg, jfloatArray affine, jfloat x, jfloat y, jfloat width, jfloat height, jfloat corners, jfloat blur, jint count, jfloatArray stops, jintArray colors, jint cycleMethod) {
    float* _stops = (float*) jEnv->GetPrimitiveArrayCritical(stops, 0);
    long* _colors = (long*) jEnv->GetPrimitiveArrayCritical(colors, 0);
    float* _affineImg = (float*) jEnv->GetPrimitiveArrayCritical(affineImg, 0);
    float* _affine = (float*) jEnv->GetPrimitiveArrayCritical(affine, 0);
    fvSetPaint((fvContext *) context, fvBoxGradientImagePaint(imageID, _affineImg, _affine, x, y, width, height, corners, blur, count, _stops, _colors, cycleMethod));
    jEnv->ReleasePrimitiveArrayCritical(affine, _affine, 0);
    jEnv->ReleasePrimitiveArrayCritical(affineImg, _affineImg, 0);
    jEnv->ReleasePrimitiveArrayCritical(colors, _colors, 0);
    jEnv->ReleasePrimitiveArrayCritical(stops, _stops, 0);
}
//---------------------------
//         Transforms
//---------------------------
JNIEXPORT void JNICALL Java_flat_backend_SVG_TransformIdentity(JNIEnv * jEnv, jclass jClass, jlong context) {
    fvSetTransform((fvContext*) context, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_TransformSet(JNIEnv * jEnv, jclass jClass, jlong context, jfloat m00, jfloat m01, jfloat m10, jfloat m11, jfloat m02, jfloat m12) {
    fvSetTransform((fvContext*) context, m00, m10, m01, m11, m02, m12);
}

//---------------------------
//          Clipping
//---------------------------
JNIEXPORT void JNICALL Java_flat_backend_SVG_ClearClip(JNIEnv * jEnv, jclass jClass, jlong context, jint enabled) {
    fvClearClip((fvContext*) context, enabled);
}
//---------------------------
//           Paths
//---------------------------
JNIEXPORT void JNICALL Java_flat_backend_SVG_PathBegin(JNIEnv * jEnv, jclass jClass, jlong context, jint type) {
    fvPathOp op = type == 0 ? fvPathOp::FILL :
                  type == 1 ? fvPathOp::STROKE :
                  type == 2 ? fvPathOp::CLIP :
                  type == 3 ? fvPathOp::UNCLIP : fvPathOp::TEXT;
    fvPathBegin((fvContext*)context, op);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_MoveTo(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y) {
    fvPathMoveTo((fvContext *) context, x, y);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_LineTo(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y) {
    fvPathLineTo((fvContext *) context, x, y);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_CubicTo(JNIEnv * jEnv, jclass jClass, jlong context, jfloat c1x, jfloat c1y, jfloat c2x, jfloat c2y, jfloat x, jfloat y) {
    fvPathCubicTo((fvContext *) context, c1x, c1y, c2x, c2y, x, y);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_QuadTo(JNIEnv * jEnv, jclass jClass, jlong context, jfloat cx, jfloat cy, jfloat x, jfloat y) {
    fvPathQuadTo((fvContext *) context, cx, cy, x, y);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_Close(JNIEnv * jEnv, jclass jClass, jlong context) {
    fvPathClose((fvContext *) context);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_PathEnd(JNIEnv * jEnv, jclass jClass, jlong context) {
    fvPathEnd((fvContext *) context);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_Rect(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y, jfloat width, jfloat height) {
    fvRect((fvContext *) context, x, y, width, height);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_Ellipse(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y, jfloat width, jfloat height) {
    fvEllipse((fvContext *) context, x, y, width, height);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_RoundRect(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y, jfloat width, jfloat height, jfloat c1, jfloat c2, jfloat c3, jfloat c4) {
    fvRoundRect((fvContext *) context, x, y, width, height, c1, c2, c3, c4);
}
//---------------------------
//           Text
//---------------------------
JNIEXPORT jlong JNICALL Java_flat_backend_SVG_FontCreate(JNIEnv * jEnv, jclass jClass, jbyteArray data, jfloat size, jint sdf) {
    void* _data = jEnv->GetPrimitiveArrayCritical(data, 0);
    void* font = fvFontCreate(_data, jEnv->GetArrayLength(data), size, sdf == 0 ? 0 : 1);
    jEnv->ReleasePrimitiveArrayCritical(data, _data, 0);
    return (jlong) font;
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_FontLoadAllGlyphs(JNIEnv * jEnv, jclass jClass, jlong font) {
    fvFontLoadAllGlyphs((fvFont *) font);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_FontLoadGlyphs(JNIEnv * jEnv, jclass jClass, jlong font, jstring characters) {
    const char * chars = jEnv->GetStringUTFChars(characters, 0);
    fvFontLoadGlyphs((fvFont*) font, chars, jEnv->GetStringUTFLength(characters));
    jEnv->ReleaseStringUTFChars(characters, chars);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_FontLoadGlyphsBuffer(JNIEnv * jEnv, jclass jClass, jlong font, jobject characters, jint offset, jint length) {
    const char * chars = (const char *) (jEnv->GetDirectBufferAddress(characters) + offset);
    fvFontLoadGlyphs((fvFont*) font, chars, length);
}
JNIEXPORT jint JNICALL Java_flat_backend_SVG_FontGetGlyphs(JNIEnv * jEnv, jclass jClass, jlong font, jstring characters, jfloatArray data) {
    const char *chars = jEnv->GetStringUTFChars(characters, 0);
    float *_data = (float *) jEnv->GetPrimitiveArrayCritical(data, 0);
    jint count = fvFontGetGlyphs((fvFont *) font, chars, jEnv->GetStringUTFLength(characters), _data);
    jEnv->ReleasePrimitiveArrayCritical(data, _data, 0);
    jEnv->ReleaseStringUTFChars(characters, chars);
    return count;
}
JNIEXPORT jint JNICALL Java_flat_backend_SVG_FontGetGlyphsBuffer(JNIEnv * jEnv, jclass jClass, jlong font, jobject characters, jint offset, jint length, jfloatArray data) {
    const char * chars = (const char *) (jEnv->GetDirectBufferAddress(characters) + offset);
    float *_data = (float *) jEnv->GetPrimitiveArrayCritical(data, 0);
    jint count = fvFontGetGlyphs((fvFont *) font, chars, length, _data);
    jEnv->ReleasePrimitiveArrayCritical(data, _data, 0);
    return count;
}
JNIEXPORT jfloat JNICALL Java_flat_backend_SVG_FontGetHeight(JNIEnv * jEnv, jclass jClass, jlong font) {
    jfloat height;
    fvFontGetMetrics((fvFont*) font, 0, 0, &height);
    return height;
}
JNIEXPORT jfloat JNICALL Java_flat_backend_SVG_FontGetAscent(JNIEnv * jEnv, jclass jClass, jlong font) {
    jfloat ascent;
    fvFontGetMetrics((fvFont*) font, &ascent, 0, 0);
    return ascent;
}
JNIEXPORT jfloat JNICALL Java_flat_backend_SVG_FontGetDescent(JNIEnv * jEnv, jclass jClass, jlong font) {
    jfloat descent;
    fvFontGetMetrics((fvFont*) font, 0, &descent, 0);
    return descent;
}
JNIEXPORT jfloat JNICALL Java_flat_backend_SVG_FontGetTextWidth(JNIEnv * jEnv, jclass jClass, jlong font, jstring characters, jfloat scale, jfloat spacing) {
    const char *chars = jEnv->GetStringUTFChars(characters, 0);
    jfloat width = fvFontGetTextWidth((fvFont *) font, chars, jEnv->GetStringUTFLength(characters), scale, spacing);
    jEnv->ReleaseStringUTFChars(characters, chars);
    return width;
}
JNIEXPORT jfloat JNICALL Java_flat_backend_SVG_FontGetTextWidthBuffer(JNIEnv * jEnv, jclass jClass, jlong font, jobject characters, jint offset, jint length, jfloat scale, jfloat spacing) {
    const char * chars = (const char *) (jEnv->GetDirectBufferAddress(characters) + offset);
    return fvFontGetTextWidth((fvFont *) font, chars, length, scale, spacing);
}
JNIEXPORT jint JNICALL Java_flat_backend_SVG_FontGetOffset(JNIEnv * jEnv, jclass jClass, jlong font, jstring characters, jfloat scale, jfloat spacing, jfloat x, jboolean half) {
    const char *chars = jEnv->GetStringUTFChars(characters, 0);
    jint offset = fvFontGetOffset((fvFont *) font, chars, jEnv->GetStringUTFLength(characters), scale, spacing, x, half);
    jEnv->ReleaseStringUTFChars(characters, chars);
    return offset;
}
JNIEXPORT jint JNICALL Java_flat_backend_SVG_FontGetOffsetBuffer(JNIEnv * jEnv, jclass jClass, jlong font, jobject characters, jint offset, jint length, jfloat scale, jfloat spacing, jfloat x, jboolean half) {
    const char * chars = (const char *) (jEnv->GetDirectBufferAddress(characters) + offset);
    return fvFontGetOffset((fvFont *) font, chars, length, scale, spacing, x, half);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_FontDestroy(JNIEnv * jEnv, jclass jClass, jlong font) {
    fvFontDestroy((fvFont*) font);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetFont(JNIEnv * jEnv, jclass jClass, jlong context, jlong font) {
    fvSetFont((fvContext*) context, (fvFont*) font);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetFontScale(JNIEnv * jEnv, jclass jClass, jlong context, jfloat scale) {
    fvSetFontScale((fvContext*) context, scale);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetFontSpacing(JNIEnv * jEnv, jclass jClass, jlong context, jfloat spacing) {
    fvSetFontSpacing((fvContext*) context, spacing);
}
JNIEXPORT jint JNICALL Java_flat_backend_SVG_DrawText(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y, jstring characters, jfloat maxWidth, jint hAlign, jint vAlign) {
    fvHAlign ha = hAlign == 0 ? fvHAlign::LEFT :
                  hAlign == 1 ? fvHAlign::CENTER : fvHAlign::RIGHT;
    fvVAlign va = vAlign == 0 ? fvVAlign::TOP :
                  vAlign == 1 ? fvVAlign::MIDDLE :
                  vAlign == 2 ? fvVAlign::BASELINE : fvVAlign::BOTTOM;
    const char *chars = jEnv->GetStringUTFChars(characters, 0);
    jfloat width = fvText((fvContext*) context, chars, jEnv->GetStringUTFLength(characters), x, y, maxWidth, ha, va);
    jEnv->ReleaseStringUTFChars(characters, chars);
    return width;
}
JNIEXPORT jint JNICALL Java_flat_backend_SVG_DrawTextBuffer(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y, jobject characters, jint offset, jint length, jfloat maxWidth, jint hAlign, jint vAlign) {
    fvHAlign ha = hAlign == 0 ? fvHAlign::LEFT :
                  hAlign == 1 ? fvHAlign::CENTER : fvHAlign::RIGHT;
    fvVAlign va = vAlign == 0 ? fvVAlign::TOP :
                  vAlign == 1 ? fvVAlign::MIDDLE :
                  vAlign == 2 ? fvVAlign::BASELINE : fvVAlign::BOTTOM;
    const char * chars = (const char *) (jEnv->GetDirectBufferAddress(characters) + offset);
    return fvText((fvContext*) context, chars, length, x, y, maxWidth, ha, va);
}