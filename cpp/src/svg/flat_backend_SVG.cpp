//
// Created by Rodrigo on 30/11/2017
//

#include "flat_backend_SVG.h"
#include <glad/glad.h>
#include <iostream>
#include <unordered_map>

#include "flatvectors.h"

#define STB_IMAGE_IMPLEMENTATION
#include "stb_image.h"
//---------------------------
//         Context
//---------------------------
JNIEXPORT jlong JNICALL Java_flat_backend_SVG_Create(JNIEnv * jEnv, jclass jClass) {
    return (jlong) fvCreate();
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_Destroy(JNIEnv * jEnv, jclass jClass, jlong context) {
    fvDestroy((fvContext*) context);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetDebug (JNIEnv * jEnv, jclass jClass, jboolean debug) {
    fvSetDebug(debug == 1);
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

JNIEXPORT void JNICALL Java_flat_backend_SVG_SetStroke(JNIEnv * jEnv, jclass jClass, jlong context, jfloat width, jint cap, jint join, jfloat mitter, jfloatArray dash, jfloat dashPhase) {
    fvCap _cap = cap == 0 ? fvCap::CAP_BUTT : cap == 1 ? fvCap::CAP_SQUARE : fvCap::CAP_ROUND;
    fvJoin _join = join == 0 ? fvJoin::JOIN_BEVEL : join == 1 ? fvJoin::JOIN_MITER : fvJoin::JOIN_ROUND;
    int dashCount = 0;
    float* dashArr = 0;
    if (dash != 0) {
        float *_dash = (float *) jEnv->GetPrimitiveArrayCritical(dash, 0);
        dashCount = jEnv->GetArrayLength(dash);
        dashArr = (float*) malloc(dashCount * sizeof(float));
        memcpy(dashArr, _dash, dashCount * sizeof(float));
        jEnv->ReleasePrimitiveArrayCritical(dash, _dash,0);

        float limit = 0;
        for (int i = 0; i < dashCount; ++i) {
            limit += dashArr[i];
        }
        if (dashCount % 2 == 1) limit *= 2;
        if (dashPhase > 0) {
            dashPhase = dashPhase - ((int)(dashPhase / limit) * limit);
        } else {
            dashPhase = limit - ((-dashPhase) - ((int)((-dashPhase) / limit) * limit));
        }
    }
    fvSetStroker((fvContext *) context, {width, _cap, _join, mitter, dashPhase, dashCount, dashArr});
}

JNIEXPORT void JNICALL Java_flat_backend_SVG_SetPaintColor(JNIEnv * jEnv, jclass jClass, jlong context, jint color) {
    fvSetPaint((fvContext *) context, fvColorPaint(color));
}

JNIEXPORT void JNICALL Java_flat_backend_SVG_SetPaintLinearGradient(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jint count, jfloatArray data, jint cycleMethod) {
    float* _data = (float*) jEnv->GetPrimitiveArrayCritical(data, 0);
    fvSetPaint((fvContext *) context, fvLinearGradientPaint(_data, x1, y1, x2, y2, count, _data + 6, (long*)(_data + 6 + 16), cycleMethod));
    jEnv->ReleasePrimitiveArrayCritical(data, _data, 0);
}

JNIEXPORT void JNICALL Java_flat_backend_SVG_SetPaintRadialGradient(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x1, jfloat y1, jfloat fx, jfloat fy, jfloat rIn, jfloat rOut, jint count, jfloatArray data, jint cycleMethod) {
    float* _data = (float*) jEnv->GetPrimitiveArrayCritical(data, 0);
    fvSetPaint((fvContext *) context, fvRadialGradientPaint(_data, x1, y1, rIn, rOut, fx, fy, count, _data + 6, (long*)(_data + 6 + 16), cycleMethod));
    jEnv->ReleasePrimitiveArrayCritical(data, _data, 0);
}

JNIEXPORT void JNICALL Java_flat_backend_SVG_SetPaintBoxGradient(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y, jfloat w, jfloat h, jfloat corners, jfloat blur, jfloat alpha, jint color, jfloatArray data) {
    float* _data = data == NULL ? 0 : (float*) jEnv->GetPrimitiveArrayCritical(data, 0);
    fvSetPaint((fvContext *) context, fvBoxGradientPaint(_data, x, y, w, h, corners, blur, alpha, color));
    if (data != NULL) {
        jEnv->ReleasePrimitiveArrayCritical(data, _data, 0);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_SVG_SetPaintImage(JNIEnv * jEnv, jclass jClass, jlong context, jint imageID, jint color, jfloatArray data, jint cycleMethod) {
    float* _data = (float*) jEnv->GetPrimitiveArrayCritical(data, 0);
    fvSetPaint((fvContext *) context, fvImagePaint(imageID, _data, color, cycleMethod));
    jEnv->ReleasePrimitiveArrayCritical(data, _data, 0);
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
JNIEXPORT void JNICALL Java_flat_backend_SVG_PathBegin(JNIEnv * jEnv, jclass jClass, jlong context, jint type, jint rule) {
    fvPathOp op = type == 0 ? fvPathOp::FILL :
                  type == 1 ? fvPathOp::STROKE : fvPathOp::CLIP;

    fvWindingRule wr = rule == 0 ? fvWindingRule::EVEN_ODD : fvWindingRule::NON_ZERO;

    fvPathBegin((fvContext*)context, op, wr);
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
JNIEXPORT jlong JNICALL Java_flat_backend_SVG_FontLoad(JNIEnv * jEnv, jclass jClass, jbyteArray data, jfloat size, jint sdf) {
    void* _data = jEnv->GetPrimitiveArrayCritical(data, 0);
    void* font = fvFontLoad(_data, jEnv->GetArrayLength(data), size, sdf == 0 ? 0 : 1);
    jEnv->ReleasePrimitiveArrayCritical(data, _data, 0);
    return (jlong) font;
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_FontUnload(JNIEnv * jEnv, jclass jClass, jlong font) {
    fvFontUnload((void *)font);
}
JNIEXPORT jlong JNICALL Java_flat_backend_SVG_FontPaintCreate(JNIEnv * jEnv, jclass jClass, jlong font) {
    fvFont* fontPaint = fvFontCreate((void*) font);
    return (jlong) fontPaint;
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_FontPaintDestroy(JNIEnv * jEnv, jclass jClass, jlong fontPaint) {
    fvFontDestroy((fvFont*) fontPaint);
}
JNIEXPORT jlong JNICALL Java_flat_backend_SVG_FontPaintGetAtlas(JNIEnv * jEnv, jclass jClass, jlong fontPaint, jintArray size) {
    int w, h;
    long imageID = fvFontGetCurrentAtlas((fvFont*) fontPaint, &w, &h);

    jint imageInfo[2];
    imageInfo[0] = w;
    imageInfo[1] = h;
    jEnv->SetIntArrayRegion(size, 0, 2, imageInfo);

    return imageID;
}
JNIEXPORT jfloatArray JNICALL Java_flat_backend_SVG_FontGetGlyphShape(JNIEnv * jEnv, jclass jClass, jlong font, jint unicode) {
    float* polygon;
    int len;
    fvFontGetGlyphShape((void *) font, unicode, &polygon, &len);

    if (polygon == NULL) {
        return NULL;
    } else {
        jfloatArray imageArray = jEnv->NewFloatArray(len);
        jEnv->SetFloatArrayRegion(imageArray, 0, len, (jfloat *)polygon);
        free(polygon);

        return imageArray;
    }
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_FontGetGlyph(JNIEnv * jEnv, jclass jClass, jlong font, jint codePoint, jfloatArray glyph) {
    float* data = (float *) malloc(8 * sizeof(float));
    fvFontGetGlyph((void *) font, codePoint, data);
    jEnv->SetFloatArrayRegion(glyph, 0, 8, (jfloat *)data);
    free(data);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_FontGetAllCodePoints(JNIEnv * jEnv, jclass jClass, jlong font, jintArray codePoints) {
    jsize size = jEnv->GetArrayLength(codePoints);
    jint* data = (jint*) malloc(size * sizeof(long int));

    fvFontGetAllCodePoints((void *) font, data);

    jEnv->SetIntArrayRegion(codePoints, 0, size, data);

    free(data);
}
JNIEXPORT jfloat JNICALL Java_flat_backend_SVG_FontGetHeight(JNIEnv * jEnv, jclass jClass, jlong font) {
    jfloat height;
    fvFontGetMetrics((void *) font, 0, 0, &height, 0, 0);
    return height;
}
JNIEXPORT jfloat JNICALL Java_flat_backend_SVG_FontGetAscent(JNIEnv * jEnv, jclass jClass, jlong font) {
    jfloat ascent;
    fvFontGetMetrics((void *) font, &ascent, 0, 0, 0, 0);
    return ascent;
}
JNIEXPORT jfloat JNICALL Java_flat_backend_SVG_FontGetDescent(JNIEnv * jEnv, jclass jClass, jlong font) {
    jfloat descent;
    fvFontGetMetrics((void *) font, 0, &descent, 0, 0, 0);
    return descent;
}
JNIEXPORT jfloat JNICALL Java_flat_backend_SVG_FontGetLineGap(JNIEnv * jEnv, jclass jClass, jlong font) {
    jfloat lineGap;
    fvFontGetMetrics((void *) font, 0, 0, 0, &lineGap, 0);
    return lineGap;
}
JNIEXPORT jint JNICALL Java_flat_backend_SVG_FontGetGlyphCount(JNIEnv * jEnv, jclass jClass, jlong font) {
    int glyphCount;
    fvFontGetMetrics((void *) font, 0, 0, 0, 0, &glyphCount);
    return (jint) glyphCount;
}
JNIEXPORT jfloat JNICALL Java_flat_backend_SVG_FontGetTextWidth(JNIEnv * jEnv, jclass jClass, jlong font, jstring characters, jfloat scale, jfloat spacing) {
    const char *chars = jEnv->GetStringUTFChars(characters, 0);
    jfloat width = fvFontGetTextWidth((void *) font, chars, jEnv->GetStringUTFLength(characters), scale, spacing);
    jEnv->ReleaseStringUTFChars(characters, chars);
    return width;
}
JNIEXPORT jfloat JNICALL Java_flat_backend_SVG_FontGetTextWidthBuffer(JNIEnv * jEnv, jclass jClass, jlong font, jobject characters, jint offset, jint length, jfloat scale, jfloat spacing) {
    const char * chars = (const char *) (jEnv->GetDirectBufferAddress(characters)) + offset;
    return fvFontGetTextWidth((void *) font, chars, length, scale, spacing);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_FontGetOffset(JNIEnv * jEnv, jclass jClass, jlong font, jstring characters, jfloat scale, jfloat spacing, jfloat x, jboolean half, jfloatArray cursor) {
    const char *chars = jEnv->GetStringUTFChars(characters, 0);
    float data[2] = {0, 1};
    fvFontGetOffset((void *) font, chars, jEnv->GetStringUTFLength(characters), scale, spacing, x, half, data, data + 1);
    jEnv->ReleaseStringUTFChars(characters, chars);
    jEnv->SetFloatArrayRegion(cursor, 0, 2, (jfloat *)data);

}
JNIEXPORT void JNICALL Java_flat_backend_SVG_FontGetOffsetBuffer(JNIEnv * jEnv, jclass jClass, jlong font, jobject characters, jint offset, jint length, jfloat scale, jfloat spacing, jfloat x, jboolean half, jfloatArray cursor) {
    const char * chars = (const char *) (jEnv->GetDirectBufferAddress(characters)) + offset;
    float data[2] = {0, 1};
    fvFontGetOffset((void *) font, chars, length, scale, spacing, x, half, data, data + 1);
    jEnv->SetFloatArrayRegion(cursor, 0, 2, (jfloat *)data);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetFont(JNIEnv * jEnv, jclass jClass, jlong context, jlong fontPaint) {
    fvSetFont((fvContext*) context, (fvFont *) fontPaint);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetFontScale(JNIEnv * jEnv, jclass jClass, jlong context, jfloat scale) {
    fvSetFontScale((fvContext*) context, scale);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetFontSpacing(JNIEnv * jEnv, jclass jClass, jlong context, jfloat spacing) {
    fvSetFontSpacing((fvContext*) context, spacing);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_SetFontBlur(JNIEnv * jEnv, jclass jClass, jlong context, jfloat blur) {
    fvSetFontBlur((fvContext*) context, blur);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_DrawText(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y, jstring characters, jfloat maxWidth, jfloat maxHeight) {
    const char *chars = jEnv->GetStringUTFChars(characters, 0);
    fvText((fvContext*) context, chars, jEnv->GetStringUTFLength(characters), x, y, maxWidth, maxHeight);
    jEnv->ReleaseStringUTFChars(characters, chars);
}
JNIEXPORT void JNICALL Java_flat_backend_SVG_DrawTextBuffer(JNIEnv * jEnv, jclass jClass, jlong context, jfloat x, jfloat y, jobject characters, jint offset, jint length, jfloat maxWidth, jfloat maxHeight) {
    const char * chars = (const char *) (jEnv->GetDirectBufferAddress(characters)) + offset;
    fvText((fvContext*) context, chars, length, x, y, maxWidth, maxHeight);
}
JNIEXPORT jbyteArray JNICALL Java_flat_backend_SVG_ReadImage(JNIEnv * jEnv, jclass jClass, jbyteArray data, jintArray imageData) {
    jbyte *imageBytes = jEnv->GetByteArrayElements(data, NULL);
    jsize imageSize = jEnv->GetArrayLength(data);

    int width, height, nrChannels;

    unsigned char *imageDataBuffer = stbi_load_from_memory((unsigned char *)imageBytes, imageSize, &width, &height, &nrChannels, 4);

    if (imageDataBuffer == NULL) {
        jEnv->ReleaseByteArrayElements(data, imageBytes, JNI_ABORT);
        return NULL;
    }

    jint imageInfo[3];
    imageInfo[0] = width;
    imageInfo[1] = height;
    imageInfo[2] = nrChannels;

    jEnv->SetIntArrayRegion(imageData, 0, 3, imageInfo);

    jbyteArray imageArray = jEnv->NewByteArray(width * height * 4);
    jEnv->SetByteArrayRegion(imageArray, 0, width * height * 4, (jbyte *)imageDataBuffer);

    stbi_image_free(imageDataBuffer);

    jEnv->ReleaseByteArrayElements(data, imageBytes, JNI_ABORT);

    return imageArray;
}