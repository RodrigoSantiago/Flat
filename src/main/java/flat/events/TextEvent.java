package flat.events;

import flat.widget.Widget;
import flat.widget.text.data.Caret;

public class TextEvent extends Event {

    public static final EventType CHANGE = new EventType();

    private Caret start;
    private Caret end;
    private String text;

    public TextEvent(Widget source, Caret start, Caret end, String text) {
        super(source, CHANGE);
        this.start = start;
        this.end = end;
        this.text = text;
    }

    public Caret getStart() {
        return start;
    }

    public Caret getEnd() {
        return end;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "TextEvent [CHANGE]";
    }
}

