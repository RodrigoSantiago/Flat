//
// Created by Rodrigo on 20/10/2018.
//
#include "flatvectors.h"
#include "render.h"
#include "curves.h"
#include "triangulate.h"
#include "utf8.h"
#include "font.h"
#include <string.h>
#include <stdlib.h>
#include <math.h>
#include <iostream>

#define EPSILON 0.00001
#define PI 3.14159265359f
#define PI2 6.28318530718f

float fv__angle(float x1, float y1, float x2, float y2) {
    return atan2(y2 - y1, x2 - x1);
}

int fv__equals(double x1, double y1, double x2, double y2) {
    return abs(x2 - x1) <= EPSILON && abs(y2 - y1) <= EPSILON;
}

void fv__transform(float* t, float x, float y, float* dx, float* dy) {
    *dx = x * t[0] + y * t[2] + t[4];
    *dy = x * t[1] + y * t[3] + t[5];
}

void fv__affineToMat4(float* m3, const float* affine) {
    m3[8] = affine[4];
    m3[9] = affine[5];

    m3[4] = affine[2];
    m3[5] = affine[3];

    m3[0] = affine[0];
    m3[1] = affine[1];

    m3[2] = 0.0f;
    m3[3] = 0.0f;
    m3[6] = 0.0f;
    m3[7] = 0.0f;
    m3[10] = 1.0f;
    m3[11] = 0.0f;
}

void fv__inverse(float* inv, const float* t) {
    double invdet, det = (double) t[0] * t[3] - (double) t[2] * t[1];
    invdet = 1.0 / det;
    float inv0 = (float) (t[3] * invdet);
    float inv2 = (float) (-t[2] * invdet);
    float inv4 = (float) (((double) t[2] * t[5] - (double) t[3] * t[4]) * invdet);
    float inv1 = (float) (-t[1] * invdet);
    float inv3 = (float) (t[0] * invdet);
    float inv5 = (float) (((double) t[1] * t[4] - (double) t[0] * t[5]) * invdet);
    inv[0] = inv0;
    inv[1] = inv1;
    inv[2] = inv2;
    inv[3] = inv3;
    inv[4] = inv4;
    inv[5] = inv5;
}

void fv__multiply(float* t, float* s) {
    float t0 = t[0] * s[0] + t[1] * s[2];
    float t2 = t[2] * s[0] + t[3] * s[2];
    float t4 = t[4] * s[0] + t[5] * s[2] + s[4];
    t[1] = t[0] * s[1] + t[1] * s[3];
    t[3] = t[2] * s[1] + t[3] * s[3];
    t[5] = t[4] * s[1] + t[5] * s[3] + s[5];
    t[0] = t0;
    t[2] = t2;
    t[4] = t4;
}

float fv__maxscale(float* t) {
    float sx2 = t[0] * t[0] + t[1] * t[1];
    float sy2 = t[2] * t[2] + t[3] * t[3];
    if (sx2 > sy2) {
        return sqrt(sx2);
    } else {
        return sqrt(sy2);
    }
}

int fv__realloc(fvContext* ctx, int paint, int shape, int element, int vertex) {
    if (paint < 0 || shape < 0 || element < 0 || vertex < 0) return 0;

    if (ctx->MPAINT < paint) {
        int n = ctx->MPAINT == 0 ? 1 : ctx->MPAINT;
        while (n < paint) n *= 2;
        ctx->MPAINT = n;
        ctx->paints = (fvPaint *) realloc(ctx->paints, n * sizeof(fvPaint));
    }
    if (ctx->MSHAPE < shape) {
        int n = ctx->MSHAPE == 0 ? 1 : ctx->MSHAPE;
        while (n < shape) n *= 2;
        ctx->MSHAPE = n;
        ctx->shapes = (int *) realloc(ctx->shapes, n * sizeof(int));
    }
    if (ctx->MELEMENT < element) {
        int n = ctx->MELEMENT == 0 ? 1 : ctx->MELEMENT;
        while (n < element) n *= 2;
        ctx->MELEMENT = n;
        ctx->elements = (short *) realloc(ctx->elements, n * sizeof(short));
    }
    if (ctx->MVERTEX < vertex) {
        int n = ctx->MVERTEX == 0 ? 1 : ctx->MVERTEX;
        while (n < vertex) n *= 2;
        ctx->MVERTEX = n;
        ctx->vtx = (float *) realloc(ctx->vtx, n * sizeof(float));
        ctx->uvs = (float *) realloc(ctx->uvs, n * sizeof(float));
    }

    if (ctx->paints == 0 || ctx->shapes == 0 || ctx->elements == 0 || ctx->vtx == 0 || ctx->uvs == 0) {
        return 0;
    } else {
        renderAlloc(ctx->rCtx, paint, element, vertex);
        return 1;
    }
}

int fv__flush(fvContext* ctx) {
    if (ctx->pInd > 0) {
        renderFlush(ctx->rCtx, ctx->paints, ctx->pInd, ctx->elements, ctx->_eInd, ctx->vtx, ctx->uvs, ctx->_vInd);

        int offSet = (ctx->_vInd) / 2;
        for (int i = ctx->_eInd; i < ctx->eInd; i++) {
            ctx->elements[i - ctx->_eInd] = (short) (ctx->elements[i] - offSet);
        }
        memmove(&ctx->vtx[0], &ctx->vtx[ctx->_vInd], (ctx->vInd - ctx->_vInd) * sizeof(float));
        memmove(&ctx->uvs[0], &ctx->uvs[ctx->_vInd], (ctx->vInd - ctx->_vInd) * sizeof(float));
        ctx->vInd -= ctx->_vInd;
        ctx->mInd -= ctx->_vInd;

        ctx->eInd -= ctx->_eInd;
        ctx->_vInd = 0;
        ctx->_eInd = 0;
        ctx->pInd = 0;
        ctx->bInd = 0;
        return offSet;
    } else {
        return 0;
    }
}

