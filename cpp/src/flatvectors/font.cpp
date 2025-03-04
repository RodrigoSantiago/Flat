//
// Created by Rodrigo on 30/10/2018.
//

#include "font.h"
#include "utf8.h"
#include "render.h"

#define STB_TRUETYPE_IMPLEMENTATION
#include "stb_truetype.h"
#include "pack.h"

#define PADDING 8
#define PADDING2 16

typedef struct ftFontData {
    stbtt_fontinfo info;
    unsigned char* data;
    int sdf;
    float scale;
    float size;
    float ascent;
    float descent;
    float lineGap;
    float height;
    int glyphCount;
    bool coded;
    fvGlyph* glyphs;
} ftFontData;

void* fontCreate(const void* data, long int length, float size, int sdf) {
    ftFontData* fdata = (ftFontData*) malloc(sizeof(ftFontData));

    // Safe Data
    fdata->data = (unsigned char*) malloc(length);
    memcpy(fdata->data, data, length);

    if (!stbtt_InitFont(&fdata->info, fdata->data, 0)) {
        free(fdata->data);
        free(fdata);
        return NULL;

    } else {
        fdata->size = size;
        fdata->sdf = sdf;

        fdata->scale = stbtt_ScaleForMappingEmToPixels(&fdata->info, size);//stbtt_ScaleForPixelHeight(&fdata->info, height);
        fdata->glyphCount = fdata->info.numGlyphs;
        fdata->glyphs = (fvGlyph*) calloc(fdata->glyphCount, sizeof(fvGlyph));
        fdata->coded = false;

        int ascent, descent, lineGap;
        stbtt_GetFontVMetrics(&fdata->info, &ascent, &descent, &lineGap);
        fdata->ascent = ascent * fdata->scale;
        fdata->descent = descent * fdata->scale;
        fdata->lineGap = lineGap * fdata->scale;
        fdata->height = (ascent - descent) * fdata->scale;

        return fdata;
    }
}

void fontDestroy(void* ctx) {
    ftFontData* fdata = (ftFontData*)(ctx);
    for (int i = 0; i < fdata->glyphCount; ++i) {
        free(fdata->glyphs[i].cell);
    }
    free(fdata->glyphs);
    free(fdata->data);
    free(fdata);
}

void fontGetData(void* ctx, fvFont* ft) {
    ftFontData* fdata = (ftFontData*)(ctx);

    ft->imageID = 0;
    ft->renderState = (fvPoint*) malloc(fdata->glyphCount * sizeof(fvPoint));
    for (int i = 0; i < fdata->glyphCount; ++i) {
        ft->renderState[i] = fvPoint {-1, -1};
    }

    ft->sdf = fdata->sdf;
    ft->count = fdata->glyphCount;
    ft->size = fdata->size;
    ft->ascent = fdata->ascent;
    ft->descent = fdata->descent;
    ft->lineGap = fdata->lineGap;
    ft->height = fdata->height;

    ft->fCtx = fdata;
    int x, y, w, h;
    stbtt_GetFontBoundingBox(&fdata->info, &x, &y, &w, &h);
    int cellW = ceil((w - x) * fdata->scale) + PADDING2;
    int cellH = ceil((h - y) * fdata->scale) + PADDING2;
    if (cellW > ceil(ft->height * 1.01 + PADDING2) * 2) {
        cellW = ceil(ft->height * 1.01 + PADDING2);
    }
    if (cellH > ceil(ft->height * 1.01 + PADDING2) * 2) {
        cellH = ceil(ft->height * 1.01 + PADDING2);
    }
    ft->pack = packCreate(cellW, cellH);
}

int __maxCodePoint(stbtt_fontinfo *info){
    stbtt_uint8 *data = info->data;
    stbtt_uint32 index_map = info->index_map;

    stbtt_uint16 format = ttUSHORT(data + index_map + 0);
    if (format == 0) { // apple byte encoding
        stbtt_int32 bytes = ttUSHORT(data + index_map + 2);
        return bytes-6;
    } else if (format == 6) {
        stbtt_uint32 first = ttUSHORT(data + index_map + 6);
        stbtt_uint32 count = ttUSHORT(data + index_map + 8);
        return first + count;
    } else if (format == 2) {
        return 0;
    } else if (format == 4) {
        return 0xffff;
    } else if (format == 12 || format == 13) {
        return 0;
    }
    return 0;
}

