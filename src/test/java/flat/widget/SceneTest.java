package flat.widget;

import flat.widget.layout.Panel;
import flat.window.Activity;
import flat.window.ActivitySupport;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.powermock.api.mockito.PowerMockito.mock;

public class SceneTest {

    @Test
    public void findById() {
        Scene parent = new Scene();

        Panel child = new Panel();
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

        Panel child1 = new Panel();
        child1.setId("child-id1");
        parent.add(child1);

        assertEquals(child1, parent.findById("child-id1"));

        Panel child2 = new Panel();
        child2.setId("child-id2");
        child1.add(child2);
        assertEquals(child2, parent.findById("child-id2"));
    }

    @Test
    public void childSceneBlockIdPropagation() {
        Scene parent = new Scene();

        Scene child1 = new Scene();
        child1.setId("child-id1");
        parent.add(child1);

        Panel child2 = new Panel();
        child2.setId("child-id2");
        child1.add(child2);

        assertEquals(child1, parent.findById("child-id1"));
        assertNull(parent.findById("child-id2"));

        assertNull(child1.findById("child-id1"));
        assertEquals(child2, child1.findById("child-id2"));
    }

    @Test
    public void changeParent() {
        Scene parentA = new Scene();
        Scene parentB = new Scene();
        Panel child1 = new Panel();

        child1.setId("child-id");
        parentA.add(child1);

        assertEquals(parentA, child1.getParent());
        assertEquals(parentA, child1.getGroup());
        assertEquals(child1, parentA.findById("child-id"));
        assertNull(parentB.findById("child-id"));

        parentB.add(child1);

        assertEquals(parentB, child1.getParent());
        assertEquals(parentB, child1.getGroup());
        assertNull(parentA.findById("child-id"));
        assertEquals(child1, parentB.findById("child-id"));
    }

    @Test
    public void changeParentEventPropagation() {
        Scene parentA = new Scene();
        Scene parentB = new Scene();
        Panel child1 = new Panel();
        Panel child2 = new Panel();
        child1.add(child2);

        child1.setId("child-id1");
        child2.setId("child-id2");
        parentA.add(child1);

        assertEquals(parentA, child1.getParent());
        assertEquals(child1, child2.getParent());
        assertEquals(parentA, child1.getGroup());
        assertEquals(parentA, child2.getGroup());
        assertEquals(child1, parentA.findById("child-id1"));
        assertEquals(child2, parentA.findById("child-id2"));
        assertNull(parentB.findById("child-id1"));
        assertNull(parentB.findById("child-id2"));

        parentB.add(child1);

        assertEquals(parentB, child1.getParent());
        assertEquals(child1, child2.getParent());
        assertEquals(parentB, child2.getGroup());
        assertEquals(parentB, child2.getGroup());
        assertNull(parentA.findById("child-id1"));
        assertNull(parentA.findById("child-id2"));
        assertEquals(child1, parentB.findById("child-id1"));
        assertEquals(child2, parentB.findById("child-id2"));
    }

    @Test
    public void changeParentEventBlockPropagation() {
        Scene parentA = new Scene();
        Scene parentB = new Scene();
        Panel child1 = new Panel();
        Scene parentC = new Scene();
        Panel child2 = new Panel();
        child1.add(parentC);
        parentC.add(child2);

        child1.setId("child-id1");
        child2.setId("child-id2");
        parentC.setId("parend-idc");
        parentA.add(child1);

        assertEquals(parentA, child1.getParent());
        assertEquals(child1, parentC.getParent());
        assertEquals(parentC, child2.getParent());

        assertEquals(parentA, child1.getGroup());
        assertEquals(parentA, parentC.getGroup());
        assertEquals(parentC, child2.getGroup());

        assertEquals(child1, parentA.findById("child-id1"));
        assertEquals(parentC, parentA.findById("parend-idc"));
        assertNull(parentA.findById("child-id2"));

        assertNull(parentB.findById("child-id1"));
        assertNull(parentB.findById("parend-idc"));
        assertNull(parentB.findById("child-id2"));

        assertNull(parentC.findById("child-id1"));
        assertNull(parentC.findById("parend-idc"));
        assertEquals(child2, parentC.findById("child-id2"));

        parentB.add(child1);

        assertEquals(parentB, child1.getParent());
        assertEquals(child1, parentC.getParent());
        assertEquals(parentC, child2.getParent());

        assertEquals(parentB, child1.getGroup());
        assertEquals(parentB, parentC.getGroup());
        assertEquals(parentC, child2.getGroup());

        assertNull(parentA.findById("child-id1"));
        assertNull(parentA.findById("parend-idc"));
        assertNull(parentA.findById("child-id2"));

        assertEquals(child1, parentB.findById("child-id1"));
        assertEquals(parentC, parentB.findById("parend-idc"));
        assertNull(parentB.findById("child-id2"));

        assertNull(parentC.findById("child-id1"));
        assertNull(parentC.findById("parend-idc"));
        assertEquals(child2, parentC.findById("child-id2"));
    }

