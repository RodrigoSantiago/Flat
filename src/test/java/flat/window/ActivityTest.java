package flat.window;

import flat.animations.Animation;
import flat.events.KeyCode;
import flat.events.KeyEvent;
import flat.graphics.Graphics;
import flat.graphics.context.Context;
import flat.math.Vector2;
import flat.resources.ResourceStream;
import flat.uxml.*;
import flat.widget.Scene;
import flat.widget.Widget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UXNode.class, UXSheet.class})
public class ActivityTest {

    Window window;
    Context context;

    @Before
    public void before() {
        window = mock(Window.class);
        context = mock(Context.class);
        when(window.getContext()).thenReturn(context);
        when(window.getDpi()).thenReturn(160f);
        when(window.getWidth()).thenReturn(200);
        when(window.getHeight()).thenReturn(100);
        when(window.getClientWidth()).thenReturn(200);
        when(window.getClientHeight()).thenReturn(100);

        when(context.getWindow()).thenReturn(window);
        when(context.getWidth()).thenReturn(200);
        when(context.getHeight()).thenReturn(100);
    }

    @Test
    public void constructor() {
        WindowSettings settings = new WindowSettings.Builder().size(200, 100).build();

        Activity activity = Activity.create(window, settings);

        assertEquals(context, activity.getContext());
        assertEquals(window, activity.getWindow());
        assertEquals(200f, activity.getWidth(), 0.00001f);
        assertEquals(100f, activity.getHeight(), 0.00001f);
    }

    @Test
    public void setTheme() {
        UXTheme theme1 = mock(UXTheme.class);
        UXTheme theme2 = mock(UXTheme.class);
        when(theme1.createInstance(anyFloat(), anyFloat(), any(), any())).thenReturn(theme1);
        when(theme2.createInstance(anyFloat(), anyFloat(), any(), any())).thenReturn(theme2);

        WindowSettings settings = new WindowSettings.Builder().size(200, 100).build();
        Activity activity = Activity.create(window, settings);

        assertNull(activity.getTheme());
        assertNull(activity.getScene());
        assertNull(activity.getController());
        activity.initialize();

        assertNull(activity.getTheme());
        assertNotNull(activity.getScene());
        assertNull(activity.getController());

        activity.setTheme(theme1);
        assertEquals(theme1, activity.getTheme());

        activity.show();
        assertEquals(theme1, activity.getTheme());

        activity.setTheme(theme2);
        assertEquals(theme2, activity.getTheme());

        activity.refreshScene();
        assertEquals(theme2, activity.getTheme());
    }

    @Test
    public void setThemeResource() {
        UXTheme theme1 = mock(UXTheme.class);
        UXTheme theme2 = mock(UXTheme.class);
        when(theme1.createInstance(anyFloat(), anyFloat(), any(), any())).thenReturn(theme1);
        when(theme2.createInstance(anyFloat(), anyFloat(), any(), any())).thenReturn(theme2);
        ResourceStream stream1 = mock(ResourceStream.class);
        ResourceStream stream2 = mock(ResourceStream.class);

        UXSheet sheet1 = mock(UXSheet.class);
        when(sheet1.instance()).thenReturn(theme1);
        when(sheet1.instance(anyFloat(), anyFloat(), any(), any())).thenReturn(theme1);
        when(stream1.getCache()).thenReturn(sheet1);

        UXSheet sheet2 = mock(UXSheet.class);
        when(sheet2.instance()).thenReturn(theme2);
        when(sheet2.instance(anyFloat(), anyFloat(), any(), any())).thenReturn(theme2);
        when(stream2.getCache()).thenReturn(sheet2);

        WindowSettings settings = new WindowSettings.Builder().size(200, 100).theme(stream1).build();
        Activity activity = Activity.create(window, settings);

        assertNull(activity.getTheme());
        assertNull(activity.getScene());
        assertNull(activity.getController());
        activity.initialize();

        assertEquals(theme1, activity.getTheme());
        assertNotNull(activity.getScene());
        assertNull(activity.getController());

        activity.show();
        assertEquals(theme1, activity.getTheme());

        activity.setTheme(stream2);
        assertEquals(theme2, activity.getTheme());

        activity.refreshScene();
        assertEquals(theme2, activity.getTheme());
    }

