package flat.backend;

import java.nio.Buffer;

public class GL {
    //---------------------------
    //         Rendering
    //---------------------------
    public static native void Flush();
    public static native void Finish();

    public static native void Clear(int maskCB);
    public static native void SetClearColor(int rgba);
    public static native void SetClearDepth(int mask);
    public static native void SetClearStencil(int mask);
    public static native int GetClearColor();
    public static native int GetClearDepth();
    public static native int GetClearStencil();

    public static native void ReadPixels(int x, int y, int width, int height, int typeDT, long offset);
    public static native void ReadPixelsB(int x, int y, int width, int height, byte[] data, int offset);
    public static native void ReadPixelsS(int x, int y, int width, int height, short[] data, int offset);
    public static native void ReadPixelsI(int x, int y, int width, int height, int[] data, int offset);
    public static native void ReadPixelsF(int x, int y, int width, int height, float[] data, int offset);
    public static native void ReadPixelsBuffer(int x, int y, int width, int height, int typeDT, Buffer buffer, int offset);

    //---------------------------
    //         State
    //---------------------------
    public static native int GetError();

    public static native void SetHint(int targetHT, int modeHM);
    public static native int GetHint(int targetHT);

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
    public static native void SetPixelStore(int targetPS, int value);
    public static native int GetPixelStore(int targetPS);
    public static native void SetColorMask(boolean r, boolean g, boolean b, boolean a);
    public static native boolean GetColorMaskR();
    public static native boolean GetColorMaskG();
    public static native boolean GetColorMaskB();
    public static native boolean GetColorMaskA();

    public static native void EnableDepthTest(boolean enable);
    public static native boolean IsDepthTestEnabled();
    public static native void SetDepthMask(boolean mask);
    public static native boolean GetDepthMask();
    public static native void SetDepthFunction(int functionMF);
    public static native int GetDepthFunction();
    public static native void SetDepthRange(double nearValue, double farValue);
    public static native double GetDepthRangeNear();
    public static native double GetDepthRangeFar();

    public static native void EnableStencilTest(boolean enable);
    public static native boolean IsStencilTestEnabled();
    public static native void SetStencilMask(int faceFC, int mask);
    public static native int GetStencilMask(int faceFG);
    public static native void SetStencilFunction(int faceFC, int functionMF, int ref, int mask);
    public static native int GetStencilFunction(int faceFG);
    public static native int GetStencilFunctionRef(int faceFG);
    public static native int GetStencilFunctionMask(int faceFG);
    public static native void SetStencilOperation(int faceFC, int stencilFailMO, int depthFailMO, int depthPassMO);
    public static native int GetStencilOperationStencilFail(int faceFG);
    public static native int GetStencilOperationDepthFail(int faceFG);
    public static native int GetStencilOperationDepthPass(int faceFG);

    public static native void EnableBlend(boolean enable);
    public static native boolean IsBlendEnabled();
    public static native void SetBlendFunction(int rgbSrcBF, int rgbDstBF, int alphaSrcBF, int alphaDstBF);
    public static native int GetBlendFunctionSrcRGB();
    public static native int GetBlendFunctionDstRGB();
    public static native int GetBlendFunctionSrcAlpha();
    public static native int GetBlendFunctionDstAlpha();
    public static native void SetBlendEquation(int rgbBE, int alphaBE);
    public static native int GetBlendEquationRGB();
    public static native int GetBlendEquationAlpha();
    public static native void SetBlendColor(int rgba);
    public static native int GetBlendColor();

    public static native void EnableCullface(boolean enable);
    public static native boolean IsCullfaceEnabled();
    public static native void SetCullface(int faceFC);
    public static native int GetCullface();
    public static native void SetFrontFace(int frontFaceFF);
    public static native int GetFrontFace();

    public static native void EnableMultisample(boolean enable);
    public static native boolean IsMultisampleEnabled();

    public static native void SetLineWidth(float width);
    public static native float GetLineWidth();

    //---------------------------
    //        Draw
    //---------------------------
    public static native void DrawArrays(int vertexModeVM, int first, int count, int instances);

