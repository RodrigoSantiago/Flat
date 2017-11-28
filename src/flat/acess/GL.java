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

    public static native void ReadPixelsB(int x, int y, int width, int height, int _format, byte[] data, int offset);
    public static native void ReadPixelsS(int x, int y, int width, int height, int _format, short[] data, int offset);
    public static native void ReadPixelsI(int x, int y, int width, int height, int _format, int[] data, int offset);
    public static native void ReadPixelsF(int x, int y, int width, int height, int _format, float[] data, int offset);
    public static native void ReadPixels(int x, int y, int width, int height, int _format, long offset);

    //---------------------------
    //         State
    //---------------------------
    public static native String GetError();

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
    public static native int GetDepthRangeNear();
    public static native int GetDepthRangeFar();

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
    public static native void SetCullface(boolean front, boolean back);
    public static native boolean GetCullfaceFront();
    public static native boolean GetCullfaceBack();
    public static native void SetFrontFace(int _clockwise);
    public static native int GetFrontFace();

    public static native void EnableSamplemask(boolean enable);
    public static native boolean IsSamplemaskEnabled();
    public static native void SetSamplemask(int mask);
    public static native int GetSamplemask();

    public static native void SetLineWidth(float width);
    public static native float GetLineWidth();

    public static native void DrawArrays(int _drawMode, int first, int count, int instances);

    public static native void DrawElements(int _drawMode, int count, int instances, int _bufferFormat, long offset, long length);
    public static native void DrawElementsB(int _drawMode, int count, int instances, byte[] indices, int offset, int length);
    public static native void DrawElementsS(int _drawMode, int count, int instances, short[] indices, int offset, int length);
    public static native void DrawElementsI(int _drawMode, int count, int instances, int[] indices, int offset, int length);

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

    //---------------------------
    //        Render Buffer
    //---------------------------
    public static native int RenderBufferCreate();
    public static native void RenderBufferDestroy(int id);
    public static native void RenderBufferBind(int id);
    public static native void RenderBufferStorage(int _imageFormat, int width, int height);
    public static native void RenderBufferStorageMultsample(int _imageFormat, int width, int height, int samples);
    public static native int RenderBufferGetImageFormat();
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

    public static native void TextureStorage(int _texTarget, int levels, int _format, int width, int height);

    public static native void TextureCopy(int _texTarget, int level, int _format, int xoffset, int yoffset, int x, int y, int width, int height, int border);
    public static native void TextureSubCopy(int _texTarget, int level, int xoffset, int yoffset, int x, int y, int width, int height);

    public static native void TextureData(int _texTarget, int level, int _format, int width, int height, int border, int _dataFormat, long offset);
    public static native void TextureDataB(int _texTarget, int level, int _format, int width, int height, int border, int _dataFormat, byte[] data, int offset);
    public static native void TextureDataI(int _texTarget, int level, int _format, int width, int height, int border, int _dataFormat, int[] data, int offset);

    public static native void TextureSubData(int _texTarget, int level, int x, int y, int width, int height, int _dataFormat, long offset);
    public static native void TextureSubDataB(int _texTarget, int level, int x, int y, int width, int height, int _dataFormat, byte[] data, int offset);
    public static native void TextureSubDataI(int _texTarget, int level, int x, int y, int width, int height, int _dataFormat, int[] data, int offset);

    public static native void TextureReadData(int _texTarget, int level, int _dataFormat, long offset);
    public static native void TextureReadDataB(int _texTarget, int level, int _dataFormat, byte[] data, int offset);
    public static native void TextureReadDataI(int _texTarget, int level, int _dataFormat, int[] data, int offset);

    public static native void TextureSetLevels(int _texTarget, int levels);
    public static native int TextureGetLevels(int _texTarget);

    public static native int TextureGetWidth(int _texTarget, int level);
    public static native int TextureGetHeight(int _texTarget, int level);
    public static native int TextureGetFormat(int _texTarget, int level);

    public static native void TextureSetDepthStencilMode(int _texTarget, int depthStencilMode);
    public static native int TextureGetDepthStencilMode(int _texTarget);
    public static native void TextureSetLOD(int _texTarget, float bias, float max, float min);
    public static native void TextureGetLODBias(int _texTarget);
    public static native void TextureGetLODMax(int _texTarget);
    public static native void TextureGetLODMin(int _texTarget);
    public static native void TextureSetFilter(int _texTarget, int mag_textureFilter, int min_textureFilter);
    public static native void TextureGetFilterMag(int _texTarget);
    public static native void TextureGetFilterMin(int _texTarget);
    public static native void TextureSetSwizzle(int _texTarget, int r_Chanel, int g_Chanel, int b_Chanel, int a_Chanel);
    public static native void TextureGetSwizzleR(int _texTarget);
    public static native void TextureGetSwizzleG(int _texTarget);
    public static native void TextureGetSwizzleB(int _texTarget);
    public static native void TextureGetSwizzleA(int _texTarget);
    public static native void TextureSetBorderColor(int _texTarget, int rgba);
    public static native int TextureGetBorderColor(int _texTarget);
    public static native void TextureSetWrap(int _texTarget, int _wrapModeS, int _wrapModeT, int _wrapModeR);
    public static native void TextureGetWrapS(int _texTarget);
    public static native void TextureGetWrapT(int _texTarget);
    public static native void TextureGetWrapR(int _texTarget);
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
    public static native void BufferDataB(int _bufferType, byte[] data, int offset, int _usageType);
    public static native void BufferDataS(int _bufferType, short[] data, int offset, int _usageType);
    public static native void BufferDataI(int _bufferType, int[] data, int offset, int _usageType);
    public static native void BufferDataF(int _bufferType, float[] data, int offset, int _usageType);
    public static native void BufferSubDataB(int _bufferType, byte[] data, int offset, long buffOffset, int _usageType);
    public static native void BufferSubDataS(int _bufferType, short[] data, int offset, long buffOffset, int _usageType);
    public static native void BufferSubDataI(int _bufferType, int[] data, int offset, long buffOffset, int _usageType);
    public static native void BufferSubDataF(int _bufferType, float[] data, int offset, long buffOffset, int _usageType);
    public static native void BufferCopy(int read_buffferType, int write_buffferType, long readOffset, long writeOffset, long length);
    public static native long BufferMap(int _bufferType, long offset, long length, int acessBimask);
    public static native void BufferUnmap(int _bufferType);
    public static native void BufferFlush(int _bufferType, long offset, long length);
    public static native int BufferGetSize(int _bufferType);
    public static native int BufferGetUsage(int _bufferType);

    //---------------------------
    //           Program
    //---------------------------
    public static native int ProgramCreate();
    public static native void ProgramDelete(int id);
    public static native void ProgramLink(int id);
    public static native void ProgramUse(int id);
    public static native boolean ProgramIsDeleted();
    public static native boolean ProgramIsLinked();
    public static native boolean ProgramisValidated();
    public static native String ProgramGetLog();

    public static native void ProgramAttachShader(int id, int shaderId);
    public static native void ProgramDetachShader(int id, int shaderId);
    public static native int ProgramGetAttachedShadersCount();
    public static native void ProgramGetAttachedShaders(int[] data);

    public static native int ProgramGetAttributesCount();
    public static native String ProgramGetAttributeName(int id, int attributeId);
    public static native int ProgramGetAttributeType(int id, int attributeId);
    public static native int ProgramGetAttributeSize(int id, int attributeId);
    public static native int ProgramGetAttributeId(int id, String name);

    public static native int ProgramGetUniformsCount();
    public static native String ProgramGetUniformName(int id, int uniformId);
    public static native int ProgramGetUniformType(int id, int uniformId);
    public static native int ProgramGetUniformSize(int id, int uniformId);
    public static native int ProgramGetUniformId(int id, String name);

    public static native void ProgramSetTFVars(int id, String[] names, int _tfBufferMode);
    public static native int ProgramGetTFVarsCount();
    public static native int ProgramTFVarsBufferMode();
    public static native String ProgramGetTFVarName(int id, int uniformId);
    public static native int ProgramGetTFVarType(int id, int uniformId);
    public static native int ProgramGetTFVarSize(int id, int uniformId);
    public static native int ProgramGetTFVarId(int id, String name);

    //public static native int ProgramGetGeometryVerticesOut();
    //public static native int ProgramGetGeometryInputType();
   // public static native int ProgramGetGeometryOutputType();

    public static native int ProgramSetUniformB(int id, int uniformId, byte value);
    public static native int ProgramSetUniformS(int id, int uniformId, short value);
    public static native int ProgramSetUniformI(int id, int uniformId, int value);
    public static native int ProgramSetUniformL(int id, int uniformId, long value);
    public static native int ProgramSetUniformF(int id, int uniformId, float value);
    public static native int ProgramSetUniformD(int id, int uniformId, double value);
    public static native int ProgramSetUniformBv(int id, int uniformId, byte[] value, int offset);
    public static native int ProgramSetUniformSv(int id, int uniformId, short[] value, int offset);
    public static native int ProgramSetUniformIv(int id, int uniformId, int[] value, int offset);
    public static native int ProgramSetUniformLv(int id, int uniformId, long[] value, int offset);
    public static native int ProgramSetUniformFv(int id, int uniformId, float[] value, int offset);
    public static native int ProgramSetUniformDv(int id, int uniformId, double[] value, int offset);

    //---------------------------
    //           Shader
    //---------------------------
    public static native int ShaderCreate(int _shaderType);
    public static native void ShaderDelete(int id);
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