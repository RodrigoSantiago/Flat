package flat.uxml.sheet;

import flat.graphics.text.FontPosture;
import flat.graphics.text.FontStyle;
import flat.graphics.text.FontWeight;
import flat.uxml.UXValue;
import flat.uxml.value.*;
import flat.widget.Widget;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
public class UXSheetReaderTest {

    @Test
    public void empty() {
        UXSheetReader reader = new UXSheetReader("");
        reader.parse();
        assertEquals(0, reader.getStyles().size());
        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void simpleStyle() {
        UXSheetReader reader = new UXSheetReader("style {}");
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null);

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void parentStyle() {
        UXSheetReader reader = new UXSheetReader("style : parent {}");
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", "parent");

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void simpleVariable() {
        UXSheetReader reader = new UXSheetReader("@variable : 10;");
        reader.parse();

        assertEquals(0, reader.getStyles().size());

        assertVariables(reader.getVariables(), "@variable", new UXValueNumber(10));
        assertLog(reader.getLogs());
    }

    @Test
    public void styleVariable() {
        UXSheetReader reader = new UXSheetReader("@variable : 10; style { color : red; }");
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null,
                "color", new UXValueText("red")
        );

        assertVariables(reader.getVariables(), "@variable", new UXValueNumber(10));
        assertLog(reader.getLogs());
    }

    @Test
    public void styleNumericAttributes() {
        UXSheetReader reader = new UXSheetReader(
                """
                style {
                    number : 10;
                    pixels : 20px;
                    inches : -30in;
                    centimeter : 40cm;
                    milimeter : 50.2mm;
                    picas : 15pc;
                    density : 10dp;
                    angle : 45º;
                    font-size : 15sp;
                }
                """
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null, 
                "number", new UXValueNumber(10),
                "pixels", new UXValueNumber(20),
                "inches", new UXValueSizeIn(-30),
                "centimeter", new UXValueSizeIn(40 * 2.54f),
                "milimeter", new UXValueSizeIn(50.2f * 25.4f),
                "picas", new UXValueSizeIn(15 * 6),
                "density", new UXValueSizeDp(10),
                "angle", new UXValueAngle(45),
                "font-size", new UXValueSizeSp(15)
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void styleTextAttributes() {
        UXSheetReader reader = new UXSheetReader(
                "style { string : \"Text\\n\"; enum : ENUM; locale : $locale; }"
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null, 
                "string", new UXValueText("Text\n"),
                "enum", new UXValueText("ENUM"),
                "locale", new UXValueLocale("$locale")
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void styleFunctionAttributes() {
        UXSheetReader reader = new UXSheetReader(
                "style { url : url(\"path\"); color : rgb(0, 128, 255.0); color-alpha : rgba(0, 128, 255.0, 128); }"
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null, 
                "url", new UXValueResource("path"),
                "color", new UXValueColor(0x0080FFFF),
                "color-alpha", new UXValueColor(0x0080FF80)
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void styleColorAttributes() {
        UXSheetReader reader = new UXSheetReader(
                "style { color : #123456; color-alpha : #FF00FFA1; }"
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null, 
                "color", new UXValueColor(0x123456FF),
                "color-alpha", new UXValueColor(0xFF00FFA1)
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void styleFontAttributes() {
        UXSheetReader reader = new UXSheetReader(
                """
                style {
                    font-family : font("Family");
                    font-italic : font(ITALIC);
                    font-bold : font(BOLD);
                    font-serif : font(SERIF);
                    font-all : font(ITALIC, SERIF, BOLD);
                    font-complex : font("Family", ITALIC, SERIF, 900);
                }
                """
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null, 
                "font-family", new UXValueFont("Family", null, null, null),
                "font-italic", new UXValueFont(null, null, FontPosture.ITALIC, null),
                "font-bold", new UXValueFont(null, FontWeight.BOLD, null, null),
                "font-serif", new UXValueFont(null, null, null, FontStyle.SERIF),
                "font-all", new UXValueFont(null, FontWeight.BOLD, FontPosture.ITALIC, FontStyle.SERIF),
                "font-complex", new UXValueFont("Family", FontWeight.BLACK, FontPosture.ITALIC, FontStyle.SERIF)
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void styleVariableAttributes() {
        UXSheetReader reader = new UXSheetReader(
                "style { variable : @variable; }"
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null, 
                "variable", new UXValueVariable("@variable")
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void styleIgnoreComments() {
        UXSheetReader reader = new UXSheetReader(
                """
                style {
                    color : /*Comment*/ #123456;
                    // Line Comment ignore : me;
                    color-alpha : #FF00FFA1;
                }
                """
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null,
                "color", new UXValueColor(0x123456FF),
                "color-alpha", new UXValueColor(0xFF00FFA1)
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void multipleStyles() {
        UXSheetReader reader = new UXSheetReader(
                """
                style {
                    variable : @variable;
                }
                style-b {
                    number : 100;
                }
                """
        );
        reader.parse();

        assertEquals(2, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null,
                "variable", new UXValueVariable("@variable")
        );
        assertStyle(reader.getStyles(), "style-b", null,
                "number", new UXValueNumber(100)
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void parse_FaledMissingParentName() {
        UXSheetReader reader = new UXSheetReader(
                """
                style : {
                    color : red;
                }
                """
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null,
                "color", new UXValueText("red")
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs(),
                UXSheetReader.ErroLog.NAME_EXPECTED, 0
        );
    }

    @Test
    public void parse_FaledMissingColon() {
        UXSheetReader reader = new UXSheetReader(
                """
                style parent {
                    color : red;
                }
                """
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null,
                "color", new UXValueText("red")
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs(),
                UXSheetReader.ErroLog.UNEXPECTED_TOKEN, 0
        );
    }

    @Test
    public void parse_FaledMissingBrace() {
        UXSheetReader reader = new UXSheetReader(
                """
                style {
                    color : red;
                
                """
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null,
                "color", new UXValueText("red")
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs(),
                UXSheetReader.ErroLog.UNEXPECTED_END_OF_TOKENS, 3
        );
    }

    @Test
    public void parse_FaledMissingSemicolon() {
        UXSheetReader reader = new UXSheetReader(
                """
                style {
                    variable : @variable
                }
                style-b {
                    number : 100;
                }
                """
        );
        reader.parse();

        assertEquals(2, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null,
                "variable", new UXValueVariable("@variable")
        );
        assertStyle(reader.getStyles(), "style-b", null,
                "number", new UXValueNumber(100)
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs(),
                UXSheetReader.ErroLog.UNEXPECTED_END_OF_TOKENS, 1
        );
    }

    @Test
    public void parse_FailedInvalidCharacter() {
        UXSheetReader reader = new UXSheetReader(
                """
                style {
                    variable : ! @variable;
                    color : #FFFFFF;
                }
                style-b {
                    number ! : 100;
                }
                """
        );
        reader.parse();

        assertEquals(2, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null,
                "variable", new UXValueVariable("@variable"),
                "color", new UXValueColor(0xFFFFFFFF)
        );
        assertStyle(reader.getStyles(), "style-b", null,
                "number", new UXValueNumber(100)
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs(),
                UXSheetReader.ErroLog.UNEXPECTED_TOKEN, 1,
                UXSheetReader.ErroLog.UNEXPECTED_TOKEN, 5
        );
    }

    @Test
    public void parse_FailedInvalidFunctions() {
        UXSheetReader reader = new UXSheetReader(
                """
                style {
                    color-on : rgb(255, 255, 255);
                    color-off : rgb(255);
                }
                style-font {
                    font-on : font("Arial");
                    font-off : font(Arial);
                    font-off2 : font(450, 150sp);
                }
                style-rgba {
                    rgba-on : rgba(255, 128, 128, 128);
                    rgba-off : rgba(255, 128, 128, 128dp);
                }
                style-url {
                    url-on : url("path");
                    url-off : url("path", and, something);
                }
                style-function {
                    function-off : function("something");
                    function-incomplete : url("something";
                }
                """
        );
        reader.parse();

        assertEquals(5, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null,
                "color-on", new UXValueColor(0xFFFFFFFF),
                "color-off", new UXValue()
        );
        assertStyle(reader.getStyles(), "style-font", null,
                "font-on", new UXValueFont("Arial", null, null, null),
                "font-off", new UXValueFont(null, null, null, null),
                "font-off2", new UXValueFont(null, null, null, null)
        );
        assertStyle(reader.getStyles(), "style-rgba", null,
                "rgba-on", new UXValueColor(0xFF808080),
                "rgba-off", new UXValue()
        );
        assertStyle(reader.getStyles(), "style-url", null,
                "url-on", new UXValueResource("path"),
                "url-off", new UXValue()
        );
        assertStyle(reader.getStyles(), "style-function", null,
                "function-off", new UXValue(),
                "function-incomplete", new UXValueResource("something")
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs(),
                UXSheetReader.ErroLog.INVALID_COLOR, 2,
                UXSheetReader.ErroLog.INVALID_FONT, 6,
                UXSheetReader.ErroLog.INVALID_FONT, 7,
                UXSheetReader.ErroLog.INVALID_COLOR, 11,
                UXSheetReader.ErroLog.INVALID_URL, 15,
                UXSheetReader.ErroLog.INVALID_FUNCTION, 18,
                UXSheetReader.ErroLog.UNEXPECTED_END_OF_TOKENS, 19
        );
    }

    @Test
    public void parse_FailedInvalidColor() {
        UXSheetReader reader = new UXSheetReader(
                """
                style {
                    color : #FFFFFF;
                    color-off : #FFF;
                }
                """
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null,
                "color", new UXValueColor(0xFFFFFFFF),
                "color-off", new UXValue()
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs(),
                UXSheetReader.ErroLog.INVALID_COLOR, 2
        );
    }

    @Test
    public void matchParent() {
        UXSheetReader reader = new UXSheetReader(
                """
                style {
                    number-m : MATCH_PARENT;
                    number-w : WRAP_CONTENT;
                }
                """
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null,
                "number-m", new UXValueNumber(Widget.MATCH_PARENT),
                "number-w", new UXValueNumber(Widget.WRAP_CONTENT)
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void pseudoClasses() {
        UXSheetReader reader = new UXSheetReader(
                """
                style {
                    color : #FFFFFF;
                    hovered {
                        color : #FF0000;
                    }
                }
                """
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null,
                "color", new UXValueColor(0xFFFFFFFF)
        );
        assertStyle(reader.getStyles().get("style").getStates(), "hovered", null,
                "color", new UXValueColor(0xFF0000FF)
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    public void assertLog(List<UXSheetReader.ErroLog> logs, Object... pair) {
        var list = new ArrayList<>(logs);
        for (int i = 0; i < pair.length; i += 2) {
            String message = (String) pair[i];
            Integer line = (Integer) pair[i + 1];
            boolean found = false;
            for (var log : logs) {
                if (log.getMessage().equals(message) && log.getLine() == line) {
                    found = true;
                    list.remove(log);
                }
            }
            if (!found) {
                fail("Error not found : '" + message + "' at line " + line + " in a total of " + logs.size() );
            }
        }
        if (list.size() > 0) {
            fail("Error not expected : '" + list.get(0).getMessage() + "' at line " + list.get(0).getLine());
        }
    }

    public void assertStyle(HashMap<String, UXSheetStyle> styleMap, String name, String parent, Object... pair) {
        UXSheetStyle style = styleMap.get(name);
        if (style == null) {
            fail("Style not found : " + name);
        }
        assertEquals("Parent unexpected", parent, style.getParent());

        var list = new ArrayList<String>();
        for (int i = 0; i < pair.length; i += 2) {
            String attrName = (String) pair[i];
            UXValue value = (UXValue) pair[i + 1];
            UXSheetAttribute attr = style.getAttributes().get(attrName);
            if (attr == null) {
                fail("Attribute not found : " + attrName);
            }
            assertEquals("Attribute \"" + attrName + "\" with a different value", value, attr.getValue());
            list.add(attrName);
        }
        for (var key : style.getAttributes().keySet()) {
            if (!list.contains(key)) {
                fail("Attribute not expected : " + key);
            }
        }
    }

    public void assertVariables(HashMap<String, UXSheetAttribute> variables, Object... pair) {
        var list = new ArrayList<String>();
        for (int i = 0; i < pair.length; i += 2) {
            String name = (String) pair[i];
            UXValue value = (UXValue) pair[i + 1];
            UXSheetAttribute variable = variables.get(name);
            if (variable == null) {
                fail("Variable not found : " + name);
            }
            assertEquals("Variable \"" + name + "\"with a different value", value, variable.getValue());
            list.add(name);
        }
        for (var key : variables.keySet()) {
            if (!list.contains(key)) {
                fail("Variable not expected : " + key);
            }
        }
    }
}