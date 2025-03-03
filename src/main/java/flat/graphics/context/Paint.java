package flat.graphics.context;

public abstract class Paint {
    public enum CycleMethod {CLAMP, REPEAT, REFLECT, GAUSIAN_CLAMP}

    protected abstract void setInternal(long svgId);

    protected int getTextureId(Texture2D texture) {
        return texture.getInternalID();
    }

}