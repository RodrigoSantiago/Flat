package flat.widget;

import flat.graphics.SmartContext;
import flat.uxml.UXChildren;
import flat.widget.enums.Visibility;
import flat.window.Activity;
import flat.window.ActivityScene;

import java.util.ArrayList;
import java.util.List;

public class Scene extends Group {

    private ArrayList<Stage> stages = new ArrayList<>();

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

    public boolean onLayoutSingleChild(Widget child) {
        if (getChildren().contains(child)) {
            child.onMeasure();
            performSingleLayoutFree(getLayoutWidth(), getLayoutHeight(), child);
            return true;
        }
        return false;
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

    void addStage(Stage stage) {
        if (stages.contains(stage)) {
            if (stages.get(stages.size() - 1) != stage) {
                stages.remove(stage);
                stages.add(stage);
                var children = getChildren();
                children.remove(stage);
                children.add(stage);
                childInvalidate(stage);
            }

        } else if (attachAndAddChild(stage)) {
            stages.add(stage);
        }
    }

    @Override
    protected boolean detachChild(Widget child) {
        if (super.detachChild(child)) {
            if (child instanceof Stage stage) {
                stages.remove(stage);
                stage.hide();
            }
            return true;
        }
        return false;
    }

    @Override
    protected void sortChildren() {
        if (stages.size() == 0) {
            super.sortChildren();
        } else {
            var children = getChildren();
            children.removeAll(stages);
            children.sort(childComparator);
            children.addAll(stages);
        }
    }

    @Override
    protected void childInvalidate(Widget child) {
        if (activityScene != null && activityScene.getActivity() != null) {
            activityScene.getActivity().invalidateWidget(child);
        } else {
            super.childInvalidate(child);
        }
    }
}