    public static native void DrawElements(int vertexModeVM, int count, int instances, int typeDT, long offset);
    public static native void DrawElementsB(int vertexModeVM, int count, int instances, byte[] indices, int offset);
    public static native void DrawElementsS(int vertexModeVM, int count, int instances, short[] indices, int offset);
    public static native void DrawElementsI(int vertexModeVM, int count, int instances, int[] indices, int offset);
    public static native void DrawElementsBuffer(int vertexModeVM, int count, int instances, int typeDT, Buffer buffer, int offset);

    //---------------------------
    //        Frame Buffer
    //---------------------------
    public static native int FrameBufferCreate();
    public static native void FrameBufferDestroy(int id);
    public static native void FrameBufferBind(int trgFB, int id);
    public static native int FrameBufferGetBound(int trgFB);
    public static native void FrameBufferBlit(int srcX, int srcY, int srcW, int srcH, int dstX, int dstY, int dstW, int dstH, int bitmaskBM, int filterBF);
    public static native void FrameBufferTexture2D(int trgFB, int attFA, int texTypeTT, int textureId, int level);
    public static native void FrameBufferTextureMultisample(int trgFB, int attFA, int textureId);
    public static native void FrameBufferRenderBuffer(int trgFB, int attFA, int renderBufferId);
    public static native int FrameBufferGetStatus(int trgFB);
    public static native int FrameBufferGetObjectType(int trgFB, int attFA);
    public static native int FrameBufferGetObjectId(int trgFB, int attaFA);
    public static native void FrameBufferGetPixelDataSize(int trgFB, int attFA, int[] data6);

    public static native void FrameBufferSetTargets(int c0FA, int c1FA, int c2FA, int c3FA, int c4FA, int c5FA, int c6FA, int c7FA);
    public static native void FrameBufferGetTargets(int[] data8);

    //---------------------------
    //        Render Buffer
    //---------------------------
    public static native int RenderBufferCreate();
    public static native void RenderBufferDestroy(int id);
    public static native void RenderBufferBind(int id);
    public static native int RenderBufferGetBound();
    public static native void RenderBufferStorage(int frormatTF, int width, int height);
    public static native void RenderBufferStorageMultsample(int frormatTF, int samples, int width, int height);
    public static native int RenderBufferGetFormat();
    public static native int RenderBufferGetWidth();
    public static native int RenderBufferGetHeight();

    //---------------------------
    //         Textures
    //---------------------------
    public static native void SetActiveTexture(int pos);
    public static native int GetActiveTexture();

    public static native int TextureCreate();
    public static native void TextureDestroy(int id);
    public static native void TextureBind(int trgTB, int id);
    public static native int TextureGetBound(int trgTB);
    public static native void TextureGenerateMipmap(int trgTB);

    public static native void TextureMultisample(int samples, int formatTF, int width, int height, boolean fixedLocations);

    public static native void TextureCopy(int trgTT, int level, int formatTF, int x, int y, int width, int height, int border);
    public static native void TextureSubCopy(int trgTT, int level, int xoffset, int yoffset, int x, int y, int width, int height);

    public static native void TextureData(int trgTT, int level, int formatTF, int width, int height, int border, long offset);
    public static native void TextureDataB(int trgTT, int level, int formatTF, int width, int height, int border, byte[] data, int offset);
    public static native void TextureDataS(int trgTT, int level, int formatTF, int width, int height, int border, short[] data, int offset);
    public static native void TextureDataI(int trgTT, int level, int formatTF, int width, int height, int border, int[] data, int offset);
    public static native void TextureDataBuffer(int trgTT, int level, int formatTF, int width, int height, int border, Buffer buffer, int offset);

    public static native void TextureSubData(int trgTT, int level, int x, int y, int width, int height, int dataFormatTF, long offset);
    public static native void TextureSubDataB(int trgTT, int level, int x, int y, int width, int height, int dataFormatTF, byte[] data, int offset);
    public static native void TextureSubDataS(int trgTT, int level, int x, int y, int width, int height, int dataFormatTF, short[] data, int offset);
    public static native void TextureSubDataI(int trgTT, int level, int x, int y, int width, int height, int dataFormatTF, int[] data, int offset);
    public static native void TextureSubDataBuffer(int trgTT, int level, int x, int y, int width, int height, int dataFormatTF, Buffer buffer, int offset);

