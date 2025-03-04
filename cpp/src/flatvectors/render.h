//
// Created by Rodrigo on 25/10/2018.
//

#ifndef FLATVECTORS_RENDER_H
#define FLATVECTORS_RENDER_H

#include <flatvectors.h>

void* renderCreate();

int renderAlign();

void renderAlloc(void* data, int paint, int element, int vertex);

void renderDestroy(void *data);

void renderBegin(void *data, unsigned int width, unsigned int height);

void renderEnd(void *data);

void renderClearClip(void* data, int clip);

void renderUnbindImage(void *data);

void renderFlush(void *data,
                 fvPaint *paints, void* uniforms, int pSize,
                 int* elements, int eSize,
                 float *vtx, float *uvs, int vSize);

unsigned long renderCreateFontTexture(int width, int height);

unsigned long renderResizeFontTexture(unsigned long oldImageID, int oldWidth, int oldHeight, int width, int height);

void renderUpdateFontTexture(unsigned long imageID, void* data, int x, int y, int width, int height);

void renderDestroyFontTexture(unsigned long imageID);

#endif //FLATVECTORS_RENDER_H