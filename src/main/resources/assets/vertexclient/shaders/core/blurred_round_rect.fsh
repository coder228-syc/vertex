#version 150

#moj_import <vertexclient:common.glsl>

uniform sampler2D Sampler0;
uniform vec2 Size;
uniform vec2 Resolution;
uniform vec4 Radius;
uniform vec4 Color;
uniform float Alpha;
uniform float Smoothness;

in vec2 FragCoord;

out vec4 OutColor;

void main() {
    vec2 blurredPos = gl_FragCoord.xy / Resolution;
    vec3 blurredColor = mix(texture(Sampler0, blurredPos).rgb, Color.rgb, Color.a);
    float mask = ralpha(Size, FragCoord, Radius, Smoothness);
    float a = mask * Alpha;
    if (a < 0.001) discard;
    OutColor = vec4(blurredColor, a);
}
