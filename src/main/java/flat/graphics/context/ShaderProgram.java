package flat.graphics.context;

import flat.backend.GL;
import flat.exception.FlatException;
import flat.graphics.context.enums.AttributeType;
import flat.math.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public final class ShaderProgram extends ContextObject {

    private final ArrayList<Shader> shaders = new ArrayList<>();

    private List<Attribute> attA;
    private List<Attribute> attU;
    private final ArrayList<Attribute> attributes = new ArrayList<>();
    private final ArrayList<Attribute> uniforms = new ArrayList<>();

    private final HashMap<String, Attribute> attributesNames = new HashMap<>();
    private final HashMap<String, Attribute> uniformsNames = new HashMap<>();
    private final HashMap<String, Integer> blocksLocations = new HashMap<>();
    private final HashMap<String, AttributeValue> uniformsValues = new HashMap<>();

    private final int programId;
    private String log;
    private boolean linked;

    public ShaderProgram(Context context, Shader... shaders) {
        super(context);
        final int programId = GL.ProgramCreate();
        this.programId = programId;
        assignDispose(() -> GL.ProgramDestroy(programId));

        for (Shader shader : shaders) {
            attach(shader);
        }
    }

    @Override
    protected boolean isBound() {
        return getContext().isShaderProgramBound(this);
    }

    int getInternalID() {
        return programId;
    }

    private void checkImmutable() {
        if (linked) {
            throw new RuntimeException("A Linked shaders program is immutable");
        }
    }

    public ShaderProgram attach(Shader shader) {
        checkImmutable();

        if (!shaders.contains(shader)) {
            shaders.add(shader);
            GL.ProgramAttachShader(programId, shader.getInternalID());
        }
        return this;
    }

    public ShaderProgram detach(Shader shader) {
        checkImmutable();

        if (shaders.contains(shader)) {
            shaders.remove(shader);
            GL.ProgramDetachShader(programId, shader.getInternalID());
        }
        return this;
    }

    public boolean link() {
        checkImmutable();

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
                uniformsValues.put(name, new AttributeValue(att));
            }
            size = GL.ProgramGetUniformBlocksCount(programId);
            for (int i = 0; i < size; i++) {
                blocksLocations.put(GL.ProgramGetUniformBlockName(programId, i), i);
            }
            shaders.clear();
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
        getContext().bindShaderProgram(this);
    }

    public void end() {
        getContext().unbindShaderProgram();
    }

    void onBound() {
        for (var value : uniformsValues.values()) {
            value.checkInvalided();
        }
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

    private AttributeValue getUniformValue(String name) {
        return uniformsValues.get(name);
    }

    public boolean set(String name, int value) {
        try {
            getUniformValue(name).set(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean set(String name, int... value) {
        try {
            getUniformValue(name).set(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean set(String name, float value) {
        try {
            getUniformValue(name).set(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean set(String name, float... value) {
        try {
            getUniformValue(name).set(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean set(String name, Vector2 value) {
        try {
            getUniformValue(name).set(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean set(String name, Vector2... value) {
        try {
            getUniformValue(name).set(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean set(String name, Vector3 value) {
        try {
            getUniformValue(name).set(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean set(String name, Vector3... value) {
        try {
            getUniformValue(name).set(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean set(String name, Vector4 value) {
        try {
            getUniformValue(name).set(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean set(String name, Vector4... value) {
        try {
            getUniformValue(name).set(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean set(String name, Affine value) {
        try {
            getUniformValue(name).set(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean set(String name, Affine... value) {
        try {
            getUniformValue(name).set(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean set(String name, Matrix3 value) {
        try {
            getUniformValue(name).set(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean set(String name, Matrix3... value) {
        try {
            getUniformValue(name).set(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean set(String name, Matrix4 value) {
        try {
            getUniformValue(name).set(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean set(String name, Matrix4... value) {
        try {
            getUniformValue(name).set(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean setTranspose(String name, boolean transpose) {
        try {
            getUniformValue(name).setTranspose(transpose);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    void setInt(int att, int typeSize, int arraySize, int... values) {
        boundCheck();

        GL.ProgramSetUniformI(att, typeSize, arraySize, values, 0);
    }

    void setFloat(int att, int typeSize, int arraySize, float... values) {
        boundCheck();

        GL.ProgramSetUniformF(att, typeSize, arraySize, values, 0);
    }

    void setMatrix(int att, int w, int h, int arraySize, boolean transpose, float... value) {
        boundCheck();

        GL.ProgramSetUniformMatrix(att, w, h, arraySize, transpose, value, 0);
    }

    public class AttributeValue {
        final Attribute att;
        final int stride;
        float[] valFloat;
        int[] valInt;

        private boolean invalid;
        private boolean transpose;

        public AttributeValue(Attribute att) {
            this.att = att;
            this.stride = att.getType().getSize();//((att.getType().getSize() - 1) / 4 + 1) * 4;
            if (att.getType().isFloat()) {
                valFloat = new float[stride * att.getArraySize()];
            } else {
                valInt = new int[stride * att.getArraySize()];
            }
        }

        private void assertSize(int size, int length) {
            if (!att.getType().isFloat()) {
                throw new FlatException("Invalid type for Uniform value. Expected : Int. Provided Float");
            }
            if (att.getType().getSize() != size) {
                throw new FlatException("Invalid value size for Uniform value. Expected : "
                        + att.getType().getSize() + ". Provided : " + size);
            }
            if (att.getArraySize() != length) {
                throw new FlatException("Invalid array size for Uniform value. Expected : "
                        + att.getArraySize() + ". Provided : " + length);
            }
        }

        private void assertSizeInt(int size, int length) {
            if (att.getType().isFloat()) {
                throw new FlatException("Invalid type for Uniform value. Expected : Float. Provided Int");
            }
            if (att.getType().getSize() != size) {
                throw new FlatException("Invalid value size for Uniform value. Expected : "
                        + att.getType().getSize() + ". Provided : " + size);
            }
            if (att.getArraySize() != length) {
                throw new FlatException("Invalid array size for Uniform value. Expected : "
                        + att.getArraySize() + ". Provided : " + length);
            }
        }

        private void setShaderValue() {
            if (!att.getType().isFloat()) {
                setInt(att.getLocation(), stride, valInt.length / stride, valInt);
            } else {
                int mw = att.getType().getMatrixWidth();
                int mh = att.getType().getMatrixHeight();
                if (mw == 0 || mh == 0) {
                    setFloat(att.getLocation(), stride, valFloat.length / stride, valFloat);
                } else {
                    setMatrix(att.getLocation(), mw, mh, valFloat.length / stride, transpose, valFloat);
                }
            }
        }

        private void set(int offset, Vector2 value) {
            valFloat[offset] = value.x;
            valFloat[offset + 1] = value.y;
        }

        private void set(int offset, Vector3 value) {
            valFloat[offset] = value.x;
            valFloat[offset + 1] = value.y;
            valFloat[offset + 2] = value.z;
        }

        private void set(int offset, Vector4 value) {
            valFloat[offset] = value.x;
            valFloat[offset + 1] = value.y;
            valFloat[offset + 2] = value.z;
            valFloat[offset + 3] = value.w;
        }

        private void set(int offset, Affine value) {
            valFloat[offset] = value.m00;
            valFloat[offset + 1] = value.m01;
            valFloat[offset + 2] = value.m10;
            valFloat[offset + 3] = value.m11;
            valFloat[offset + 4] = value.m02;
            valFloat[offset + 5] = value.m12;
        }

        private void set(int offset, Matrix3 value) {
            System.arraycopy(value.val, 0, valFloat, offset, 9);
        }

        private void set(int offset, Matrix4 value) {
            System.arraycopy(value.val, 0, valFloat, offset, 16);
        }

        private void onUpdate() {
            if (isBound()) {
                setShaderValue();
                invalid = false;
            } else {
                invalid = true;
            }
        }

        public boolean isTranspose() {
            return transpose;
        }

        public void setTranspose(boolean transpose) {
            if (this.transpose != transpose) {
                this.transpose = transpose;
                onUpdate();
            }
        }

        public void checkInvalided() {
            if (invalid) {
                onUpdate();
            }
        }

        public void set(float value) {
            assertSize(1, 1);
            valFloat[0] = value;
            onUpdate();
        }

        public void set(Vector2 value) {
            assertSize(2, 1);
            set(0, value);
            onUpdate();
        }

        public void set(Vector3 value) {
            assertSize(3, 1);
            set(0, value);
            onUpdate();
        }

        public void set(Vector4 value) {
            assertSize(4, 1);
            set(0, value);
            onUpdate();
        }

        public void set(Affine value) {
            assertSize(6, 1);
            set(0, value);
            onUpdate();
        }

        public void set(Matrix3 value) {
            assertSize(9, 1);
            set(0, value);
            onUpdate();
        }

        public void set(Matrix4 value) {
            assertSize(16, 1);
            set(0, value);
            onUpdate();
        }

        public void set(float... value) {
            if (!att.getType().isFloat()) {
                throw new FlatException("Invalid type for Uniform value. Expected : Int. Provided Float");
            }
            if (value.length != att.getType().getSize() * att.getArraySize()) {
                throw new FlatException("Invalid array size for Uniform value. Expected : "
                        + (att.getType().getSize() * att.getArraySize()) + ". Provided : " + value.length);
            }

            for (int i = 0; i < att.getArraySize(); i++) {
                int offsetA = i * stride;
                int offsetB = i * att.getType().getSize();
                System.arraycopy(value, offsetB, valFloat, offsetA, att.getType().getSize());
            }
            onUpdate();
        }

        public void set(Vector2... value) {
            assertSize(2, value.length);
            for (int i = 0; i < att.getArraySize(); i++) {
                set(i * stride, value[i]);
            }
            onUpdate();
        }

        public void set(Vector3... value) {
            assertSize(3, value.length);
            for (int i = 0; i < att.getArraySize(); i++) {
                set(i * stride, value[i]);
            }
            onUpdate();
        }

        public void set(Vector4... value) {
            assertSize(4, value.length);
            for (int i = 0; i < att.getArraySize(); i++) {
                set(i * stride, value[i]);
            }
            onUpdate();
        }

        public void set(Affine... value) {
            assertSize(6, value.length);
            for (int i = 0; i < att.getArraySize(); i++) {
                set(i * stride, value[i]);
            }
            onUpdate();
        }

        public void set(Matrix3... value) {
            assertSize(9, value.length);
            for (int i = 0; i < att.getArraySize(); i++) {
                set(i * stride, value[i]);
            }
            onUpdate();
        }

        public void set(Matrix4... value) {
            assertSize(16, value.length);
            for (int i = 0; i < att.getArraySize(); i++) {
                set(i * stride, value[i]);
            }
            onUpdate();
        }

        public void set(int value) {
            assertSizeInt(1, 1);
            valInt[0] = value;
            onUpdate();
        }

        public void set(int... value) {
            if (att.getType().isFloat()) {
                throw new FlatException("Invalid type for Uniform value. Expected : Float. Provided Int");
            }
            if (value.length != att.getType().getSize() * att.getArraySize()) {
                throw new FlatException("Invalid array size for Uniform value. Expected : "
                        + (att.getType().getSize() * att.getArraySize()) + ". Provided : " + value.length);
            }

            for (int i = 0; i < att.getArraySize(); i++) {
                int offsetA = i * stride;
                int offsetB = i * att.getType().getSize();
                System.arraycopy(value, offsetB, valInt, offsetA, att.getType().getSize());
            }
            onUpdate();
        }
    }

}
