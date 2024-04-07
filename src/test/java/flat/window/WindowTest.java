package flat.window;

import flat.animations.ActivityTransition;
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
@PrepareForTest({FlatLibrary.class, WL.class, GL.class, SVG.class, Application.class})
public class WindowTest {

    @Before
    public void before() {
        PowerMockito.mockStatic(FlatLibrary.class);
        PowerMockito.mockStatic(WL.class);
        PowerMockito.mockStatic(GL.class);
        PowerMockito.mockStatic(SVG.class);
        PowerMockito.mockStatic(Application.class);
    }

    @After
    public void after() {

    }

    @Test
    public void constructor() {
        // Setup
        ActivityFactory activityFactory = mock(ActivityFactory.class);
        Context context = mock(Context.class);
        when(Application.createContext(any())).thenReturn(context);
        when(WL.WindowCreate(anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(1L);
        when(SVG.Create()).thenReturn(1L);

        // Execution
        Window window = new Window(activityFactory, 800, 600, 1, false);

        // Assertion
        verifyStatic(Application.class);
        Application.createContext(any());

        verifyStatic(WL.class);
        WL.WindowCreate(800, 600, 1, false);

        verifyStatic(SVG.class);
        SVG.Create();

        assertEquals("Unexpected Window Context", context, window.getContext());
    }

    @Test
    public void constructor_InvalidContextCreation() {
        // Setup
        ActivityFactory activityFactory = mock(ActivityFactory.class);
        when(WL.WindowCreate(anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(0L);
        when(SVG.Create()).thenReturn(1L);

        // Execution
        try {
            Window window = new Window(activityFactory, 800, 600, 1, false);

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
        ActivityFactory activityFactory = mock(ActivityFactory.class);
        when(WL.WindowCreate(anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(1L);
        when(SVG.Create()).thenReturn(0L);

        // Execution
        try {
            Window window = new Window(activityFactory, 800, 600, 1, false);

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
        ActivityFactory activityFactory = mock(ActivityFactory.class);
        EventData eventData = mock(EventData.class);

        when(WL.WindowCreate(anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(1L);
        when(SVG.Create()).thenReturn(1L);

        Window window = new Window(activityFactory, 800, 600, 1, false);

        // Execution
        window.addEvent(eventData);
        window.processEvents();

        // Assertion
        verify(eventData, times(1)).handle(window);
    }

    @Test
    public void releaseEvents() {
        // Setup
        ActivityFactory activityFactory = mock(ActivityFactory.class);
        EventData eventData = mock(EventData.class);

        when(WL.WindowCreate(anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(1L);
        when(SVG.Create()).thenReturn(1L);

        Window window = new Window(activityFactory, 800, 600, 1, false);

        // Execution
        window.addEvent(eventData);
        window.releaseEvents();
        window.processEvents();

        // Assertion
        verify(eventData, times(0)).handle(window);
    }

    @Test
    public void processTransitions() {
        // Setup
        Activity activityA = mock(Activity.class);
        Activity activityB = mock(Activity.class);
        ActivityFactory activityFactory = mock(ActivityFactory.class);

        when(WL.WindowCreate(anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(1L);
        when(SVG.Create()).thenReturn(1L);
        when(activityFactory.build(any())).thenReturn(activityA);

        Window window = new Window(activityFactory, 800, 600, 1, false);

        // Execution/Assertion
        window.processStartup();
        assertEquals(activityA, window.getActivity());

        window.setActivity(activityB);
        assertEquals(activityA, window.getActivity());

        window.processTransitions();
        assertEquals(activityB, window.getActivity());

        verify(activityA, times(1)).onShow();
        verify(activityA, times(1)).onStart();
        verify(activityA, times(1)).onPause();
        verify(activityA, times(1)).onHide();

        verify(activityB, times(1)).onShow();
        verify(activityB, times(1)).onStart();
    }

    @Test
    public void processSyncCalls() throws Exception {
        // Setup
        ActivityFactory activityFactory = mock(ActivityFactory.class);
        Callable<Integer> callable = mock(Callable.class);
        Runnable runnable = mock(Runnable.class);
        FutureTask<Integer> task = mock(FutureTask.class);

        when(WL.WindowCreate(anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(1L);
        when(SVG.Create()).thenReturn(1L);
        when(callable.call()).thenReturn(1);

        Window window = new Window(activityFactory, 800, 600, 1, false);

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
        ActivityFactory activityFactory = mock(ActivityFactory.class);
        Activity activityA = mock(Activity.class);

        when(WL.WindowCreate(anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(1L);
        when(SVG.Create()).thenReturn(1L);
        when(activityFactory.build(any())).thenReturn(activityA);
        when(activityA.onCloseRequest(true)).thenReturn(true);
        when(activityA.onCloseRequest(false)).thenReturn(false);

        Window window = new Window(activityFactory, 800, 600, 1, false);
        window.processStartup();

        // Execution
        boolean closeA = window.requestClose();
        boolean closeB = window.onRequestClose(true);
        boolean closeC = window.requestClose();

        // Assertion
        verify(activityA, times(1)).onCloseRequest(true);
        verify(activityA, times(1)).onCloseRequest(false);

        assertFalse(closeA);
        assertTrue(closeB);
        assertTrue(closeC);
    }

    @Test
    public void onCloseRequest_overTransition() {
        // Setup
        ActivityFactory activityFactory = mock(ActivityFactory.class);
        Activity activityA = mock(Activity.class);
        Activity activityB = mock(Activity.class);
        ActivityTransition transition = mock(ActivityTransition.class);
        Context context = mock(Context.class);

        when(WL.WindowCreate(anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(1L);
        when(SVG.Create()).thenReturn(1L);
        when(activityFactory.build(any())).thenReturn(activityA);
        when(activityA.onCloseRequest(true)).thenReturn(true);
        when(activityA.onCloseRequest(false)).thenReturn(false);
        when(transition.isPlaying()).thenReturn(true);

        when(Application.createContext(any())).thenReturn(context);

        Window window = new Window(activityFactory, 800, 600, 1, false);
        window.processStartup();
        window.setActivity(transition);
        window.processTransitions();

        // Execution
        boolean closeA = window.requestClose();
        boolean closeB = window.onRequestClose(true);

        // Assertion
        verify(activityA, times(0)).onCloseRequest(anyBoolean());

        assertFalse(closeA);
        assertFalse(closeB);
    }

    @Test
    public void dispose() {
        // Setup
        ActivityFactory activityFactory = mock(ActivityFactory.class);
        Context context = mock(Context.class);

        when(WL.WindowCreate(anyInt(), anyInt(), anyInt(), anyBoolean())).thenReturn(1L);
        when(SVG.Create()).thenReturn(1L);
        when(Application.createContext(any())).thenReturn(context);

        Window window = new Window(activityFactory, 800, 600, 1, false);

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
