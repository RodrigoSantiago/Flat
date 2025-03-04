//
// Created by Rodrigo on 30/10/2018.
//

#include "font.h"
#include "utf8.h"
#include "render.h"

#include <unordered_map>
#define STB_TRUETYPE_IMPLEMENTATION
#include "stb_truetype.h"
#include <vector>
#include <algorithm>
#include <iostream>

#define PADDING 4
#define PADDING2 8

typedef struct ftFontData {
    stbtt_fontinfo info;
    unsigned char* data;
    float scale;
    int glyphCount;
    fvGlyph* glyphs;
} ftFontData;

struct Rect {
    fvGlyph* glyph;
    int w, h;

    bool operator < (const Rect& node) const {
        return (h > node.h);
    }
};

struct RectNode {
    int x, y, w, h;
    bool used;
    int right, down;
};

class Packer {
public:
    std::vector<RectNode> nodes;
    int root;

    int create(int x, int y, int w, int h) {
        int index = (int) nodes.size();
        nodes.push_back({x, y, w, h, false, -1, -1});
        return index;
    }

    Rect fit(std::vector<Rect> & blocks) {
        std::sort (blocks.begin(), blocks.end());

        int w = !blocks.empty() ? nextPower(1, blocks[0].w) : 1;
        int h = !blocks.empty() ? nextPower(1, blocks[0].h) : 1;
        root = create(0, 0, w, h);

        int node;
        for (auto block : blocks) {
            int rect;
            if ((node = findNode(root, block.w, block.h)) != -1) {
                rect = splitNode(node, block.w, block.h);
            } else {
                rect = growNode(block.w, block.h);
            }
            block.glyph->u = nodes[rect].x;
            block.glyph->v = nodes[rect].y;
        }

        return {nullptr, nodes[root].w, nodes[root].h};
    }

    int findNode(int root, int w, int h) {
        int node;
        if (nodes[root].used) {
            node = findNode(nodes[root].right, w, h);
            if (node == -1) {
                node = findNode(nodes[root].down, w, h);
            }
            return node;
        } else if ((w <= nodes[root].w) && (h <= nodes[root].h)) {
            return root;
        } else {
            return -1;
        }
    }

    int splitNode(int node, int w, int h) {
        int down = create(nodes[node].x, nodes[node].y + h, nodes[node].w, nodes[node].h - h);
        int right = create(nodes[node].x + w, nodes[node].y, nodes[node].w - w, h);
        nodes[node].used = true;
        nodes[node].down = down;
        nodes[node].right = right;
        return node;
    }

    int growNode(int w, int h) {
        bool canGrowDown = (w <= nodes[root].w);
        bool canGrowRight = (h <= nodes[root].h);

        bool shouldGrowRight = canGrowRight && (nodes[root].h >= (nodes[root].w + w));
        bool shouldGrowDown = canGrowDown && (nodes[root].w >= (nodes[root].h + h));

        if (shouldGrowRight) {
            return growRight(w, h);
        } else if (shouldGrowDown) {
            return growDown(w, h);
        } else if (canGrowRight) {
            return growRight(w, h);
        } else if (canGrowDown) {
            return growDown(w, h);
        } else {
            return -1;
        }
    }

    int growRight(int w, int h) {
        int oldRoot = root;

        int nw = nextPower(nodes[oldRoot].w, nodes[oldRoot].w + w) - nodes[oldRoot].w;

        root = create(0, 0, nodes[oldRoot].w + nw, nodes[oldRoot].h);
        nodes[root].used = true;
        nodes[root].down = oldRoot;
        int right = create(nodes[oldRoot].w, 0, nw, nodes[oldRoot].h);
        nodes[root].right = right;
        int node = findNode(root, w, h);
        if (node != -1) {
            return splitNode(node, w, h);
        } else {
            return -1;
        }
    }