    @Test
    public void changeParentActivity() {
        Activity activityA = mock(Activity.class);
        Activity activityB = mock(Activity.class);
        Scene parentA = new Scene();
        ActivitySupport.setActivity(parentA, activityA);
        Scene parentB = new Scene();
        ActivitySupport.setActivity(parentB, activityB);

        Panel child1 = new Panel();
        Scene parentC = new Scene();
        Panel child2 = new Panel();
        child1.add(parentC);
        parentC.add(child2);

        child1.setId("child-id1");
        child2.setId("child-id2");
        parentC.setId("parend-idc");
        parentA.add(child1);

        assertEquals(parentA, child1.getParent());
        assertEquals(child1, parentC.getParent());
        assertEquals(parentC, child2.getParent());

        assertEquals(parentA, child1.getGroup());
        assertEquals(parentA, parentC.getGroup());
        assertEquals(parentC, child2.getGroup());

        assertEquals(activityA, child1.getActivity());
        assertEquals(activityA, parentC.getActivity());
        assertEquals(activityA, child2.getActivity());

        assertEquals(child1, parentA.findById("child-id1"));
        assertEquals(parentC, parentA.findById("parend-idc"));
        assertNull(parentA.findById("child-id2"));

        assertNull(parentB.findById("child-id1"));
        assertNull(parentB.findById("parend-idc"));
        assertNull(parentB.findById("child-id2"));

        assertNull(parentC.findById("child-id1"));
        assertNull(parentC.findById("parend-idc"));
        assertEquals(child2, parentC.findById("child-id2"));

        parentB.add(child1);

        assertEquals(parentB, child1.getParent());
        assertEquals(child1, parentC.getParent());
        assertEquals(parentC, child2.getParent());

        assertEquals(parentB, child1.getGroup());
        assertEquals(parentB, parentC.getGroup());
        assertEquals(parentC, child2.getGroup());

        assertEquals(activityB, child1.getActivity());
        assertEquals(activityB, parentC.getActivity());
        assertEquals(activityB, child2.getActivity());

        assertNull(parentA.findById("child-id1"));
        assertNull(parentA.findById("parend-idc"));
        assertNull(parentA.findById("child-id2"));

        assertEquals(child1, parentB.findById("child-id1"));
        assertEquals(parentC, parentB.findById("parend-idc"));
        assertNull(parentB.findById("child-id2"));

        assertNull(parentC.findById("child-id1"));
        assertNull(parentC.findById("parend-idc"));
        assertEquals(child2, parentC.findById("child-id2"));

        parentB.add(child2);

        assertEquals(parentB, child1.getParent());
        assertEquals(child1, parentC.getParent());
        assertEquals(parentB, child2.getParent());

        assertEquals(parentB, child1.getGroup());
        assertEquals(parentB, parentC.getGroup());
        assertEquals(parentB, child2.getGroup());

        assertEquals(activityB, child1.getActivity());
        assertEquals(activityB, parentC.getActivity());
        assertEquals(activityB, child2.getActivity());

        assertNull(parentA.findById("child-id1"));
        assertNull(parentA.findById("parend-idc"));
        assertNull(parentA.findById("child-id2"));

        assertEquals(child1, parentB.findById("child-id1"));
        assertEquals(parentC, parentB.findById("parend-idc"));
        assertEquals(child2, parentB.findById("child-id2"));

        assertNull(parentC.findById("child-id1"));
        assertNull(parentC.findById("parend-idc"));
        assertNull(parentC.findById("child-id2"));
    }
}