int fv__assert(fvContext* ctx, int vertex, int element) {
    if (ctx->vInd + vertex * 2 >= ctx->MVERTEX || ctx->eInd + element * 3 >= ctx->MELEMENT) {
        return fv__flush(ctx);
    } else {
        return 0;
    }
}

int fv__vertex(fvContext* ctx, float x, float y) {
    if (ctx->vInd + 2 >= ctx->MVERTEX) {
        fv__flush(ctx);
        std::cout << "invalid flush v" << std::endl;
    }

    ctx->vtx[ctx->vInd] = x;
    ctx->vtx[ctx->vInd + 1] = y;
    ctx->vInd += 2;

    return (ctx->vInd - 2) / 2;
}

void fv__text_vertex(fvContext* ctx, float x, float y, float u, float v) {
    if (ctx->vInd + 2 >= ctx->MVERTEX) {
        fv__flush(ctx);
        std::cout << "invalid flush tv" << std::endl;
    }

    ctx->vtx[ctx->vInd] = x;
    ctx->vtx[ctx->vInd + 1] = y;
    ctx->uvs[ctx->vInd] = u;
    ctx->uvs[ctx->vInd + 1] = v;
    ctx->vInd += 2;
}

void fv__triangle(fvContext* ctx, int e01, int e02, int e03) {
    if (ctx->eInd + 3 >= ctx->MELEMENT) {
        int offSet = fv__flush(ctx);
        e01 -= offSet;
        e02 -= offSet;
        e03 -= offSet;
        std::cout << "invalid flush el" << std::endl;
    }

    ctx->elements[ctx->eInd] = (short) e01;
    ctx->elements[ctx->eInd + 1] = (short) e02;
    ctx->elements[ctx->eInd + 2] = (short) e03;
    ctx->eInd += 3;
}

void fv__expandline(float w, float x1, float y1, float x2, float y2,
                    float& px1, float& py1, float& px2, float& py2,
                    float& sx1, float& sy1, float& sx2, float& sy2) {
    w /= 2;
    float l = sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    float y = w * (y2 - y1) / l;
    float x = w * (x1 - x2) / l;

    px1 = x1 + y;
    sx1 = x2 + y;
    py1 = y1 + x;
    sy1 = y2 + x;

    px2 = x1 - y;
    sx2 = x2 - y;
    py2 = y1 - x;
    sy2 = y2 - x;
}

void fv__mitterpoint (float mt2, float cx, float cy,
                      float x1, float y1, float x2, float y2,
                      float x3, float y3, float x4, float y4, float& px, float &py) {
    float x12 = x1 - x2;
    float x34 = x3 - x4;
    float y12 = y1 - y2;
    float y34 = y3 - y4;

    float c = x12 * y34 - y12 * x34;

    if (fabs(c) < 0.01) {
        px = (x2 + x3) / 2;
        py = (y2 + y3) / 2;
    } else {
        float a = x1 * y2 - y1 * x2;
        float b = x3 * y4 - y3 * x4;

        px = (a * x34 - b * x12) / c;
        py = (a * y34 - b * y12) / c;

        if (((px - cx) * (px - cx) + (py - cy) * (py - cy)) > mt2) {
            px = (x2 + x3) / 2;
            py = (y2 + y3) / 2;
        }
    }
}

void fv__round(fvContext* ctx, float xc, float yc, float w, float s, float e, int pEl) {
    float v = (abs(w * fv__maxscale(ctx->transform)) * 0.8f + 8);
    v = (abs(e) / PI2) * (v < 8 ? 8 : v > 24 ? 24 : v);
    int n = (int) ceil(v < 2 ? 2 : v);

    pEl -= fv__assert(ctx, n + 1, n);

    int cEl = (ctx->vInd / 2);
    fv__vertex(ctx, xc, yc);

    for (int i = 1; i <= n; i += 1) {
        float u = i / (float) n;
        float a = s + e * u;

        int el = (ctx->vInd / 2);
        fv__vertex(ctx, xc + cos(a) * w, yc + sin(a) * w);
        fv__triangle(ctx, cEl, pEl, el);
        pEl = el;
    }
}