    int growDown(int w, int h) {
        int oldRoot = root;

        int nh = nextPower(nodes[oldRoot].h, nodes[oldRoot].h + h) - nodes[oldRoot].h;

        root = create(0, 0, nodes[oldRoot].w, nodes[oldRoot].h + nh);
        nodes[root].used = true;
        nodes[root].right = oldRoot;
        int down = create(0, nodes[oldRoot].h, nodes[oldRoot].w, nh);;
        nodes[root].down = down;
        int node = findNode(root, w, h);
        if (node != -1) {
            return splitNode(node, w, h);
        } else {
            return -1;
        }
    }

    int nextPower(int val, int next) {
        if (val <= 0) val = 1;
        while (val < next) {
            val += val;
        }
        return val;
    }
};

fvFont* fontCreate(const void* data, long int length, float size, int sdf) {
    fvFont* ft = (fvFont*) malloc(sizeof(fvFont));
    ftFontData* fdata = (ftFontData*) malloc(sizeof(ftFontData));
    fdata->data = (unsigned char*) malloc(length);
    memcpy(fdata->data, data, length);

    if (!stbtt_InitFont(&fdata->info, fdata->data, 0)) {
        free(fdata->data);
        free(fdata);
        free(ft);
        return NULL;
    } else {
        ft->sdf = sdf;
        ft->size = size;
        ft->iw = 0;
        ft->ih = 0;
        ft->imageID = 0;

        fdata->scale = stbtt_ScaleForMappingEmToPixels(&fdata->info, size);//stbtt_ScaleForPixelHeight(&fdata->info, height);
        fdata->glyphCount = fdata->info.numGlyphs;
        fdata->glyphs = (fvGlyph*) calloc(fdata->glyphCount, sizeof(fvGlyph));

        int ascent, descent, lineGap;
        stbtt_GetFontVMetrics(&fdata->info, &ascent, &descent, &lineGap);
        ft->ascent = ascent * fdata->scale;
        ft->descent = descent * fdata->scale;
        ft->lineGap = lineGap * fdata->scale;
        ft->height = (ascent - descent) * fdata->scale;

        ft->fCtx = fdata;
        return ft;
    }

}

void fontDestroy(fvFont* font) {
    ftFontData* fdata = (ftFontData*)(font->fCtx);
    renderDestroyFontTexture(font->imageID);
    free(fdata->glyphs);
    free(fdata->data);
    free(fdata);
    free(font);
}

void __loadGlyph(fvFont* font, int glyphIndex) {
    ftFontData* fdata = (ftFontData*)(font->fCtx);

    int c_x1, c_y1, c_x2, c_y2;
    stbtt_GetGlyphBitmapBox(&fdata->info, glyphIndex, fdata->scale, fdata->scale, &c_x1, &c_y1, &c_x2, &c_y2);

    int ax, lb;
    stbtt_GetGlyphHMetrics(&fdata->info, glyphIndex, &ax, &lb);

    fvGlyph& glyph = fdata->glyphs[glyphIndex];
    glyph.enabled = 1;
    glyph.advance = ceil(ax * fdata->scale);
    glyph.x = c_x1 - PADDING;
    glyph.y = c_y1 + font->ascent - PADDING;
    glyph.w = c_x2 - c_x1 + PADDING2;
    glyph.h = c_y2 - c_y1 + PADDING2;
    glyph.u = glyph.v = glyph.u2 = glyph.v2 = -1;
}