void fontGetAllCodePoints(void* ctx, long int* codePoints) {
    ftFontData* fdata = (ftFontData*)(ctx);
    if (!fdata->coded) {
        int max = __maxCodePoint(&fdata->info);
        if (max == 0) {
            max = 0x10FFFF;
        }

        for (long int codePoint = 0; codePoint <= max; codePoint++) {
            int glyphIndex = stbtt_FindGlyphIndex(&fdata->info, codePoint);
            if (glyphIndex != 0) {
                fdata->glyphs[glyphIndex].unicode = codePoint;
            }
        }
        fdata->coded = true;
    }
    for (int i = 0; i < fdata->glyphCount; ++i) {
        codePoints[i] = fdata->glyphs[i].unicode;
    }
}

void __loadGlyph(ftFontData* fdata, int glyphIndex) {
    int c_x1, c_y1, c_x2, c_y2;
    stbtt_GetGlyphBitmapBox(&fdata->info, glyphIndex, fdata->scale, fdata->scale, &c_x1, &c_y1, &c_x2, &c_y2);

    int ax, lb;
    stbtt_GetGlyphHMetrics(&fdata->info, glyphIndex, &ax, &lb);

    fvGlyph& glyph = fdata->glyphs[glyphIndex];
    glyph.enabled = 1;
    glyph.advance = ax * fdata->scale;

    if (c_x2 - c_x1 <= 0 || c_y2 - c_y1 <= 0) {
        glyph.x = 0;
        glyph.y = 0;
        glyph.w = 0;
        glyph.h = 0;
    } else {
        glyph.x = c_x1 - PADDING;
        glyph.y = c_y1 + fdata->ascent - PADDING;
        glyph.w = c_x2 - c_x1 + PADDING2;
        glyph.h = c_y2 - c_y1 + PADDING2;
    }
}

int __renderGlyph(ftFontData* fdata, fvFont* font, int glyphIndex) {
    fvGlyph& glyph = fdata->glyphs[glyphIndex];

    if (!glyph.enabled) {
        __loadGlyph(fdata, glyphIndex);
    }

    int width = (int) ceil(glyph.w);
    int height = (int) ceil(glyph.h);

    if (width > 0 && height > 0) {
        int oW = font->pack->width;
        int oH = font->pack->height;
        fvPoint * point = &font->renderState[glyphIndex];
        int state = packAddRect(font->pack, width, height, point);
        if (state == 2) {
            return 2; // CLEARED - NO TEXT CREATED

        } else if (state != -1) {
            if (font->imageID == 0) {
                font->imageID = renderCreateFontTexture(font->pack->width, font->pack->height);
            } else if (state == 1) {
                font->imageID = renderResizeFontTexture(font->imageID, oW, oH, font->pack->width, font->pack->height);
            }

            int cellW, cellH;
            packToCellSize(font->pack, width, height, &cellW, &cellH);

            if (glyph.cell == NULL) {
                unsigned char *img = (unsigned char *) calloc(cellW * cellH, sizeof(unsigned char));

                if (font->sdf) {
                    int w, h, xof, yof;
                    unsigned char *bmap = stbtt_GetGlyphSDF(&fdata->info, fdata->scale, glyphIndex, PADDING, 128, 16
                                                            , &w, &h, &xof, &yof);
                    if (bmap != 0) {
                        for (int y = 0; y < height && y < h; y++) {
                            for (int x = 0; x < width && x < w; x++) {
                                img[x + y * cellW] = bmap[x + y * w];
                            }
                        }
                        stbtt_FreeSDF(bmap, &fdata->info.userdata);
                    }
                } else {
                    stbtt_MakeGlyphBitmap(&fdata->info, img + (PADDING * cellW)/*y*/ + PADDING/*x*/
                            , width - PADDING2
                            , height - PADDING2
                            , cellW
                            , fdata->scale, fdata->scale, glyphIndex);
                }

                glyph.cell = img;
            }
            renderUpdateFontTexture(font->imageID, glyph.cell, point->x, point->y, cellW, cellH);
            return 1; // TEXT CREATED
        }
    }

    font->renderState[glyphIndex] = fvPoint {-2, -2};
    return 0; // NOTHING HAPPENED, NO TEXT CREATED
}

