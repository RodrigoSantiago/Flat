package flat.widget.selection;

import flat.events.ActionEvent;
import flat.graphics.context.Context;
import flat.graphics.image.Drawable;
import flat.graphics.image.DrawableReader;
import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueColor;
import flat.uxml.value.UXValueNumber;
import flat.uxml.value.UXValueText;
import flat.widget.Widget;
import flat.widget.enums.ImageFilter;
import flat.widget.enums.SelectionState;
import flat.window.Activity;
import flat.window.Window;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DrawableReader.class})
public class CheckBoxTest {

    Window window;
    Context context;
    Activity activity;
    UXTheme theme;

    ResourceStream resActive;
    ResourceStream resInactive;
    ResourceStream resIndeterminate;

    Drawable iconActive;
    Drawable iconInactive;
    Drawable iconIndeterminate;

    @Before
    public void before() {
        window = mock(Window.class);
        context = mock(Context.class);

        when(context.getWindow()).thenReturn(window);
        when(context.getWidth()).thenReturn(200);
        when(context.getHeight()).thenReturn(100);

        activity = mock(Activity.class);
        when(activity.getContext()).thenReturn(context);
        when(activity.getWindow()).thenReturn(window);
        when(activity.getWidth()).thenReturn(200f);
        when(activity.getHeight()).thenReturn(100f);

        theme = mock(UXTheme.class);

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

        CheckBox checkBox = new CheckBox();
        checkBox.setAttributes(createNonDefaultValues(), "checkBox");
        checkBox.applyAttributes(controller);
        checkBox.applyStyle();

        assertEquals(SelectionState.ACTIVE, checkBox.getSelectionState());
        assertEquals(iconActive, checkBox.getIconActive());
        assertEquals(iconInactive, checkBox.getIconInactive());
        assertEquals(iconIndeterminate, checkBox.getIconIdeterminate());
        assertEquals(1.0f, checkBox.getIconTransitionDuration(), 0.0001f);
        assertEquals(0xFF0000FF, checkBox.getColor());
        assertEquals(ImageFilter.LINEAR, checkBox.getIconImageFilter());

        assertEquals(action, checkBox.getActionListener());
    }

    @Test
    public void measure() {
        CheckBox checkBox = new CheckBox();
        checkBox.setIconActive(iconActive);
        checkBox.setIconInactive(iconInactive);
        checkBox.setIconIdeterminate(iconIndeterminate);
        checkBox.setSelectionState(SelectionState.ACTIVE);
        checkBox.onMeasure();

        assertEquals(20, checkBox.getMeasureWidth(), 0.1f);
        assertEquals(20, checkBox.getMeasureHeight(), 0.1f);

        checkBox.setMargins(1, 2, 3, 4);
        checkBox.setPadding(5, 4, 2, 3);
        checkBox.onMeasure();

        assertEquals(20 + 13, checkBox.getMeasureWidth(), 0.1f);
        assertEquals(20 + 11, checkBox.getMeasureHeight(), 0.1f);

        checkBox.setPrefSize(100, 200);
        checkBox.onMeasure();

        assertEquals(106, checkBox.getMeasureWidth(), 0.1f);
        assertEquals(204, checkBox.getMeasureHeight(), 0.1f);

        checkBox.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        checkBox.onMeasure();

        assertEquals(Widget.MATCH_PARENT, checkBox.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, checkBox.getMeasureHeight(), 0.1f);
    }

    @Test
    public void fireAction() {
        CheckBox checkBox = new CheckBox();
        checkBox.setIconActive(iconActive);
        checkBox.setIconInactive(iconInactive);
        checkBox.setIconIdeterminate(iconIndeterminate);
        checkBox.setSelectionState(SelectionState.INDETERMINATE);

        assertEquals(SelectionState.INDETERMINATE, checkBox.getSelectionState());
        checkBox.fire();
        assertEquals(SelectionState.ACTIVE, checkBox.getSelectionState());
        checkBox.fire();
        assertEquals(SelectionState.INACTIVE, checkBox.getSelectionState());
        checkBox.fire();
        assertEquals(SelectionState.ACTIVE, checkBox.getSelectionState());
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