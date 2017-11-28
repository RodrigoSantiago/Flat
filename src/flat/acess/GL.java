package flat.acess;

public class GL {
    static {
        System.loadLibrary("flat");
    }

    public static void load() {
        System.out.println("Graphic Layer Library loaded");
    }

    //---------------------------
    //         Rendering
    //---------------------------
    public static native void Flush();
    public static native void Finish();

    public static native void Clear(int bitmask);
    public static native void SetClearColor(int rgba);
    public static native void SetClearDepth(int mask);
    public static native void SetClearStencil(int mask);
    public static native int GetClearColor();
    public static native int GetClearDepth();
    public static native int GetClearStencil();

    public static native void ReadPixels(int x, int y, int width, int height, int _format, int type, long offset);
    public static native void ReadPixelsB(int x, int y, int width, int height, int _format, byte[] data, int offset);
    public static native void ReadPixelsS(int x, int y, int width, int height, int _format, short[] data, int offset);
    public static native void ReadPixelsI(int x, int y, int width, int height, int _format, int[] data, int offset);
    public static native void ReadPixelsF(int x, int y, int width, int height, int _format, float[] data, int offset);

    //---------------------------
    //         State
    //---------------------------
    public static native int GetError();

    public static native void SetHint(int target, int mode);
    public static native int GetHint(int target);

    public static native void SetViewport(int x, int y, int width, int height);
    public static native int GetViewportX();
    public static native int GetViewportY();
    public static native int GetViewportWidth();
    public static native int GetViewportHeight();

    public static native void EnableScissorTest(boolean enable);
    public static native boolean IsScissorTestEnabled();
    public static native void SetScissor(int x, int y, int width, int height);
    public static native int GetScissorX();
    public static native int GetScissorY();
    public static native int GetScissorWidth();
    public static native int GetScissorHeight();

    public static native void EnableRasterizer(boolean enable);
    public static native boolean IsRasterizerEnabled();
    public static native void SetPixelStore(int target, int value);
    public static native int GetPixelStore(int target);
    public static native void SetColorMask(boolean r, boolean g, boolean b, boolean a);
    public static native boolean GetColorMaskR();
    public static native boolean GetColorMaskG();
    public static native boolean GetColorMaskB();
    public static native boolean GetColorMaskA();

    public static native void EnableDepthTest(boolean enable);
    public static native boolean IsDepthTestEnabled();
    public static native void SetDepthMask(boolean mask);
    public static native boolean GetDepthMask();
    public static native void SetDepthFunction(int _mathFunction);
    public static native int GetDepthFunction();
    public static native void SetDepthRange(double nearValue, double farValue);
    public static native double GetDepthRangeNear();
    public static native double GetDepthRangeFar();

    public static native void EnableStencilTest(boolean enable);
    public static native boolean IsStencilTestEnabled();
    public static native void SetStencilMask(int _face, int mask);
    public static native int GetStencilMask(int _face);
    public static native void SetStencilFunction(int _face, int _mathFunction, int ref, int mask);
    public static native int GetStencilFunction(int _face);
    public static native int GetStencilFunctionRef(int _face);
    public static native int GetStencilFunctionMask(int _face);
    public static native void SetStencilOperation(int _face, int stencil_mathOperation, int depth_mathOperation, int both_mathOperation);
    public static native int GetStencilOperationStencil(int _face);
    public static native int GetStencilOperationDepth(int _face);
    public static native int GetStencilOperationBoth(int _face);

    public static native void EnableBlend(boolean enable);
    public static native boolean IsBlendEnabled();
    public static native void SetBlendFunction(int srcRGB_blend, int dstRGB_blend, int srcA_blend, int dstA_blend);
    public static native int GetBlendFunctionSrcRGB();
    public static native int GetBlendFunctionDstRGB();
    public static native int GetBlendFunctionSrcAlpha();
    public static native int GetBlendFunctionDstAlpha();
    public static native void SetBlendEquation(int RGB_blendEquation, int Alpha_blendEquation);
    public static native int GetBlendEquationRGB();
    public static native int GetBlendEquationAlpha();
    public static native void SetBlendColor(int rgba);
    public static native int GetBlendColor();

