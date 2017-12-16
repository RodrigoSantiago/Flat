package flat.graphics;

import flat.graphics.context.Context;

public class SmartContext {

    private final Context context;
    public SmartContext(Context context) {
        this.context = context;
    }

    // SVG DRAWS <svg config, svg paint>

    // IMAGE DRAWS <effect, image batch>

    // MODEL DRAWS <material, model batch, model sorter>
}
