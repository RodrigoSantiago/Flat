#version 330 core
out vec4 FragColor;

in vec2 uv;

uniform int effectType = 0;
uniform sampler2D srcImg;

uniform int data1 = 1;

vec4 hblur() {
    int size = data1;
    int hsize = data1 / 2;
    float col = 0.0;
    for (int i = 0; i < size; i++) {
        col += texelFetch(srcImg, ivec2(uv.x + i - hsize, uv.y), 0).a;
    }
    return vec4(0,0,0, col / size);
}
vec4 vblur() {
    int size = data1;
    int hsize = data1 / 2;
    float col = 0.0;
    for (int i = 0; i < size; i++) {
        col += texelFetch(srcImg, ivec2(uv.x, uv.y + i - hsize), 0).a;
    }
    return vec4(0,0,0, col / size);
}

void main() {
    switch (effectType) {
        case 0: FragColor = texelFetch(srcImg, ivec2(uv), 0); break;
        case 1: FragColor = hblur(); break;
        case 2: FragColor = vblur(); break;
    }
}