#version 330 core

uniform vec4 box;
uniform vec4 inbox;
uniform vec4 color;
uniform float sigma;
uniform float[4] corners;

varying vec2 vertex;

float gaussian(float x, float sigma) {
    const float pi = 3.141592653589793;
    return exp(-(x * x) / (2.0 * sigma * sigma)) / (sqrt(2.0 * pi) * sigma);
}

vec2 erf(vec2 x) {
    vec2 s = sign(x), a = abs(x);
    x = 1.0 + (0.278393 + (0.230389 + 0.078108 * (a * a)) * a) * a;
    x *= x;
    return s - s / (x * x);
}

float roundedBoxShadowX(float x, float y, float sigma, float corner, vec2 halfSize) {
    float delta = min(halfSize.y - corner - abs(y), 0.0);
    float curved = halfSize.x - corner + sqrt(max(0.0, corner * corner - delta * delta));
    vec2 integral = 0.5 + 0.5 * erf((x + vec2(-curved, curved)) * (sqrt(0.5) / sigma));
    return integral.y - integral.x;
}

float roundedBoxShadow(vec2 lower, vec2 upper, vec2 point, float sigma, float corner) {
    vec2 center = (lower + upper) * 0.5;
    vec2 halfSize = (upper - lower) * 0.5;
    point -= center;

    float low = point.y - halfSize.y;
    float high = point.y + halfSize.y;
    float start = clamp(-3.0 * sigma, low, high);
    float end = clamp(3.0 * sigma, low, high);

    float step = (end - start) / 4.0;
    float y = start + step * 0.5;
    float value = 0.0;
    for (int i = 0; i < 4; i++) {
        value += roundedBoxShadowX(point.x, point.y - y, sigma, corner, halfSize) * gaussian(y, sigma) * step;
        y += step;
    }

    return value;
}

void main() {
    gl_FragColor = color;
    int index = 0;
    if ((vertex.x - box.x) < (box.z - box.x) / 2f) {
        index = ((vertex.y - box.y) < (box.w - box.y) / 2f) ? 0 : 3;
    } else {
        index = ((vertex.y - box.y) < (box.w - box.y) / 2f) ? 1 : 2;
    }
    if (inbox.x < 0 || (vertex.x < inbox.x || vertex.y < inbox.y || vertex.x >= inbox.z || vertex.y >= inbox.w)) {
        gl_FragColor.a *= roundedBoxShadow(box.xy, box.zw, vertex.xy, sigma, corners[index]);
    }
}