    public static native void EnableCullface(boolean enable);
    public static native boolean IsCullfaceEnabled();
    public static native void SetCullface(int _face);
    public static native int GetCullface();
    public static native void SetFrontFace(int _clockwise);
    public static native int GetFrontFace();

    public static native void EnableMultisample(boolean enable);
    public static native boolean IsMultisampleEnabled();

    public static native void SetLineWidth(float width);
    public static native float GetLineWidth();

    public static native void DrawArrays(int _drawMode, int first, int count, int instances);

    public static native void DrawElements(int _drawMode, int count, int instances, int _bufferFormat, long offset);
    public static native void DrawElementsB(int _drawMode, int count, int instances, byte[] indices, int offset);
    public static native void DrawElementsS(int _drawMode, int count, int instances, short[] indices, int offset);
    public static native void DrawElementsI(int _drawMode, int count, int instances, int[] indices, int offset);

    //---------------------------
    //        Frame Buffer
    //---------------------------
    public static native int FrameBufferCreate();
    public static native void FrameBufferDestroy(int id);
    public static native void FrameBufferBind(int _target, int id);
    public static native void FrameBufferBlit(int srcX, int srcY, int srcW, int srcH, int dstX, int dstY, int dstW, int dstH, int bitmask, int _filter);
    public static native void FrameBufferTexture2D(int _target, int _attachment, int _textureTarget, int textureId, int level);
    public static native void FrameBufferRenderBuffer(int _target, int _attachment, int renderBufferId);
    public static native int FrameBufferGetStatus(int _target);
    public static native int FrameBufferGetObjectType(int _target, int _attachment);
    public static native int FrameBufferGetObjectId(int _target, int _attachment);
    public static native void FrameBufferGetPixelDataSize(int _target, int _attachment, int[] data6);

    //---------------------------
    //        Render Buffer
    //---------------------------
    public static native int RenderBufferCreate();
    public static native void RenderBufferDestroy(int id);
    public static native void RenderBufferBind(int id);
    public static native void RenderBufferStorage(int _imageFormat, int width, int height);
    public static native void RenderBufferStorageMultsample(int _imageFormat, int samples, int width, int height);
    public static native int RenderBufferGetFormat();
    public static native int RenderBufferGetWidth();
    public static native int RenderBufferGetHeight();

    //---------------------------
    //         Textures
    //---------------------------
    public static native void SetActiveTexture(int pos);

    public static native int TextureCreate();
    public static native void TextureDestroy(int id);
    public static native void TextureBind(int _texType, int id);
    public static native void TextureGenerateMipmap(int id);

    public static native void TextureCopy(int _texTarget, int level, int _format, int x, int y, int width, int height, int border);
    public static native void TextureSubCopy(int _texTarget, int level, int xoffset, int yoffset, int x, int y, int width, int height);

    public static native void TextureData(int _texTarget, int level, int _format, int width, int height, int border, int _dataFormat, int _type, long offset);
    public static native void TextureDataB(int _texTarget, int level, int _format, int width, int height, int border, int _dataFormat, byte[] data, int offset);
    public static native void TextureDataS(int _texTarget, int level, int _format, int width, int height, int border, int _dataFormat, short[] data, int offset);
    public static native void TextureDataI(int _texTarget, int level, int _format, int width, int height, int border, int _dataFormat, int[] data, int offset);

    public static native void TextureSubData(int _texTarget, int level, int x, int y, int width, int height, int _dataFormat, int _type, long offset);
    public static native void TextureSubDataB(int _texTarget, int level, int x, int y, int width, int height, int _dataFormat, byte[] data, int offset);
    public static native void TextureSubDataS(int _texTarget, int level, int x, int y, int width, int height, int _dataFormat, short[] data, int offset);
    public static native void TextureSubDataI(int _texTarget, int level, int x, int y, int width, int height, int _dataFormat, int[] data, int offset);