void fv__linejoin(fvContext* ctx, float x1, float y1,
                  float _px1, float _sx1, float _py1, float _sy1,
                  float _px2, float _sx2, float _py2, float _sy2, fvJoin join) {

    // Remove unecessaris closer joints
    float tsx1, tsy1, tsx2, tsy2, tpx1, tpy1, tpx2, tpy2;
    fv__transform(ctx->transform, ctx->sx1, ctx->sy1, &tsx1, &tsy1);
    fv__transform(ctx->transform, ctx->sx2, ctx->sy2, &tsx2, &tsy2);
    fv__transform(ctx->transform, _px1, _py1, &tpx1, &tpy1);
    fv__transform(ctx->transform, _px2, _py2, &tpx2, &tpy2);
    if (abs(tsx1 - tpx1) < 0.5 && abs(tsx2 - tpx2) < 0.5 &&
        abs(tsy1 - tpy1) < 0.5 && abs(tsy2 - tpy2) < 0.5) {
        return;
    }

    if (join == fvJoin::JOIN_BEVEL) {
        fv__assert(ctx, 2, 2);

        fv__vertex(ctx, _px1, _py1);
        fv__vertex(ctx, _px2, _py2);

        int el = (ctx->vInd / 2) - 4;
        fv__triangle(ctx, el, el + 1, el + 2);
        fv__triangle(ctx, el, el + 3, el + 1);
    } else if (join == fvJoin::JOIN_MITER) {
        fv__assert(ctx, 5, 4);

        float mx1, my1, mx2, my2;
        fv__mitterpoint(ctx->mt2, x1, y1, ctx->px1, ctx->py1, ctx->sx1, ctx->sy1, _px1, _py1, _sx1, _sy1, mx1, my1);
        fv__mitterpoint(ctx->mt2, x1, y1, ctx->px2, ctx->py2, ctx->sx2, ctx->sy2, _px2, _py2, _sx2, _sy2, mx2, my2);
        fv__vertex(ctx, mx1, my1);
        fv__vertex(ctx, x1, y1);
        fv__vertex(ctx, mx2, my2);
        fv__vertex(ctx, _px1, _py1);
        fv__vertex(ctx, _px2, _py2);

        int el = (ctx->vInd / 2) - 7;
        fv__triangle(ctx, el, el + 2, el + 3);
        fv__triangle(ctx, el + 2, el + 5, el + 3);
        fv__triangle(ctx, el + 1, el + 3, el + 4);
        fv__triangle(ctx, el + 4, el + 3, el + 6);
    } else if (join == fvJoin::JOIN_ROUND) {
        float s = fv__angle(ctx->sx1, ctx->sy1, x1, y1);
        float e = fv__angle(_px1, _py1, x1, y1);
        float a = e - s;
        if (a > PI) {
            a -= PI2;
        } else if (a < -PI) {
            a += PI2;
        }

        if (abs(a) < 0.25) {
            fv__assert(ctx, 2, 2);

            fv__vertex(ctx, _px1, _py1);
            fv__vertex(ctx, _px2, _py2);

            int fInd = (ctx->vInd / 2) - 4;
            fv__triangle(ctx, fInd, fInd + 1, fInd + 2);
            fv__triangle(ctx, fInd, fInd + 3, fInd + 1);
        } else {
            if (a < 0) {
                fv__round(ctx, x1, y1, ctx->stroker.width / 2, s, a, (ctx->vInd / 2) - 1);
            } else {
                fv__round(ctx, x1, y1, -ctx->stroker.width / 2, s, a, (ctx->vInd / 2) - 2);
            }

            fv__assert(ctx, 2, 0);

            fv__vertex(ctx, _px1, _py1);
            fv__vertex(ctx, _px2, _py2);
        }
    }
}

void fv__linestroke(fvContext* ctx, int first, float x1, float y1, float x2, float y2, fvJoin join) {
    float _px1, _sx1, _py1, _sy1, _px2, _sx2, _py2, _sy2;
    fv__expandline(ctx->stroker.width, x1, y1, x2, y2, _px1, _py1, _px2, _py2, _sx1, _sy1, _sx2, _sy2);

    if (!first) {
        fv__linejoin(ctx, x1, y1, _px1, _sx1, _py1, _sy1, _px2, _sx2, _py2, _sy2, join);
    } else {
        fv__assert(ctx, 2, 0);

        fv__vertex(ctx, _px1, _py1);
        fv__vertex(ctx, _px2, _py2);
    }

    fv__assert(ctx, 2, 2);

    fv__vertex(ctx, _sx1, _sy1);
    fv__vertex(ctx, _sx2, _sy2);
    int el = (ctx->vInd / 2) - 4;
    fv__triangle(ctx, el, el + 2, el + 3);
    fv__triangle(ctx, el, el + 1, el + 3);

    ctx->px1 = _px1;
    ctx->py1 = _py1;
    ctx->px2 = _px2;
    ctx->py2 = _py2;
    ctx->sx1 = _sx1;
    ctx->sy1 = _sy1;
    ctx->sx2 = _sx2;
    ctx->sy2 = _sy2;

    if (ctx->ft == 0) {
        ctx->ft = 1;
        ctx->fx = x2;
        ctx->fy = y2;
    }
}

void fv__linejoincap(fvContext* ctx, float x, float y) {
    float _px1, _sx1, _py1, _sy1, _px2, _sx2, _py2, _sy2;
    fv__expandline(ctx->stroker.width, x, y, ctx->fx, ctx->fy, _px1, _py1, _px2, _py2, _sx1, _sy1, _sx2, _sy2);
    fv__linejoin(ctx, x, y, _px1, _sx1, _py1, _sy1, _px2, _sx2, _py2, _sy2, ctx->stroker.join);
}

