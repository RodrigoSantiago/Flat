#version 330 core

layout (location = 0) in vec2 iPos;
layout (location = 1) in vec2 iUV;

uniform mat4 view;
uniform mat4 local;

out vec2 uv;

void main() {
    uv = iUV;
    gl_Position = view * vec4(iPos, 0, 1);
}