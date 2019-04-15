//
// Created by Rodrigo on 04/12/2018.
//
#include "utf8.h"

int utf8loop(const char* str, int strLen, int& i, unsigned long& unicode) {
    if (i >= strLen) return 0;

    if (((unsigned char) str[i] | 0b01111111u) == 0b01111111u) {             //0xxxxxxx
        unicode = ((unsigned long) str[i]) & 127u;
        i += 1;
    } else if (((unsigned char) str[i] | 0b11011111u) == 0b11011111u) {      //110xxxxx
        if (i + 1 < strLen) {
            unicode = (((unsigned long) str[i]) & 31u) << 6u |
                      (((unsigned long) str[i + 1]) & 63u);
            i += 2;
        } else {
            return 0;
        }
    } else if (((unsigned char) str[i] | 0b11101111u) == 0b11101111u) {     //1110xxxx
        if (i + 2 < strLen) {
            unicode = (((unsigned long) str[i]) & 15u) << 12u |
                      (((unsigned long) str[i + 1]) & 63u) << 6u |
                      (((unsigned long) str[i + 2]) & 63u);
            i += 3;
        } else {
            return 0;
        }
    } else if (((unsigned char) str[i] | 0b11110111u) == 0b11110111u) {     //11110xxx
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