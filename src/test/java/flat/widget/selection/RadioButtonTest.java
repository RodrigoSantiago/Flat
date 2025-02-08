package flat.widget.selection;

import flat.events.ActionEvent;
import flat.graphics.context.Context;
import flat.graphics.image.Drawable;
import flat.graphics.image.DrawableReader;
import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.uxml.value.*;
import flat.widget.Scene;
import flat.widget.Widget;
import flat.widget.enums.ImageFilter;
import flat.window.Activity;
import flat.window.Window;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DrawableReader.class})
public class RadioButtonTest {

    Window window;
    Context context;
    Activity activity;
    UXTheme theme;

    ResourceStream resActive;
    ResourceStream resInactive;

    Drawable iconActive;
    Drawable iconInactive;

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
        when(iconInactive.getWidth()).thenReturn(20f);
        when(iconInactive.getHeight()).thenReturn(16f);

        resActive = mock(ResourceStream.class);
        resInactive = mock(ResourceStream.class);

        when(DrawableReader.parse(resActive)).thenReturn(iconActive);
        when(DrawableReader.parse(resInactive)).thenReturn(iconInactive);
    }

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);
        UXListener<ActionEvent> action = (UXListener<ActionEvent>) mock(UXListener.class);
        when(controller.getListenerMethod("onActionWork", ActionEvent.class)).thenReturn(action);

        RadioButton radioButton = new RadioButton();

        radioButton.setAttributes(createNonDefaultValues(), "radiobutton");
        radioButton.applyAttributes(controller);
        radioButton.applyStyle();

        assertTrue(radioButton.isActive());
        assertEquals(iconActive, radioButton.getIconActive());
        assertEquals(iconInactive, radioButton.getIconInactive());
        assertEquals(1.0f, radioButton.getIconTransitionDuration(), 0.0001f);
        assertEquals(0xFF0000FF, radioButton.getColor());
        assertEquals(ImageFilter.LINEAR, radioButton.getIconImageFilter());

        assertEquals(action, radioButton.getActionListener());
    }

    @Test
    public void measure() {
        RadioButton radioButton = new RadioButton();
        radioButton.setIconActive(iconActive);
        radioButton.setIconInactive(iconInactive);
        radioButton.setActive(true);
        radioButton.onMeasure();

        assertEquals(20, radioButton.getMeasureWidth(), 0.1f);
        assertEquals(20, radioButton.getMeasureHeight(), 0.1f);

        radioButton.setMargins(1, 2, 3, 4);
        radioButton.setPadding(5, 4, 2, 3);
        radioButton.onMeasure();

        assertEquals(20 + 13, radioButton.getMeasureWidth(), 0.1f);
        assertEquals(20 + 11, radioButton.getMeasureHeight(), 0.1f);

        radioButton.setPrefSize(100, 200);
        radioButton.onMeasure();

        assertEquals(106, radioButton.getMeasureWidth(), 0.1f);
        assertEquals(204, radioButton.getMeasureHeight(), 0.1f);

        radioButton.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        radioButton.onMeasure();

        assertEquals(Widget.MATCH_PARENT, radioButton.getMeasureWidth(), 0.1f);
        assertEquals(Widget.MATCH_PARENT, radioButton.getMeasureHeight(), 0.1f);
    }

    @Test
    public void fireAction() {
        RadioButton radioButton = new RadioButton();
        radioButton.setIconActive(iconActive);
        radioButton.setIconInactive(iconInactive);

        radioButton.setActive(false);
        assertFalse(radioButton.isActive());

        radioButton.fire();
        assertTrue(radioButton.isActive());

        radioButton.fire();
        assertTrue(radioButton.isActive());
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        UXValue uxIconActive = mock(UXValue.class);
        when(uxIconActive.asResource(any())).thenReturn(resActive);
        UXValue uxIconInactive = mock(UXValue.class);
        when(uxIconInactive.asResource(any())).thenReturn(resInactive);

        hash.put(UXHash.getHash("on-action"), new UXValueText("onActionWork"));
        hash.put(UXHash.getHash("color"), new UXValueColor(0xFF0000FF));
        hash.put(UXHash.getHash("icon-image-filter"), new UXValueText(ImageFilter.LINEAR.toString()));
        hash.put(UXHash.getHash("icon-active"), uxIconActive);
        hash.put(UXHash.getHash("icon-inactive"), uxIconInactive);
        hash.put(UXHash.getHash("icon-transition-duration"), new UXValueNumber(1.0f));
        hash.put(UXHash.getHash("active"), new UXValueBool(true));
        return hash;
    }
}