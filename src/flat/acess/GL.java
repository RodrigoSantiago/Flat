package flat.acess;

import com.sun.jna.Pointer;

public class GL {
    static {
        // ligação nativa
    }

    //---------------------------
    //         Context
    //---------------------------
    public static native void gl_Flush();
    public static native void gl_Finish();

    public static native void gl_Clear(boolean color, boolean depth, boolean stencil);
    public static native void gl_SetClearColor(int rgba);
    public static native void gl_SetClearDepth(int mask);
    public static native void gl_SetClearStencil(int mask);
    public static native int gl_GetClearColor();
    public static native int gl_GetClearDepth();
    public static native int gl_GetClearStencil();

    public static native void gl_EnableRasterizer(boolean enabled);
    public static native boolean gl_IsRasterizerEnabled();
    public static native void gl_EnableAlphaTest(boolean enable);
    public static native boolean gl_IsAlphaTestEnabled();

    public static native void gl_EnableScissorTest(boolean enable);
    public static native boolean gl_IsScissorTestEnabled();
    public static native void gl_SetScissor(int x, int y, int width, int height);
    public static native int gl_GetScissorX();
    public static native int gl_GetScissorY();
    public static native int gl_GetScissorWidth();
    public static native int gl_GetScissorHeight();

    public static native void gl_SetPolygonMode(int _face, int _polygonMode);
    public static native int gl_GetPolygonMode(int _face);
    public static native void gl_SetLineWidth(float width);
    public static native float gl_GetLineWidth();
    public static native void gl_SetPointSize(float size);
    public static native float gl_GetPointSize();

    public static native void gl_EnableDepthTest(boolean enable);
    public static native boolean gl_IsDepthTestEnabled();
    public static native void gl_SetDepthMask(int mask);
    public static native int gl_GetDepthMask();
    public static native void gl_SetDepthFunction(int _mathFunction);
    public static native int gl_GetDepthFunction();

    public static native void gl_EnableStencilTest(boolean enable);
    public static native boolean gl_IsStencilTestEnabled();
    public static native void gl_SetStencilMask(int mask);
    public static native int gl_GetStencilMask();
    public static native void gl_SetStencilFunction(int _mathFunction);
    public static native int gl_GetStencilFunction();
    public static native void gl_SetStencilOperation(int stencil_mathOperation, int depth_mathOperation, int both_mathOperation);
    public static native int gl_GetStencilFailOperation();
    public static native int gl_GetDepthFailOperation();
    public static native int gl_GetStencilDepthFailOperation();

    public static native void gl_EnableBlend(boolean enable);
    public static native boolean gl_IsBlendEnabled();
    public static native void gl_SetBlendFunction(int srcRGB_blend, int dstRGB_blend, int srcA_blend, int dstA_blend);
    public static native int gl_GetSrcAlphaBlendFunction();
    public static native int gl_GetDstAlphaBlendFunction();
    public static native int gl_GetSrcRGBBlendFunction();
    public static native int gl_GetDstRGBBlendFunction();
    public static native void gl_SetBlendEquation(int RGB_blendEquation, int A_blendEquation);
    public static native int gl_GetRGBBlendEquation();
    public static native int gl_GetAlphaBlendEquation();

    public static native void gl_SetBlendColor(int rgba);
    public static native int gl_GetBlendColor();

    public static native void gl_EnableCullface(boolean enable);
    public static native boolean gl_IsCullfaceEnabled();
    public static native void gl_SetCullface(int _face);
    public static native int gl_GetCullface();

    public static native void gl_DrawArrays(int _drawMode, int first, int count);
    public static native void gl_DrawArraysInstanced(int _drawMode, int first, int count, int instances);
    public static native void gl_DrawElements(int _drawMode, int first, int count, long offset);
    public static native void gl_DrawElementsInstanced(int _drawMode, int first, int count, long offset, int instances);
    public static native void gl_ReadPixels(int x, int y, int width, int height, byte[] data);
    public static native void gl_ReadPixels(int x, int y, int width, int height, short[] data);
    public static native void gl_ReadPixels(int x, int y, int width, int height, int[] data);
    public static native void gl_ReadPixels(int x, int y, int width, int height, float[] data);
    public static native String gl_GetError();

