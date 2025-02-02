package flat.uxml.value;

import flat.graphics.context.Font;
import flat.graphics.text.Align;
import flat.uxml.Controller;
import flat.uxml.UXTheme;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Font.class})
public class UXValueTest {

    @Test
    public void values() {
        mockStatic(Font.class);

        Font defaultFont = mock(Font.class);
        when(Font.getDefault()).thenReturn(defaultFont);

        UXTheme theme = mock(UXTheme.class);
        Controller controller = mock(Controller.class);

        UXValue value = new UXValue();
        assertFalse(value.isSize());
        assertEquals("", value.asString(theme));
        assertFalse(value.asBool(theme));
        assertEquals(0, value.asNumber(theme), 0);
        assertEquals(0, value.asSize(theme, 160), 0);
        assertEquals(0, value.asAngle(theme), 0);
        assertEquals(0, value.asColor(theme));
        assertEquals(Font.getDefault(), value.asFont(theme));
        assertNull(value.asResource(theme));
        assertNull(value.asConstant(theme, Align.Horizontal.class));
        assertNull(value.asListener(theme, Object.class, controller));
    }
}