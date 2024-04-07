package flat.window.event;

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
public class EventDataCharModsTest {

    @Test
    public void happyDay() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widget = mock(Widget.class);

        when(window.getActivity()).thenReturn(activity);
        when(activity.getFocus()).thenReturn(widget);
        ArgumentCaptor<KeyEvent> argumentCaptor = ArgumentCaptor.forClass(KeyEvent.class);

        // Execution
        EventDataCharMods event = EventDataCharMods.get("A".codePointAt(0), 0b1010);
        event.handle(window);

        // Assertion
        verify(window, times(1)).getActivity();
        verify(activity, times(1)).getFocus();

        verify(widget, times(1)).fireKey(argumentCaptor.capture());

        KeyEvent keyEvent = argumentCaptor.getValue();
        assertEquals("A", keyEvent.getChar());
        assertEquals(KeyEvent.TYPED, keyEvent.getType());

        assertFalse(keyEvent.isShiftDown());
        assertFalse(keyEvent.isAltDown());
        assertTrue(keyEvent.isCtrlDown());
        assertTrue(keyEvent.isSuperDown());
    }
}