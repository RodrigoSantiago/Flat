package flat.widget.text;

import flat.animations.Animation;
import flat.animations.StateInfo;
import flat.events.*;
import flat.graphics.symbols.Font;
import flat.graphics.symbols.FontManager;
import flat.math.Vector2;
import flat.uxml.UXAttrs;
import flat.widget.layout.Scrollable;
import flat.widget.text.content.Caret;
import flat.widget.text.content.SelectionPos;
import flat.widget.text.content.SelectionRange;
import flat.widget.text.content.TextContentController;
import flat.window.Activity;
import flat.window.Application;

public abstract class TextEditor extends Scrollable {
    
    private float textSize = 16f;
    private Font textFont = Font.getDefault();
    private int textSelectedColor = 0x00000080;
    private int caretColor = 0x000000FF;
    private float caretBlinkDuration = 0.5f;
    
    protected final Caret startCaret = new Caret();
    protected final Caret endCaret = new Caret();
    private final CaretBlink caretBlink = new CaretBlink();
    private boolean showCaret;
    
    private int keyCopy = KeyCode.KEY_C;
    private int keyPaste = KeyCode.KEY_V;
    private int keyCut = KeyCode.KEY_X;
    private int keySelectAll = KeyCode.KEY_A;
    private int keyClearSelection = KeyCode.KEY_ESCAPE;
    private int keyBackspace = KeyCode.KEY_BACKSPACE;
    private int keyDelete = KeyCode.KEY_DELETE;
    private int keyMenu  = KeyCode.KEY_MENU;
    
    protected TextContentController textContent;
    
    protected abstract void editText(Caret start, Caret end, String text);
    
    @Override
    public void applyStyle() {
        super.applyStyle();
        
        UXAttrs attrs = getAttrs();
        StateInfo info = getStateInfo();
        
        setTextFont(attrs.getFont("text-font", info, getTextFont()));
        setTextSize(attrs.getSize("text-size", info, getTextSize()));
        setTextSelectedColor(attrs.getColor("text-selected-color", info, getTextSelectedColor()));
        setCaretColor(attrs.getColor("caret-color", info, getCaretColor()));
        setCaretBlinkDuration(attrs.getNumber("caret-blink-duration", info, getCaretBlinkDuration()));
    }
    
    // -------- Events ----------
    
    @Override
    public void scroll(ScrollEvent event) {
        super.scroll(event);
        if (!event.isConsumed() && isVerticalDimensionScroll()) {
            event.consume();
            slideVertical(- event.getDeltaY() * getScrollSensibility());
        }
    }
    
    @Override
    public void key(KeyEvent event) {
        super.key(event);
        if (event.isConsumed()) {
            return;
        }
        
        var first = getFirstCaret();
        var second = getSecondCaret();
        
        if (event.getType() == KeyEvent.TYPED) {
            editText(first, second, new String(Character.toChars(event.getKeycode())));
            event.consume();
        }
        
        if (event.getKeycode() != KeyCode.KEY_UNKNOWN &&
                    (event.getType() == KeyEvent.PRESSED || event.getType() == KeyEvent.REPEATED)) {
            
            if (event.getKeycode() == KeyCode.KEY_ENTER || event.getKeycode() == KeyCode.KEY_KP_ENTER) {
                editText(first, second, "\n");
            } else if (event.getKeycode() == KeyCode.KEY_TAB) {
                if (caretBlink.isPlaying()) {
                    editText(first, second, "\t");
                } else {
                    return;
                }
            } else if (event.getKeycode() == keyBackspace) {
                actionDeleteBackwards(first, second);
            } else if (event.getKeycode() == keyDelete) {
                actionDeleteForwards(first, second);
            } else if (event.isCtrlDown() && event.getKeycode() == keyPaste) {
                actionPaste(first, second);
            } else if (event.isCtrlDown() && event.getKeycode() == keyCopy) {
                actionCopy(first, second);
            } else if (event.isCtrlDown() && event.getKeycode() == keyCut) {
                actionCut(first, second);
            } else if (event.isCtrlDown() && event.getKeycode() == keySelectAll) {
                actionSelectAll();
            } else if (event.getKeycode() == keyClearSelection) {
                actionClearSelection();
            } else if (event.getKeycode() == keyMenu) {
                showContextMenu();
            } else {
                if (event.getKeycode() == KeyCode.KEY_LEFT) {
                    textContent.moveCaretBackwards(endCaret);
                    if (!event.isShiftDown()) startCaret.set(endCaret);
                } else if (event.getKeycode() == KeyCode.KEY_RIGHT) {
                    textContent.moveCaretForward(endCaret);
                    if (!event.isShiftDown()) startCaret.set(endCaret);
                } else if (event.getKeycode() == KeyCode.KEY_DOWN) {
                    textContent.moveCaretVertical(endCaret, 1);
                    if (!event.isShiftDown()) startCaret.set(endCaret);
                } else if (event.getKeycode() == KeyCode.KEY_UP) {
                    textContent.moveCaretVertical(endCaret, -1);
                    if (!event.isShiftDown()) startCaret.set(endCaret);
                } else if (event.getKeycode() == KeyCode.KEY_PAGE_DOWN) {
                    textContent.moveCaretVertical(endCaret, (int) (getViewDimensionY() / getTextFont().getHeight(getTextSize())));
                    if (!event.isShiftDown()) startCaret.set(endCaret);
                } else if (event.getKeycode() == KeyCode.KEY_PAGE_UP) {
                    textContent.moveCaretVertical(endCaret, -(int) (getViewDimensionY() / getTextFont().getHeight(getTextSize())));
                    if (!event.isShiftDown()) startCaret.set(endCaret);
                } else if (event.getKeycode() == KeyCode.KEY_HOME) {
                    textContent.moveCaretLineBegin(endCaret);
                    if (!event.isShiftDown()) startCaret.set(endCaret);
                } else if (event.getKeycode() == KeyCode.KEY_END) {
                    textContent.moveCaretLineEnd(endCaret);
                    if (!event.isShiftDown()) startCaret.set(endCaret);
                } else {
                    return;
                }
                setCaretVisible();
                slideToCaret(1);
                invalidate(false);
            }
            event.consume();
        }
    }
    
