package flat.widget.text.area;

import flat.graphics.symbols.Font;
import flat.widget.text.content.Caret;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TextBoxTest {

    Font font;

    @Before
    public void before() {
        font = mock(Font.class);
        when(font.getHeight(16f)).thenReturn(16f);
    }

    @Test
    public void empty() {
        mockFont(0, 0, 0, 0.0f);

        TextBox textBox = new TextBox();
        textBox.setFont(font);
        textBox.setTextSize(16f);
        textBox.setText("");
        assertEquals("", textBox.getText());
        assertEquals(0, textBox.getTextWidth(), 0.001f);
        assertEquals(16, textBox.getTextHeight(), 0.001f);

        Caret caret = new Caret();
        textBox.moveCaretBegin(caret);
        assertCaret(caret, 0, 0, 0, 0);

        textBox.moveCaretEnd(caret);
        assertCaret(caret, 0, 0, 0, 0);
    }

    @Test
    public void singleLetter() {
        mockFont(0, 0, 0, 0.0f);
        mockFont(0, 1, 1, 5.0f);
        mockFontPos(0, 1, 0.0f, 0, 0.0f);
        mockFontPos(0, 1, 5.0f, 1, 5.0f);
        when(font.getWidth(any(), eq(0), eq(1), anyFloat(), anyFloat())).thenReturn(5.0f);

        TextBox textBox = new TextBox();
        textBox.setFont(font);
        textBox.setTextSize(16f);
        textBox.setText("A");
        assertEquals("A", textBox.getText());
        assertEquals(5, textBox.getTextWidth(), 0.001f);
        assertEquals(16, textBox.getTextHeight(), 0.001f);

        Caret caret = new Caret();
        textBox.moveCaretEnd(caret);
        assertCaret(caret, 0, 1, 1, 5);

        textBox.moveCaretBegin(caret);
        assertCaret(caret, 0, 0, 0, 0);

        textBox.moveCaretForward(caret);
        assertCaret(caret, 0, 1, 1, 5);

        textBox.moveCaretForward(caret);
        assertCaret(caret, 0, 1, 1, 5);

        textBox.moveCaretBackwards(caret);
        assertCaret(caret, 0, 0, 0, 0);

        textBox.moveCaretBackwards(caret);
        assertCaret(caret, 0, 0, 0, 0);

        textBox.moveCaretVertical(caret, 1);
        assertCaret(caret, 0, 0, 0, 0);

        textBox.moveCaretVertical(caret, -1);
        assertCaret(caret, 0, 0, 0, 0);

        textBox.moveCaretForward(caret);
        assertCaret(caret, 0, 1, 1, 5);

        textBox.moveCaretVertical(caret, 1);
        assertCaret(caret, 0, 1, 1, 5);

        textBox.moveCaretVertical(caret, -1);
        assertCaret(caret, 0, 1, 1, 5);
    }

    @Test
    public void multLetter() {
        mockFont(0, 0, 0, 0.0f);
        mockFont(0, 1, 1, 5.0f);
        mockFont(0, 2, 2, 10.0f);
        mockFont(0, 3, 3, 15.0f);
        mockFontPos(0, 3, 0.0f, 0, 0.0f);
        mockFontPos(0, 3, 5.0f, 1, 5.0f);
        mockFontPos(0, 3, 10.0f, 2, 10.0f);
        mockFontPos(0, 3, 15.0f, 3, 15.0f);
        when(font.getWidth(any(), eq(0), eq(3), anyFloat(), anyFloat())).thenReturn(15.0f);

        TextBox textBox = new TextBox();
        textBox.setFont(font);
        textBox.setTextSize(16f);
        textBox.setText("ABC");
        assertEquals("ABC", textBox.getText());
        assertEquals(15, textBox.getTextWidth(), 0.001f);
        assertEquals(16, textBox.getTextHeight(), 0.001f);

        Caret caret = new Caret();
        textBox.moveCaretEnd(caret);
        assertCaret(caret, 0, 3, 3, 15);

        textBox.moveCaretBegin(caret);
        assertCaret(caret, 0, 0, 0, 0);

        textBox.moveCaretForward(caret);
        assertCaret(caret, 0, 1, 1, 5);

        textBox.moveCaretForward(caret);
        assertCaret(caret, 0, 2, 2, 10);

        textBox.moveCaretBackwards(caret);
        assertCaret(caret, 0, 1, 1, 5);

        textBox.moveCaretBackwards(caret);
        assertCaret(caret, 0, 0, 0, 0);

        textBox.moveCaretVertical(caret, 1);
        assertCaret(caret, 0, 0, 0, 0);

        textBox.moveCaretVertical(caret, -1);
        assertCaret(caret, 0, 0, 0, 0);

        textBox.moveCaretForward(caret);
        assertCaret(caret, 0, 1, 1, 5);

        textBox.moveCaretVertical(caret, 1);
        assertCaret(caret, 0, 1, 1, 5);

        textBox.moveCaretVertical(caret, -1);
        assertCaret(caret, 0, 1, 1, 5);

        textBox.moveCaretForward(caret);
        assertCaret(caret, 0, 2, 2, 10);

        textBox.moveCaretVertical(caret, 1);
        assertCaret(caret, 0, 2, 2, 10);

        textBox.moveCaretVertical(caret, -1);
        assertCaret(caret, 0, 2, 2, 10);

        textBox.moveCaretForward(caret);
        assertCaret(caret, 0, 3, 3, 15);

        textBox.moveCaretVertical(caret, 1);
        assertCaret(caret, 0, 3, 3, 15);

        textBox.moveCaretVertical(caret, -1);
        assertCaret(caret, 0, 3, 3, 15);

        textBox.moveCaretBegin(caret);
        assertCaret(caret, 0, 0, 0, 0);

        textBox.moveCaretLineEnd(caret);
        assertCaret(caret, 0, 3, 3, 15);

        textBox.moveCaretLineBegin(caret);
        assertCaret(caret, 0, 0, 0, 0);
    }

    @Test
    public void multLine() {
        mockFont(0, 0, 0, 0.0f);
        mockFont(0, 1, 1, 5.0f);
        mockFont(0, 2, 2, 10.0f);
        mockFont(3, 0, 0, 0.0f);
        mockFont(3, 1, 1, 5.0f);
        mockFont(3, 2, 2, 10.0f);
        mockFontPos(0, 2, 0.0f, 0, 0.0f);
        mockFontPos(0, 2, 5.0f, 1, 5.0f);
        mockFontPos(0, 2, 10.0f, 2, 10.0f);
        mockFontPos(3, 2, 0.0f, 0, 0.0f);
        mockFontPos(3, 2, 5.0f, 1, 5.0f);
        mockFontPos(3, 2, 10.0f, 2, 10.0f);
        when(font.getWidth(any(), eq(0), eq(2), anyFloat(), anyFloat())).thenReturn(10.0f);
        when(font.getWidth(any(), eq(3), eq(2), anyFloat(), anyFloat())).thenReturn(10.0f);

        TextBox textBox = new TextBox();
        textBox.setFont(font);
        textBox.setTextSize(16f);
        textBox.setText("AB\nCD");
        assertEquals("AB\nCD", textBox.getText());
        assertEquals(10, textBox.getTextWidth(), 0.001f);
        assertEquals(32, textBox.getTextHeight(), 0.001f);

        Caret caret = new Caret();
        textBox.moveCaretEnd(caret);
        assertCaret(caret, 1, 2, 5, 10);

        textBox.moveCaretBegin(caret);
        assertCaret(caret, 0, 0, 0, 0);

        textBox.moveCaretForward(caret);
        assertCaret(caret, 0, 1, 1, 5);

        textBox.moveCaretForward(caret);
        assertCaret(caret, 0, 2, 2, 10);

        textBox.moveCaretBackwards(caret);
        assertCaret(caret, 0, 1, 1, 5);

        textBox.moveCaretBackwards(caret);
        assertCaret(caret, 0, 0, 0, 0);

        textBox.moveCaretVertical(caret, 1);
        assertCaret(caret, 1, 0, 3, 0);

        textBox.moveCaretVertical(caret, -1);
        assertCaret(caret, 0, 0, 0, 0);

        textBox.moveCaretForward(caret);
        assertCaret(caret, 0, 1, 1, 5);

        textBox.moveCaretVertical(caret, 1);
        assertCaret(caret, 1, 1, 4, 5);

        textBox.moveCaretVertical(caret, -1);
        assertCaret(caret, 0, 1, 1, 5);

        textBox.moveCaretForward(caret);
        assertCaret(caret, 0, 2, 2, 10);

        textBox.moveCaretVertical(caret, 1);
        assertCaret(caret, 1, 2, 5, 10);

        textBox.moveCaretVertical(caret, -1);
        assertCaret(caret, 0, 2, 2, 10);

        textBox.moveCaretLineBegin(caret);
        assertCaret(caret, 0, 0, 0, 0);

        textBox.moveCaretLineEnd(caret);
        assertCaret(caret, 0, 2, 2, 10);

        textBox.moveCaretVertical(caret, 1);
        assertCaret(caret, 1, 2, 5, 10);

        textBox.moveCaretLineBegin(caret);
        assertCaret(caret, 1, 0, 3, 0);

        textBox.moveCaretLineEnd(caret);
        assertCaret(caret, 1, 2, 5, 10);

        textBox.moveCaretLineBegin(caret);
        assertCaret(caret, 1, 0, 3, 0);

        textBox.moveCaretBackwards(caret);
        assertCaret(caret, 0, 2, 2, 10);

        textBox.moveCaretForward(caret);
        assertCaret(caret, 1, 0, 3, 0);
    }

    @Test
    public void editText() {
        mockFont(0, 0, 0, 0.0f);
        mockFont(0, 1, 1, 5.0f);
        mockFont(0, 2, 2, 10.0f);
        mockFont(0, 3, 3, 15.0f);
        mockFontPos(0, 3, 0.0f, 0, 0.0f);
        mockFontPos(0, 3, 5.0f, 1, 5.0f);
        mockFontPos(0, 3, 10.0f, 2, 10.0f);
        mockFontPos(0, 3, 15.0f, 3, 15.0f);
        when(font.getWidth(any(), eq(0), eq(0), anyFloat(), anyFloat())).thenReturn(0.0f);
        when(font.getWidth(any(), eq(0), eq(1), anyFloat(), anyFloat())).thenReturn(5.0f);
        when(font.getWidth(any(), eq(0), eq(2), anyFloat(), anyFloat())).thenReturn(10.0f);
        when(font.getWidth(any(), eq(0), eq(3), anyFloat(), anyFloat())).thenReturn(15.0f);

        TextBox textBox = new TextBox();
        textBox.setFont(font);
        textBox.setTextSize(16f);
        textBox.setText("BC");
        assertEquals("BC", textBox.getText());
        assertEquals(10, textBox.getTextWidth(), 0.001f);
        assertEquals(16, textBox.getTextHeight(), 0.001f);

        Caret caretA = new Caret();
        Caret caretB = new Caret();
        textBox.editText(caretA, caretB, "A", caretA);
        assertEquals("ABC", textBox.getText());
        assertEquals(15, textBox.getTextWidth(), 0.001f);
        assertEquals(16, textBox.getTextHeight(), 0.001f);
        assertCaret(caretA, 0, 1, 1, 5);

        mockFont(0, 0, 0, 0.0f);
        mockFont(0, 1, 1, 5.0f);
        mockFont(0, 2, 2, 10.0f);
        mockFont(3, 0, 0, 0.0f);
        mockFont(3, 1, 1, 5.0f);
        mockFont(3, 2, 2, 10.0f);
        mockFontPos(0, 2, 0.0f, 0, 0.0f);
        mockFontPos(0, 2, 5.0f, 1, 5.0f);
        mockFontPos(0, 2, 10.0f, 2, 10.0f);
        mockFontPos(3, 2, 0.0f, 0, 0.0f);
        mockFontPos(3, 2, 5.0f, 1, 5.0f);
        mockFontPos(3, 2, 10.0f, 2, 10.0f);
        when(font.getWidth(any(), eq(0), eq(2), anyFloat(), anyFloat())).thenReturn(10.0f);
        when(font.getWidth(any(), eq(3), eq(2), anyFloat(), anyFloat())).thenReturn(10.0f);

        caretB.set(caretA);
        textBox.moveCaretForward(caretB);
        assertCaret(caretB, 0, 2, 2, 10);

        textBox.editText(caretA, caretB, "B\n", caretA);
        assertEquals("AB\nC", textBox.getText());
        assertEquals(10, textBox.getTextWidth(), 0.001f);
        assertEquals(32, textBox.getTextHeight(), 0.001f);
    }

    @Test
    public void copyText() {
        mockFont(0, 0, 0, 0.0f);
        mockFont(0, 1, 1, 5.0f);
        mockFont(0, 2, 2, 10.0f);
        mockFont(3, 0, 0, 0.0f);
        mockFont(3, 1, 1, 5.0f);
        mockFont(3, 2, 2, 10.0f);
        mockFontPos(0, 2, 0.0f, 0, 0.0f);
        mockFontPos(0, 2, 5.0f, 1, 5.0f);
        mockFontPos(0, 2, 10.0f, 2, 10.0f);
        mockFontPos(3, 2, 0.0f, 0, 0.0f);
        mockFontPos(3, 2, 5.0f, 1, 5.0f);
        mockFontPos(3, 2, 10.0f, 2, 10.0f);
        when(font.getWidth(any(), eq(0), eq(2), anyFloat(), anyFloat())).thenReturn(10.0f);
        when(font.getWidth(any(), eq(3), eq(2), anyFloat(), anyFloat())).thenReturn(10.0f);

        TextBox textBox = new TextBox();
        textBox.setFont(font);
        textBox.setTextSize(16f);
        textBox.setText("AB\nCD");
        assertEquals("AB\nCD", textBox.getText());
        assertEquals(10, textBox.getTextWidth(), 0.001f);
        assertEquals(32, textBox.getTextHeight(), 0.001f);

        Caret caretA = new Caret();
        Caret caretB = new Caret();
        textBox.moveCaretForward(caretA);
        textBox.moveCaretForward(caretB);
        textBox.moveCaretForward(caretB);
        textBox.moveCaretForward(caretB);
        textBox.moveCaretForward(caretB);
        assertEquals("B\nC", textBox.getText(caretA, caretB));
    }

    @Test
    public void clear() {
        mockFont(0, 0, 0, 0.0f);
        mockFont(0, 1, 1, 5.0f);
        mockFont(0, 2, 2, 10.0f);
        mockFont(0, 3, 3, 15.0f);
        mockFontPos(0, 3, 0.0f, 0, 0.0f);
        mockFontPos(0, 3, 5.0f, 1, 5.0f);
        mockFontPos(0, 3, 10.0f, 2, 10.0f);
        mockFontPos(0, 3, 15.0f, 3, 15.0f);
        when(font.getWidth(any(), eq(0), eq(3), anyFloat(), anyFloat())).thenReturn(15.0f);

        TextBox textBox = new TextBox();
        textBox.setFont(font);
        textBox.setTextSize(16f);
        textBox.setText("ABC");
        assertEquals("ABC", textBox.getText());
        assertEquals(15, textBox.getTextWidth(), 0.001f);
        assertEquals(16, textBox.getTextHeight(), 0.001f);

        Caret caretA = new Caret();
        textBox.moveCaretBegin(caretA);
        assertCaret(caretA, 0, 0, 0, 0);

        Caret caretB = new Caret();
        textBox.moveCaretEnd(caretB);
        assertCaret(caretB, 0, 3, 3, 15);

        textBox.editText(caretA, caretB, "", caretA);

        assertEquals("", textBox.getText());
        assertEquals(0, textBox.getTextWidth(), 0.001f);
        assertEquals(16, textBox.getTextHeight(), 0.001f);
    }

    @Test
    public void fourLines() {
        mockFont(0, 0, 0, 0.0f);
        mockFont(0, 1, 1, 5.0f);
        mockFont(0, 2, 2, 10.0f);
        mockFont(3, 0, 0, 0.0f);
        mockFont(3, 1, 1, 5.0f);
        mockFont(3, 2, 2, 10.0f);
        mockFont(6, 0, 0, 0.0f);
        mockFont(7, 0, 0, 0.0f);
        mockFontPos(0, 2, 0.0f, 0, 0.0f);
        mockFontPos(0, 2, 5.0f, 1, 5.0f);
        mockFontPos(0, 2, 10.0f, 2, 10.0f);
        mockFontPos(3, 2, 0.0f, 0, 0.0f);
        mockFontPos(3, 2, 5.0f, 1, 5.0f);
        mockFontPos(3, 2, 10.0f, 2, 10.0f);
        when(font.getWidth(any(), eq(0), eq(2), anyFloat(), anyFloat())).thenReturn(10.0f);
        when(font.getWidth(any(), eq(3), eq(2), anyFloat(), anyFloat())).thenReturn(10.0f);
        when(font.getWidth(any(), eq(6), eq(0), anyFloat(), anyFloat())).thenReturn(0.0f);
        when(font.getWidth(any(), eq(7), eq(0), anyFloat(), anyFloat())).thenReturn(0.0f);

        TextBox textBox = new TextBox();
        textBox.setFont(font);
        textBox.setTextSize(16f);
        textBox.setText("AB\nCD\n\n");
        assertEquals("AB\nCD\n\n", textBox.getText());
        assertEquals(10, textBox.getTextWidth(), 0.001f);
        assertEquals(64, textBox.getTextHeight(), 0.001f);

        Caret caret = new Caret();
        textBox.moveCaretBegin(caret);
        assertCaret(caret, 0, 0, 0, 0);

        textBox.moveCaretForward(caret);
        assertCaret(caret, 0, 1, 1, 5);

        textBox.moveCaretForward(caret);
        assertCaret(caret, 0, 2, 2, 10);

        textBox.moveCaretForward(caret);
        assertCaret(caret, 1, 0, 3, 0);

        textBox.moveCaretForward(caret);
        assertCaret(caret, 1, 1, 4, 5);

        textBox.moveCaretForward(caret);
        assertCaret(caret, 1, 2, 5, 10);

        textBox.moveCaretForward(caret);
        assertCaret(caret, 2, 0, 6, 0);

        textBox.moveCaretForward(caret);
        assertCaret(caret, 3, 0, 7, 0);
    }

    public void mockFont(int offset, int length, int charIndex, float width) {
        when(font.getCaretOffset(any(), eq(offset), eq(length), anyFloat(), anyFloat(), anyFloat(), anyBoolean()))
                .thenReturn(new Font.CaretData(charIndex, width));
    }

    public void mockFontPos(int offset, int length, float pos, int charIndex, float width) {
        when(font.getCaretOffset(any(), eq(offset), eq(length), anyFloat(), anyFloat(), eq(pos), anyBoolean()))
                .thenReturn(new Font.CaretData(charIndex, width));
    }

    public void assertCaret(Caret caret, int line, int lineChar, int offset, float width) {
        assertEquals(line, caret.getLine());
        assertEquals(lineChar, caret.getLineChar());
        assertEquals(offset, caret.getOffset());
        assertEquals(width, caret.getWidth(), 0.001f);
    }
}