package flat.widget.bars;

import flat.uxml.UXStyle;
import flat.widget.text.Label;

public class ToolItem extends Label {

    private boolean showAction = true;

    @Override
    public void applyStyle() {
        super.applyStyle();
        UXStyle style = getStyle();
        if (style == null) return;

        setShowAction(style.asBool("show-action", isShowAction()));
    }

    public boolean isShowAction() {
        return showAction;
    }

    public void setShowAction(boolean showAction) {
        if (this.showAction != showAction) {
            this.showAction = showAction;
            invalidate(true);
        }
    }
}
