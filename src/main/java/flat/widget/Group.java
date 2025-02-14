package flat.widget;

import java.util.HashMap;

public abstract class Group extends Parent {

    private final HashMap<String, Widget> idMap = new HashMap<>();

    @Override
    public Widget findById(String id) {
        return idMap.get(id);
    }

    @Override
    protected Group getCurrentOrGroup() {
        return this;
    }

    final void assign(Widget widget) {
        String id = widget.getId();
        if (id != null) {
            idMap.put(id, widget);
        }
    }

    final void unassign(Widget widget) {
        String id = widget.getId();
        if (id != null) {
            if (idMap.get(id) == widget) {
                idMap.remove(id, widget);
            }
        }
    }

    final void reassign(String oldId, Widget widget) {
        if (idMap.get(oldId) == widget) {
            idMap.remove(oldId);
        }

        String newID = widget.getId();
        if (newID != null) {
            idMap.put(newID, widget);
        }
    }
}
