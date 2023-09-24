//
// Created by Rodrigo on 30/10/2018.
//

#ifndef FLATVECTORS_FONT_H
#define FLATVECTORS_FONT_H

#include <flatvectors.h>

fvFont* fontCreate(const void* data, long int length, float size, int sdf);

void fontDestroy(fvFont* data);

void fontLoadGlyphs(fvFont* font, const char* str, int strLen);

void fontLoadAllGlyphs(fvFont* font);

fvGlyph& fontGlyph(fvFont*, long);

fvGlyph& fontGlyphRendered(fvFont*, long);

float fontKerning(fvFont*, long, long);

#endif //FLATVECTORS_FONT_H