void fv__linecap(fvContext* ctx) {
    if (ctx->stroker.cap == CAP_BUTT) {

    } else if (ctx->stroker.cap == CAP_SQUARE) {
        float _px1, _sx1, _py1, _sy1, _px2, _sx2, _py2, _sy2;
        fv__expandline(ctx->stroker.width, ctx->mx, ctx->my, ctx->fx, ctx->fy,
                       _px1, _py1, _px2, _py2, _sx1, _sy1, _sx2, _sy2);
        fv__expandline(ctx->stroker.width, _px1, _py1, _px2, _py2,
                       _px1, _py1, _px2, _py2, _sx1, _sy1, _sx2, _sy2);
        fv__transform(ctx->transform, _px2, _py2, &_px2, &_py2);
        fv__transform(ctx->transform, _sx2, _sy2, &_sx2, &_sy2);
        ctx->vtx[ctx->mInd + 2] = _px2;
        ctx->vtx[ctx->mInd + 3] = _py2;
        ctx->vtx[ctx->mInd + 4] = _sx2;
        ctx->vtx[ctx->mInd + 5] = _sy2;

        fv__expandline(ctx->stroker.width, ctx->sx1, ctx->sy1, ctx->sx2, ctx->sy2,
                       _px1, _py1, _px2, _py2, _sx1, _sy1, _sx2, _sy2);
        fv__transform(ctx->transform, _px1, _py1, &_px1, &_py1);
        fv__transform(ctx->transform, _sx1, _sy1, &_sx1, &_sy1);
        ctx->vtx[ctx->vInd - 4] = _px1;
        ctx->vtx[ctx->vInd - 3] = _py1;
        ctx->vtx[ctx->vInd - 2] = _sx1;
        ctx->vtx[ctx->vInd - 1] = _sy1;
    } else if (ctx->stroker.cap == CAP_ROUND) {
        float a1 = fv__angle(ctx->sx2, ctx->sy2, ctx->sx1, ctx->sy1);
        fv__round(ctx, ctx->lx, ctx->ly, ctx->stroker.width / 2, a1, PI, (ctx->vInd / 2) - 2);

        float _px1, _sx1, _py1, _sy1, _px2, _sx2, _py2, _sy2;
        fv__expandline(ctx->stroker.width, ctx->mx, ctx->my, ctx->fx, ctx->fy,
                       _px1, _py1, _px2, _py2, _sx1, _sy1, _sx2, _sy2);
        float a2 = fv__angle(_sx1, _sy1, _sx2, _sy2);
        fv__round(ctx, ctx->mx, ctx->my, ctx->stroker.width / 2, a2, PI, (ctx->mInd / 2) + 2);
    }
}

void fv__curvestroke(void* data, double x, double y) {
    fvContext *ctx = (fvContext *) data;
    if (fv__equals(x, y, ctx->cx, ctx->cy)) {
        ctx->cp = 3;
    } else {
        fv__linestroke(ctx, ctx->cp == 0, ctx->cx, ctx->cy, (float) x, (float) y,
                       ctx->cp == 3 ? JOIN_ROUND :
                       (ctx->cp == 2 ? JOIN_ROUND : ctx->stroker.join));
        ctx->cp = 2;
        ctx->cx = (float) x;
        ctx->cy = (float) y;
    }
}

void fv__curvepoint(void* data, double x, double y) {
    fvContext *ctx = (fvContext *) data;
    if (fv__equals(x, y, ctx->cx, ctx->cy)) return;

    fv__assert(ctx, 1, 0);

    fv__vertex(ctx, (float) x, (float) y);
    ctx->cx = (float) x;
    ctx->cy = (float) y;
}

void fv__commit(fvContext* ctx) {
    for (int i = ctx->bInd; i < ctx->vInd; i += 2) {
        float x = ctx->vtx[i], y = ctx->vtx[i + 1];
        ctx->vtx[i] = x * ctx->transform[0] + y * ctx->transform[2] + ctx->transform[4];
        ctx->vtx[i + 1] = x * ctx->transform[1] + y * ctx->transform[3] + ctx->transform[5];
    }

    if (ctx->pInd + 1 >= ctx->MPAINT) {
        fv__flush(ctx);
    }

    fvPaint drawpaint = ctx->paint;

    drawpaint.size = (unsigned  long) ctx->eInd;

    if (ctx->op == TEXT) {
        drawpaint.edgeAA = (ctx->op << 2u) | ((ctx->font->sdf ? 1u : 0u) << 1u) | (ctx->aa ? 1u : 0u);

        if (drawpaint.type == 0) {
            drawpaint.type = 3;
        }
        if (drawpaint.type == 1) {
            drawpaint.type = 2;
        }
        drawpaint.image1 = ctx->font->imageID;
    } else {
        drawpaint.edgeAA = (ctx->op << 2u) | (0u << 1u) | (ctx->aa ? 1u : 0u);
        drawpaint.image1 = 0;
    }

    fv__multiply(drawpaint.colorMat, ctx->transform);
    fv__inverse(drawpaint.colorMat, drawpaint.colorMat);
    fv__affineToMat4(drawpaint.colorMat, drawpaint.colorMat);

    fv__multiply(drawpaint.imageMat, ctx->transform);
    fv__inverse(drawpaint.imageMat, drawpaint.imageMat);
    fv__affineToMat4(drawpaint.imageMat, drawpaint.imageMat);

    ctx->paints[ctx->pInd] = drawpaint;
    ctx->pInd++;
    ctx->_vInd = ctx->vInd;
    ctx->_eInd = ctx->eInd;

    ctx->op = NOONE;
}

fvContext* fvCreate() {
    fvContext *ctx = (fvContext *) malloc(sizeof(fvContext));
    memset(ctx, 0, sizeof(fvContext));

    // Settings
    ctx->font = NULL;
    ctx->fontScale = 1;
    ctx->fontSpacing = 1;
    ctx->paint = {};
    ctx->op = NOONE;
    ctx->transform[0] = 1.0f;
    ctx->transform[1] = 0.0f;
    ctx->transform[2] = 0.0f;
    ctx->transform[3] = 1.0f;
    ctx->transform[4] = 0.0f;
    ctx->transform[5] = 0.0f;

    ctx->stroker = {1, CAP_BUTT, JOIN_MITER, 10};
    ctx->mt2 = (ctx->stroker.width * ctx->stroker.miterLimit / 2) * (ctx->stroker.width * ctx->stroker.miterLimit / 2);

    ctx->lt = -1;
    ctx->pInd = 0;
    ctx->vInd = 0;
    ctx->eInd = 0;
    ctx->mInd = 0;
    ctx->_vInd = 0;
    ctx->_eInd = 0;

    ctx->rCtx = renderCreate();

    ctx->paints = 0;
    ctx->elements = 0;
    ctx->vtx = 0;
    ctx->uvs = 0;
    ctx->shapes = 0;

    ctx->MPAINT = 0;
    ctx->MSHAPE = 0;
    ctx->MELEMENT = 0;
    ctx->MVERTEX = 0;

    fv__realloc(ctx, 64, 256, 3072, 2048);

    return ctx;
}