    //---------------------------
    //        Frame Buffer
    //---------------------------
    public static native int gl_FrameBufferCreate();
    public static native void gl_FrameBufferBind(int id);
    public static native void gl_FrameBufferUnbind(int id);
    public static native void gl_FrameBufferDestroy(int id);
    public static native void gl_FrameBufferTexture(int _targetType, int _textureType, int textureId, int mipMap);
    public static native void gl_FrameBufferRenderBuffer(int _targetType, int renderbufferId);
    public static native boolean gl_FrameBufferIsComplete();

    //---------------------------
    //        Render Buffer
    //---------------------------
    public static native int gl_RenderBufferCreate();
    public static native void gl_RenderBufferBind(int id);
    public static native void gl_RenderBufferUnbind(int id);
    public static native void gl_RenderBufferDestroy(int id);
    public static native void gl_RenderBufferStorage(int _type, int width, int height);


    //---------------------------
    //         Textures
    //---------------------------
    public static native void gl_SetActiveTexture(int pos);
    public static native int gl_TextureCreate();
    public static native void gl_TextureBind(int id, int _textureType);
    public static native void gl_TextureUnbind(int id);
    public static native void gl_TextureGenerateMipmap(int id);
    public static native void gl_TextureDestroy(int id);
    public static native void gl_TextureData(int _textureTarget, int level, int _format, int width, int height, int border, int _dataFormat, byte[] data);
    public static native void gl_TextureData(int _textureTarget, int level, int _format, int width, int height, int border, int _dataFormat, int[] data);
    public static native void gl_TextureData(int _textureTarget, int level, int _format, int width, int height, int border, int _dataFormat, long offset);
    public static native void gl_TextureSubData(int _textureTarget, int level, int x, int y, int width, int height, int _dataFormat, byte[] data);
    public static native void gl_TextureSubData(int _textureTarget, int level, int x, int y, int width, int height, int _dataFormat, int[] data);
    public static native void gl_TextureSubData(int _textureTarget, int level, int x, int y, int width, int height, int _dataFormat, long offset);
    public static native void gl_TextureReadData(int _textureTarget, int level, int _dataFormat, byte[] offset);
    public static native void gl_TextureReadData(int _textureTarget, int level, int _dataFormat, int[] offset);
    public static native void gl_TextureReadData(int _textureTarget, int level, int _dataFormat, long offset);
    public static native void gl_TextureSetDepthStencilMode(int id, int _textureTarget, boolean depthMode);
    public static native void gl_TextureSetLevel(int _textureTarget, int base, int max);
    public static native void gl_TextureSetLOD(int _textureTarget, float bias, float max, float min);
    public static native void gl_TextureSetMinFilter(int _textureTarget, int _textureFilter);
    public static native void gl_TextureSetMagFilter(int _textureTarget, int _textureFilter);
    public static native void gl_TextureSetSwizzle(int _textureTarget, int r_Chanel, int g_Chanel, int b_Chanel, int a_Chanel);
    public static native void gl_TextureSetBorderColor(int _textureTarget, int rgba);
    public static native void gl_TextureSetWrapHorizontal(int _textureTarget, int _wrapMode);
    public static native void gl_TextureSetWrapVertical(int _textureTarget, int _wrapMode);
    public static native void gl_TextureSetWrapDepth(int _textureTarget, int _wrapMode);
    public static native void gl_TextureSetCompareFunction(int _textureTarget, int _mathFunction);
    public static native void gl_TextureSetCompareMode(int _textureTarget, boolean toTextureRef);

    //---------------------------
    //         Buffers
    //---------------------------
    public static native int gl_BufferCreate();
    public static native void gl_BufferBind(int id, int _bufferType);
    public static native void gl_BufferUnbind(int id);
    public static native void gl_BufferDestroy(int id);
    public static native void gl_BufferData(byte[] data, int size, int _usageType);
    public static native void gl_BufferData(short[] data, int size, int _usageType);
    public static native void gl_BufferData(int[] data, int size, int _usageType);
    public static native void gl_BufferData(float[] data, int size, int _usageType);
    public static native void gl_BufferSubData(byte[] data, int size, int offset, int _usageType);
    public static native void gl_BufferSubData(short[] data, int size, int offset, int _usageType);
    public static native void gl_BufferSubData(int[] data, int size, int offset, int _usageType);
    public static native void gl_BufferSubData(float[] data, int size, int offset, int _usageType);
    public static native void gl_BufferCopy(int read_buffferType, int write_buffferType, int readOffset, int writeOffset, byte[] data);
    public static native void gl_BufferCopy(int read_buffferType, int write_buffferType, int readOffset, int writeOffset, short[] data);
    public static native void gl_BufferCopy(int read_buffferType, int write_buffferType, int readOffset, int writeOffset, int[] data);
    public static native void gl_BufferCopy(int read_buffferType, int write_buffferType, int readOffset, int writeOffset, float[] data);
    public static native Pointer gl_BufferMap(int id);
    public static native void gl_BufferUnmap(int id);
    public static native int gl_BufferGetSize(int id);
    public static native int gl_BufferGetUsage(int id);