    public static native void TextureSetLevels(int trgTB, int levels);
    public static native int TextureGetLevels(int trgTB);

    public static native int TextureGetWidth(int trgTB, int level);
    public static native int TextureGetHeight(int trgTB, int level);
    public static native int TextureGetFormat(int trgTB, int level);

    public static native void TextureSetLOD(int trgTB, float bias, float max, float min);
    public static native float TextureGetLODBias(int trgTB);
    public static native float TextureGetLODMax(int trgTB);
    public static native float TextureGetLODMin(int trgTB);
    public static native void TextureSetFilter(int trgTB, int magFilterIF, int minFilterIF);
    public static native int TextureGetFilterMag(int trgTB);
    public static native int TextureGetFilterMin(int trgTB);
    public static native void TextureSetSwizzle(int trgTB, int rChanelCC, int gChanelCC, int bChanelCC, int aChanelCC);
    public static native int TextureGetSwizzleR(int trgTB);
    public static native int TextureGetSwizzleG(int trgTB);
    public static native int TextureGetSwizzleB(int trgTB);
    public static native int TextureGetSwizzleA(int trgTB);
    public static native void TextureSetBorderColor(int trgTB, int rgba);
    public static native int TextureGetBorderColor(int trgTB);
    public static native void TextureSetWrap(int trgTB, int horizontalIW, int verticalIW);
    public static native int TextureGetWrapHorizontal(int trgTB);
    public static native int TextureGetWrapVertical(int trgTB);
    public static native void TextureSetCompareFunction(int trgTB, int functionMF);
    public static native int TextureGetCompareFunction(int trgTB);
    public static native void TextureSetCompareMode(int trgTB, int compareModeCM);
    public static native int TextureGetCompareMode(int trgTB);

    //---------------------------
    //         Buffers
    //---------------------------
    public static native int BufferCreate();
    public static native void BufferDestroy(int id);
    public static native void BufferBind(int trgBB, int id);
    public static native void BufferBindRange(int trgBB, int id, int buffer, int offset, int length);
    public static native int BufferGetBound(int trgBB);

    public static native void BufferDataB(int trgBB, byte[] data, int offset, int length, int usageTypeUT);
    public static native void BufferDataS(int trgBB, short[] data, int offset, int length, int usageTypeUT);
    public static native void BufferDataI(int trgBB, int[] data, int offset, int length, int usageTypeUT);
    public static native void BufferDataF(int trgBB, float[] data, int offset, int length, int usageTypeUT);
    public static native void BufferDataBuffer(int trgBB, Buffer buffer, int offset, int length, int usageTypeUT);

    public static native void BufferSubDataB(int trgBB, byte[] data, int offset, int length, long buffOffset);
    public static native void BufferSubDataS(int trgBB, short[] data, int offset, int length, long buffOffset);
    public static native void BufferSubDataI(int trgBB, int[] data, int offset, int length, long buffOffset);
    public static native void BufferSubDataF(int trgBB, float[] data, int offset, int length, long buffOffset);
    public static native void BufferSubDataBuffer(int trgBB, Buffer data, int offset, int length, long buffOffset);

    public static native void BufferReadDataB(int trgBB, byte[] data, int offset, int length, long buffOffset);
    public static native void BufferReadDataS(int trgBB, short[] data, int offset, int length, long buffOffset);
    public static native void BufferReadDataI(int trgBB, int[] data, int offset, int length, long buffOffset);
    public static native void BufferReadDataF(int trgBB, float[] data, int offset, int length, long buffOffset);
    public static native void BufferReadDataBuffer(int trgBB, Buffer data, int offset, int length, long buffOffset);

    public static native void BufferCopy(int readTargetBB, int writeTargetBB, long readOffset, long writeOffset, long length);
    public static native long BufferMap(int trgBB, long offset, long length, int acessBimaskAM);
    public static native void BufferUnmap(int trgBB);
    public static native void BufferFlush(int trgBB, long offset, long length);
    public static native int BufferGetSize(int trgBB);
    public static native int BufferGetUsage(int trgBB);
    public static native int BufferGetAcess(int trgBB);
    public static native boolean BufferIsMapped(int trgBB);

