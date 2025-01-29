package flat.widget;

import flat.graphics.context.Context;
import flat.uxml.Controller;
import flat.uxml.UXBuilder;
import flat.uxml.UXChildren;
import flat.uxml.UXTheme;
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
    public void childrenFromUx() {
        Scene scene = new Scene();
        Widget child = new Widget();

        UXChildren uxChild = mock(UXChildren.class);
        when(uxChild.next()).thenReturn(child).thenReturn(null);

        when(activity.getScene()).thenReturn(scene);
        ActivitySupport.setActivity(scene, activity);

        assertNull(child.getParent());
        scene.applyChildren(uxChild);
        assertEquals(child, scene.getChildrenIterable().get(0));
        assertEquals(scene, child.getParent());
    }

    @Test
    public void siblingsFromUx() {
        Scene scene = new Scene();
        Widget child1 = new Widget();
        Widget child2 = new Widget();

        UXChildren uxChild = mock(UXChildren.class);
        when(uxChild.next()).thenReturn(child1).thenReturn(child2).thenReturn(null);

        when(activity.getScene()).thenReturn(scene);
        ActivitySupport.setActivity(scene, activity);

        assertNull(child1.getParent());
        assertNull(child2.getParent());
        scene.applyChildren(uxChild);
        assertEquals(child1, scene.getChildrenIterable().get(0));
        assertEquals(child2, scene.getChildrenIterable().get(1));
        assertEquals(scene, child1.getParent());
        assertEquals(scene, child2.getParent());
    }

    @Test
    public void siblingsFromAdding() {
        Scene scene = new Scene();
        Widget child1 = new Widget();
        Widget child2 = new Widget();


        assertNull(child1.getParent());
        assertNull(child2.getParent());

        scene.add(child1, child2);

        assertEquals(child1, scene.getChildrenIterable().get(0));
        assertEquals(child2, scene.getChildrenIterable().get(1));
        assertEquals(scene, child1.getParent());
        assertEquals(scene, child2.getParent());
    }

    @Test
    public void swapParentsFromAdding() {
        Scene scene1 = new Scene();
        Scene scene2 = new Scene();
        Widget child = new Widget();

        UXChildren uxChild = mock(UXChildren.class);

        assertNull(child.getParent());
        scene1.add(child);

        assertEquals(child, scene1.getChildrenIterable().get(0));
        assertEquals(0, scene2.getChildrenIterable().size());
        assertEquals(scene1, child.getParent());

        scene2.add(child);
        assertEquals(child, scene2.getChildrenIterable().get(0));
        assertEquals(0, scene1.getChildrenIterable().size());
        assertEquals(scene2, child.getParent());
    }

    @Test
    public void addTwice() {
        Scene scene = new Scene();
        Widget child = new Widget();

        UXChildren uxChild = mock(UXChildren.class);

        assertNull(child.getParent());
        scene.add(child);

        assertEquals(child, scene.getChildrenIterable().get(0));
        assertEquals(1, scene.getChildrenIterable().size());
        assertEquals(scene, child.getParent());

        scene.add(child);
        assertEquals(child, scene.getChildrenIterable().get(0));
        assertEquals(1, scene.getChildrenIterable().size());
        assertEquals(scene, child.getParent());
    }

    @Test
    public void addAndRemove() {
        Scene scene = new Scene();
        Widget child = new Widget();

        UXChildren uxChild = mock(UXChildren.class);

        assertNull(child.getParent());
        scene.add(child);

        assertEquals(child, scene.getChildrenIterable().get(0));
        assertEquals(1, scene.getChildrenIterable().size());
        assertEquals(scene, child.getParent());

        scene.remove(child);
        assertEquals(0, scene.getChildrenIterable().size());
        assertNull(child.getParent());
    }

    @Test
    public void addAndRemoveSibling() {
        Scene scene = new Scene();
        Widget child1 = new Widget();
        Widget child2 = new Widget();

        UXChildren uxChild = mock(UXChildren.class);

        assertNull(child1.getParent());
        assertNull(child2.getParent());
        scene.add(child1);
        scene.add(child2);

        assertEquals(child1, scene.getChildrenIterable().get(0));
        assertEquals(child2, scene.getChildrenIterable().get(1));
        assertEquals(2, scene.getChildrenIterable().size());
        assertEquals(scene, child1.getParent());
        assertEquals(scene, child2.getParent());

        scene.remove(child1);
        assertEquals(child2, scene.getChildrenIterable().get(0));
        assertEquals(1, scene.getChildrenIterable().size());
        assertNull(child1.getParent());
        assertEquals(scene, child2.getParent());
    }

    @Test
    public void siblingsOrderFromElevation() {
        Scene scene = new Scene();
        Widget child1 = new Widget();
        Widget child2 = new Widget();

        assertNull(child1.getParent());
        assertNull(child2.getParent());

        scene.add(child1, child2);

        assertEquals(child1, scene.getChildrenIterable().get(0));
        assertEquals(child2, scene.getChildrenIterable().get(1));
        assertEquals(scene, child1.getParent());
        assertEquals(scene, child2.getParent());

        child1.setElevation(2);
        child2.setElevation(0);

        assertEquals(child2, scene.getChildrenIterable().get(0));
        assertEquals(child1, scene.getChildrenIterable().get(1));
        assertEquals(scene, child1.getParent());
        assertEquals(scene, child2.getParent());
    }

    @Test
    public void measureScene() {
        Scene scene = new Scene();

        scene.setPrefSize(150, 100);
        scene.onMeasure();

        assertEquals(150f, scene.getMeasureWidth(), 0.0001f);
        assertEquals(100f, scene.getMeasureHeight(), 0.0001f);

        scene.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        scene.onMeasure();

        assertEquals(Widget.WRAP_CONTENT, scene.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.WRAP_CONTENT, scene.getMeasureHeight(), 0.0001f);

        scene.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        scene.onMeasure();

        assertEquals(Widget.MATCH_PARENT, scene.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, scene.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void measureSceneWithMin() {
        // Min should affect measure values
        Scene scene = new Scene();

        scene.setPrefSize(150, 100);
        scene.setMinSize(200, 250);
        scene.onMeasure();

        assertEquals(200, scene.getMeasureWidth(), 0.0001f);
        assertEquals(250, scene.getMeasureHeight(), 0.0001f);

        scene.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        scene.onMeasure();

        assertEquals(200, scene.getMeasureWidth(), 0.0001f);
        assertEquals(250, scene.getMeasureHeight(), 0.0001f);

        scene.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        scene.onMeasure();

        assertEquals(Widget.MATCH_PARENT, scene.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, scene.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void measureSceneWithMinAndPadding() {
        Scene scene = new Scene();

        scene.setPrefSize(150, 100);
        scene.setPadding(10, 10, 10, 10);
        scene.onMeasure();

        assertEquals(170, scene.getMeasureWidth(), 0.0001f);
        assertEquals(120, scene.getMeasureHeight(), 0.0001f);

        scene.setPrefSize(150, 100);
        scene.setMinSize(160, 110);
        scene.setPadding(10, 10, 10, 10);
        scene.onMeasure();

        assertEquals(170, scene.getMeasureWidth(), 0.0001f);
        assertEquals(120, scene.getMeasureHeight(), 0.0001f);

        scene.setPrefSize(150, 100);
        scene.setMinSize(180, 130);
        scene.setPadding(10, 10, 10, 10);
        scene.onMeasure();

        assertEquals(180, scene.getMeasureWidth(), 0.0001f);
        assertEquals(130, scene.getMeasureHeight(), 0.0001f);

        scene.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        scene.setMinSize(160, 110);
        scene.setPadding(10, 10, 10, 10);
        scene.onMeasure();

        assertEquals(160, scene.getMeasureWidth(), 0.0001f);
        assertEquals(110, scene.getMeasureHeight(), 0.0001f);

        scene.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        scene.setMinSize(160, 110);
        scene.setPadding(10, 10, 10, 10);
        scene.onMeasure();

        assertEquals(Widget.MATCH_PARENT, scene.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, scene.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void measureSceneWithMinAndMargin() {
        Scene scene = new Scene();

        scene.setPrefSize(150, 100);
        scene.setMargins(10, 10, 10, 10);
        scene.onMeasure();

        assertEquals(170, scene.getMeasureWidth(), 0.0001f);
        assertEquals(120, scene.getMeasureHeight(), 0.0001f);

        scene.setPrefSize(150, 100);
        scene.setMinSize(160, 110);
        scene.setMargins(10, 10, 10, 10);
        scene.onMeasure();

        assertEquals(170, scene.getMeasureWidth(), 0.0001f);
        assertEquals(120, scene.getMeasureHeight(), 0.0001f);

        scene.setPrefSize(150, 100);
        scene.setMinSize(180, 130);
        scene.setMargins(10, 10, 10, 10);
        scene.onMeasure();

        assertEquals(180, scene.getMeasureWidth(), 0.0001f);
        assertEquals(130, scene.getMeasureHeight(), 0.0001f);

        scene.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        scene.setMinSize(160, 110);
        scene.setMargins(10, 10, 10, 10);
        scene.onMeasure();

        assertEquals(160, scene.getMeasureWidth(), 0.0001f);
        assertEquals(110, scene.getMeasureHeight(), 0.0001f);

        scene.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        scene.setMinSize(160, 110);
        scene.setMargins(10, 10, 10, 10);
        scene.onMeasure();

        assertEquals(Widget.MATCH_PARENT, scene.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, scene.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void measureSceneWithMax() {
        // Max should not affect measure values
        Scene scene = new Scene();
        scene.setMaxSize(150, 100);
        scene.setPrefSize(200, 250);
        scene.onMeasure();

        assertEquals(200, scene.getMeasureWidth(), 0.0001f);
        assertEquals(250, scene.getMeasureHeight(), 0.0001f);

        scene.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);
        scene.onMeasure();

        assertEquals(Widget.WRAP_CONTENT, scene.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.WRAP_CONTENT, scene.getMeasureHeight(), 0.0001f);

        scene.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        scene.onMeasure();

        assertEquals(Widget.MATCH_PARENT, scene.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, scene.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void measureSceneMatchParent() {
        Scene parent = new Scene();
        parent.setPrefSize(150, 100);

        Scene child = new Scene();
        child.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        parent.add(child);

        parent.onMeasure();

        assertEquals(150, parent.getMeasureWidth(), 0.0001f);
        assertEquals(100, parent.getMeasureHeight(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, child.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, child.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void measureSceneMatchParentChild() {
        Scene parent = new Scene();
        parent.setPrefSize(150, 100);

        Scene child = new Scene();
        child.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        parent.add(child);

        parent.onMeasure();

        assertEquals(150f, parent.getMeasureWidth(), 0.0001f);
        assertEquals(100f, parent.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void measureSceneMatchParentChildAndWrapContent() {
        Scene parent = new Scene();
        parent.setPrefSize(Widget.WRAP_CONTENT, Widget.WRAP_CONTENT);

        Scene child = new Scene();
        child.setPrefSize(Widget.MATCH_PARENT, Widget.MATCH_PARENT);
        parent.add(child);

        parent.onMeasure();

        assertEquals(Widget.MATCH_PARENT, child.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, child.getMeasureHeight(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, parent.getMeasureWidth(), 0.0001f);
        assertEquals(Widget.MATCH_PARENT, parent.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void measureChildBiggerThanParent() {
        Scene parent = new Scene();
        parent.setPrefSize(150, 100);

        Scene child = new Scene();
        child.setPrefSize(200, 250);
        parent.add(child);

        parent.onMeasure();

        assertEquals(200, parent.getMeasureWidth(), 0.0001f);
        assertEquals(250, parent.getMeasureHeight(), 0.0001f);
        assertEquals(200, child.getMeasureWidth(), 0.0001f);
        assertEquals(250, child.getMeasureHeight(), 0.0001f);
    }

    @Test
    public void findById() {
        Scene parent = new Scene();

        Scene child = new Scene();
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

        Scene child1 = new Scene();
        child1.setId("child-id1");
        parent.add(child1);

        assertEquals(child1, parent.findById("child-id1"));

        Scene child2 = new Scene();
        child2.setId("child-id2");
        child1.add(child2);
        assertEquals(child2, child1.findById("child-id2"));
    }
}