void __renderGlyph(fvFont* font, int glyphIndex) {
    ftFontData *fdata = (ftFontData *) (font->fCtx);

    fvGlyph& glyph = fdata->glyphs[glyphIndex];

    int width = (int) ceil(glyph.w);
    int height = (int) ceil(glyph.h);

    if (width > 0 && height > 0) {
        int recW = width;
        int recH = height;
        unsigned char *img = (unsigned char *) malloc(recW * recH * sizeof(unsigned char));

        if (font->sdf) {
            int w, h, xof, yof;
            // todo direct make
            unsigned char *bmap = stbtt_GetGlyphSDF(&fdata->info, fdata->scale, glyphIndex, PADDING, 128, 16/*64*/, &w, &h, &xof, &yof);
            if (bmap != 0) {
                for (int y = 0; y < recH; y++) {
                    for (int x = 0; x < recW; x++) {
                        if (x >= 0 && x < w && y >= 0 && y < h) {
                            int p = x + y * recW;
                            img[p] = bmap[p];
                        } else {
                            img[x + y * recW] = 0;
                        }
                    }
                }
                stbtt_FreeSDF(bmap, &fdata->info.userdata);
            }
        } else {
            stbtt_MakeGlyphBitmap(&fdata->info, img + (recW + 1),
                                  width, height,
                                  recW, fdata->scale, fdata->scale, glyphIndex);
            // Padding
            int off = recW * (recH - PADDING);
            for (int i = 0; i < recW; i++) {
                img[i] = 0;
                img[off + i] = 0;
            }
            off = recW - PADDING;
            for (int i = 0, len = recW * recH; i < len; i+= recW) {
                img[i] = 0;
                img[i + off] = 0;
            }
        }

        if (font->imageID == 0) {
            font->imageID = renderCreateFontTexture(0, font->iw, font->ih);
        }

        renderUpdateFontTexture(font->imageID, img, (int) ceil(glyph.u), (int) ceil(glyph.v), recW, recH);

        free(img);

        glyph.u2 = (glyph.u + glyph.w) / font->iw;
        glyph.v2 = (glyph.v + glyph.h) / font->ih;
        glyph.u = (glyph.u) / font->iw;
        glyph.v = (glyph.v) / font->ih;
    }

    glyph.rendered = 1;
}

void fontLoadGlyphs(fvFont* font, const char* str, int strLen) {
    ftFontData *fdata = (ftFontData *) (font->fCtx);
    std::vector<Rect> rects;
    rects.reserve(fdata->glyphCount);

    int i = 0;
    unsigned long chr = 0;
    while (utf8loop(str, strLen, i, chr)) {
        int gIndex = stbtt_FindGlyphIndex(&fdata->info, chr);

        if (!fdata->glyphs[gIndex].enabled) {
            __loadGlyph(font, gIndex);

            fvGlyph glyph = fdata->glyphs[gIndex];
            if ((int) ceil(glyph.w) > 0 && (int) ceil(glyph.h) > 0) {
                rects.push_back({&fdata->glyphs[gIndex], (int) ceil(glyph.w + PADDING2), (int) ceil(glyph.h + PADDING2)});
            }
        }
    }

    Packer p = Packer();
    Rect size = p.fit(rects);

    font->iw = size.w;
    font->ih = size.h;
}

void fontLoadAllGlyphs(fvFont* font) {
    ftFontData* fdata = (ftFontData*)(font->fCtx);
    std::vector<Rect> rects;
    rects.reserve(fdata->glyphCount);

    for (int i = 0; i < fdata->glyphCount; i++) {
        __loadGlyph(font, i);

        fvGlyph glyph = fdata->glyphs[i];
        if ((int) ceil(glyph.w) > 0 && (int) ceil(glyph.h) > 0) {
            rects.push_back({&fdata->glyphs[i], (int) ceil(glyph.w + PADDING2), (int) ceil(glyph.h + PADDING2)});
        }
    }

    Packer p = Packer();
    Rect size = p.fit(rects);

    font->iw = size.w;
    font->ih = size.h;
}

fvGlyph& fontGlyph(fvFont* font, long unicode) {
    ftFontData* fdata = (ftFontData*)(font->fCtx);
    return fdata->glyphs[stbtt_FindGlyphIndex(&fdata->info, unicode)];
}

fvGlyph& fontGlyphRendered(fvFont* font, long unicode) {
    ftFontData* fdata = (ftFontData*)(font->fCtx);

    int index = stbtt_FindGlyphIndex(&fdata->info, unicode);
    fvGlyph& glyph = fdata->glyphs[index];
    if (glyph.enabled && !glyph.rendered) {
        __renderGlyph(font, index);
    }

    return fdata->glyphs[index];
}

float fontKerning(fvFont* font, long unicode1, long unicode2) {
    ftFontData* fdata = (ftFontData*)(font->fCtx);
    return stbtt_GetCodepointKernAdvance(&fdata->info, unicode1, unicode2) * fdata->scale;
}