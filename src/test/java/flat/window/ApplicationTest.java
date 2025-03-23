package flat.window;

import flat.backend.*;
import flat.graphics.context.Context;
import flat.resources.ResourcesManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.doThrow;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FlatLibrary.class, WL.class, GL.class, SVG.class, Context.class, Activity.class, Window.class})
public class ApplicationTest {

    @Before
    public void before() {
        PowerMockito.mockStatic(FlatLibrary.class);
        PowerMockito.mockStatic(WL.class);
        PowerMockito.mockStatic(GL.class);
        PowerMockito.mockStatic(SVG.class);
        PowerMockito.mockStatic(Context.class);
        PowerMockito.mockStatic(Activity.class);
        PowerMockito.mockStatic(Window.class);
    }

    @Test
    public void init() {
        // Setup
        ResourcesManager resources = mock(ResourcesManager.class);
        File fileLibrary = mock(File.class);
        WindowSettings settings = new WindowSettings.Builder().size(200, 400).build();

        when(resources.getFlatLibraryFile()).thenReturn(fileLibrary);
        when(WL.Init()).thenReturn(true);

        // Execution
        Application.init(resources);

        // Assertion
        assertUsualInit(fileLibrary);
        assertNoMoreNatives();
    }

    @Test
    public void init_FailedToLoadLibrary() {
        // Setup
        ResourcesManager resources = mock(ResourcesManager.class);
        WindowSettings settings = new WindowSettings.Builder().size(200, 400).build();
        File fileLibrary = mock(File.class);

        when(resources.getFlatLibraryFile()).thenReturn(fileLibrary);

        doThrow(new RuntimeException()).when(FlatLibrary.class);
        FlatLibrary.load(fileLibrary);

        // Execution/Assertion
        try {
            Application.init(resources);

            fail("Exception expected");
        } catch (Exception e) {
            assertEquals("Failed to load Flat Library", e.getMessage());
        }

        verifyStatic(FlatLibrary.class);
        FlatLibrary.load(fileLibrary);

        assertNoMoreNatives();
    }

    @Test
    public void init_InvalidWindowCreation() {
        // Setup
        ResourcesManager resources = mock(ResourcesManager.class);
        WindowSettings settings = new WindowSettings.Builder().size(200, 400).build();
        File fileLibrary = mock(File.class);

        when(resources.getFlatLibraryFile()).thenReturn(fileLibrary);
        when(WL.Init()).thenReturn(false);

        // Execution/Assertion
        try {
            Application.init(resources);
            Application.launch(settings);

            fail("Exception expected");
        } catch (Exception e) {
            assertEquals("Invalid context creation", e.getMessage());
        }

        verifyStatic(FlatLibrary.class);
        FlatLibrary.load(fileLibrary);

        verifyStatic(WL.class);
        WL.Init();

        assertNoMoreNatives();
    }

    @Test
    public void launch() {
        // Setup
        ResourcesManager resources = mock(ResourcesManager.class);
        WindowSettings settings = new WindowSettings.Builder().size(200, 400).build();
        File fileLibrary = mock(File.class);

        Window window = mock(Window.class);
        when(window.isClosed()).thenReturn(false).thenReturn(true);
        when(Window.create(settings)).thenReturn(window);

        when(resources.getFlatLibraryFile()).thenReturn(fileLibrary);
        when(WL.Init()).thenReturn(true);

        // Execution
        Application.init(resources);
        Application.launch(settings);

        // Assertion
        verify(window).loop(anyFloat());

        assertUsualInit(fileLibrary);

        verifyStatic(WL.class);
        WL.HandleEvents(anyDouble());

        verifyStatic(WL.class);
        WL.Finish();

        assertNoMoreNatives();
    }

