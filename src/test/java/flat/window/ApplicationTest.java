package flat.window;

import flat.backend.*;
import flat.graphics.context.Context;
import flat.graphics.emojis.EmojiManager;
import flat.resources.ResourceStream;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;
import static org.powermock.api.mockito.PowerMockito.doThrow;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FlatLibrary.class, WL.class, GL.class, SVG.class, Context.class, Activity.class, Window.class, EmojiManager.class})
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
        PowerMockito.mockStatic(EmojiManager.class);
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
            Application.setup(settings);
            Application.launch();

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
        Application.setup(settings);
        Application.launch();

        // Assertion
        verify(window).loop(anyFloat());

        assertUsualInit(fileLibrary);
        assertQualityInit();

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
        Application.setup(settings);
        Application.launch();

        // Assertion
        assertUsualInit(fileLibrary);
        assertQualityInit();

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
        Application.setup(settings);
        Application.launch();

        // Assertion
        assertUsualInit(fileLibrary);
        assertQualityInit();

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
        Application.setup(settings);
        Application.launch();

        // Assertion
        verify(window, times(2)).loop(anyFloat());

        verifyStatic(WL.class);
        WL.SetVsync(anyInt());

        assertUsualInit(fileLibrary);
        assertQualityInit();

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
        Application.setup(settings);
        Application.launch();

        // Assertion
        verify(window).loop(anyFloat());
        verify(window).close();

        assertUsualInit(fileLibrary);
        assertQualityInit();

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
        Application.setup(settings);
        Application.launch();

        // Assertion
        verify(window).loop(anyFloat());
        verify(task).run();

        assertUsualInit(fileLibrary);
        assertQualityInit();

        verifyStatic(WL.class);
        WL.HandleEvents(anyDouble());

        verifyStatic(WL.class);
        WL.Finish();

        assertNoMoreNatives();
    }

    @Test
    public void launch_EmojiCreated() {
        // Setup
        ResourcesManager resources = mock(ResourcesManager.class);
        WindowSettings settings = new WindowSettings.Builder().size(200, 400).build();
        File fileLibrary = mock(File.class);

        Window window = mock(Window.class);
        when(window.isClosed()).thenReturn(false).thenReturn(true);
        when(Window.create(settings)).thenReturn(window);

        when(resources.getFlatLibraryFile()).thenReturn(fileLibrary);
        when(WL.Init()).thenReturn(true);

        when(GL.GetMaxTextureSize()).thenReturn(1024);
        doAnswer((a) -> null).when(EmojiManager.class);
        EmojiManager.load(any());

        // Execution
        Application.init(resources);
        Application.setup(settings);
        Application.launch();

        // Assertion
        verify(window).loop(anyFloat());

        verifyStatic(EmojiManager.class);
        EmojiManager.load(new ResourceStream("/default/emojis/emojis-1024.png"));

        assertUsualInit(fileLibrary);
        assertQualityInit();

        verifyStatic(WL.class);
        WL.HandleEvents(anyDouble());

        verifyStatic(WL.class);
        WL.Finish();

        assertNoMoreNatives();
    }

    @Test
    public void launch_EmojiNotCreated() {
        // Setup
        ResourcesManager resources = mock(ResourcesManager.class);
        WindowSettings settings = new WindowSettings.Builder().size(200, 400).build();
        File fileLibrary = mock(File.class);

        Window window = mock(Window.class);
        when(window.isClosed()).thenReturn(false).thenReturn(true);
        when(Window.create(settings)).thenReturn(window);

        when(resources.getFlatLibraryFile()).thenReturn(fileLibrary);
        when(WL.Init()).thenReturn(true);

        when(GL.GetMaxTextureSize()).thenReturn(512);
        doAnswer((a) -> null).when(EmojiManager.class);
        EmojiManager.load(any());

        // Execution
        Application.init(resources);
        Application.setup(settings);
        Application.launch();

        // Assertion
        verify(window).loop(anyFloat());

        verifyStatic(EmojiManager.class, times(0));
        EmojiManager.load(any());

        assertUsualInit(fileLibrary);
        assertQualityInit();

        verifyStatic(WL.class);
        WL.HandleEvents(anyDouble());

        verifyStatic(WL.class);
        WL.Finish();

        assertNoMoreNatives();
    }

    private void assertQualityInit() {
        verifyStatic(GL.class);
        GL.GetMaxTextureSize();

        verifyStatic(GL.class);
        GL.GetMaxElementsVertices();

        verifyStatic(GL.class);
        GL.GetMaxElementsIndices();

        verifyStatic(GL.class);
        GL.GetMaxUniformBlockSize();
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
/*

#version 330 core
out vec4 FragColor;
layout (std140) uniform Paint {
    vec4 data;
    mat3 colorMat;
    mat3 imageMat;
    vec4 shape;
    vec4 extra;
    vec4 stops[4];
    vec4 colors[16];
};
uniform int stc;
uniform int dbg;
uniform sampler2D tex;
uniform sampler2D fnt;
uniform sampler2D emj;
in vec2 oPos;
in vec2 oTex;
in float oEmj;

float roundrect(vec2 pt, vec2 ext, float rad) {
	 vec2 ext2 = ext - vec2(rad,rad);
	 vec2 d = abs(pt) - ext2;
	 return min(max(d.x, d.y), 0.0) + length(max(d, 0.0)) - rad;
}

float focuscircle(vec2 coord, vec2 focus) {
    float gradLength = 1.0;
    vec2 diff = focus;
    vec2 rayDir = normalize(coord - focus);
    float a = dot(rayDir, rayDir);
    float b = 2.0 * dot(rayDir, diff);
    float c = dot(diff, diff) - 1;
    float disc = b * b - 4.0 * a * c;
    if (disc >= 0.0) {
        float t = (-b + sqrt(abs(disc))) / (2.0 * a);
        vec2 projection = focus + rayDir * t;
        gradLength = distance(projection, focus);
    }
    return distance(coord, focus) / gradLength;
}

float expin(float a, float power) {
	 return 1 - pow(1 - a, power);
}

void main() {
    if (stc == 1) {
        FragColor = vec4(1);
    } else if (oEmj > 0 && dbg != 1) {
        vec4 emjCol = texture(emj, oTex);
        emjCol.a *= colors[0].a;
        FragColor = emjCol;
    } else {
        vec4 color = colors[0];
        if (data[3] > 0) {
            vec2 cPt = (colorMat * vec3(oPos, 1.0)).xy;
            float t;
            if (extra[2] == 0) {
                t = (roundrect(cPt, shape.xy, shape.z) + shape.w * 0.5) / shape.w;
            } else {
                t = focuscircle(cPt / (shape.z * 2.0), extra.xy);
            }
            if (data[2] == 0) {
                t = clamp(t, 0.0, 1.0);
            } else if (data[2] == 1) {
                t = t - floor(t);
            } else if (data[2] == 2) {
                t = (int(t) % 2 == 0) ? t - floor(t) : 1 - (t - floor(t));
            } else {
                t = expin(clamp(t, 0.0, 1.0), 2);
            }
            color = mix(color, colors[1], clamp((t - stops[0].x) / (stops[0].y - stops[0].x), 0.0, 1.0));
            if(data[3] > 1) color = mix(color, colors[2], clamp((t - stops[0].y) / (stops[0].z - stops[0].y), 0.0, 1.0));
            if(data[3] > 2) color = mix(color, colors[3], clamp((t - stops[0].z) / (stops[0].w - stops[0].z), 0.0, 1.0));
            if(data[3] > 3) color = mix(color, colors[4], clamp((t - stops[0].w) / (stops[1].x - stops[0].w), 0.0, 1.0));
            if(data[3] > 4) color = mix(color, colors[5], clamp((t - stops[1].x) / (stops[1].y - stops[1].x), 0.0, 1.0));
            if(data[3] > 5) color = mix(color, colors[6], clamp((t - stops[1].y) / (stops[1].z - stops[1].y), 0.0, 1.0));
            if(data[3] > 6) color = mix(color, colors[7], clamp((t - stops[1].z) / (stops[1].w - stops[1].z), 0.0, 1.0));
            if(data[3] > 7) color = mix(color, colors[8], clamp((t - stops[1].w) / (stops[2].x - stops[1].w), 0.0, 1.0));
            if(data[3] > 8) color = mix(color, colors[9], clamp((t - stops[2].x) / (stops[2].y - stops[2].x), 0.0, 1.0));
            if(data[3] > 9) color = mix(color, colors[10], clamp((t - stops[2].y) / (stops[2].z - stops[2].y), 0.0, 1.0));
            if(data[3] >10) color = mix(color, colors[11], clamp((t - stops[2].z) / (stops[2].w - stops[2].z), 0.0, 1.0));
            if(data[3] >11) color = mix(color, colors[12], clamp((t - stops[2].w) / (stops[3].x - stops[2].w), 0.0, 1.0));
            if(data[3] >12) color = mix(color, colors[13], clamp((t - stops[3].x) / (stops[3].y - stops[3].x), 0.0, 1.0));
            if(data[3] >13) color = mix(color, colors[14], clamp((t - stops[3].y) / (stops[3].z - stops[3].y), 0.0, 1.0));
            if(data[3] >14) color = mix(color, colors[15], clamp((t - stops[3].z) / (stops[3].w - stops[3].z), 0.0, 1.0));
        }
        if (data[0] == 1 || data[0] == 3) {
            ivec2 sz = textureSize(tex, 0);
            vec2 tPt = (imageMat * vec3(oPos, 1.0)).xy;
            if (data[2] == 0) {
                tPt = clamp(tPt, vec2(0.0), vec2(1.0));
            } else if (data[2] == 1) {
                tPt = tPt - floor(tPt);
            } else if (data[2] == 2) {
                tPt = vec2((int(tPt.x) % 2 == 0) ? tPt.x - floor(tPt.x) : 1 - (tPt.x - floor(tPt.x)),
                           (int(tPt.y) % 2 == 0) ? tPt.y - floor(tPt.y) : 1 - (tPt.y - floor(tPt.y)));
            }
            color *= texture(tex, tPt);
        }
        float a = color.a;
        if (data[0] > 1) {
            ivec2 sz = textureSize(fnt, 0);
            float dist = texture(fnt, oTex / sz).r;
            if (dbg == 1) {
                a = 1;
            } else if (extra[3] >= 0) {
                float screenSpaceScale = fwidth(oTex).x * 0.05 + extra[3] * 0.5;
                float aliasing = smoothstep(0.5 - screenSpaceScale, 0.5 + screenSpaceScale, dist);
                a = a * aliasing;
            } else {
                a = a * dist;
            }
        }
        FragColor = vec4(color.rgb * a, a);
    }
}

*/