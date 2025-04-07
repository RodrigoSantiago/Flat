package flat.widget.text.data;

import flat.graphics.symbols.Font;
import flat.widget.enums.HorizontalAlign;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TextRenderTest {

    Font font;

    @Before
    public void before() {
        font = mock(Font.class);
        when(font.getHeight(16f)).thenReturn(16f);
    }

    @Test
    public void empty() {
        mockFont(0, 0, 0, 0.0f);

        TextRender textRender = new TextRender();
        textRender.setFont(font);
        textRender.setTextSize(16f);
        textRender.setText("");
        assertEquals("", textRender.getText());
        assertEquals(0, textRender.getTextWidth(), 0.001f);
        assertEquals(16, textRender.getTextHeight(), 0.001f);

        Caret caret = new Caret();
        textRender.moveCaretBegin(caret);
        assertCaret(caret, 0, 0, 0, 0);

        textRender.moveCaretEnd(caret);
        assertCaret(caret, 0, 0, 0, 0);
    }

    @Test
    public void singleLetter() {
        mockFont(0, 0, 0, 0.0f);
        mockFont(0, 1, 1, 5.0f);
        mockFontPos(0, 1, 0.0f, 0, 0.0f);
        mockFontPos(0, 1, 5.0f, 1, 5.0f);
        when(font.getWidth(any(), eq(0), eq(1), anyFloat(), anyFloat())).thenReturn(5.0f);

        TextRender textRender = new TextRender();
        textRender.setFont(font);
        textRender.setTextSize(16f);
        textRender.setText("A");
        assertEquals("A", textRender.getText());
        assertEquals(5, textRender.getTextWidth(), 0.001f);
        assertEquals(16, textRender.getTextHeight(), 0.001f);

        Caret caret = new Caret();
        textRender.moveCaretEnd(caret);
        assertCaret(caret, 0, 1, 1, 5);

        textRender.moveCaretBegin(caret);
        assertCaret(caret, 0, 0, 0, 0);

        textRender.moveCaretFoward(caret);
        assertCaret(caret, 0, 1, 1, 5);

        textRender.moveCaretFoward(caret);
        assertCaret(caret, 0, 1, 1, 5);

        textRender.moveCaretBackwards(caret);
        assertCaret(caret, 0, 0, 0, 0);

        textRender.moveCaretBackwards(caret);
        assertCaret(caret, 0, 0, 0, 0);

        textRender.moveCaretVertical(caret, HorizontalAlign.LEFT, 1);
        assertCaret(caret, 0, 0, 0, 0);

        textRender.moveCaretVertical(caret, HorizontalAlign.LEFT, -1);
        assertCaret(caret, 0, 0, 0, 0);

        textRender.moveCaretFoward(caret);
        assertCaret(caret, 0, 1, 1, 5);

        textRender.moveCaretVertical(caret, HorizontalAlign.LEFT, 1);
        assertCaret(caret, 0, 1, 1, 5);

        textRender.moveCaretVertical(caret, HorizontalAlign.LEFT, -1);
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

        TextRender textRender = new TextRender();
        textRender.setFont(font);
        textRender.setTextSize(16f);
        textRender.setText("ABC");
        assertEquals("ABC", textRender.getText());
        assertEquals(15, textRender.getTextWidth(), 0.001f);
        assertEquals(16, textRender.getTextHeight(), 0.001f);

        Caret caret = new Caret();
        textRender.moveCaretEnd(caret);
        assertCaret(caret, 0, 3, 3, 15);

        textRender.moveCaretBegin(caret);
        assertCaret(caret, 0, 0, 0, 0);

        textRender.moveCaretFoward(caret);
        assertCaret(caret, 0, 1, 1, 5);

        textRender.moveCaretFoward(caret);
        assertCaret(caret, 0, 2, 2, 10);

        textRender.moveCaretBackwards(caret);
        assertCaret(caret, 0, 1, 1, 5);

        textRender.moveCaretBackwards(caret);
        assertCaret(caret, 0, 0, 0, 0);

        textRender.moveCaretVertical(caret, HorizontalAlign.LEFT, 1);
        assertCaret(caret, 0, 0, 0, 0);

        textRender.moveCaretVertical(caret, HorizontalAlign.LEFT, -1);
        assertCaret(caret, 0, 0, 0, 0);

        textRender.moveCaretFoward(caret);
        assertCaret(caret, 0, 1, 1, 5);

        textRender.moveCaretVertical(caret, HorizontalAlign.LEFT, 1);
        assertCaret(caret, 0, 1, 1, 5);

        textRender.moveCaretVertical(caret, HorizontalAlign.LEFT, -1);
        assertCaret(caret, 0, 1, 1, 5);

        textRender.moveCaretFoward(caret);
        assertCaret(caret, 0, 2, 2, 10);

        textRender.moveCaretVertical(caret, HorizontalAlign.LEFT, 1);
        assertCaret(caret, 0, 2, 2, 10);

        textRender.moveCaretVertical(caret, HorizontalAlign.LEFT, -1);
        assertCaret(caret, 0, 2, 2, 10);

        textRender.moveCaretFoward(caret);
        assertCaret(caret, 0, 3, 3, 15);

        textRender.moveCaretVertical(caret, HorizontalAlign.LEFT, 1);
        assertCaret(caret, 0, 3, 3, 15);

        textRender.moveCaretVertical(caret, HorizontalAlign.LEFT, -1);
        assertCaret(caret, 0, 3, 3, 15);

        textRender.moveCaretBegin(caret);
        assertCaret(caret, 0, 0, 0, 0);

        textRender.moveCaretFowardsLine(caret);
        assertCaret(caret, 0, 3, 3, 15);

        textRender.moveCaretBackwardsLine(caret);
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

        TextRender textRender = new TextRender();
        textRender.setFont(font);
        textRender.setTextSize(16f);
        textRender.setText("AB\nCD");
        assertEquals("AB\nCD", textRender.getText());
        assertEquals(10, textRender.getTextWidth(), 0.001f);
        assertEquals(32, textRender.getTextHeight(), 0.001f);

        Caret caret = new Caret();
        textRender.moveCaretEnd(caret);
        assertCaret(caret, 1, 2, 5, 10);

        textRender.moveCaretBegin(caret);
        assertCaret(caret, 0, 0, 0, 0);

        textRender.moveCaretFoward(caret);
        assertCaret(caret, 0, 1, 1, 5);

        textRender.moveCaretFoward(caret);
        assertCaret(caret, 0, 2, 2, 10);

        textRender.moveCaretBackwards(caret);
        assertCaret(caret, 0, 1, 1, 5);

        textRender.moveCaretBackwards(caret);
        assertCaret(caret, 0, 0, 0, 0);

        textRender.moveCaretVertical(caret, HorizontalAlign.LEFT, 1);
        assertCaret(caret, 1, 0, 3, 0);

        textRender.moveCaretVertical(caret, HorizontalAlign.LEFT, -1);
        assertCaret(caret, 0, 0, 0, 0);

        textRender.moveCaretFoward(caret);
        assertCaret(caret, 0, 1, 1, 5);

        textRender.moveCaretVertical(caret, HorizontalAlign.LEFT, 1);
        assertCaret(caret, 1, 1, 4, 5);

        textRender.moveCaretVertical(caret, HorizontalAlign.LEFT, -1);
        assertCaret(caret, 0, 1, 1, 5);

        textRender.moveCaretFoward(caret);
        assertCaret(caret, 0, 2, 2, 10);

        textRender.moveCaretVertical(caret, HorizontalAlign.LEFT, 1);
        assertCaret(caret, 1, 2, 5, 10);

        textRender.moveCaretVertical(caret, HorizontalAlign.LEFT, -1);
        assertCaret(caret, 0, 2, 2, 10);

        textRender.moveCaretBackwardsLine(caret);
        assertCaret(caret, 0, 0, 0, 0);

        textRender.moveCaretFowardsLine(caret);
        assertCaret(caret, 0, 2, 2, 10);

        textRender.moveCaretVertical(caret, HorizontalAlign.LEFT, 1);
        assertCaret(caret, 1, 2, 5, 10);

        textRender.moveCaretBackwardsLine(caret);
        assertCaret(caret, 1, 0, 3, 0);

        textRender.moveCaretFowardsLine(caret);
        assertCaret(caret, 1, 2, 5, 10);

        textRender.moveCaretBackwardsLine(caret);
        assertCaret(caret, 1, 0, 3, 0);

        textRender.moveCaretBackwards(caret);
        assertCaret(caret, 0, 2, 2, 10);

        textRender.moveCaretFoward(caret);
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

        TextRender textRender = new TextRender();
        textRender.setFont(font);
        textRender.setTextSize(16f);
        textRender.setText("BC");
        assertEquals("BC", textRender.getText());
        assertEquals(10, textRender.getTextWidth(), 0.001f);
        assertEquals(16, textRender.getTextHeight(), 0.001f);

        Caret caretA = new Caret();
        Caret caretB = new Caret();
        textRender.editText(caretA, caretB, "A", caretA);
        assertEquals("ABC", textRender.getText());
        assertEquals(15, textRender.getTextWidth(), 0.001f);
        assertEquals(16, textRender.getTextHeight(), 0.001f);
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
        textRender.moveCaretFoward(caretB);
        assertCaret(caretB, 0, 2, 2, 10);

        textRender.editText(caretA, caretB, "B\n", caretA);
        assertEquals("AB\nC", textRender.getText());
        assertEquals(10, textRender.getTextWidth(), 0.001f);
        assertEquals(32, textRender.getTextHeight(), 0.001f);
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

        TextRender textRender = new TextRender();
        textRender.setFont(font);
        textRender.setTextSize(16f);
        textRender.setText("AB\nCD");
        assertEquals("AB\nCD", textRender.getText());
        assertEquals(10, textRender.getTextWidth(), 0.001f);
        assertEquals(32, textRender.getTextHeight(), 0.001f);

        Caret caretA = new Caret();
        Caret caretB = new Caret();
        textRender.moveCaretFoward(caretA);
        textRender.moveCaretFoward(caretB);
        textRender.moveCaretFoward(caretB);
        textRender.moveCaretFoward(caretB);
        textRender.moveCaretFoward(caretB);
        assertEquals("B\nC", textRender.getText(caretA, caretB));
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

        TextRender textRender = new TextRender();
        textRender.setFont(font);
        textRender.setTextSize(16f);
        textRender.setText("ABC");
        assertEquals("ABC", textRender.getText());
        assertEquals(15, textRender.getTextWidth(), 0.001f);
        assertEquals(16, textRender.getTextHeight(), 0.001f);

        Caret caretA = new Caret();
        textRender.moveCaretBegin(caretA);
        assertCaret(caretA, 0, 0, 0, 0);

        Caret caretB = new Caret();
        textRender.moveCaretEnd(caretB);
        assertCaret(caretB, 0, 3, 3, 15);

        textRender.editText(caretA, caretB, "", caretA);

        assertEquals("", textRender.getText());
        assertEquals(0, textRender.getTextWidth(), 0.001f);
        assertEquals(16, textRender.getTextHeight(), 0.001f);
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

        TextRender textRender = new TextRender();
        textRender.setFont(font);
        textRender.setTextSize(16f);
        textRender.setText("AB\nCD\n\n");
        assertEquals("AB\nCD\n\n", textRender.getText());
        assertEquals(10, textRender.getTextWidth(), 0.001f);
        assertEquals(64, textRender.getTextHeight(), 0.001f);

        Caret caret = new Caret();
        textRender.moveCaretBegin(caret);
        assertCaret(caret, 0, 0, 0, 0);

        textRender.moveCaretFoward(caret);
        assertCaret(caret, 0, 1, 1, 5);

        textRender.moveCaretFoward(caret);
        assertCaret(caret, 0, 2, 2, 10);

        textRender.moveCaretFoward(caret);
        assertCaret(caret, 1, 0, 3, 0);

        textRender.moveCaretFoward(caret);
        assertCaret(caret, 1, 1, 4, 5);

        textRender.moveCaretFoward(caret);
        assertCaret(caret, 1, 2, 5, 10);

        textRender.moveCaretFoward(caret);
        assertCaret(caret, 2, 0, 6, 0);

        textRender.moveCaretFoward(caret);
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