package flat.widget.layout;

import flat.events.ActionEvent;
import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueBool;
import flat.uxml.value.UXValueNumber;
import flat.uxml.value.UXValueText;
import flat.widget.Scene;
import flat.widget.enums.*;
import flat.widget.stages.Dialog;
import flat.window.Activity;
import flat.window.ActivitySupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UXNode.class})
public class FrameTest {

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);
        Frame frame = new Frame();

        assertEquals(HorizontalAlign.CENTER, frame.getHorizontalAlign());
        assertEquals(VerticalAlign.MIDDLE, frame.getVerticalAlign());

        frame.setAttributes(createNonDefaultValues(), "frame");
        frame.applyAttributes(controller);

        assertEquals(HorizontalAlign.CENTER, frame.getHorizontalAlign());
        assertEquals(VerticalAlign.MIDDLE, frame.getVerticalAlign());

        frame.applyStyle();

        assertEquals(HorizontalAlign.RIGHT, frame.getHorizontalAlign());
        assertEquals(VerticalAlign.BOTTOM, frame.getVerticalAlign());
    }

    @Test
    public void build() {
        Panel child = new Panel();

        Controller controller = mock(Controller.class);
        ResourceStream stream = mock(ResourceStream.class);
        UXNode node = mock(UXNode.class);
        UXBuilder builder = mock(UXBuilder.class);
        UXTheme theme = mock(UXTheme.class);

        mockStatic(UXNode.class);
        when(UXNode.parse(stream)).thenReturn(node);
        when(node.instance(controller)).thenReturn(builder);
        when(builder.build(theme)).thenReturn(child);

        Frame frame = new Frame();
        frame.setTheme(theme);
        frame.build(stream, controller);

        assertEquals(1, frame.getChildrenIterable().size());
        assertEquals(child, frame.getChildrenIterable().get(0));
    }

    @Test
    public void showHide() {
        Activity activity = mock(Activity.class);
        Scene scene = new Scene();
        ActivitySupport.setActivity(scene, activity);
        when(activity.getScene()).thenReturn(scene);

        Panel child = new Panel();
        Controller controller = mock(Controller.class);
        ResourceStream stream = mock(ResourceStream.class);
        UXNode node = mock(UXNode.class);
        UXBuilder builder = mock(UXBuilder.class);
        UXTheme theme = mock(UXTheme.class);

        mockStatic(UXNode.class);
        when(UXNode.parse(stream)).thenReturn(node);
        when(node.instance(controller)).thenReturn(builder);
        when(builder.build(theme)).thenReturn(child);

        Frame frame = new Frame();
        frame.setTheme(theme);
        frame.build(stream, controller);
        scene.add(frame);

        verify(controller, times(1)).onShow();
        verify(controller, times(0)).onHide();

        scene.remove(frame);
        verify(controller, times(1)).onShow();
        verify(controller, times(1)).onHide();
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        hash.put(UXHash.getHash("horizontal-align"), new UXValueText(HorizontalAlign.RIGHT.toString()));
        hash.put(UXHash.getHash("vertical-align"), new UXValueText(VerticalAlign.BOTTOM.toString()));
        return hash;
    }
}