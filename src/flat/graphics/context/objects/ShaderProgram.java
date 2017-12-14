package flat.graphics.context.objects;

import flat.graphics.context.ContextObject;
import flat.math.Affine;
import flat.math.Matrix3;
import flat.math.Matrix4;

public class ShaderProgram extends ContextObject {

    private int internalID;

    public ShaderProgram(Shader... shaders) {

    }

    public ShaderProgram attach(Shader shader) {
        return this;
    }

    public ShaderProgram detach(Shader shader) {
        return this;
    }

    public boolean link() {
        return false;
    }

    public String getLog() {
        return "";
    }

    public void setInt(int att, int value) {

    }

    public void setInt(String att, int value) {

    }

    public void setInt(int att, int... value) {

    }

    public void setInt(String att, int... value) {

    }

    public void setFloat(int att, float value) {

    }

    public void setFloat(String att, float value) {

    }

    public void setFloat(int att, float... value) {

    }

    public void setFloat(String att, float... value) {

    }

    public void setVec2(int att, float x, float y) {

    }

    public void setVec2(String att, float x, float y) {

    }

    public void setVec2(int att, int count, float... value) {

    }

    public void setVec2(String att, int count, float... value) {

    }

    public void setVec3(int att, float x, float y, float z) {

    }

    public void setVec3(String att, float x, float y, float z) {

    }

    public void setVec3(int att, int count, float... value) {

    }

    public void setVec3(String att, int count, float... value) {

    }

    public void setVec4(int att, float x, float y, float z, float w) {

    }

    public void setVec4(String att, float x, float y, float z, float w) {

    }

    public void setVec4(int att, int count, float... value) {

    }

    public void setVec4(String att, int count, float... value) {

    }

    public void setMatrix(int att, int count, int w, int h, boolean transpose, float... value) {

    }

    public void setMatrix(String att, int count, int w, int h, boolean transpose, float... value) {

    }

    public void setMatrix(int att, boolean transpose, Matrix4 matrix) {

    }

    public void setMatrix(String att, boolean transpose, Matrix4 matrix) {

    }

    public void setMatrix(int att, boolean transpose, Matrix3 matrix) {

    }

    public void setMatrix(String att, boolean transpose, Matrix3 matrix) {

    }

    public void setMatrix(int att, boolean transpose, Affine matrix) {

    }

    public void setMatrix(String att, boolean transpose, Affine matrix) {

    }

    @Override
    protected void onDispose() {

    }

    public int getInternalID() {
        return internalID;
    }
}
