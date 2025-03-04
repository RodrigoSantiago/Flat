//
// Created by Rodrigo Santiago on 27/11/2017
//

#include "flat_backend_GL.h"
#include <glad/glad.h>
#include <iostream>

void convertImageType(jint &_format, GLenum &dafaFormat, GLenum &dataType) {
    if (_format == GL_RG) {
        dafaFormat = GL_RG;
        dataType = GL_UNSIGNED_BYTE;
    } else if (_format == GL_RED) {
        dafaFormat = GL_RED;
        dataType = GL_UNSIGNED_BYTE;
    } else if (_format == GL_RGB) {
        dafaFormat = GL_RGB;
        dataType = GL_UNSIGNED_BYTE;
    } else if (_format == GL_RGBA) {
        dafaFormat = GL_RGBA;
        dataType = GL_UNSIGNED_BYTE;
    } else if (_format == GL_DEPTH_COMPONENT32F) {
        dafaFormat = GL_DEPTH_COMPONENT;
        dataType = GL_FLOAT;
    } else if (_format == GL_DEPTH_COMPONENT24) {
        dafaFormat = GL_DEPTH_COMPONENT;
        dataType = GL_UNSIGNED_INT;
    } else if (_format == GL_DEPTH_COMPONENT16) {
        dafaFormat = GL_DEPTH_COMPONENT;
        dataType = GL_UNSIGNED_SHORT;
    } else if (_format == GL_DEPTH32F_STENCIL8) {
        dafaFormat = GL_DEPTH_STENCIL;
        dataType = GL_FLOAT_32_UNSIGNED_INT_24_8_REV;
    } else if (_format == GL_DEPTH24_STENCIL8) {
        dafaFormat = GL_DEPTH_STENCIL;
        dataType = GL_UNSIGNED_INT_24_8;
    }
}
void convertElementsType(jint& type) {
    if (type == GL_BYTE) type = GL_UNSIGNED_BYTE;
    else if (type == GL_INT) type = GL_UNSIGNED_INT;
    else if (type == GL_SHORT) type = GL_UNSIGNED_SHORT;
}

JNIEXPORT void JNICALL Java_flat_backend_GL_Flush(JNIEnv * jEnv, jclass jClass) {
    glFlush();
}
JNIEXPORT void JNICALL Java_flat_backend_GL_Finish(JNIEnv * jEnv, jclass jClass) {
    glFinish();
}

