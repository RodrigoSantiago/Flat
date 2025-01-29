package flat.uxml.node;

import flat.uxml.value.UXValue;
import flat.uxml.value.UXValueText;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
public class UXNodeParserTest {

    @Test
    public void empty() {
        UXNodeParser parser = new UXNodeParser("");
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertNull(result);
        assertLog(parser.getLogs());
    }

    @Test
    public void simpleTag() {
        UXNodeParser parser = new UXNodeParser("<button></button>");
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button");
        assertChild(result);
        assertLog(parser.getLogs());
    }

    @Test
    public void simpleAttribute() {
        UXNodeParser parser = new UXNodeParser("<button attr=\"value\"></button>");
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button",
                "attr", new UXValueText("value")
        );
        assertChild(result);
        assertLog(parser.getLogs());
    }

    @Test
    public void complexAttribute() {
        UXNodeParser parser = new UXNodeParser("<button attr=\"value&gt;&lt;&amp;&quot;&apos;&#64;&#x128;\\n\"></button>");
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button",
                "attr", new UXValueText("value><&\"'\u0040\u0128\\n")
        );
        assertChild(result);
        assertLog(parser.getLogs());
    }

    @Test
    public void blankAttribute() {
        UXNodeParser parser = new UXNodeParser("<button attr=\"\"></button>");
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button",
                "attr", new UXValueText("")
        );
        assertChild(result);
        assertLog(parser.getLogs());
    }

    @Test
    public void parse_FailMalformedString() {
        UXNodeParser parser = new UXNodeParser(
                """
                <button
                    attr="&any;"
                    attr2="&00000000064;"
                    attr3="&lt">
                </button>
                """);
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button",
                "attr", new UXValueText(""),
                "attr2", new UXValueText(""),
                "attr3", new UXValueText("")
        );
        assertChild(result);
        assertLog(parser.getLogs(),
                UXNodeParser.ErroLog.MALFORMED_STRING, 1,
                UXNodeParser.ErroLog.MALFORMED_STRING, 2,
                UXNodeParser.ErroLog.MALFORMED_STRING, 3
        );
    }

    @Test
    public void parse_FailMissingEndQuote() {
        UXNodeParser parser = new UXNodeParser(
                """
                <button attr=\">
                </button>
                """);
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button",
                "attr", new UXValueText("")
        );
        assertChild(result);
        assertLog(parser.getLogs(),
                UXNodeParser.ErroLog.MALFORMED_STRING, 0,
                UXNodeParser.ErroLog.UNEXPECTED_END_OF_TOKENS, 2,
                UXNodeParser.ErroLog.MISSING_TAG_CLOSURE, 2
        );
    }

    @Test
    public void emptyAttribute() {
        UXNodeParser parser = new UXNodeParser("<button attr></button>");
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button",
                "attr", null
        );
        assertChild(result);
        assertLog(parser.getLogs());
    }

    @Test
    public void multipleAttribute() {
        UXNodeParser parser = new UXNodeParser("<button attr value=\"value\"></button>");
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button",
                "attr", null,
                "value", new UXValueText("value")
        );
        assertChild(result);
        assertLog(parser.getLogs());
    }

    @Test
    public void multipleAttribute2() {
        UXNodeParser parser = new UXNodeParser("<button value=\"value\" attr></button>");
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button",
                "value", new UXValueText("value"),
                "attr", null
        );
        assertChild(result);
        assertLog(parser.getLogs());
    }

    @Test
    public void selfClosure() {
        UXNodeParser parser = new UXNodeParser("<button value=\"value\" attr/>");
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button",
                "value", new UXValueText("value"),
                "attr", null
        );
        assertChild(result);
        assertLog(parser.getLogs());
    }

    @Test
    public void parse_FailSelfDoubleClosure() {
        UXNodeParser parser = new UXNodeParser("<button value=\"value\" attr//>");
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button",
                "value", new UXValueText("value"),
                "attr", null
        );
        assertChild(result);
        assertLog(parser.getLogs(),
                UXNodeParser.ErroLog.UNEXPECTED_TOKEN, 0
        );
    }

    @Test
    public void parse_FailDoubleClosure() {
        UXNodeParser parser = new UXNodeParser(
                """
                <button value=\"value\" attr>
                </button/>
                """);
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button",
                "value", new UXValueText("value"),
                "attr", null
        );
        assertChild(result);
        assertLog(parser.getLogs(),
                UXNodeParser.ErroLog.UNEXPECTED_TOKEN, 1
        );
    }

    @Test
    public void parse_FailDoubleClosureTag() {
        UXNodeParser parser = new UXNodeParser(
                """
                <button value=\"value\" attr>
                <//button>
                """);
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button",
                "value", new UXValueText("value"),
                "attr", null
        );
        assertChild(result);
        assertLog(parser.getLogs(),
                UXNodeParser.ErroLog.UNEXPECTED_TOKEN, 1
        );
    }
    @Test
    public void children() {
        UXNodeParser parser = new UXNodeParser("<button value=\"value\" attr><text/></button>");
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button",
                "value", new UXValueText("value"),
                "attr", null
        );
        assertChild(result, "text");
        assertLog(parser.getLogs());
    }

    @Test
    public void children2() {
        UXNodeParser parser = new UXNodeParser("<button value=\"value\" attr><text></text></button>");
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button",
                "value", new UXValueText("value"),
                "attr", null
        );
        assertChild(result, "text");
        assertLog(parser.getLogs());
    }

    @Test
    public void siblings() {
        UXNodeParser parser = new UXNodeParser("<button value=\"value\" attr><text></text><label></label></button>");
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button",
                "value", new UXValueText("value"),
                "attr", null
        );
        assertChild(result, "text", "label");
        assertLog(parser.getLogs());
    }

    @Test
    public void grandson() {
        UXNodeParser parser = new UXNodeParser("<button value=\"value\" attr><text attr><label></label></text></button>");
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button",
                "value", new UXValueText("value"),
                "attr", null
        );
        assertChild(result, "text");
        assertChild(result.getChildren().get(0), "label");
        assertElement(result.getChildren().get(0), "text",
                "attr", null
        );
        assertLog(parser.getLogs());
    }

    @Test
    public void sameNameFamily() {
        UXNodeParser parser = new UXNodeParser("<button value=\"valueA\"><button value=\"valueB\"></button><button value=\"valueC\"></button></button>");
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button",
                "value", new UXValueText("valueA")
        );
        assertChild(result, "button", "button");
        assertLog(parser.getLogs());
    }

    @Test
    public void ignoreComment() {
        UXNodeParser parser = new UXNodeParser(
                """
                <button value="value" attr>
                    <text></text>
                    <!-- Just a Comment -->
                </button>
                """);
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button",
                "value", new UXValueText("value"),
                "attr", null
        );
        assertChild(result, "text");
        assertLog(parser.getLogs());
    }

    @Test
    public void parse_FailInvalidCharacter() {
        UXNodeParser parser = new UXNodeParser(
                """
                <button value=\"value\" attr ¨>
                </button>
                """);
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button",
                "value", new UXValueText("value"),
                "attr", null
        );
        assertChild(result);
        assertLog(parser.getLogs(),
                UXNodeParser.ErroLog.UNEXPECTED_TOKEN, 0
        );
    }

    @Test
    public void parse_FailMissingClosure() {
        UXNodeParser parser = new UXNodeParser("<button>");
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button");
        assertChild(result);
        assertLog(parser.getLogs(),
                UXNodeParser.ErroLog.MISSING_TAG_CLOSURE, 0
        );
    }

    @Test
    public void parse_FailMissingChildClosure() {
        UXNodeParser parser = new UXNodeParser(
                """
                 <button>
                    <text>
                 </button>
                 """);
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button");
        assertChild(result, "text");
        assertLog(parser.getLogs(),
                UXNodeParser.ErroLog.INVALID_CLOSE_TAG, 2,
                UXNodeParser.ErroLog.MISSING_TAG_CLOSURE, 3
        );
    }

    @Test
    public void parse_FailMissingDoubleClosure() {
        UXNodeParser parser = new UXNodeParser(
                """
                 <button>
                    <text>
                 """);
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button");
        assertChild(result, "text");
        assertLog(parser.getLogs(),
                UXNodeParser.ErroLog.MISSING_TAG_CLOSURE, 2
        );
    }

    @Test
    public void parse_FailIncompleteAttribute() {
        UXNodeParser parser = new UXNodeParser(
                """
                 <button attr=>
                    <text/>
                 </button>
                 """);
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button",
                "attr", null
        );
        assertChild(result, "text");
        assertLog(parser.getLogs(),
                UXNodeParser.ErroLog.MISSING_VALUE, 0
        );
    }

    @Test
    public void parse_FailMissingQuotes() {
        UXNodeParser parser = new UXNodeParser(
                """
                 <button attr=text>
                    <text/>
                 </button>
                 """);
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button",
                "attr", null
        );
        assertChild(result, "text");
        assertLog(parser.getLogs(),
                UXNodeParser.ErroLog.UNEXPECTED_TOKEN, 0,
                UXNodeParser.ErroLog.MISSING_VALUE, 0
        );
    }

    @Test
    public void parse_FailMissingQuotesChild() {
        UXNodeParser parser = new UXNodeParser(
                """
                 <button>
                    <text attr=text/>
                 </button>
                 """);
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button");
        assertElement(result.getChildren().get(0), "text",
                "attr", null
        );
        assertChild(result, "text");
        assertChild(result.getChildren().get(0));
        assertLog(parser.getLogs(),
                UXNodeParser.ErroLog.UNEXPECTED_TOKEN, 1,
                UXNodeParser.ErroLog.MISSING_VALUE, 1
        );
    }

    @Test
    public void contentValue() {
        UXNodeParser parser = new UXNodeParser(
                """
                 <button>
                     Content
                 </button>
                 """);
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button");
        assertEquals("Invalid content for XML element", "\n    Content\n", result.getContent());
        assertChild(result);
        assertLog(parser.getLogs());
    }

    @Test
    public void contentComplexValue() {
        UXNodeParser parser = new UXNodeParser(
                """
                 <button>
                     Content&#x128;
                 </button>
                 """);
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button");
        assertEquals("Invalid content for XML element", "\n    Content\u0128\n", result.getContent());
        assertChild(result);
        assertLog(parser.getLogs());
    }

    @Test
    public void multipleContentValue() {
        UXNodeParser parser = new UXNodeParser(
                """
                 <button>
                     ContentA
                     <text/>
                     ContentB
                 </button>
                 """);
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button");
        assertEquals("Invalid content for XML element", "\n    ContentA\n    \n    ContentB\n", result.getContent());
        assertChild(result, "text");
        assertLog(parser.getLogs());
    }

    @Test
    public void parse_FailTextBefore() {
        UXNodeParser parser = new UXNodeParser(
                """
                 ContentA<button></button>
                 """);
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button");
        assertNull("Invalid content for XML element", result.getContent());
        assertChild(result);
        assertLog(parser.getLogs(),
                UXNodeParser.ErroLog.UNEXPECTED_TOKEN, 0
        );
    }

    @Test
    public void parse_FailTextAfter() {
        UXNodeParser parser = new UXNodeParser(
                """
                 <button></button>
                 
                 ContentA
                 """);
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button");
        assertNull("Invalid content for XML element", result.getContent());
        assertChild(result);
        assertLog(parser.getLogs(),
                UXNodeParser.ErroLog.UNEXPECTED_TEXT, 3
        );
    }

    @Test
    public void emptyTextAfter() {
        UXNodeParser parser = new UXNodeParser(
                """
                 <button></button>
                 
                 
                 """);
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button");
        assertNull("Invalid content for XML element", result.getContent());
        assertChild(result);
        assertLog(parser.getLogs());
    }

    @Test
    public void emptyTextBefore() {
        UXNodeParser parser = new UXNodeParser(
                """
                 
                 
                 <button></button>
                 """);
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button");
        assertNull("Invalid content for XML element", result.getContent());
        assertChild(result);
        assertLog(parser.getLogs());
    }

    @Test
    public void parse_FailMultipleRoot() {
        UXNodeParser parser = new UXNodeParser(
                """
                <button/>
                <button/>
                """);
        parser.parse();
        UXNodeElement result = parser.getRootElement();

        assertElement(result, "button");
        assertChild(result);
        assertLog(parser.getLogs(),
                UXNodeParser.ErroLog.MULTIPLE_ROOT_ELEMENTS, 1
        );
    }

    public void assertLog(List<UXNodeParser.ErroLog> logs, Object... pair) {
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

    public void assertElement(UXNodeElement element, String name, Object... pair) {
        assertEquals("Invalid Element name", name, element.getName());

        var list = new ArrayList<String>();
        for (int i = 0; i < pair.length; i += 2) {
            String attrName = (String) pair[i];
            UXValue value = (UXValue) pair[i + 1];
            UXNodeAttribute attr = element.getAttributes().get(attrName);
            if (attr == null) {
                fail("Attribute not found : " + attrName);
            }
            assertEquals("Attribute \"" + attrName + "\" with a different value", value, attr.getValue());
            list.add(attrName);
        }
        for (var key : element.getAttributes().keySet()) {
            if (!list.contains(key)) {
                fail("Attribute not expected : " + key);
            }
        }
    }

    public void assertChild(UXNodeElement element, Object... children) {
        var list = new ArrayList<>(element.getChildren());
        for (int i = 0; i < children.length; i ++) {
            String childName = (String) children[i];
            boolean found = false;
            for (var child : list) {
                if (child.getName().equals(childName)) {
                    found = true;
                    list.remove(child);
                    break;
                }
            }
            if (!found) {
                fail("Child not found : '" + childName + "' in a total of " + element.getChildren().size());
            }
        }
        if (list.size() > 0) {
            fail("Child not expected(" + list.size() + ") : '" + listToString(list) + "'");
        }
    }

    private String listToString(List<?> list) {
        StringBuilder sb = new StringBuilder();
        for (var obj : list) {
            sb.append(", ").append(obj);
        }
        return sb.substring(Math.min(sb.length(), 2));
    }
}