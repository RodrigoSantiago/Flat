package flat.events;

import flat.widget.Widget;

public class StyledTextEvent extends Event {

    public static final Type FILTER = new Type("FILTER");
    public static final Type TYPE = new Type("TYPE");

    private final int startLine;
    private final int endLine;
    private final int startCharacter;
    private final int endCharacter;
    private String text;

    public StyledTextEvent(Widget source, Type type, int startLine, int startChar, int endLine, int endChar, String text) {
        super(source, type);
        this.startLine = startLine;
        this.startCharacter = startChar;
        this.endLine = endLine;
        this.endCharacter = endChar;
        this.text = text;
    }
    
    public int getStartLine() {
        return startLine;
    }
    
    public int getStartCharacter() {
        return startCharacter;
    }
    
    public int getEndLine() {
        return endLine;
    }
    
    public int getEndCharacter() {
        return endCharacter;
    }
    
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "(" + getSource() + ") StyledTextEvent " + getType();
    }

    public static class Type extends EventType {
        Type(String name) {
            super(name);
        }
    }
}

