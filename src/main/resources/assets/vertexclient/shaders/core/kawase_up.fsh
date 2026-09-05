#version 150

uniform sampler2D Sampler0;
uniform float offset;
uniform vec2 resolution;

in vec2 TexCoord;

out vec4 OutColor;

void main() {
    vec2 uv = TexCoord / 2.0;
    vec2 halfpixel = resolution / 2.0 * offset;

    vec3 sum = vec3(0.0);
    sum += texture(Sampler0, uv + vec2(-halfpixel.x * 2.0, 0.0)).rgb;
    sum += texture(Sampler0, uv + vec2(-halfpixel.x, halfpixel.y)).rgb * 2.0;
    sum += texture(Sampler0, uv + vec2(0.0, halfpixel.y * 2.0)).rgb;
    sum += texture(Sampler0, uv + vec2(halfpixel.x, halfpixel.y)).rgb * 2.0;
    sum += texture(Sampler0, uv + vec2(halfpixel.x * 2.0, 0.0)).rgb;
    sum += texture(Sampler0, uv + vec2(halfpixel.x, -halfpixel.y)).rgb * 2.0;
    sum += texture(Sampler0, uv + vec2(0.0, -halfpixel.y * 2.0)).rgb;
    sum += texture(Sampler0, uv + vec2(-halfpixel.x, -halfpixel.y)).rgb * 2.0;

    OutColor = vec4(sum / 12.0, 1.0);
}
