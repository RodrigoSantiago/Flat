//
// Created by Rodrigo on 21/10/2018.
//

#ifndef FLATVECTORS_CURVES_H
#define FLATVECTORS_CURVES_H

//------------------------------------------------------------------------
void tessQuad(double scale,
              double x1, double y1,
              double x2, double y2,
              double x3, double y3,
              void* data, void (*vertex)(void*, double x, double y));

//------------------------------------------------------------------------
void tessCubic(double scale,
               double x1, double y1,
               double x2, double y2,
               double x3, double y3,
               double x4, double y4,
               void* data, void (*vertex)(void*, double x, double y));

#endif //FLATVECTORS_CURVES_H