    @Test
    public void setScene() {
        Scene scene1 = mock(Scene.class);
        when(scene1.getActivityScene()).thenReturn(mock(ActivityScene.class));
        Scene scene2 = mock(Scene.class);
        when(scene2.getActivityScene()).thenReturn(mock(ActivityScene.class));

        WindowSettings settings = new WindowSettings.Builder().size(200, 100).layout(scene1).build();
        Activity activity = Activity.create(window, settings);

        assertNull(activity.getTheme());
        assertNull(activity.getScene());
        assertNull(activity.getController());
        activity.initialize();

        assertNull(activity.getTheme());
        assertEquals(scene1, activity.getScene());
        assertNull(activity.getController());

        activity.show();
        assertEquals(scene1, activity.getScene());

        activity.setLayoutBuilder(scene2, null);
        assertEquals(scene1, activity.getScene());

        activity.refreshScene();
        assertEquals(scene2, activity.getScene());
    }

    @Test
    public void setSceneResource() {
        Scene scene1 = mock(Scene.class);
        when(scene1.getActivityScene()).thenReturn(mock(ActivityScene.class));
        Scene scene2 = mock(Scene.class);
        when(scene2.getActivityScene()).thenReturn(mock(ActivityScene.class));

        UXBuilder builder1 = mock(UXBuilder.class);
        when(builder1.buildScene(any())).thenReturn(scene1);
        UXBuilder builder2 = mock(UXBuilder.class);
        when(builder2.buildScene(any())).thenReturn(scene2);

        UXNode root1 = mock(UXNode.class);
        when(root1.instance(any())).thenReturn(builder1);
        UXNode root2 = mock(UXNode.class);
        when(root2.instance(any())).thenReturn(builder2);

        ResourceStream stream1 = mock(ResourceStream.class);
        when(stream1.getCache()).thenReturn(root1);
        ResourceStream stream2 = mock(ResourceStream.class);
        when(stream2.getCache()).thenReturn(root2);

        WindowSettings settings = new WindowSettings.Builder().size(200, 100).layout(stream1).build();
        Activity activity = Activity.create(window, settings);

        assertNull(activity.getTheme());
        assertNull(activity.getScene());
        assertNull(activity.getController());
        activity.initialize();

        assertNull(activity.getTheme());
        assertEquals(scene1, activity.getScene());
        assertNull(activity.getController());

        activity.show();
        assertEquals(scene1, activity.getScene());

        activity.setLayoutBuilder(stream2, null);
        assertEquals(scene1, activity.getScene());

        activity.refreshScene();
        assertEquals(scene2, activity.getScene());
    }

    @Test
    public void performLayout() {
        UXTheme theme = mock(UXTheme.class);
        Scene scene = mock(Scene.class);
        Widget child = mock(Widget.class);
        when(scene.getActivityScene()).thenReturn(mock(ActivityScene.class));
        mockLocalToScreen(scene);
        mockLocalToScreen(child);

        when(child.getLayoutWidth()).thenReturn(20f);
        when(child.getLayoutHeight()).thenReturn(10f);

        WindowSettings settings = new WindowSettings.Builder().size(200, 100).layout(scene).theme(theme).build();
        Activity activity = Activity.create(window, settings);
        when(scene.getActivity()).thenReturn(activity);
        when(child.getActivity()).thenReturn(activity);

        activity.initialize();

        assertEquals(theme, activity.getTheme());
        assertEquals(scene, activity.getScene());

        activity.show();
        verify(scene).onMeasure();
        verify(scene).onLayout(200f, 100f);

        assertEquals(scene, activity.getScene());

        activity.layout(200, 100);
        verify(scene, times(1)).onMeasure();
        verify(scene, times(1)).onLayout(200f, 100f);

        activity.invalidateWidget(scene, true);
        activity.layout(100, 100);
        verify(scene, times(2)).onMeasure();
        verify(scene, times(1)).onLayout(100f, 100f);

        activity.invalidateWidget(child, true);
        activity.layout(100, 100);
        verify(scene, times(2)).onMeasure();
        verify(scene, times(1)).onLayout(100f, 100f);
        verify(child, times(1)).onMeasure();
        verify(child, times(1)).onLayout(20f, 10f);
    }