void fvDestroy(fvContext* ctx) {
    renderDestroy(ctx->rCtx);
    free(ctx);
}

void fvBegin(fvContext* ctx, int width, int height) {
    ctx->width = width;
    ctx->height = height;

    ctx->lt = -1;
    ctx->pInd = 0;
    ctx->vInd = 0;
    ctx->eInd = 0;
    ctx->bInd = 0;
    ctx->mInd = 0;
    ctx->_vInd = 0;
    ctx->_eInd = 0;

    renderBegin(ctx->rCtx, width, height);
}

void fvFlush(fvContext* ctx) {
    fv__flush(ctx);
}

void fvEnd(fvContext* ctx) {
    fvFlush(ctx);
    renderEnd(ctx->rCtx);
}

void fvAntiAlias(fvContext* ctx, int enabled) {
    ctx->aa = enabled > 0 ? 1 : 0;
}

void fvSetPaint(fvContext* ctx, fvPaint paint) {
    ctx->paint = paint;
}

void fvSetStroker(fvContext* ctx, fvStroker stroker) {
    ctx->stroker = stroker;
    ctx->mt2 = (ctx->stroker.width * ctx->stroker.miterLimit / 2) * (ctx->stroker.width * ctx->stroker.miterLimit / 2);
}

void fvSetTransform(fvContext* ctx, float m00, float m10, float m01, float m11, float m02, float m12) {
    ctx->transform[0] = m00;
    ctx->transform[1] = m10;
    ctx->transform[2] = m01;
    ctx->transform[3] = m11;
    ctx->transform[4] = m02;
    ctx->transform[5] = m12;
}

void fvClearClip(fvContext* ctx, int clip) {
    renderClearClip(ctx->rCtx, clip);
}

void fvPathBegin(fvContext* ctx, fvPathOp op) {
    ctx->op = op;
    ctx->lt = -1;
    ctx->bInd = ctx->vInd;
    ctx->sInd = 0;
}

void fvPathMoveTo(fvContext* ctx, float x, float y) {
    ctx->mx = x;
    ctx->my = y;
    if (ctx->op == STROKE) {
        if (ctx->lt != -1) {
            fv__linecap(ctx);
        }
        ctx->ft = 0;

        fv__assert(ctx, 1, 0);

        ctx->mInd = ctx->vInd;
        fv__vertex(ctx, x, y);
    } else {
        fv__assert(ctx, 1, 0);

        ctx->mInd = ctx->vInd;
        fv__vertex(ctx, x, y);
    }

    ctx->lt = 0;
    ctx->lx = x;
    ctx->ly = y;
}

void fvPathLineTo(fvContext* ctx, float x, float y) {
    if (fv__equals(x, y, ctx->lx, ctx->ly)) return;

    if (ctx->op == STROKE) {
        fv__linestroke(ctx, ctx->lt == 0, ctx->lx, ctx->ly, x, y, ctx->stroker.join);
    } else {
        fv__assert(ctx, 1, 0);

        fv__vertex(ctx, x, y);
    }
    ctx->lt = 1;
    ctx->lx = x;
    ctx->ly = y;
}

void fvPathQuadTo(fvContext* ctx, float cx, float cy, float x, float y) {
    if (ctx->op == STROKE) {
        ctx->cp = ctx->lt == 0 ? 0 : 1;
        ctx->cx = ctx->lx;
        ctx->cy = ctx->ly;
        tessQuad(fv__maxscale(ctx->transform), ctx->lx, ctx->ly, cx, cy, x, y, ctx, fv__curvestroke);
    } else {
        ctx->cx = ctx->lx;
        ctx->cy = ctx->ly;
        tessQuad(fv__maxscale(ctx->transform), ctx->lx, ctx->ly, cx, cy, x, y, ctx, fv__curvepoint);
    }
    ctx->lt = 2;
    ctx->lx = x;
    ctx->ly = y;
}

void fvPathCubicTo(fvContext* ctx, float cx1, float cy1, float cx2, float cy2, float x, float y) {
    if (ctx->op == STROKE) {
        ctx->cp = ctx->lt == 0 ? 0 : 1;
        ctx->cx = ctx->lx;
        ctx->cy = ctx->ly;
        tessCubic(fv__maxscale(ctx->transform), ctx->lx, ctx->ly, cx1, cy1, cx2, cy2, x, y, ctx, fv__curvestroke);
    } else {
        ctx->cx = ctx->lx;
        ctx->cy = ctx->ly;
        tessCubic(fv__maxscale(ctx->transform), ctx->lx, ctx->ly, cx1, cy1, cx2, cy2, x, y, ctx, fv__curvepoint);
    }
    ctx->lt = 3;
    ctx->lx = x;
    ctx->ly = y;
}

void fvPathClose(fvContext* ctx) {
    if (ctx->op == STROKE) {
        fvPathLineTo(ctx, ctx->mx, ctx->my);
        fv__linejoincap(ctx, ctx->mx, ctx->my);
    } else {
        if (fv__equals(ctx->lx, ctx->ly, ctx->mx, ctx->my)) {
            ctx->vInd -= 2;
        }
        ctx->shapes[ctx->sInd] = ctx->vInd - ctx->mInd;
        ctx->sInd++;
    }
    ctx->lt = -1;
}

