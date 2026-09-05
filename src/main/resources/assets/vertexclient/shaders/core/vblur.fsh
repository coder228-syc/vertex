#version 150

#moj_import <vertexclient:common.glsl>

in vec2 FragCoord;
in vec2 TexCoord;
in vec4 FragColor;

uniform sampler2D Sampler0;
uniform vec2 Size;
uniform vec4 Radius;
uniform float Smoothness;
uniform float BlurRadius;

out vec4 OutColor;

const int SAMPLES = 12;

void main() {
    vec2 texSize = vec2(textureSize(Sampler0, 0));
    vec2 offset = vec2(0.0, BlurRadius / texSize.y);

    vec3 color = texture(Sampler0, TexCoord).rgb;
    float total = 1.0;

    for (int i = 1; i <= SAMPLES; i++) {
        float weight = 1.0 - float(i) / float(SAMPLES);
        color += texture(Sampler0, TexCoord + offset * float(i)).rgb * weight;
        color += texture(Sampler0, TexCoord - offset * float(i)).rgb * weight;
        total += weight * 2.0;
    }

    color /= total;

    float dist = roundedBoxSDF(Size * 0.5 - (FragCoord * Size), Size * 0.5 - 1.0, Radius);
    float alpha = 1.0 - smoothstep(1.0 - Smoothness, 1.0, dist);
    alpha *= FragColor.a;

    if (alpha <= 0.0) discard;

    OutColor = vec4(color, alpha);
}