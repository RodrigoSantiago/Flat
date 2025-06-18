package flat.window.event;

import flat.backend.WLEnums;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class EventDataMouseMoveTest {

    // move
    @Test
    public void eventMove() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widgetA = mock(Widget.class);
        Widget widgetB = mock(Widget.class);
        EventDataPointer pointer = new EventDataPointer(window, -1);
        pointer.setPosition(10, 20);

        when(window.getActivity()).thenReturn(activity);
        when(activity.findByPosition(anyFloat(), anyFloat(), anyBoolean())).thenReturn(widgetA).thenReturn(widgetB);
        when(window.getPointer()).thenReturn(pointer);
        ArgumentCaptor<HoverEvent> argumentCaptor = ArgumentCaptor.forClass(HoverEvent.class);

        // Execution
        EventDataMouseMove.get(10, 20).handle(window);
        EventDataMouseMove.get(20, 30).handle(window);

        // Assertion
        verify(activity, times(2)).findByPosition(anyFloat(), anyFloat(), anyBoolean());

        verify(widgetA, times(3)).fireHover(argumentCaptor.capture());
        verify(widgetB, times(2)).fireHover(argumentCaptor.capture());

        assertHoverEvent(argumentCaptor.getAllValues().get(0), HoverEvent.ENTERED, 10, 20);
        assertHoverEvent(argumentCaptor.getAllValues().get(1), HoverEvent.MOVED, 10, 20);
        assertHoverEvent(argumentCaptor.getAllValues().get(2), HoverEvent.EXITED, 20, 30);
        assertHoverEvent(argumentCaptor.getAllValues().get(3), HoverEvent.ENTERED, 20, 30);
        assertHoverEvent(argumentCaptor.getAllValues().get(4), HoverEvent.MOVED, 20, 30);
    }

    // press + drag [accept] + releas
    @Test
    public void eventPressDragRelease() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widget = mock(Widget.class);
        EventDataPointer pointer = new EventDataPointer(window, -1);
        pointer.setPosition(10, 20);

        when(window.getActivity()).thenReturn(activity);
        when(widget.getActivity()).thenReturn(activity);
        when(activity.findByPosition(anyFloat(), anyFloat(), anyBoolean())).thenReturn(widget);
        when(window.getPointer()).thenReturn(pointer);

        int[] time = {0};
        doAnswer(obj -> {
            DragEvent dragEvent = obj.getArgument(0);
            if (time[0] == 0) {
                time[0] = 1;
                dragEvent.accept(widget);
            }
            return null;
        }).when(widget).fireDrag(any());

        ArgumentCaptor<HoverEvent> capHover = ArgumentCaptor.forClass(HoverEvent.class);
        ArgumentCaptor<DragEvent> capDrag = ArgumentCaptor.forClass(DragEvent.class);
        ArgumentCaptor<PointerEvent> capPointer = ArgumentCaptor.forClass(PointerEvent.class);

        // Execution
        EventDataMouseMove.get(10, 20).handle(window);
        EventDataMouseButton.get(1, WLEnums.PRESS).handle(window);
        EventDataMouseMove.get(20, 30).handle(window);
        EventDataMouseButton.get(1, WLEnums.RELEASE).handle(window);

        // Assertion
        verify(activity, times(4)).findByPosition(anyFloat(), anyFloat(), anyBoolean());

        verify(widget, times(3)).fireHover(capHover.capture());

        verify(widget, times(3)).firePointer(capPointer.capture());
        verify(widget, times(3)).fireDrag(capDrag.capture());

        // Hover is Independent
        assertHoverEvent(capHover.getAllValues().get(0), HoverEvent.ENTERED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(1), HoverEvent.MOVED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(2), HoverEvent.MOVED, 20, 30);

        assertPointerEvent(capPointer.getAllValues().get(0), PointerEvent.PRESSED, 10, 20);
        assertPointerEvent(capPointer.getAllValues().get(1), PointerEvent.DRAGGED, 20, 30);
        assertPointerEvent(capPointer.getAllValues().get(2), PointerEvent.RELEASED, 20, 30);

        assertDragEvent(capDrag.getAllValues().get(0), DragEvent.STARTED, widget, null, null);
        assertDragEvent(capDrag.getAllValues().get(1), DragEvent.OVER, widget, null, null);
        assertDragEvent(capDrag.getAllValues().get(2), DragEvent.DONE, widget, null, null);

    }


    // press + drag [accept] + releas + press + drag [accept] + releas
    @Test
    public void eventPressDragReleaseRepeat() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widget = mock(Widget.class);
        EventDataPointer pointer = new EventDataPointer(window, -1);
        pointer.setPosition(10, 20);

        when(window.getActivity()).thenReturn(activity);
        when(widget.getActivity()).thenReturn(activity);
        when(activity.findByPosition(anyFloat(), anyFloat(), anyBoolean())).thenReturn(widget);
        when(window.getPointer()).thenReturn(pointer);

        int[] time = {0};
        doAnswer(obj -> {
            DragEvent dragEvent = obj.getArgument(0);
            if (time[0] == 0) {
                time[0] = 1;
                dragEvent.accept(widget);
            }
            return null;
        }).when(widget).fireDrag(any());

        ArgumentCaptor<HoverEvent> capHover = ArgumentCaptor.forClass(HoverEvent.class);
        ArgumentCaptor<DragEvent> capDrag = ArgumentCaptor.forClass(DragEvent.class);
        ArgumentCaptor<PointerEvent> capPointer = ArgumentCaptor.forClass(PointerEvent.class);

        // Execution
        EventDataMouseMove.get(10, 20).handle(window);
        EventDataMouseButton.get(1, WLEnums.PRESS).handle(window);
        EventDataMouseMove.get(20, 30).handle(window);
        EventDataMouseButton.get(1, WLEnums.RELEASE).handle(window);
        time[0] = 0;
        EventDataMouseMove.get(10, 20).handle(window);
        EventDataMouseButton.get(1, WLEnums.PRESS).handle(window);
        EventDataMouseMove.get(20, 30).handle(window);
        EventDataMouseButton.get(1, WLEnums.RELEASE).handle(window);

        // Assertion
        verify(activity, times(8)).findByPosition(anyFloat(), anyFloat(), anyBoolean());

        verify(widget, times(5)).fireHover(capHover.capture());

        verify(widget, times(6)).firePointer(capPointer.capture());
        verify(widget, times(6)).fireDrag(capDrag.capture());

        // Hover is Independent
        assertHoverEvent(capHover.getAllValues().get(0), HoverEvent.ENTERED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(1), HoverEvent.MOVED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(2), HoverEvent.MOVED, 20, 30);

        assertPointerEvent(capPointer.getAllValues().get(0), PointerEvent.PRESSED, 10, 20);
        assertPointerEvent(capPointer.getAllValues().get(1), PointerEvent.DRAGGED, 20, 30);
        assertPointerEvent(capPointer.getAllValues().get(2), PointerEvent.RELEASED, 20, 30);

        assertDragEvent(capDrag.getAllValues().get(0), DragEvent.STARTED, widget, null, null);
        assertDragEvent(capDrag.getAllValues().get(1), DragEvent.OVER, widget, null, null);
        assertDragEvent(capDrag.getAllValues().get(2), DragEvent.DONE, widget, null, null);

        // Hover is Independent
        assertHoverEvent(capHover.getAllValues().get(3), HoverEvent.MOVED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(4), HoverEvent.MOVED, 20, 30);

        assertPointerEvent(capPointer.getAllValues().get(3), PointerEvent.PRESSED, 10, 20);
        assertPointerEvent(capPointer.getAllValues().get(4), PointerEvent.DRAGGED, 20, 30);
        assertPointerEvent(capPointer.getAllValues().get(5), PointerEvent.RELEASED, 20, 30);

        assertDragEvent(capDrag.getAllValues().get(3), DragEvent.STARTED, widget, null, null);
        assertDragEvent(capDrag.getAllValues().get(4), DragEvent.OVER, widget, null, null);
        assertDragEvent(capDrag.getAllValues().get(5), DragEvent.DONE, widget, null, null);
    }

    // press + drag [reject] + releas
    @Test
    public void eventPressDragRejectRelease() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widget = mock(Widget.class);
        EventDataPointer pointer = new EventDataPointer(window, -1);
        pointer.setPosition(10, 20);

        when(window.getActivity()).thenReturn(activity);
        when(activity.findByPosition(anyFloat(), anyFloat(), anyBoolean())).thenReturn(widget);
        when(window.getPointer()).thenReturn(pointer);

        ArgumentCaptor<HoverEvent> capHover = ArgumentCaptor.forClass(HoverEvent.class);
        ArgumentCaptor<DragEvent> capDrag = ArgumentCaptor.forClass(DragEvent.class);
        ArgumentCaptor<PointerEvent> capPointer = ArgumentCaptor.forClass(PointerEvent.class);

        // Execution
        EventDataMouseMove.get(10, 20).handle(window);
        EventDataMouseButton.get(1, WLEnums.PRESS).handle(window);
        EventDataMouseMove.get(20, 30).handle(window);
        EventDataMouseButton.get(1, WLEnums.RELEASE).handle(window);

        // Assertion
        verify(activity, times(4)).findByPosition(anyFloat(), anyFloat(), anyBoolean());

        verify(widget, times(3)).fireHover(capHover.capture());

        verify(widget, times(3)).firePointer(capPointer.capture());
        verify(widget, times(1)).fireDrag(capDrag.capture());

        // Hover is Independent
        assertHoverEvent(capHover.getAllValues().get(0), HoverEvent.ENTERED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(1), HoverEvent.MOVED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(2), HoverEvent.MOVED, 20, 30);

        assertPointerEvent(capPointer.getAllValues().get(0), PointerEvent.PRESSED, 10, 20);
        assertPointerEvent(capPointer.getAllValues().get(1), PointerEvent.DRAGGED, 20, 30);
        assertPointerEvent(capPointer.getAllValues().get(2), PointerEvent.RELEASED, 20, 30);

        assertDragEvent(capDrag.getAllValues().get(0), DragEvent.STARTED, null, null, null);
    }

    // press + drag [accept] + press2 + releas
    @Test
    public void eventPressDragPress2Release() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widget = mock(Widget.class);
        EventDataPointer pointer = new EventDataPointer(window, -1);
        pointer.setPosition(10, 20);

        when(window.getActivity()).thenReturn(activity);
        when(widget.getActivity()).thenReturn(activity);
        when(activity.findByPosition(anyFloat(), anyFloat(), anyBoolean())).thenReturn(widget);
        when(window.getPointer()).thenReturn(pointer);

        int[] time = {0};
        doAnswer(obj -> {
            DragEvent dragEvent = obj.getArgument(0);
            if (time[0] == 0) {
                time[0] = 1;
                dragEvent.accept(widget);
            }
            return null;
        }).when(widget).fireDrag(any());

        ArgumentCaptor<HoverEvent> capHover = ArgumentCaptor.forClass(HoverEvent.class);
        ArgumentCaptor<DragEvent> capDrag = ArgumentCaptor.forClass(DragEvent.class);
        ArgumentCaptor<PointerEvent> capPointer = ArgumentCaptor.forClass(PointerEvent.class);

        // Execution
        EventDataMouseMove.get(10, 20).handle(window);
        EventDataMouseButton.get(1, WLEnums.PRESS).handle(window);
        EventDataMouseMove.get(20, 30).handle(window);
        EventDataMouseButton.get(2, WLEnums.PRESS).handle(window);

        // Assertion
        verify(activity, times(4)).findByPosition(anyFloat(), anyFloat(), anyBoolean());

        verify(widget, times(3)).fireHover(capHover.capture());

        verify(widget, times(3)).firePointer(any());
        verify(widget, times(2)).fireDrag(any());

        // Execution 2
        EventDataMouseButton.get(1, WLEnums.RELEASE).handle(window);

        // Assertion2
        verify(activity, times(5)).findByPosition(anyFloat(), anyFloat(), anyBoolean());

        verify(widget, times(4)).firePointer(capPointer.capture());
        verify(widget, times(3)).fireDrag(capDrag.capture());

        // Hover is Independent
        assertHoverEvent(capHover.getAllValues().get(0), HoverEvent.ENTERED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(1), HoverEvent.MOVED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(2), HoverEvent.MOVED, 20, 30);

        assertPointerEvent(capPointer.getAllValues().get(0), PointerEvent.PRESSED, 10, 20);
        assertPointerEvent(capPointer.getAllValues().get(1), PointerEvent.DRAGGED, 20, 30);
        assertPointerEvent(capPointer.getAllValues().get(2), PointerEvent.PRESSED, 20, 30);
        assertPointerEvent(capPointer.getAllValues().get(3), PointerEvent.RELEASED, 20, 30);

        assertDragEvent(capDrag.getAllValues().get(0), DragEvent.STARTED, widget, null, null);
        assertDragEvent(capDrag.getAllValues().get(1), DragEvent.OVER, widget, null, null);
        assertDragEvent(capDrag.getAllValues().get(2), DragEvent.DONE, widget, null, null);
    }

    // press + drag [accept] + releas2 + releas
    @Test
    public void eventPressDragRelease2Release() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widget = mock(Widget.class);
        EventDataPointer pointer = new EventDataPointer(window, -1);
        pointer.setPosition(10, 20);

        when(window.getActivity()).thenReturn(activity);
        when(widget.getActivity()).thenReturn(activity);
        when(activity.findByPosition(anyFloat(), anyFloat(), anyBoolean())).thenReturn(widget);
        when(window.getPointer()).thenReturn(pointer);

        int[] time = {0};
        doAnswer(obj -> {
            DragEvent dragEvent = obj.getArgument(0);
            if (time[0] == 0) {
                time[0] = 1;
                dragEvent.accept(widget);
            }
            return null;
        }).when(widget).fireDrag(any());

        ArgumentCaptor<HoverEvent> capHover = ArgumentCaptor.forClass(HoverEvent.class);
        ArgumentCaptor<DragEvent> capDrag = ArgumentCaptor.forClass(DragEvent.class);
        ArgumentCaptor<PointerEvent> capPointer = ArgumentCaptor.forClass(PointerEvent.class);

        // Execution
        EventDataMouseMove.get(10, 20).handle(window);
        EventDataMouseButton.get(1, WLEnums.PRESS).handle(window);
        EventDataMouseMove.get(20, 30).handle(window);
        EventDataMouseButton.get(2, WLEnums.RELEASE).handle(window);

        // Assertion
        verify(activity, times(4)).findByPosition(anyFloat(), anyFloat(), anyBoolean());

        verify(widget, times(3)).fireHover(capHover.capture());

        verify(widget, times(3)).firePointer(any());
        verify(widget, times(2)).fireDrag(any());

        // Execution 2
        EventDataMouseButton.get(1, WLEnums.RELEASE).handle(window);

        // Assertion2
        verify(activity, times(5)).findByPosition(anyFloat(), anyFloat(), anyBoolean());

        verify(widget, times(4)).firePointer(capPointer.capture());
        verify(widget, times(3)).fireDrag(capDrag.capture());

        // Hover is Independent
        assertHoverEvent(capHover.getAllValues().get(0), HoverEvent.ENTERED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(1), HoverEvent.MOVED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(2), HoverEvent.MOVED, 20, 30);

        assertPointerEvent(capPointer.getAllValues().get(0), PointerEvent.PRESSED, 10, 20);
        assertPointerEvent(capPointer.getAllValues().get(1), PointerEvent.DRAGGED, 20, 30);
        assertPointerEvent(capPointer.getAllValues().get(2), PointerEvent.RELEASED, 20, 30);
        assertPointerEvent(capPointer.getAllValues().get(3), PointerEvent.RELEASED, 20, 30);

        assertDragEvent(capDrag.getAllValues().get(0), DragEvent.STARTED, widget, null, null);
        assertDragEvent(capDrag.getAllValues().get(1), DragEvent.OVER, widget, null, null);
        assertDragEvent(capDrag.getAllValues().get(2), DragEvent.DONE, widget, null, null);

    }

    // press + drag [accept] + move [accept] + releas
    @Test
    public void eventPressDragMoveRelease() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widgetA = mock(Widget.class);
        Widget widgetB = mock(Widget.class);
        EventDataPointer pointer = new EventDataPointer(window, -1);
        pointer.setPosition(10, 20);

        when(window.getActivity()).thenReturn(activity);
        when(widgetA.getActivity()).thenReturn(activity);
        when(widgetB.getActivity()).thenReturn(activity);
        when(activity.findByPosition(10f, 20f, false)).thenReturn(widgetA);
        when(activity.findByPosition(20f, 30f, false)).thenReturn(widgetB);
        when(window.getPointer()).thenReturn(pointer);

        int[] time = {0};
        doAnswer(obj -> {
            DragEvent dragEvent = obj.getArgument(0);
            if (time[0] == 0) {
                time[0] = 1;
                dragEvent.accept(widgetA);
            }
            return null;
        }).when(widgetA).fireDrag(any());
        doAnswer(obj -> {
            DragEvent dragEvent = obj.getArgument(0);
            dragEvent.accept(widgetB);
            return null;
        }).when(widgetB).fireDrag(any());

        ArgumentCaptor<HoverEvent> capHover = ArgumentCaptor.forClass(HoverEvent.class);
        ArgumentCaptor<DragEvent> capDrag = ArgumentCaptor.forClass(DragEvent.class);
        ArgumentCaptor<PointerEvent> capPointer = ArgumentCaptor.forClass(PointerEvent.class);

        // Execution
        EventDataMouseMove.get(10, 20).handle(window);
        EventDataMouseButton.get(1, WLEnums.PRESS).handle(window);
        EventDataMouseMove.get(20, 30).handle(window);
        EventDataMouseButton.get(1, WLEnums.RELEASE).handle(window);

        // Assertion
        verify(activity, times(4)).findByPosition(anyFloat(), anyFloat(), anyBoolean());

        verify(widgetA, times(3)).fireHover(capHover.capture());
        verify(widgetB, times(2)).fireHover(capHover.capture());

        verify(widgetA, times(3)).firePointer(capPointer.capture());
        verify(widgetA, times(3)).fireDrag(capDrag.capture());
        verify(widgetB, times(4)).fireDrag(capDrag.capture());

        // Hover is Independent
        assertHoverEvent(capHover.getAllValues().get(0), HoverEvent.ENTERED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(1), HoverEvent.MOVED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(2), HoverEvent.EXITED, 20, 30);
        assertHoverEvent(capHover.getAllValues().get(3), HoverEvent.ENTERED, 20, 30);
        assertHoverEvent(capHover.getAllValues().get(4), HoverEvent.MOVED, 20, 30);

        assertPointerEvent(capPointer.getAllValues().get(0), PointerEvent.PRESSED, 10, 20);
        assertPointerEvent(capPointer.getAllValues().get(1), PointerEvent.DRAGGED, 20, 30);
        assertPointerEvent(capPointer.getAllValues().get(2), PointerEvent.RELEASED, 20, 30);

        assertDragEvent(capDrag.getAllValues().get(0), DragEvent.STARTED, widgetA, null, null);
        assertDragEvent(capDrag.getAllValues().get(1), DragEvent.OVER, widgetA, null, widgetB);
        assertDragEvent(capDrag.getAllValues().get(2), DragEvent.DONE, widgetA, null, widgetB);

        assertDragEvent(capDrag.getAllValues().get(3), DragEvent.ENTERED, widgetA, null, widgetB);
        assertDragEvent(capDrag.getAllValues().get(4), DragEvent.HOVER, widgetA, null, widgetB);
        assertDragEvent(capDrag.getAllValues().get(5), DragEvent.DROPPED, widgetA, null, widgetB);
        assertDragEvent(capDrag.getAllValues().get(6), DragEvent.EXITED, widgetA, null, widgetB);
    }

    // press + drag [accept] + move [reject] + releas
    @Test
    public void eventPressDragMoveRejectRelease() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widgetA = mock(Widget.class);
        Widget widgetB = mock(Widget.class);
        EventDataPointer pointer = new EventDataPointer(window, -1);
        pointer.setPosition(10, 20);

        when(window.getActivity()).thenReturn(activity);
        when(widgetA.getActivity()).thenReturn(activity);
        when(widgetB.getActivity()).thenReturn(activity);
        when(activity.findByPosition(10f, 20f, false)).thenReturn(widgetA);
        when(activity.findByPosition(20f, 30f, false)).thenReturn(widgetB);
        when(window.getPointer()).thenReturn(pointer);

        int[] time = {0};
        doAnswer(obj -> {
            DragEvent dragEvent = obj.getArgument(0);
            if (time[0] == 0) {
                time[0] = 1;
                dragEvent.accept(widgetA);
            }
            return null;
        }).when(widgetA).fireDrag(any());

        ArgumentCaptor<HoverEvent> capHover = ArgumentCaptor.forClass(HoverEvent.class);
        ArgumentCaptor<DragEvent> capDrag = ArgumentCaptor.forClass(DragEvent.class);
        ArgumentCaptor<PointerEvent> capPointer = ArgumentCaptor.forClass(PointerEvent.class);

        // Execution
        EventDataMouseMove.get(10, 20).handle(window);
        EventDataMouseButton.get(1, WLEnums.PRESS).handle(window);
        EventDataMouseMove.get(20, 30).handle(window);
        EventDataMouseButton.get(1, WLEnums.RELEASE).handle(window);

        // Assertion
        verify(activity, times(4)).findByPosition(anyFloat(), anyFloat(), anyBoolean());

        verify(widgetA, times(3)).fireHover(capHover.capture());
        verify(widgetB, times(2)).fireHover(capHover.capture());

        verify(widgetA, times(3)).firePointer(capPointer.capture());
        verify(widgetA, times(3)).fireDrag(capDrag.capture());
        verify(widgetB, times(3)).fireDrag(capDrag.capture());

        // Hover is Independent
        assertHoverEvent(capHover.getAllValues().get(0), HoverEvent.ENTERED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(1), HoverEvent.MOVED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(2), HoverEvent.EXITED, 20, 30);
        assertHoverEvent(capHover.getAllValues().get(3), HoverEvent.ENTERED, 20, 30);
        assertHoverEvent(capHover.getAllValues().get(4), HoverEvent.MOVED, 20, 30);

        assertPointerEvent(capPointer.getAllValues().get(0), PointerEvent.PRESSED, 10, 20);
        assertPointerEvent(capPointer.getAllValues().get(1), PointerEvent.DRAGGED, 20, 30);
        assertPointerEvent(capPointer.getAllValues().get(2), PointerEvent.RELEASED, 20, 30);

        assertDragEvent(capDrag.getAllValues().get(0), DragEvent.STARTED, widgetA, null, null);
        assertDragEvent(capDrag.getAllValues().get(1), DragEvent.OVER, widgetA, null, null);
        assertDragEvent(capDrag.getAllValues().get(2), DragEvent.DONE, widgetA, null, null);

        assertDragEvent(capDrag.getAllValues().get(3), DragEvent.ENTERED, widgetA, null, null);
        assertDragEvent(capDrag.getAllValues().get(4), DragEvent.HOVER, widgetA, null, null);
        assertDragEvent(capDrag.getAllValues().get(5), DragEvent.EXITED, widgetA, null, null);

    }

    // press + drag [accept] + move [accept] + releas
    @Test
    public void eventPressDragMoveCancelRelease() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widgetA = mock(Widget.class);
        Widget widgetB = mock(Widget.class);
        EventDataPointer pointer = new EventDataPointer(window, -1);
        pointer.setPosition(10, 20);

        when(window.getActivity()).thenReturn(activity);
        when(widgetA.getActivity()).thenReturn(activity);
        when(widgetB.getActivity()).thenReturn(activity);
        when(activity.findByPosition(10f, 20f, false)).thenReturn(widgetA);
        when(activity.findByPosition(20f, 30f, false)).thenReturn(widgetB);
        when(window.getPointer()).thenReturn(pointer);

        int[] time = {0};
        doAnswer(obj -> {
            DragEvent dragEvent = obj.getArgument(0);
            if (time[0] == 0) {
                time[0] = 1;
                dragEvent.accept(widgetA);
            } else if (time[0] == 1) {
                time[0] = 2;
                dragEvent.cancel();
            }
            return null;
        }).when(widgetA).fireDrag(any());
        doAnswer(obj -> {
            DragEvent dragEvent = obj.getArgument(0);
            dragEvent.accept(widgetB);
            return null;
        }).when(widgetB).fireDrag(any());

        ArgumentCaptor<HoverEvent> capHover = ArgumentCaptor.forClass(HoverEvent.class);
        ArgumentCaptor<DragEvent> capDrag = ArgumentCaptor.forClass(DragEvent.class);
        ArgumentCaptor<PointerEvent> capPointer = ArgumentCaptor.forClass(PointerEvent.class);

        // Execution
        EventDataMouseMove.get(10, 20).handle(window);
        EventDataMouseButton.get(1, WLEnums.PRESS).handle(window);
        EventDataMouseMove.get(20, 30).handle(window);
        EventDataMouseButton.get(1, WLEnums.RELEASE).handle(window);

        // Assertion
        verify(activity, times(4)).findByPosition(anyFloat(), anyFloat(), anyBoolean());

        verify(widgetA, times(3)).fireHover(capHover.capture());
        verify(widgetB, times(2)).fireHover(capHover.capture());

        verify(widgetA, times(3)).firePointer(capPointer.capture());
        verify(widgetA, times(3)).fireDrag(capDrag.capture());
        verify(widgetB, times(3)).fireDrag(capDrag.capture());

        // Hover is Independent
        assertHoverEvent(capHover.getAllValues().get(0), HoverEvent.ENTERED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(1), HoverEvent.MOVED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(2), HoverEvent.EXITED, 20, 30);
        assertHoverEvent(capHover.getAllValues().get(3), HoverEvent.ENTERED, 20, 30);
        assertHoverEvent(capHover.getAllValues().get(4), HoverEvent.MOVED, 20, 30);

        assertPointerEvent(capPointer.getAllValues().get(0), PointerEvent.PRESSED, 10, 20);
        assertPointerEvent(capPointer.getAllValues().get(1), PointerEvent.DRAGGED, 20, 30);
        assertPointerEvent(capPointer.getAllValues().get(2), PointerEvent.RELEASED, 20, 30);

        assertDragEvent(capDrag.getAllValues().get(0), DragEvent.STARTED, widgetA, null, null);
        assertDragEvent(capDrag.getAllValues().get(1), DragEvent.OVER, widgetA, null, widgetB);
        assertDragEvent(capDrag.getAllValues().get(2), DragEvent.DONE, widgetA, null, null);
        assertTrue(capDrag.getAllValues().get(2).isCanceled());

        assertDragEvent(capDrag.getAllValues().get(3), DragEvent.ENTERED, widgetA, null, widgetB);
        assertDragEvent(capDrag.getAllValues().get(4), DragEvent.HOVER, widgetA, null, widgetB);
        assertDragEvent(capDrag.getAllValues().get(5), DragEvent.EXITED, widgetA, null, widgetB);
    }

    // press + drag [accept] + move [accept+cancel] + releas
    @Test
    public void eventPressDragMoveAcceptCancelRelease() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widgetA = mock(Widget.class);
        Widget widgetB = mock(Widget.class);
        EventDataPointer pointer = new EventDataPointer(window, -1);
        pointer.setPosition(10, 20);

        when(window.getActivity()).thenReturn(activity);
        when(widgetA.getActivity()).thenReturn(activity);
        when(widgetB.getActivity()).thenReturn(activity);
        when(activity.findByPosition(10f, 20f, false)).thenReturn(widgetA);
        when(activity.findByPosition(20f, 30f, false)).thenReturn(widgetB);
        when(window.getPointer()).thenReturn(pointer);

        int[] time = {0, 0};
        doAnswer(obj -> {
            DragEvent dragEvent = obj.getArgument(0);
            if (time[0] == 0) {
                time[0] = 1;
                dragEvent.accept(widgetA);
            }
            return null;
        }).when(widgetA).fireDrag(any());
        doAnswer(obj -> {
            DragEvent dragEvent = obj.getArgument(0);
            if (time[1] == 0) {
                time[1] = 1;
                dragEvent.accept(widgetB);
                dragEvent.cancel();
            }
            return null;
        }).when(widgetB).fireDrag(any());

        ArgumentCaptor<HoverEvent> capHover = ArgumentCaptor.forClass(HoverEvent.class);
        ArgumentCaptor<DragEvent> capDrag = ArgumentCaptor.forClass(DragEvent.class);
        ArgumentCaptor<PointerEvent> capPointer = ArgumentCaptor.forClass(PointerEvent.class);

        // Execution
        EventDataMouseMove.get(10, 20).handle(window);
        EventDataMouseButton.get(1, WLEnums.PRESS).handle(window);
        EventDataMouseMove.get(20, 30).handle(window);
        EventDataMouseButton.get(1, WLEnums.RELEASE).handle(window);

        // Assertion
        verify(activity, times(4)).findByPosition(anyFloat(), anyFloat(), anyBoolean());

        verify(widgetA, times(3)).fireHover(capHover.capture());
        verify(widgetB, times(2)).fireHover(capHover.capture());

        verify(widgetA, times(3)).firePointer(capPointer.capture());
        verify(widgetA, times(2)).fireDrag(capDrag.capture());
        verify(widgetB, times(2)).fireDrag(capDrag.capture());

        // Hover is Independent
        assertHoverEvent(capHover.getAllValues().get(0), HoverEvent.ENTERED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(1), HoverEvent.MOVED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(2), HoverEvent.EXITED, 20, 30);
        assertHoverEvent(capHover.getAllValues().get(3), HoverEvent.ENTERED, 20, 30);
        assertHoverEvent(capHover.getAllValues().get(4), HoverEvent.MOVED, 20, 30);

        assertPointerEvent(capPointer.getAllValues().get(0), PointerEvent.PRESSED, 10, 20);
        assertPointerEvent(capPointer.getAllValues().get(1), PointerEvent.DRAGGED, 20, 30);
        assertPointerEvent(capPointer.getAllValues().get(2), PointerEvent.RELEASED, 20, 30);

        assertDragEvent(capDrag.getAllValues().get(0), DragEvent.STARTED, widgetA, null, null);
        assertDragEvent(capDrag.getAllValues().get(1), DragEvent.DONE, widgetA, null, null);
        assertTrue(capDrag.getAllValues().get(1).isCanceled());

        assertDragEvent(capDrag.getAllValues().get(2), DragEvent.ENTERED, widgetA, null, widgetB);
        assertDragEvent(capDrag.getAllValues().get(3), DragEvent.EXITED, widgetA, null, null);
    }

    // press + drag [accept] + move [accept] + releas
    @Test
    public void eventPressDragMoveCancelOnExitRelease() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widgetA = mock(Widget.class);
        Widget widgetB = mock(Widget.class);
        EventDataPointer pointer = new EventDataPointer(window, -1);
        pointer.setPosition(10, 20);

        when(window.getActivity()).thenReturn(activity);
        when(widgetA.getActivity()).thenReturn(activity);
        when(widgetB.getActivity()).thenReturn(activity);
        when(activity.findByPosition(10f, 20f, false)).thenReturn(widgetA);
        when(activity.findByPosition(20f, 30f, false)).thenReturn(widgetB);
        when(window.getPointer()).thenReturn(pointer);

        int[] time = {0};
        doAnswer(obj -> {
            DragEvent dragEvent = obj.getArgument(0);
            if (time[0] == 0) {
                time[0] = 1;
                dragEvent.accept(widgetA);
            }
            return null;
        }).when(widgetA).fireDrag(any());
        doAnswer(obj -> {
            DragEvent dragEvent = obj.getArgument(0);
            if (dragEvent.getType() == DragEvent.ENTERED) {
                dragEvent.accept(widgetB);
            } else if (dragEvent.getType() != DragEvent.HOVER) {
                dragEvent.cancel();
            }
            return null;
        }).when(widgetB).fireDrag(any());

        ArgumentCaptor<HoverEvent> capHover = ArgumentCaptor.forClass(HoverEvent.class);
        ArgumentCaptor<DragEvent> capDrag = ArgumentCaptor.forClass(DragEvent.class);
        ArgumentCaptor<PointerEvent> capPointer = ArgumentCaptor.forClass(PointerEvent.class);

        // Execution
        EventDataMouseMove.get(10, 20).handle(window);
        EventDataMouseButton.get(1, WLEnums.PRESS).handle(window);
        EventDataMouseMove.get(20, 30).handle(window);
        EventDataMouseMove.get(10, 20).handle(window);
        EventDataMouseButton.get(1, WLEnums.RELEASE).handle(window);

        // Assertion
        verify(activity, times(5)).findByPosition(anyFloat(), anyFloat(), anyBoolean());

        verify(widgetA, times(5)).fireHover(capHover.capture());
        verify(widgetB, times(3)).fireHover(capHover.capture());

        verify(widgetA, times(4)).firePointer(capPointer.capture());
        verify(widgetA, times(4)).fireDrag(capDrag.capture());
        verify(widgetB, times(3)).fireDrag(capDrag.capture());

        // Hover is Independent
        assertHoverEvent(capHover.getAllValues().get(0), HoverEvent.ENTERED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(1), HoverEvent.MOVED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(2), HoverEvent.EXITED, 20, 30);

        assertHoverEvent(capHover.getAllValues().get(5), HoverEvent.ENTERED, 20, 30);
        assertHoverEvent(capHover.getAllValues().get(6), HoverEvent.MOVED, 20, 30);
        assertHoverEvent(capHover.getAllValues().get(7), HoverEvent.EXITED, 10, 20);

        assertHoverEvent(capHover.getAllValues().get(3), HoverEvent.ENTERED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(4), HoverEvent.MOVED, 10, 20);

        assertPointerEvent(capPointer.getAllValues().get(0), PointerEvent.PRESSED, 10, 20);
        assertPointerEvent(capPointer.getAllValues().get(1), PointerEvent.DRAGGED, 20, 30);
        assertPointerEvent(capPointer.getAllValues().get(2), PointerEvent.DRAGGED, 10, 20);
        assertPointerEvent(capPointer.getAllValues().get(3), PointerEvent.RELEASED, 10, 20);

        assertDragEvent(capDrag.getAllValues().get(0), DragEvent.STARTED, widgetA, null, null);
        assertDragEvent(capDrag.getAllValues().get(1), DragEvent.OVER, widgetA, null, widgetB);
        assertDragEvent(capDrag.getAllValues().get(2), DragEvent.OVER, widgetA, null, null);
        assertFalse(capDrag.getAllValues().get(2).isCanceled());

        assertDragEvent(capDrag.getAllValues().get(3), DragEvent.DONE, widgetA, null, null);
        assertDragEvent(capDrag.getAllValues().get(4), DragEvent.ENTERED, widgetA, null, widgetB);
        assertDragEvent(capDrag.getAllValues().get(5), DragEvent.HOVER, widgetA, null, widgetB);
        assertDragEvent(capDrag.getAllValues().get(6), DragEvent.EXITED, widgetA, null, null);
    }

    @Test
    public void eventPressDragRejectMoveRelease() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widgetA = mock(Widget.class);
        Widget widgetB = mock(Widget.class);
        EventDataPointer pointer = new EventDataPointer(window, -1);
        pointer.setPosition(10, 20);

        when(window.getActivity()).thenReturn(activity);
        when(activity.findByPosition(10f, 20f, false)).thenReturn(widgetA);
        when(activity.findByPosition(20f, 30f, false)).thenReturn(widgetB);
        when(window.getPointer()).thenReturn(pointer);
        ArgumentCaptor<HoverEvent> capHover = ArgumentCaptor.forClass(HoverEvent.class);
        ArgumentCaptor<DragEvent> capDrag = ArgumentCaptor.forClass(DragEvent.class);
        ArgumentCaptor<PointerEvent> capPointer = ArgumentCaptor.forClass(PointerEvent.class);

        // Execution
        EventDataMouseMove.get(10, 20).handle(window);
        EventDataMouseButton.get(1, WLEnums.PRESS).handle(window);
        EventDataMouseMove.get(20, 30).handle(window);
        EventDataMouseButton.get(1, WLEnums.RELEASE).handle(window);

        // Assertion
        verify(activity, times(4)).findByPosition(anyFloat(), anyFloat(), anyBoolean());

        verify(widgetA, times(3)).fireHover(capHover.capture());
        verify(widgetB, times(2)).fireHover(capHover.capture());

        verify(widgetA, times(3)).firePointer(capPointer.capture());
        verify(widgetA, times(1)).fireDrag(capDrag.capture());

        // Hover is Independent
        assertHoverEvent(capHover.getAllValues().get(0), HoverEvent.ENTERED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(1), HoverEvent.MOVED, 10, 20);
        assertHoverEvent(capHover.getAllValues().get(2), HoverEvent.EXITED, 20, 30);
        assertHoverEvent(capHover.getAllValues().get(3), HoverEvent.ENTERED, 20, 30);
        assertHoverEvent(capHover.getAllValues().get(4), HoverEvent.MOVED, 20, 30);

        assertPointerEvent(capPointer.getAllValues().get(0), PointerEvent.PRESSED, 10, 20);
        assertPointerEvent(capPointer.getAllValues().get(1), PointerEvent.DRAGGED, 20, 30);
        assertPointerEvent(capPointer.getAllValues().get(2), PointerEvent.RELEASED, 20, 30);

        assertDragEvent(capDrag.getAllValues().get(0), DragEvent.STARTED, null, null, null);

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