package flat.window;

import flat.backend.FlatLibrary;
import flat.backend.GL;
import flat.backend.SVG;
import flat.backend.WL;
import flat.graphics.context.Context;
import flat.window.event.EventData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FlatLibrary.class, WL.class, GL.class, SVG.class, Application.class, Context.class, Activity.class})
public class WindowTest {

    @Before
    public void before() {
        PowerMockito.mockStatic(FlatLibrary.class);
        PowerMockito.mockStatic(WL.class);
        PowerMockito.mockStatic(GL.class);
        PowerMockito.mockStatic(SVG.class);
        PowerMockito.mockStatic(Application.class);
        PowerMockito.mockStatic(Context.class);
        PowerMockito.mockStatic(Activity.class);
    }

    @After
    public void after() {

    }

    @Test
    public void constructor() {
        // Setup
        Context context = mock(Context.class);
        Activity activity = mock(Activity.class);
        when(Context.create(any(), anyLong())).thenReturn(context);
        when(Activity.create(any(), any())).thenReturn(activity);
        when(WL.WindowCreate(anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(1L);
        when(SVG.Create()).thenReturn(1L);

        WindowSettings settings = new WindowSettings.Builder().size(800, 600).multiSamples(1).build();

        // Execution
        Window window = Window.create(settings);

        // Assertion
        assertEquals(context, window.getContext());
        assertEquals(activity, window.getActivity());

        verifyStatic(WL.class);
        WL.WindowCreate(800, 600, 1, false);

        verifyStatic(SVG.class);
        SVG.Create();
    }

    @Test
    public void constructor_InvalidContextCreation() {
        // Setup
        when(WL.WindowCreate(anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(0L);
        when(SVG.Create()).thenReturn(1L);

        WindowSettings settings = new WindowSettings.Builder().size(800, 600).multiSamples(1).build();

        // Execution
        try {
            Window window = Window.create(settings);

            fail("Exception expected");
        } catch (Exception e) {
            assertEquals("Invalid context creation", e.getMessage());
        }

        // Assertion
        verifyStatic(WL.class);
        WL.WindowCreate(800, 600, 1, false);

        verifyZeroInteractions(SVG.class);
        SVG.Create();
    }

    @Test
    public void constructor_InvalidContextCreationSVG() {
        // Setup
        when(WL.WindowCreate(anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(1L);
        when(SVG.Create()).thenReturn(0L);

        WindowSettings settings = new WindowSettings.Builder().size(800, 600).multiSamples(1).build();

        // Execution
        try {
            Window window = Window.create(settings);

            fail("Exception expected");
        } catch (Exception e) {
            assertEquals("Invalid context creation", e.getMessage());
        }

        // Assertion
        verifyStatic(WL.class);
        WL.WindowCreate(800, 600, 1, false);

        verifyStatic(SVG.class);
        SVG.Create();

        verifyStatic(WL.class);
        WL.WindowDestroy(1L);
    }

    @Test
    public void processEvents() {
        // Setup
        Context context = mock(Context.class);
        Activity activity = mock(Activity.class);
        when(Context.create(any(), anyLong())).thenReturn(context);
        when(Activity.create(any(), any())).thenReturn(activity);
        EventData eventData = mock(EventData.class);

        when(WL.WindowCreate(anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(1L);
        when(SVG.Create()).thenReturn(1L);

        WindowSettings settings = new WindowSettings.Builder().size(800, 600).multiSamples(1).build();

        Window window = Window.create(settings);

        // Execution
        window.addEvent(eventData);
        window.processEvents();

        // Assertion
        verify(eventData, times(1)).handle(window);
    }

    @Test
    public void releaseEvents() {
        // Setup
        Context context = mock(Context.class);
        Activity activity = mock(Activity.class);
        when(Context.create(any(), anyLong())).thenReturn(context);
        when(Activity.create(any(), any())).thenReturn(activity);
        EventData eventData = mock(EventData.class);

        when(WL.WindowCreate(anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(1L);
        when(SVG.Create()).thenReturn(1L);

        WindowSettings settings = new WindowSettings.Builder().size(800, 600).multiSamples(1).build();

        Window window = Window.create(settings);

        // Execution
        window.addEvent(eventData);
        window.releaseEvents();
        window.processEvents();

        // Assertion
        verify(eventData, times(0)).handle(window);
    }

    @Test
    public void processSyncCalls() throws Exception {
        // Setup
        Context context = mock(Context.class);
        Activity activity = mock(Activity.class);
        when(Context.create(any(), anyLong())).thenReturn(context);
        when(Activity.create(any(), any())).thenReturn(activity);
        Callable<Integer> callable = mock(Callable.class);
        Runnable runnable = mock(Runnable.class);
        FutureTask<Integer> task = mock(FutureTask.class);

        when(WL.WindowCreate(anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(1L);
        when(SVG.Create()).thenReturn(1L);
        when(callable.call()).thenReturn(1);

        WindowSettings settings = new WindowSettings.Builder().size(800, 600).multiSamples(1).build();

        Window window = Window.create(settings);

        // Execution
        window.runSync(callable);
        window.runSync(runnable);
        window.processSyncCalls();

        // Assertion
        verify(callable, times(1)).call();
        verify(runnable, times(1)).run();
    }

    @Test
    public void onCloseRequest() {
        // Setup

        Context context = mock(Context.class);
        Activity activity = mock(Activity.class);
        when(Context.create(any(), anyLong())).thenReturn(context);
        when(Activity.create(any(), any())).thenReturn(activity);

        when(activity.closeRequest(true)).thenReturn(true);
        when(activity.closeRequest(false)).thenReturn(false);

        when(WL.WindowCreate(anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(1L);
        when(SVG.Create()).thenReturn(1L);

        WindowSettings settings = new WindowSettings.Builder().size(800, 600).multiSamples(1).build();


        Window window = new Window(settings);
        window.processStartup();

        // Execution
        boolean closeA = window.requestClose();
        boolean closeB = window.onRequestClose(true);
        boolean closeC = window.requestClose();

        // Assertion
        verify(activity, times(1)).closeRequest(true);
        verify(activity, times(1)).closeRequest(false);

        assertFalse(closeA);
        assertTrue(closeB);
        assertTrue(closeC);
    }

    @Test
    public void dispose() {
        // Setup
        Context context = mock(Context.class);
        Activity activity = mock(Activity.class);
        when(Context.create(any(), anyLong())).thenReturn(context);
        when(Activity.create(any(), any())).thenReturn(activity);
        EventData eventData = mock(EventData.class);

        when(WL.WindowCreate(anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(1L);
        when(SVG.Create()).thenReturn(1L);

        WindowSettings settings = new WindowSettings.Builder().size(800, 600).multiSamples(1).build();

        Window window = Window.create(settings);

        // Execution
        window.dispose();

        // Assertion
        verify(context, times(1)).dispose();

        verifyStatic(WL.class);
        WL.Close(anyLong());

        verifyStatic(SVG.class);
        SVG.Destroy(anyLong());

        verifyStatic(WL.class);
        WL.WindowDestroy(anyLong());
    }
}
