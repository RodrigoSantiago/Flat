#version 330 core

layout (location = 0) in vec2 iPos;
layout (location = 1) in vec2 iUV;

uniform mat4 view;

uniform vec4 box;
uniform float sigma;

varying vec2 vertex;

void main() {
    float padding = 3.0 * sigma;
    vertex = mix(box.xy - padding, box.zw + padding, iUV);
    gl_Position = view * vec4(iPos + (iUV - 0.5) * 2 * padding, 0, 1);
}