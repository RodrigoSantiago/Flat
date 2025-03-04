package flat.widget.layout;

import flat.uxml.UXChildren;
import flat.widget.Parent;
import flat.widget.Widget;

import java.util.List;

public class Panel extends Parent {

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        for (var child : children) {
            add(child.getWidget());
        }
    }

    @Override
    public void onMeasure() {
        performMeasureStack();
    }

    @Override
    public void onLayout(float width, float height) {
        setLayout(width, height);
        performLayoutFree(getInWidth(), getInHeight());
    }

    @Override
    public boolean onLayoutSingleChild(Widget child) {
        if (getChildren().contains(child)) {
            child.onMeasure();
            performSingleLayoutFree(getInWidth(), getInHeight(), child);
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

}
