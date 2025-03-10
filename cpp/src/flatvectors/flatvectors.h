//
// Created by Rodrigo on 20/10/2018.
//

#ifndef FLATVECTORS_FLATVECTORS_H
#define FLATVECTORS_FLATVECTORS_H

enum fvCap {
    CAP_BUTT, CAP_ROUND, CAP_SQUARE
};

enum fvJoin {
    JOIN_MITER, JOIN_ROUND, JOIN_BEVEL      //TODO - , ARCS, CLIP
};

enum fvPathOp {
    NOONE, CONVEX, FILL, STROKE, CLIP, TEXT
};

enum fvWindingRule {
    EVEN_ODD, NON_ZERO
};

typedef struct fvPoint {
    int x;
    int y;
} fvPoint;

typedef struct fvUniform {
    // Buffer
    float type;                 // [0] - Color|Grad, [1] - Color + Image, [2] - Text + Color, [3] - Text + Color + Image
    float joinType;             // Round / Bevel
    float cycleType;            // Clamp / Cycle / Reflect
    float stopCount;            // 0 -- 16
    float colorMat[12];
    float imageMat[12];
    float shape[4];             // Extent[0,1], Radius [2], Feather [3]
    float extra[4];             // Focus[0,1], Circle/Rect[2], Blur [3]
    float stops[16];
    float colors[64];
} fvUniform;

typedef struct fvGlyph {
    int enabled;
    float advance;
    long int unicode;
    void* cell;

    float x;
    float y;
    float w;
    float h;
} fvGlyph;

typedef struct fvCell {
    fvPoint* uvPtr;
    int w;
    int h;
} fvCell;

typedef struct fvPack {
    int cellWidth;
    int cellHeight;
    int width;
    int height;
    int widthCount;
    int heightCount;
    int minX;
    int minY;
    int clearQuad;
    fvCell* matrix;
} fvPack;

typedef struct fvFont {
    // Font Context
    void* fCtx;

    // Properties
    fvPack* pack;
    unsigned long imageID;
    fvPoint* renderState;

    int count;
    int sdf;
    float size;
    float height;
    float ascent;
    float descent;
    float lineGap;

} fvFont;

typedef struct fvPaint {
    unsigned long int size;

    fvWindingRule winding;
    fvPathOp paintOp;

    int aa;
    int convex;
    fvFont* font;

    unsigned long int image0;
    float mat[12];

    fvUniform uniform;
} fvPaint;

typedef struct fvStroker {
    float width;
    fvCap cap;
    fvJoin join;
    float miterLimit;

    float dashPhase;
    int dashCount;
    float* dash;
} fvStroker;

typedef struct fvContext {
    // Render Context
    void* rCtx;

    // Settings
    fvFont* font;
    fvPaint paint;
    fvStroker stroker;
    float transform[6];
    float fontScale;
    float fontSpacing;
    float fontBlur;
    int width, height;
    int aa;

    // Path Tesselation
    fvPathOp op;
    fvWindingRule wr;
    int convex; // Simple convex shape
    int mInd;   // Move index

    // Stroke
    float mt2;          // mitter square
    float cx, cy, cp;   // curve points
    float sx1, sy1, sx2, sy2, px1, py1, px2, py2;   // last line stroke points
    float lt, lx, ly;   // last operation
    float ft, fx, fy;   // first pos move operation [cap]
    float mx, my;       // move x move y

    float phaseNext, px, py, dSx, dSy, sft, sfx, sfy;
    int phaseIndex, phaseFill, open, dashCommand, startFill;

    int MPAINT;         // Max paints count
    int MSHAPE;         // Max shapes per path
    int MELEMENT;       // Max elements index
    int MVERTEX;        // Max vertex index

    // Paints
    fvPaint* paints;
    char* uniforms;
    int pInd;               // paint index

    // Triangulation
    int* shapes;
    int sInd, bInd;         // shade index, begin vertex index

    // Elements
    int* elements;
    int eInd,_eInd;         // element index, element commited index

    // Vertexes
    float* vtx;
    float* uvs;
    int vInd, _vInd;        // vertex index, vertex commited index
} fvContext;

fvContext* fvCreate();

void fvDestroy(fvContext* context);

void fvSetDebug(bool debug);

bool fvIsDebug();

void fvBegin(fvContext* context, int width, int height);

void fvFlush(fvContext* context);

void fvEnd(fvContext* context);

void fvAntiAlias(fvContext* context, int enable);

void fvSetPaint(fvContext* context, fvPaint paint);

void fvSetStroker(fvContext* context, fvStroker stroker);

void fvSetTransform(fvContext* context, float m00, float m10, float m01, float m11, float m02, float m12);

void fvClearClip(fvContext* context, int clip);

void fvPathBegin(fvContext* context, fvPathOp op, fvWindingRule wr);

void fvPathMoveTo(fvContext* context, float x, float y);

void fvPathLineTo(fvContext* context, float x, float y);

void fvPathQuadTo(fvContext* context, float cx, float cy, float x, float y);

void fvPathCubicTo(fvContext* context, float cx1, float cy1, float cx2, float cy2, float x, float y);

void fvPathClose(fvContext* context);

void fvPathEnd(fvContext* context);

void fvRect(fvContext* context, float x, float y, float width, float height);

void fvEllipse(fvContext* context, float x, float y, float width, float height);

void fvRoundRect(fvContext* context, float x, float y, float width, float height, float c1, float c2, float c3, float c4);
//-----------------------------------------
//
//-----------------------------------------

void* fvFontLoad(void* data, long int length, float size, int sdf);

void fvFontUnload(void* ctx);

fvFont* fvFontCreate(void* ctx);

void fvFontDestroy(fvFont* font);

long fvFontGetCurrentAtlas(fvFont* font, int* w, int* h);

void fvFontGetGlyphShape(void* ctx, long unicode, float** polygon, int* len);

void fvFontGetGlyph(void* ctx, int codePoint, float* info);

void fvFontGetAllCodePoints(void* ctx, long int* codePoints);

void fvFontGetMetrics(void* ctx, float* ascender, float* descender, float* height, float* lineGap, int* glyphCount);

float fvFontGetTextWidth(void* ctx, const char* str, int strLen, float size, float spacing);

void fvFontGetOffset(void* ctx, const char* str, int strLen, float size, float spacing, float x, int half, float* index, float* width);
//-----------------------------------------
//
//-----------------------------------------

void fvSetFont(fvContext* context, fvFont* font);

void fvSetFontScale(fvContext* context, float scale);

void fvSetFontSpacing(fvContext* context, float spacing);

void fvSetFontBlur(fvContext* context, float blur);

void fvText(fvContext* context, const char* str, int strLen, float x, float y, float maxWidth, float maxHeight);

//-----------------------------------------
//
//-----------------------------------------

fvPaint fvColorPaint(long color);

fvPaint fvImagePaint(unsigned long imageID, float* affine, long color, int cycleMethod);

fvPaint fvLinearGradientPaint(float* affine, float x1, float y1, float x2, float y2, int count, float* stops, long* colors, int cycleMethod);

fvPaint fvRadialGradientPaint(float* affine, float x, float y, float rIn, float rOut, float fx, float fy, int count, float* stops, long* colors, int cycleMethod);

fvPaint fvBoxGradientPaint(float* affine, float x, float y, float w, float h, float r, float f, float a, long c);

#endif //FLATVECTORS_FLATVECTORS_H
