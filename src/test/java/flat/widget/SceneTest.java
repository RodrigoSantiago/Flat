package flat.widget;

import flat.graphics.context.Context;
import flat.uxml.Controller;
import flat.uxml.UXBuilder;
import flat.uxml.UXTheme;
import flat.widget.layout.Box;
import flat.window.Activity;
import flat.window.ActivitySupport;
import flat.window.Window;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class SceneTest {

    Window window;
    Context context;
    Activity activity;
    UXTheme theme;
    Controller controller;
    UXBuilder builder;

    @Before
    public void before() {
        window = mock(Window.class);
        context = mock(Context.class);

        when(context.getWindow()).thenReturn(window);
        when(context.getWidth()).thenReturn(200);
        when(context.getHeight()).thenReturn(100);

        activity = mock(Activity.class);
        when(activity.getContext()).thenReturn(context);
        when(activity.getWindow()).thenReturn(window);
        when(activity.getWidth()).thenReturn(200f);
        when(activity.getHeight()).thenReturn(100f);

        theme = mock(UXTheme.class);
        controller = mock(Controller.class);
        builder = mock(UXBuilder.class);
    }

    @After
    public void after() {

    }

    @Test
    public void constructor() {
        Scene scene = new Scene();

        when(activity.getScene()).thenReturn(scene);
        ActivitySupport.setActivity(scene, activity);

        assertEquals(activity, scene.getActivity());
    }

    @Test
    public void findById() {
        Scene parent = new Scene();

        Box child = new Box();
        parent.add(child);

        child.setId("child-id");

        assertEquals(child, parent.findById("child-id"));

        child.setId("child-id2");

        assertEquals(child, parent.findById("child-id2"));

        parent.remove(child);

        assertNull(parent.findById("child-id"));
        assertNull(parent.findById("child-id2"));
    }

    @Test
    public void assignIdBeforeAdd() {
        Scene parent = new Scene();

        Box child1 = new Box();
        child1.setId("child-id1");
        parent.add(child1);

        assertEquals(child1, parent.findById("child-id1"));

        Box child2 = new Box();
        child2.setId("child-id2");
        child1.add(child2);
        assertEquals(child2, parent.findById("child-id2"));
    }

    @Test
    public void childSceneBlockIdPropagation() {
        Scene parent = new Scene();

        Scene child1 = new Scene();
        parent.add(child1);

        Box child2 = new Box();
        child2.setId("child-id2");
        child1.add(child2);

        assertNull(parent.findById("child-id2"));
        assertEquals(child2, child1.findById("child-id2"));
    }
}