void fvPathEnd(fvContext* ctx) {
    if (ctx->op == TEXT) {

    } else if (ctx->op == STROKE) {
        if (ctx->lt != -1) {
            fv__linecap(ctx);
        }
    } else {
        if (ctx->lt != -1) {
            fvPathClose(ctx);
        }

        fv__assert(ctx, 0, (ctx->vInd - ctx->bInd) / 2);

        ctx->eInd += triangulate(ctx->vtx, ctx->bInd, ctx->shapes, ctx->sInd, ctx->elements, ctx->eInd);
    }

    fv__commit(ctx);
}

void fvRect(fvContext* ctx, float x, float y, float width, float height) {
    fvPathBegin(ctx, fvPathOp::FILL);

    fv__assert(ctx, 4, 2);

    int el = (ctx->vInd / 2);

    fv__vertex(ctx, x, y);
    fv__vertex(ctx, x + width, y);
    fv__vertex(ctx, x + width, y + height);
    fv__vertex(ctx, x, y + height);
    fv__triangle(ctx, el, el + 1, el + 2);
    fv__triangle(ctx, el, el + 2, el + 3);

    fv__commit(ctx);
}

void fvEllipse(fvContext* ctx, float x, float y, float width, float height) {
    fvPathBegin(ctx, fvPathOp::FILL);
    float points = fv__maxscale(ctx->transform) * sqrt(width*width + height*height);
    points = (ceil)((points < 64 ? 64 : points > 256 ? 256 : points) / 4.0f);
    int n = (int) points;

    fv__assert(ctx, n, n - 1);

    int el = (ctx->vInd / 2);

    float dtr = PI / 180.0f;
    float hw = width / 2.0f, hh = height / 2.0f;
    float xc = x + hw, yc = y + hh;
    for (int i = 0; i < n ; i++) {
        float a = (i/(float)n*360) * dtr;
        fv__vertex(ctx, xc + cos(a) * hw, yc - sin(a) * hh);
    }
    for (int i = 1; i < n - 1; i++) {
        fv__triangle(ctx, el, el + i, el + i + 1);
    }
    fv__commit(ctx);
}

void fvRoundRect(fvContext* ctx, float x, float y, float width, float height, float c1, float c2, float c3, float c4) {
    fvPathBegin(ctx, fvPathOp::FILL);
    fv__assert(ctx, 40, 39);

    int el = (ctx->vInd / 2);

    float dtr = PI / 180.0f;

    float xc = x + c1, yc = y + c1;
    for (int i = 90; i <= 180; i+= 10) {
        float a = i * dtr;
        fv__vertex(ctx, xc + cos(a) * c1, yc - sin(a) * c1);
    }
    xc = x + c4, yc = y + height - c4;
    for (int i = 180; i <= 270; i+= 10) {
        float a = i * dtr;
        fv__vertex(ctx, xc + cos(a) * c4, yc - sin(a) * c4);
    }
    xc = x + width - c3, yc = y + height - c3;
    for (int i = 270; i <= 360; i+= 10) {
        float a = i * dtr;
        fv__vertex(ctx, xc + cos(a) * c3, yc - sin(a) * c3);
    }
    xc = x + width - c2, yc = y + c2;
    for (int i = 0; i <= 90; i+= 10) {
        float a = i * dtr;
        fv__vertex(ctx, xc + cos(a) * c2, yc - sin(a) * c2);
    }
    for (int i = 2; i < 39; i++) {
        fv__triangle(ctx, el, el + i, el + i + 1);
    }
    fv__commit(ctx);
}

fvFont* fvFontCreate(void* data, long length, float size, int sdf) {
    return fontCreate(data, length, size, sdf);
}

void fvFontDestroy(fvFont* font) {
    fontDestroy(font);
}

void fvFontLoadGlyphs(fvFont* font, const char* str, int strLen) {
    fontLoadGlyphs(font, str, strLen);
}

void fvFontLoadAllGlyphs(fvFont* font) {
    fontLoadAllGlyphs(font);
}

int fvFontGetGlyphs(fvFont* font, const char* str, int strLen, float* info) {
    int i = 0, f = 0, index = 0, count;
    unsigned long chr = 0, prev = 0;
    while (utf8loop(str, strLen, i, chr)) {
        fvGlyph& glyph = fontGlyph(font, chr);
        if (glyph.enabled) {
            info[index++] = f ? fontKerning(font, prev, chr) : 0;
            info[index++] = glyph.enabled;
            info[index++] = glyph.advance;

            info[index++] = glyph.x;
            info[index++] = glyph.y;
            info[index++] = glyph.w;
            info[index++] = glyph.h;

            info[index++] = glyph.u;
            info[index++] = glyph.v;
            prev = chr;
            f = 1;
            count++;
        }
    }
    return count;
}

void fvFontGetMetrics(fvFont* font, float* ascender, float* descender, float* height) {
    if (ascender != 0) *ascender = font->ascent;
    if (descender != 0) *descender = font->descent;
    if (height != 0) *height = font->height;
}

float fvFontGetTextWidth(fvFont* font, const char* str, int strLen, float scale, float spacing) {
    float scl = scale * spacing;

    float w = 0;
    int i = 0, f = 0;
    unsigned long chr = 0, prev = 0;
    while (utf8loop(str, strLen, i, chr)) {
        fvGlyph& glyph = fontGlyph(font, chr);
        if (glyph.enabled) {
            w += (glyph.advance + (f ? fontKerning(font, prev, chr) : 0));
            prev = chr;
            f = 1;
        }
    }
    return w * scl;
}

