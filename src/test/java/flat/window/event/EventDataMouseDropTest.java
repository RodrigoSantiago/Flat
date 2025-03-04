package flat.window.event;

import flat.events.DragEvent;
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
public class EventDataMouseDropTest {

    @Test
    public void happyDay() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widget = mock(Widget.class);

        when(window.getActivity()).thenReturn(activity);
        when(window.getPointerX()).thenReturn(10f);
        when(window.getPointerY()).thenReturn(20f);
        when(activity.findByPosition(anyFloat(), anyFloat(), anyBoolean())).thenReturn(widget);
        ArgumentCaptor<DragEvent> argumentCaptor = ArgumentCaptor.forClass(DragEvent.class);

        String[] paths = new String[] {"path", "test"};

        // Execution
        EventDataMouseDrop event = EventDataMouseDrop.get(paths);
        event.handle(window);

        // Assertion
        verify(window, times(1)).getActivity();
        verify(activity, times(1)).findByPosition(anyFloat(), anyFloat(), anyBoolean());

        verify(widget, times(1)).fireDrag(argumentCaptor.capture());

        DragEvent dragEvent = argumentCaptor.getValue();
        assertEquals(10f, dragEvent.getX(), 0.0001f);
        assertEquals(20f, dragEvent.getY(), 0.0001f);
        assertEquals(paths, dragEvent.getData());
        assertEquals(widget, dragEvent.getDragHandler());
        assertFalse(dragEvent.isCanceled());
        assertFalse(dragEvent.isAccepted());
        assertNull(dragEvent.getDragAccepted());
        assertEquals(DragEvent.DROPPED, dragEvent.getType());
    }
}