    public static native void TextureSetLevels(int _texTarget, int levels);
    public static native int TextureGetLevels(int _texTarget);

    public static native int TextureGetWidth(int _texTarget, int level);
    public static native int TextureGetHeight(int _texTarget, int level);
    public static native int TextureGetFormat(int _texTarget, int level);

    public static native void TextureSetLOD(int _texTarget, float bias, float max, float min);
    public static native float TextureGetLODBias(int _texTarget);
    public static native float TextureGetLODMax(int _texTarget);
    public static native float TextureGetLODMin(int _texTarget);
    public static native void TextureSetFilter(int _texTarget, int mag_textureFilter, int min_textureFilter);
    public static native int TextureGetFilterMag(int _texTarget);
    public static native int TextureGetFilterMin(int _texTarget);
    public static native void TextureSetSwizzle(int _texTarget, int r_Chanel, int g_Chanel, int b_Chanel, int a_Chanel);
    public static native int TextureGetSwizzleR(int _texTarget);
    public static native int TextureGetSwizzleG(int _texTarget);
    public static native int TextureGetSwizzleB(int _texTarget);
    public static native int TextureGetSwizzleA(int _texTarget);
    public static native void TextureSetBorderColor(int _texTarget, int rgba);
    public static native int TextureGetBorderColor(int _texTarget);
    public static native void TextureSetWrap(int _texTarget, int horizontal_wrapMode, int vertical_wrapMode);
    public static native int TextureGetWrapHorizontal(int _texTarget);
    public static native int TextureGetWrapVertical(int _texTarget);
    public static native void TextureSetCompareFunction(int _texTarget, int _mathFunction);
    public static native int TextureGetCompareFunction(int _texTarget);
    public static native void TextureSetCompareMode(int _texTarget, int _compareMode);
    public static native int TextureGetCompareMode(int _texTarget);

    //---------------------------
    //         Buffers
    //---------------------------
    public static native int BufferCreate();
    public static native void BufferDestroy(int id);
    public static native void BufferBind(int _bufferType, int id);
    public static native void BufferDataB(int _bufferType, byte[] data, int offset, int length, int _usageType);
    public static native void BufferDataS(int _bufferType, short[] data, int offset, int length, int _usageType);
    public static native void BufferDataI(int _bufferType, int[] data, int offset, int length, int _usageType);
    public static native void BufferDataF(int _bufferType, float[] data, int offset, int length, int _usageType);
    public static native void BufferSubDataB(int _bufferType, byte[] data, int offset, int length, long buffOffset);
    public static native void BufferSubDataS(int _bufferType, short[] data, int offset, int length, long buffOffset);
    public static native void BufferSubDataI(int _bufferType, int[] data, int offset, int length, long buffOffset);
    public static native void BufferSubDataF(int _bufferType, float[] data, int offset, int length, long buffOffset);
    public static native void BufferReadDataB(int _bufferType, byte[] data, int offset, int length, long buffOffset);
    public static native void BufferReadDataS(int _bufferType, short[] data, int offset, int length, long buffOffset);
    public static native void BufferReadDataI(int _bufferType, int[] data, int offset, int length, long buffOffset);
    public static native void BufferReadDataF(int _bufferType, float[] data, int offset, int length, long buffOffset);
    public static native void BufferCopy(int read_buffferType, int write_buffferType, long readOffset, long writeOffset, long length);
    public static native long BufferMap(int _bufferType, long offset, long length, int acessBimask);
    public static native void BufferUnmap(int _bufferType);
    public static native void BufferFlush(int _bufferType, long offset, long length);
    public static native int BufferGetSize(int _bufferType);
    public static native int BufferGetUsage(int _bufferType);
    public static native int BufferGetAcess(int _bufferType);
    public static native boolean BufferIsMapped(int _bufferType);

    //---------------------------
    //           Program
    //---------------------------
    public static native int ProgramCreate();
    public static native void ProgramDestroy(int id);
    public static native void ProgramLink(int id);
    public static native void ProgramUse(int id);
    public static native boolean ProgramIsDeleted(int id);
    public static native boolean ProgramIsLinked(int id);
    public static native boolean ProgramisValidated(int id);
    public static native String ProgramGetLog(int id);