int fvFontGetOffset(fvFont* font, const char* str, int strLen, float scale, float spacing, float x) {
    float scl = scale * spacing;

    float w = 0;
    int i = 0, f = 0, pi = 0;
    unsigned long chr = 0, pchr = 0;
    while (utf8loop(str, strLen, i, chr)) {
        fvGlyph& glyph = fontGlyph(font, chr);
        if (glyph.enabled) {
            float advance = (glyph.advance + (f ? fontKerning(font, pchr, chr) : 0)) * scl;
            if (w + advance / 2 > x) {
                return pi;
            } else if (w + advance > x) {
                return i;
            }
            w += advance;
            pchr = chr;
            pi = i;
            f = 1;
        }
    }
    return i;
}
//-----------------------------------------
//
//-----------------------------------------

void fvSetFont(fvContext* ctx, fvFont* font) {
    ctx->font = font;
}

void fvSetFontScale(fvContext* ctx, float scale) {
    ctx->fontScale = scale;
}

void fvSetFontSpacing(fvContext* ctx, float spacing) {
    ctx->fontSpacing = spacing;
}

float fvText(fvContext* ctx, const char* str, int strLen, float x, float y, float maxWidth, fvHAlign hAlign, fvVAlign vAlign) {
    fvFont *font = ctx->font;
    maxWidth = ceil(maxWidth);

    float start = x;
    int fEl = ctx->vInd / 2;

    float scl = ctx->fontScale, spc = ctx->fontSpacing;

    if (vAlign == fvVAlign::BOTTOM) {
        y -= font->height * scl;
    } else if (vAlign == fvVAlign::MIDDLE) {
        y -= font->height / 2 * scl;
    } else if (vAlign == fvVAlign::BASELINE) {
        y -= font->ascent * scl;
    }

    int i = 0, f = 0;
    unsigned long chr = 0, prev = 0;
    while (utf8loop(str, strLen, i, chr)) {
        fvGlyph &glyph = fontGlyphRendered(font, chr);
        if (glyph.enabled) {
            float kern = (f ? fontKerning(font, prev, chr) * scl * spc : 0);
            float advance = glyph.advance * scl * spc;
            if (maxWidth > 0 && floor(x + kern + advance - start) > maxWidth) {
                break;
            }
            x += kern;
            if (glyph.u > -1) {
                float x1 = x + glyph.x * scl, y1 = y + glyph.y * scl;
                float x2 = x1 + (glyph.w * scl), y2 = y1 + (glyph.h * scl);

                fEl -= fv__assert(ctx, 4, 2);

                int el = (ctx->vInd / 2);
                fv__text_vertex(ctx, x1, y1, glyph.u, glyph.v);
                fv__text_vertex(ctx, x2, y1, glyph.u2, glyph.v);
                fv__text_vertex(ctx, x2, y2, glyph.u2, glyph.v2);
                fv__text_vertex(ctx, x1, y2, glyph.u, glyph.v2);
                fv__triangle(ctx, el, el + 1, el + 2);
                fv__triangle(ctx, el, el + 2, el + 3);
            }
            x += advance;

            prev = chr;
            f = 1;
        }
    }

    if (hAlign == fvHAlign::RIGHT) {
        float offset = x - start;
        int el = (ctx->vInd / 2);
        for (int i = fEl; i < el; i++) {
            ctx->vtx[i * 2] -= offset;
        }
    } else if (hAlign == fvHAlign::CENTER) {
        float offset = (x - start) / 2;
        int el = (ctx->vInd / 2);
        for (int i = fEl; i < el; i++) {
            ctx->vtx[i * 2] -= offset;
        }
    }
    return x - start;
}

//-----------------------------

void fv__identity(float* t) {
    t[0] = 1.0f;
    t[1] = 0.0f;
    t[2] = 0.0f;
    t[3] = 1.0f;
    t[4] = 0.0f;
    t[5] = 0.0f;
}

fvPaint fvColorPaint(long color) {
    fvPaint p = fvImagePaint(0, 0, color);
    p.type = 0;
    return p;
}

fvPaint fvImagePaint(unsigned long imageID, float* affineImg, long color) {
    fvPaint p{};
    p.type = 1;
    p.image0 = imageID;
    p.image1 = 0;
    if (affineImg != 0) {
        for (int i = 0; i < 6; i++) {
            p.imageMat[i] = affineImg[i];
        }
    } else {
        fv__identity(p.imageMat);
    }

    fv__identity(p.colorMat);

    p.shape[0] = 0;
    p.shape[1] = 0;
    p.shape[2] = 0;
    p.shape[3] = 0;

    p.stopCount = 0;
    p.joinType = 0;
    p.colors[0] = ((color >> 24) & 0xFF) / 255.f;
    p.colors[1] = ((color >> 16) & 0xFF) / 255.f;
    p.colors[2] = ((color >> 8) & 0xFF) / 255.f;
    p.colors[3] = ((color >> 0) & 0xFF) / 255.f;
    p.cycleType = 0;
    p.edgeAA = 0;

    return p;
}

fvPaint fvLinearGradientPaint(float* affine, float x1, float y1, float x2, float y2, int count, float* stops, long* colors, int cycleMethod) {
    fvPaint p = fvLinearGradientImagePaint(0, 0, affine, x1, y1, x2, y2, count, stops, colors, cycleMethod);
    p.type = 0;
    return p;
}

fvPaint fvRadialGradientPaint(float* affine, float x, float y, float rIn, float rOut, int count, float* stops, long* colors, int cycleMethod) {
    fvPaint p = fvRadialGradientImagePaint(0, 0, affine, x, y, rIn, rOut, count, stops, colors, cycleMethod);
    p.type = 0;
    return p;
}

