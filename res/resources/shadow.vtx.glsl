#version 330 core

layout (location = 0) in vec2 iPos;
layout (location = 1) in vec2 iUV;

uniform mat3 transform;

uniform vec2 view;
uniform vec4 box;
uniform float sigma;

varying vec2 vertex;

void main() {
    float padding = 3.0 * sigma;
    vertex = mix(box.xy - padding, box.zw + padding, iUV);
    vec2 normal = (iUV - 0.5) * 2;
    vec2 pos = vec3(((iPos + 1f) / 2f * view) + (padding * normal), 0).xy;
    pos = (vec3(pos, 0) * transform).xy;
    gl_Position = vec4(pos / view * 2f - 1f, 1, 1);
}