JNIEXPORT void JNICALL Java_flat_backend_GL_Clear(JNIEnv * jEnv, jclass jClass, jint maskCB) {
    glClear(maskCB);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_SetClearColor(JNIEnv * jEnv, jclass jClass, jint rgba) {
    glClearColor(((rgba >> 24) & 0xff) / 255.0f,
                 ((rgba >> 16) & 0xff) / 255.0f,
                 ((rgba >> 8) & 0xff) / 255.0f,
                 ((rgba) & 0xff) / 255.0f);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_SetClearDepth(JNIEnv * jEnv, jclass jClass, jdouble mask) {
    glClearDepth(mask);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_SetClearStencil(JNIEnv * jEnv, jclass jClass, jint mask) {
    glClearStencil(mask);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetClearColor(JNIEnv * jEnv, jclass jClass) {
    GLfloat bkColor[4];
    glGetFloatv(GL_COLOR_CLEAR_VALUE, bkColor);
    jint r = (jint) (255 * (bkColor[0] < 0 ? 0 : bkColor[0] >= 0.999f ? 1 : bkColor[0]));
    jint g = (jint) (255 * (bkColor[1] < 0 ? 0 : bkColor[1] >= 0.999f ? 1 : bkColor[1]));
    jint b = (jint) (255 * (bkColor[2] < 0 ? 0 : bkColor[2] >= 0.999f ? 1 : bkColor[2]));
    jint a = (jint) (255 * (bkColor[3] < 0 ? 0 : bkColor[3] >= 0.999f ? 1 : bkColor[3]));
    return (r << 24) | (g << 16) | (b << 8) | a;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetClearDepth(JNIEnv * jEnv, jclass jClass) {
    GLint mask;
    glGetIntegerv(GL_DEPTH_CLEAR_VALUE, &mask);
    return mask;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetClearStencil(JNIEnv * jEnv, jclass jClass) {
    GLint mask;
    glGetIntegerv(GL_STENCIL_CLEAR_VALUE, &mask);
    return mask;
}

JNIEXPORT void JNICALL Java_flat_backend_GL_ReadPixels(JNIEnv * jEnv, jclass jClass, jint x, jint y, jint width, jint height, jint typeDT, jlong offset) {
    glReadPixels(x, y, width, height, GL_RGBA, typeDT, (void *) offset);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_ReadPixelsB(JNIEnv * jEnv, jclass jClass, jint x, jint y, jint width, jint height, jbyteArray data, jint offset) {
    jbyte * pointer = (jbyte *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glReadPixels(x, y, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pointer + offset);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_ReadPixelsS(JNIEnv * jEnv, jclass jClass, jint x, jint y, jint width, jint height, jshortArray data, jint offset) {
    jshort * pointer = (jshort *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glReadPixels(x, y, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pointer + offset);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_ReadPixelsI(JNIEnv * jEnv, jclass jClass, jint x, jint y, jint width, jint height, jintArray data, jint offset) {
    jint * pointer = (jint *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glReadPixels(x, y, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pointer + offset);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_ReadPixelsF(JNIEnv * jEnv, jclass jClass, jint x, jint y, jint width, jint height, jfloatArray data, jint offset) {
    jfloat * pointer = (jfloat *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glReadPixels(x, y, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pointer + offset);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}

JNIEXPORT void JNICALL Java_flat_backend_GL_ReadPixelsBuffer(JNIEnv * jEnv, jclass jClass, jint x, jint y, jint width, jint height, jint typeDT, jobject buffer, jint offset) {
    char * pointer = (char *) jEnv->GetDirectBufferAddress(buffer);
    glReadPixels(x, y, width, height, GL_RGBA, typeDT, pointer + offset);
}
//---------------------------
//         State
//---------------------------

JNIEXPORT void JNICALL Java_flat_backend_GL_SetDebug (JNIEnv * jEnv, jclass jClass, jboolean debug) {
    glPolygonMode(GL_FRONT_AND_BACK, debug ? GL_LINE : GL_FILL);
}

JNIEXPORT jint JNICALL Java_flat_backend_GL_GetError(JNIEnv * jEnv, jclass jClass) {
    return glGetError();
}

JNIEXPORT void JNICALL Java_flat_backend_GL_SetHint(JNIEnv * jEnv, jclass jClass, jint targetHT, jint modeHM) {
    glHint(targetHT, modeHM);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetHint(JNIEnv * jEnv, jclass jClass, jint targetHT) {
    GLint value;
    glGetIntegerv(targetHT, &value);
    return value;
}

JNIEXPORT void JNICALL Java_flat_backend_GL_SetViewport(JNIEnv * jEnv, jclass jClass, jint x, jint y, jint width, jint height) {
    glViewport(x, y, width, height);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetViewportX(JNIEnv * jEnv, jclass jClass) {
    GLint data[4];
    glGetIntegerv(GL_VIEWPORT, data);
    return data[0];
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetViewportY(JNIEnv * jEnv, jclass jClass) {
    GLint data[4];
    glGetIntegerv(GL_VIEWPORT, data);
    return data[1];
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetViewportWidth(JNIEnv * jEnv, jclass jClass) {
    GLint data[4];
    glGetIntegerv(GL_VIEWPORT, data);
    return data[2];
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetViewportHeight(JNIEnv * jEnv, jclass jClass) {
    GLint data[4];
    glGetIntegerv(GL_VIEWPORT, data);
    return data[3];
}

JNIEXPORT void JNICALL Java_flat_backend_GL_EnableScissorTest(JNIEnv * jEnv, jclass jClass, jboolean enable) {
    if  (enable) glEnable(GL_SCISSOR_TEST); else glDisable(GL_SCISSOR_TEST);
}
JNIEXPORT jboolean JNICALL Java_flat_backend_GL_IsScissorTestEnabled(JNIEnv * jEnv, jclass jClass) {
    return glIsEnabled(GL_SCISSOR_TEST);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_SetScissor(JNIEnv * jEnv, jclass jClass, jint x, jint y, jint width, jint height) {
    glScissor(x, y, width, height);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetScissorX(JNIEnv * jEnv, jclass jClass) {
    GLint data[4];
    glGetIntegerv(GL_SCISSOR_BOX, data);
    return data[0];
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetScissorY(JNIEnv * jEnv, jclass jClass) {
    GLint data[4];
    glGetIntegerv(GL_SCISSOR_BOX, data);
    return data[1];
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetScissorWidth(JNIEnv * jEnv, jclass jClass) {
    GLint data[4];
    glGetIntegerv(GL_SCISSOR_BOX, data);
    return data[2];
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetScissorHeight(JNIEnv * jEnv, jclass jClass) {
    GLint data[4];
    glGetIntegerv(GL_SCISSOR_BOX, data);
    return data[3];
}

JNIEXPORT void JNICALL Java_flat_backend_GL_EnableRasterizer(JNIEnv * jEnv, jclass jClass, jboolean enable) {
    if (enable) glDisable(GL_RASTERIZER_DISCARD); else glEnable(GL_RASTERIZER_DISCARD);
}
JNIEXPORT jboolean JNICALL Java_flat_backend_GL_IsRasterizerEnabled(JNIEnv * jEnv, jclass jClass) {
    return !glIsEnabled(GL_RASTERIZER_DISCARD);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_SetPixelStore(JNIEnv * jEnv, jclass jClass, jint targetPS, jint value) {
    glPixelStorei(targetPS, value);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetPixelStore(JNIEnv * jEnv, jclass jClass, jint targetPS) {
    GLint data;
    glGetIntegerv(targetPS, &data);
    return data;
}
JNIEXPORT void JNICALL Java_flat_backend_GL_SetColorMask(JNIEnv * jEnv, jclass jClass, jboolean r, jboolean g, jboolean b, jboolean a) {
    glColorMask(r,g,b,a);
}
JNIEXPORT jboolean JNICALL Java_flat_backend_GL_GetColorMaskR(JNIEnv * jEnv, jclass jClass) {
    GLboolean data[4];
    glGetBooleanv(GL_COLOR_WRITEMASK, data);
    return data[0];
}
JNIEXPORT jboolean JNICALL Java_flat_backend_GL_GetColorMaskG(JNIEnv * jEnv, jclass jClass) {
    GLboolean data[4];
    glGetBooleanv(GL_COLOR_WRITEMASK, data);
    return data[1];
}
JNIEXPORT jboolean JNICALL Java_flat_backend_GL_GetColorMaskB(JNIEnv * jEnv, jclass jClass) {
    GLboolean data[4];
    glGetBooleanv(GL_COLOR_WRITEMASK, data);
    return data[2];
}
JNIEXPORT jboolean JNICALL Java_flat_backend_GL_GetColorMaskA(JNIEnv * jEnv, jclass jClass) {
    GLboolean data[4];
    glGetBooleanv(GL_COLOR_WRITEMASK, data);
    return data[3];
}

JNIEXPORT void JNICALL Java_flat_backend_GL_EnableDepthTest(JNIEnv * jEnv, jclass jClass, jboolean enable) {
    if (enable) glEnable(GL_DEPTH_TEST); else glDisable(GL_DEPTH_TEST);
}
JNIEXPORT jboolean JNICALL Java_flat_backend_GL_IsDepthTestEnabled(JNIEnv * jEnv, jclass jClass) {
    return glIsEnabled(GL_DEPTH_TEST);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_SetDepthMask(JNIEnv * jEnv, jclass jClass, jboolean mask) {
    glDepthMask(mask);
}
JNIEXPORT jboolean JNICALL Java_flat_backend_GL_GetDepthMask(JNIEnv * jEnv, jclass jClass) {
    GLboolean data;
    glGetBooleanv(GL_DEPTH_WRITEMASK, &data);
    return data;
}
JNIEXPORT void JNICALL Java_flat_backend_GL_SetDepthFunction(JNIEnv * jEnv, jclass jClass, jint functionMF) {
    glDepthFunc(functionMF);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetDepthFunction(JNIEnv * jEnv, jclass jClass) {
    GLint data;
    glGetIntegerv(GL_DEPTH_FUNC, &data);
    return data;
}
JNIEXPORT void JNICALL Java_flat_backend_GL_SetDepthRange(JNIEnv * jEnv, jclass jClass, jdouble nearValue, jdouble farValue) {
    glDepthRange(nearValue, farValue);
}
JNIEXPORT jdouble JNICALL Java_flat_backend_GL_GetDepthRangeNear(JNIEnv * jEnv, jclass jClass) {
    GLdouble data[2];
    glGetDoublev(GL_DEPTH_RANGE, data);
    return data[0];
}
JNIEXPORT jdouble JNICALL Java_flat_backend_GL_GetDepthRangeFar(JNIEnv * jEnv, jclass jClass) {
    GLdouble data[2];
    glGetDoublev(GL_DEPTH_RANGE, data);
    return data[1];
}

JNIEXPORT void JNICALL Java_flat_backend_GL_EnableStencilTest(JNIEnv * jEnv, jclass jClass, jboolean enable) {
    if (enable) glEnable(GL_STENCIL_TEST); else glDisable(GL_STENCIL_TEST);
}
JNIEXPORT jboolean JNICALL Java_flat_backend_GL_IsStencilTestEnabled(JNIEnv * jEnv, jclass jClass) {
    return glIsEnabled(GL_STENCIL_TEST);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_SetStencilMask(JNIEnv * jEnv, jclass jClass, jint faceFC, jint mask) {
    glStencilMaskSeparate(faceFC, mask);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetStencilMask(JNIEnv * jEnv, jclass jClass, jint faceFG) {
    GLint data;
    if (faceFG == GL_BACK) glGetIntegerv(GL_STENCIL_BACK_WRITEMASK, &data);
    else glGetIntegerv(GL_STENCIL_WRITEMASK, &data);
    return data;
    
}
JNIEXPORT void JNICALL Java_flat_backend_GL_SetStencilFunction(JNIEnv * jEnv, jclass jClass, jint faceFC, jint functionMF, jint ref, jint mask) {
    glStencilFuncSeparate(faceFC, functionMF, ref, mask);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetStencilFunction(JNIEnv * jEnv, jclass jClass, jint faceFG) {
    GLint data;
    if (faceFG == GL_BACK) glGetIntegerv(GL_STENCIL_BACK_FUNC, &data);
    else glGetIntegerv(GL_STENCIL_FUNC, &data);
    return data;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetStencilFunctionRef(JNIEnv * jEnv, jclass jClass, jint faceFG) {
    GLint data;
    if (faceFG == GL_BACK) glGetIntegerv(GL_STENCIL_BACK_REF, &data);
    else glGetIntegerv(GL_STENCIL_REF, &data);
    return data;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetStencilFunctionMask(JNIEnv * jEnv, jclass jClass, jint faceFG) {
    GLint data;
    if (faceFG == GL_BACK) glGetIntegerv(GL_STENCIL_BACK_VALUE_MASK, &data);
    else glGetIntegerv(GL_STENCIL_VALUE_MASK, &data);
    return data;
}
JNIEXPORT void JNICALL Java_flat_backend_GL_SetStencilOperation(JNIEnv * jEnv, jclass jClass, jint faceFC, jint stencilFailMO, jint depthFailMO, jint depthPassMO) {
    glStencilOpSeparate(faceFC, stencilFailMO, depthFailMO, depthPassMO);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetStencilOperationStencilFail(JNIEnv * jEnv, jclass jClass, jint faceFG) {
    GLint data;
    if (faceFG == GL_BACK) glGetIntegerv(GL_STENCIL_BACK_FAIL, &data);
    else glGetIntegerv(GL_STENCIL_FAIL, &data);
    return data;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetStencilOperationDepthFail(JNIEnv * jEnv, jclass jClass, jint faceFG) {
    GLint data;
    if (faceFG == GL_BACK) glGetIntegerv(GL_STENCIL_BACK_PASS_DEPTH_PASS, &data);
    else glGetIntegerv(GL_STENCIL_PASS_DEPTH_PASS, &data);
    return data;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetStencilOperationDepthPass(JNIEnv * jEnv, jclass jClass, jint faceFG) {
    GLint data;
    if (faceFG == GL_BACK) glGetIntegerv(GL_STENCIL_BACK_PASS_DEPTH_FAIL, &data);
    else glGetIntegerv(GL_STENCIL_PASS_DEPTH_FAIL, &data);
    return data;
}

JNIEXPORT void JNICALL Java_flat_backend_GL_EnableBlend(JNIEnv * jEnv, jclass jClass, jboolean enable) {
    if (enable) glEnable(GL_BLEND); else glDisable(GL_BLEND);
}
JNIEXPORT jboolean JNICALL Java_flat_backend_GL_IsBlendEnabled(JNIEnv * jEnv, jclass jClass) {
    return glIsEnabled(GL_BLEND);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_SetBlendFunction(JNIEnv * jEnv, jclass jClass, jint rgbSrcBF, jint rgbDstBF, jint alphaSrcBF, jint alphaDstBF) {
    glBlendFuncSeparate(rgbSrcBF, rgbDstBF, alphaSrcBF, alphaDstBF);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetBlendFunctionSrcRGB(JNIEnv * jEnv, jclass jClass) {
    GLint data;
    glGetIntegerv(GL_BLEND_SRC_RGB, &data);
    return data;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetBlendFunctionDstRGB(JNIEnv * jEnv, jclass jClass) {
    GLint data;
    glGetIntegerv(GL_BLEND_DST_RGB, &data);
    return data;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetBlendFunctionSrcAlpha(JNIEnv * jEnv, jclass jClass) {
    GLint data;
    glGetIntegerv(GL_BLEND_SRC_ALPHA, &data);
    return data;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetBlendFunctionDstAlpha(JNIEnv * jEnv, jclass jClass) {
    GLint data;
    glGetIntegerv(GL_BLEND_DST_ALPHA, &data);
    return data;
}
JNIEXPORT void JNICALL Java_flat_backend_GL_SetBlendEquation(JNIEnv * jEnv, jclass jClass, jint rgbBE, jint alphaBE) {
    glBlendEquationSeparate(rgbBE, alphaBE);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetBlendEquationRGB(JNIEnv * jEnv, jclass jClass) {
    GLint data;
    glGetIntegerv(GL_BLEND_EQUATION_RGB, &data);
    return data;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetBlendEquationAlpha(JNIEnv * jEnv, jclass jClass) {
    GLint data;
    glGetIntegerv(GL_BLEND_EQUATION_ALPHA, &data);
    return data;
}
JNIEXPORT void JNICALL Java_flat_backend_GL_SetBlendColor(JNIEnv * jEnv, jclass jClass, jint rgba) {
    glBlendColor(((rgba >> 24) & 0xff) / 255.0f,
                 ((rgba >> 16) & 0xff) / 255.0f,
                 ((rgba >> 8) & 0xff) / 255.0f,
                 ((rgba) & 0xff) / 255.0f);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetBlendColor(JNIEnv * jEnv, jclass jClass) {
    GLfloat bkColor[4];
    glGetFloatv(GL_BLEND_COLOR, bkColor);
    jint r = (jint) (255 * (bkColor[0] < 0 ? 0 : bkColor[0] >= 0.999f ? 1 : bkColor[0]));
    jint g = (jint) (255 * (bkColor[1] < 0 ? 0 : bkColor[1] >= 0.999f ? 1 : bkColor[1]));
    jint b = (jint) (255 * (bkColor[2] < 0 ? 0 : bkColor[2] >= 0.999f ? 1 : bkColor[2]));
    jint a = (jint) (255 * (bkColor[3] < 0 ? 0 : bkColor[3] >= 0.999f ? 1 : bkColor[3]));
    return (r << 24) | (g << 16) | (b << 8) | a;
}

JNIEXPORT void JNICALL Java_flat_backend_GL_EnableCullface(JNIEnv * jEnv, jclass jClass, jboolean enable) {
    if (enable) glEnable(GL_CULL_FACE); else glDisable(GL_CULL_FACE);
}
JNIEXPORT jboolean JNICALL Java_flat_backend_GL_IsCullfaceEnabled(JNIEnv * jEnv, jclass jClass) {
    return glIsEnabled(GL_CULL_FACE);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_SetCullface(JNIEnv * jEnv, jclass jClass, jint faceFC) {
    glCullFace(faceFC);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetCullface(JNIEnv * jEnv, jclass jClass) {
    GLint data;
    glGetIntegerv(GL_CULL_FACE_MODE, &data);
    return data;
}
JNIEXPORT void JNICALL Java_flat_backend_GL_SetFrontFace(JNIEnv * jEnv, jclass jClass, jint frontFaceFF) {
    glFrontFace(frontFaceFF);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetFrontFace(JNIEnv * jEnv, jclass jClass) {
    GLint data;
    glGetIntegerv(GL_FRONT_FACE, &data);
    return data;
}

JNIEXPORT void JNICALL Java_flat_backend_GL_EnableMultisample(JNIEnv * jEnv, jclass jClass, jboolean enable) {
    if (enable) glEnable(GL_MULTISAMPLE); else glDisable(GL_MULTISAMPLE);
}
JNIEXPORT jboolean JNICALL Java_flat_backend_GL_IsMultisampleEnabled(JNIEnv * jEnv, jclass jClass) {
    return glIsEnabled(GL_MULTISAMPLE);
}

JNIEXPORT void JNICALL Java_flat_backend_GL_SetLineWidth(JNIEnv * jEnv, jclass jClass, jfloat width) {
    glLineWidth(width);
}
JNIEXPORT jfloat JNICALL Java_flat_backend_GL_GetLineWidth(JNIEnv * jEnv, jclass jClass) {
    GLfloat data;
    glGetFloatv(GL_LINE_WIDTH, &data);
    return data;
}
JNIEXPORT void JNICALL Java_flat_backend_GL_DrawArrays(JNIEnv * jEnv, jclass jClass, jint vertexModeVM, jint first, jint count, jint instances) {
    if (instances > 1) {
        glDrawArraysInstanced(vertexModeVM, first, count, instances);
    } else {
        glDrawArrays(vertexModeVM, first, count);
    }
}
JNIEXPORT void JNICALL Java_flat_backend_GL_DrawElements(JNIEnv * jEnv, jclass jClass, jint vertexModeVM, jint count, jint instances, jint typeDT, jlong offset) {
    convertElementsType(typeDT);
    if (instances > 1) {
        glDrawElementsInstanced(vertexModeVM, count, typeDT, (void *)offset, instances);
    } else {
        glDrawElements(vertexModeVM, count, typeDT, (void *)offset);
    }
}
JNIEXPORT void JNICALL Java_flat_backend_GL_DrawElementsB(JNIEnv * jEnv, jclass jClass, jint vertexModeVM, jint count, jint instances, jbyteArray indices, jint offset) {
    jbyte * pointer = (jbyte *) jEnv->GetPrimitiveArrayCritical(indices, 0);
    if (instances > 1) {
        glDrawElementsInstanced(vertexModeVM, count, GL_UNSIGNED_BYTE, pointer + offset, instances);
    } else {
        glDrawElements(vertexModeVM, count, GL_UNSIGNED_BYTE, pointer + offset);
    }
    jEnv->ReleasePrimitiveArrayCritical(indices, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_DrawElementsS(JNIEnv * jEnv, jclass jClass, jint vertexModeVM, jint count, jint instances, jshortArray indices, jint offset) {
    jshort * pointer = (jshort *) jEnv->GetPrimitiveArrayCritical(indices, 0);
    if (instances > 1) {
        glDrawElementsInstanced(vertexModeVM, count, GL_UNSIGNED_SHORT, pointer + offset, instances);
    } else {
        glDrawElements(vertexModeVM, count, GL_UNSIGNED_SHORT, pointer + offset);
    }
    jEnv->ReleasePrimitiveArrayCritical(indices, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_DrawElementsI(JNIEnv * jEnv, jclass jClass, jint vertexModeVM, jint count, jint instances, jintArray indices, jint offset) {
    jint * pointer = (jint *) jEnv->GetPrimitiveArrayCritical(indices, 0);
    if (instances > 1) {
        glDrawElementsInstanced(vertexModeVM, count, GL_UNSIGNED_INT, pointer + offset, instances);
    } else {
        glDrawElements(vertexModeVM, count, GL_UNSIGNED_INT, pointer + offset);
    }
    jEnv->ReleasePrimitiveArrayCritical(indices, pointer, 0);
}

JNIEXPORT void JNICALL Java_flat_backend_GL_DrawElementsBuffer(JNIEnv * jEnv, jclass jClass, jint vertexModeVM, jint count, jint instances, jint typeDT, jobject buffer, jint offset) {
    convertElementsType(typeDT);
    char * pointer = (char *) jEnv->GetDirectBufferAddress(buffer);
    if (instances > 1) {
        glDrawElementsInstanced(vertexModeVM, count, typeDT, pointer + offset, instances);
    } else {
        glDrawElements(vertexModeVM, count, typeDT, pointer + offset);
    }
}
//---------------------------
//        Frame Buffer
//---------------------------
JNIEXPORT jint JNICALL Java_flat_backend_GL_FrameBufferCreate(JNIEnv * jEnv, jclass jClass) {
    GLuint id;
    glGenFramebuffers(1, &id);
    return id;
}
JNIEXPORT void JNICALL Java_flat_backend_GL_FrameBufferDestroy(JNIEnv * jEnv, jclass jClass, jint id) {
    GLuint did = id;
    glDeleteFramebuffers(1, &did);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_FrameBufferBind(JNIEnv * jEnv, jclass jClass, jint trgFB, jint id) {
    glBindFramebuffer(trgFB, id);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_FrameBufferGetBound(JNIEnv * jEnv, jclass jClass, jint trgFB) {
    GLint id = 0;
    if (trgFB == GL_FRAMEBUFFER) {
        glGetIntegerv(GL_FRAMEBUFFER_BINDING, &id);
    } else if (trgFB == GL_DRAW_FRAMEBUFFER) {
        glGetIntegerv(GL_DRAW_FRAMEBUFFER_BINDING, &id);
    } else if (trgFB == GL_READ_FRAMEBUFFER) {
        glGetIntegerv(GL_READ_FRAMEBUFFER_BINDING, &id);
    }
    return id;
}
JNIEXPORT void JNICALL Java_flat_backend_GL_FrameBufferBlit(JNIEnv * jEnv, jclass jClass, jint srcX, jint srcY, jint srcW, jint srcH, jint dstX, jint dstY, jint dstW, jint dstH, jint bitmaskBM, jint filterBF) {
    glBlitFramebuffer(srcX, srcY, srcX + srcW, srcY + srcH, dstX, dstY, dstX + dstW, dstY + dstH, bitmaskBM, filterBF);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_FrameBufferTexture2D(JNIEnv * jEnv, jclass jClass, jint trgFB, jint attFA, jint texTypeTT, jint textureId, jint level) {
    glFramebufferTexture2D(trgFB, attFA, texTypeTT, textureId, level);
}

JNIEXPORT void JNICALL Java_flat_backend_GL_FrameBufferTextureMultisample(JNIEnv * jEnv, jclass jClass, jint trgFB, jint attFA, jint textureId) {
    glFramebufferTexture2D(trgFB, attFA, GL_TEXTURE_2D_MULTISAMPLE, textureId, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_FrameBufferRenderBuffer(JNIEnv * jEnv, jclass jClass, jint trgFB, jint attFA, jint renderBufferId) {
    glFramebufferRenderbuffer(trgFB, attFA, GL_RENDERBUFFER, renderBufferId);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_FrameBufferGetStatus(JNIEnv * jEnv, jclass jClass, jint trgFB) {
    return glCheckFramebufferStatus(trgFB);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_FrameBufferGetObjectType(JNIEnv * jEnv, jclass jClass, jint trgFB, jint attFA) {
    GLint data;
    glGetFramebufferAttachmentParameteriv(trgFB, attFA, GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE, &data);
    return data;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_FrameBufferGetObjectId(JNIEnv * jEnv, jclass jClass, jint trgFB, jint attFA) {
    GLint data;
    glGetFramebufferAttachmentParameteriv(trgFB, attFA, GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME, &data);
    return data;
}

JNIEXPORT void JNICALL Java_flat_backend_GL_FrameBufferGetPixelDataSize(JNIEnv * jEnv, jclass jClass, jint trgFB, jint attFA, jintArray data6) {
    GLint data;
    jint val;
    glGetFramebufferAttachmentParameteriv(trgFB, attFA, GL_FRAMEBUFFER_ATTACHMENT_RED_SIZE, &data);
    val = data;
    jEnv->SetIntArrayRegion(data6, 0, 1, &val);
    glGetFramebufferAttachmentParameteriv(trgFB, attFA, GL_FRAMEBUFFER_ATTACHMENT_GREEN_SIZE, &data);
    val = data;
    jEnv->SetIntArrayRegion(data6, 1, 1, &val);
    glGetFramebufferAttachmentParameteriv(trgFB, attFA, GL_FRAMEBUFFER_ATTACHMENT_BLUE_SIZE, &data);
    val = data;
    jEnv->SetIntArrayRegion(data6, 2, 1, &val);
    glGetFramebufferAttachmentParameteriv(trgFB, attFA, GL_FRAMEBUFFER_ATTACHMENT_ALPHA_SIZE, &data);
    val = data;
    jEnv->SetIntArrayRegion(data6, 3, 1, &val);
    glGetFramebufferAttachmentParameteriv(trgFB, attFA, GL_FRAMEBUFFER_ATTACHMENT_DEPTH_SIZE, &data);
    val = data;
    jEnv->SetIntArrayRegion(data6, 4, 1, &val);
    glGetFramebufferAttachmentParameteriv(trgFB, attFA, GL_FRAMEBUFFER_ATTACHMENT_STENCIL_SIZE, &data);
    val = data;
    jEnv->SetIntArrayRegion(data6, 5, 1, &val);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_FrameBufferSetTargets(JNIEnv *jEnv, jclass jClass, jint c0FA, jint c1FA, jint c2FA, jint c3FA, jint c4FA, jint c5FA, jint c6FA, jint c7FA) {
    GLint id;
    glGetIntegerv(GL_DRAW_FRAMEBUFFER_BINDING, &id);
    GLuint arr[8] = {(GLuint)(id == 0 && c0FA != 0 ? GL_BACK_LEFT : c0FA),
                     (GLuint)c1FA, (GLuint)c2FA, (GLuint)c3FA,
                     (GLuint)c4FA, (GLuint)c5FA, (GLuint)c6FA, (GLuint)c7FA };
    glDrawBuffers(8, arr);
}

JNIEXPORT void JNICALL Java_flat_backend_GL_FrameBufferGetTargets(JNIEnv * jEnv, jclass, jintArray data8) {
    void * pointer = jEnv->GetPrimitiveArrayCritical(data8, 0);
    jint * arr = (jint *) pointer;
    GLint data;
    for (GLuint i = 0; i < 8; i++) {
        glGetIntegerv(GL_DRAW_BUFFER0 + i, &data);
        arr[i] = data;
    }
    GLint id;
    glGetIntegerv(GL_DRAW_FRAMEBUFFER_BINDING, &id);
    if (id == 0) {
        glGetIntegerv(GL_DRAW_BUFFER0, &data);
        arr[0] = (data == GL_BACK_LEFT || data == GL_BACK) ? GL_COLOR_ATTACHMENT0 : GL_NONE;
    }
    jEnv->ReleasePrimitiveArrayCritical(data8, pointer, 0);
}
//---------------------------
//        Render Buffer
//---------------------------
JNIEXPORT jint JNICALL Java_flat_backend_GL_RenderBufferCreate(JNIEnv * jEnv, jclass jClass) {
    GLuint id;
    glGenRenderbuffers(1, &id);
    return id;
}
JNIEXPORT void JNICALL Java_flat_backend_GL_RenderBufferDestroy(JNIEnv * jEnv, jclass jClass, jint id) {
    GLuint did = id;
    glDeleteRenderbuffers(1, &did);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_RenderBufferBind(JNIEnv * jEnv, jclass jClass, jint id) {
    glBindRenderbuffer(GL_RENDERBUFFER, id);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_RenderBufferGetBound(JNIEnv * jEnv, jclass jClass) {
    GLint id = 0;
    glGetIntegerv(GL_RENDERBUFFER_BINDING, &id);
    return id;
}
JNIEXPORT void JNICALL Java_flat_backend_GL_RenderBufferStorage(JNIEnv * jEnv, jclass jClass, jint frormatTF, jint width, jint height) {
    glRenderbufferStorage(GL_RENDERBUFFER, frormatTF, width, height);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_RenderBufferStorageMultsample(JNIEnv * jEnv, jclass jClass, jint frormatTF, jint samples, jint width, jint height) {
    glRenderbufferStorageMultisample(GL_RENDERBUFFER, samples, frormatTF, width, height);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_RenderBufferGetFormat(JNIEnv * jEnv, jclass jClass) {
    GLint data;
    glGetRenderbufferParameteriv(GL_RENDERBUFFER, GL_RENDERBUFFER_INTERNAL_FORMAT, &data);
    return data;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_RenderBufferGetWidth(JNIEnv * jEnv, jclass jClass) {
    GLint data;
    glGetRenderbufferParameteriv(GL_RENDERBUFFER, GL_RENDERBUFFER_WIDTH, &data);
    return data;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_RenderBufferGetHeight(JNIEnv * jEnv, jclass jClass) {
    GLint data;
    glGetRenderbufferParameteriv(GL_RENDERBUFFER, GL_RENDERBUFFER_HEIGHT, &data);
    return data;
}

//---------------------------
//         Textures
//---------------------------
JNIEXPORT void JNICALL Java_flat_backend_GL_SetActiveTexture(JNIEnv * jEnv, jclass jClass, jint pos) {
    glActiveTexture(GL_TEXTURE0 + static_cast<GLuint>(pos));
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_GetActiveTexture(JNIEnv * jEnv, jclass jClass) {
    GLint id;
    glGetIntegerv(GL_ACTIVE_TEXTURE, &id);
    return id;
}

JNIEXPORT jint JNICALL Java_flat_backend_GL_TextureCreate(JNIEnv * jEnv, jclass jClass) {
    GLuint id;
    glGenTextures(1, &id);
    return id;
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TextureDestroy(JNIEnv * jEnv, jclass jClass, jint id) {
    GLuint did = id;
    glDeleteTextures(1, &did);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TextureBind(JNIEnv * jEnv, jclass jClass, jint trgTB, jint id) {
    glBindTexture(trgTB, id);
}

JNIEXPORT jint JNICALL Java_flat_backend_GL_TextureGetBound(JNIEnv *jEnv, jclass jClass, jint trgTB) {
    GLint id = 0;
    if (trgTB == GL_TEXTURE_2D) {
        glGetIntegerv(GL_TEXTURE_BINDING_2D, &id);
    } else if (trgTB == GL_TEXTURE_CUBE_MAP) {
        glGetIntegerv(GL_TEXTURE_BINDING_CUBE_MAP, &id);
    } else if (trgTB == GL_TEXTURE_2D_MULTISAMPLE) {
        glGetIntegerv(GL_TEXTURE_BINDING_2D_MULTISAMPLE, &id);
    } else if (trgTB == GL_TEXTURE_3D) {
        glGetIntegerv(GL_TEXTURE_BINDING_3D, &id);
    } else if (trgTB == GL_TEXTURE_2D_ARRAY) {
        glGetIntegerv(GL_TEXTURE_BINDING_2D_ARRAY, &id);
    }
    return id;
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TextureGenerateMipmap(JNIEnv * jEnv, jclass jClass, jint id) {
    glGenerateMipmap(id);
}

JNIEXPORT void JNICALL Java_flat_backend_GL_TextureMultisample(JNIEnv *jEnv, jclass jClass, jint samples, jint formatTF, jint width, jint height, jboolean fixedLocations) {
    glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, samples, formatTF, width, height, fixedLocations);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TextureCopy(JNIEnv * jEnv, jclass jClass, jint trgTT, jint level, jint formatTF, jint x, jint y, jint width, jint height, jint border) {
    glCopyTexImage2D(trgTT, level, formatTF, x, y, width, height, border);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TextureSubCopy(JNIEnv * jEnv, jclass jClass, jint trgTT, jint level, jint xoffset, jint yoffset, jint x, jint y, jint width, jint height) {
    glCopyTexSubImage2D(trgTT, level, xoffset, yoffset, x, y, width, height);
}

JNIEXPORT void JNICALL Java_flat_backend_GL_TextureData(JNIEnv * jEnv, jclass jClass, jint trgTT, jint level, jint formatTF, jint width, jint height, jint border, jlong offset) {
    GLenum dafaFormat, dataType;
    convertImageType(formatTF, dafaFormat, dataType);
    glTexImage2D(trgTT, level, formatTF, width, height, border, dafaFormat, dataType, (void *) offset);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TextureDataB(JNIEnv * jEnv, jclass jClass, jint trgTT, jint level, jint formatTF, jint width, jint height, jint border, jbyteArray data, jint offset) {
    GLenum dafaFormat, dataType;
    convertImageType(formatTF, dafaFormat, dataType);
    jbyte * pointer = (jbyte *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glTexImage2D(trgTT, level, formatTF, width, height, border, dafaFormat, dataType, pointer + offset);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TextureDataS(JNIEnv * jEnv, jclass jClass, jint trgTT, jint level, jint formatTF, jint width, jint height, jint border, jshortArray data, jint offset) {
    GLenum dafaFormat, dataType;
    convertImageType(formatTF, dafaFormat, dataType);

    jshort * pointer = (jshort *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glTexImage2D(trgTT, level, formatTF, width, height, border, dafaFormat, dataType, pointer + offset);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TextureDataI(JNIEnv * jEnv, jclass jClass, jint trgTT, jint level, jint formatTF, jint width, jint height, jint border, jintArray data, jint offset) {
    GLenum dafaFormat, dataType;
    convertImageType(formatTF, dafaFormat, dataType);

    jint * pointer = (jint *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glTexImage2D(trgTT, level, formatTF, width, height, border, dafaFormat, dataType, pointer + offset);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TextureDataF(JNIEnv * jEnv, jclass jClass, jint trgTT, jint level, jint formatTF, jint width, jint height, jint border, jfloatArray data, jint offset) {
    GLenum dafaFormat, dataType;
    convertImageType(formatTF, dafaFormat, dataType);

    jfloat * pointer = (jfloat *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glTexImage2D(trgTT, level, formatTF, width, height, border, dafaFormat, dataType, pointer + offset);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TextureDataBuffer(JNIEnv * jEnv, jclass jClass, jint trgTT, jint level, jint formatTF, jint width, jint height, jint border, jobject buffer, jint offset) {
    GLenum dafaFormat, dataType;
    convertImageType(formatTF, dafaFormat, dataType);

    if (buffer == nullptr) {
        glTexImage2D(trgTT, level, formatTF, width, height, border, dafaFormat, dataType, nullptr);
    } else {
        char *pointer = (char *) jEnv->GetDirectBufferAddress(buffer);
        glTexImage2D(trgTT, level, formatTF, width, height, border, dafaFormat, dataType, pointer + offset);
    }
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TexGetImageB(JNIEnv * jEnv, jclass jClass, jint trgTT, jint level, jint formatTF, jbyteArray data, jint offset) {
    GLenum dafaFormat, dataType;
    convertImageType(formatTF, dafaFormat, dataType);

    jbyte * pointer = (jbyte *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glGetTexImage(trgTT, level, formatTF, dataType, pointer + offset);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TexGetImageS(JNIEnv * jEnv, jclass jClass, jint trgTT, jint level, jint formatTF, jshortArray data, jint offset) {
    GLenum dafaFormat, dataType;
    convertImageType(formatTF, dafaFormat, dataType);

    jshort * pointer = (jshort *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glGetTexImage(trgTT, level, formatTF, dataType, pointer + offset);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TexGetImageI(JNIEnv * jEnv, jclass jClass, jint trgTT, jint level, jint formatTF, jintArray data, jint offset) {
    GLenum dafaFormat, dataType;
    convertImageType(formatTF, dafaFormat, dataType);

    jint * pointer = (jint *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glGetTexImage(trgTT, level, formatTF, dataType, pointer + offset);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TexGetImageF(JNIEnv * jEnv, jclass jClass, jint trgTT, jint level, jint formatTF, jfloatArray data, jint offset) {
    GLenum dafaFormat, dataType;
    convertImageType(formatTF, dafaFormat, dataType);

    jfloat * pointer = (jfloat *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glGetTexImage(trgTT, level, formatTF, dataType, pointer + offset);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TexGetImageBuffer(JNIEnv * jEnv, jclass jClass, jint trgTT, jint level, jint formatTF, jobject buffer, jint offset) {
    GLenum dafaFormat, dataType;
    convertImageType(formatTF, dafaFormat, dataType);

    char *pointer = (char *) jEnv->GetDirectBufferAddress(buffer);
    glGetTexImage(trgTT, level, formatTF, dataType, pointer + offset);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TextureSubData(JNIEnv * jEnv, jclass jClass, jint trgTT, jint level, jint x, jint y, jint width, jint height, jint dataFormatTF, jlong offset) {
    GLenum dafaFormat, dataType;
    convertImageType(dataFormatTF, dafaFormat, dataType);

    glTexSubImage2D(trgTT, level, x, y, width, height, dafaFormat, dataType, (void *) offset);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TextureSubDataB(JNIEnv * jEnv, jclass jClass, jint trgTT, jint level, jint x, jint y, jint width, jint height, jint dataFormatTF, jbyteArray data, jint offset) {
    GLenum dafaFormat, dataType;
    convertImageType(dataFormatTF, dafaFormat, dataType);

    jbyte * pointer = (jbyte *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glTexSubImage2D(trgTT, level, x, y, width, height, dafaFormat, dataType, pointer + offset);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TextureSubDataS(JNIEnv * jEnv, jclass jClass, jint trgTT, jint level, jint x, jint y, jint width, jint height, jint dataFormatTF, jshortArray data, jint offset) {
    GLenum dafaFormat, dataType;
    convertImageType(dataFormatTF, dafaFormat, dataType);

    jshort * pointer = (jshort *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glTexSubImage2D(trgTT, level, x, y, width, height, dafaFormat, dataType, pointer + offset);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TextureSubDataI(JNIEnv * jEnv, jclass jClass, jint trgTT, jint level, jint x, jint y, jint width, jint height, jint dataFormatTF, jintArray data, jint offset) {
    GLenum dafaFormat, dataType;
    convertImageType(dataFormatTF, dafaFormat, dataType);

    jint * pointer = (jint *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glTexSubImage2D(trgTT, level, x, y, width, height, dafaFormat, dataType, pointer + offset);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TextureSubDataF(JNIEnv * jEnv, jclass jClass, jint trgTT, jint level, jint x, jint y, jint width, jint height, jint dataFormatTF, jfloatArray data, jint offset) {
    GLenum dafaFormat, dataType;
    convertImageType(dataFormatTF, dafaFormat, dataType);

    jfloat * pointer = (jfloat *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glTexSubImage2D(trgTT, level, x, y, width, height, dafaFormat, dataType, pointer + offset);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TextureSubDataBuffer(JNIEnv * jEnv, jclass jClass, jint trgTT, jint level, jint x, jint y, jint width, jint height, jint dataFormatTF, jobject buffer, jint offset) {
    GLenum dafaFormat, dataType;
    convertImageType(dataFormatTF, dafaFormat, dataType);

    char * pointer = (char *) jEnv->GetDirectBufferAddress(buffer);
    glTexSubImage2D(trgTT, level, x, y, width, height, dafaFormat, dataType, pointer + offset);
}

JNIEXPORT void JNICALL Java_flat_backend_GL_TextureSetLevels(JNIEnv * jEnv, jclass jClass, jint trgTB, jint levels) {
    glTexParameteri(trgTB, GL_TEXTURE_MAX_LEVEL, levels);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_TextureGetLevels(JNIEnv * jEnv, jclass jClass, jint trgTB) {
    GLint data;
    glGetTexParameteriv(trgTB, GL_TEXTURE_MAX_LEVEL, &data);
    return data;
}

JNIEXPORT jint JNICALL Java_flat_backend_GL_TextureGetWidth(JNIEnv * jEnv, jclass jClass, jint trgTB, jint level) {
    GLint data;
    glGetTexLevelParameteriv(trgTB, level, GL_TEXTURE_WIDTH, &data);
    return data;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_TextureGetHeight(JNIEnv * jEnv, jclass jClass, jint trgTB, jint level) {
    GLint data;
    glGetTexLevelParameteriv(trgTB, level, GL_TEXTURE_HEIGHT, &data);
    return data;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_TextureGetFormat(JNIEnv * jEnv, jclass jClass, jint trgTB, jint level) {
    GLint data;
    glGetTexLevelParameteriv(trgTB, level, GL_TEXTURE_INTERNAL_FORMAT, &data);
    return data;
}

JNIEXPORT void JNICALL Java_flat_backend_GL_TextureSetLOD(JNIEnv * jEnv, jclass jClass, jint trgTB, jfloat bias, jfloat max, jfloat min) {
    glTexParameterf(trgTB, GL_TEXTURE_LOD_BIAS, bias);
    glTexParameterf(trgTB, GL_TEXTURE_MAX_LOD, max);
    glTexParameterf(trgTB, GL_TEXTURE_MIN_LOD, min);
}
JNIEXPORT jfloat JNICALL Java_flat_backend_GL_TextureGetLODBias(JNIEnv * jEnv, jclass jClass, jint trgTB) {
    GLfloat data;
    glGetTexParameterfv(trgTB, GL_TEXTURE_LOD_BIAS, &data);
    return data;
}
JNIEXPORT jfloat JNICALL Java_flat_backend_GL_TextureGetLODMax(JNIEnv * jEnv, jclass jClass, jint trgTB) {
    GLfloat data;
    glGetTexParameterfv(trgTB, GL_TEXTURE_MAX_LOD, &data);
    return data;
}
JNIEXPORT jfloat JNICALL Java_flat_backend_GL_TextureGetLODMin(JNIEnv * jEnv, jclass jClass, jint trgTB) {
    GLfloat data;
    glGetTexParameterfv(trgTB, GL_TEXTURE_MIN_LOD,&data);
    return data;
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TextureSetFilter(JNIEnv * jEnv, jclass jClass, jint trgTB, jint magFilterTF, jint minFilterTF) {
    glTexParameteri(trgTB, GL_TEXTURE_MAG_FILTER, magFilterTF);
    glTexParameteri(trgTB, GL_TEXTURE_MIN_FILTER, minFilterTF);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_TextureGetFilterMag(JNIEnv * jEnv, jclass jClass, jint trgTB) {
    GLint data;
    glGetTexParameteriv(trgTB, GL_TEXTURE_MAG_FILTER, &data);
    return data;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_TextureGetFilterMin(JNIEnv * jEnv, jclass jClass, jint trgTB) {
    GLint data;
    glGetTexParameteriv(trgTB, GL_TEXTURE_MIN_FILTER, &data);
    return data;
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TextureSetSwizzle(JNIEnv * jEnv, jclass jClass, jint trgTB, jint rChanelCC, jint gChanelCC, jint bChanelCC, jint aChanelCC) {
    glTexParameteri(trgTB, GL_TEXTURE_SWIZZLE_R, rChanelCC);
    glTexParameteri(trgTB, GL_TEXTURE_SWIZZLE_G, gChanelCC);
    glTexParameteri(trgTB, GL_TEXTURE_SWIZZLE_B, bChanelCC);
    glTexParameteri(trgTB, GL_TEXTURE_SWIZZLE_A, aChanelCC);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_TextureGetSwizzleR(JNIEnv * jEnv, jclass jClass, jint trgTB) {
    GLint data;
    glGetTexParameteriv(trgTB, GL_TEXTURE_SWIZZLE_R, &data);
    return data;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_TextureGetSwizzleG(JNIEnv * jEnv, jclass jClass, jint trgTB) {
    GLint data;
    glGetTexParameteriv(trgTB, GL_TEXTURE_SWIZZLE_G, &data);
    return data;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_TextureGetSwizzleB(JNIEnv * jEnv, jclass jClass, jint trgTB) {
    GLint data;
    glGetTexParameteriv(trgTB, GL_TEXTURE_SWIZZLE_B, &data);
    return data;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_TextureGetSwizzleA(JNIEnv * jEnv, jclass jClass, jint trgTB) {
    GLint data;
    glGetTexParameteriv(trgTB, GL_TEXTURE_SWIZZLE_A, &data);
    return data;
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TextureSetBorderColor(JNIEnv * jEnv, jclass jClass, jint trgTB, jint rgba) {
    GLint data[4] = {((rgba >> 24) & 0xff),
                     ((rgba >> 16) & 0xff),
                     ((rgba >> 8) & 0xff),
                     ((rgba) & 0xff)};
    glTexParameteriv(trgTB, GL_TEXTURE_BORDER_COLOR, data);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_TextureGetBorderColor(JNIEnv * jEnv, jclass jClass, jint trgTB) {
    GLint data[4];
    glGetTexParameteriv(trgTB, GL_TEXTURE_BORDER_COLOR, data);
    return (data[0] << 24) | (data[1] << 16) | (data[2] << 8) | data[3];
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TextureSetWrap(JNIEnv * jEnv, jclass jClass, jint trgTB, jint horizontalIW, jint verticalIW) {
    glTexParameteri(trgTB, GL_TEXTURE_WRAP_S, horizontalIW);
    glTexParameteri(trgTB, GL_TEXTURE_WRAP_T, verticalIW);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_TextureGetWrapHorizontal(JNIEnv * jEnv, jclass jClass, jint trgTB) {
    GLint data;
    glGetTexParameteriv(trgTB, GL_TEXTURE_WRAP_S, &data);
    return data;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_TextureGetWrapVertical(JNIEnv * jEnv, jclass jClass, jint trgTB) {
    GLint data;
    glGetTexParameteriv(trgTB, GL_TEXTURE_WRAP_T, &data);
    return data;
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TextureSetCompareFunction(JNIEnv * jEnv, jclass jClass, jint trgTB, jint functionMF) {
    glTexParameteri(trgTB, GL_TEXTURE_COMPARE_FUNC, functionMF);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_TextureGetCompareFunction(JNIEnv * jEnv, jclass jClass, jint trgTB) {
    GLint data;
    glGetTexParameteriv(trgTB, GL_TEXTURE_COMPARE_FUNC, &data);
    return data;
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TextureSetCompareMode(JNIEnv * jEnv, jclass jClass, jint trgTB, jint compareModeCM) {
    glTexParameteri(trgTB, GL_TEXTURE_COMPARE_MODE, compareModeCM);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_TextureGetCompareMode(JNIEnv * jEnv, jclass jClass, jint trgTB) {
    GLint data;
    glGetTexParameteriv(trgTB, GL_TEXTURE_COMPARE_MODE, &data);
    return data;
}

//---------------------------
//         Buffers
//---------------------------
JNIEXPORT jint JNICALL Java_flat_backend_GL_BufferCreate(JNIEnv * jEnv, jclass jClass) {
    GLuint id;
    glGenBuffers(1 , &id);
    return id;
}
JNIEXPORT void JNICALL Java_flat_backend_GL_BufferDestroy(JNIEnv * jEnv, jclass jClass, jint id) {
    GLuint did = id;
    glDeleteBuffers(1, &did);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_BufferBind(JNIEnv * jEnv, jclass jClass, jint trgBB, jint id) {
    glBindBuffer(trgBB, id);
}

JNIEXPORT void JNICALL Java_flat_backend_GL_BufferBindBase(JNIEnv * jEnv, jclass jClass, jint trgBB, jint bindIndex, jint buffer) {
    glBindBufferBase(trgBB, bindIndex, buffer);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_BufferBindRange(JNIEnv * jEnv, jclass jClass, jint trgBB, jint bindIndex, jint buffer, jint offset, jint length) {
    glBindBufferRange(trgBB, bindIndex, buffer, offset, length);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_BufferGetBound(JNIEnv *jEnv, jclass jClass, jint trgBB) {
    GLint id = 0;
    if (trgBB == GL_ARRAY_BUFFER) {
        glGetIntegerv(GL_ARRAY_BUFFER_BINDING, &id);
    } else if (trgBB == GL_ELEMENT_ARRAY_BUFFER) {
        glGetIntegerv(GL_ELEMENT_ARRAY_BUFFER_BINDING, &id);
    } else if (trgBB == GL_PIXEL_PACK_BUFFER) {
        glGetIntegerv(GL_PIXEL_PACK_BUFFER_BINDING, &id);
    } else if (trgBB == GL_PIXEL_UNPACK_BUFFER) {
        glGetIntegerv(GL_PIXEL_UNPACK_BUFFER_BINDING, &id);
    } else if (trgBB == GL_TRANSFORM_FEEDBACK_BUFFER) {
        glGetIntegerv(GL_TRANSFORM_FEEDBACK_BUFFER_BINDING, &id);
    } else if (trgBB == GL_UNIFORM_BUFFER) {
        glGetIntegerv(GL_UNIFORM_BUFFER_BINDING, &id);
    } else if (trgBB == GL_COPY_READ_BUFFER) {
        glGetIntegerv(GL_COPY_READ_BUFFER, &id);
    } else if (trgBB == GL_COPY_WRITE_BUFFER) {
        glGetIntegerv(GL_COPY_WRITE_BUFFER, &id);
    }
    return id;
}
JNIEXPORT void JNICALL Java_flat_backend_GL_BufferDataB(JNIEnv * jEnv, jclass jClass, jint trgBB, jbyteArray data, jint offset, jint length, jint usageTypeUT) {
    jbyte * pointer = (jbyte *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glBufferData(trgBB, (length * sizeof(jbyte)), pointer + offset, usageTypeUT);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_BufferDataS(JNIEnv * jEnv, jclass jClass, jint trgBB, jshortArray data, jint offset, jint length, jint usageTypeUT) {
    jshort * pointer = (jshort *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glBufferData(trgBB, (length * sizeof(jshort)), pointer + offset, usageTypeUT);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_BufferDataI(JNIEnv * jEnv, jclass jClass, jint trgBB, jintArray data, jint offset, jint length, jint usageTypeUT) {
    jint * pointer = (jint *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glBufferData(trgBB, (length * sizeof(jint)), pointer + offset, usageTypeUT);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_BufferDataF(JNIEnv * jEnv, jclass jClass, jint trgBB, jfloatArray data, jint offset, jint length, jint usageTypeUT) {
    jfloat * pointer = (jfloat *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glBufferData(trgBB, (length * sizeof(jfloat)), pointer + offset, usageTypeUT);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_BufferDataBuffer(JNIEnv * jEnv, jclass jClass, jint trgBB, jobject buffer, jint offset, jint length, jint usageTypeUT) {
    if (buffer == nullptr) {
        glBufferData(trgBB, length, nullptr, usageTypeUT);
    } else {
        char *pointer = (char *) jEnv->GetDirectBufferAddress(buffer);
        glBufferData(trgBB, length, pointer + offset, usageTypeUT);
    }
}

JNIEXPORT void JNICALL Java_flat_backend_GL_BufferSubDataB(JNIEnv * jEnv, jclass jClass, jint trgBB, jbyteArray data, jint offset, jint length, jlong buffOffset) {
    jbyte * pointer = (jbyte *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glBufferSubData(trgBB, buffOffset, (length * sizeof(jbyte)), pointer + offset);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_BufferSubDataS(JNIEnv * jEnv, jclass jClass, jint trgBB, jshortArray data, jint offset, jint length, jlong buffOffset) {
    jshort * pointer = (jshort *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glBufferSubData(trgBB, buffOffset, (length * sizeof(jshort)), pointer + offset);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_BufferSubDataI(JNIEnv * jEnv, jclass jClass, jint trgBB, jintArray data, jint offset, jint length, jlong buffOffset) {
    jint * pointer = (jint *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glBufferSubData(trgBB, buffOffset, (length * sizeof(jint)), pointer + offset);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_BufferSubDataF(JNIEnv * jEnv, jclass jClass, jint trgBB, jfloatArray data, jint offset, jint length, jlong buffOffset) {
    jfloat * pointer = (jfloat *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glBufferSubData(trgBB, buffOffset, (length * sizeof(jfloat)), pointer + offset);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_BufferSubDataBuffer(JNIEnv * jEnv, jclass jClass, jint trgBB, jobject buffer, jint offset, jint length, jlong buffOffset) {
    char * pointer = (char *) jEnv->GetDirectBufferAddress(buffer);
    glBufferSubData(trgBB, buffOffset, length, pointer + offset);
}

JNIEXPORT void JNICALL Java_flat_backend_GL_BufferReadDataB(JNIEnv * jEnv, jclass jClass, jint trgBB, jbyteArray data, jint offset, jint length, jlong buffOffset) {
    jbyte * pointer = (jbyte *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glGetBufferSubData(trgBB, buffOffset, (length * sizeof(jbyte)), pointer + offset);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_BufferReadDataS(JNIEnv * jEnv, jclass jClass, jint trgBB, jshortArray data, jint offset, jint length, jlong buffOffset) {
    jshort * pointer = (jshort *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glGetBufferSubData(trgBB, buffOffset, (length * sizeof(jshort)), pointer + offset);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_BufferReadDataI(JNIEnv * jEnv, jclass jClass, jint trgBB, jintArray data, jint offset, jint length, jlong buffOffset) {
    jint * pointer = (jint *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glGetBufferSubData(trgBB, buffOffset, (length * sizeof(jint)), pointer + offset);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_BufferReadDataF(JNIEnv * jEnv, jclass jClass, jint trgBB, jfloatArray data, jint offset, jint length, jlong buffOffset) {
    jfloat * pointer = (jfloat *) jEnv->GetPrimitiveArrayCritical(data, 0);
    glGetBufferSubData(trgBB, buffOffset, (length * sizeof(jfloat)), pointer + offset);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_BufferReadDataBuffer(JNIEnv * jEnv, jclass jClass, jint trgBB, jobject buffer, jint offset, jint length, jlong buffOffset) {
    char * pointer = (char *) jEnv->GetDirectBufferAddress(buffer);
    glGetBufferSubData(trgBB, buffOffset, length, pointer + offset);
}

JNIEXPORT void JNICALL Java_flat_backend_GL_BufferCopy(JNIEnv * jEnv, jclass jClass, jint readTargetBB, jint writeTargetBB, jlong readOffset, jlong writeOffset, jlong length) {
    glCopyBufferSubData(readTargetBB, writeTargetBB, readOffset, writeOffset, length);
}
JNIEXPORT jlong JNICALL Java_flat_backend_GL_BufferMap(JNIEnv * jEnv, jclass jClass, jint trgBB, jlong offset, jlong length, jint acessBimaskAM) {
    return (jlong) glMapBufferRange(trgBB, offset, length, acessBimaskAM);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_BufferUnmap(JNIEnv * jEnv, jclass jClass, jint trgBB) {
    glUnmapBuffer(trgBB);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_BufferFlush(JNIEnv * jEnv, jclass jClass, jint trgBB, jlong offset, jlong length) {
    glFlushMappedBufferRange(trgBB, offset, length);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_BufferGetSize(JNIEnv * jEnv, jclass jClass, jint trgBB) {
    GLint data;
    glGetBufferParameteriv(trgBB, GL_BUFFER_SIZE, &data);
    return data;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_BufferGetUsage(JNIEnv * jEnv, jclass jClass, jint trgBB) {
    GLint data;
    glGetBufferParameteriv(trgBB, GL_BUFFER_USAGE, &data);
    return data;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_BufferGetAcess(JNIEnv * jEnv, jclass jClass, jint trgBB) {
    GLint data;
    glGetBufferParameteriv(trgBB, GL_BUFFER_ACCESS,  &data);
    return data;
}
JNIEXPORT jboolean JNICALL Java_flat_backend_GL_BufferIsMapped(JNIEnv * jEnv, jclass jClass, jint trgBB) {
    GLint data;
    glGetBufferParameteriv(trgBB, GL_BUFFER_MAPPED, &data);
    return data == GL_TRUE;
}

//---------------------------
//           Program
//---------------------------
JNIEXPORT jint JNICALL Java_flat_backend_GL_ProgramCreate(JNIEnv * jEnv, jclass jClass) {
    return glCreateProgram();
}
JNIEXPORT void JNICALL Java_flat_backend_GL_ProgramDestroy(JNIEnv * jEnv, jclass jClass, jint id) {
    glDeleteProgram(id);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_ProgramLink(JNIEnv * jEnv, jclass jClass, jint id) {
    glLinkProgram(id);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_ProgramUse(JNIEnv * jEnv, jclass jClass, jint id) {
    glUseProgram(id);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_ProgramGetUsed(JNIEnv * jEnv, jclass jClass) {
    GLint id;
    glGetIntegerv(GL_CURRENT_PROGRAM, &id);
    return id;
}
JNIEXPORT jboolean JNICALL Java_flat_backend_GL_ProgramIsDeleted(JNIEnv * jEnv, jclass jClass, jint id) {
    GLint data;
    glGetProgramiv(id, GL_DELETE_STATUS, &data);
    return data;
}
JNIEXPORT jboolean JNICALL Java_flat_backend_GL_ProgramIsLinked(JNIEnv * jEnv, jclass jClass, jint id) {
    GLint data;
    glGetProgramiv(id, GL_LINK_STATUS, &data);
    return data;
}
JNIEXPORT jboolean JNICALL Java_flat_backend_GL_ProgramIsValidated(JNIEnv * jEnv, jclass jClass, jint id) {
    GLint data;
    glGetProgramiv(id, GL_VALIDATE_STATUS, &data);
    return data;
}
JNIEXPORT jstring JNICALL Java_flat_backend_GL_ProgramGetLog(JNIEnv * jEnv, jclass jClass, jint id) {
    GLint size;
    GLint lsize;
    glGetProgramiv(id, GL_INFO_LOG_LENGTH, &size);
    char log[size];
    glGetProgramInfoLog(id, size, &lsize, log);
    return jEnv->NewStringUTF(log);
}

JNIEXPORT void JNICALL Java_flat_backend_GL_ProgramAttachShader(JNIEnv * jEnv, jclass jClass, jint id, jint shaderId) {
    glAttachShader(id, shaderId);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_ProgramDetachShader(JNIEnv * jEnv, jclass jClass, jint id, jint shaderId) {
    glDetachShader(id, shaderId);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_ProgramGetAttachedShadersCount(JNIEnv * jEnv, jclass jClass, jint id) {
    GLint size;
    glGetProgramiv(id, GL_ATTACHED_SHADERS, &size);
    return size;
}
JNIEXPORT void JNICALL Java_flat_backend_GL_ProgramGetAttachedShaders(JNIEnv * jEnv, jclass jClass, jint id, jintArray data) {
    GLint size;
    GLint count;
    glGetProgramiv(id, GL_ATTACHED_SHADERS, &size);
    GLuint shaders[size];
    glGetAttachedShaders(id, size, &count, shaders);
    for (int i = 0; i < count; i++) {
        jint shader = shaders[i];
        jEnv->SetIntArrayRegion(data, i, 1, &shader);
    }
}

JNIEXPORT jint JNICALL Java_flat_backend_GL_ProgramGetAttributesCount(JNIEnv * jEnv, jclass jClass, jint id) {
    GLint size;
    glGetProgramiv(id, GL_ACTIVE_ATTRIBUTES, &size);
    return size;
}
JNIEXPORT jstring JNICALL Java_flat_backend_GL_ProgramGetAttributeName(JNIEnv * jEnv, jclass jClass, jint id, jint attributeId) {
    GLint size;
    glGetProgramiv(id, GL_ACTIVE_ATTRIBUTE_MAX_LENGTH, &size);
    char name[size];
    GLint nameLen, verSize;
    GLuint verType;
    glGetActiveAttrib(id, attributeId, size, &nameLen, &verSize, &verType, name);
    return jEnv->NewStringUTF(name);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_ProgramGetAttributeType(JNIEnv * jEnv, jclass jClass, jint id, jint attributeId) {
    GLint verSize;
    GLuint verType;
    glGetActiveAttrib(id, attributeId, 0, 0, &verSize, &verType, 0);
    return verType;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_ProgramGetAttributeSize(JNIEnv * jEnv, jclass jClass, jint id, jint attributeId) {
    GLint verSize;
    GLuint verType;
    glGetActiveAttrib(id, attributeId, 0, 0, &verSize, &verType, 0);
    return verSize;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_ProgramGetAttributeId(JNIEnv * jEnv, jclass jClass, jint id, jstring name) {
    const char * str = jEnv->GetStringUTFChars(name, 0);
    jint pos = glGetAttribLocation(id, str);
    jEnv->ReleaseStringUTFChars(name, str);
    return pos;
}

JNIEXPORT jint JNICALL Java_flat_backend_GL_ProgramGetUniformsCount(JNIEnv * jEnv, jclass jClass, jint id) {
    GLint size;
    glGetProgramiv(id, GL_ACTIVE_UNIFORMS, &size);
    return size;
}
JNIEXPORT jstring JNICALL Java_flat_backend_GL_ProgramGetUniformName(JNIEnv * jEnv, jclass jClass, jint id, jint uniformId) {
    GLint size;
    glGetProgramiv(id, GL_ACTIVE_UNIFORM_MAX_LENGTH, &size);
    char name[size];
    GLint nameLen, verSize;
    GLuint verType;
    glGetActiveUniform(id, uniformId, size, &nameLen, &verSize, &verType, name);
    return jEnv->NewStringUTF(name);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_ProgramGetUniformType(JNIEnv * jEnv, jclass jClass, jint id, jint uniformId) {
    GLint verSize;
    GLuint verType;
    glGetActiveUniform(id, uniformId, 0, 0, &verSize, &verType, 0);
    return verType;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_ProgramGetUniformSize(JNIEnv * jEnv, jclass jClass, jint id, jint uniformId) {
    GLint verSize;
    GLuint verType;
    glGetActiveUniform(id, uniformId, 0, 0, &verSize, &verType, 0);
    return verSize;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_ProgramGetUniformId(JNIEnv * jEnv, jclass jClass, jint id, jstring name) {
    const char * str = jEnv->GetStringUTFChars(name, 0);
    jint pos = glGetUniformLocation(id, str);
    jEnv->ReleaseStringUTFChars(name, str);
    return pos;
}


JNIEXPORT jint JNICALL Java_flat_backend_GL_ProgramGetUniformBlocksCount(JNIEnv *jEnv, jclass jClass, jint id) {
    GLint size;
    glGetProgramiv(id, GL_ACTIVE_UNIFORM_BLOCKS, &size);
    return size;
}
JNIEXPORT jstring JNICALL Java_flat_backend_GL_ProgramGetUniformBlockName(JNIEnv *jEnv, jclass jClass, jint id, jint blockId) {
    GLint size;
    glGetProgramiv(id, GL_ACTIVE_UNIFORM_BLOCK_MAX_NAME_LENGTH, &size);
    char name[size];
    GLint nameLen;
    glGetActiveUniformBlockName(id, blockId, size, &nameLen, name);
    return jEnv->NewStringUTF(name);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_ProgramGetUniformBlockBinding(JNIEnv *jEnv, jclass jClass, jint id, jint blockId) {
    GLint data;
    glGetActiveUniformBlockiv(id, blockId, GL_UNIFORM_BLOCK_BINDING, &data);
    return data;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_ProgramGetUniformBlockSize(JNIEnv *jEnv, jclass jClass, jint id, jint blockId) {
    GLint data;
    glGetActiveUniformBlockiv(id, blockId, GL_UNIFORM_BLOCK_DATA_SIZE, &data);
    return data;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_ProgramGetUniformBlockId(JNIEnv *jEnv, jclass jClass, jint id, jstring name) {
    const char * str = jEnv->GetStringUTFChars(name, 0);
    jint pos = glGetUniformBlockIndex(id, str);
    jEnv->ReleaseStringUTFChars(name, str);
    return pos;
}
JNIEXPORT void JNICALL Java_flat_backend_GL_ProgramUniformBlockBinding(JNIEnv *jEnv, jclass jClass, jint id, jint blockId, jint blockBind) {
    glUniformBlockBinding(id, blockId, blockBind);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_ProgramGetUniformBlockChildrenCount(JNIEnv *jEnv, jclass jClass, jint id, jint blockId) {
    GLint data;
    glGetActiveUniformBlockiv(id, blockId, GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS, &data);
    return data;
}
JNIEXPORT void JNICALL Java_flat_backend_GL_ProgramGetUniformBlockChildren(JNIEnv *jEnv, jclass jClass, jint id, jint blockId, jintArray data) {
    void * pointer = jEnv->GetPrimitiveArrayCritical(data, 0);
    glGetActiveUniformBlockiv(id, blockId, GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES, (GLint *) pointer);
    jEnv->ReleasePrimitiveArrayCritical(data, pointer, 0);
}

JNIEXPORT void JNICALL Java_flat_backend_GL_ProgramSetTFVars(JNIEnv * jEnv, jclass jClass, jint id, jobjectArray names, jint bufferModeFM) {
    jint length = jEnv->GetArrayLength(names);
    const char* cNames[length];
    for (int i = 0; i < length; i++) {
        cNames[i] = jEnv->GetStringUTFChars((jstring)jEnv->GetObjectArrayElement(names, i), 0);
    }
    glTransformFeedbackVaryings(id, length, cNames, bufferModeFM);
    for (int i = 0; i < length; i++) {
        jEnv->ReleaseStringUTFChars((jstring)jEnv->GetObjectArrayElement(names, i), 0);
    }
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_ProgramGetTFVarsCount(JNIEnv * jEnv, jclass jClass, jint id) {
    GLint size;
    glGetProgramiv(id, GL_TRANSFORM_FEEDBACK_VARYINGS, &size);
    return size;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_ProgramTFVarsBufferMode(JNIEnv * jEnv, jclass jClass, jint id) {
    GLint mode;
    glGetProgramiv(id, GL_TRANSFORM_FEEDBACK_BUFFER_MODE, &mode);
    return mode;
}
JNIEXPORT jstring JNICALL Java_flat_backend_GL_ProgramGetTFVarName(JNIEnv * jEnv, jclass jClass, jint id, jint tfVarId) {
    GLint size;
    glGetProgramiv(id, GL_TRANSFORM_FEEDBACK_VARYING_MAX_LENGTH, &size);
    char name[size];
    GLint nameLen, varSize;
    GLuint varType;
    glGetTransformFeedbackVarying(id, tfVarId, size, &nameLen, &varSize, &varType, name);
    return jEnv->NewStringUTF(name);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_ProgramGetTFVarType(JNIEnv * jEnv, jclass jClass, jint id, jint tfVarId) {
    GLint verSize;
    GLuint verType;
    glGetTransformFeedbackVarying(id, tfVarId, 0, 0, &verSize, &verType, 0);
    return verSize;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_ProgramGetTFVarSize(JNIEnv * jEnv, jclass jClass, jint id, jint tfVarId) {
    GLint verSize;
    GLuint verType;
    glGetTransformFeedbackVarying(id, tfVarId, 0, 0, &verSize, &verType, 0);
    return verSize;
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_ProgramGetTFVarId(JNIEnv * jEnv, jclass jClass, jint id, jstring name) {
    const char * str = jEnv->GetStringUTFChars(name, 0);
    jint pos = glGetAttribLocation(id, str);
    jEnv->ReleaseStringUTFChars(name, str);
    return pos;
}

//JNIEXPORT jint JNICALL Java_flat_backend_GL_ProgramGetGeometryVerticesOut(JNIEnv * jEnv, jclass jClass, jint id)
//JNIEXPORT jint JNICALL Java_flat_backend_GL_ProgramGetGeometryInputType(JNIEnv * jEnv, jclass jClass, jint id)
//JNIEXPORT jint JNICALL Java_flat_backend_GL_ProgramGetGeometryOutputType(JNIEnv * jEnv, jclass jClass, jint id)

JNIEXPORT void JNICALL Java_flat_backend_GL_ProgramSetUniformI(JNIEnv * jEnv, jclass jClass, jint uniformId, jint attSize, jint arrSize, jintArray value, jint offset) {
    jint * pointer = (jint *) jEnv->GetPrimitiveArrayCritical(value, 0);
    if (attSize == 1) glUniform1iv(uniformId, arrSize, (const GLint *) (pointer + offset));
    else if (attSize == 2) glUniform2iv(uniformId, arrSize, (const GLint *) (pointer + offset));
    else if (attSize == 3) glUniform3iv(uniformId, arrSize, (const GLint *) (pointer + offset));
    else if (attSize == 4) glUniform4iv(uniformId, arrSize, (const GLint *) (pointer + offset));
    jEnv->ReleasePrimitiveArrayCritical(value, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_ProgramSetUniformF(JNIEnv * jEnv, jclass jClass, jint uniformId, jint attSize, jint arrSize, jfloatArray value, jint offset) {
    jfloat * pointer = (jfloat *) jEnv->GetPrimitiveArrayCritical(value, 0);
    if (attSize == 1) glUniform1fv(uniformId, arrSize, (const GLfloat *) (pointer + offset));
    else if (attSize == 2) glUniform2fv(uniformId, arrSize, (const GLfloat *) (pointer + offset));
    else if (attSize == 3) glUniform3fv(uniformId, arrSize, (const GLfloat *) (pointer + offset));
    else if (attSize == 4) glUniform4fv(uniformId, arrSize, (const GLfloat *) (pointer + offset));
    jEnv->ReleasePrimitiveArrayCritical(value, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_ProgramSetUniformMatrix(JNIEnv * jEnv, jclass jClass, jint uniformId, jint w, jint h, jint arrSize, jboolean transpose, jfloatArray value, jint offset) {
    jfloat * pointer = (jfloat *) jEnv->GetPrimitiveArrayCritical(value, 0);
    if (w == 2) {
        if (h == 2) glUniformMatrix2fv(uniformId, arrSize, transpose, (const GLfloat *) (pointer + offset));
        if (h == 3) glUniformMatrix2x3fv(uniformId, arrSize, transpose, (const GLfloat *) (pointer + offset));
        if (h == 4) glUniformMatrix2x4fv(uniformId, arrSize, transpose, (const GLfloat *) (pointer + offset));
    } else if (w == 3) {
        if (h == 2) glUniformMatrix3x2fv(uniformId, arrSize, transpose, (const GLfloat *) (pointer + offset));
        if (h == 3) glUniformMatrix3fv(uniformId, arrSize, transpose, (const GLfloat *) (pointer + offset));
        if (h == 4) glUniformMatrix3x4fv(uniformId, arrSize, transpose, (const GLfloat *) (pointer + offset));
    } else if (w == 4) {
        if (h == 2) glUniformMatrix4x2fv(uniformId, arrSize, transpose, (const GLfloat *) (pointer + offset));
        if (h == 3) glUniformMatrix4x3fv(uniformId, arrSize, transpose, (const GLfloat *) (pointer + offset));
        if (h == 4) glUniformMatrix4fv(uniformId, arrSize, transpose, (const GLfloat *) (pointer + offset));
    }
    jEnv->ReleasePrimitiveArrayCritical(value, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_ProgramSetUniformBuffer(JNIEnv * jEnv, jclass jClass, jint uniformId, jint attSize, jint arrSize, jint typeDT, jobject buffer, jint offset) {
    char * pointer = (char *) jEnv->GetDirectBufferAddress(buffer);
    if (typeDT == GL_INT) {
        if (attSize == 1) glUniform1iv(uniformId, attSize, (const GLint *) (pointer + offset));
        else if (attSize == 2) glUniform2iv(uniformId, attSize, (const GLint *) (pointer + offset));
        else if (attSize == 3) glUniform3iv(uniformId, attSize, (const GLint *) (pointer + offset));
        else if (attSize == 4) glUniform4iv(uniformId, attSize, (const GLint *) (pointer + offset));
    } else {
        if (attSize == 1) glUniform1fv(uniformId, attSize, (const GLfloat *) (pointer + offset));
        else if (attSize == 2) glUniform2fv(uniformId, attSize, (const GLfloat *) (pointer + offset));
        else if (attSize == 3) glUniform3fv(uniformId, attSize, (const GLfloat *) (pointer + offset));
        else if (attSize == 4) glUniform4fv(uniformId, attSize, (const GLfloat *) (pointer + offset));
    }
}

JNIEXPORT void JNICALL Java_flat_backend_GL_ProgramGetUniformI(JNIEnv * jEnv, jclass jClass, jint id, jint uniformId, jintArray value, jint offset) {
    void * pointer = jEnv->GetPrimitiveArrayCritical(value, 0);
    glGetUniformiv(id, uniformId, (GLint *) &pointer);
    jEnv->ReleasePrimitiveArrayCritical(value, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_ProgramGetUniformF(JNIEnv * jEnv, jclass jClass, jint id, jint uniformId, jfloatArray value, jint offset) {
    void * pointer = jEnv->GetPrimitiveArrayCritical(value, 0);
    glGetUniformfv(id, uniformId, (GLfloat *) &pointer);
    jEnv->ReleasePrimitiveArrayCritical(value, pointer, 0);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_ProgramGetUniformBuffer(JNIEnv * jEnv, jclass jClass, jint id, jint uniformId, jint typeDT, jobject buffer, jint offset) {
    void * pointer = jEnv->GetDirectBufferAddress(buffer);
    if (typeDT == GL_INT) {
        glGetUniformiv(id, uniformId, (GLint *) &pointer);
    } else {
        glGetUniformfv(id, uniformId, (GLfloat *) &pointer);
    }
}

//---------------------------
//           Vertex
//---------------------------
JNIEXPORT jint JNICALL Java_flat_backend_GL_VertexArrayCreate(JNIEnv * jEnv, jclass jClass) {
    GLuint id;
    glGenVertexArrays(1, &id);
    return id;
}

JNIEXPORT void JNICALL Java_flat_backend_GL_VertexArrayDestroy(JNIEnv * jEnv, jclass jClass, jint id) {
    GLuint did = id;
    glDeleteVertexArrays(1, &did);
}

JNIEXPORT void JNICALL Java_flat_backend_GL_VertexArrayBind(JNIEnv * jEnv, jclass jClass, jint id) {
    glBindVertexArray(id);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_VertexArrayGetBound(JNIEnv * jEnv, jclass jClass) {
    GLint id;
    glGetIntegerv(GL_VERTEX_ARRAY_BINDING, &id);
    return id;
}

JNIEXPORT void JNICALL Java_flat_backend_GL_VertexArrayAttribEnable(JNIEnv * jEnv, jclass jClass, jint attrId, jboolean enable) {
    if (enable) glEnableVertexAttribArray(attrId); else glDisableVertexAttribArray(attrId);
}

JNIEXPORT jboolean JNICALL Java_flat_backend_GL_VertexArrayAttribIsEnabled(JNIEnv * jEnv, jclass jClass, jint attrId) {
    GLint data;
    glGetVertexAttribiv(attrId, GL_VERTEX_ATTRIB_ARRAY_ENABLED, &data);
    return data == GL_TRUE;
}

JNIEXPORT void JNICALL Java_flat_backend_GL_VertexArrayAttribSetDivisor(JNIEnv * jEnv, jclass jClass, jint attrId, jint divisor) {
    glVertexAttribDivisor(attrId, divisor);
}

JNIEXPORT jint JNICALL Java_flat_backend_GL_VertexArrayAttribGetDivisor(JNIEnv * jEnv, jclass jClass, jint attrId) {
    GLint data;
    glGetVertexAttribiv(attrId, GL_VERTEX_ATTRIB_ARRAY_DIVISOR, &data);
    return data;
}

JNIEXPORT void JNICALL Java_flat_backend_GL_VertexArrayAttribPointer(JNIEnv * jEnv, jclass jClass, jint attrId, jint attSize, jboolean normalized, jlong stride, jint _type, jlong offset) {
    glVertexAttribPointer(attrId, attSize, _type, normalized, stride, (void *) offset);
}

JNIEXPORT void JNICALL Java_flat_backend_GL_VertexArrayAttribPointerB(JNIEnv * jEnv, jclass jClass, jint attrId, jint attSize, jboolean normalized, jint stride, jbyteArray value, jint offset) {
    jbyte * pointer = (jbyte *) jEnv->GetPrimitiveArrayCritical(value, 0);
    glVertexAttribPointer(attrId, attSize, GL_BYTE, normalized, (stride * sizeof(jbyte)), (pointer + offset));
    jEnv->ReleasePrimitiveArrayCritical(value, pointer, 0);
}

JNIEXPORT void JNICALL Java_flat_backend_GL_VertexArrayAttribPointerS(JNIEnv * jEnv, jclass jClass, jint attrId, jint attSize, jboolean normalized, jint stride, jshortArray value, jint offset) {
    jshort * pointer = (jshort *) jEnv->GetPrimitiveArrayCritical(value, 0);
    glVertexAttribPointer(attrId, attSize, GL_SHORT, normalized, (stride * sizeof(jshort)), (pointer + offset));
    jEnv->ReleasePrimitiveArrayCritical(value, pointer, 0);
}

JNIEXPORT void JNICALL Java_flat_backend_GL_VertexArrayAttribPointerI(JNIEnv * jEnv, jclass jClass, jint attrId, jint attSize, jboolean normalized, jint stride, jintArray value, jint offset) {
    jint * pointer = (jint *) jEnv->GetPrimitiveArrayCritical(value, 0);
    glVertexAttribPointer(attrId, attSize, GL_INT, normalized, (stride * sizeof(jint)), (pointer + offset));
    jEnv->ReleasePrimitiveArrayCritical(value, pointer, 0);
}

JNIEXPORT void JNICALL Java_flat_backend_GL_VertexArrayAttribPointerF(JNIEnv * jEnv, jclass jClass, jint attrId, jint attSize, jboolean normalized, jint stride, jfloatArray value, jint offset) {
    jfloat * pointer = (jfloat *) jEnv->GetPrimitiveArrayCritical(value, 0);
    glVertexAttribPointer(attrId, attSize, GL_FLOAT, normalized, (stride * sizeof(jfloat)), (pointer + offset));
    jEnv->ReleasePrimitiveArrayCritical(value, pointer, 0);
}

JNIEXPORT void JNICALL Java_flat_backend_GL_VertexArrayAttribPointerD(JNIEnv * jEnv, jclass jClass, jint attrId, jint attSize, jboolean normalized, jint stride, jdoubleArray value, jint offset) {
    jdouble * pointer = (jdouble *) jEnv->GetPrimitiveArrayCritical(value, 0);
    glVertexAttribPointer(attrId, attSize, GL_DOUBLE, normalized, (stride * sizeof(jdouble)), (pointer + offset));
    jEnv->ReleasePrimitiveArrayCritical(value, pointer, 0);
}

JNIEXPORT void JNICALL Java_flat_backend_GL_VertexArrayAttribPointerBuffer(JNIEnv * jEnv, jclass jClass, jint attrId, jint attSize, jboolean normalized, jint stride, jint _type, jobject buffer, jint offset) {
    char * pointer = (char *) jEnv->GetDirectBufferAddress(buffer);
    glVertexAttribPointer(attrId, attSize, _type, normalized, stride, pointer + offset);
}
//---------------------------
//           Shader
//---------------------------
JNIEXPORT jint JNICALL Java_flat_backend_GL_ShaderCreate(JNIEnv * jEnv, jclass jClass, jint typeST) {
    return glCreateShader(typeST);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_ShaderDestroy(JNIEnv * jEnv, jclass jClass, jint id) {
    GLuint did = id;
    glDeleteSamplers(1, &did);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_ShaderCompile(JNIEnv * jEnv, jclass jClass, jint id) {
    glCompileShader(id);
}

JNIEXPORT jboolean JNICALL Java_flat_backend_GL_ShaderIsDeleted(JNIEnv * jEnv, jclass jClass, jint id) {
    GLint data;
    glGetShaderiv(id, GL_DELETE_STATUS, &data);
    return data == GL_TRUE;
}
JNIEXPORT jboolean JNICALL Java_flat_backend_GL_ShaderIsCompiled(JNIEnv * jEnv, jclass jClass, jint id) {
    GLint data;
    glGetShaderiv(id, GL_COMPILE_STATUS, &data);
    return data == GL_TRUE;
}
JNIEXPORT jstring JNICALL Java_flat_backend_GL_ShaderGetLog(JNIEnv * jEnv, jclass jClass, jint id) {
    GLint size;
    GLint lsize;
    glGetShaderiv(id, GL_INFO_LOG_LENGTH, &size);
    char log[size];
    glGetShaderInfoLog(id, size, &lsize, log);
    return jEnv->NewStringUTF(log);
}
JNIEXPORT jint JNICALL Java_flat_backend_GL_ShaderGetType(JNIEnv * jEnv, jclass jClass, jint id) {
    GLint data;
    glGetShaderiv(id, GL_SHADER_TYPE, &data);
    return data;
}
JNIEXPORT void JNICALL Java_flat_backend_GL_ShaderSetSource(JNIEnv * jEnv, jclass jClass, jint id, jstring content) {
    const char * str = jEnv->GetStringUTFChars(content, 0);
    GLint length = jEnv->GetStringUTFLength(content);
    glShaderSource(id, 1, &str, &length);
    jEnv->ReleaseStringUTFChars(content, str);
}
JNIEXPORT jstring JNICALL Java_flat_backend_GL_ShaderGetSource(JNIEnv * jEnv, jclass jClass, jint id) {
    GLint size;
    GLint lsize;
    glGetShaderiv(id, GL_SHADER_SOURCE_LENGTH, &size);
    char log[size];
    glGetShaderSource(id, size, &lsize, log);
    return jEnv->NewStringUTF(log);
}

//---------------------------
//    Transform Feedback
//---------------------------
JNIEXPORT void JNICALL Java_flat_backend_GL_TransformFeedbackBegin(JNIEnv * jEnv, jclass jClass, jint polygonTypeFP) {
    glBeginTransformFeedback(polygonTypeFP);
}
JNIEXPORT void JNICALL Java_flat_backend_GL_TransformFeedbackEnd(JNIEnv * jEnv, jclass jClass) {
    glEndTransformFeedback();
}
