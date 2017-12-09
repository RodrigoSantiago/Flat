#version 330 core
out vec4 FragColor;

in vec2 uv;

uniform vec4 view;
uniform int effectId = 0;
uniform int[20] intData;
uniform float[20] floatData;
uniform sampler2D srcImg;

vec4 RoundRectShadow(in float blur, in float alpha, in vec2 pos, in vec2 size, in vec4 corners, in mat3x2 trn) {
    vec2 coord = vec2(gl_FragCoord.x, view[3] - gl_FragCoord.y);
    vec2 center = (pos + size) / 2.0f;
    float dist = distance(vec2(coord.x, coord.y), center) / length(size);
    return vec4(dist,dist,dist,1.0f);
}

void main() {
    switch (effectId) {
        case 0: FragColor = texelFetch(srcImg, ivec2(uv), 0); break;
        case 1: FragColor = RoundRectShadow(floatData[0], floatData[1],
            vec2(floatData[2], floatData[3]),
            vec2(floatData[4], floatData[5]),
            vec4(floatData[6], floatData[7], floatData[8], floatData[9]),
            mat3x2(floatData[10], floatData[11], floatData[12], floatData[13], floatData[14], floatData[15])); break;
    }
}