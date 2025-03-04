//
// Created by Rodrigo on 04/02/2025.
//

#ifndef FLAT_PACK_H
#define FLAT_PACK_H

#include "flatvectors.h"

fvPack* packCreate(int cellWidth, int cellHeight);

void packDestroy(fvPack* pack);

void packToCellSize(fvPack* pack, int w, int h, int* cellW, int* cellH);

int packAddRect(fvPack* pack, int w, int h, fvPoint* point);

bool packGrow(fvPack* pack);

void packClear(fvPack* pack);

#endif //FLAT_PACK_H
