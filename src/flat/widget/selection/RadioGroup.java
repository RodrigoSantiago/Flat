package flat.widget.selection;

import flat.uxml.Controller;
import flat.uxml.UXChildren;
import flat.uxml.UXStyleAttrs;
import flat.widget.Gadget;
import flat.widget.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RadioGroup implements Gadget {

    private String id;

    private int selectionIndex = -1;

    private boolean enableEmptySelection;

    ArrayList<RadioButton> radios = new ArrayList<>();

    @Override
    public void applyAttributes(UXStyleAttrs style, Controller controller) {
        if (style == null) return;

        String id = style.asString("id");
        if (id != null) {
            this.id = id;
            if (controller != null) {
                controller.assign(id, this);
            }
        }

        setEmptySelectionEnabled(style.asBool("empty-selection", isEmptySelectionEnabled()));
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

            if (!this.enableEmptySelection && radios.size() > 0) {
                radioSelect(radios.get(0));
            }
        }
    }

    public int getSelectionIndex() {
        return selectionIndex;
    }

    public void setSelectionIndex(int selectionIndex) {
        selectionIndex = Math.min(radios.size() - 1, selectionIndex);
        if (this.selectionIndex != selectionIndex) {
            int oldIndex = this.selectionIndex;

            this.selectionIndex = selectionIndex;
            if (oldIndex != -1) {
                RadioButton toggle = radios.get(oldIndex);
                toggle.setActivated(false);
            }
            if (selectionIndex != -1) {
                RadioButton toggle = radios.get(selectionIndex);
                toggle.setActivated(true);
            }
        }
    }

    public List<RadioButton> radiobuttons() {
        return Collections.unmodifiableList(radios);
    }

    public void radioAdd(RadioButton toggle) {
        if (toggle.getRadioGroup() != this) {
            toggle.setRadioGroup(this);
        } else {
            int index = radios.indexOf(toggle);
            if (index == - 1) {
                radios.add(toggle);

                if (toggle.isActivated() || (!enableEmptySelection && selectionIndex == -1)) {
                    radioSelect(toggle);
                } else {
                    toggle.setActivated(false);
                }
            }
        }
    }

    public void radioRemove(RadioButton toggle) {
        if (toggle.getRadioGroup() == this) {
            toggle.setRadioGroup(null);
        } else {
            int index = radios.indexOf(toggle);
            if (index > - 1) {
                radios.remove(toggle);
                if (index == selectionIndex) {
                    if (radios.size() == 0 || enableEmptySelection) {
                        selectionIndex = -1;
                    } else {
                        radios.get(index == radios.size() ? index - 1 : index).setActivated(true);
                    }
                } else if (index < selectionIndex) {
                    selectionIndex--;
                }
            }
        }
    }

    public int radioIndex(RadioButton widget) {
        return radios.indexOf(widget);
    }

    public void radioSelect(RadioButton toggle) {
        setSelectionIndex(radios.indexOf(toggle));
    }
}
