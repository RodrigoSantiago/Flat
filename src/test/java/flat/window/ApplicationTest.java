package flat.window;

import flat.backend.*;
import flat.graphics.context.Context;
import flat.resources.ResourcesManager;
import org.junit.After;
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
@PrepareForTest({FlatLibrary.class, WL.class, GL.class, SVG.class, Context.class})
public class ApplicationTest {

    @Before
    public void before() {
        PowerMockito.mockStatic(FlatLibrary.class);
        PowerMockito.mockStatic(WL.class);
        PowerMockito.mockStatic(GL.class);
        PowerMockito.mockStatic(SVG.class);
        PowerMockito.mockStatic(Context.class);
        when(Context.create(any(), anyLong(), anyLong())).thenReturn(null);
    }

    @After
    public void after() {

    }

    @Test
    public void init() {
        // Setup
        ResourcesManager resources = mock(ResourcesManager.class);
        Application.Settings settings = mock(Application.Settings.class);
        File fileLibrary = mock(File.class);
        Window window = mock(Window.class);

        when(resources.getFlatLibraryFile()).thenReturn(fileLibrary);
        when(settings.createResources()).thenReturn(resources);
        when(settings.createWindow()).then(invocationOnMock -> null);
        when(WL.Init()).thenReturn(true);

        // Execution
        Application.launch(settings);

        // Assertion
        assertUsualInit(fileLibrary);
        assertNoMoreNatives();
    }

    @Test
    public void init_FailedToLoadLibrary() {
        // Setup
        ResourcesManager resources = mock(ResourcesManager.class);
        Application.Settings settings = mock(Application.Settings.class);
        File fileLibrary = mock(File.class);

        when(resources.getFlatLibraryFile()).thenReturn(fileLibrary);
        when(settings.createResources()).thenReturn(resources);
        when(settings.createWindow()).then(invocationOnMock -> null);
        doThrow(new RuntimeException()).when(FlatLibrary.class);
        FlatLibrary.load(fileLibrary);

        // Execution/Assertion
        try {
            Application.launch(settings);

            fail("Exception expected");
        } catch (Exception e) {
            assertEquals("Failed to load Flat Library", e.getMessage());
        }

        verifyStatic(FlatLibrary.class);
        FlatLibrary.load(fileLibrary);

        assertNoMoreNatives();
    }

