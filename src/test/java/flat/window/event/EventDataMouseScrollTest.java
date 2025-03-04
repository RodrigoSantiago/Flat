package flat.window.event;

import flat.events.PointerEvent;
import flat.events.ScrollEvent;
import flat.widget.Widget;
import flat.window.Activity;
import flat.window.Window;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class EventDataMouseScrollTest {

    @Test
    public void happyDay() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widget = mock(Widget.class);

        EventDataPointer pointer = new EventDataPointer(-1);
        pointer.setPosition(5, 15);
        when(window.getPointer()).thenReturn(pointer);

        when(window.getActivity()).thenReturn(activity);
        when(activity.findByPosition(anyFloat(), anyFloat(), anyBoolean())).thenReturn(widget);
        ArgumentCaptor<ScrollEvent> argumentCaptor = ArgumentCaptor.forClass(ScrollEvent.class);

        // Execution
        EventDataMouseScroll event = EventDataMouseScroll.get(10, 20);
        event.handle(window);

        // Assertion
        verify(activity, times(1)).findByPosition(anyFloat(), anyFloat(), anyBoolean());

        verify(widget, times(1)).fireScroll(argumentCaptor.capture());

        ScrollEvent scrollEvent = argumentCaptor.getValue();
        assertEquals(10f, scrollEvent.getDeltaX(), 0.0001f);
        assertEquals(20f, scrollEvent.getDeltaY(), 0.0001f);
        assertEquals(5f, scrollEvent.getX(), 0.0001f);
        assertEquals(15f, scrollEvent.getY(), 0.0001f);
        assertEquals(ScrollEvent.SCROLL, scrollEvent.getType());
    }
}