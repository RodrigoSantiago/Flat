package flat.graphics.context;

public abstract class Paint {

    protected abstract void setInternal(long svgId);

    protected int getTextureId(Texture2D texture) {
        return texture.getInternalId();
    }

    public abstract Paint multiply(int color);
}