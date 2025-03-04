package flat.widget;

import flat.graphics.cursor.Cursor;
import flat.uxml.Controller;
import flat.uxml.UXHash;
import flat.uxml.UXTheme;
import flat.uxml.value.*;
import flat.widget.enums.Visibility;
import flat.window.Activity;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class WidgetTest {

    @Test
    public void constructor() {
        UXTheme theme = mock(UXTheme.class);
        Controller controller = mock(Controller.class);

        Widget widget = new Widget();
        widget.setTheme(theme);
        widget.setAttributes(null, null);
        widget.applyAttributes(controller);
        widget.applyStyle();
    }

    @Test
    public void states() {
        UXTheme theme = mock(UXTheme.class);
        Controller controller = mock(Controller.class);

        Activity activity = mock(Activity.class);
        when(activity.getFocus()).thenReturn(null);

        Widget widget = new Widget();
        widget.setTheme(theme);
        widget.setAttributes(null, null);
        widget.applyAttributes(controller);
        widget.applyStyle();
        widget.activity = activity;

        assertFalse(widget.isDisabled());
        assertTrue(widget.isEnabled());
        assertFalse(widget.isActivated());
        assertFalse(widget.isPressed());
        assertFalse(widget.isHovered());
        assertFalse(widget.isDragged());
        assertFalse(widget.isActivated());
        assertFalse(widget.isError());
        assertFalse(widget.isFocused());

        widget.setEnabled(false);
        assertTrue(widget.isDisabled());
        assertFalse(widget.isEnabled());

        widget.setEnabled(true);
        assertFalse(widget.isDisabled());
        assertTrue(widget.isEnabled());

        widget.setActivated(true);
        assertTrue(widget.isActivated());

        widget.setPressed(true);
        assertTrue(widget.isPressed());

        widget.setHovered(true);
        assertTrue(widget.isHovered());

        widget.setDragged(true);
        assertTrue(widget.isDragged());

        widget.setActivated(true);
        assertTrue(widget.isActivated());

        widget.setError(true);
        assertTrue(widget.isError());

        widget.setFocused(true);
        assertFalse(widget.isFocused());

        widget.setFocusable(true);
        widget.setFocused(true);
        assertTrue(widget.isFocused());


        assertFalse(widget.isDisabled());
        assertTrue(widget.isEnabled());
        assertTrue(widget.isActivated());
        assertTrue(widget.isPressed());
        assertTrue(widget.isHovered());
        assertTrue(widget.isDragged());
        assertTrue(widget.isActivated());
        assertTrue(widget.isError());
        assertTrue(widget.isFocused());
    }

    @Test
    public void noDefaultValues() {
        UXTheme theme = mock(UXTheme.class);
        when(theme.getFontScale()).thenReturn(1f);

        Controller controller = mock(Controller.class);

        var hash = createNonDefaultValues();

        Widget widget = new Widget();
        widget.setTheme(theme);
        widget.setAttributes(hash, null);
        widget.applyAttributes(controller);
        widget.applyStyle();

        assertEquals(Visibility.INVISIBLE, widget.getVisibility());
        assertEquals(Cursor.ARROW, widget.getCursor());

        assertEquals(true, widget.isFocusable());
        assertEquals(false, widget.isHandleEventsEnabled());

        assertEquals(160, widget.getPrefWidth(), 0.0001f);
        assertEquals(80, widget.getPrefHeight(), 0.0001f);
        assertEquals(120, widget.getMaxWidth(), 0.0001f);
        assertEquals(150, widget.getMaxHeight(), 0.0001f);
        assertEquals(80, widget.getMinWidth(), 0.0001f);
        assertEquals(40, widget.getMinHeight(), 0.0001f);
        assertEquals(2, widget.getWeight(), 0.0001f);

        assertEquals(16, widget.getTranslateX(), 0.0001f);
        assertEquals(24, widget.getTranslateY(), 0.0001f);
        assertEquals(4, widget.getCenterX(), 0.0001f);
        assertEquals(8, widget.getCenterY(), 0.0001f);
        assertEquals(1.5f, widget.getScaleX(), 0.0001f);
        assertEquals(1.5f, widget.getScaleY(), 0.0001f);

        assertEquals(180, widget.getRotate(), 0.0001f);

        assertEquals(12, widget.getElevation(), 0.0001f);
        assertEquals(true, widget.isShadowEnabled());

        assertEquals(true, widget.isRippleEnabled());
        assertEquals(0xFF0000FF, widget.getRippleColor());
        assertEquals(false, widget.isRippleOverflow());

        assertEquals(1, widget.getMarginTop(), 0.0001f);
        assertEquals(2, widget.getMarginRight(), 0.0001f);
        assertEquals(3, widget.getMarginBottom(), 0.0001f);
        assertEquals(4, widget.getMarginLeft(), 0.0001f);

        assertEquals(5, widget.getPaddingTop(), 0.0001f);
        assertEquals(6, widget.getPaddingRight(), 0.0001f);
        assertEquals(7, widget.getPaddingBottom(), 0.0001f);
        assertEquals(8, widget.getPaddingLeft(), 0.0001f);

        assertEquals(1, widget.getRadiusTop(), 0.0001f);
        assertEquals(2, widget.getRadiusRight(), 0.0001f);
        assertEquals(3, widget.getRadiusBottom(), 0.0001f);
        assertEquals(4, widget.getRadiusLeft(), 0.0001f);

        assertEquals(0x00FF00FF, widget.getBackgroundColor());
        assertEquals(true, widget.isBorderRound());
        assertEquals(0x000000FF, widget.getBorderColor());
        assertEquals(1, widget.getBorderWidth(), 0.0001f);

        assertNoUnfollow(widget, hash);
    }

    @Test
    public void resetValuesAfterStyle() {
        UXTheme theme = mock(UXTheme.class);
        when(theme.getFontScale()).thenReturn(1f);

        Controller controller = mock(Controller.class);

        var hash = createNonDefaultValues();

        Widget widget = new Widget();
        widget.setTheme(theme);
        widget.setAttributes(hash, null);
        widget.applyAttributes(controller);
        widget.applyStyle();

        widget.setVisibility(Visibility.VISIBLE);
        widget.setCursor(Cursor.UNSET);

        widget.setFocusable(false);
        widget.setHandleEventsEnabled(true);

        widget.setPrefWidth(161);
        widget.setPrefHeight(801);
        widget.setMaxWidth(121);
        widget.setMaxHeight(151);
        widget.setMinWidth(801);
        widget.setMinHeight(401);
        widget.setWeight(4);

        widget.setTranslateX(161);
        widget.setTranslateY(241);
        widget.setCenterX(41);
        widget.setCenterY(81);
        widget.setScaleX(1.51f);
        widget.setScaleY(1.51f);

        widget.setRotate(181);

        widget.setElevation(121);
        widget.setShadowEnabled(false);

        widget.setRippleEnabled(false);
        widget.setRippleColor(0xFF1000FF);
        widget.setRippleOverflow(true);

        widget.setMarginTop(11);
        widget.setMarginRight(21);
        widget.setMarginBottom(31);
        widget.setMarginLeft(41);

        widget.setPaddingTop(51);
        widget.setPaddingRight(61);
        widget.setPaddingBottom(71);
        widget.setPaddingLeft(81);

        widget.setRadiusTop(11);
        widget.setRadiusRight(21);
        widget.setRadiusBottom(31);
        widget.setRadiusLeft(41);

        widget.setBackgroundColor(0x10FF00FF);
        widget.setBorderRound(false);
        widget.setBorderColor(0x100000FF);
        widget.setBorderWidth(11);

        assertNoUnfollow(widget, hash);

        widget.applyStyle();

        assertEquals(true, widget.isFocusable());
        assertEquals(false, widget.isHandleEventsEnabled());

        assertEquals(160, widget.getPrefWidth(), 0.0001f);
        assertEquals(80, widget.getPrefHeight(), 0.0001f);
        assertEquals(120, widget.getMaxWidth(), 0.0001f);
        assertEquals(150, widget.getMaxHeight(), 0.0001f);
        assertEquals(80, widget.getMinWidth(), 0.0001f);
        assertEquals(40, widget.getMinHeight(), 0.0001f);
        assertEquals(2, widget.getWeight(), 0.0001f);

        assertEquals(16, widget.getTranslateX(), 0.0001f);
        assertEquals(24, widget.getTranslateY(), 0.0001f);
        assertEquals(4, widget.getCenterX(), 0.0001f);
        assertEquals(8, widget.getCenterY(), 0.0001f);
        assertEquals(1.5f, widget.getScaleX(), 0.0001f);
        assertEquals(1.5f, widget.getScaleY(), 0.0001f);

        assertEquals(180, widget.getRotate(), 0.0001f);

        assertEquals(12, widget.getElevation(), 0.0001f);
        assertEquals(true, widget.isShadowEnabled());

        assertEquals(true, widget.isRippleEnabled());
        assertEquals(0xFF0000FF, widget.getRippleColor());
        assertEquals(false, widget.isRippleOverflow());

        assertEquals(1, widget.getMarginTop(), 0.0001f);
        assertEquals(2, widget.getMarginRight(), 0.0001f);
        assertEquals(3, widget.getMarginBottom(), 0.0001f);
        assertEquals(4, widget.getMarginLeft(), 0.0001f);

        assertEquals(5, widget.getPaddingTop(), 0.0001f);
        assertEquals(6, widget.getPaddingRight(), 0.0001f);
        assertEquals(7, widget.getPaddingBottom(), 0.0001f);
        assertEquals(8, widget.getPaddingLeft(), 0.0001f);

        assertEquals(1, widget.getRadiusTop(), 0.0001f);
        assertEquals(2, widget.getRadiusRight(), 0.0001f);
        assertEquals(3, widget.getRadiusBottom(), 0.0001f);
        assertEquals(4, widget.getRadiusLeft(), 0.0001f);

        assertEquals(0x00FF00FF, widget.getBackgroundColor());
        assertEquals(true, widget.isBorderRound());
        assertEquals(0x000000FF, widget.getBorderColor());
        assertEquals(1, widget.getBorderWidth(), 0.0001f);

        assertNoUnfollow(widget, hash);
    }

    @Test
    public void doNotResetValuesIfUnfollow() {
        UXTheme theme = mock(UXTheme.class);
        when(theme.getFontScale()).thenReturn(1f);

        Controller controller = mock(Controller.class);

        var hash = createNonDefaultValues();

        Widget widget = new Widget();
        widget.setTheme(theme);
        widget.setAttributes(hash, null);
        widget.applyAttributes(controller);
        widget.applyStyle();

        widget.setVisibility(Visibility.VISIBLE);
        widget.setFollowStyleProperty("visibility", false);
        widget.setCursor(Cursor.UNSET);
        widget.setFollowStyleProperty("cursor", false);

        widget.setFocusable(false);
        widget.setFollowStyleProperty("focusable", false);
        widget.setHandleEventsEnabled(true);
        widget.setFollowStyleProperty("handle-events-enabled", false);

        widget.setPrefWidth(161);
        widget.setFollowStyleProperty("width", false);
        widget.setPrefHeight(801);
        widget.setFollowStyleProperty("height", false);
        widget.setMaxWidth(121);
        widget.setFollowStyleProperty("max-width", false);
        widget.setMaxHeight(151);
        widget.setFollowStyleProperty("max-height", false);
        widget.setMinWidth(801);
        widget.setFollowStyleProperty("min-width", false);
        widget.setMinHeight(401);
        widget.setFollowStyleProperty("min-height", false);
        widget.setWeight(4);
        widget.setFollowStyleProperty("weight", false);

        widget.setTranslateX(161);
        widget.setFollowStyleProperty("translate-x", false);
        widget.setTranslateY(241);
        widget.setFollowStyleProperty("translate-y", false);
        widget.setCenterX(41);
        widget.setFollowStyleProperty("center-x", false);
        widget.setCenterY(81);
        widget.setFollowStyleProperty("center-y", false);
        widget.setScaleX(1.51f);
        widget.setFollowStyleProperty("scale-x", false);
        widget.setScaleY(1.51f);
        widget.setFollowStyleProperty("scale-y", false);

        widget.setRotate(181);
        widget.setFollowStyleProperty("rotate", false);

        widget.setElevation(121);
        widget.setFollowStyleProperty("elevation", false);
        widget.setShadowEnabled(false);
        widget.setFollowStyleProperty("shadow-enabled", false);

        widget.setRippleEnabled(false);
        widget.setFollowStyleProperty("ripple-enabled", false);
        widget.setRippleColor(0xFF1000FF);
        widget.setFollowStyleProperty("ripple-color", false);
        widget.setRippleOverflow(true);
        widget.setFollowStyleProperty("ripple-overflow", false);

        widget.setMarginTop(11);
        widget.setFollowStyleProperty("margin-top", false);
        widget.setMarginRight(21);
        widget.setFollowStyleProperty("margin-right", false);
        widget.setMarginBottom(31);
        widget.setFollowStyleProperty("margin-bottom", false);
        widget.setMarginLeft(41);
        widget.setFollowStyleProperty("margin-left", false);

        widget.setPaddingTop(51);
        widget.setFollowStyleProperty("padding-top", false);
        widget.setPaddingRight(61);
        widget.setFollowStyleProperty("padding-right", false);
        widget.setPaddingBottom(71);
        widget.setFollowStyleProperty("padding-bottom", false);
        widget.setPaddingLeft(81);
        widget.setFollowStyleProperty("padding-left", false);

        widget.setRadiusTop(11);
        widget.setFollowStyleProperty("radius-top", false);
        widget.setRadiusRight(21);
        widget.setFollowStyleProperty("radius-right", false);
        widget.setRadiusBottom(31);
        widget.setFollowStyleProperty("radius-bottom", false);
        widget.setRadiusLeft(41);
        widget.setFollowStyleProperty("radius-left", false);

        widget.setBackgroundColor(0x10FF00FF);
        widget.setFollowStyleProperty("background-color", false);
        widget.setBorderRound(false);
        widget.setFollowStyleProperty("border-round", false);
        widget.setBorderColor(0x100000FF);
        widget.setFollowStyleProperty("border-color", false);
        widget.setBorderWidth(11);
        widget.setFollowStyleProperty("border-width", false);

        assertUnfollow(widget, hash);

        widget.applyStyle();

        assertEquals(false, widget.isFocusable());
        assertEquals(true, widget.isHandleEventsEnabled());

        assertEquals(161, widget.getPrefWidth(), 0.0001f);
        assertEquals(801, widget.getPrefHeight(), 0.0001f);
        assertEquals(121, widget.getMaxWidth(), 0.0001f);
        assertEquals(151, widget.getMaxHeight(), 0.0001f);
        assertEquals(801, widget.getMinWidth(), 0.0001f);
        assertEquals(401, widget.getMinHeight(), 0.0001f);
        assertEquals(4, widget.getWeight(), 0.0001f);

        assertEquals(161, widget.getTranslateX(), 0.0001f);
        assertEquals(241, widget.getTranslateY(), 0.0001f);
        assertEquals(41, widget.getCenterX(), 0.0001f);
        assertEquals(81, widget.getCenterY(), 0.0001f);
        assertEquals(1.51f, widget.getScaleX(), 0.0001f);
        assertEquals(1.51f, widget.getScaleY(), 0.0001f);

        assertEquals(181, widget.getRotate(), 0.0001f);

        assertEquals(121, widget.getElevation(), 0.0001f);
        assertEquals(false, widget.isShadowEnabled());

        assertEquals(false, widget.isRippleEnabled());
        assertEquals(0xFF1000FF, widget.getRippleColor());
        assertEquals(true, widget.isRippleOverflow());

        assertEquals(11, widget.getMarginTop(), 0.0001f);
        assertEquals(21, widget.getMarginRight(), 0.0001f);
        assertEquals(31, widget.getMarginBottom(), 0.0001f);
        assertEquals(41, widget.getMarginLeft(), 0.0001f);

        assertEquals(51, widget.getPaddingTop(), 0.0001f);
        assertEquals(61, widget.getPaddingRight(), 0.0001f);
        assertEquals(71, widget.getPaddingBottom(), 0.0001f);
        assertEquals(81, widget.getPaddingLeft(), 0.0001f);

        assertEquals(11, widget.getRadiusTop(), 0.0001f);
        assertEquals(21, widget.getRadiusRight(), 0.0001f);
        assertEquals(31, widget.getRadiusBottom(), 0.0001f);
        assertEquals(41, widget.getRadiusLeft(), 0.0001f);

        assertEquals(0x10FF00FF, widget.getBackgroundColor());
        assertEquals(false, widget.isBorderRound());
        assertEquals(0x100000FF, widget.getBorderColor());
        assertEquals(11, widget.getBorderWidth(), 0.0001f);
    }

    private HashMap<Integer, UXValue> createNonDefaultValues() {
        var hash = new HashMap<Integer, UXValue>();

        hash.put(UXHash.getHash("visibility"), new UXValueText(Visibility.INVISIBLE.toString()));
        hash.put(UXHash.getHash("cursor"), new UXValueText(Cursor.ARROW.toString()));

        hash.put(UXHash.getHash("focusable"), new UXValueBool(true));
        hash.put(UXHash.getHash("handle-events-enabled"), new UXValueBool(false));

        hash.put(UXHash.getHash("width"), new UXValueSizeDp(160));
        hash.put(UXHash.getHash("height"), new UXValueSizeDp(80));
        hash.put(UXHash.getHash("max-width"), new UXValueSizeDp(120));
        hash.put(UXHash.getHash("max-height"), new UXValueSizeDp(150));
        hash.put(UXHash.getHash("min-width"), new UXValueSizeDp(80));
        hash.put(UXHash.getHash("min-height"), new UXValueSizeDp(40));
        hash.put(UXHash.getHash("weight"), new UXValueNumber(2));

        hash.put(UXHash.getHash("translate-x"), new UXValueSizeDp(16));
        hash.put(UXHash.getHash("translate-y"), new UXValueSizeDp(24));
        hash.put(UXHash.getHash("center-x"), new UXValueSizeDp(4));
        hash.put(UXHash.getHash("center-y"), new UXValueSizeDp(8));
        hash.put(UXHash.getHash("scale-x"), new UXValueNumber(1.5f));
        hash.put(UXHash.getHash("scale-y"), new UXValueNumber(1.5f));

        hash.put(UXHash.getHash("rotate"), new UXValueAngle(180));

        hash.put(UXHash.getHash("elevation"), new UXValueSizeDp(12));
        hash.put(UXHash.getHash("shadow-enabled"), new UXValueBool(true));

        hash.put(UXHash.getHash("ripple-enabled"), new UXValueBool(true));
        hash.put(UXHash.getHash("ripple-color"), new UXValueColor(0xFF0000FF));
        hash.put(UXHash.getHash("ripple-overflow"), new UXValueBool(false));

        hash.put(UXHash.getHash("margin-top"), new UXValueSizeDp(1));
        hash.put(UXHash.getHash("margin-right"), new UXValueSizeDp(2));
        hash.put(UXHash.getHash("margin-bottom"), new UXValueSizeDp(3));
        hash.put(UXHash.getHash("margin-left"), new UXValueSizeDp(4));

        hash.put(UXHash.getHash("padding-top"), new UXValueSizeDp(5));
        hash.put(UXHash.getHash("padding-right"), new UXValueSizeDp(6));
        hash.put(UXHash.getHash("padding-bottom"), new UXValueSizeDp(7));
        hash.put(UXHash.getHash("padding-left"), new UXValueSizeDp(8));

        hash.put(UXHash.getHash("radius-top"), new UXValueSizeDp(1));
        hash.put(UXHash.getHash("radius-right"), new UXValueSizeDp(2));
        hash.put(UXHash.getHash("radius-bottom"), new UXValueSizeDp(3));
        hash.put(UXHash.getHash("radius-left"), new UXValueSizeDp(4));

        hash.put(UXHash.getHash("background-color"), new UXValueColor(0x00FF00FF));
        hash.put(UXHash.getHash("border-round"), new UXValueBool(true));
        hash.put(UXHash.getHash("border-color"), new UXValueColor(0x000000FF));
        hash.put(UXHash.getHash("border-width"), new UXValueSizeDp(1));

        return hash;
    }

    private void assertUnfollow(Widget widget, HashMap<Integer, UXValue> hash, String... except) {
        List<String> list = List.of(except);
        for (var key : hash.keySet()) {
            var name = UXHash.findByHash(key);
            assertNotNull("Hash not found", name);
            if (!list.contains(name)) {
                assertTrue("Attribute '" + name + "' should be unfollowed", widget.getAttrs().isUnfollow(name));
            }
        }
    }

    private void assertNoUnfollow(Widget widget, HashMap<Integer, UXValue> hash, String... except) {
        List<String> list = List.of(except);
        for (var key : hash.keySet()) {
            var name = UXHash.findByHash(key);
            assertNotNull("Hash not found", name);
            if (!list.contains(name)) {
                assertFalse("Attribute '" + name + "' should not be unfollowed", widget.getAttrs().isUnfollow(name));
            }
        }
    }
}