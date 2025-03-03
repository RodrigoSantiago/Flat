package flat.widget.layout;

import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueText;
import flat.widget.Scene;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;
import flat.window.Activity;
import flat.window.ActivitySupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UXNode.class})
public class FrameTest {

    @Test
    public void childrenFromUx() {
        Frame frame = new Frame();
        Widget child1 = new Widget();
        Widget child2 = new Widget();

        UXChildren uxChild = mockChildren(child1, child2);

        assertNull(child1.getParent());
        assertNull(child2.getParent());
        frame.applyChildren(uxChild);
        assertEquals(child1, frame.getChildrenIterable().get(0));
        assertEquals(child2, frame.getChildrenIterable().get(1));
        assertEquals(frame, child1.getParent());
        assertEquals(frame, child2.getParent());
    }

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

    @Test
    public void showFramehideFrame() {
        Activity activity = mock(Activity.class);
        Scene scene = new Scene();
        ActivitySupport.setActivity(scene, activity);
        when(activity.getScene()).thenReturn(scene);

        Frame frameA = new Frame();
        Frame frameB = new Frame();

        Controller controllerA = new Controller() {
            @Override
            public void onShow() {
                frameB.getParent().remove(frameB);
            }
        };
        Controller controllerB = mock(Controller.class);
        frameA.build(new Panel(), controllerA);
        frameB.build(new Panel(), controllerB);

        Panel parent = new Panel();
        parent.add(frameA, frameB);

        scene.add(parent);
        assertEquals(activity, frameA.getActivity());
        assertEquals(parent, frameA.getParent());
        assertNull(frameB.getActivity());
        assertNull(frameB.getParent());

        verify(controllerB, times(0)).onShow();
        verify(controllerB, times(0)).onHide();
    }

    @Test
    public void showFramehideFrameAfter() {
        Activity activity = mock(Activity.class);
        Scene scene = new Scene();
        ActivitySupport.setActivity(scene, activity);
        when(activity.getScene()).thenReturn(scene);

        Frame frameA = new Frame();
        Frame frameB = new Frame();

        Controller controllerA = new Controller() {
            @Override
            public void onShow() {
                frameB.getParent().remove(frameB);
            }
        };
        Controller controllerB = mock(Controller.class);
        frameA.build(new Panel(), controllerA);
        frameB.build(new Panel(), controllerB);

        Panel parent = new Panel();
        parent.add(frameB, frameA);

        scene.add(parent);
        assertEquals(activity, frameA.getActivity());
        assertEquals(parent, frameA.getParent());
        assertNull(frameB.getActivity());
        assertNull(frameB.getParent());

        verify(controllerB, times(1)).onShow();
        verify(controllerB, times(1)).onHide();
    }

    private UXChildren mockChildren(Widget... widgets) {
        UXChildren uxChild = mock(UXChildren.class);
        ArrayList<UXChild> children = new ArrayList<>();
        for (var widget : widgets) {
            children.add(new UXChild(widget, null));
        }
        when(uxChild.iterator()).thenReturn(children.iterator());
        return uxChild;
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        hash.put(UXHash.getHash("horizontal-align"), new UXValueText(HorizontalAlign.RIGHT.toString()));
        hash.put(UXHash.getHash("vertical-align"), new UXValueText(VerticalAlign.BOTTOM.toString()));
        return hash;
    }
}