    //---------------------------
    //           Program
    //---------------------------
    public static native int ProgramCreate();
    public static native void ProgramDestroy(int id);
    public static native void ProgramLink(int id);
    public static native void ProgramUse(int id);
    public static native int ProgramGetUsed();
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

    public static native int ProgramGetUniformBlocksCount(int id);
    public static native String ProgramGetUniformBlockName(int id, int blockId);
    public static native int ProgramGetUniformBlockBinding(int id, int blockId);
    public static native int ProgramGetUniformBlockSize(int id, int blockId);
    public static native int ProgramGetUniformBlockId(int id, String name);
    public static native void ProgramUniformBlockBinding(int id, int blockId, int blockBind);
    public static native int ProgramGetUniformBlockChildrenCount(int id, int blockId);
    public static native void ProgramGetUniformBlockChildren(int id, int blockId, int[] data);

    public static native void ProgramSetTFVars(int id, String[] names, int bufferModeFM);
    public static native int ProgramGetTFVarsCount(int id);
    public static native int ProgramTFVarsBufferMode(int id);
    public static native String ProgramGetTFVarName(int id, int tfVarId);
    public static native int ProgramGetTFVarType(int id, int tfVarId);
    public static native int ProgramGetTFVarSize(int id, int tfVarId);
    public static native int ProgramGetTFVarId(int id, String name);

    //public static native int ProgramGetGeometryVerticesOut(int id);
    //public static native int ProgramGetGeometryInputType(int id);
    //public static native int ProgramGetGeometryOutputType(int id);

    public static native void ProgramSetUniformI(int uniformId, int attSize, int arrSize, int[] value, int offset);
    public static native void ProgramSetUniformF(int uniformId, int attSize, int arrSize, float[] value, int offset);
    public static native void ProgramSetUniformMatrix(int uniformId, int w, int h, int arrSize, boolean transpose, float[] value, int offset);
    public static native void ProgramSetUniformBuffer(int uniformId, int attSize, int arrSize, int typeDT, Buffer buffer, int offset);

    public static native void ProgramGetUniformI(int id, int uniformId, int[] value, int offset);
    public static native void ProgramGetUniformF(int id, int uniformId, float[] value, int offset);
    public static native void ProgramGetUniformBuffer(int id, int uniformId, int typeDT, Buffer buffer, int offset);

    //---------------------------
    //         Vertex
    //---------------------------
    public static native int VertexArrayCreate();
    public static native void VertexArrayDestroy(int id);
    public static native void VertexArrayBind(int id);
    public static native int VertexArrayGetBound();

    public static native void VertexArrayAttribEnable(int attrId, boolean enable);
    public static native boolean VertexArrayAttribIsEnabled(int attrId);
    public static native void VertexArrayAttribSetDivisor(int attrId, int divisor);
    public static native int VertexArrayAttribGetDivisor(int attrId);

    public static native void VertexArrayAttribPointer(int attrId, int attSize, boolean normalized, long stride, int typeDT, long offset);
    public static native void VertexArrayAttribPointerB(int attrId, int attSize, boolean normalized, int stride, byte[] data, int offset);
    public static native void VertexArrayAttribPointerS(int attrId, int attSize, boolean normalized, int stride, short[] data, int offset);
    public static native void VertexArrayAttribPointerI(int attrId, int attSize, boolean normalized, int stride, int[] data, int offset);
    public static native void VertexArrayAttribPointerF(int attrId, int attSize, boolean normalized, int stride, float[] data, int offset);
    public static native void VertexArrayAttribPointerD(int attrId, int attSize, boolean normalized, int stride, double[] data, int offset);
    public static native void VertexArrayAttribPointerBuffer(int attrId, int attSize, boolean normalized, int stride, int typeDT, Buffer buffer, int offset);

    //---------------------------
    //           Shader
    //---------------------------
    public static native int ShaderCreate(int typeST);
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
    public static native void TransformFeedbackBegin(int polygonTypeFP);
    public static native void TransformFeedbackEnd();
}