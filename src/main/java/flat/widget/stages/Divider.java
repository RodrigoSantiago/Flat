package flat.widget.stages;

import flat.widget.Widget;

public class Divider extends Widget {
    
    @Override
    public float getLayoutMaxWidth() {
        if (getMeasureWidth() == MATCH_PARENT && getParent() != null) {
            return Math.min(getParent().getInWidth(), super.getLayoutMaxWidth());
        }
        return super.getLayoutMaxWidth();
    }
    
    @Override
    public float getLayoutMaxHeight() {
        if (getMeasureHeight() == MATCH_PARENT && getParent() != null) {
            return Math.min(getParent().getInHeight(), super.getLayoutMaxHeight());
        }
        return super.getLayoutMaxHeight();
    }
}
