//
// Created by Rodrigo on 04/12/2018.
//

#ifndef FLATVECTORS_UTF8_H
#define FLATVECTORS_UTF8_H

int utf8loop(const char* str, int strLen, int& i, unsigned long& unicode);

int readChar(const char* str, int strLen, int& i, unsigned long& out);

#endif //FLATVECTORS_UTF8_H