    @Test
    public void launch_EventHandling() {
        // Setup
        ResourcesManager resources = mock(ResourcesManager.class);
        WindowSettings settings = new WindowSettings.Builder().size(200, 400).build();
        File fileLibrary = mock(File.class);

        Window window = mock(Window.class);
        when(window.isClosed()).thenReturn(false).thenReturn(true);
        when(Window.create(settings)).thenReturn(window);

        when(resources.getFlatLibraryFile()).thenReturn(fileLibrary);
        when(WL.Init()).thenReturn(true);

        // Event Setup
        doAnswer(invocation -> {
            var captor1 = ArgumentCaptor.forClass(WLEnums.MouseButtonCallback.class);
            verifyStatic(WL.class);
            WL.SetMouseButtonCallback(captor1.capture());
            captor1.getValue().handle(0, 1, 1, 0);

            var captor2 = ArgumentCaptor.forClass(WLEnums.CursorPosCallback.class);
            verifyStatic(WL.class);
            WL.SetCursorPosCallback(captor2.capture());
            captor2.getValue().handle(0, 1, 1);

            var captor3 = ArgumentCaptor.forClass(WLEnums.ScrollCallback.class);
            verifyStatic(WL.class);
            WL.SetScrollCallback(captor3.capture());
            captor3.getValue().handle(0, 1, 1);

            var captor4 = ArgumentCaptor.forClass(WLEnums.DropCallback.class);
            verifyStatic(WL.class);
            WL.SetDropCallback(captor4.capture());
            captor4.getValue().handle(0, any());

            var captor5 = ArgumentCaptor.forClass(WLEnums.KeyCallback.class);
            verifyStatic(WL.class);
            WL.SetKeyCallback(captor5.capture());
            captor5.getValue().handle(0, 1, 1, 1, 1);

            var captor6 = ArgumentCaptor.forClass(WLEnums.CharModsCallback.class);
            verifyStatic(WL.class);
            WL.SetCharModsCallback(captor6.capture());
            captor6.getValue().handle(0, 1, 1);

            var captor7 = ArgumentCaptor.forClass(WLEnums.WindowCloseCallback.class);
            verifyStatic(WL.class);
            WL.SetWindowCloseCallback(captor7.capture());
            captor7.getValue().handle(0);

            verify(window).addEvent(any(), anyFloat(), anyFloat());
            verify(window, times(5)).addEvent(any());
            verify(window).onRequestClose(true);
            return null;
        }).when(WL.class);
        WL.HandleEvents(anyDouble());

        // Execution
        Application.init(resources);
        Application.launch(settings);

        // Assertion
        assertUsualInit(fileLibrary);

        verifyStatic(WL.class);
        WL.HandleEvents(anyDouble());

        verifyStatic(WL.class);
        WL.Finish();

        assertNoMoreNatives();
    }

    @Test
    public void launch_ScreenSizeEventHandling() {
        // Setup
        ResourcesManager resources = mock(ResourcesManager.class);
        WindowSettings settings = new WindowSettings.Builder().size(200, 400).build();
        File fileLibrary = mock(File.class);

        Window window = mock(Window.class);
        when(window.isStarted()).thenReturn(true);
        when(window.isClosed()).thenReturn(false).thenReturn(false).thenReturn(true);
        when(Window.create(settings)).thenReturn(window);

        when(resources.getFlatLibraryFile()).thenReturn(fileLibrary);
        when(WL.Init()).thenReturn(true);

        // Event Setup
        doAnswer(invocation -> {
            var captor = ArgumentCaptor.forClass(WLEnums.WindowSizeCallback.class);
            verifyStatic(WL.class);
            WL.SetWindowSizeCallback(captor.capture());
            captor.getValue().handle(0, 800, 600);

            verify(window).addEvent(any());
            return null;
        }).when(WL.class);
        WL.HandleEvents(anyDouble());

        // Execution
        Application.init(resources);
        Application.launch(settings);

        // Assertion
        assertUsualInit(fileLibrary);

        verifyStatic(WL.class);
        WL.Finish();

        verifyStatic(WL.class);
        WL.HandleEvents(anyDouble());

        verifyStatic(WL.class);
        WL.Finish();

        verify(window).addEvent(any());
        verify(window, times(2)).loop(anyFloat());

        assertNoMoreNatives();
    }

