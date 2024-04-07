package flat.window.event;

import flat.backend.WLEnums;
import flat.events.EventType;
import flat.events.PointerEvent;
import flat.widget.Widget;
import flat.window.Activity;
import flat.window.Window;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class EventDataMouseButtonTest {

    @Test
    public void eventPressed() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widget = mock(Widget.class);
        EventDataPointer pointer = new EventDataPointer(-1);
        pointer.setPosition(10, 20);

        when(window.getActivity()).thenReturn(activity);
        when(activity.findByPosition(anyFloat(), anyFloat(), anyBoolean())).thenReturn(widget);
        ArgumentCaptor<PointerEvent> argumentCaptor = ArgumentCaptor.forClass(PointerEvent.class);
        when(window.getPointer()).thenReturn(pointer);
        when(window.getPointerX()).thenReturn(10f);
        when(window.getPointerY()).thenReturn(20f);

        // Execution
        int button = 1;
        EventDataMouseButton event = EventDataMouseButton.get(button, WLEnums.PRESS);
        event.handle(window);

        // Assertion
        verify(window, times(1)).getActivity();
        verify(activity, times(1)).findByPosition(anyFloat(), anyFloat(), anyBoolean());

        verify(widget, times(1)).firePointer(argumentCaptor.capture());

        assertPointerEvent(argumentCaptor.getValue(), button, PointerEvent.PRESSED);
    }

    @Test
    public void eventReleased() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widget = mock(Widget.class);
        EventDataPointer pointer = new EventDataPointer(-1);
        pointer.setPosition(10, 20);

        when(window.getActivity()).thenReturn(activity);
        when(activity.findByPosition(anyFloat(), anyFloat(), anyBoolean())).thenReturn(widget);
        ArgumentCaptor<PointerEvent> argumentCaptor = ArgumentCaptor.forClass(PointerEvent.class);
        when(window.getPointer()).thenReturn(pointer);
        when(window.getPointerX()).thenReturn(10f);
        when(window.getPointerY()).thenReturn(20f);

        // Execution
        int button = 1;
        EventDataMouseButton event = EventDataMouseButton.get(button, WLEnums.RELEASE);
        event.handle(window);

        // Assertion
        verify(window, times(1)).getActivity();
        verify(activity, times(1)).findByPosition(anyFloat(), anyFloat(), anyBoolean());

        verify(widget, times(1)).firePointer(argumentCaptor.capture());

        assertPointerEvent(argumentCaptor.getValue(), button, PointerEvent.RELEASED);
    }

    @Test
    public void eventPressedReleased() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widget = mock(Widget.class);
        Widget widget2 = mock(Widget.class);
        EventDataPointer pointer = new EventDataPointer(-1);
        pointer.setPosition(10, 20);

        when(window.getActivity()).thenReturn(activity);
        when(activity.findByPosition(anyFloat(), anyFloat(), anyBoolean())).thenReturn(widget).thenReturn(widget2);
        ArgumentCaptor<PointerEvent> argumentCaptor = ArgumentCaptor.forClass(PointerEvent.class);
        when(window.getPointer()).thenReturn(pointer);
        when(window.getPointerX()).thenReturn(10f);
        when(window.getPointerY()).thenReturn(20f);

        // Execution
        int button = 1;
        EventDataMouseButton event = EventDataMouseButton.get(button, WLEnums.PRESS);
        event.handle(window);
        EventDataMouseButton event2 = EventDataMouseButton.get(button, WLEnums.RELEASE);
        event2.handle(window);

        // Assertion
        verify(window, times(2)).getActivity();
        verify(activity, times(2)).findByPosition(anyFloat(), anyFloat(), anyBoolean());

        verify(widget, times(2)).firePointer(argumentCaptor.capture());
        verify(widget2, times(0)).firePointer(any());

        assertPointerEvent(argumentCaptor.getAllValues().get(0), button, PointerEvent.PRESSED);
        assertPointerEvent(argumentCaptor.getAllValues().get(1), button, PointerEvent.RELEASED);
    }

    @Test
    public void eventPressedReleasedWithDifferentButton() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widget = mock(Widget.class);
        Widget widget2 = mock(Widget.class);
        EventDataPointer pointer = new EventDataPointer(-1);
        pointer.setPosition(10, 20);

        when(window.getActivity()).thenReturn(activity);
        when(activity.findByPosition(anyFloat(), anyFloat(), anyBoolean())).thenReturn(widget).thenReturn(widget2);
        ArgumentCaptor<PointerEvent> argumentCaptor = ArgumentCaptor.forClass(PointerEvent.class);
        when(window.getPointer()).thenReturn(pointer);
        when(window.getPointerX()).thenReturn(10f);
        when(window.getPointerY()).thenReturn(20f);

        // Execution
        int button = 1;
        int button2 = 2;
        EventDataMouseButton event = EventDataMouseButton.get(button, WLEnums.PRESS);
        event.handle(window);
        EventDataMouseButton event2 = EventDataMouseButton.get(button2, WLEnums.RELEASE);
        event2.handle(window);

        // Assertion
        verify(window, times(2)).getActivity();
        verify(activity, times(2)).findByPosition(anyFloat(), anyFloat(), anyBoolean());

        verify(widget, times(1)).firePointer(argumentCaptor.capture());
        verify(widget2, times(1)).firePointer(argumentCaptor.capture());

        assertPointerEvent(argumentCaptor.getAllValues().get(0), button, PointerEvent.PRESSED);
        assertPointerEvent(argumentCaptor.getAllValues().get(1), button2, PointerEvent.RELEASED);
    }

    @Test
    public void eventPressedTwiceWithDifferentButtons() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widget = mock(Widget.class);
        Widget widget2 = mock(Widget.class);
        EventDataPointer pointer = new EventDataPointer(-1);
        pointer.setPosition(10, 20);

        when(window.getActivity()).thenReturn(activity);
        when(activity.findByPosition(anyFloat(), anyFloat(), anyBoolean())).thenReturn(widget).thenReturn(widget2);
        ArgumentCaptor<PointerEvent> argumentCaptor = ArgumentCaptor.forClass(PointerEvent.class);
        when(window.getPointer()).thenReturn(pointer);
        when(window.getPointerX()).thenReturn(10f);
        when(window.getPointerY()).thenReturn(20f);

        // Execution
        int button = 1;
        int button2 = 2;
        EventDataMouseButton event = EventDataMouseButton.get(button, WLEnums.PRESS);
        event.handle(window);
        EventDataMouseButton event2 = EventDataMouseButton.get(button2, WLEnums.PRESS);
        event2.handle(window);

        // Assertion
        verify(window, times(2)).getActivity();
        verify(activity, times(2)).findByPosition(anyFloat(), anyFloat(), anyBoolean());

        verify(widget, times(2)).firePointer(argumentCaptor.capture());
        verify(widget2, times(0)).firePointer(any());

        assertPointerEvent(argumentCaptor.getAllValues().get(0), button, PointerEvent.PRESSED);
        assertPointerEvent(argumentCaptor.getAllValues().get(1), button2, PointerEvent.PRESSED);
    }

    private static void assertPointerEvent(PointerEvent pointerEvent, int button, EventType type) {
        assertEquals(10f, pointerEvent.getX(), 0.0001f);
        assertEquals(20f, pointerEvent.getY(), 0.0001f);
        assertEquals(button, pointerEvent.getPointerID());
        assertEquals(type, pointerEvent.getType());
    }
}