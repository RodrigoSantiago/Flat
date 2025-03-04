//
// Created by Rodrigo on 30/10/2018.
//

#ifndef FLATVECTORS_FONT_H
#define FLATVECTORS_FONT_H

#include <flatvectors.h>

void* fontCreate(const void* data, long int length, float size, int sdf);

void fontDestroy(void* ctx);

void fontGetData(void* ctx, fvFont* ft);

void fontGetAllCodePoints(void* ctx, long int* codePoints);

void fontGetGlyphShape(void* ctx, long unicode, float** data, int* len);

void fontGetMetrics(void* ctx, float* ascender, float* descender, float* height, float* lineGap, int* glyphCount);

fvGlyph& fontGlyph(void* ctx, long unicode);

fvGlyph& fontGlyphRendered(void* ctx, fvFont* font, long unicode, fvPoint* uv, int* recreate);

float fontKerning(void* ctx, long unicode1, long unicode2);

#endif //FLATVECTORS_FONT_H
