package flat.widget.selection;

import flat.uxml.Controller;
import flat.uxml.UXAttrs;
import flat.uxml.UXValueListener;
import flat.uxml.ValueChange;
import flat.widget.Widget;
import flat.widget.enums.Visibility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RadioGroup extends Widget {

    private UXValueListener<Integer> selectedListener;
    private final List<RadioButton> radioButtons = new ArrayList<>();
    private List<RadioButton> unmodifiableRadioButtons;
    private RadioButton selectedButton;
    private int selectedIndex = -1;

    public RadioGroup() {
        setVisibility(Visibility.GONE);
    }

    @Override
    public void applyAttributes(Controller controller) {
        super.applyAttributes(controller);

        UXAttrs attrs = getAttrs();
        setSelectedListener(attrs.getAttributeValueListener("on-selected-change", Integer.class, controller, getSelectedListener()));
    }

    public List<RadioButton> getUnmodifiableRadioButtons() {
        if (unmodifiableRadioButtons == null) {
            unmodifiableRadioButtons = Collections.unmodifiableList(radioButtons);
        }
        return unmodifiableRadioButtons;
    }

    public int getSelected() {
        return selectedIndex;
    }

    public void add(RadioButton... radioButton) {
        for (var btn : radioButton) {
            add(btn);
        }
    }

    public void add(List<RadioButton> radioButton) {
        for (var btn : radioButton) {
            add(btn);
        }
    }

    public void add(RadioButton radioButton) {
        if (!radioButtons.contains(radioButton)) {
            if (radioButton.radioGroup != null) {
                radioButton.radioGroup.remove(radioButton);
            }
            radioButton.radioGroup = this;
            radioButtons.add(radioButton);

            radioButton.setActivated(false);
        }
    }

    public void remove(RadioButton radioButton) {
        if (radioButton.radioGroup == this) {
            radioButton.radioGroup = null;
            radioButtons.remove(radioButton);

            if (selectedButton == radioButton) {
                int oldValue = selectedIndex;
                selectedIndex = -1;
                selectedButton = null;
                fireSelectedListener(oldValue);
            } else {
                int oldValue = selectedIndex;
                selectedIndex = radioButtons.indexOf(selectedButton);
                fireSelectedListener(oldValue);
            }
        }
    }

    public void select(RadioButton radioButton) {
        int index = radioButtons.indexOf(radioButton);
        if (index >= 0) {
            select(index);
        }
    }

    public void select(int index) {
        if (index < 0 || index >= radioButtons.size()) {
            if (selectedIndex != -1) {
                int oldValue = selectedIndex;
                RadioButton oldSelection = selectedButton;
                selectedIndex = -1;
                selectedButton = null;
                if (oldSelection != null) oldSelection.setActivated(false);
                fireSelectedListener(oldValue);
            }
        } else {
            if (selectedIndex != index) {
                int oldValue = selectedIndex;
                RadioButton oldSelection = selectedButton;
                selectedIndex = index;
                selectedButton = radioButtons.get(index);
                selectedButton.setActivated(true);
                if (oldSelection != null) oldSelection.setActivated(false);
                fireSelectedListener(oldValue);
            }
        }
    }

    void unselect(RadioButton radioButton) {
        if (selectedButton == radioButton) {
            int oldValue = selectedIndex;
            selectedIndex = -1;
            selectedButton = null;
            fireSelectedListener(oldValue);
        }
    }

    public void setSelectedListener(UXValueListener<Integer> selectedListener) {
        this.selectedListener = selectedListener;
    }

    public UXValueListener<Integer> getSelectedListener() {
        return selectedListener;
    }

    private void fireSelectedListener(int oldValue) {
        if (selectedListener != null && oldValue != selectedIndex) {
            UXValueListener.safeHandle(selectedListener, new ValueChange<>(this, oldValue, selectedIndex));
        }
    }
}
