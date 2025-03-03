package flat.widget.selection;

import flat.uxml.Controller;
import flat.uxml.UXHash;
import flat.uxml.UXValueListener;
import flat.uxml.value.*;
import flat.widget.enums.Visibility;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class RadioGroupTest {

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);
        var listener = (UXValueListener<Integer>) mock(UXValueListener.class);
        when(controller.getValueListenerMethod("onSelectedWork", Integer.class)).thenReturn(listener);

        RadioGroup radioGroup = new RadioGroup();

        assertEquals(Visibility.GONE, radioGroup.getVisibility());
        assertNull(radioGroup.getSelectedListener());

        radioGroup.setAttributes(createNonDefaultValues(), null);
        radioGroup.applyAttributes(controller);

        assertEquals(Visibility.GONE, radioGroup.getVisibility());
        assertEquals(listener, radioGroup.getSelectedListener());

        radioGroup.applyStyle();

        assertEquals(Visibility.GONE, radioGroup.getVisibility());
        assertEquals(listener, radioGroup.getSelectedListener());
    }

    @Test
    public void selection() {
        RadioGroup radioGroup = new RadioGroup();
        RadioButton radioButton1 = new RadioButton();
        RadioButton radioButton2 = new RadioButton();
        RadioButton radioButton3 = new RadioButton();

        assertEquals(-1, radioGroup.getSelected());

        radioGroup.add(radioButton1);
        assertEquals(-1, radioGroup.getSelected());
        assertEquals(radioGroup, radioButton1.getRadioGroup());

        radioButton1.setActive(true);
        assertEquals(0, radioGroup.getSelected());
        assertTrue(radioButton1.isActive());

        radioButton1.setActive(false);
        assertEquals(-1, radioGroup.getSelected());
        assertFalse(radioButton1.isActive());

        radioButton2.setActive(true);
        radioGroup.add(radioButton2);
        assertEquals(-1, radioGroup.getSelected());
        assertFalse(radioButton1.isActive());
        assertFalse(radioButton2.isActive());
        assertEquals(radioGroup, radioButton1.getRadioGroup());
        assertEquals(radioGroup, radioButton2.getRadioGroup());

        radioButton1.setActive(true);
        assertEquals(0, radioGroup.getSelected());
        assertTrue(radioButton1.isActive());
        assertFalse(radioButton2.isActive());

        radioButton2.setActive(true);
        assertEquals(1, radioGroup.getSelected());
        assertFalse(radioButton1.isActive());
        assertTrue(radioButton2.isActive());

        radioButton2.setActive(false);
        assertEquals(-1, radioGroup.getSelected());
        assertFalse(radioButton1.isActive());
        assertFalse(radioButton2.isActive());

        radioButton1.setActive(true);
        radioGroup.add(radioButton3);
        assertEquals(0, radioGroup.getSelected());
        assertTrue(radioButton1.isActive());
        assertFalse(radioButton2.isActive());
        assertFalse(radioButton3.isActive());
        assertEquals(radioGroup, radioButton1.getRadioGroup());
        assertEquals(radioGroup, radioButton2.getRadioGroup());
        assertEquals(radioGroup, radioButton3.getRadioGroup());

        radioButton2.setActive(true);
        radioGroup.remove(radioButton1);
        assertEquals(0, radioGroup.getSelected());
        assertFalse(radioButton1.isActive());
        assertTrue(radioButton2.isActive());
        assertFalse(radioButton3.isActive());
        assertNull(radioButton1.getRadioGroup());
        assertEquals(radioGroup, radioButton2.getRadioGroup());
        assertEquals(radioGroup, radioButton3.getRadioGroup());

        radioGroup.add(radioButton1);
        assertEquals(0, radioGroup.getSelected());
        assertFalse(radioButton1.isActive());
        assertTrue(radioButton2.isActive());
        assertFalse(radioButton3.isActive());
        assertEquals(radioGroup, radioButton1.getRadioGroup());
        assertEquals(radioGroup, radioButton2.getRadioGroup());
        assertEquals(radioGroup, radioButton3.getRadioGroup());

        radioGroup.remove(radioButton2);
        assertEquals(-1, radioGroup.getSelected());
        assertFalse(radioButton1.isActive());
        assertTrue(radioButton2.isActive());
        assertFalse(radioButton3.isActive());
        assertEquals(radioGroup, radioButton1.getRadioGroup());
        assertNull(radioButton2.getRadioGroup());
        assertEquals(radioGroup, radioButton3.getRadioGroup());

        radioGroup.add(radioButton2);
        assertEquals(-1, radioGroup.getSelected());
        assertFalse(radioButton1.isActive());
        assertFalse(radioButton2.isActive());
        assertFalse(radioButton3.isActive());
        assertEquals(radioGroup, radioButton1.getRadioGroup());
        assertEquals(radioGroup, radioButton2.getRadioGroup());
        assertEquals(radioGroup, radioButton3.getRadioGroup());

        radioGroup.select(0);
        assertEquals(0, radioGroup.getSelected());
        assertFalse(radioButton1.isActive());
        assertFalse(radioButton2.isActive());
        assertTrue(radioButton3.isActive());

        radioGroup.select(-1);
        assertEquals(-1, radioGroup.getSelected());
        assertFalse(radioButton1.isActive());
        assertFalse(radioButton2.isActive());
        assertFalse(radioButton3.isActive());

        radioGroup.select(-1);
        assertEquals(-1, radioGroup.getSelected());
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
        assertEquals(-1, radioGroup1.getSelected());
        assertEquals(radioGroup1, radioButton1.getRadioGroup());

        radioGroup2.add(radioButton1);
        assertEquals(-1, radioGroup1.getSelected());
        assertEquals(-1, radioGroup2.getSelected());
        assertEquals(radioGroup2, radioButton1.getRadioGroup());
        assertEquals(0, radioGroup1.getUnmodifiableRadioButtons().size());
        assertEquals(1, radioGroup2.getUnmodifiableRadioButtons().size());

        radioGroup2.add(radioButton1);
        radioGroup2.select(0);
        assertEquals(-1, radioGroup1.getSelected());
        assertEquals(0, radioGroup2.getSelected());
        assertEquals(0, radioGroup1.getUnmodifiableRadioButtons().size());
        assertEquals(1, radioGroup2.getUnmodifiableRadioButtons().size());

        radioGroup1.add(radioButton1);
        assertEquals(-1, radioGroup1.getSelected());
        assertEquals(-1, radioGroup2.getSelected());
        assertEquals(1, radioGroup1.getUnmodifiableRadioButtons().size());
        assertEquals(0, radioGroup2.getUnmodifiableRadioButtons().size());
    }

    @Test
    public void fireSelectionListener() {
        RadioGroup radioGroup = new RadioGroup();
        RadioButton radioButtonA = new RadioButton();
        RadioButton radioButtonB = new RadioButton();

        var listenerA = (UXValueListener<Boolean>) mock(UXValueListener.class);
        radioButtonA.setActiveListener(listenerA);

        var listenerB = (UXValueListener<Boolean>) mock(UXValueListener.class);
        radioButtonB.setActiveListener(listenerB);

        var listenerC = (UXValueListener<Integer>) mock(UXValueListener.class);
        radioGroup.setSelectedListener(listenerC);

        radioGroup.add(radioButtonA);
        radioGroup.add(radioButtonB);

        radioButtonA.toggle();
        radioButtonB.toggle();
        radioGroup.select(0);
        radioGroup.select(1);

        verify(listenerA, times(4)).handle(any());
        verify(listenerB, times(3)).handle(any());
        verify(listenerC, times(4)).handle(any());
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        hash.put(UXHash.getHash("on-selected-change"), new UXValueText("onSelectedWork"));
        return hash;
    }
}
