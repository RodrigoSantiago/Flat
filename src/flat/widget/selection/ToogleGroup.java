package flat.widget.selection;

import flat.uxml.Controller;
import flat.uxml.UXAttributes;
import flat.widget.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ToogleGroup extends Widget {

    private int selectionIndex;

    private boolean enableUnselection;
    ArrayList<ToogleWidget> toogles = new ArrayList<>();

    @Override
    public void applyAttributes(Controller controller, UXAttributes attributes) {
        super.applyAttributes(controller, attributes);

        selectionIndex = -1;
        setEnableUnselection(attributes.asBoolean("enableUnselection", false));
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(GONE);
    }

    @Override
    public int getVisibility() {
        return GONE;
    }

    public boolean isEnableUnselection() {
        return enableUnselection;
    }

    public void setEnableUnselection(boolean enableUnselection) {
        this.enableUnselection = enableUnselection;
    }

    public int getSelectionIndex() {
        return selectionIndex;
    }

    public void setSelectionIndex(int selectionIndex) {
        selectionIndex = Math.min(toogles.size() - 1, selectionIndex);
        if (this.selectionIndex != selectionIndex) {
            int oldIndex = this.selectionIndex;

            this.selectionIndex = selectionIndex;
            if (oldIndex != -1) {
                ToogleWidget toogle = toogles.get(oldIndex);
                toogle.onSelected(toogle.selected = false);
            }
            if (selectionIndex != -1) {
                ToogleWidget toogle = toogles.get(selectionIndex);
                toogle.onSelected(toogle.selected = true);
            }
        }
    }

    public List<ToogleWidget> toogles() {
        return Collections.unmodifiableList(toogles);
    }

    public void toogleAdd(ToogleWidget widget) {
        widget.setToogleGroup(this);
    }

    public void toogleRemove(ToogleWidget widget) {
        widget.setToogleGroup(null);
    }

    protected void toogleAttach(ToogleWidget toogle) {
        toogles.add(toogle);
        toogle.toogleGroup = this;

        if (toogle.isSelected() || (!enableUnselection && selectionIndex == -1)) {
            toogleSelect(toogle);
        } else {
            toogle.setSelected(false);
        }
    }

    protected void toogleDetach(ToogleWidget toogle) {
        int index = toogles.indexOf(toogle);
        toogles.remove(toogle);
        toogle.toogleGroup = null;
        if (index == selectionIndex) {
            if (toogles.size() == 0 || enableUnselection) {
                selectionIndex = -1;
            } else {
                toogles.get(index == toogles.size() ? index - 1 : index).setSelected(true);
            }
        } else if (index < selectionIndex) {
            selectionIndex --;
        }
    }

    protected void toogleSelect(ToogleWidget toogle) {
        setSelectionIndex(toogles.indexOf(toogle));
    }
}