    public void slideToCaret(float speed) {
    
    }
    
    public void slideToCaretLater(float speed) {
        if (getActivity() != null) {
            getActivity().runLater(() -> slideToCaret(speed));
        }
    }
    
    @Override
    public void requestFocus(boolean focus) {
        if (isFocusable()) {
            Activity activity = getActivity();
            if (activity != null) {
                activity.runLater(() -> {
                    setFocused(focus);
                    if (focus) setCaretVisible();
                });
            }
        }
    }
    
    @Override
    public void focus(FocusEvent event) {
        super.focus(event);
        if (!isFocused()) {
            setCaretHidden();
        }
    }
    
    // -------- Caret ----------
    protected Caret getFirstCaret() {
        if (startCaret.getLine() == endCaret.getLine()) {
            return startCaret.getOffset() <= endCaret.getOffset() ? startCaret : endCaret;
        } else {
            return startCaret.getLine() < endCaret.getLine() ? startCaret : endCaret;
        }
    }
    
    protected Caret getSecondCaret() {
        if (startCaret.getLine() == endCaret.getLine()) {
            return startCaret.getOffset() <= endCaret.getOffset() ? endCaret : startCaret;
        } else {
            return startCaret.getLine() < endCaret.getLine() ? endCaret : startCaret;
        }
    }
    
    protected void setCaretVisible() {
        showCaret = true;
        invalidate(false);
        caretBlink.play();
    }
    
    protected void setCaretHidden() {
        showCaret = false;
        invalidate(false);
        caretBlink.stop();
    }
    
    protected void blinkCaret() {
        showCaret = isFocused() && !showCaret;
        invalidate(false);
    }
    
    protected boolean isShowCaret() {
        return showCaret;
    }
    
    public abstract Caret getCaretFromPosition(float x, float y);
    
    long lastPressedTime = 0;
    int clickCount = 0;
    
    protected void textPointer(PointerEvent event, Vector2 point) {
        if (event.isConsumed() || event.getSource() != this || event.getPointerID() != 1) {
            return;
        }
        
        Caret caretPos = getCaretFromPosition(point.x, point.y);
        
        if (event.getType() == PointerEvent.PRESSED) {
            event.consume();
            
            long now = System.currentTimeMillis();
            if (now - lastPressedTime < 200) {
                clickCount++;
            } else {
                clickCount = 1;
            }
            lastPressedTime = now;
            
            if (clickCount == 1) {
                startCaret.set(caretPos);
                endCaret.set(caretPos);
            } else if (clickCount == 2) {
                if (caretPos.getLine() == endCaret.getLine()) {
                    selectWord();
                } else {
                    startCaret.set(caretPos);
                    endCaret.set(caretPos);
                    clickCount = 1;
                }
            } else if (clickCount == 3) {
                if (caretPos.getLine() == endCaret.getLine()) {
                    selectLine();
                } else {
                    startCaret.set(caretPos);
                    endCaret.set(caretPos);
                    clickCount = 1;
                }
            } else if (clickCount == 4) {
                if (caretPos.getLine() == endCaret.getLine()) {
                    selectAll();
                } else {
                    startCaret.set(caretPos);
                    endCaret.set(caretPos);
                    clickCount = 1;
                }
            }
            setCaretVisible();
        } else if (event.getType() == PointerEvent.DRAGGED) {
            event.consume();
            endCaret.set(caretPos);
            setCaretVisible();
            slideToCaret(Application.getLoopTime() * 10f);
        } else if (event.getType() == PointerEvent.RELEASED) {
            event.consume();
        }
        invalidate(false);
    }
    