    @Test
    public void init_InvalidContextCreation() {
        // Setup
        ResourcesManager resources = mock(ResourcesManager.class);
        Application.Settings settings = mock(Application.Settings.class);
        File fileLibrary = mock(File.class);

        when(resources.getFlatLibraryFile()).thenReturn(fileLibrary);
        when(settings.createResources()).thenReturn(resources);
        when(settings.createWindow()).then(invocationOnMock -> null);
        when(WL.Init()).thenReturn(false);

        // Execution/Assertion
        try {
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
        Application.Settings settings = mock(Application.Settings.class);
        File fileLibrary = mock(File.class);
        Window window = mock(Window.class);

        when(resources.getFlatLibraryFile()).thenReturn(fileLibrary);
        when(settings.createResources()).thenReturn(resources);
        when(settings.createWindow()).then(invocationOnMock -> {
            Application.createContext(window);
            return window;
        });
        when(window.isClosed()).thenReturn(true);
        when(WL.Init()).thenReturn(true);

        // Execution
        Application.launch(settings);

        // Assertion
        verify(window).loop(anyFloat());

        assertUsualInit(fileLibrary);

        verifyStatic(WL.class);
        WL.WindowAssign(anyLong());

        verifyStatic(WL.class);
        WL.HandleEvents(anyDouble());

        assertNoMoreNatives();
    }

    @Test
    public void launch_EventHandling() {
        // Setup
        ResourcesManager resources = mock(ResourcesManager.class);
        Application.Settings settings = mock(Application.Settings.class);
        File fileLibrary = mock(File.class);
        Window window = mock(Window.class);

        when(resources.getFlatLibraryFile()).thenReturn(fileLibrary);
        when(settings.createResources()).thenReturn(resources);
        when(settings.createWindow()).then(invocationOnMock -> {
            Application.createContext(window);
            return window;
        });
        when(window.isClosed()).thenReturn(true);
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
        Application.launch(settings);

        // Assertion
        assertUsualInit(fileLibrary);

        verifyStatic(WL.class);
        WL.WindowAssign(anyLong());

        verifyStatic(WL.class);
        WL.HandleEvents(anyDouble());

        assertNoMoreNatives();
    }

    @Test
    public void launch_ScreenSizeEventHandling() {
        // Setup
        ResourcesManager resources = mock(ResourcesManager.class);
        Application.Settings settings = mock(Application.Settings.class);
        File fileLibrary = mock(File.class);
        Window window = mock(Window.class);
        when(window.isStarted()).thenReturn(true);

        when(resources.getFlatLibraryFile()).thenReturn(fileLibrary);
        when(settings.createResources()).thenReturn(resources);
        when(settings.createWindow()).then(invocationOnMock -> {
            Application.createContext(window);
            return window;
        });
        when(window.isClosed()).thenReturn(false).thenReturn(true);
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
        Application.launch(settings);

        // Assertion
        assertUsualInit(fileLibrary);

        verifyStatic(WL.class);
        WL.WindowAssign(anyLong());

        verifyStatic(WL.class);
        WL.Finish();

        verifyStatic(WL.class);
        WL.HandleEvents(anyDouble());

        verify(window).addEvent(any());
        verify(window, times(2)).loop(anyFloat());

        assertNoMoreNatives();
    }

    @Test
    public void vsyncConfiguration() {
        // Setup
        ResourcesManager resources = mock(ResourcesManager.class);
        Application.Settings settings = mock(Application.Settings.class);
        File fileLibrary = mock(File.class);
        Window window = mock(Window.class);

        when(resources.getFlatLibraryFile()).thenReturn(fileLibrary);
        when(settings.createResources()).thenReturn(resources);
        when(settings.getVsync()).thenReturn(1);
        when(settings.createWindow()).then(invocationOnMock -> {
            Application.createContext(window);
            return window;
        });
        when(window.loop(anyFloat())).then(invocation -> true);
        when(window.isClosed()).thenReturn(true);
        when(WL.Init()).thenReturn(true);

        // Execution
        Application.launch(settings);

        // Assertion
        verify(window).loop(anyFloat());

        assertUsualInit(fileLibrary);

        verifyStatic(WL.class);
        WL.WindowAssign(anyLong());

        verifyStatic(WL.class);
        WL.HandleEvents(anyDouble());

        assertEquals("Unexpected Application Vsync", 1, Application.getVsync());

        assertNoMoreNatives();
    }

    @Test
    public void launch_WindowLoopException() {
        // Setup
        ResourcesManager resources = mock(ResourcesManager.class);
        Application.Settings settings = mock(Application.Settings.class);
        File fileLibrary = mock(File.class);
        Window window = mock(Window.class);

        when(resources.getFlatLibraryFile()).thenReturn(fileLibrary);
        when(settings.createResources()).thenReturn(resources);
        when(settings.createWindow()).then(invocationOnMock -> {
            Application.createContext(window);
            return window;
        });
        when(window.isClosed()).thenReturn(true);
        when(WL.Init()).thenReturn(true);
        when(window.loop(anyFloat())).thenThrow(new RuntimeException("Problem"));

        // Execution
        try {
            Application.launch(settings);

            fail("Exception expected");
        } catch (Exception e) {
            assertEquals("Problem", e.getMessage());
        }

        // Assertion
        verify(window).loop(anyFloat());

        assertUsualInit(fileLibrary);

        verifyStatic(WL.class);
        WL.WindowAssign(anyLong());

        assertNoMoreNatives();
    }

    @Test
    public void runVsync() {
        // Setup
        ResourcesManager resources = mock(ResourcesManager.class);
        Application.Settings settings = mock(Application.Settings.class);
        File fileLibrary = mock(File.class);
        Window window = mock(Window.class);

        when(resources.getFlatLibraryFile()).thenReturn(fileLibrary);
        when(settings.createResources()).thenReturn(resources);
        when(settings.getVsync()).thenReturn(1);
        when(settings.createWindow()).then(invocationOnMock -> {
            Application.createContext(window);
            return window;
        });
        when(window.loop(anyFloat())).then(invocation -> true);
        when(window.isClosed()).thenReturn(true);
        when(WL.Init()).thenReturn(true);

        // Execution
        Runnable task = mock(Runnable.class);
        Application.runVsync(task);
        Application.launch(settings);

        // Assertion
        verify(window).loop(anyFloat());
        verify(task).run();

        assertUsualInit(fileLibrary);

        verifyStatic(WL.class);
        WL.WindowAssign(anyLong());

        verifyStatic(WL.class);
        WL.HandleEvents(anyDouble());

        assertEquals("Unexpected Application Vsync", 1, Application.getVsync());

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

        verifyStatic(WL.class);
        WL.SetVsync(anyInt());

        verifyStatic(WL.class);
        WL.Finish();
    }

    private void assertNoMoreNatives() {
        verifyNoMoreInteractions(FlatLibrary.class);
        verifyNoMoreInteractions(WL.class);
        verifyNoMoreInteractions(GL.class);
        verifyNoMoreInteractions(SVG.class);
    }
}