package flat.widget.selection;

import flat.events.ActionEvent;
import flat.graphics.image.Drawable;
import flat.graphics.image.DrawableReader;
import flat.resources.ResourceStream;
import flat.uxml.Controller;
import flat.uxml.UXHash;
import flat.uxml.UXListener;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueColor;
import flat.uxml.value.UXValueNumber;
import flat.uxml.value.UXValueText;
import flat.widget.Widget;
import flat.widget.enums.ImageFilter;
import flat.widget.enums.SelectionState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DrawableReader.class})
public class CheckboxTest {
    ResourceStream resActive;
    ResourceStream resInactive;
    ResourceStream resIndeterminate;

    Drawable iconActive;
    Drawable iconInactive;
    Drawable iconIndeterminate;

    @Before
    public void before() {
        mockStatic(DrawableReader.class);

        iconActive = mock(Drawable.class);
        when(iconActive.getWidth()).thenReturn(16f);
        when(iconActive.getHeight()).thenReturn(20f);
        iconInactive = mock(Drawable.class);
        when(iconInactive.getWidth()).thenReturn(18f);
        when(iconInactive.getHeight()).thenReturn(18f);
        iconIndeterminate = mock(Drawable.class);
        when(iconIndeterminate.getWidth()).thenReturn(20f);
        when(iconIndeterminate.getHeight()).thenReturn(16f);

        resActive = mock(ResourceStream.class);
        resInactive = mock(ResourceStream.class);
        resIndeterminate = mock(ResourceStream.class);

        when(DrawableReader.parse(resActive)).thenReturn(iconActive);
        when(DrawableReader.parse(resInactive)).thenReturn(iconInactive);
        when(DrawableReader.parse(resIndeterminate)).thenReturn(iconIndeterminate);
    }

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);
        UXListener<ActionEvent> action = (UXListener<ActionEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onActionWork", ActionEvent.class)).thenReturn(action);

        Checkbox checkbox = new Checkbox();
        checkbox.setAttributes(createNonDefaultValues(), "checkbox");
        checkbox.applyAttributes(controller);
        checkbox.applyStyle();

        assertEquals(SelectionState.ACTIVE, checkbox.getSelectionState());
        assertEquals(iconActive, checkbox.getIconActive());
        assertEquals(iconInactive, checkbox.getIconInactive());
        assertEquals(iconIndeterminate, checkbox.getIconIdeterminate());
        assertEquals(1.0f, checkbox.getIconTransitionDuration(), 0.0001f);
        assertEquals(0xFF0000FF, checkbox.getColor());
        assertEquals(ImageFilter.LINEAR, checkbox.getIconImageFilter());

        assertEquals(action, checkbox.getActionListener());
    }

    @Test
    public void measure() {
        Checkbox checkbox = new Checkbox();
        checkbox.setIconActive(iconActive);
        checkbox.setIconInactive(iconInactive);
        checkbox.setIconIdeterminate(iconIndeterminate);
        checkbox.setSelectionState(SelectionState.ACTIVE);
        checkbox.onMeasure();

        assertEquals(20, checkbox.getMeasureWidth(), 0.1f);
        assertEquals(20, checkbox.getMeasureHeight(), 0.1f);

        checkbox.setMargins(1, 2, 3, 4);
        checkbox.setPadding(5, 4, 2, 3);
        checkbox.onMeasure();

        assertEquals(20 + 13, checkbox.getMeasureWidth(), 0.1f);
        assertEquals(20 + 11, checkbox.getMeasureHeight(), 0.1f);

        checkbox.setPrefSize(100, 200);
        checkbox.onMeasure();

        assertEquals(106, checkbox.getMeasureWidth(), 0.1f);
        assertEquals(204, checkbox.getMeasureHeight(), 0.1f);

        checkbox.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        checkbox.onMeasure();

        assertEquals(Widget.MATCH_PARENT, checkbox.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, checkbox.getMeasureHeight(), 0.1f);
    }

    @Test
    public void fireAction() {
        Checkbox checkbox = new Checkbox();
        checkbox.setIconActive(iconActive);
        checkbox.setIconInactive(iconInactive);
        checkbox.setIconIdeterminate(iconIndeterminate);
        checkbox.setSelectionState(SelectionState.INDETERMINATE);

        UXListener<ActionEvent> action = (UXListener<ActionEvent>) mock(UXListener.class);
        checkbox.setActionListener(action);

        assertEquals(SelectionState.INDETERMINATE, checkbox.getSelectionState());
        checkbox.fire();
        assertEquals(SelectionState.ACTIVE, checkbox.getSelectionState());
        checkbox.fire();
        assertEquals(SelectionState.INACTIVE, checkbox.getSelectionState());
        checkbox.fire();
        assertEquals(SelectionState.ACTIVE, checkbox.getSelectionState());

        verify(action, times(3)).handle(any());
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        UXValue uxIconActive = mock(UXValue.class);
        when(uxIconActive.asResource(any())).thenReturn(resActive);
        UXValue uxIconInactive = mock(UXValue.class);
        when(uxIconInactive.asResource(any())).thenReturn(resInactive);
        UXValue uxIconIndeterminate = mock(UXValue.class);
        when(uxIconIndeterminate.asResource(any())).thenReturn(resIndeterminate);

        hash.put(UXHash.getHash("on-action"), new UXValueText("onActionWork"));
        hash.put(UXHash.getHash("color"), new UXValueColor(0xFF0000FF));
        hash.put(UXHash.getHash("icon-image-filter"), new UXValueText(ImageFilter.LINEAR.toString()));
        hash.put(UXHash.getHash("icon-active"), uxIconActive);
        hash.put(UXHash.getHash("icon-inactive"), uxIconInactive);
        hash.put(UXHash.getHash("icon-indeterminate"), uxIconIndeterminate);
        hash.put(UXHash.getHash("icon-transition-duration"), new UXValueNumber(1.0f));
        hash.put(UXHash.getHash("selection-state"), new UXValueText(SelectionState.ACTIVE.toString()));
        return hash;
    }
}