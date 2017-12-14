package flat.graphics.context.enuns;

public enum BufferType {
    Array, Element, PixelPack, PixelUnpack, Uniform, TransformFeedback, CopyRead, CopyWrite;

    public int getInternalIndex() {
        return 0;
    }

    public int getInternalEnum() {
        return 0;
    }
}
