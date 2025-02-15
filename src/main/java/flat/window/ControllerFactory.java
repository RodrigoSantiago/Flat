package flat.window;

import flat.uxml.Controller;

public interface ControllerFactory {
    Controller build(Activity activity);
}
