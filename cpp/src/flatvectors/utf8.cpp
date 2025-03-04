//
// Created by Rodrigo on 04/12/2018.
//
#include "utf8.h"

int readChar(const char* str, int strLen, int& i, unsigned long& unicode) {
    if (i >= strLen) return 0;

    if (((unsigned char) str[i] | 0b01111111u) == 0b01111111u) {    //0xxxxxxx
        unicode = ((unsigned long) str[i]) & 127u;
        i += 1;
    } else if (((unsigned char) str[i] >> 5) == 0b110u) {           //110xxxxx
        if (i + 1 < strLen) {
            unicode = (((unsigned long) str[i]) & 31u) << 6u |
                      (((unsigned long) str[i + 1]) & 63u);
            i += 2;
        } else {
            return 0;
        }
    } else if (((unsigned char) str[i] >> 4) == 0b1110u) {          //1110xxxx
        if (i + 2 < strLen) {
            unicode = (((unsigned long) str[i]) & 15u) << 12u |
                      (((unsigned long) str[i + 1]) & 63u) << 6u |
                      (((unsigned long) str[i + 2]) & 63u);
            i += 3;
        } else {
            return 0;
        }
    } else if (((unsigned char) str[i] >> 3) == 0b11110u) {         //11110xxx
        if (i + 3 < strLen) {
            unicode = (((unsigned long) str[i]) & 7u) << 18u |
                      (((unsigned long) str[i + 1]) & 63u) << 12u |
                      (((unsigned long) str[i + 2]) & 63u) << 6u |
                      (((unsigned long) str[i + 3]) & 63u);
            i += 4;
        } else {
            return 0;
        }
    } else {                            //invalid character
        unicode = 0xFFFD;
        i += 1;
    }
    return 1;
}

int utf8loop(const char* str, int strLen, int& i, unsigned long& out) {
    if (readChar(str, strLen, i, out)) {
        if (out >= 0xD800 && out <= 0xDBFF) {
            unsigned long high = out;
            if (readChar(str, strLen, i, out)) {
                if (out >= 0xDC00 && out <= 0xDFFF) {
                    out = (((high - 0xD800) << 10) | (out - 0xDC00)) + 0x10000;
                } else {
                    out = 0xFFFD;
                }
                return 1;
            } else {
                return 0;
            }
        } else {
            return 1;
        }
    } else {
        return 0;
    }
}

/*int readChar(const char* str, int strLen, int& i, unsigned long& out) {
    if (i >= strLen) {
        return 0;
    }

    unsigned char c = static_cast<unsigned char>(str[i]);
    int unicode;
    int numBytes;
    if (c < 0x80) {
        unicode = c;
        numBytes = 1;
    } else if ((c & 0xE0) == 0xC0) {
        unicode = c & 0x1F;
        numBytes = 2;
    } else if ((c & 0xF0) == 0xE0) {
        unicode = c & 0x0F;
        numBytes = 3;
    } else if ((c & 0xF8) == 0xF0) {
        unicode = c & 0x07;
        numBytes = 4;
    } else {
        out = 0xFFFD;
        return 1;
    }

    if (i + numBytes > strLen) {
        return 0;
    }

    for (int j = 1; j < numBytes; ++j) {
        c = static_cast<unsigned char>(str[i + j]);
        if ((c & 0xC0) != 0x80) {
            out = 0xFFFD;
            return 1;
        }
        unicode = (unicode << 6) | (c & 0x3F);
    }

    i += numBytes;
    out = unicode;
    return 1;
}*/