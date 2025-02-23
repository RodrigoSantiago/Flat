package flat.widget.structure;

import flat.uxml.Controller;
import flat.uxml.UXChildren;
import flat.widget.Widget;
import flat.widget.layout.Frame;
import flat.widget.text.Chip;
import flat.window.Application;

public class Page extends Chip {

    private Frame frame;

    @Override
    public void applyChildren(UXChildren children) {
        super.applyChildren(children);

        Frame autoFrame = null;
        Widget widget;
        while ((widget = children.next()) != null ) {
            if (autoFrame == null && widget instanceof Frame firstFrame) {
                if (!children.hasNext()) {
                    autoFrame = firstFrame;
                    break;
                }
            } else {
                if (autoFrame == null) {
                    autoFrame = new Frame();
                    autoFrame.setPrefSize(MATCH_PARENT, MATCH_PARENT);
                }
                autoFrame.add(widget);
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
            if (getParent() instanceof Tab tab) {
                if (isSelected()) {
                    tab.refreshPage(this);
                }
            }
        }
    }

    public Frame getFrame() {
        return frame;
    }

    public boolean isSelected() {
        if (getParent() instanceof Tab tab) {
            return tab.getSelectedPage() == this;
        }
        return false;
    }

    public void requestSelect() {
        if (getParent() instanceof Tab tab) {
            tab.selectPage(this);
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
            if (getParent() instanceof Tab tab) {
                tab.removePage(this);
            }
        }
    }

    void refreshSelectedState() {
        setActivated(isSelected());
    }
}
