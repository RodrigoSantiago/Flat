package flat.widget.text.content;

public interface TextContentController {
    
    void moveCaretBegin(Caret caret);
    
    void moveCaretEnd(Caret caret);
    
    void moveCaretBackwards(Caret caret);
    
    void moveCaretForward(Caret caret);
    
    void moveCaretVertical(Caret caret, int jumpLines);
    
    default void moveCaretHorizontal(Caret caret, int characters) {
        if (characters > 0) {
            for (int i = 0; i < characters; i++) {
                if (isCaretLastOfLine(caret)) {
                    break;
                }
                moveCaretForward(caret);
            }
        } else {
            for (int i = 0; i < -characters; i++) {
                if (caret.getOffset() == 0) {
                    break;
                }
                moveCaretBackwards(caret);
            }
        }
    }
    
    void moveCaretLineBegin(Caret caret);
    
    void moveCaretLineEnd(Caret caret);
    
    boolean isCaretLastOfLine(Caret caret);
    
    void moveCaretWordForward(Caret caret);
    
    void moveCaretWordBackwards(Caret caret);
    
    String getText(Caret first, Caret second);
}
