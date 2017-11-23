package flat.context;

import flat.math.*;

public abstract class Shader {
    protected String code;
    protected boolean compiled;

    public String compile() {
        return "Erro [LOG]";
    }

    public boolean isCompiled() {
        return compiled;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        if (!code.equals(this.code)) {
            compiled = false;
        }
        this.code = code;
    }

    public boolean getBool(String name) {
        return false;
    }

    public byte getByte(String name) {
        return 0;
    }

    public short getShort(String name) {
        return 0;
    }

    public int getInt(String name) {
        return 0;
    }

    public long getLong(String name) {
        return 0;
    }

    public float getFloat(String name) {
        return 0;
    }

    public double getDouble(String name) {
        return 0;
    }

    public Quaternion getQuaternion(String name) {
        return null;
    }

    public Vector2 getVector2(String name) {
        return null;
    }

    public Vector3 getVector3(String name) {
        return null;
    }

    public Matrix3 getMatrix3(String name) {
        return null;
    }

    public Matrix4 getMatrix4(String name) {
        return null;
    }

    public <T> T[] getArray(String name, T[] values) {
        return values;
    }

    public <T> T get(String name) {
        return null;
    }

    public boolean set(String name, boolean... val) {
        return false;
    }

    public boolean set(String name, byte... val) {
        return false;
    }

    public boolean set(String name, short... val) {
        return false;
    }

    public boolean set(String name, int... val) {
        return false;
    }

    public boolean set(String name, long... val) {
        return false;
    }

    public boolean set(String name, float... val) {
        return false;
    }

    public boolean set(String name, double... val) {
        return false;
    }

    public boolean set(String name, Quaternion... quaternions) {
        return false;
    }

    public boolean set(String name, Vector2... vectors) {
        return false;
    }

    public boolean set(String name, Vector3... vectors) {
        return false;
    }

    public boolean set(String name, Matrix3... matrices) {
        return false;
    }

    public boolean set(String name, Matrix4... matrices) {
        return false;
    }
}
