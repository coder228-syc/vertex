#version 150

uniform sampler2D Sampler0;
uniform float offset;
uniform vec2 resolution;

in vec2 TexCoord;

out vec4 OutColor;

void main() {
    vec2 uv = TexCoord * 2.0;
    vec2 halfpixel = resolution * 2.0 * offset;
    vec3 sum = texture(Sampler0, uv).rgb * 4.0;
    sum += texture(Sampler0, uv - halfpixel).rgb;
    sum += texture(Sampler0, uv + halfpixel).rgb;
    sum += texture(Sampler0, uv + vec2(halfpixel.x, -halfpixel.y)).rgb;
    sum += texture(Sampler0, uv - vec2(halfpixel.x, -halfpixel.y)).rgb;
    OutColor = vec4(sum / 8.0, 1.0);
}
