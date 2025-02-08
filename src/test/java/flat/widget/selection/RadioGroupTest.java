package flat.widget.selection;

import flat.widget.enums.Visibility;
import org.junit.Test;

import static org.junit.Assert.*;

public class RadioGroupTest {

    @Test
    public void properties() {
        RadioGroup radioGroup = new RadioGroup();
        assertEquals(Visibility.GONE, radioGroup.getVisibility());
    }

    @Test
    public void selection() {
        RadioGroup radioGroup = new RadioGroup();
        RadioButton radioButton1 = new RadioButton();
        RadioButton radioButton2 = new RadioButton();
        RadioButton radioButton3 = new RadioButton();

        assertEquals(-1, radioGroup.getSelectedIndex());

        radioGroup.add(radioButton1);
        assertEquals(-1, radioGroup.getSelectedIndex());
        assertEquals(radioGroup, radioButton1.getRadioGroup());

        radioButton1.setActive(true);
        assertEquals(0, radioGroup.getSelectedIndex());
        assertTrue(radioButton1.isActive());

        radioButton1.setActive(false);
        assertEquals(-1, radioGroup.getSelectedIndex());
        assertFalse(radioButton1.isActive());

        radioButton2.setActive(true);
        radioGroup.add(radioButton2);
        assertEquals(-1, radioGroup.getSelectedIndex());
        assertFalse(radioButton1.isActive());
        assertFalse(radioButton2.isActive());
        assertEquals(radioGroup, radioButton1.getRadioGroup());
        assertEquals(radioGroup, radioButton2.getRadioGroup());

        radioButton1.setActive(true);
        assertEquals(0, radioGroup.getSelectedIndex());
        assertTrue(radioButton1.isActive());
        assertFalse(radioButton2.isActive());

        radioButton2.setActive(true);
        assertEquals(1, radioGroup.getSelectedIndex());
        assertFalse(radioButton1.isActive());
        assertTrue(radioButton2.isActive());

        radioButton2.setActive(false);
        assertEquals(-1, radioGroup.getSelectedIndex());
        assertFalse(radioButton1.isActive());
        assertFalse(radioButton2.isActive());

        radioButton1.setActive(true);
        radioGroup.add(radioButton3);
        assertEquals(0, radioGroup.getSelectedIndex());
        assertTrue(radioButton1.isActive());
        assertFalse(radioButton2.isActive());
        assertFalse(radioButton3.isActive());
        assertEquals(radioGroup, radioButton1.getRadioGroup());
        assertEquals(radioGroup, radioButton2.getRadioGroup());
        assertEquals(radioGroup, radioButton3.getRadioGroup());

        radioButton2.setActive(true);
        radioGroup.remove(radioButton1);
        assertEquals(0, radioGroup.getSelectedIndex());
        assertFalse(radioButton1.isActive());
        assertTrue(radioButton2.isActive());
        assertFalse(radioButton3.isActive());
        assertNull(radioButton1.getRadioGroup());
        assertEquals(radioGroup, radioButton2.getRadioGroup());
        assertEquals(radioGroup, radioButton3.getRadioGroup());

        radioGroup.add(radioButton1);
        assertEquals(0, radioGroup.getSelectedIndex());
        assertFalse(radioButton1.isActive());
        assertTrue(radioButton2.isActive());
        assertFalse(radioButton3.isActive());
        assertEquals(radioGroup, radioButton1.getRadioGroup());
        assertEquals(radioGroup, radioButton2.getRadioGroup());
        assertEquals(radioGroup, radioButton3.getRadioGroup());

        radioGroup.remove(radioButton2);
        assertEquals(-1, radioGroup.getSelectedIndex());
        assertFalse(radioButton1.isActive());
        assertTrue(radioButton2.isActive());
        assertFalse(radioButton3.isActive());
        assertEquals(radioGroup, radioButton1.getRadioGroup());
        assertNull(radioButton2.getRadioGroup());
        assertEquals(radioGroup, radioButton3.getRadioGroup());

        radioGroup.add(radioButton2);
        assertEquals(-1, radioGroup.getSelectedIndex());
        assertFalse(radioButton1.isActive());
        assertFalse(radioButton2.isActive());
        assertFalse(radioButton3.isActive());
        assertEquals(radioGroup, radioButton1.getRadioGroup());
        assertEquals(radioGroup, radioButton2.getRadioGroup());
        assertEquals(radioGroup, radioButton3.getRadioGroup());

        radioGroup.select(0);
        assertEquals(0, radioGroup.getSelectedIndex());
        assertFalse(radioButton1.isActive());
        assertFalse(radioButton2.isActive());
        assertTrue(radioButton3.isActive());

        radioGroup.select(-1);
        assertEquals(-1, radioGroup.getSelectedIndex());
        assertFalse(radioButton1.isActive());
        assertFalse(radioButton2.isActive());
        assertFalse(radioButton3.isActive());

        radioGroup.select(-1);
        assertEquals(-1, radioGroup.getSelectedIndex());
        assertFalse(radioButton1.isActive());
        assertFalse(radioButton2.isActive());
        assertFalse(radioButton3.isActive());
    }

    @Test
    public void transfer() {
        RadioGroup radioGroup1 = new RadioGroup();
        RadioGroup radioGroup2 = new RadioGroup();
        RadioButton radioButton1 = new RadioButton();

        assertNull(radioButton1.getRadioGroup());
        radioGroup1.add(radioButton1);
        assertEquals(-1, radioGroup1.getSelectedIndex());
        assertEquals(radioGroup1, radioButton1.getRadioGroup());

        radioGroup2.add(radioButton1);
        assertEquals(-1, radioGroup1.getSelectedIndex());
        assertEquals(-1, radioGroup2.getSelectedIndex());
        assertEquals(radioGroup2, radioButton1.getRadioGroup());
        assertEquals(0, radioGroup1.getUnmodifiableRadioButtons().size());
        assertEquals(1, radioGroup2.getUnmodifiableRadioButtons().size());

        radioGroup2.add(radioButton1);
        radioGroup2.select(0);
        assertEquals(-1, radioGroup1.getSelectedIndex());
        assertEquals(0, radioGroup2.getSelectedIndex());
        assertEquals(0, radioGroup1.getUnmodifiableRadioButtons().size());
        assertEquals(1, radioGroup2.getUnmodifiableRadioButtons().size());

        radioGroup1.add(radioButton1);
        assertEquals(-1, radioGroup1.getSelectedIndex());
        assertEquals(-1, radioGroup2.getSelectedIndex());
        assertEquals(1, radioGroup1.getUnmodifiableRadioButtons().size());
        assertEquals(0, radioGroup2.getUnmodifiableRadioButtons().size());
    }
}
