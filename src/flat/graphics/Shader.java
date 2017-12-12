package flat.graphics;

import flat.backend.GL;
import static flat.backend.GLEnuns.*;

public class Shader extends ContextObject {

    private String vtxCode, frgCode, vtxLog, frgLog, linkLog;
    private boolean compiled;
    private final int[] passInt = new int[16];
    private final float[] passFloat = new float[16];

    int shaderProgramId;

    public Shader() {
    }

    public Shader(String vertex, String fragment) {
        setVertexSorce(vertex);
        setFragmentSorce(fragment);
    }

    public void setInt(String name, int value) {
        int id = GL.ProgramGetUniformId(shaderProgramId, name);
        passInt[0] = value;
        GL.ProgramSetUniformI(id, 1, 1, passInt, 0);
    }

    public void setFloat(String name, float value) {
        int id = GL.ProgramGetUniformId(shaderProgramId, name);
        passFloat[0] = value;
        GL.ProgramSetUniformF(id, 1, 1, passFloat, 0);
    }

    public void setVec2(String name, float v1, float v2) {
        int id = GL.ProgramGetUniformId(shaderProgramId, name);
        passFloat[0] = v1;
        passFloat[1] = v2;
        GL.ProgramSetUniformF(id, 2, 1, passFloat, 0);
    }

    public void setVec3(String name, float v1, float v2, float v3) {
        int id = GL.ProgramGetUniformId(shaderProgramId, name);
        passFloat[0] = v1;
        passFloat[1] = v2;
        passFloat[2] = v3;
        GL.ProgramSetUniformF(id, 3, 1, passFloat, 0);
    }

    public void setVec4(String name, float v1, float v2, float v3, float v4) {
        int id = GL.ProgramGetUniformId(shaderProgramId, name);
        passFloat[0] = v1;
        passFloat[1] = v2;
        passFloat[2] = v3;
        passFloat[3] = v4;
        GL.ProgramSetUniformF(id, 4, 1, passFloat, 0);
    }

    public void setColor4F(String name, int rgba) {
        int id = GL.ProgramGetUniformId(shaderProgramId, name);
        passFloat[0] = ((rgba >> 24) & 0xFF) / 255f;
        passFloat[1] = ((rgba >> 16) & 0xFF) / 255f;
        passFloat[2] = ((rgba >> 8) & 0xFF) / 255f;
        passFloat[3] = (rgba & 0xFF) / 255f;
        GL.ProgramSetUniformF(id, 4, 1, passFloat, 0);
    }

    public void setMatrix(String name, float[] data, int w, int h) {
        int id = GL.ProgramGetUniformId(shaderProgramId, name);
        GL.ProgramSetUniformMatrix(id, w, h, 1, true, data, 0);
    }

    public void setIntArray(String name, int[] value, int size) {
        int id = GL.ProgramGetUniformId(shaderProgramId, name);
        GL.ProgramSetUniformI(id, 1, size, value, 0);
    }

    public void setFloatArray(String name, float[] value, int size) {
        int id = GL.ProgramGetUniformId(shaderProgramId, name);
        GL.ProgramSetUniformF(id, 1, size, value, 0);
    }

    public void setFloatArray(String name, float... values) {
        int id = GL.ProgramGetUniformId(shaderProgramId, name);
        GL.ProgramSetUniformF(id, 1, values.length, values, 0);
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
            linkLog = null;
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
                linkLog = GL.ProgramGetLog(shaderProgramId);
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

    public String getLinkLog() {
        return linkLog;
    }

    public String getLog() {
        StringBuilder log = new StringBuilder();
        if (vtxLog != null) {
            log.append("Vertex Shader Log: \n").append(vtxLog).append("\n");
        }
        if (frgLog != null) {
            log.append("Fragment Shader Log : \n").append(frgLog).append("\n");
        }
        if (linkLog != null) {
            log.append("Link Log : \n").append(linkLog).append("\n");
        }
        return log.toString();
    }
}
