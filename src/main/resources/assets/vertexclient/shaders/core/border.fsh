#version 150
#moj_import <vertexclient:common.glsl>

in vec2 FragCoord;
in vec4 FragColor;

uniform vec2 Size;
uniform vec4 Radius;
uniform float Thickness;
uniform float Smoothness;

out vec4 OutColor;

void main() {
    float dist = rsdf(Size, FragCoord, Radius);
    float outer = 1.0 - smoothstep(0.0, max(Smoothness, 0.001), dist);
    float inner = 1.0 - smoothstep(0.0, max(Smoothness, 0.001), dist + abs(Thickness));
    float alpha = clamp(outer - inner, 0.0, 1.0) * FragColor.a;
    if (alpha < 0.001) discard;
    OutColor = vec4(FragColor.rgb, alpha);
}