    @Test
    public void performDraw() {
        Graphics graphics = mock(Graphics.class);
        UXTheme theme = mock(UXTheme.class);
        Scene scene = mock(Scene.class);
        when(scene.getActivityScene()).thenReturn(mock(ActivityScene.class));
        mockLocalToScreen(scene);

        WindowSettings settings = new WindowSettings.Builder().size(200, 100).theme(theme).layout(scene).build();
        Activity activity = Activity.create(window, settings);
        when(scene.getActivity()).thenReturn(activity);

        activity.initialize();
        assertEquals(theme, activity.getTheme());
        assertEquals(scene, activity.getScene());

        activity.show();

        activity.layout(200, 100);
        activity.draw(graphics);
        verify(graphics, times(1)).clear(0x0, 1, 0);
        verify(scene, times(1)).onDraw(graphics);

        activity.layout(200, 100);
        activity.draw(graphics);
        verify(graphics, times(1)).clear(0x0, 1, 0);
        verify(scene, times(1)).onDraw(graphics);

        activity.repaint();

        activity.layout(200, 100);
        activity.draw(graphics);
        verify(graphics, times(2)).clear(0x0, 1, 0);
        verify(scene, times(2)).onDraw(graphics);
    }

    @Test
    public void performAnimations() {
        UXTheme theme = mock(UXTheme.class);
        Animation animation = mock(Animation.class);
        Scene scene = mock(Scene.class);
        when(scene.getActivityScene()).thenReturn(mock(ActivityScene.class));
        when(animation.isPlaying()).thenReturn(true);
        mockLocalToScreen(scene);

        WindowSettings settings = new WindowSettings.Builder().size(200, 100).theme(theme).layout(scene).build();
        Activity activity = Activity.create(window, settings);
        when(scene.getActivity()).thenReturn(activity);
        when(animation.getSource()).thenReturn(activity);

        activity.initialize();
        assertEquals(theme, activity.getTheme());
        assertEquals(scene, activity.getScene());

        activity.show();

        activity.addAnimation(animation);
        boolean animate1 = activity.animate(1f);
        activity.removeAnimation(animation);
        boolean animate2 = activity.animate(1f);
        verify(animation).handle(1f);
        assertTrue(animate1);
        assertFalse(animate2);

        // Play to Stop
        Animation animation2 = mock(Animation.class);
        when(animation2.getSource()).thenReturn(activity);
        when(animation2.isPlaying()).thenReturn(true).thenReturn(false);
        activity.addAnimation(animation2);
        boolean animate3 = activity.animate(1f);
        assertTrue(animate3);
        boolean animate4 = activity.animate(1f);
        assertFalse(animate4);
        verify(animation2).handle(1f);

        // Invalid source
        Animation animation3 = mock(Animation.class);
        when(animation3.getSource()).thenReturn(null);
        activity.addAnimation(animation3);
        boolean animate5 = activity.animate(1f);
        assertFalse(animate5);
        verify(animation3, times(0)).handle(1f);
    }

    @Test
    public void findById() {
        String findId = "findId";
        UXTheme theme = mock(UXTheme.class);
        Scene scene = mock(Scene.class);
        when(scene.getActivityScene()).thenReturn(mock(ActivityScene.class));
        Widget widget = mock(Widget.class);
        when(scene.findById("findId")).thenReturn(widget);
        mockLocalToScreen(scene);
        mockLocalToScreen(widget);

        WindowSettings settings = new WindowSettings.Builder().size(200, 100).theme(theme).layout(scene).build();
        Activity activity = Activity.create(window, settings);
        when(scene.getActivity()).thenReturn(activity);

        activity.initialize();
        assertEquals(theme, activity.getTheme());
        assertEquals(scene, activity.getScene());

        activity.show();

        Widget found = activity.findById(findId);
        assertEquals(widget, found);
        verify(scene).findById(findId);
    }

