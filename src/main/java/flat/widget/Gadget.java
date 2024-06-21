package flat.widget;

import flat.uxml.*;

import java.util.HashMap;

public interface Gadget {

    void setAttributes(HashMap<Integer, UXValue> attributes, String style);

    void applyAttributes(UXTheme theme, Controller controller, UXBuilder builder);

    void applyChildren(UXChildren children);

    String getId();

    Widget getWidget();
}
