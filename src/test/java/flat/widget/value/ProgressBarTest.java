package flat.widget.value;

import flat.events.SlideEvent;
import flat.uxml.Controller;
import flat.uxml.UXHash;
import flat.uxml.value.*;
import flat.widget.enums.ProgressLineMode;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mock;

public class ProgressBarTest {

    @Test
    public void properties() {
        Controller controller = mock(Controller.class);
        ProgressBar progressBar = new ProgressBar();

        assertEquals(ProgressLineMode.REGULAR, progressBar.getLineMode());
        assertEquals(0, progressBar.getValue(), 0.001f);
        assertEquals(4, progressBar.getLineWidth(), 0.001f);
        assertEquals(0xFFFFFFFF, progressBar.getLineColor());
        assertEquals(0x000000FF, progressBar.getLineFilledColor());
        assertEquals(0, progressBar.getSmoothTransitionDuration(), 0.001f);
        assertEquals(2, progressBar.getAnimationDuration(), 0.001f);

        progressBar.setAttributes(createNonDefaultValues(), "progress-bar");
        progressBar.applyAttributes(controller);

        assertEquals(ProgressLineMode.REGULAR, progressBar.getLineMode());
        assertEquals(0.5f, progressBar.getValue(), 0.001f);
        assertEquals(4, progressBar.getLineWidth(), 0.001f);
        assertEquals(0xFFFFFFFF, progressBar.getLineColor());
        assertEquals(0x000000FF, progressBar.getLineFilledColor());
        assertEquals(1, progressBar.getSmoothTransitionDuration(), 0.001f);
        assertEquals(4, progressBar.getAnimationDuration(), 0.001f);

        progressBar.applyStyle();

        assertEquals(ProgressLineMode.SPLIT, progressBar.getLineMode());
        assertEquals(0.5f, progressBar.getValue(), 0.001f);
        assertEquals(4, progressBar.getLineWidth(), 0.001f);
        assertEquals(0x00FFFFFF, progressBar.getLineColor());
        assertEquals(0x00FF00FF, progressBar.getLineFilledColor());
        assertEquals(1, progressBar.getSmoothTransitionDuration(), 0.001f);
        assertEquals(4, progressBar.getAnimationDuration(), 0.001f);
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