    @Test
    public void findByPosition() {
        UXTheme theme = mock(UXTheme.class);
        Scene scene = mock(Scene.class);
        when(scene.getActivityScene()).thenReturn(mock(ActivityScene.class));
        Widget widget = mock(Widget.class);
        when(scene.findByPosition(10, 20, true)).thenReturn(widget);
        mockLocalToScreen(scene);
        mockLocalToScreen(widget);

        WindowSettings settings = new WindowSettings.Builder().size(200, 100).theme(theme).layout(scene).build();
        Activity activity = Activity.create(window, settings);
        when(scene.getActivity()).thenReturn(activity);

        activity.initialize();
        assertEquals(theme, activity.getTheme());
        assertEquals(scene, activity.getScene());

        activity.show();

        Widget found = activity.findByPosition(10, 20, true);
        assertEquals(widget, found);
        verify(scene).findByPosition(10, 20, true);
    }

    @Test
    public void hide() {
        Controller controller = mock(Controller.class);
        ControllerFactory factory = mock(ControllerFactory.class);
        when(factory.build()).thenReturn(controller);

        UXTheme theme = mock(UXTheme.class);
        Scene scene = mock(Scene.class);
        when(scene.getActivityScene()).thenReturn(mock(ActivityScene.class));
        mockLocalToScreen(scene);

        WindowSettings settings = new WindowSettings.Builder().size(200, 100)
                .theme(theme).layout(scene).controller(factory).build();
        Activity activity = Activity.create(window, settings);
        when(scene.getActivity()).thenReturn(activity);

        activity.initialize();
        assertEquals(theme, activity.getTheme());
        assertEquals(scene, activity.getScene());

        activity.show();
        activity.close();

        verify(controller).onShow();
        verify(controller).onHide();
    }

    private void mockLocalToScreen(Widget widget) {
        when(widget.localToScreen(anyFloat(), anyFloat())).thenAnswer(a -> new Vector2(a.getArgument(0), a.getArgument(1)));
    }

    @Test
    public void onTabPressedChangeFocus() {
        String sceneId = "sceneId";
        String focus1Id = "focus1Id";
        String focus2Id = "focis2Id";

        UXTheme theme = mock(UXTheme.class);
        Scene scene = mock(Scene.class);
        when(scene.getActivityScene()).thenReturn(mock(ActivityScene.class));
        Widget focus1 = mock(Widget.class);
        Widget focus2 = mock(Widget.class);
        mockLocalToScreen(scene);
        mockLocalToScreen(focus1);
        mockLocalToScreen(focus2);

        when(scene.findById(sceneId)).thenReturn(scene);
        when(scene.findById(focus1Id)).thenReturn(focus1);
        when(scene.findById(focus2Id)).thenReturn(focus2);
        when(focus1.findById(focus1Id)).thenReturn(focus1);
        when(focus1.findById(focus2Id)).thenReturn(focus2);
        when(focus2.findById(focus1Id)).thenReturn(focus1);
        when(focus2.findById(focus2Id)).thenReturn(focus2);

        when(scene.getNextFocusId()).thenReturn(focus1Id);
        when(focus1.getNextFocusId()).thenReturn(focus2Id);
        when(focus2.getNextFocusId()).thenReturn(sceneId);

        when(scene.isFocusable()).thenReturn(false);
        when(focus1.isFocusable()).thenReturn(true);
        when(focus2.isFocusable()).thenReturn(true);

        when(focus1.getGroup()).thenReturn(scene);
        when(focus2.getGroup()).thenReturn(scene);

        KeyEvent keyEvent = mock(KeyEvent.class);
        when(keyEvent.getType()).thenReturn(KeyEvent.PRESSED);
        when(keyEvent.getKeycode()).thenReturn(KeyCode.KEY_TAB);

        WindowSettings settings = new WindowSettings.Builder().size(200, 100).theme(theme).layout(scene).build();
        Activity activity = Activity.create(window, settings);

        when(scene.getActivity()).thenReturn(activity);
        when(focus1.getActivity()).thenReturn(activity);
        when(focus2.getActivity()).thenReturn(activity);

        activity.initialize();
        assertEquals(theme, activity.getTheme());
        assertEquals(scene, activity.getScene());

        activity.show();

        activity.setFocus(focus1);
        activity.onKey(keyEvent);

        verify(focus1, times(2)).fireFocus(any());
        verify(focus2).fireFocus(any());
    }
}