package flat.widget;

import flat.uxml.Controller;
import flat.uxml.UXBuilder;
import flat.uxml.UXHash;
import flat.uxml.UXTheme;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mock;

public class WidgetTest {

    @Test
    public void constructor() {
        Widget widget = new Widget();
    }

    @Test
    public void id() {
        UXTheme theme = mock(UXTheme.class);
        Controller controller = mock(Controller.class);
        UXBuilder builder = mock(UXBuilder.class);

        Widget widget = new Widget();
        //widget.setAttributes();
        widget.applyAttributes(theme, controller, builder);
    }
}