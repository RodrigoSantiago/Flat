/*
    Anti-Grain Geometry - Version 2.4
    Copyright (C) 2002-2004 Maxim Shemanarev (McSeem)

    Permission to copy, use, modify, sell and distribute this software
    is granted provided this copyright notice appears in all copies.
    This software is provided "as is" without express or implied
    warranty, and with no claim as to its suitability for any purpose.
*/

#include <curves.h>
#include <cmath>

#define pi 3.14159265359

#define collinearity_epsilon 1e-30
#define angle_epsilon 0.01

#define m_angle 0
#define m_cusp 0
#define m_level 32

inline double distance2(double x1, double y1, double x2, double y2) {
    double dx = x2 - x1;
    double dy = y2 - y1;
    return dx * dx + dy * dy;
}

void quad(double m_distance,
          double x1, double y1,
          double x2, double y2,
          double x3, double y3,
          unsigned level, void* data, void (*vertex)(void*, double x, double y)) {
    if(level > m_level) {
        return;
    }

    double x12   = (x1 + x2) / 2;
    double y12   = (y1 + y2) / 2;
    double x23   = (x2 + x3) / 2;
    double y23   = (y2 + y3) / 2;
    double x123  = (x12 + x23) / 2;
    double y123  = (y12 + y23) / 2;

    double dx = x3-x1;
    double dy = y3-y1;
    double d = fabs(((x2 - x3) * dy - (y2 - y3) * dx));
    double da;

    if(d > collinearity_epsilon) {
        if(d * d <= m_distance * (dx*dx + dy*dy)) {
            if(m_angle < angle_epsilon) {
                vertex(data, x123, y123);
                return;
            }

            da = fabs(atan2(y3 - y2, x3 - x2) - atan2(y2 - y1, x2 - x1));
            if(da >= pi) da = 2*pi - da;

            if(da < m_angle) {
                vertex(data, x123, y123);
                return;
            }
        }
    } else {
        da = dx*dx + dy*dy;
        if(da == 0) {
            d = distance2(x1, y1, x2, y2);
        } else {
            d = ((x2 - x1) * dx + (y2 - y1) * dy) / da;
            if (d > 0 && d < 1) {
                return;
            }
            if (d <= 0) {
                d = distance2(x2, y2, x1, y1);
            } else if (d >= 1) {
                d = distance2(x2, y2, x3, y3);
            } else {
                d = distance2(x2, y2, x1 + d * dx, y1 + d * dy); // todo - unreacble ?
            }
        }
        if (d < m_distance) {
            vertex(data, x2, y2);
            return;
        }
    }
    quad(m_distance, x1, y1, x12, y12, x123, y123, level + 1, data, vertex);
    quad(m_distance, x123, y123, x23, y23, x3, y3, level + 1, data, vertex);
}

void tessQuad(double scale,
              double x1, double y1,
              double x2, double y2,
              double x3, double y3,
              void* data, void (*vertex)(void*, double, double)) {
    //vertex(data, x1, y1);
    scale = 1 / ((scale) * (scale));
    quad(scale, x1, y1, x2, y2, x3, y3, 0, data, vertex);
    vertex(data, x3, y3);
}

