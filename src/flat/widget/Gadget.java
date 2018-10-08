package flat.widget;

import flat.uxml.Controller;
import flat.uxml.UXChildren;
import flat.uxml.UXStyleAttrs;

public interface Gadget {

    void applyAttributes(UXStyleAttrs style, Controller controller);

    void applyChildren(UXChildren children);

    String getId();

    Widget getWidget();
}
