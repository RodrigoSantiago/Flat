package flat.widget.selection;

import flat.events.ActionEvent;
import flat.events.ActionListener;
import flat.uxml.Controller;
import flat.uxml.UXAttributes;
import flat.widget.Widget;

public abstract class ToogleWidget extends Widget {

    private ActionListener toogleListener;

    protected ToogleGroup toogleGroup;
    protected boolean selected;

    @Override
    public void applyAttributes(Controller controller, UXAttributes attributes) {
        super.applyAttributes(controller, attributes);

        setSelected(attributes.asBoolean("selected", false));
        attributes.link("toogleGroup", (widget) -> setToogleGroup((ToogleGroup) widget));
    }

    public ActionListener getToogleListener() {
        return toogleListener;
    }

    public void setToogleListener(ActionListener toogleListener) {
        this.toogleListener = toogleListener;
    }

    public void fireAction(ActionEvent event) {
        if (toogleListener != null) {
            toogleListener.handle(event);
        }
    }

    public void toogle() {
        setSelected(!selected);
    }

    public ToogleGroup getToogleGroup() {
        return toogleGroup;
    }

    public void setToogleGroup(ToogleGroup group) {
        if (this.toogleGroup != group) {
            if (this.toogleGroup != null) {
                this.toogleGroup.toogleDetach(this);
            }
            if (group != null) {
                group.toogleAttach(this);
            }
        }
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        if (this.selected != selected) {
            if (toogleGroup == null) {
                onSelected(this.selected = selected);
            } else {
                if (!selected) {
                    if (toogleGroup.getSelectionIndex() == toogleGroup.toogles.indexOf(this)
                            && toogleGroup.isEnableUnselection()) {
                        this.selected = false;
                        toogleGroup.toogleSelect(null);
                    }
                } else {
                    this.selected = true;
                    toogleGroup.toogleSelect(this);
                }
            }
        }
    }

    protected void onSelected(boolean selected) {
        fireAction(new ActionEvent(this, ActionEvent.ACTION));
    }
}
