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
import flat.window.ActivitySupport;
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
        sceneA = new Scene();
        ActivitySupport.setActivity(sceneA, activityA);
        when(activityA.getScene()).thenReturn(sceneA);
        when(activityA.getWidth()).thenReturn(800f);
        when(activityA.getHeight()).thenReturn(600f);

        activityB = mock(Activity.class);
        sceneB = new Scene();
        ActivitySupport.setActivity(sceneB, activityB);
        when(activityB.getScene()).thenReturn(sceneB);
        when(activityB.getWidth()).thenReturn(800f);
        when(activityB.getHeight()).thenReturn(600f);
    }

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);

        Dialog dialog = new Dialog();

        assertEquals(HorizontalAlign.CENTER, dialog.getHorizontalAlign());
        assertEquals(VerticalAlign.MIDDLE, dialog.getVerticalAlign());
        assertEquals(0, dialog.getShowTransitionDuration(), 0.001f);
        assertEquals(0, dialog.getHideTransitionDuration(), 0.001f);

        dialog.setAttributes(createNonDefaultValues(), null);
        dialog.applyAttributes(controller);

        assertEquals(HorizontalAlign.CENTER, dialog.getHorizontalAlign());
        assertEquals(VerticalAlign.MIDDLE, dialog.getVerticalAlign());
        assertEquals(0, dialog.getShowTransitionDuration(), 0.001f);
        assertEquals(0, dialog.getHideTransitionDuration(), 0.001f);

        dialog.applyStyle();

        assertEquals(HorizontalAlign.RIGHT, dialog.getHorizontalAlign());
        assertEquals(VerticalAlign.TOP, dialog.getVerticalAlign());
        assertEquals(0.25f, dialog.getShowTransitionDuration(), 0.001f);
        assertEquals(0.15f, dialog.getHideTransitionDuration(), 0.001f);
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
        Activity activity = mock(Activity.class);
        Scene scene = new Scene();
        ActivitySupport.setActivity(scene, activity);
        when(activity.getScene()).thenReturn(scene);

        Dialog dialog = new Dialog();
        dialog.show(activity);

        assertTrue(dialog.isShown());
        verify(activity, times(1)).addPointerFilter(dialog);

        dialog.hide();
        assertFalse(dialog.isShown());
    }

    @Test
    public void removeParentManually() {
        Dialog dialog = new Dialog();
        dialog.show(activityA);

        assertTrue(dialog.isShown());
        assertEquals(sceneA, dialog.getParent());
        verify(activityA, times(1)).addPointerFilter(dialog);

        dialog.getParent().remove(dialog);

        assertEquals(sceneA, dialog.getParent());
        assertTrue(dialog.isShown());
    }

    @Test
    public void moveParentManually() {
        Panel child = new Panel();
        sceneA.add(child);

        Dialog dialog = new Dialog();
        dialog.show(activityA);

        assertTrue(dialog.isShown());
        assertEquals(sceneA, dialog.getParent());
        verify(activityA, times(1)).addPointerFilter(dialog);

        child.add(dialog);

        assertEquals(sceneA, dialog.getParent());
        assertTrue(dialog.isShown());
    }

    @Test
    public void closeOnShow() {
        Dialog dialog = new Dialog();
        dialog.setController(new Controller() {
            @Override
            public void onShow() {
                dialog.hide();
            }
        });
        dialog.show(activityA);

        assertFalse(dialog.isShown());
        assertNull(dialog.getParent());
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
        assertEquals(sceneA, dialog.getParent());
        verify(activityA, times(1)).addPointerFilter(dialog);

        sceneA.onMeasure();
        sceneA.onLayout(800, 600);

        assertEquals(150, dialog.getLayoutX(), 0.1f);
        assertEquals(225, dialog.getLayoutY(), 0.1f);
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        hash.put(UXHash.getHash("horizontal-align"), new UXValueText(HorizontalAlign.RIGHT.toString()));
        hash.put(UXHash.getHash("vertical-align"), new UXValueText(VerticalAlign.TOP.toString()));
        hash.put(UXHash.getHash("show-transition-duration"), new UXValueNumber(0.25f));
        hash.put(UXHash.getHash("hide-transition-duration"), new UXValueNumber(0.15f));
        return hash;
    }
}