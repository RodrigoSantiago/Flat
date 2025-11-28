package flat.widget.text.content;

import flat.graphics.emojis.EmojiManager;

import java.nio.charset.StandardCharsets;

public class CaretControl {
    private static final int[] temp = new int[2];
    
    public static int countChars(byte[] arr, int off, int end) {
        temp[0] = 0;
        temp[1] = off;
        int count = 0;
        while (temp[1] < end) {
            count++;
            readNextUnicode(end, arr, temp);
        }
        return count;
    }
    
    public static int getNextCharIndex(int currentIndex, byte[] textBytes, int length) {
        if (currentIndex < 0 || currentIndex >= length) {
            throw new IndexOutOfBoundsException("Invalid currentIndex");
        }
        temp[0] = 0;
        temp[1] = currentIndex;
        readNextUnicode(length, textBytes, temp);
        return temp[1];
    }
    
    public static int getPrevCharIndex(int currentIndex, byte[] textBytes, int length) {
        if (currentIndex <= 0 || currentIndex > length) {
            throw new IndexOutOfBoundsException("Invalid currentIndex");
        }
        
        int pos = currentIndex;
        for (int i = 0; i < 7; i++) {
            pos = readPrevChar(pos, textBytes);
        }
        
        int err = 10;
        temp[0] = 0;
        temp[1] = pos;
        while (temp[1] < currentIndex && err-- > 0) {
            pos = temp[1];
            readNextUnicode(length, textBytes, temp);
        }
        return pos;
    }
    
    private static int readPrevChar(int currentIndex, byte[] arr) {
        int prevIndex = currentIndex - 1;
        
        while (prevIndex > 0) {
            int byteValue = arr[prevIndex] & 0xFF;
            
            if ((byteValue & 0xC0) != 0x80) {
                return prevIndex;
            }
            
            prevIndex--;
        }
        
        return 0;
    }
    
    private static boolean isEmoji(int chr) {
        return EmojiManager.isEnabled() && ((chr >= 0x1F000 && chr <= 0x1FAFF) || (chr >= 0x200D && chr <= 0x3300) || chr == 0xFE0E || chr == 0xFE0F);
    }
    
    private static void readNextUnicode(int length, byte[] array, int[] outPut) {
        // Usual Char
        readNextChar(length, array, outPut);
        int pc = outPut[0];
        int pi = outPut[1];
        
        if (!isEmoji(pc)) {
            return;
        }
        
        // Emoji
        
        boolean waitNext = false;
        int pos = 1;
        while (readNextChar(length, array, outPut)) {
            int chr = outPut[0];
            if (chr == 0x200D) {
                waitNext = true;
            } else if (waitNext || chr == 0x1F3FB || chr == 0x1F3FC || chr == 0x1F3FD || chr == 0x1F3FE || chr == 0x1F3FF) {
                waitNext = false;
                pos++;
                if (pos == 6) {
                    break;
                }
            } else if (pos == 1 && (pc >= 0x1f1e6 && pc <= 0x1f1ff) && (chr >= 0x1f1e6 && chr <= 0x1f1ff)) {
                break;
            } else if (chr != 0xFE0E && chr != 0xFE0F) {
                outPut[0] = pc;
                outPut[1] = pi;
                break;
            }
            pc = outPut[0];
            pi = outPut[1];
        }
    }
    
    private static boolean readNextChar(int length, byte[] array, int[] outPut) {
        int position = outPut[1];
        int nextPosition = position;
        if (position < 0 || position >= length) return false;
        
        int firstByte = array[position] & 0xFF;
        int codePoint;
        
        if ((firstByte & 0x80) == 0) {
            codePoint = firstByte;
            nextPosition += 1;
        } else if ((firstByte & 0xE0) == 0xC0 && position + 1 < length) {
            int secondByte = array[position + 1] & 0xFF;
            codePoint = ((firstByte & 0x1F) << 6) | (secondByte & 0x3F);
            nextPosition += 2;
        } else if ((firstByte & 0xF0) == 0xE0 && position + 2 < length) {
            int secondByte = array[position + 1] & 0xFF;
            int thirdByte = array[position + 2] & 0xFF;
            codePoint = ((firstByte & 0x0F) << 12) | ((secondByte & 0x3F) << 6) | (thirdByte & 0x3F);
            nextPosition += 3;
        } else if ((firstByte & 0xF8) == 0xF0 && position + 3 < length) {
            int secondByte = array[position + 1] & 0xFF;
            int thirdByte = array[position + 2] & 0xFF;
            int fourthByte = array[position + 3] & 0xFF;
            codePoint = ((firstByte & 0x07) << 18) | ((secondByte & 0x3F) << 12) |
                                ((thirdByte & 0x3F) << 6) | (fourthByte & 0x3F);
            nextPosition += 4;
        } else {
            codePoint = 0xFFFD;
            nextPosition += 1;
        }
        outPut[0] = codePoint;
        outPut[1] = nextPosition;
        return true;
    }
    
    public static String offsetByCodePoints(String s, int maxCodePoints) {
        if (s == null) return null;
        
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        int index = 0;
        while (index < maxCodePoints && index < bytes.length) {
            index = CaretControl.getNextCharIndex(index, bytes, bytes.length);
        }
        return new String(bytes, 0, index, StandardCharsets.UTF_8);
    }
}
