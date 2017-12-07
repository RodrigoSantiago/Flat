package flat.graphics.effects;

import flat.Internal;
import flat.graphics.Shader;

public abstract class Effect {

    private final int id;

    Effect(int id) {
        this.id = id;
    }

    @Internal
    public void applyAttributes(Shader shader) {
        shader.setInt("effectType", id);
    }
}