fvPaint fvBoxGradientPaint(float* affine, float x, float y, float w, float h, float r, float f, int count, float* stops, long* colors, int cycleMethod) {
    fvPaint p = fvBoxGradientImagePaint(0, 0, affine, x, y, w, h, r, f, count, stops, colors, cycleMethod);
    p.type = 0;
    return p;
}

fvPaint fvLinearGradientImagePaint(unsigned long imageID, float* affineImg, float* affine, float x1, float y1, float x2, float y2, int count, float* stops, long* colors, int cycleMethod) {
    fvPaint p{};
    p.type = 1;
    p.image0 = imageID;
    p.image1 = 0;
    if (affineImg != 0) {
        for (int i = 0; i < 6; i++) {
            p.imageMat[i] = affineImg[i];
        }
    } else {
        fv__identity(p.imageMat);
    }

    float dx, dy, d;
    const float large = 1e5;

    dx = x2 - x1;
    dy = y2 - y1;
    d = sqrtf(dx * dx + dy * dy);
    if (d > 0.0001f) {
        dx /= d;
        dy /= d;
    } else {
        dx = 0;
        dy = 1;
    }

    p.colorMat[0] = dy;
    p.colorMat[1] = -dx;
    p.colorMat[2] = dx;
    p.colorMat[3] = dy;
    p.colorMat[4] = x1 - dx * large;
    p.colorMat[5] = y1 - dy * large;
    fv__multiply(p.colorMat, affine);

    p.shape[0] = large;
    p.shape[1] = large + d * 0.5f;
    p.shape[2] = 0.0f;
    p.shape[3] = d < 1.0f ? 1.0f : d;

    p.stopCount = count - 1;
    p.joinType = 0;
    for (int i = 0; i < count; i++) {
        p.stops[i] = stops[i];
        p.colors[i * 4] = ((colors[i] >> 24) & 0xFF) / 255.f;
        p.colors[i * 4 + 1] = ((colors[i] >> 16) & 0xFF) / 255.f;
        p.colors[i * 4 + 2] = ((colors[i] >> 8) & 0xFF) / 255.f;
        p.colors[i * 4 + 3] = ((colors[i] >> 0) & 0xFF) / 255.f;
    }
    p.cycleType = cycleMethod;
    p.edgeAA = 0;

    return p;
}

fvPaint fvRadialGradientImagePaint(unsigned long imageID, float* affineImg, float* affine, float x, float y, float rIn, float rOut, int count, float* stops, long* colors, int cycleMethod) {
    fvPaint p{};
    p.type = 1;
    p.image0 = imageID;
    p.image1 = 0;
    if (affineImg != 0) {
        for (int i = 0; i < 6; i++) {
            p.imageMat[i] = affineImg[i];
        }
    } else {
        fv__identity(p.imageMat);
    }

    float r = (rIn+rOut)*0.5f;
    float f = (rOut-rIn);

    p.colorMat[0] = 1.0f;
    p.colorMat[1] = 0.0f;
    p.colorMat[2] = 0.0f;
    p.colorMat[3] = 1.0f;
    p.colorMat[4] = x;
    p.colorMat[5] = y;
    fv__multiply(p.colorMat, affine);

    p.shape[0] = r;
    p.shape[1] = r;
    p.shape[2] = r;
    p.shape[3] = f < 1.0f ? 1.0f : f;

    p.stopCount = count - 1;
    p.joinType = 0;
    for (int i = 0; i < count; i++) {
        p.stops[i] = stops[i];
        p.colors[i * 4] = ((colors[i] >> 24) & 0xFF) / 255.f;
        p.colors[i * 4 + 1] = ((colors[i] >> 16) & 0xFF) / 255.f;
        p.colors[i * 4 + 2] = ((colors[i] >> 8) & 0xFF) / 255.f;
        p.colors[i * 4 + 3] = ((colors[i] >> 0) & 0xFF) / 255.f;
    }
    p.cycleType = cycleMethod;
    p.edgeAA = 0;

    return p;
}

fvPaint fvBoxGradientImagePaint(unsigned long imageID, float* affineImg, float* affine, float x, float y, float w, float h, float r, float f, int count, float* stops, long* colors, int cycleMethod) {
    fvPaint p{};
    p.type = 1;
    p.image0 = imageID;
    p.image1 = 0;
    if (affineImg != 0) {
        for (int i = 0; i < 6; i++) {
            p.imageMat[i] = affineImg[i];
        }
    } else {
        fv__identity(p.imageMat);
    }

    p.colorMat[0] = 1.0f;
    p.colorMat[1] = 0.0f;
    p.colorMat[2] = 0.0f;
    p.colorMat[3] = 1.0f;
    p.colorMat[4] = x + w * 0.5f;
    p.colorMat[5] = y + h * 0.5f;
    fv__multiply(p.colorMat, affine);

    p.shape[0] = w * 0.5f;
    p.shape[1] = h * 0.5f;
    p.shape[2] = r;
    p.shape[3] = f < 1.0f ? 1.0f : f;

    p.stopCount = count - 1;
    p.joinType = 0;
    for (int i = 0; i < count; i++) {
        p.stops[i] = stops[i];
        p.colors[i * 4] = ((colors[i] >> 24) & 0xFF) / 255.f;
        p.colors[i * 4 + 1] = ((colors[i] >> 16) & 0xFF) / 255.f;
        p.colors[i * 4 + 2] = ((colors[i] >> 8) & 0xFF) / 255.f;
        p.colors[i * 4 + 3] = ((colors[i] >> 0) & 0xFF) / 255.f;
    }
    p.cycleType = cycleMethod;
    p.edgeAA = 0;

    return p;
}
