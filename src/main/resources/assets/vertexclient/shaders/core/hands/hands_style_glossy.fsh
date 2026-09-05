#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform vec4 Tint;
uniform vec4 Params;
uniform float alpha;

in vec2 TexCoord;
out vec4 OutColor;

const float TAU = 6.28318530718;
const int ITERATIONS = 5;

vec3 glossyColor(vec2 uv) {
    vec2 resolution = max(Params.xy, vec2(1.0));
    float time = Params.z * 0.5 + 23.0;
    vec2 position = mod(uv * TAU, TAU) - 250.0;
    vec2 warped = position;
    float field = 1.0;
    float lineIntensity = 0.005;
    for (int index = 0; index < ITERATIONS; index++) {
        float iterationTime = time * (1.0 - 3.5 / float(index + 1));
        warped = position + vec2(
            cos(iterationTime - warped.x) + sin(iterationTime + warped.y),
            sin(iterationTime - warped.y) + cos(iterationTime + warped.x)
        );
        field += 1.0 / length(vec2(
            position.x / (sin(warped.x + iterationTime) / lineIntensity),
            position.y / (cos(warped.y + iterationTime) / lineIntensity)
        ));
    }
    field /= float(ITERATIONS);
    field = 1.17 - pow(field, 1.4);
    vec3 color = clamp(vec3(pow(abs(field), 8.0)) + vec3(0.0, 0.35, 0.5), 0.0, 1.0);
    return clamp(color * Params.w, 0.0, 1.0) * Tint.rgb;
}

void main() {
    vec2 uv = TexCoord;
    float mask = texture(Sampler1, uv).a;
    if (mask < 0.01) discard;
    vec4 src = texture(Sampler0, uv);
    vec3 col = glossyColor(uv);
    vec3 finalRGB = mix(src.rgb, col, alpha);
    OutColor = vec4(clamp(finalRGB, 0.0, 1.0), mask * max(alpha, 0.06));
}