package flat.window.event;

import flat.events.DragEvent;
import flat.events.EventType;
import flat.events.HoverEvent;
import flat.events.PointerEvent;
import flat.widget.Widget;
import flat.window.Activity;
import flat.window.Window;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;

@RunWith(PowerMockRunner.class)
public class EventDataPointerTest {

    @Test
    public void position() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widgetA = mock(Widget.class);
        Widget widgetB = mock(Widget.class);

        // Execute
        EventDataPointer pointer = new EventDataPointer(-1);
        pointer.setPosition(10, 20);

        // Assert
        assertEquals(10f, pointer.getX(), 0.0001f);
        assertEquals(20f, pointer.getY(), 0.0001f);
    }

    @Test
    public void reset() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widgetA = mock(Widget.class);
        Widget widgetB = mock(Widget.class);

        doAnswer(obj -> {
            DragEvent dragEvent = obj.getArgument(0);
            if (dragEvent.getType() == DragEvent.STARTED) {
                dragEvent.accept(widgetA);
            }
            return null;
        }).when(widgetA).fireDrag(any());

        ArgumentCaptor<HoverEvent> capHover = ArgumentCaptor.forClass(HoverEvent.class);
        ArgumentCaptor<DragEvent> capDrag = ArgumentCaptor.forClass(DragEvent.class);
        ArgumentCaptor<PointerEvent> capPointer = ArgumentCaptor.forClass(PointerEvent.class);

        // Execute
        EventDataPointer pointer = new EventDataPointer(-1);
        pointer.setPosition(10, 20);

        pointer.setHover(widgetA);
        pointer.performHover();
        pointer.setPressed(widgetA, 1);
        pointer.performPressed(1);
        pointer.requestDrag();
        pointer.setDragHover(widgetA);
        pointer.performDrag();
        pointer.performPointerDrag();

        pointer.reset(20, 30);

        // Assert
        verify(widgetA, times(3)).fireHover(capHover.capture());
        verify(widgetA, times(3)).firePointer(capPointer.capture());
        verify(widgetA, times(3)).fireDrag(capDrag.capture());

        assertEquals(20f, pointer.getX(), 0.0001f);
        assertEquals(30f, pointer.getY(), 0.0001f);

        assertHoverEvent(capHover.getAllValues().get(0), HoverEvent.ENTERED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(1), HoverEvent.MOVED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(2), HoverEvent.EXITED, 20, 30);

        assertPointerEvent(capPointer.getAllValues().get(0), PointerEvent.PRESSED, 10, 20);
        assertPointerEvent(capPointer.getAllValues().get(1), PointerEvent.DRAGGED, 10, 20);
        assertPointerEvent(capPointer.getAllValues().get(2), PointerEvent.RELEASED, 20, 30);

        assertDragEvent(capDrag.getAllValues().get(0), DragEvent.STARTED, widgetA, null, null);
        assertDragEvent(capDrag.getAllValues().get(1), DragEvent.OVER, widgetA, null, null);
        assertDragEvent(capDrag.getAllValues().get(2), DragEvent.DONE, widgetA, null, null);
    }

    private static void assertHoverEvent(HoverEvent event, EventType type, float x, float y) {
        assertEquals(type, event.getType());
        assertEquals(x, event.getX(), 0.0001f);
        assertEquals(y, event.getY(), 0.0001f);
    }

    private static void assertPointerEvent(PointerEvent event, EventType type, float x, float y) {
        assertEquals(type, event.getType());
        assertEquals(x, event.getX(), 0.0001f);
        assertEquals(y, event.getY(), 0.0001f);
    }

    private static void assertDragEvent(DragEvent event, EventType type, Widget handler, Object data, Widget accepted) {
        assertEquals(type, event.getType());
        assertEquals(data, event.getData());
        assertEquals(handler, event.getDragHandler());
        assertEquals(accepted, event.getDragAccepted());
    }
}