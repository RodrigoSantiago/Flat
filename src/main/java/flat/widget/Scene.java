package flat.widget;

import flat.graphics.SmartContext;
import flat.widget.enums.Visibility;
import flat.widget.layout.Box;
import flat.window.Activity;
import flat.window.ActivityScene;

import java.util.HashMap;

public class Scene extends Box {

    ActivityScene activityScene = new ActivityScene(this);
    HashMap<String, Widget> idMap = new HashMap<>();

    public Scene() {

    }

    public ActivityScene getActivityScene() {
        return activityScene;
    }

    @Override
    public void onActivityChange(Activity prev, Activity activity) {
        if (activity == getActivity()) {
            super.onActivityChange(prev, activity);
        }
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
    protected void childInvalidate(Widget child, boolean source) {
        if (getParent() == null) {
            Activity activity = getActivity();
            if (activity != null) {
                activity.invalidateWidget(source ? this : child);
            }
        } else {
            super.childInvalidate(child, source);
        }
    }

    @Override
    public void invalidate(boolean layout) {
        if (getParent() == null) {
            Activity activity = getActivity();
            if (activity != null) {
                if (!layout) {
                    activity.invalidate();
                } else {
                    activity.invalidateWidget(this);
                }
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
            idMap.put(id, widget);
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
            idMap.remove(id, widget);
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
            idMap.put(newID, widget);
        }
    }
}
