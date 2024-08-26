package flat.uxml;

import flat.animations.StateInfo;
import flat.events.PointerEvent;
import flat.graphics.context.Font;
import flat.graphics.text.Align;
import flat.resources.ResourceStream;
import flat.resources.ResourcesManager;
import flat.uxml.value.*;
import flat.widget.State;
import flat.window.Application;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.*;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Font.class, Application.class})
public class UXAttrsTest {
    private UXTheme theme;
    private UXStyle base;
    private UXStyle nameStyle;

    private int hashPropertyBase;
    private int hashPropertyStyle;
    private int hashPropertyFalse;
    private int hashPropertyAdded;

    private ResourceStream stream;

    private UXValue[] buildStates(UXValue base) {
        return new UXValue[]{base, null, null, null, null, null, null, null};
    }

    @Before
    public void before() {
        hashPropertyBase = UXHash.getHash("property-base");
        hashPropertyStyle = UXHash.getHash("property-style");
        hashPropertyFalse = UXHash.getHash("property-false");
        hashPropertyAdded = UXHash.getHash("property-added");
        theme = mock(UXTheme.class);
        base = mock(UXStyle.class);
        nameStyle = mock(UXStyle.class);
        when(theme.getStyle("test")).thenReturn(base);
        when(theme.getStyle("name")).thenReturn(nameStyle);
        when(theme.getDensity()).thenReturn(160f);
        when(theme.getFontScale()).thenReturn(1f);

        when(base.contains(hashPropertyBase)).thenReturn(true);
        when(nameStyle.contains(hashPropertyStyle)).thenReturn(true);

        when(base.get(hashPropertyBase)).thenReturn(buildStates(new UXValueNumber(55)));
        when(nameStyle.get(hashPropertyStyle)).thenReturn(buildStates(new UXValueNumber(75)));

        when(nameStyle.get(UXHash.getHash("property-constant"))).thenReturn(buildStates(new UXValueText("LEFT")));
        when(nameStyle.get(UXHash.getHash("property-string"))).thenReturn(buildStates(new UXValueText("Text")));
        when(nameStyle.get(UXHash.getHash("property-bool"))).thenReturn(buildStates(new UXValueBool(true)));
        when(nameStyle.get(UXHash.getHash("property-size"))).thenReturn(buildStates(new UXValueSizeDp(24)));
        when(nameStyle.get(UXHash.getHash("property-angle"))).thenReturn(buildStates(new UXValueAngle(180)));
        when(nameStyle.get(UXHash.getHash("property-color"))).thenReturn(buildStates(new UXValueColor(0xFF00FFFF)));
        when(nameStyle.get(UXHash.getHash("property-font"))).thenReturn(buildStates(new UXValueFont(null, null, null, null)));
        when(nameStyle.get(UXHash.getHash("property-resource"))).thenReturn(buildStates(new UXValueResource("default-url")));

        when(nameStyle.get(UXHash.getHash("property-mix"))).thenReturn(
                new UXValue[]{new UXValueSizeDp(24), new UXValueSizeDp(48), null, null, null, null, null, null});
        when(nameStyle.get(UXHash.getHash("property-mix-different"))).thenReturn(
                new UXValue[]{new UXValueSizeDp(24), new UXValueSizeIn(1), null, null, null, null, null, null});

        mockStatic(Font.class);
        Font defaultFont = mock(Font.class);
        when(Font.getDefault()).thenReturn(defaultFont);
        when(Font.findFont(any(), any(), any(), any())).thenReturn(defaultFont);

        mockStatic(Application.class);
        ResourcesManager resourcesManager = mock(ResourcesManager.class);
        when(Application.getResourcesManager()).thenReturn(resourcesManager);
        when(resourcesManager.getResource("default-url")).thenReturn(stream);
    }

    @After
    public void after() {

    }

