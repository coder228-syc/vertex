#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;
uniform vec3 glowColor1;
uniform vec3 glowColor2;
uniform float exposure;
uniform float saturation;
uniform int outerOnly;
uniform int colorMode;

in vec2 TexCoord;
out vec4 OutColor;

void main() {
    vec2 uv = TexCoord;
    vec4 b = texture(Sampler0, uv);
    float mask = texture(Sampler1, uv).a;
    float outerMask = 1.0 - clamp(mask, 0.0, 1.0);
    float edge = outerOnly == 1 ? smoothstep(0.10, 0.92, outerMask) : 1.0;
    float i = b.a * edge * exposure;
    if (i <= 0.001) discard;
    vec3 themed;
    if (colorMode == 0) {
        themed = mix(glowColor1, glowColor2, clamp(uv.y, 0.0, 1.0));
    } else if (colorMode == 2) {
        vec3 itemCol = texture(Sampler2, uv).rgb;
        float lum = dot(itemCol, vec3(0.299, 0.587, 0.114));
        itemCol = mix(vec3(lum), itemCol, 1.35);
        themed = mix(glowColor1, itemCol, 0.72);
    } else {
        themed = mix(glowColor1, glowColor2, clamp(uv.y, 0.0, 1.0));
    }
    vec3 c = b.rgb / max(b.a, 0.001);
    float m = max(c.r, max(c.g, c.b));
    if (m > 0.001) c /= m;
    float l = dot(c, vec3(0.299, 0.587, 0.114));
    vec3 autoCol = clamp(mix(vec3(l), c, saturation), 0.0, 1.0);
    float themedWeight = colorMode == 0 ? 0.88 : (colorMode == 2 ? 0.82 : 0.72);
    vec3 col = clamp(autoCol * (1.0 - themedWeight) + themed * themedWeight, 0.0, 1.0);
    OutColor = vec4(col, i);
}