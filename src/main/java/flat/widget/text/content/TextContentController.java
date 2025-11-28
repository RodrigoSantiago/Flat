package flat.widget.text.content;

public interface TextContentController {
    
    void moveCaretBegin(Caret caret);
    
    void moveCaretEnd(Caret caret);
    
    void moveCaretBackwards(Caret caret);
    
    void moveCaretForward(Caret caret);
    
    void moveCaretVertical(Caret caret, int jumpLines);
    
    void moveCaretLineBegin(Caret caret);
    
    void moveCaretLineEnd(Caret caret);
    
    boolean isCaretLastOfLine(Caret caret);
    
    void moveCaretWordForward(Caret caret);
    
    void moveCaretWordBackwards(Caret caret);
    
    String getText(Caret first, Caret second);
}