    public static native void ProgramAttachShader(int id, int shaderId);
    public static native void ProgramDetachShader(int id, int shaderId);
    public static native int ProgramGetAttachedShadersCount(int id);
    public static native void ProgramGetAttachedShaders(int id, int[] data);

    public static native int ProgramGetAttributesCount(int id);
    public static native String ProgramGetAttributeName(int id, int attributeId);
    public static native int ProgramGetAttributeType(int id, int attributeId);
    public static native int ProgramGetAttributeSize(int id, int attributeId);
    public static native int ProgramGetAttributeId(int id, String name);

    public static native int ProgramGetUniformsCount(int id);
    public static native String ProgramGetUniformName(int id, int uniformId);
    public static native int ProgramGetUniformType(int id, int uniformId);
    public static native int ProgramGetUniformSize(int id, int uniformId);
    public static native int ProgramGetUniformId(int id, String name);

    public static native void ProgramSetTFVars(int id, String[] names, int _tfBufferMode);
    public static native int ProgramGetTFVarsCount(int id);
    public static native int ProgramTFVarsBufferMode(int id);
    public static native String ProgramGetTFVarName(int id, int tfVarId);
    public static native int ProgramGetTFVarType(int id, int tfVarId);
    public static native int ProgramGetTFVarSize(int id, int tfVarId);
    public static native int ProgramGetTFVarId(int id, String name);

    //public static native int ProgramGetGeometryVerticesOut(int id);
    //public static native int ProgramGetGeometryInputType(int id);
    //public static native int ProgramGetGeometryOutputType(int id);

    public static native void ProgramSetUniformI(int uniformId, int value);
    public static native void ProgramSetUniformF(int uniformId, float value);
    public static native void ProgramSetUniformIv(int uniformId, int[] value, int offset, int length);
    public static native void ProgramSetUniformFv(int uniformId, float[] value, int offset, int length);

    public static native int ProgramGetUniformI(int id, int uniformId);
    public static native float ProgramGetUniformF(int id, int uniformId);

    //---------------------------
    //         Vertex
    //---------------------------
    public static native int VertexArrayCreate();
    public static native void VertexArrayDestroy(int id);
    public static native void VertexArrayBind(int id);

    public static native void VertexAttribEnable(int attrId, boolean enable);
    public static native boolean VertexAttribIsEnabled(int attrId);
    public static native void VertexAttribSetDivisor(int attrId, int divisor);
    public static native int VertexAttribGetDivisor(int attrId);

    public static native void VertexAttribSetPointer(int attrId, int count, boolean normalized, long stride, int _type, long offset);
    public static native void VertexAttribSetPointerB(int attrId, int count, boolean normalized, int stride, byte[] data, int offset);
    public static native void VertexAttribSetPointerS(int attrId, int count, boolean normalized, int stride, short[] data, int offset);
    public static native void VertexAttribSetPointerI(int attrId, int count, boolean normalized, int stride, int[] data, int offset);
    public static native void VertexAttribSetPointerF(int attrId, int count, boolean normalized, int stride, float[] data, int offset);
    public static native void VertexAttribSetPointerD(int attrId, int count, boolean normalized, int stride, double[] data, int offset);

    //---------------------------
    //           Shader
    //---------------------------
    public static native int ShaderCreate(int _shaderType);
    public static native void ShaderDestroy(int id);
    public static native void ShaderCompile(int id);

    public static native boolean ShaderIsDeleted(int id);
    public static native boolean ShaderIsCompiled(int id);
    public static native String ShaderGetLog(int id);
    public static native int ShaderGetType(int id);
    public static native void ShaderSetSource(int id, String content);
    public static native String ShaderGetSource(int id);

    //---------------------------
    //    Transform Feedback
    //---------------------------
    public static native void TransformFeedbackBegin(int _polygonType);
    public static native void TransformFeedbackEnd();
}