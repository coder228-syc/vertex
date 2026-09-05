#version 150

#moj_import <vertexclient:common.glsl>

in vec2 FragCoord;
in vec4 FragColor;

uniform vec2 Size;
uniform vec4 Radius;
uniform float Smoothness;
uniform float Strength;

out vec4 OutColor;

const int SAMPLES = 12;

void main() {
    vec2 center = Size * 0.5;
    vec2 uv = FragCoord;
    vec2 dir = uv - vec2(0.5);

    vec3 color = vec3(0.0);
    float total = 0.0;

    for (int i = 0; i < SAMPLES; i++) {
        float t = float(i) / float(SAMPLES - 1);
        float scale = 1.0 + t * Strength * 0.1;
        vec2 offset = dir * (scale - 1.0);
        color += FragColor.rgb;
        total += 1.0;
    }

    color /= total;

    float dist = roundedBoxSDF(center - (uv * Size), center - 1.0, Radius);
    float alpha = 1.0 - smoothstep(1.0 - Smoothness, 1.0, dist);
    alpha *= FragColor.a;

    if (alpha <= 0.0) discard;

    OutColor = vec4(color, alpha);
}