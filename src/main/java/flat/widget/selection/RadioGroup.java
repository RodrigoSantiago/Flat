package flat.widget.selection;

import flat.uxml.*;
import flat.widget.Gadget;
import flat.widget.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public final class RadioGroup implements Gadget {

    private String id;

    private int selectionIndex = -1;

    private boolean enableEmptySelection;

    ArrayList<RadioButton> radios = new ArrayList<>();

    @Override
    public void setAttributes(HashMap<Integer, UXValue> attributes, String style) {
        //
    }

    @Override
    public void applyAttributes(UXTheme theme, Controller controller, UXBuilder builder) {
        if (theme == null) return;

        /*String id = theme.asString("id");
        if (id != null) {
            this.id = id;
            if (controller != null) {
                controller.assign(id, this);
            }
        }

        setEmptySelectionEnabled(theme.asBool("empty-selection", isEmptySelectionEnabled()));*/
    }

    @Override
    public void applyChildren(UXChildren children) {

    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Widget getWidget() {
        return null;
    }

    public boolean isEmptySelectionEnabled() {
        return enableEmptySelection;
    }

    public void setEmptySelectionEnabled(boolean emptySelectionEnabled) {
        if (this.enableEmptySelection != emptySelectionEnabled) {
            this.enableEmptySelection = emptySelectionEnabled;

            if (selectionIndex == -1 && !this.enableEmptySelection && radios.size() > 0) {
                setSelectionIndex(0);
            }
        }
    }

    public int getSelectionIndex() {
        return selectionIndex;
    }

    public void setSelectionIndex(int selectionIndex) {
        selectionIndex = Math.max(-1, Math.min(radios.size() - 1, selectionIndex));

        if (this.selectionIndex != selectionIndex &&
                (selectionIndex > -1 || enableEmptySelection || radios.size() == 0)) {

            int oldIndex = this.selectionIndex;
            this.selectionIndex = selectionIndex;

            if (oldIndex > -1 && oldIndex < radios.size()) {
                radios.get(oldIndex)._setActivated(false);
            }

            if (selectionIndex > -1) {
                radios.get(selectionIndex)._setActivated(true);
            }
        }
    }

    public List<RadioButton> radiobuttons() {
        return Collections.unmodifiableList(radios);
    }

    public void add(RadioButton radio) {
        if (!radios.contains(radio)) {
            radios.add(radio);
            if (radios.size() == 1 && !enableEmptySelection) {
                setSelectionIndex(0);
            }
        }
    }

    public void remove(RadioButton radio) {
        int index = radios.indexOf(radio);
        if (index > -1) {
            radios.remove(index);
            if (selectionIndex > index) {
                selectionIndex -= 1;
            } else if (selectionIndex == index) {
                if (enableEmptySelection || radios.size() == 0) {
                    selectionIndex = -1;
                } else if (selectionIndex >= radios.size()) {
                    selectionIndex = radios.size() - 1;
                    radios.get(selectionIndex)._setActivated(true);
                } else {
                    radios.get(selectionIndex)._setActivated(true);
                }
            }
        }
    }

    void radioSetActivated(RadioButton radio, boolean activated) {
        int index = radios.indexOf(radio);
        if (index > -1) {
            if (activated) {
                setSelectionIndex(index);
            } else {
                setSelectionIndex(-1);
            }
        }
    }
}
