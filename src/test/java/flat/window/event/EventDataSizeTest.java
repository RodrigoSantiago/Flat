package flat.window.event;

import flat.backend.WLEnums;
import flat.events.KeyEvent;
import flat.widget.Scene;
import flat.widget.Widget;
import flat.window.Activity;
import flat.window.Window;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class EventDataSizeTest {

    @Test
    public void happyDay() {
        // Setup
        Activity activity = mock(Activity.class);
        Window window = mock(Window.class);
        Widget widget = mock(Widget.class);

        when(window.getActivity()).thenReturn(activity);
        when(activity.getFocus()).thenReturn(widget);

        // Execution
        EventDataSize event = EventDataSize.get(400, 600);
        event.handle(window);

        // Assertion
        verify(activity, times(1)).invalidateWidget(any(), anyBoolean());
    }
}