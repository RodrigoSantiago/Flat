package flat.widget.value;

import flat.uxml.Controller;
import flat.uxml.UXHash;
import flat.uxml.value.*;
import flat.widget.enums.ProgressLineMode;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mock;

public class ProgressCircleTest {

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);
        ProgressCircle progressCircle = new ProgressCircle();

        assertEquals(ProgressLineMode.REGULAR, progressCircle.getLineMode());
        assertEquals(0, progressCircle.getValue(), 0.001f);
        assertEquals(4, progressCircle.getLineWidth(), 0.001f);
        assertEquals(0xFFFFFFFF, progressCircle.getLineColor());
        assertEquals(0x000000FF, progressCircle.getLineFilledColor());
        assertEquals(0, progressCircle.getSmoothTransitionDuration(), 0.001f);
        assertEquals(2, progressCircle.getAnimationDuration(), 0.001f);

        progressCircle.setAttributes(createNonDefaultValues(), null);
        progressCircle.applyAttributes(controller);

        assertEquals(ProgressLineMode.REGULAR, progressCircle.getLineMode());
        assertEquals(0.5f, progressCircle.getValue(), 0.001f);
        assertEquals(4, progressCircle.getLineWidth(), 0.001f);
        assertEquals(0xFFFFFFFF, progressCircle.getLineColor());
        assertEquals(0x000000FF, progressCircle.getLineFilledColor());
        assertEquals(1, progressCircle.getSmoothTransitionDuration(), 0.001f);
        assertEquals(4, progressCircle.getAnimationDuration(), 0.001f);

        progressCircle.applyStyle();

        assertEquals(ProgressLineMode.SPLIT, progressCircle.getLineMode());
        assertEquals(0.5f, progressCircle.getValue(), 0.001f);
        assertEquals(4, progressCircle.getLineWidth(), 0.001f);
        assertEquals(0x00FFFFFF, progressCircle.getLineColor());
        assertEquals(0x00FF00FF, progressCircle.getLineFilledColor());
        assertEquals(1, progressCircle.getSmoothTransitionDuration(), 0.001f);
        assertEquals(4, progressCircle.getAnimationDuration(), 0.001f);
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();
        hash.put(UXHash.getHash("value"), new UXValueNumber(0.5f));
        hash.put(UXHash.getHash("line-color"), new UXValueColor(0x00FFFFFF));
        hash.put(UXHash.getHash("line-filled-color"), new UXValueColor(0x00FF00FF));
        hash.put(UXHash.getHash("line-width"), new UXValueSizeDp(3));
        hash.put(UXHash.getHash("line-mode"), new UXValueText(ProgressLineMode.SPLIT.toString()));
        hash.put(UXHash.getHash("smooth-transition-duration"), new UXValueNumber(1));
        hash.put(UXHash.getHash("animation-duration"), new UXValueNumber(4));
        return hash;
    }
}