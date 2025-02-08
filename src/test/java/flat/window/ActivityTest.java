package flat.window;

import flat.animations.Animation;
import flat.events.KeyCode;
import flat.events.KeyEvent;
import flat.graphics.SmartContext;
import flat.graphics.context.Context;
import flat.resources.ResourceStream;
import flat.uxml.UXBuilder;
import flat.uxml.UXNode;
import flat.uxml.UXSheet;
import flat.uxml.UXTheme;
import flat.widget.Scene;
import flat.widget.Widget;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class ActivityTest {

    @Test
    public void constructor() {
        Window window = mock(Window.class);
        Context context = mock(Context.class);

        when(context.getWindow()).thenReturn(window);
        when(context.getWidth()).thenReturn(200);
        when(context.getHeight()).thenReturn(100);

        Activity activity = new Activity(context);

        assertEquals(context, activity.getContext());
        assertEquals(window, activity.getWindow());
        assertEquals(200f, activity.getWidth(), 0.00001f);
        assertEquals(100f, activity.getHeight(), 0.00001f);
    }

    @Test
    public void setTheme() {
        Window window = mock(Window.class);
        Context context = mock(Context.class);
        UXTheme theme1 = mock(UXTheme.class);
        UXTheme theme2 = mock(UXTheme.class);

        when(context.getWindow()).thenReturn(window);
        when(window.getDpi()).thenReturn(160f);

        Activity activity = new Activity(context);

        assertNull(activity.getTheme());
        activity.setTheme(theme1);
        assertNull(activity.getTheme());

        activity.show();
        assertEquals(theme1, activity.getTheme());

        activity.setTheme(theme2);
        assertEquals(theme1, activity.getTheme());

        activity.refreshScene();
        assertEquals(theme2, activity.getTheme());
    }

    @Test
    public void setThemeResource() {
        Window window = mock(Window.class);
        Context context = mock(Context.class);
        UXTheme theme1 = mock(UXTheme.class);
        UXTheme theme2 = mock(UXTheme.class);
        ResourceStream stream = mock(ResourceStream.class);

        UXSheet sheet = mock(UXSheet.class);
        when(sheet.instance()).thenReturn(theme2);
        when(stream.getCache()).thenReturn(sheet);

        when(context.getWindow()).thenReturn(window);
        when(window.getDpi()).thenReturn(160f);

        Activity activity = new Activity(context);

        assertNull(activity.getTheme());
        activity.setTheme(theme1);
        assertNull(activity.getTheme());

        activity.show();
        assertEquals(theme1, activity.getTheme());

        activity.setTheme(stream);
        assertEquals(theme1, activity.getTheme());

        activity.refreshScene();
        assertEquals(theme2, activity.getTheme());
    }

    @Test
    public void setScene() {
        Window window = mock(Window.class);
        Context context = mock(Context.class);
        UXTheme theme = mock(UXTheme.class);
        Scene scene1 = mock(Scene.class);
        when(scene1.getActivityScene()).thenReturn(mock(ActivityScene.class));
        Scene scene2 = mock(Scene.class);
        when(scene2.getActivityScene()).thenReturn(mock(ActivityScene.class));

        when(context.getWindow()).thenReturn(window);
        when(window.getDpi()).thenReturn(160f);

        Activity activity = new Activity(context);

        assertNull(activity.getTheme());
        activity.setTheme(theme);
        activity.setScene(scene1);
        assertNull(activity.getTheme());
        assertNull(activity.getScene());

        activity.show();
        verify(scene1, times(1)).applyTheme();
        assertEquals(scene1, activity.getScene());

        activity.setScene(scene2);
        assertEquals(scene1, activity.getScene());

        activity.refreshScene();
        verify(scene2, times(1)).applyTheme();
        assertEquals(scene2, activity.getScene());
    }

    @Test
    public void setSceneResource() {
        Window window = mock(Window.class);
        Context context = mock(Context.class);
        UXTheme theme1 = mock(UXTheme.class);
        Scene scene1 = mock(Scene.class);
        when(scene1.getActivityScene()).thenReturn(mock(ActivityScene.class));
        Scene scene2 = mock(Scene.class);
        when(scene2.getActivityScene()).thenReturn(mock(ActivityScene.class));
        UXBuilder builder = mock(UXBuilder.class);
        when(builder.build(any())).thenReturn(scene2);

        UXNode root = mock(UXNode.class);
        when(root.instance(any())).thenReturn(builder);

        ResourceStream stream = mock(ResourceStream.class);
        when(stream.getCache()).thenReturn(root);

        when(context.getWindow()).thenReturn(window);
        when(window.getDpi()).thenReturn(160f);

        Activity activity = new Activity(context);

        assertNull(activity.getTheme());
        activity.setTheme(theme1);
        activity.setScene(scene1);
        assertNull(activity.getTheme());
        assertNull(activity.getScene());

        activity.show();
        verify(scene1, times(1)).applyTheme();
        assertEquals(theme1, activity.getTheme());
        assertEquals(scene1, activity.getScene());

        activity.setScene(stream);
        assertEquals(scene1, activity.getScene());

        activity.refreshScene();
        verify(scene2, times(1)).applyTheme();
        assertEquals(scene2, activity.getScene());
    }

    @Test
    public void performLayout() {
        Window window = mock(Window.class);
        Context context = mock(Context.class);
        UXTheme theme = mock(UXTheme.class);
        Scene scene = mock(Scene.class);
        Widget child = mock(Widget.class);
        when(scene.getActivityScene()).thenReturn(mock(ActivityScene.class));

        when(context.getWindow()).thenReturn(window);
        when(window.getClientWidth()).thenReturn(200);
        when(window.getClientHeight()).thenReturn(100);
        when(child.getLayoutWidth()).thenReturn(20f);
        when(child.getLayoutHeight()).thenReturn(10f);
        when(window.getDpi()).thenReturn(160f);

        Activity activity = new Activity(context);

        assertNull(activity.getTheme());
        activity.setTheme(theme);
        activity.setScene(scene);
        assertNull(activity.getTheme());
        assertNull(activity.getScene());

        activity.show();
        verify(scene).onMeasure();
        verify(scene).onLayout(200f, 100f);

        verify(scene, times(1)).applyTheme();
        assertEquals(scene, activity.getScene());

        activity.layout(200f, 100f);
        verify(scene, times(1)).onMeasure();
        verify(scene, times(1)).onLayout(200f, 100f);

        activity.invalidateWidget(scene);
        activity.layout(100f, 100f);
        verify(scene, times(2)).onMeasure();
        verify(scene, times(1)).onLayout(100f, 100f);

        activity.invalidateWidget(child);
        activity.layout(100f, 100f);
        verify(scene, times(2)).onMeasure();
        verify(scene, times(1)).onLayout(100f, 100f);
        verify(child, times(0)).onMeasure();
        verify(child, times(1)).onLayout(20f, 10f);
    }

    @Test
    public void performDraw() {
        Window window = mock(Window.class);
        Context context = mock(Context.class);
        SmartContext smartContext = mock(SmartContext.class);
        UXTheme theme = mock(UXTheme.class);
        Scene scene = mock(Scene.class);
        when(scene.getActivityScene()).thenReturn(mock(ActivityScene.class));

        when(context.getWindow()).thenReturn(window);
        when(window.getClientWidth()).thenReturn(200);
        when(window.getClientHeight()).thenReturn(100);
        when(window.getDpi()).thenReturn(160f);

        Activity activity = new Activity(context);

        assertNull(activity.getTheme());
        activity.setTheme(theme);
        activity.setScene(scene);
        assertNull(activity.getTheme());
        assertNull(activity.getScene());

        activity.show();
        verify(scene, times(1)).applyTheme();
        assertEquals(scene, activity.getScene());

        activity.draw(smartContext);
        verify(smartContext).setView(0, 0, 200, 100);
        verify(smartContext).clear(0x0, 1, 0);
        verify(smartContext).clearClip();
        verify(scene).onDraw(smartContext);
    }

    @Test
    public void performAnimations() {
        Window window = mock(Window.class);
        Context context = mock(Context.class);
        UXTheme theme = mock(UXTheme.class);
        Animation animation = mock(Animation.class);
        Scene scene = mock(Scene.class);
        when(scene.getActivityScene()).thenReturn(mock(ActivityScene.class));

        when(context.getWindow()).thenReturn(window);
        when(context.getWidth()).thenReturn(200);
        when(context.getHeight()).thenReturn(100);
        when(window.getDpi()).thenReturn(160f);

        when(animation.isPlaying()).thenReturn(true);

        Activity activity = new Activity(context);
        when(animation.getSource()).thenReturn(activity);

        assertNull(activity.getTheme());
        activity.setTheme(theme);
        activity.setScene(scene);
        assertNull(activity.getTheme());
        assertNull(activity.getScene());

        activity.show();
        verify(scene, times(1)).applyTheme();
        assertEquals(scene, activity.getScene());

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
        Window window = mock(Window.class);
        Context context = mock(Context.class);
        UXTheme theme = mock(UXTheme.class);
        Scene scene = mock(Scene.class);
        when(scene.getActivityScene()).thenReturn(mock(ActivityScene.class));
        Widget widget = mock(Widget.class);
        when(context.getWindow()).thenReturn(window);
        when(window.getDpi()).thenReturn(160f);
        when(scene.findById(findId)).thenReturn(widget);

        Activity activity = new Activity(context);

        assertNull(activity.getTheme());
        activity.setTheme(theme);
        activity.setScene(scene);
        assertNull(activity.getTheme());
        assertNull(activity.getScene());
        assertNull(activity.findById(findId));

        activity.show();
        verify(scene, times(1)).applyTheme();
        assertEquals(scene, activity.getScene());

        Widget found = activity.findById(findId);
        assertEquals(widget, found);
        verify(scene).findById(findId);
    }

    @Test
    public void findByPosition() {
        Window window = mock(Window.class);
        Context context = mock(Context.class);
        UXTheme theme = mock(UXTheme.class);
        Scene scene = mock(Scene.class);
        when(scene.getActivityScene()).thenReturn(mock(ActivityScene.class));
        Widget widget = mock(Widget.class);
        when(context.getWindow()).thenReturn(window);
        when(window.getDpi()).thenReturn(160f);
        when(scene.findByPosition(10, 20, true)).thenReturn(widget);

        Activity activity = new Activity(context);

        assertNull(activity.getTheme());
        activity.setTheme(theme);
        activity.setScene(scene);
        assertNull(activity.getTheme());
        assertNull(activity.getScene());
        assertNull(activity.findByPosition(10, 20, true));

        activity.show();
        verify(scene, times(1)).applyTheme();
        assertEquals(scene, activity.getScene());

        Widget found = activity.findByPosition(10, 20, true);
        assertEquals(widget, found);
        verify(scene).findByPosition(10, 20, true);
    }

    @Test
    public void findFocused() {
        Window window = mock(Window.class);
        Context context = mock(Context.class);
        UXTheme theme = mock(UXTheme.class);
        Scene scene = mock(Scene.class);
        when(scene.getActivityScene()).thenReturn(mock(ActivityScene.class));
        Widget widget = mock(Widget.class);
        when(context.getWindow()).thenReturn(window);
        when(window.getDpi()).thenReturn(160f);
        when(scene.findFocused()).thenReturn(widget);

        Activity activity = new Activity(context);

        assertNull(activity.getTheme());
        activity.setTheme(theme);
        activity.setScene(scene);
        assertNull(activity.getTheme());
        assertNull(activity.getScene());
        assertNull(activity.findFocused());

        activity.show();
        verify(scene, times(1)).applyTheme();
        assertEquals(scene, activity.getScene());

        Widget found = activity.findFocused();
        assertEquals(widget, found);
        verify(scene).findFocused();
    }

    @Test
    public void hide() {
        Window window = mock(Window.class);
        Context context = mock(Context.class);
        UXTheme theme = mock(UXTheme.class);
        Scene scene = mock(Scene.class);
        when(scene.getActivityScene()).thenReturn(mock(ActivityScene.class));

        when(context.getWindow()).thenReturn(window);
        when(window.getDpi()).thenReturn(160f);

        Activity activity = new Activity(context);

        assertNull(activity.getTheme());
        activity.setTheme(theme);
        activity.setScene(scene);
        assertNull(activity.getTheme());
        assertNull(activity.getScene());
        assertFalse(activity.isListening());

        activity.show();
        verify(scene, times(1)).applyTheme();
        assertEquals(scene, activity.getScene());
        assertFalse(activity.isListening());

        activity.start();
        verify(scene, times(1)).applyTheme();
        assertEquals(scene, activity.getScene());
        assertTrue(activity.isListening());

        activity.pause();
        verify(scene, times(1)).applyTheme();
        assertEquals(scene, activity.getScene());
        assertFalse(activity.isListening());

        activity.hide();
        verify(scene, times(1)).applyTheme();
        assertEquals(scene, activity.getScene());
        assertFalse(activity.isListening());
    }

    @Test
    public void onTabPressedChangeFocus() {
        String sceneId = "sceneId";
        String focus1Id = "focus1Id";
        String focus2Id = "focis2Id";

        Window window = mock(Window.class);
        Context context = mock(Context.class);
        UXTheme theme = mock(UXTheme.class);
        Scene scene = mock(Scene.class);
        when(scene.getActivityScene()).thenReturn(mock(ActivityScene.class));
        Widget focus1 = mock(Widget.class);
        Widget focus2 = mock(Widget.class);

        when(scene.findById(sceneId)).thenReturn(scene);
        when(scene.findById(focus1Id)).thenReturn(focus1);
        when(scene.findById(focus2Id)).thenReturn(focus2);

        when(scene.getNextFocusId()).thenReturn(focus1Id);
        when(focus1.getNextFocusId()).thenReturn(focus2Id);
        when(focus2.getNextFocusId()).thenReturn(sceneId);

        when(context.getWindow()).thenReturn(window);
        when(window.getDpi()).thenReturn(160f);

        KeyEvent keyEvent = mock(KeyEvent.class);
        when(keyEvent.getType()).thenReturn(KeyEvent.RELEASED);
        when(keyEvent.getKeycode()).thenReturn(KeyCode.KEY_TAB);

        Activity activity = new Activity(context);
        when(scene.getActivity()).thenReturn(null).thenReturn(activity);
        when(focus1.getActivity()).thenReturn(activity);
        when(focus2.getActivity()).thenReturn(activity);

        assertNull(activity.getTheme());
        activity.setTheme(theme);
        activity.setScene(scene);
        assertNull(activity.getTheme());
        assertNull(activity.getScene());

        activity.show();
        verify(scene, times(1)).applyTheme();
        assertEquals(scene, activity.getScene());

        activity.onKeyPress(keyEvent);
        activity.onKeyPress(keyEvent);

        verify(focus1, times(2)).fireFocus(any());
        verify(focus2).fireFocus(any());
    }
}