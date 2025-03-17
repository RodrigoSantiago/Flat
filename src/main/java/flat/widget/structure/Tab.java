package flat.widget.structure;

import flat.uxml.Controller;
import flat.uxml.UXChildren;
import flat.widget.Widget;
import flat.widget.layout.Frame;
import flat.widget.text.Chip;
import flat.window.Application;

public class Tab extends Chip {

    private Frame frame;

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        Frame autoFrame = null;
        Widget widget;
        for (var child : children) {
            if (autoFrame == null && child.getWidget() instanceof Frame firstFrame) {
                if (children.getChildrenCount() == 1) {
                    autoFrame = firstFrame;
                    break;
                }
            } else {
                if (autoFrame == null) {
                    autoFrame = new Frame();
                    autoFrame.setPrefSize(MATCH_PARENT, MATCH_PARENT);
                }
                autoFrame.add(child.getWidget());
            }
        }
        if (autoFrame != null) {
            setFrame(autoFrame);
        }
    }

    @Override
    public void action() {
        requestSelect();
        super.action();
    }

    @Override
    public void requestClose() {
        requestClose(true);
        super.requestClose();
    }

    public void setFrame(Frame frame) {
        if (this.frame != frame) {
            this.frame = frame;
            if (getParent() instanceof TabView tab) {
                if (isSelected()) {
                    tab.refreshTab(this);
                }
            }
        }
    }

    public Frame getFrame() {
        return frame;
    }

    public boolean isSelected() {
        if (getParent() instanceof TabView tab) {
            return tab.getSelectedTab() == this;
        }
        return false;
    }

    public void requestSelect() {
        if (getParent() instanceof TabView tab) {
            tab.selectTab(this);
        }
    }

    public void requestClose(boolean systemRequest) {
        boolean close = true;
        if (getFrame() != null) {
            Controller controller = getFrame().getController();
            if (controller != null) {
                try {
                    close = controller.onCloseRequest(systemRequest);
                } catch (Exception e) {
                    Application.handleException(e);
                }
            }
        }
        if (close) {
            if (getParent() instanceof TabView tab) {
                tab.removeTab(this);
            }
        }
    }

    void refreshSelectedState() {
        setActivated(isSelected());
    }
}