    @Test
    public void constructor() {
        UXAttrs attrs = new UXAttrs("test");
        attrs.setName("name");
        attrs.setTheme(theme);

        assertEquals(theme, attrs.getTheme());

        assertEquals("test", attrs.getBase());
        assertEquals("name", attrs.getName());

        assertEquals(nameStyle, attrs.getStyle());
        assertEquals(base, attrs.getBaseStyle());
    }

    @Test
    public void contains() {
        UXAttrs attrs = new UXAttrs("test");
        attrs.setName("name");
        attrs.setTheme(theme);

        assertTrue(attrs.contains("property-base"));
        assertTrue(attrs.contains("property-style"));
        assertFalse(attrs.contains("property-false"));
    }

    @Test
    public void containsChange() {
        UXAttrs attrs = new UXAttrs("test");
        attrs.setName("name");
        attrs.setTheme(theme);

        assertFalse(attrs.containsChange((byte) 0b00000001, (byte) 0b00000011));

        verify(base, times(1)).containsChange((byte) 0b00000001, (byte) 0b00000011);
        verify(nameStyle, times(1)).containsChange((byte) 0b00000001, (byte) 0b00000011);
    }

    @Test
    public void containsAttribute() {
        UXAttrs attrs = new UXAttrs("test");
        attrs.setName("name");
        attrs.setTheme(theme);
        attrs.addAttribute("property-added", new UXValueNumber(100));

        assertTrue(attrs.contains("property-base"));
        assertTrue(attrs.contains("property-style"));
        assertTrue(attrs.contains("property-added"));
        assertFalse(attrs.contains("property-false"));

        assertEquals(100, attrs.getNumber("property-added"), 0.0001f);

        attrs.removeAttribute("property-added");

        assertEquals(0, attrs.getNumber("property-added"), 0.0001f);

        HashMap<Integer, UXValue> attributes = new HashMap<>();
        attributes.put(hashPropertyAdded, new UXValueNumber(100));
        attrs.setAttributes(attributes);
        assertEquals(new UXValueNumber(100), attrs.getAttribute("property-added"));
        assertEquals(100, attrs.getNumber("property-added"), 0.0001f);

        attrs.setAttributes(null);
        assertNull(attrs.getAttribute("property-added"));
        assertEquals(0, attrs.getNumber("property-added"), 0.0001f);
    }

