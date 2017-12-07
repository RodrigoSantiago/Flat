package flat.graphics.effects;

import flat.graphics.Shader;

public final class HBlur extends Effect {

    private int blur;

    public HBlur() {
        super(1);
    }

    public HBlur(int blur) {
        this();
        setBlur(blur);
    }

    public int getBlur() {
        return blur;
    }

    public void setBlur(int blur) {
        this.blur = blur;
    }

    @Override
    public void applyAttributes(Shader shader) {
        super.applyAttributes(shader);
        shader.setInt("data1", blur);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (this == obj) return true;
        if (obj instanceof HBlur) {
            HBlur other = (HBlur) obj;
            return this.blur == other.blur;
        } else {
            return false;
        }
    }
}
