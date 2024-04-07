package flat.window.event;

import flat.backend.WLEnums;
import flat.events.KeyEvent;
import flat.widget.Widget;
import flat.window.Activity;
import flat.window.Window;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class EventDataKeyTest {

    @Test
    public void eventPressed() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widget = mock(Widget.class);

        when(window.getActivity()).thenReturn(activity);
        when(activity.getFocus()).thenReturn(widget);
        ArgumentCaptor<KeyEvent> argumentCaptor = ArgumentCaptor.forClass(KeyEvent.class);

        int keyCode = WLEnums.KEY_A;
        // Execution
        EventDataKey event = EventDataKey.get(keyCode, 0, WLEnums.PRESS, 0b1010);
        event.handle(window);

        // Assertion
        verify(window, times(1)).getActivity();
        verify(activity, times(1)).getFocus();

        verify(widget, times(1)).fireKey(argumentCaptor.capture());

        KeyEvent keyEvent = argumentCaptor.getValue();
        assertEquals("", keyEvent.getChar());
        assertEquals(keyCode, keyEvent.getKeycode());
        assertEquals(KeyEvent.PRESSED, keyEvent.getType());

        assertFalse(keyEvent.isShiftDown());
        assertFalse(keyEvent.isAltDown());
        assertTrue(keyEvent.isCtrlDown());
        assertTrue(keyEvent.isSuperDown());
    }

    @Test
    public void eventReleased() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widget = mock(Widget.class);

        when(window.getActivity()).thenReturn(activity);
        when(activity.getFocus()).thenReturn(widget);
        ArgumentCaptor<KeyEvent> argumentCaptor = ArgumentCaptor.forClass(KeyEvent.class);

        int keyCode = WLEnums.KEY_A;
        // Execution
        EventDataKey event = EventDataKey.get(keyCode, 0, WLEnums.RELEASE, 0b1010);
        event.handle(window);

        // Assertion
        verify(window, times(1)).getActivity();
        verify(activity, times(1)).getFocus();

        verify(widget, times(1)).fireKey(argumentCaptor.capture());

        KeyEvent keyEvent = argumentCaptor.getValue();
        assertEquals("", keyEvent.getChar());
        assertEquals(keyCode, keyEvent.getKeycode());
        assertEquals(KeyEvent.RELEASED, keyEvent.getType());

        assertFalse(keyEvent.isShiftDown());
        assertFalse(keyEvent.isAltDown());
        assertTrue(keyEvent.isCtrlDown());
        assertTrue(keyEvent.isSuperDown());
    }

    @Test
    public void eventRepeated() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widget = mock(Widget.class);

        when(window.getActivity()).thenReturn(activity);
        when(activity.getFocus()).thenReturn(widget);
        ArgumentCaptor<KeyEvent> argumentCaptor = ArgumentCaptor.forClass(KeyEvent.class);

        int keyCode = WLEnums.KEY_A;
        // Execution
        EventDataKey event = EventDataKey.get(keyCode, 0, WLEnums.REPEAT, 0b1010);
        event.handle(window);

        // Assertion
        verify(window, times(1)).getActivity();
        verify(activity, times(1)).getFocus();

        verify(widget, times(1)).fireKey(argumentCaptor.capture());

        KeyEvent keyEvent = argumentCaptor.getValue();
        assertEquals("", keyEvent.getChar());
        assertEquals(keyCode, keyEvent.getKeycode());
        assertEquals(KeyEvent.REPEATED, keyEvent.getType());

        assertFalse(keyEvent.isShiftDown());
        assertFalse(keyEvent.isAltDown());
        assertTrue(keyEvent.isCtrlDown());
        assertTrue(keyEvent.isSuperDown());
    }
}