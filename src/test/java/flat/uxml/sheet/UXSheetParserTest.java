package flat.uxml.sheet;

import flat.graphics.text.FontPosture;
import flat.graphics.text.FontStyle;
import flat.graphics.text.FontWeight;
import flat.uxml.value.UXValue;
import flat.uxml.value.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
public class UXSheetParserTest {

    @Test
    public void empty() {
        UXSheetParser reader = new UXSheetParser("");
        reader.parse();
        assertEquals(0, reader.getStyles().size());
        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void simpleStyle() {
        UXSheetParser reader = new UXSheetParser("style {}");
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null);

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void parentStyle() {
        UXSheetParser reader = new UXSheetParser("style : parent {}");
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", "parent");

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void simpleVariable() {
        UXSheetParser reader = new UXSheetParser("$variable : 10;");
        reader.parse();

        assertEquals(0, reader.getStyles().size());

        assertVariables(reader.getVariables(), "$variable", new UXValueNumber(10));
        assertLog(reader.getLogs());
    }

    @Test
    public void subStyle() {
        UXSheetParser reader = new UXSheetParser("widget.style {}");
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "widget.style", null);

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void styleVariable() {
        UXSheetParser reader = new UXSheetParser("$variable : 10; style { color : red; }");
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null,
                "color", new UXValueText("red")
        );

        assertVariables(reader.getVariables(), "$variable", new UXValueNumber(10));
        assertLog(reader.getLogs());
    }

    @Test
    public void styleNumericAttributes() {
        UXSheetParser reader = new UXSheetParser(
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
                "centimeter", new UXValueSizeIn(40 / 2.54f),
                "milimeter", new UXValueSizeIn(50.2f / 25.4f),
                "picas", new UXValueSizeIn(15.0f / 6.0f),
                "density", new UXValueSizeDp(10),
                "angle", new UXValueAngle(45),
                "font-size", new UXValueSizeSp(15)
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void styleBooleanAttributes() {
        UXSheetParser reader = new UXSheetParser(
                """
                style {
                    bool-true : true;
                    bool-false : false;
                }
                """
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null,
                "bool-true", new UXValueBool(true),
                "bool-false", new UXValueBool(false)
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void styleTextAttributes() {
        UXSheetParser reader = new UXSheetParser(
                "style { string : \"Text\\n\"; enum : ENUM; locale : @locale; }"
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null, 
                "string", new UXValueText("Text\n"),
                "enum", new UXValueText("ENUM"),
                "locale", new UXValueLocale("@locale")
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void styleFunctionAttributes() {
        UXSheetParser reader = new UXSheetParser(
                "style { font : font(\"family\"); color : rgb(0, 128, 255.0); color-alpha : rgba(0, 128, 255.0, 128); }"
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null, 
                "font", new UXValueFont("family", null, null, null),
                "color", new UXValueColor(0x0080FFFF),
                "color-alpha", new UXValueColor(0x0080FF80)
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void styleColorAttributes() {
        UXSheetParser reader = new UXSheetParser(
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
        UXSheetParser reader = new UXSheetParser(
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
        UXSheetParser reader = new UXSheetParser(
                "style { variable : $variable; }"
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null, 
                "variable", new UXValueVariable("$variable")
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void styleIgnoreComments() {
        UXSheetParser reader = new UXSheetParser(
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
    public void styleIgnoreOpenComments() {
        UXSheetParser reader = new UXSheetParser(
                """
                style {
                    color-alpha : #FF00FFA1;
                }
                /* Open Comment
                """
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null,
                "color-alpha", new UXValueColor(0xFF00FFA1)
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void multipleStyles() {
        UXSheetParser reader = new UXSheetParser(
                """
                style {
                    variable : $variable;
                }
                style-b {
                    number : 100;
                }
                """
        );
        reader.parse();

        assertEquals(2, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null,
                "variable", new UXValueVariable("$variable")
        );
        assertStyle(reader.getStyles(), "style-b", null,
                "number", new UXValueNumber(100)
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void parse_FaledMissingParentName() {
        UXSheetParser reader = new UXSheetParser(
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
                UXSheetParser.ErroLog.NAME_EXPECTED, 0
        );
    }

    @Test
    public void parse_FaledMissingColon() {
        UXSheetParser reader = new UXSheetParser(
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
                UXSheetParser.ErroLog.UNEXPECTED_TOKEN, 0
        );
    }

    @Test
    public void parse_FaledMissingBrace() {
        UXSheetParser reader = new UXSheetParser(
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
                UXSheetParser.ErroLog.UNEXPECTED_END_OF_TOKENS, 3
        );
    }

    @Test
    public void parse_FailedUnexpectedBrace() {
        UXSheetParser reader = new UXSheetParser(
                """
                style {
                    color :
                }
                """
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null,
                "color", new UXValue()
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs(),
                UXSheetParser.ErroLog.UNEXPECTED_END_OF_TOKENS, 1
        );
    }

    @Test
    public void parse_FailedMissingValue() {
        UXSheetParser reader = new UXSheetParser(
                """
                style {
                    color;
                }
                """
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null);

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs(),
                UXSheetParser.ErroLog.UNEXPECTED_TOKEN, 1,
                UXSheetParser.ErroLog.UNEXPECTED_END_OF_TOKENS, 1
        );
    }

    @Test
    public void parse_FailedMissingColonConsecutive() {
        UXSheetParser reader = new UXSheetParser(
                """
                style {
                    color #FFFFFF;
                }
                """
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null);

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs(),
                UXSheetParser.ErroLog.UNEXPECTED_TOKEN, 1,
                UXSheetParser.ErroLog.UNEXPECTED_END_OF_TOKENS, 1,
                UXSheetParser.ErroLog.UNEXPECTED_TOKEN, 1
        );
    }

    @Test
    public void parse_FailedUnexpectedBraceBeforeAttr() {
        UXSheetParser reader = new UXSheetParser(
                """
                style {
                    color
                }
                """
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null);

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs(),
                UXSheetParser.ErroLog.UNEXPECTED_END_OF_TOKENS, 1
        );
    }

    @Test
    public void parse_FaledMissingSemicolon() {
        UXSheetParser reader = new UXSheetParser(
                """
                style {
                    variable : $variable
                }
                style-b {
                    number : 100;
                }
                """
        );
        reader.parse();

        assertEquals(2, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null,
                "variable", new UXValueVariable("$variable")
        );
        assertStyle(reader.getStyles(), "style-b", null,
                "number", new UXValueNumber(100)
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs(),
                UXSheetParser.ErroLog.UNEXPECTED_END_OF_TOKENS, 1
        );
    }

    @Test
    public void parse_FailedInvalidCharacter() {
        UXSheetParser reader = new UXSheetParser(
                """
                style {
                    variable : ! $variable;
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
                "variable", new UXValueVariable("$variable"),
                "color", new UXValueColor(0xFFFFFFFF)
        );
        assertStyle(reader.getStyles(), "style-b", null,
                "number", new UXValueNumber(100)
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs(),
                UXSheetParser.ErroLog.UNEXPECTED_TOKEN, 1,
                UXSheetParser.ErroLog.UNEXPECTED_TOKEN, 5
        );
    }

    @Test
    public void parse_FailedInvalidNumber() {
        UXSheetParser reader = new UXSheetParser(
                """
                style {
                    number : 100pt;
                }
                """
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null,
                "number", new UXValueNumber(100)
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs(),
                UXSheetParser.ErroLog.UNEXPECTED_TOKEN, 1
        );
    }

    @Test
    public void parse_FailedInvalidFunctions() {
        UXSheetParser reader = new UXSheetParser(
                """
                style {
                    color-on : rgb(255, 255, 255);
                    color-off : rgb(#FFFFFF, 255);
                }
                style-font {
                    font-on : font("Time News");
                    font-off : font(Time News);
                    font-off2 : font(450, 150sp);
                }
                style-rgba {
                    rgba-on : rgba(255, 128, 128, 128);
                    rgba-off : rgba(255, 128, 128, 128dp);
                }
                style-function {
                    function-off : function("something");
                    function-incomplete : font("something";
                }
                style-list {
                    list-on : list(1, 2dp, MATCH_PARENT, WRAP_CONTENT, "10", $var, @locale);
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
                "font-on", new UXValueFont("Time News", null, null, null),
                "font-off", new UXValueFont("Time", null, null, null),
                "font-off2", new UXValueFont(null, null, null, null)
        );
        assertStyle(reader.getStyles(), "style-rgba", null,
                "rgba-on", new UXValueColor(0xFF808080),
                "rgba-off", new UXValue()
        );
        assertStyle(reader.getStyles(), "style-function", null,
                "function-off", new UXValue(),
                "function-incomplete", new UXValueFont("something", null, null, null)
        );
        assertStyle(reader.getStyles(), "style-list", null,
                "list-on", new UXValueSizeList("list", new UXValue[] {
                        new UXValueNumber(1), new UXValueSizeDp(2),
                        new UXValueSizeMp(), new UXValueNumber(0),
                        new UXValueText("10"), new UXValueVariable("$var"), new UXValueLocale("@locale")
                })
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs(),
                UXSheetParser.ErroLog.INVALID_COLOR, 2,
                UXSheetParser.ErroLog.UNEXPECTED_TOKEN, 6,
                UXSheetParser.ErroLog.INVALID_FONT, 7,
                UXSheetParser.ErroLog.INVALID_FONT, 7,
                UXSheetParser.ErroLog.INVALID_COLOR, 11,
                UXSheetParser.ErroLog.INVALID_FUNCTION, 14,
                UXSheetParser.ErroLog.UNEXPECTED_END_OF_TOKENS, 15
        );
    }

    @Test
    public void parse_FailedExtraCharacter() {
        UXSheetParser reader = new UXSheetParser("$variable : 10; style { color : red; } #FFFFFF");
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null,
                "color", new UXValueText("red")
        );

        assertVariables(reader.getVariables(), "$variable", new UXValueNumber(10));
        assertLog(reader.getLogs(),
                UXSheetParser.ErroLog.UNEXPECTED_TOKEN, 0
        );
    }

    @Test
    public void parse_FailedNoBody() {
        UXSheetParser reader = new UXSheetParser(
                """
                $variable : 10;
                style
                }
                """);
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null);

        assertVariables(reader.getVariables(), "$variable", new UXValueNumber(10));
        assertLog(reader.getLogs(),
                UXSheetParser.ErroLog.UNEXPECTED_TOKEN, 2,
                UXSheetParser.ErroLog.UNEXPECTED_END_OF_TOKENS, 2
        );
    }

    @Test
    public void parse_FailedInvalidColor() {
        UXSheetParser reader = new UXSheetParser(
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
                UXSheetParser.ErroLog.INVALID_COLOR, 2
        );
    }

    @Test
    public void matchParent() {
        UXSheetParser reader = new UXSheetParser(
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
                "number-m", new UXValueSizeMp(),
                "number-w", new UXValueNumber(0)
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void pseudoClasses() {
        UXSheetParser reader = new UXSheetParser(
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
        var list = reader.getStyles().get(0).getStates().values();
        assertStyle(new ArrayList<>(list), "hovered", null,
                "color", new UXValueColor(0xFF0000FF)
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void include() {
        UXSheetParser reader = new UXSheetParser(
                """
                @include '../class';
                style {
                    color : #FFFFFF;
                    hovered {
                        color : #FF0000;
                    }
                }
                @include 'other';
                """
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null,
                "color", new UXValueColor(0xFFFFFFFF)
        );
        var list = reader.getStyles().get(0).getStates().values();
        assertStyle(new ArrayList<>(list), "hovered", null,
                "color", new UXValueColor(0xFF0000FF)
        );
        var includes = reader.getIncludes();
        assertIncludes(new ArrayList<>(includes), "../class", "other");

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void parse_FailedInvalidInclude() {
        UXSheetParser reader = new UXSheetParser(
                """
                @include '../class';
                style {
                    color : #FFFFFF;
                    hovered {
                        color : #FF0000;
                    }
                }
                @include;
                """
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style", null,
                "color", new UXValueColor(0xFFFFFFFF)
        );
        var list = reader.getStyles().get(0).getStates().values();
        assertStyle(new ArrayList<>(list), "hovered", null,
                "color", new UXValueColor(0xFF0000FF)
        );
        var includes = reader.getIncludes();
        assertIncludes(new ArrayList<>(includes), "../class");

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs(),
                UXSheetParser.ErroLog.UNEXPECTED_TOKEN, 7,
                UXSheetParser.ErroLog.UNEXPECTED_END_OF_TOKENS, 7,
                UXSheetParser.ErroLog.INVALID_INCLUDE, 7
        );
    }

    @Test
    public void listFunctions() {
        UXSheetParser reader = new UXSheetParser(
                """
                style-list {
                    list-on : list(1, 2dp, MATCH_PARENT, WRAP_CONTENT, "10", $var, @locale);
                    list-space : list(1 2dp MATCH_PARENT WRAP_CONTENT "10" $var @locale);
                }
                """
        );
        reader.parse();

        assertEquals(1, reader.getStyles().size());
        assertStyle(reader.getStyles(), "style-list", null,
                "list-on", new UXValueSizeList("list", new UXValue[] {
                        new UXValueNumber(1), new UXValueSizeDp(2),
                        new UXValueSizeMp(), new UXValueNumber(0),
                        new UXValueText("10"), new UXValueVariable("$var"), new UXValueLocale("@locale")
                }),
                "list-space", new UXValueSizeList("list", new UXValue[] {
                        new UXValueNumber(1), new UXValueSizeDp(2),
                        new UXValueSizeMp(), new UXValueNumber(0),
                        new UXValueText("10"), new UXValueVariable("$var"), new UXValueLocale("@locale")
                })
        );

        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void parseXML() {
        UXSheetParser reader = new UXSheetParser("#FFFFFF");
        UXValue result = reader.parseXmlAttribute();

        assertEquals(new UXValueXML("#FFFFFF", new UXValueColor(0xFFFFFFFF)), result);

        assertEquals(0, reader.getStyles().size());
        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void parseXMLText() {
        UXSheetParser reader = new UXSheetParser("#FFFFFA ");
        UXValue result = reader.parseXmlAttribute();

        assertEquals(new UXValueText("#FFFFFA "), result);

        assertEquals(0, reader.getStyles().size());
        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void parseXMLVariable() {
        UXSheetParser reader = new UXSheetParser("$variable");
        UXValue result = reader.parseXmlAttribute();

        assertEquals(new UXValueVariable("$variable"), result);

        assertEquals(0, reader.getStyles().size());
        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void parseXMLLocale() {
        UXSheetParser reader = new UXSheetParser("@locale");
        UXValue result = reader.parseXmlAttribute();

        assertEquals(new UXValueLocale("@locale"), result);

        assertEquals(0, reader.getStyles().size());
        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    @Test
    public void parseXMLFailedFunction() {
        UXSheetParser reader = new UXSheetParser("rgba(failed)");
        UXValue result = reader.parseXmlAttribute();

        assertEquals(new UXValueText("rgba(failed)"), result);

        assertEquals(0, reader.getStyles().size());
        assertEquals(0, reader.getVariables().size());
        assertLog(reader.getLogs());
    }

    public void assertLog(List<UXSheetParser.ErroLog> logs, Object... pair) {
        var list = new ArrayList<>(logs);
        for (int i = 0; i < pair.length; i += 2) {
            String message = (String) pair[i];
            Integer line = (Integer) pair[i + 1];
            boolean found = false;
            for (var log : list) {
                if (log.message().equals(message) && log.line() == line) {
                    found = true;
                    list.remove(log);
                    break;
                }
            }
            if (!found) {
                fail("Error not found : '" + message + "' at line " + line + " in a total of " + logs.size());
            }
        }
        if (list.size() > 0) {
            fail("Error not expected(" + list.size() + ") : '" + list.get(0).message() + "' at line " + list.get(0).line());
        }
    }

    public void assertStyle(List<UXSheetStyle> styleMap, String name, String parent, Object... pair) {
        UXSheetStyle style = null;
        for (var st : styleMap) {
            if (st.getName().equals(name)) {
                style = st;
                break;
            }
        }
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

    public void assertVariables(List<UXSheetAttribute> variables, Object... pair) {
        var list = new ArrayList<String>();
        for (int i = 0; i < pair.length; i += 2) {
            String name = (String) pair[i];
            UXValue value = (UXValue) pair[i + 1];


            UXSheetAttribute variable = null;
            for (var vr : variables) {
                if (vr.getName().equals(name)) {
                    variable = vr;
                    break;
                }
            }
            if (variable == null) {
                fail("Variable not found : " + name);
            }
            assertEquals("Variable \"" + name + "\"with a different value", value, variable.getValue());
            list.add(name);
        }
        for (var key : variables) {
            if (!list.contains(key.getName())) {
                fail("Variable not expected : " + key);
            }
        }
    }

    public void assertIncludes(List<UXSheetAttribute> includes, String... names) {
        var list = new ArrayList<String>();
        for (int i = 0; i < names.length; i ++) {
            String name = names[i];

            UXSheetAttribute variable = null;
            for (var vr : includes) {
                if (name.equals(vr.getValue().asString(null))) {
                    variable = vr;
                    break;
                }
            }
            if (variable == null) {
                fail("Include not found : " + name);
            }
            list.add(name);
        }
        for (var key : includes) {
            String str = key.getValue().asString(null);
            if (str == null) {
                fail("Include not expected : " + str);
            }
            if (!list.contains(str)) {
                fail("Include not expected : " + str);
            }
        }
    }

}