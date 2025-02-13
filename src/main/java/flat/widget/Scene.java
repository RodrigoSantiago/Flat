package flat.widget;

import flat.graphics.SmartContext;
import flat.uxml.UXChildren;
import flat.widget.enums.Visibility;
import flat.window.Activity;
import flat.window.ActivityScene;

import java.util.List;

public class Scene extends Group {

    ActivityScene activityScene;

    public ActivityScene getActivityScene() {
        if (activityScene == null) {
            activityScene = new ActivityScene(new SceneActivity(this));
        }
        return activityScene;
    }

    @Override
    public Activity getActivity() {
        if (activityScene != null && activityScene.getActivity() != null) {
            return activityScene.getActivity();
        } else {
            return super.getActivity();
        }
    }

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        Widget widget;
        while ((widget = children.next()) != null ) {
            add(widget);
        }
    }

    @Override
    public void onMeasure() {
        performMeasureStack();
    }

    @Override
    public void onLayout(float width, float height) {
        performLayoutFree(width, height);
    }

    @Override
    public void add(Widget child) {
        super.add(child);
    }

    @Override
    public void add(Widget... children) {
        super.add(children);
    }

    @Override
    public void add(List<Widget> children) {
        super.add(children);
    }

    @Override
    public void onDraw(SmartContext context) {
        if (getVisibility() == Visibility.VISIBLE) {
            super.onDraw(context);
        }
    }
}
