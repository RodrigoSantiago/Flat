package flat.graphics;

import flat.backend.GL;
import static flat.backend.GLEnuns.*;

public class Shader extends ContextObject {

    private String vtxCode, frgCode, vtxLog, frgLog, log;
    private boolean compiled;

    int shaderProgramId;

    public Shader() {
    }

    public Shader(String vertex, String fragment) {
        setVertexSorce(vertex);
        setFragmentSorce(fragment);
    }

    public void setInt(String name, int value) {
        compile();
        int id = GL.ProgramGetUniformId(shaderProgramId, name);
        GL.ProgramSetUniformI(id, 1, 1, new int[] {value}, 0);
    }

    @Override
    protected void onDispose() {
        clearCompilation();
    }

    public String getFragmentSource() {
        return frgCode;
    }

    public void setFragmentSorce(String fragmentCode) {
        frgCode = fragmentCode;
        clearCompilation();
    }

    public String getVertexSorce() {
        return frgCode;
    }

    public void setVertexSorce(String vertexCode) {
        vtxCode = vertexCode;
        clearCompilation();
    }

    public boolean isCompiled() {
        return compiled;
    }

    private void clearCompilation() {
        Context.getContext();

        if (shaderProgramId != 0) {
            GL.ProgramDestroy(shaderProgramId);
            shaderProgramId = 0;
            frgLog = null;
            vtxLog = null;
        }
        compiled = false;
    }

    public void compile() {
        Context.getContext();
        if  (isCompiled()) return;

        int frg = GL.ShaderCreate(ST_FRAGMENT_SHADER);
        GL.ShaderSetSource(frg, frgCode);
        GL.ShaderCompile(frg);
        if (!GL.ShaderIsCompiled(frg)) {
            frgLog = GL.ShaderGetLog(frg);
        }

        int vtx = GL.ShaderCreate(ST_VERTEX_SHADER);
        GL.ShaderSetSource(vtx, vtxCode);
        GL.ShaderCompile(vtx);
        if (!GL.ShaderIsCompiled(vtx)) {
            vtxLog = GL.ShaderGetLog(vtx);
        }

        if (GL.ShaderIsCompiled(frg) && GL.ShaderIsCompiled(vtx)) {
            shaderProgramId = GL.ProgramCreate();
            GL.ProgramAttachShader(shaderProgramId, frg);
            GL.ProgramAttachShader(shaderProgramId, vtx);
            GL.ProgramLink(shaderProgramId);
            if (GL.ProgramIsLinked(shaderProgramId)) {
                compiled = true;
            } else {
                log = GL.ProgramGetLog(shaderProgramId);
            }
        }

        GL.ShaderDestroy(frg);
        GL.ShaderDestroy(vtx);
    }

    public String getVertexLog() {
        return vtxLog;
    }

    public String getFragmentLog() {
        return frgLog;
    }

    public String getLog() {
        return log;
    }
}
