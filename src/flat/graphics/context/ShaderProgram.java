package flat.graphics.context;

import flat.math.Vector2;
import flat.backend.GL;
import flat.graphics.context.enuns.AttributeType;
import flat.math.*;
import flat.screen.Application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ShaderProgram extends ContextObject {

    private ArrayList<Shader> shaders = new ArrayList<>();

    private List<Attribute> attA;
    private List<Attribute> attU;
    private ArrayList<Attribute> attributes = new ArrayList<>();
    private ArrayList<Attribute> uniforms = new ArrayList<>();

    private HashMap<String, Attribute> attributesNames = new HashMap<>();
    private HashMap<String, Attribute> uniformsNames = new HashMap<>();
    private HashMap<String, Integer> blocksLocations = new HashMap<>();

    private int programId;
    private String log;
    private boolean linked;

    public ShaderProgram(Shader... shaders) {
        init();
        for (Shader shader : shaders) {
            attach(shader);
        }
    }

    @Override
    protected void onInitialize() {
        this.programId = GL.ProgramCreate();
    }

    @Override
    protected void onDispose() {
        GL.ProgramDestroy(programId);
    }

    int getInternalID() {
        return programId;
    }

    public ShaderProgram attach(Shader shader) {
        if (linked) {
            throw new RuntimeException("A Linked shaders program is immutable");
        }
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
        if (shaders.contains(shader)) {
            shaders.remove(shader);
            GL.ProgramDetachShader(programId, shader.getInternalID());
        }
        return this;
    }

    public boolean link() {
        if (!linked) {
            GL.ProgramLink(programId);
            linked = GL.ProgramIsLinked(programId);
            if (!linked) {
                log = GL.ProgramGetLog(programId);
            } else {
                log = null;
                int size = GL.ProgramGetAttributesCount(programId);
                for (int i = 0; i < size; i++) {
                    String name = GL.ProgramGetAttributeName(programId, i);
                    AttributeType type = AttributeType.fromInternalEnum(GL.ProgramGetAttributeType(programId, i));
                    int arraySize = GL.ProgramGetAttributeSize(programId, i);
                    if (name.contains("[")) {
                        name = name.substring(0, name.indexOf("["));
                    }
                    Attribute att = new Attribute(i, name, type, arraySize);
                    attributes.add(att);
                    attributesNames.put(name, att);
                }
                size = GL.ProgramGetUniformsCount(programId);
                for (int i = 0; i < size; i++) {
                    String name = GL.ProgramGetUniformName(programId, i);
                    AttributeType type = AttributeType.fromInternalEnum(GL.ProgramGetUniformType(programId, i));
                    int arraySize = GL.ProgramGetUniformSize(programId, i);
                    if (name.contains("[")) {
                        name = name.substring(0, name.indexOf("["));
                    }
                    Attribute att = new Attribute(i, name, type, arraySize);
                    uniforms.add(att);
                    uniformsNames.put(name, att);
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
        Application.getCurrentContext().bindShaderProgram(this);
    }

    public void end() {
        Application.getCurrentContext().unbindShaderProgram();
    }

    public List<Attribute> getAttributes() {
        if (attA == null) {
            attA = Collections.unmodifiableList(attributes);
        }
        return attA;
    }

    public List<Attribute> getUniforms() {
        if (attU == null) {
            attU = Collections.unmodifiableList(uniforms);
        }
        return attU;
    }

    public Attribute getAttribute(String name) {
        return attributesNames.get(name);
    }

    public Attribute getUniform(String name) {
        return uniformsNames.get(name);
    }

    public int getUniformBlockLocation(String name) {
        Integer loc = blocksLocations.get(name);
        return loc == null ? -1 : loc;
    }

    public boolean set(String name, Object value) {
        return set(getUniform(name), value);
    }

    public boolean set(int att, Object value) {
        return set(attributes.get(att), value);
    }

    private boolean set(Attribute atribute, Object value) {
        if (atribute == null) {
            return false;
        }
        int att = atribute.location;

        AttributeType type = atribute.type;

        if (type == AttributeType.INT || type == AttributeType.BOOL ||
                type == AttributeType.SAMPLER_2D || type == AttributeType.SAMPLER_CUBE) {
            if (value instanceof int[]) {
                int[] data = (int[]) value;
                setInt(att, 1, data.length, data);
            } else {
                setInt(att, 1, 1, (int)value);
            }
        } else if (type == AttributeType.INT_VEC2 || type == AttributeType.BOOL_VEC2) {
            int[] data = (int[]) value;
            setInt(att, 2, data.length / 2, data);
        } else if (type == AttributeType.INT_VEC3 || type == AttributeType.BOOL_VEC3) {
            int[] data = (int[]) value;
            setInt(att, 3, data.length / 3, data);
        } else if (type == AttributeType.INT_VEC4 || type == AttributeType.BOOL_VEC4) {
            int[] data = (int[]) value;
            setInt(att, 4, data.length / 4, data);
        } else if (type == AttributeType.FLOAT) {
            if (value instanceof float[]) {
                float[] data = (float[]) value;
                setFloat(att, 1, data.length, data);
            } else {
                setFloat(att, 1, 1, (float)value);
            }
        } else if (type == AttributeType.FLOAT_VEC2) {
            if (value instanceof Vector2[]) {
                Vector2[] data = (Vector2[]) value;
                float[] tmp = new float[data.length * 2];
                for (int i = 0; i < data.length; i++) {
                    tmp[i * 2] = data[i].x;
                    tmp[i * 2 + 1] = data[i].y;
                }
                setFloat(att, 2, data.length, tmp);
            } else if  (value instanceof Vector2) {
                Vector2 data = (Vector2) value;
                setFloat(att, 2, 1, data.x, data.y);
            } else {
                float[] data = (float[]) value;
                setFloat(att, 2, data.length / 2, data);
            }
        } else if (type == AttributeType.FLOAT_VEC3) {
            if (value instanceof Vector3[]) {
                Vector3[] data = (Vector3[]) value;
                float[] tmp = new float[data.length * 3];
                for (int i = 0; i < data.length; i++) {
                    tmp[i * 2] = data[i].x;
                    tmp[i * 2 + 1] = data[i].y;
                    tmp[i * 2 + 2] = data[i].z;
                }
                setFloat(att, 3, data.length, tmp);
            } else if  (value instanceof Vector3) {
                Vector3 data = (Vector3) value;
                setFloat(att, 3, 1, data.x, data.y, data.z);
            } else {
                float[] data = (float[]) value;
                setFloat(att, 3, data.length / 3, data);
            }
        } else if (type == AttributeType.FLOAT_VEC4) {
            float[] data = (float[]) value;
            setFloat(att, 4, data.length / 4, data);
        } else if (type == AttributeType.FLOAT_MAT2) {
            float[] data = (float[]) value;
            setMatrix(att, 2, 2, data.length / 4, false, data);
        } else if (type == AttributeType.FLOAT_MAT3) {
            if (value instanceof Matrix3[]) {
                Matrix3[] data = (Matrix3[]) value;
                float[] tmp = new float[data.length * 9];
                for (int i = 0; i < data.length; i++) {
                    tmp[i * 9] = data[i].val[0];
                    tmp[i * 9 + 1] = data[i].val[3];
                    tmp[i * 9 + 2] = data[i].val[6];
                    tmp[i * 9 + 3] = data[i].val[1];
                    tmp[i * 9 + 4] = data[i].val[4];
                    tmp[i * 9 + 5] = data[i].val[7];
                    tmp[i * 9 + 6] = data[i].val[2];
                    tmp[i * 9 + 7] = data[i].val[5];
                    tmp[i * 9 + 8] = data[i].val[8];
                }
                setMatrix(att, 3, 3, data.length, false, tmp);
            } else if  (value instanceof Matrix3) {
                Matrix3 data = (Matrix3) value;
                float[] tmp = new float[]{
                        data.val[0], data.val[3], data.val[6],
                        data.val[1], data.val[4], data.val[7],
                        data.val[2], data.val[5], data.val[8]};
                setMatrix(att, 3, 3, 1, false, tmp);
            } else {
                float[] data = (float[]) value;
                setMatrix(att, 3, 3, 1, false, data);
            }
        } else if (type == AttributeType.FLOAT_MAT4) {
            if (value instanceof Matrix4[]) {
                Matrix4[] data = (Matrix4[]) value;
                float[] tmp = new float[data.length * 16];
                for (int i = 0; i < data.length; i++) {
                    tmp[i * 9] = data[i].val[0];
                    tmp[i * 9 + 1] = data[i].val[4];
                    tmp[i * 9 + 2] = data[i].val[8];
                    tmp[i * 9 + 3] = data[i].val[12];
                    tmp[i * 9 + 4] = data[i].val[1];
                    tmp[i * 9 + 5] = data[i].val[5];
                    tmp[i * 9 + 6] = data[i].val[9];
                    tmp[i * 9 + 7] = data[i].val[13];
                    tmp[i * 9 + 8] = data[i].val[2];
                    tmp[i * 9 + 9] = data[i].val[6];
                    tmp[i * 9 + 10] = data[i].val[10];
                    tmp[i * 9 + 11] = data[i].val[13];
                    tmp[i * 9 + 12] = data[i].val[3];
                    tmp[i * 9 + 13] = data[i].val[7];
                    tmp[i * 9 + 14] = data[i].val[11];
                    tmp[i * 9 + 15] = data[i].val[15];
                }
                setMatrix(att, 4, 4, data.length, false, tmp);
            } else if  (value instanceof Matrix4) {
                Matrix4 data = (Matrix4) value;
                float[] tmp = new float[]{
                        data.val[0], data.val[4], data.val[8], data.val[12],
                        data.val[1], data.val[5], data.val[9], data.val[13],
                        data.val[2], data.val[6], data.val[10], data.val[14],
                        data.val[3], data.val[7], data.val[11], data.val[15]};
                setMatrix(att, 4, 4, 1, false, tmp);
            } else {
                float[] data = (float[]) value;
                setMatrix(att, 4, 4, 1, false, data);
            }
        } else if (type == AttributeType.FLOAT_MAT2x3) {
            if (value instanceof Affine[]) {
                Affine[] data = (Affine[]) value;
                float[] tmp = new float[data.length * 6];
                for (int i = 0; i < data.length; i++) {
                    tmp[i * 9] = data[i].m00;
                    tmp[i * 9 + 1] = data[i].m01;
                    tmp[i * 9 + 2] = data[i].m10;
                    tmp[i * 9 + 3] = data[i].m11;
                    tmp[i * 9 + 4] = data[i].m02;
                    tmp[i * 9 + 5] = data[i].m12;
                }
                setMatrix(att, 2, 3, data.length, false, tmp);
            } else if  (value instanceof Affine) {
                Affine data = (Affine) value;
                float[] tmp = new float[]{
                        data.m00, data.m01,
                        data.m10, data.m11,
                        data.m02, data.m12};
                setMatrix(att, 2, 3, 1, false, tmp);
            } else {
                float[] data = (float[]) value;
                setMatrix(att, 2, 3, 1, false, data);
            }
        } else if (type == AttributeType.FLOAT_MAT2x4) {
            float[] data = (float[]) value;
            setMatrix(att, 2, 4, data.length / 8, false, data);
        } else if (type == AttributeType.FLOAT_MAT3x2) {
            float[] data = (float[]) value;
            setMatrix(att, 3, 2, data.length / 6, false, data);
        } else if (type == AttributeType.FLOAT_MAT3x4) {
            float[] data = (float[]) value;
            setMatrix(att, 3, 4, data.length / 12, false, data);
        } else if (type == AttributeType.FLOAT_MAT4x2) {
            float[] data = (float[]) value;
            setMatrix(att, 4, 2, data.length / 8, false, data);
        } else if (type == AttributeType.FLOAT_MAT4x3) {
            float[] data = (float[]) value;
            setMatrix(att, 4, 3, data.length / 12, false, data);
        }
        return true;
    }

    public void setInt(String name, int typeSize, int arraySize, int... values) {
        setInt(getUniform(name).location, typeSize, arraySize, values);
    }

    public void setInt(int att, int typeSize, int arraySize, int... values) {
        GL.ProgramSetUniformI(att, typeSize, arraySize, values, 0);
    }

    public void setFloat(String name, int typeSize, int arraySize, float... values) {
        setFloat(getUniform(name).location, typeSize, arraySize, values);
    }

    public void setFloat(int att, int typeSize, int arraySize, float... values) {
        GL.ProgramSetUniformF(att, typeSize, arraySize, values, 0);
    }

    public void setMatrix(String name, int w, int h, int arraySize, boolean transpose, float... value) {
        setMatrix(getUniform(name).location, w, h, arraySize, transpose, value);
    }

    public void setMatrix(int att, int w, int h, int arraySize, boolean transpose, float... value) {
        GL.ProgramSetUniformMatrix(att, w, h, arraySize, transpose, value, 0);
    }

    public static class Attribute {

        public final int location;
        public final String name;
        public final AttributeType type;
        public final int arraySize;

        private Attribute(int location, String name, AttributeType type, int arraySize) {
            this.location = location;
            this.name = name;
            this.type = type;
            this.arraySize = arraySize;
        }
    }
}
