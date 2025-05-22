package flat.widget.value;

import flat.uxml.*;
import flat.uxml.value.*;
import flat.widget.Scene;
import flat.widget.layout.Panel;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.powermock.api.mockito.PowerMockito.mock;

public class HorizontalSplitterTest {

    Controller controller;
    UXBuilder builder;

    @Before
    public void before() {
        controller = mock(Controller.class);
        builder = mock(UXBuilder.class);
    }

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);
        Scene scene = new Scene();
        Panel target = new Panel();
        target.setId("targetId");
        scene.add(target);

        HorizontalSplitter splitter = new HorizontalSplitter();
        scene.add(splitter);

        assertNull(splitter.getTarget());

        splitter.setAttributes(createNonDefaultValues(), null);
        splitter.applyAttributes(controller);

        assertEquals(target, splitter.getTarget());

        splitter.applyStyle();

        assertEquals(target, splitter.getTarget());
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        hash.put(UXHash.getHash("target-id"), new UXValueText("targetId"));
        return hash;
    }
}