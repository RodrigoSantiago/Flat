#version 330 core

layout (location = 0) in vec2 coord;
layout (location = 1) in vec2 iUV;

uniform vec4 box;
uniform vec2 window;
uniform float sigma;
uniform float[4] corner;
uniform mat3 affine;

varying vec2 vertex;
varying vec4 inBox;

void main() {
    float padding = 3.0 * sigma;
    vertex = mix(box.xy - padding, box.zw + padding, coord);
    vec2 arc = min(box.zw - box.xy, vec2(max(max(max(corner[0], corner[1]), corner[2]), corner[3])));
    vec2 ds = -0.7071f * arc + arc + padding;
    inBox = vec4(box.xy + ds, box.zw - ds);
    if (inBox.x > inBox.z || inBox.y > inBox.w) {
        inBox = vec4(-1, -1, -1, -1);
    }

    vec2 point = vec2(affine[0][0] * vertex.x + affine[0][1] * vertex.y + affine[0][2],
                      affine[1][0] * vertex.x + affine[1][1] * vertex.y + affine[1][2]);
    gl_Position = vec4(point.x / window.x * 2.0 - 1.0, -(point.y / window.y * 2.0 - 1.0), 0.0, 1.0);
}