    // -------- Actions --------
    
    public Caret getCaret() {
        return new Caret(endCaret);
    }
    
    public void setCaretPosition(int line, int character) {
        setCaretPosition(line, character, false);
    }
    
    public void setCaretPosition(int line, int character, boolean holdSelection) {
        textContent.moveCaretBegin(endCaret);
        textContent.moveCaretVertical(endCaret, line);
        textContent.moveCaretLineBegin(endCaret);
        for (int i = 0; i < character; i++) {
            if (textContent.isCaretLastOfLine(endCaret)) {
                break;
            }
            textContent.moveCaretForward(endCaret);
        }
        if (!holdSelection) {
            startCaret.set(endCaret);
        }
        if (isFocused()) setCaretVisible();
        invalidate(false);
    }
    
    public void moveCaretHorizontal(int characters, boolean limitToLine) {
        if (characters > 0) {
            for (int i = 0; i < characters; i++) {
                if (limitToLine && textContent.isCaretLastOfLine(startCaret)) {
                    break;
                }
                textContent.moveCaretForward(startCaret);
            }
        } else {
            for (int i = 0; i < -characters; i++) {
                if (limitToLine && startCaret.getOffset() == 0) {
                    break;
                }
                textContent.moveCaretBackwards(startCaret);
            }
        }
        endCaret.set(startCaret);
        if (isFocused()) setCaretVisible();
        invalidate(false);
    }
    
    public void moveCaretVertical(int lines) {
        textContent.moveCaretVertical(startCaret, lines);
        endCaret.set(startCaret);
        if (isFocused()) setCaretVisible();
        invalidate(false);
    }
    
    public Vector2 getContextMenuTextPosition() {
        return new Vector2();
    }
    
    public void showContextMenu() {
    
    }
    
    public boolean isTextSelected() {
        return endCaret.getLine() != startCaret.getLine() || endCaret.getOffset() != startCaret.getOffset();
    }
    
    public void cut() {
        var first = getFirstCaret();
        var second = getSecondCaret();
        actionCut(first, second);
    }
    
    public void copy() {
        var first = getFirstCaret();
        var second = getSecondCaret();
        actionCopy(first, second);
    }
    
    public void paste() {
        var first = getFirstCaret();
        var second = getSecondCaret();
        actionPaste(first, second);
    }
    
    public void delete() {
        var first = getFirstCaret();
        var second = getSecondCaret();
        editText(first, second, "");
    }
    
    public void clearSelection() {
        actionClearSelection();
    }
    
    public void selectAll() {
        actionSelectAll();
    }
    
    public void selectWord() {
        actionSelectWord();
    }
    
    public void selectLine() {
        textContent.moveCaretLineEnd(endCaret);
        startCaret.set(endCaret);
        textContent.moveCaretLineBegin(startCaret);
        slideToCaret(1);
        if (isFocused()) setCaretVisible();
        invalidate(false);
    }
    
    public void selectRange(SelectionRange range) {
        selectRange(range.getStartLine(), range.getStartCharacter(), range.getEndLine(), range.getEndCharacter());
    }
    
    public void selectRange(int startLine, int startCharacter, int endLine, int endCharacter) {
        textContent.moveCaretBegin(startCaret);
        textContent.moveCaretVertical(startCaret, startLine);
        textContent.moveCaretLineBegin(startCaret);
        for (int i = 0; i < startCharacter; i++) {
            if (textContent.isCaretLastOfLine(startCaret)) {
                break;
            }
            textContent.moveCaretForward(startCaret);
        }
        
        textContent.moveCaretBegin(endCaret);
        textContent.moveCaretVertical(endCaret, endLine);
        textContent.moveCaretLineBegin(endCaret);
        for (int i = 0; i < endCharacter; i++) {
            if (textContent.isCaretLastOfLine(endCaret)) {
                break;
            }
            textContent.moveCaretForward(endCaret);
        }
        
        slideToCaret(1);
        if (isFocused()) setCaretVisible();
        invalidate(false);
    }
    
    protected void actionClearSelection() {
        startCaret.set(endCaret);
        invalidate(false);
    }
    
    protected void actionSelectAll() {
        textContent.moveCaretBegin(startCaret);
        textContent.moveCaretEnd(endCaret);
        invalidate(false);
    }
    