    @Test
    public void doNotUnfollowIfValueIsEqual() {
        UXAttrs attrs = new UXAttrs("test");
        attrs.setName("name");
        attrs.setTheme(theme);
        attrs.addAttribute("property-added", new UXValueNumber(100));
        attrs.addAttribute("property-added-bool", new UXValueBool(true));
        attrs.addAttribute("property-added-int", new UXValueColor(0xFF00FFFF));
        attrs.addAttribute("property-added-object", new UXValueText("Text"));

        assertTrue(attrs.contains("property-base"));
        assertTrue(attrs.contains("property-style"));
        assertTrue(attrs.contains("property-added"));
        assertFalse(attrs.contains("property-false"));

        // -- Number
        attrs.getNumber("property-added");
        attrs.checkSetUnfollow("property-added", 100f);

        assertEquals(100, attrs.getNumber("property-added"), 0.0001f);
        assertFalse(attrs.isUnfollow("property-added"));

        attrs.checkSetUnfollow("property-added", 100f);
        attrs.checkSetUnfollow("property-added", 100f);

        assertEquals(0, attrs.getNumber("property-added"), 0.0001f);
        assertTrue(attrs.isUnfollow("property-added"));

        // -- bool
        attrs.getBool("property-added-bool");
        attrs.checkSetUnfollow("property-added-bool", true);

        assertTrue(attrs.getBool("property-added-bool"));
        assertFalse(attrs.isUnfollow("property-added-bool"));

        attrs.checkSetUnfollow("property-added-bool", true);
        attrs.checkSetUnfollow("property-added-bool", true);

        assertFalse(attrs.getBool("property-added-bool"));
        assertTrue(attrs.isUnfollow("property-added-bool"));

        // -- int
        attrs.getColor("property-added-int");
        attrs.checkSetUnfollow("property-added-int", 0xFF00FFFF);

        assertEquals(0xFF00FFFF, attrs.getColor("property-added-int"));
        assertFalse(attrs.isUnfollow("property-added-int"));

        attrs.checkSetUnfollow("property-added-int", 0xFF00FFFF);
        attrs.checkSetUnfollow("property-added-int", 0xFF00FFFF);

        assertEquals(0, attrs.getColor("property-added-int"));
        assertTrue(attrs.isUnfollow("property-added-int"));

        // -- object
        attrs.getString("property-added-object");
        attrs.checkSetUnfollow("property-added-object", "Text");

        assertEquals("Text", attrs.getString("property-added-object"));
        assertFalse(attrs.isUnfollow("property-added-object"));

        attrs.checkSetUnfollow("property-added-object", "Text");
        attrs.checkSetUnfollow("property-added-object", "Text");

        assertEquals(null, attrs.getString("property-added-object"));
        assertTrue(attrs.isUnfollow("property-added-object"));

        // --
        attrs.clearUnfollow("property-added");
        attrs.clearUnfollow("property-added-bool");
        attrs.clearUnfollow("property-added-int");
        attrs.clearUnfollow("property-added-object");

        assertEquals(100, attrs.getNumber("property-added"), 0.0001f);
        assertFalse(attrs.isUnfollow("property-added"));
        assertTrue(attrs.getBool("property-added-bool"));
        assertFalse(attrs.isUnfollow("property-added-bool"));
        assertEquals(0xFF00FFFF, attrs.getColor("property-added-int"));
        assertFalse(attrs.isUnfollow("property-added-int"));
        assertEquals("Text", attrs.getString("property-added-object"));
        assertFalse(attrs.isUnfollow("property-added-object"));
    }

    @Test
    public void getAttributeValues() {
        UXAttrs attrs = new UXAttrs("test");
        attrs.setName("name");
        attrs.setTheme(theme);
        attrs.addAttribute("property-added", new UXValueNumber(100));

        ResourceStream stream = mock(ResourceStream.class);

        assertEquals(100, attrs.getAttributeNumber("property-added", 0), 0.0001f);
        assertEquals(0, attrs.getAttributeNumber("property-base", 0), 0.0001f);
        assertEquals("Default", attrs.getAttributeString("property-string", "Default"));
        assertEquals(Align.Horizontal.LEFT, attrs.getAttributeConstant("property-constant", Align.Horizontal.LEFT));
        assertFalse(attrs.getAttributeBool("property-bool", false));
        assertEquals(0, attrs.getAttributeSize("property-size", 0), 0.0001f);
        assertEquals(360, attrs.getAttributeAngle("property-size", 360), 0.0001f);
        assertEquals(0xFF00FFFF, attrs.getAttributeColor("property-color", 0xFF00FFFF), 0.0001f);
        assertEquals(Font.getDefault(), attrs.getAttributeFont("property-font", Font.getDefault()));
        assertEquals(stream, attrs.getAttributeResource("property-resource", stream));
    }