void fontGetGlyphShape(void* ctx, long unicode, float** polygon, int* len) {
    ftFontData* fdata = (ftFontData*)(ctx);
    int glyphIndex = stbtt_FindGlyphIndex(&fdata->info, unicode);

    stbtt_vertex* vertices;
    int num = stbtt_GetGlyphShape(&fdata->info, glyphIndex, &vertices);

    if (num > 0) {
        float* data = (float *) calloc(num * 7, sizeof(float));
        int ipos = 0;
        for (int i = 0; i < num; ++i) {
            const stbtt_vertex* v = &vertices[i];

            switch (v->type) {
                case STBTT_vmove:
                    data[ipos++] = 0;
                    data[ipos++] = v->x / fdata->size;
                    data[ipos++] = -v->y / fdata->size;
                    break;
                case STBTT_vline:
                    data[ipos++] = 1;
                    data[ipos++] = v->x / fdata->size;
                    data[ipos++] = -v->y / fdata->size;
                    break;
                case STBTT_vcurve:
                    data[ipos++] = 2;
                    data[ipos++] = v->cx / fdata->size;
                    data[ipos++] = -v->cy / fdata->size;
                    data[ipos++] = v->x / fdata->size;
                    data[ipos++] = -v->y / fdata->size;
                    break;
                case STBTT_vcubic:
                    data[ipos++] = 3;
                    data[ipos++] = v->cx1 / fdata->size;
                    data[ipos++] = -v->cy1 / fdata->size;
                    data[ipos++] = v->cx / fdata->size;
                    data[ipos++] = -v->cy / fdata->size;
                    data[ipos++] = v->x / fdata->size;
                    data[ipos++] = -v->y / fdata->size;
                    break;
            }
        }
        *polygon = data;
        *len = ipos;
        stbtt_FreeShape(&fdata->info, vertices);
    } else {
        *polygon = NULL;
        *len = 0;
    }
}

void fontGetMetrics(void* ctx, float* ascender, float* descender, float* height, float* lineGap, int* glyphCount) {
    ftFontData* fdata = (ftFontData*)(ctx);
    if (ascender != 0) *ascender = fdata->ascent;
    if (descender != 0) *descender = fdata->descent;
    if (height != 0) *height = fdata->height;
    if (lineGap != 0) *lineGap = fdata->lineGap;
    if (glyphCount != 0) *glyphCount = fdata->glyphCount;
}

fvGlyph& fontGlyph(void* ctx, long unicode) {
    ftFontData* fdata = (ftFontData*)(ctx);

    int glyphIndex = stbtt_FindGlyphIndex(&fdata->info, unicode);
    fvGlyph& glyph = fdata->glyphs[glyphIndex];

    if (!glyph.enabled) {
        __loadGlyph(fdata, glyphIndex);
    }

    return glyph;
}

fvGlyph& fontGlyphRendered(void* ctx, fvFont* font, long unicode, fvPoint* uv, int* recreate) {
    ftFontData* fdata = (ftFontData*)(ctx);

    int glyphIndex = stbtt_FindGlyphIndex(&fdata->info, unicode);

    if (font->renderState[glyphIndex].x == -1) {
        *recreate = __renderGlyph(fdata, font, glyphIndex);
    }

    (*uv) = font->renderState[glyphIndex];
    return fdata->glyphs[glyphIndex];
}

float fontKerning(void* ctx, long unicode1, long unicode2) {
    ftFontData* fdata = (ftFontData*)(ctx);

    return stbtt_GetCodepointKernAdvance(&fdata->info, unicode1, unicode2) * fdata->scale;
}