    //---------------------------
    //           Program
    //---------------------------
    public static native int gl_ProgramCreate();
    public static native void gl_ProgramDelete(int id);
    public static native void gl_ProgramLink(int id);
    public static native void gl_ProgramUse(int id);
    public static native void gl_ProgramSetTFVars(int id, String[] names, int _tfBufferMode);

    public static native void gl_ProgramAttachShader(int id, int shaderId);
    public static native void gl_ProgramDetachShader(int id, int shaderId);
    public static native int gl_ProgramGetAttachedShadersCount();
    public static native void gl_ProgramGetAttachedShaders(int[] data);

    public static native boolean gl_ProgramIsDeleted();
    public static native boolean gl_ProgramIsLinked();
    public static native boolean gl_ProgramisValidated();
    public static native String gl_ProgramGetLog();
    public static native int gl_ProgramGetBinaryLength();

    public static native int gl_ProgramGetActiveAtomicCountersBuff();

    public static native int gl_ProgramGetAttributesCount();
    public static native String gl_ProgramGetAttributeName(int id, int attributeId);
    public static native int gl_ProgramGetAttributeType(int id, int attributeId);
    public static native int gl_ProgramGetAttributeSize(int id, int attributeId);
    public static native int gl_ProgramGetAttributeId(int id, String name);

    public static native int gl_ProgramGetUniformsCount();
    public static native String gl_ProgramGetUniformName(int id, int uniformId);
    public static native int gl_ProgramGetUniformType(int id, int uniformId);
    public static native int gl_ProgramGetUniformSize(int id, int uniformId);
    public static native int gl_ProgramGetUniformId(int id, String name);

    public static native int gl_ProgramGetTFVarsCount();
    public static native int gl_ProgramTFVarsBufferMode();
    public static native String gl_ProgramGetTFVarName(int id, int uniformId);
    public static native int gl_ProgramGetTFVarType(int id, int uniformId);
    public static native int gl_ProgramGetTFVarSize(int id, int uniformId);
    public static native int gl_ProgramGetTFVarId(int id, String name);

    //public static native int gl_ProgramGetGeometryVerticesOut();
    //public static native int gl_ProgramGetGeometryInputType();
   // public static native int gl_ProgramGetGeometryOutputType();

    public static native int gl_ProgramSetUniform(int id, int uniformId, byte value);
    public static native int gl_ProgramSetUniform(int id, int uniformId, short value);
    public static native int gl_ProgramSetUniform(int id, int uniformId, int value);
    public static native int gl_ProgramSetUniform(int id, int uniformId, long value);
    public static native int gl_ProgramSetUniform(int id, int uniformId, float value);
    public static native int gl_ProgramSetUniform(int id, int uniformId, double value);
    public static native int gl_ProgramSetUniform(int id, int uniformId, byte[] value);
    public static native int gl_ProgramSetUniform(int id, int uniformId, short[] value);
    public static native int gl_ProgramSetUniform(int id, int uniformId, int[] value);
    public static native int gl_ProgramSetUniform(int id, int uniformId, long[] value);
    public static native int gl_ProgramSetUniform(int id, int uniformId, float[] value);
    public static native int gl_ProgramSetUniform(int id, int uniformId, double[] value);

    //---------------------------
    //           Shader
    //---------------------------
    public static native int gl_ShaderCreate(int _shaderType);
    public static native void gl_ShaderDelete(int id);
    public static native void gl_ShaderCompile(int id);

    public static native boolean gl_ShaderIsDeleted(int id);
    public static native boolean gl_ShaderIsCompiled(int id);
    public static native String gl_ShaderGetLog(int id);
    public static native int gl_ShaderGetType(int id);
    public static native String gl_ShaderGetSource(int id);
    public static native void gl_ShaderSetSource(int id, String content);

    //---------------------------
    //     TransformFeedback
    //---------------------------
    public static native void gl_TransformFeedbackBegin(int _polygonType);
    public static native void gl_TransformFeedbackEnd();
}