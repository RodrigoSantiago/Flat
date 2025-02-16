package flat.widget.stages;

import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueNumber;
import flat.uxml.value.UXValueText;
import flat.widget.Scene;
import flat.widget.Widget;
import flat.widget.enums.HorizontalAlign;
import flat.widget.enums.VerticalAlign;
import flat.widget.layout.Panel;
import flat.window.Activity;
import org.junit.Before;
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
public class DialogTest {

    Activity activityA;
    Scene sceneA;
    Activity activityB;
    Scene sceneB;

    @Before
    public void before() {
        activityA = mock(Activity.class);
        sceneA = mock(Scene.class);
        when(activityA.getScene()).thenReturn(sceneA);
        when(sceneA.getActivity()).thenReturn(activityA);

        activityB = mock(Activity.class);
        sceneB = mock(Scene.class);
        when(activityB.getScene()).thenReturn(sceneB);
        when(sceneB.getActivity()).thenReturn(activityB);
    }

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);

        Dialog dialog = new Dialog();

        assertEquals(HorizontalAlign.CENTER, dialog.getHorizontalAlign());
        assertEquals(VerticalAlign.MIDDLE, dialog.getVerticalAlign());
        assertEquals(0, dialog.getShowupTransitionDuration(), 0.001f);

        dialog.setAttributes(createNonDefaultValues(), "dialog");
        dialog.applyAttributes(controller);

        assertEquals(HorizontalAlign.CENTER, dialog.getHorizontalAlign());
        assertEquals(VerticalAlign.MIDDLE, dialog.getVerticalAlign());
        assertEquals(0, dialog.getShowupTransitionDuration(), 0.001f);

        dialog.applyStyle();

        assertEquals(HorizontalAlign.RIGHT, dialog.getHorizontalAlign());
        assertEquals(VerticalAlign.TOP, dialog.getVerticalAlign());
        assertEquals(0.25f, dialog.getShowupTransitionDuration(), 0.001f);
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

        Dialog dialog = new Dialog();
        dialog.setTheme(theme);
        dialog.build(stream, controller);

        assertEquals(1, dialog.getChildrenIterable().size());
        assertEquals(child, dialog.getChildrenIterable().get(0));
    }

    @Test
    public void showHide() {
        Dialog dialog = new Dialog();
        dialog.show(activityA);

        assertTrue(dialog.isShown());
        verify(activityA, times(1)).addPointerFilter(dialog);

        dialog.hide();
        assertFalse(dialog.isShown());
    }

    @Test
    public void showChangeActivity() {
        Dialog dialog = new Dialog();
        dialog.show(activityA);

        assertTrue(dialog.isShown());
        verify(activityA, times(1)).addPointerFilter(dialog);

        dialog.onActivityChange(activityA, activityB);
        assertFalse(dialog.isShown());
    }

    @Test
    public void showChangeGroup() {
        Dialog dialog = new Dialog();
        dialog.show(activityA);

        assertTrue(dialog.isShown());
        verify(activityA, times(1)).addPointerFilter(dialog);

        dialog.onGroupChange(sceneA, sceneB);
        assertFalse(dialog.isShown());
    }

    @Test
    public void measure() {
        Dialog dialog = new Dialog();
        Panel content = new Panel();
        content.setPrefSize(500, 350);
        dialog.build(content, null);
        dialog.onMeasure();

        assertEquals(500, dialog.getMeasureWidth(), 0.1f);
        assertEquals(350, dialog.getMeasureHeight(), 0.1f);

        dialog.setMargins(1, 2, 3, 4);
        dialog.setPadding(5, 4, 2, 3);
        dialog.onMeasure();

        assertEquals(500 + 13, dialog.getMeasureWidth(), 0.1f);
        assertEquals(350 + 11, dialog.getMeasureHeight(), 0.1f);

        dialog.setPrefSize(100, 200);
        dialog.onMeasure();

        assertEquals(100 + 6, dialog.getMeasureWidth(), 0.1f);
        assertEquals(200 + 4, dialog.getMeasureHeight(), 0.1f);

        dialog.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        dialog.onMeasure();

        assertEquals(Widget.MATCH_PARENT, dialog.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, dialog.getMeasureHeight(), 0.1f);
    }

    @Test
    public void layoutPosition() {
        Dialog dialog = new Dialog();
        dialog.setPrefSize(100, 150);
        dialog.show(activityA, 200, 300);

        assertTrue(dialog.isShown());
        verify(activityA, times(1)).addPointerFilter(dialog);

        dialog.onMeasure();
        dialog.onLayout(100, 150);

        assertEquals(200, dialog.getX(), 0.1f);
        assertEquals(300, dialog.getY(), 0.1f);
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        hash.put(UXHash.getHash("horizontal-align"), new UXValueText(HorizontalAlign.RIGHT.toString()));
        hash.put(UXHash.getHash("vertical-align"), new UXValueText(VerticalAlign.TOP.toString()));
        hash.put(UXHash.getHash("showup-transition-duration"), new UXValueNumber(0.25f));
        return hash;
    }
}