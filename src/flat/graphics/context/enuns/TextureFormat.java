package flat.graphics.context.enuns;

public enum TextureFormat {
    RGB, RGBA, DEPTH32, DEPTH16, DEPTH8, STENCIL, DEPTHS_TENCIL;

    private int internalEnum;
    private int internalTypeEnum;

    public int getInternalEnum() {
        return internalEnum;
    }

    public int getInternalTypeEnum() {
        return internalTypeEnum;
    }
}
