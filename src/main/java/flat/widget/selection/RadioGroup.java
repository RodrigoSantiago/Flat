package flat.widget.selection;

import flat.widget.Widget;
import flat.widget.enums.Visibility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RadioGroup extends Widget {

    private List<RadioButton> radioButtons = new ArrayList<>();
    private List<RadioButton> unmodifiableRadioButtons;
    private RadioButton selectedButton;
    private int selectedIndex = -1;

    public RadioGroup() {
        setVisibility(Visibility.GONE);
    }

    public List<RadioButton> getUnmodifiableRadioButtons() {
        if (unmodifiableRadioButtons == null) {
            unmodifiableRadioButtons = Collections.unmodifiableList(radioButtons);
        }
        return unmodifiableRadioButtons;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void add(RadioButton radioButton) {
        if (!radioButtons.contains(radioButton)) {
            if (radioButton.radioGroup != null) {
                radioButton.radioGroup.remove(radioButton);
            }
            radioButton.radioGroup = this;
            radioButtons.add(radioButton);

            radioButton.setActive(false);
        }
    }

    public void remove(RadioButton radioButton) {
        if (radioButton.radioGroup == this) {
            radioButton.radioGroup = null;
            radioButtons.remove(radioButton);

            if (selectedButton == radioButton) {
                selectedIndex = -1;
                selectedButton = null;
            } else {
                selectedIndex = radioButtons.indexOf(selectedButton);
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
                RadioButton oldSelection = selectedButton;
                selectedIndex = -1;
                selectedButton = null;
                if (oldSelection != null) oldSelection.setActive(false);
            }
        } else {
            if (selectedIndex != index) {
                RadioButton oldSelection = selectedButton;
                selectedIndex = index;
                selectedButton = radioButtons.get(index);
                selectedButton.setActive(true);
                if (oldSelection != null) oldSelection.setActive(false);
            }
        }
    }

    void unselect(RadioButton radioButton) {
        if (selectedButton == radioButton) {
            selectedIndex = -1;
            selectedButton = null;
        }
    }
}
