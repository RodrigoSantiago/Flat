package flat.window;

import flat.graphics.context.Context;

public interface ActivityFactory {
    Activity build(Context context);
}
