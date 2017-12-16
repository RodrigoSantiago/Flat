package flat.graphics.context;

import flat.Internal;
import flat.backend.GL;
import flat.math.Affine;
import flat.math.Matrix3;
import flat.math.Matrix4;

import java.util.ArrayList;
import java.util.HashMap;

public class ShaderProgram extends ContextObject {

    private ArrayList<Shader> shaders = new ArrayList<>();
    private HashMap<String, Integer> attLocations = new HashMap<>();
    private HashMap<String, Integer> uniLocations = new HashMap<>();
    private HashMap<String, Integer> blocksLocations = new HashMap<>();

    private int programId;
    private String log;
    private boolean linked;
    private int[] parserI = new int[4];
    private float[] parserF = new float[4];

    public ShaderProgram() {
        super();
    }

    public ShaderProgram(Shader... shaders) {
        super();
        for (Shader shader : shaders) {
            attach(shader);
        }
    }

    @Override
    protected void onInitialize() {
        Context.getContext();

        final int programId = GL.ProgramCreate();

        setDispose(() -> GL.ProgramDestroy(programId));

        this.programId = programId;
    }

    int getInternalID() {
        init();
        return programId;
    }

    public ShaderProgram attach(Shader shader) {
        if (linked) {
            throw new RuntimeException("A Linked shaders program is immutable");
        }
        init();
        if (!shaders.contains(shader)) {
            shaders.add(shader);
            GL.ProgramAttachShader(programId, shader.getInternalID());
        }
        return this;
    }

    public ShaderProgram detach(Shader shader) {
        if (linked) {
            throw new RuntimeException("A Linked shaders program is immutable");
        }
        init();
        if (shaders.contains(shader)) {
            shaders.remove(shader);
            GL.ProgramDetachShader(programId, shader.getInternalID());
        }
        return this;
    }

    public boolean link() {
        if (!linked) {
            init();
            GL.ProgramLink(programId);
            linked = GL.ProgramIsLinked(programId);
            if (!linked) {
                log = GL.ProgramGetLog(programId);
            } else {
                log = null;
                int size = GL.ProgramGetAttributesCount(programId);
                for (int i = 0; i < size; i++) {
                    attLocations.put(GL.ProgramGetAttributeName(programId, i), i);
                }
                size = GL.ProgramGetUniformsCount(programId);
                for (int i = 0; i < size; i++) {
                    uniLocations.put(GL.ProgramGetUniformName(programId, i), i);
                }
                size = GL.ProgramGetUniformBlocksCount(programId);
                for (int i = 0; i < size; i++) {
                    blocksLocations.put(GL.ProgramGetUniformBlockName(programId, i), i);
                }
            }
        }
        return linked;
    }

    public boolean isLinked() {
        return linked;
    }

    public String getLog() {
        return log;
    }

    public void begin() {
        Context.getContext().bindShaderProgram(this);
    }

    public void end() {
        Context.getContext().bindShaderProgram(null);
    }

    public int getAttributeLocation(String name) {
        Integer loc = attLocations.get(name);
        return loc == null ? -1 : loc;
    }

    public int getUniformLocation(String name) {
        Integer loc = uniLocations.get(name);
        return loc == null ? -1 : loc;
    }

    public int getUniformBlockLocation(String name) {
        Integer loc = blocksLocations.get(name);
        return loc == null ? -1 : loc;
    }

    public void setInt(int att, int value) {
        parserI[0] = value;
        GL.ProgramSetUniformI(att, 1, 1, parserI, 1);
    }

    public void setInt(String att, int value) {
        setInt(getUniformLocation(att), value);
    }

    public void setInt(int att, int count, int... value) {
        GL.ProgramSetUniformI(att, 1, count, value, count);
    }

    public void setInt(String att, int count, int... value) {
        setInt(getUniformLocation(att), count, value);
    }

    public void setFloat(int att, float value) {
        parserF[0] = value;
        GL.ProgramSetUniformF(att, 1, 1, parserF, 1);
    }

    public void setFloat(String att, float value) {
        setFloat(getUniformLocation(att), value);
    }

    public void setFloat(int att, int count, float... value) {
        GL.ProgramSetUniformF(att, 1, count, value, count);
    }

    public void setFloat(String att, int count, float... value) {
        setFloat(getUniformLocation(att), count, value);
    }

    public void setVec2(int att, float x, float y) {
        parserF[0] = x;
        parserF[1] = y;
        GL.ProgramSetUniformF(att, 2, 1, parserF, 2);
    }

    public void setVec2(String att, float x, float y) {
        setVec2(getUniformLocation(att), x, y);
    }

    public void setVec2(int att, int count, float... value) {
        GL.ProgramSetUniformF(att, 2, count, value, count * 2);
    }

    public void setVec2(String att, int count, float... value) {
        setVec2(getUniformLocation(att), count, value);
    }

    public void setVec3(int att, float x, float y, float z) {
        parserF[0] = x;
        parserF[1] = y;
        parserF[2] = z;
        GL.ProgramSetUniformF(att, 3, 1, parserF, 3);
    }

    public void setVec3(String att, float x, float y, float z) {
        setVec3(getUniformLocation(att), x, y, z);
    }

    public void setVec3(int att, int count, float... value) {
        GL.ProgramSetUniformF(att, 3, count, value, count * 3);
    }

    public void setVec3(String att, int count, float... value) {
        setVec3(getUniformLocation(att), count, value);
    }

    public void setVec4(int att, float x, float y, float z, float w) {
        parserF[0] = x;
        parserF[1] = y;
        parserF[2] = z;
        parserF[3] = w;
        GL.ProgramSetUniformF(att, 4, 1, parserF, 4);
    }

    public void setVec4(String att, float x, float y, float z, float w) {
        setVec4(getUniformLocation(att), x, y, z, w);
    }

    public void setVec4(int att, int count, float... value) {
        GL.ProgramSetUniformF(att, 4, count, value, count * 4);
    }

    public void setVec4(String att, int count, float... value) {
        setVec4(getUniformLocation(att), count, value);
    }

    public void setMatrix(int att, int count, int w, int h, boolean transpose, float... value) {
        GL.ProgramSetUniformMatrix(att, w, h, count, transpose, value, 0);
    }

    public void setMatrix(String att, int count, int w, int h, boolean transpose, float... value) {
        setMatrix(getUniformLocation(att), count, w, h, transpose, value);
    }

    public void setMatrix(int att, boolean transpose, Matrix4 matrix) {
        setMatrix(att, 1, 4, 4, transpose, matrix.val);
    }

    public void setMatrix(String att, boolean transpose, Matrix4 matrix) {
        setMatrix(getUniformLocation(att), 1, 4, 4, transpose, matrix.val);
    }

    public void setMatrix(int att, boolean transpose, Matrix3 matrix) {
        setMatrix(att, 1, 3, 3, transpose, matrix.val);
    }

    public void setMatrix(String att, boolean transpose, Matrix3 matrix) {
        setMatrix(getUniformLocation(att), 1, 3, 3, transpose, matrix.val);
    }

    public void setMatrix(int att, boolean transpose, Affine matrix) {
        setMatrix(att, 1, 3, 2, transpose, matrix.val);
    }

    public void setMatrix(String att, boolean transpose, Affine matrix) {
        setMatrix(getUniformLocation(att), 1, 3, 2, transpose, matrix.val);
    }
}
