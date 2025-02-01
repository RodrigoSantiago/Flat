package flat.widget;

import flat.graphics.SmartContext;
import flat.widget.enums.Visibility;
import flat.widget.layout.Box;
import flat.window.Activity;
import flat.window.ActivityScene;
import org.tinylog.Logger;

import java.util.HashMap;

public class Scene extends Box {

    ActivityScene activityScene = new ActivityScene();
    HashMap<String, Widget> idMap = new HashMap<>();

    public Scene() {

    }

    public ActivityScene getActivityScene() {
        return activityScene;
    }

    @Override
    public Activity getActivity() {
        if (activityScene.getActivity() != null) {
            return activityScene.getActivity();
        } else {
            return super.getActivity();
        }
    }

    @Override
    public void invalidate(boolean layout) {
        if (getParent() == null) {
            Activity activity = getActivity();
            if (activity != null) {
                activity.invalidate(layout);
            }
        } else {
            super.invalidate(layout);
        }
    }

    @Override
    public void onDraw(SmartContext context) {
        if (getVisibility() == Visibility.VISIBLE) {
            super.onDraw(context);
        }
    }

    @Override
    public Scene getScene() {
        return this;
    }

    @Override
    public Widget findById(String id) {
        return idMap.get(id);
    }

    final void assign(Widget widget) {
        String id = widget.getId();
        if (id != null) {
            Widget old = idMap.put(id, widget);
            if (old != null && old != widget) {
                Logger.info("Id override {}", id);
            }
        }
        if (!(widget instanceof Scene) && widget.children != null) {
            for (Widget child : widget.getChildrenIterable()) {
                assign(child);
            }
        }
    }

    final void unassign(Widget widget) {
        String id = widget.getId();
        if (id != null) {
            if (!idMap.remove(id, widget)) {
                Logger.info("Id {} not assigned", id);
            }
        }
        if (!(widget instanceof Scene) && widget.children != null) {
            for (Widget child : widget.getChildrenIterable()) {
                unassign(child);
            }
        }
    }

    final void reassign(String oldId, Widget widget) {
        if (idMap.get(oldId) == widget) {
            idMap.remove(oldId);
        }

        String newID = widget.getId();
        if (newID != null) {
            Widget old = idMap.put(newID, widget);
            if (old != null && old != widget) {
                Logger.info("Id override {}", newID);
            }
        }
    }
}
