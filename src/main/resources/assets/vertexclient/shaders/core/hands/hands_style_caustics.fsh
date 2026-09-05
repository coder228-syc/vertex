#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform vec4 Tint;
uniform vec4 Params;
uniform float alpha;

in vec2 TexCoord;
out vec4 OutColor;

vec3 causticsColor(vec2 uv) {
    vec2 resolution = max(Params.xy, vec2(1.0));
    float minimum = min(resolution.x, resolution.y);
    vec2 coords = (uv * resolution * 2.0 - resolution) / minimum;
    float time = Params.z;
    float depth = -time * 0.5;
    float accumulator = 0.0;
    for (float index = 0.0; index < 8.0; index++) {
        accumulator += cos(index - depth - accumulator * coords.x);
        depth += sin(coords.y * index + accumulator);
    }
    depth += time * 0.5;
    vec3 color = vec3(
        cos(coords * vec2(depth, accumulator)) * 0.6 + 0.4,
        cos(accumulator + depth) * 0.5 + 0.5
    );
    color = cos(color * cos(vec3(depth, accumulator, 2.5)) * 0.5 + 0.5);
    return clamp(color * Params.w, 0.0, 1.0) * Tint.rgb;
}

void main() {
    vec2 uv = TexCoord;
    float mask = texture(Sampler1, uv).a;
    if (mask < 0.01) discard;
    vec4 src = texture(Sampler0, uv);
    vec3 col = causticsColor(uv);
    vec3 finalRGB = mix(src.rgb, col, alpha);
    OutColor = vec4(clamp(finalRGB, 0.0, 1.0), mask * max(alpha, 0.06));
}