    @Test
    public void getStyleValues() {
        UXAttrs attrs = new UXAttrs("test");
        attrs.setName("name");
        attrs.setTheme(theme);
        attrs.addAttribute("property-added", new UXValueNumber(100));

        StateInfo stateInfo = mock(StateInfo.class);
        when(stateInfo.get(any())).thenReturn(0f);
        when(stateInfo.get(State.ENABLED)).thenReturn(1f);

        assertEquals(100, attrs.getNumber("property-added"), 0.0001f);
        assertEquals(55, attrs.getNumber("property-base"), 0.0001f);
        assertEquals("Text", attrs.getString("property-string"));
        assertEquals(Align.Horizontal.LEFT, attrs.getConstant("property-constant", Align.Horizontal.LEFT));
        assertTrue(attrs.getBool("property-bool"));
        assertEquals(24, attrs.getSize("property-size"), 0.0001f);
        assertEquals(180, attrs.getAngle("property-angle"), 0.0001f);
        assertEquals(0xFF00FFFF, attrs.getColor("property-color"), 0.0001f);
        assertEquals(Font.getDefault(), attrs.getFont("property-font"));
        assertEquals(stream, attrs.getResource("property-resource"));

        assertEquals("Default", attrs.getString("property-string-invalid", stateInfo, "Default"));
        assertEquals(Align.Horizontal.LEFT, attrs.getConstant("property-constant-invalid", stateInfo, Align.Horizontal.LEFT));
        assertFalse(attrs.getBool("property-bool-invalid", stateInfo, false));
        assertEquals(0, attrs.getSize("property-size-invalid", stateInfo, 0), 0.0001f);
        assertEquals(360, attrs.getAngle("property-size-invalid", stateInfo, 360), 0.0001f);
        assertEquals(0xFF00FFFF, attrs.getColor("property-color-invalid", stateInfo, 0xFF00FFFF), 0.0001f);
        assertNull(attrs.getFont("property-font-invalid", stateInfo, null));
        assertNull(attrs.getResource("property-resource-invalid", stateInfo, null));
    }

    @Test
    public void mixStyleValues() {
        UXAttrs attrs = new UXAttrs("test");
        attrs.setName("name");
        attrs.setTheme(theme);

        StateInfo stateInfo = mock(StateInfo.class);
        when(stateInfo.get(any())).thenReturn(0f);
        when(stateInfo.get(State.ENABLED)).thenReturn(1f);
        when(stateInfo.get(State.FOCUSED)).thenReturn(0.5f);

        assertEquals(36, attrs.getSize("property-mix", stateInfo, 0), 0.0001f);
    }

    @Test
    public void mixDifferentValues() {
        UXAttrs attrs = new UXAttrs("test");
        attrs.setName("name");
        attrs.setTheme(theme);

        StateInfo stateInfo = mock(StateInfo.class);
        when(stateInfo.get(any())).thenReturn(0f);
        when(stateInfo.get(State.ENABLED)).thenReturn(1f);
        when(stateInfo.get(State.FOCUSED)).thenReturn(0.5f);

        assertEquals(24, attrs.getSize("property-mix-different"), 0.0001f);
        assertEquals(92, attrs.getSize("property-mix-different", stateInfo, 0), 0.0001f);
    }

    @Test
    public void linkIds() {
        UXAttrs attrs = new UXAttrs("test");
        attrs.setName("name");
        attrs.setTheme(theme);
        attrs.addAttribute("item-id", new UXValueText("item"));

        UXBuilder builder = mock(UXBuilder.class);
        UXGadgetLinker linker = mock(UXGadgetLinker.class);
        attrs.link("item-id", builder, linker);

        verify(builder, times(1)).addLink("item", linker);
    }

    @Test
    public void getListeners() {
        UXAttrs attrs = new UXAttrs("test");
        attrs.setName("name");
        attrs.setTheme(theme);
        attrs.addAttribute("on-click", new UXValueText("method"));

        Controller controller = mock(Controller.class);
        UXListener<PointerEvent> listener = mock(UXListener.class);
        when(controller.getListenerMethod("method", PointerEvent.class)).thenReturn(listener);

        UXListener<PointerEvent> result = attrs.getAttributeListener("on-click", PointerEvent.class, controller);
        UXListener<PointerEvent> fallback = attrs.getAttributeListener("on-hover", PointerEvent.class, controller);

        assertEquals(listener, result);
        assertNull(fallback);

        verify(controller, times(1)).getListenerMethod("method", PointerEvent.class);
    }

}