void cubic(double m_distance,
           double x1, double y1,
           double x2, double y2,
           double x3, double y3,
           double x4, double y4,
           unsigned level, void* data, void (*vertex)(void*, double x, double y)) {
    if (level > m_level) {
        return;
    }

    double x12 = (x1 + x2) / 2;
    double y12 = (y1 + y2) / 2;
    double x23 = (x2 + x3) / 2;
    double y23 = (y2 + y3) / 2;
    double x34 = (x3 + x4) / 2;
    double y34 = (y3 + y4) / 2;
    double x123 = (x12 + x23) / 2;
    double y123 = (y12 + y23) / 2;
    double x234 = (x23 + x34) / 2;
    double y234 = (y23 + y34) / 2;
    double x1234 = (x123 + x234) / 2;
    double y1234 = (y123 + y234) / 2;

    double dx = x4 - x1;
    double dy = y4 - y1;

    double d2 = fabs(((x2 - x4) * dy - (y2 - y4) * dx));
    double d3 = fabs(((x3 - x4) * dy - (y3 - y4) * dx));
    double da1, da2, k;

    switch ((int(d2 > collinearity_epsilon) << 1) + int(d3 > collinearity_epsilon)) {
        case 0:
            k = dx * dx + dy * dy;
            if (k == 0) {
                d2 = distance2(x1, y1, x2, y2);
                d3 = distance2(x4, y4, x3, y3);
            } else {
                k = 1 / k;
                da1 = x2 - x1;
                da2 = y2 - y1;
                d2 = k * (da1 * dx + da2 * dy);
                da1 = x3 - x1;
                da2 = y3 - y1;
                d3 = k * (da1 * dx + da2 * dy);
                if (d2 > 0 && d2 < 1 && d3 > 0 && d3 < 1) {
                    return;
                }
                if (d2 <= 0) {
                    d2 = distance2(x2, y2, x1, y1);
                } else if (d2 >= 1) {
                    d2 = distance2(x2, y2, x4, y4);
                } else {
                    d2 = distance2(x2, y2, x1 + d2 * dx, y1 + d2 * dy);
                }

                if (d3 <= 0) {
                    d3 = distance2(x3, y3, x1, y1);
                } else if (d3 >= 1) {
                    d3 = distance2(x3, y3, x4, y4);
                } else {
                    d3 = distance2(x3, y3, x1 + d3 * dx, y1 + d3 * dy);
                }
            }
            if (d2 > d3) {
                if (d2 < m_distance) {
                    vertex(data, x2, y2);
                    return;
                }
            } else {
                if (d3 < m_distance) {
                    vertex(data, x3, y3);
                    return;
                }
            }
            break;

        case 1:
            if (d3 * d3 <= m_distance * (dx * dx + dy * dy)) {
                if (m_angle < angle_epsilon) {
                    vertex(data, x23, y23);
                    return;
                }

                da1 = fabs(atan2(y4 - y3, x4 - x3) - atan2(y3 - y2, x3 - x2));
                if (da1 >= pi) da1 = 2 * pi - da1;

                if (da1 < m_angle) {
                    vertex(data, x2, y2);
                    vertex(data, x3, y3);
                    return;
                }

                if (m_cusp != 0.0) {
                    if (da1 > m_cusp) {
                        vertex(data, x3, y3);
                        return;
                    }
                }
            }
            break;
        case 2:
            if (d2 * d2 <= m_distance * (dx * dx + dy * dy)) {
                if (m_angle < angle_epsilon) {
                    vertex(data, x23, y23);
                    return;
                }

                da1 = fabs(atan2(y3 - y2, x3 - x2) - atan2(y2 - y1, x2 - x1));
                if (da1 >= pi) da1 = 2 * pi - da1;

                if (da1 < m_angle) {
                    vertex(data, x2, y2);
                    vertex(data, x3, y3);
                    return;
                }

                if (m_cusp != 0.0) {
                    if (da1 > m_cusp) {
                        vertex(data, x2, y2);
                        return;
                    }
                }
            }
            break;
        case 3:
            if ((d2 + d3) * (d2 + d3) <= m_distance * (dx * dx + dy * dy)) {
                if (m_angle < angle_epsilon) {
                    vertex(data, x23, y23);
                    return;
                }

                k = atan2(y3 - y2, x3 - x2);
                da1 = fabs(k - atan2(y2 - y1, x2 - x1));
                da2 = fabs(atan2(y4 - y3, x4 - x3) - k);
                if (da1 >= pi) da1 = 2 * pi - da1;
                if (da2 >= pi) da2 = 2 * pi - da2;

                if (da1 + da2 < m_angle) {
                    vertex(data, x23, y23);
                    return;
                }

                if (m_cusp != 0.0) {
                    if (da1 > m_cusp) {
                        vertex(data, x2, y2);
                        return;
                    }

                    if (da2 > m_cusp) {
                        vertex(data, x3, y3);
                        return;
                    }
                }
            }
            break;
    }
    cubic(m_distance, x1, y1, x12, y12, x123, y123, x1234, y1234, level + 1, data, vertex);
    cubic(m_distance, x1234, y1234, x234, y234, x34, y34, x4, y4, level + 1, data, vertex);
}

void tessCubic(double scale,
               double x1, double y1,
               double x2, double y2,
               double x3, double y3,
               double x4, double y4,
               void* data, void (*vertex)(void*, double x, double y)) {
    //vertex(data, x1, y1);
    scale = 1 / ((scale) * (scale));
    cubic(scale, x1, y1, x2, y2, x3, y3, x4, y4, 0, data, vertex);
    vertex(data, x4, y4);
}