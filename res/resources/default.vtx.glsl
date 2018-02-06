#version 330 core

layout (location = 0) in vec2 iPos;
layout (location = 1) in vec2 iUV;

uniform vec2 view;
uniform mat4 prj2D;

out vec2 uv;

void main() {
    uv = iUV;
    gl_Position = prj2D * vec4(iPos, 0, 1);
}