    @Test
    public void vsyncConfiguration() {
        // Setup
        ResourcesManager resources = mock(ResourcesManager.class);
        WindowSettings settings = new WindowSettings.Builder().size(200, 400).build();
        File fileLibrary = mock(File.class);

        Window window = mock(Window.class);
        int[] fakeWindowState = new int[]{0};
        doAnswer(a -> fakeWindowState[0]).when(window).getVsync();
        doAnswer(a -> fakeWindowState[0] = a.getArgument(0)).when(window).setVsync(1);

        when(window.isClosed()).thenReturn(false).thenReturn(false).thenReturn(true);
        when(window.isBufferInvalided()).thenReturn(true);
        when(Window.create(settings)).thenReturn(window);

        when(resources.getFlatLibraryFile()).thenReturn(fileLibrary);
        when(WL.Init()).thenReturn(true);

        // Execution
        Application.init(resources);
        Application.setVsync(1);
        Application.launch(settings);

        // Assertion
        verify(window, times(2)).loop(anyFloat());

        verifyStatic(WL.class);
        WL.SetVsync(anyInt());

        assertUsualInit(fileLibrary);

        verifyStatic(WL.class, times(2));
        WL.SwapBuffers(anyLong());

        verifyStatic(WL.class, times(2));
        WL.HandleEvents(anyDouble());

        verifyStatic(WL.class);
        WL.Finish();

        assertEquals("Unexpected Application Vsync", 1, Application.getVsync());

        assertNoMoreNatives();
    }

    @Test
    public void launch_WindowLoopException() {
        // Setup
        ResourcesManager resources = mock(ResourcesManager.class);
        WindowSettings settings = new WindowSettings.Builder().size(200, 400).build();
        File fileLibrary = mock(File.class);

        Window window = mock(Window.class);
        when(window.isClosed()).thenReturn(false).thenReturn(true);
        when(Window.create(settings)).thenReturn(window);

        when(resources.getFlatLibraryFile()).thenReturn(fileLibrary);
        when(WL.Init()).thenReturn(true);
        doThrow(new RuntimeException("Problem")).when(window).loop(anyFloat());

        // Execution
        Application.init(resources);
        Application.launch(settings);

        // Assertion
        verify(window).loop(anyFloat());
        verify(window).close();

        assertUsualInit(fileLibrary);

        verifyStatic(WL.class);
        WL.HandleEvents(anyDouble());

        verifyStatic(WL.class);
        WL.Finish();

        assertNoMoreNatives();
    }

    @Test
    public void runOnContextSync() {
        // Setup
        ResourcesManager resources = mock(ResourcesManager.class);
        WindowSettings settings = new WindowSettings.Builder().size(200, 400).build();
        File fileLibrary = mock(File.class);

        Window window = mock(Window.class);
        when(window.isClosed()).thenReturn(false).thenReturn(true);
        when(Window.create(settings)).thenReturn(window);

        when(resources.getFlatLibraryFile()).thenReturn(fileLibrary);
        when(WL.Init()).thenReturn(true);

        Runnable task = mock(Runnable.class);
        doAnswer(a -> {
            Application.runOnContextSync(task);
            return false;
        }).when(window).loop(anyFloat());

        // Execution
        Application.init(resources);
        Application.launch(settings);

        // Assertion
        verify(window).loop(anyFloat());
        verify(task).run();

        assertUsualInit(fileLibrary);

        verifyStatic(WL.class);
        WL.HandleEvents(anyDouble());

        verifyStatic(WL.class);
        WL.Finish();

        assertNoMoreNatives();
    }

    private void assertUsualInit(File fileLibrary) {
        verifyStatic(FlatLibrary.class);
        FlatLibrary.load(fileLibrary);

        verifyStatic(WL.class);
        WL.Init();

        verifyStatic(WL.class);
        WL.SetErrorCallback(any());

        verifyStatic(WL.class);
        WL.SetMouseButtonCallback(any());

        verifyStatic(WL.class);
        WL.SetCursorPosCallback(any());

        verifyStatic(WL.class);
        WL.SetScrollCallback(any());

        verifyStatic(WL.class);
        WL.SetDropCallback(any());

        verifyStatic(WL.class);
        WL.SetKeyCallback(any());

        verifyStatic(WL.class);
        WL.SetCharModsCallback(any());

        verifyStatic(WL.class);
        WL.SetWindowSizeCallback(any());

        verifyStatic(WL.class);
        WL.SetWindowCloseCallback(any());
    }

    private void assertNoMoreNatives() {
        verifyNoMoreInteractions(FlatLibrary.class);
        verifyNoMoreInteractions(WL.class);
        verifyNoMoreInteractions(GL.class);
        verifyNoMoreInteractions(SVG.class);
    }
}