    protected void actionSelectWord() {
        textContent.moveCaretWordBackwards(startCaret);
        textContent.moveCaretWordForward(endCaret);
        if (startCaret.getOffset() == endCaret.getOffset()) {
            if (!textContent.isCaretLastOfLine(startCaret)) {
                textContent.moveCaretForward(endCaret);
            } else if (startCaret.getOffset() > 0) {
                textContent.moveCaretBackwards(endCaret);
            }
        }
        invalidate(false);
    }
    
    protected void actionDeleteBackwards(Caret first, Caret second) {
        if (first.getOffset() == second.getOffset()) {
            textContent.moveCaretBackwards(first);
        }
        editText(first, second, "");
    }
    
    protected void actionDeleteForwards(Caret first, Caret second) {
        if (first.getOffset() == second.getOffset()) {
            textContent.moveCaretForward(second);
        }
        editText(first, second, "");
    }
    
    protected void actionCut(Caret first, Caret second) {
        String str = textContent.getText(first, second);
        if (str != null && !str.isEmpty()) {
            Application.setClipboard(str);
        }
        editText(first, second, "");
    }
    
    protected void actionCopy(Caret first, Caret second) {
        String str = textContent.getText(first, second);
        if (str != null && !str.isEmpty()) {
            Application.setClipboard(str);
        }
    }
    
    protected void actionPaste(Caret first, Caret second) {
        String str = Application.getClipboard();
        if (str != null && !str.isEmpty()) {
            editText(first, second, str);
            slideToCaretLater(1);
        }
    }
    
    // ---------- Keys ---------
    
    public int getKeyCopy() {
        return keyCopy;
    }
    
    public void setKeyCopy(int keyCopy) {
        this.keyCopy = keyCopy;
    }
    
    public int getKeyPaste() {
        return keyPaste;
    }
    
    public void setKeyPaste(int keyPaste) {
        this.keyPaste = keyPaste;
    }
    
    public int getKeyCut() {
        return keyCut;
    }
    
    public void setKeyCut(int keyCut) {
        this.keyCut = keyCut;
    }
    
    public int getKeySelectAll() {
        return keySelectAll;
    }
    
    public void setKeySelectAll(int keySelectAll) {
        this.keySelectAll = keySelectAll;
    }
    
    public int getKeyClearSelection() {
        return keyClearSelection;
    }
    
    public void setKeyClearSelection(int keyClearSelection) {
        this.keyClearSelection = keyClearSelection;
    }
    
    public int getKeyBackspace() {
        return keyBackspace;
    }
    
    public void setKeyBackspace(int keyBackspace) {
        this.keyBackspace = keyBackspace;
    }
    
    public int getKeyDelete() {
        return keyDelete;
    }
    
    public void setKeyDelete(int keyDelete) {
        this.keyDelete = keyDelete;
    }
    
    public int getKeyMenu() {
        return keyMenu;
    }
    
    public void setKeyMenu(int keyMenu) {
        this.keyMenu = keyMenu;
    }
    
    // -------------------------
    public Font getTextFont() {
        return textFont;
    }
    
    public void setTextFont(Font textFont) {
        if (textFont == null) textFont = FontManager.getDefault();
        
        if (this.textFont != textFont) {
            this.textFont = textFont;
            invalidate(true);
        }
    }
    
    public float getTextSize() {
        return textSize;
    }
    
    public void setTextSize(float textSize) {
        if (this.textSize != textSize) {
            this.textSize = textSize;
            invalidate(isWrapContent());
        }
    }
    
    public int getTextSelectedColor() {
        return textSelectedColor;
    }
    
    public void setTextSelectedColor(int textSelectedColor) {
        if (this.textSelectedColor != textSelectedColor) {
            this.textSelectedColor = textSelectedColor;
            invalidate(false);
        }
    }
    
    public float getCaretBlinkDuration() {
        return caretBlinkDuration;
    }
    
    public void setCaretBlinkDuration(float caretBlinkDuration) {
        this.caretBlinkDuration = caretBlinkDuration;
    }
    
    public int getCaretColor() {
        return caretColor;
    }
    
    public void setCaretColor(int caretColor) {
        if (this.caretColor != caretColor) {
            this.caretColor = caretColor;
            invalidate(false);
        }
    }
    
    protected class CaretBlink implements Animation {
        
        private boolean playing;
        private float timer;
        
        public void play() {
            timer = 0;
            if (getActivity() != null) {
                playing = true;
                getActivity().addAnimation(this);
            }
        }
        
        public void stop() {
            playing = false;
        }
        
        @Override
        public Activity getSource() {
            return getActivity();
        }
        
        @Override
        public boolean isPlaying() {
            return playing;
        }
        
        @Override
        public void handle(float seconds) {
            timer += seconds;
            if (timer >= getCaretBlinkDuration()) {
                timer = 0;
                blinkCaret();
            }
        }
    }
}
