#version 150

#moj_import <skycore:common.glsl>

in vec2 FragCoord;
in vec2 TexCoord;
in vec4 FragColor;

uniform sampler2D Sampler0;
uniform vec2 Size;
uniform vec4 Radius;
uniform float Smoothness;
uniform vec4 ColorModulator;

out vec4 OutColor;

void main() {
    vec2 center = Size * 0.5;
    float dist = roundedBoxSDF(center - (FragCoord * Size), center - 1.0, Radius);
    float alpha = 1.0 - smoothstep(1.0 - Smoothness, 1.0, dist);

    vec4 color = texture(Sampler0, TexCoord) * FragColor;
    color.a *= alpha;

    if (color.a <= 0.0) discard;

    OutColor = color * ColorModulator;
}