#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform vec2 resolution;
uniform float time;
uniform float alpha;
uniform float speed;
uniform vec3 primaryColor;
uniform vec3 accentColor;

in vec2 TexCoord;
out vec4 OutColor;

float hash21(vec2 p) {
    p = fract(p * vec2(443.897, 397.297));
    p += dot(p, p + 23.317);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p), f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash21(i), b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0)), d = hash21(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0, a = 0.5;
    for (int i = 0; i < 4; i++) {
        v += noise(p) * a;
        p = mat2(1.62, -1.18, 1.18, 1.62) * p + vec2(5.2, 2.7);
        a *= 0.5;
    }
    return v;
}

vec3 sat(vec3 c, float k) {
    float g = dot(c, vec3(0.299, 0.587, 0.114));
    return clamp(mix(vec3(g), c, k), 0.0, 1.0);
}

vec3 cosmosColor(vec2 p, float t) {
    vec2 q = p * 4.6;
    float n = fbm(q + vec2(t * 0.12, -t * 0.08));
    float r = fbm(q * 1.8 + vec2(-t * 0.07, t * 0.11) + 8.0);
    float d = smoothstep(0.18, 0.95, mix(n, r, 0.45));
    vec3 base = primaryColor / max(max(primaryColor.r, max(primaryColor.g, primaryColor.b)), 0.001);
    vec3 col = mix(base * 0.05, base * 0.9, d);
    col = mix(col, vec3(1.0), smoothstep(0.82, 1.0, d) * 0.55);
    float stars = smoothstep(0.988, 0.999, hash21(floor(p * vec2(95.0, 70.0))))
            * smoothstep(0.34, 0.0, length(fract(p * vec2(95.0, 70.0)) - 0.5));
    return sat(col + stars, 1.35);
}

void main() {
    vec2 uv = TexCoord;
    float mask = texture(Sampler1, uv).a;
    if (mask < 0.01) discard;
    vec4 src = texture(Sampler0, uv);
    vec2 p = gl_FragCoord.xy / max(resolution, vec2(1.0));
    p.x *= resolution.x / max(resolution.y, 1.0);
    float t = time * speed;
    vec3 col = cosmosColor(p, t);
    float rim = mask * (1.0 - smoothstep(0.01, 0.62, texture(Sampler1, uv + vec2(1.0 / resolution.x, 0.0)).a
            * texture(Sampler1, uv + vec2(-1.0 / resolution.x, 0.0)).a
            * texture(Sampler1, uv + vec2(0.0, 1.0 / resolution.y)).a
            * texture(Sampler1, uv + vec2(0.0, -1.0 / resolution.y)).a));
    col += accentColor * rim * 0.4;
    vec3 finalRGB = mix(src.rgb, col, alpha);
    OutColor = vec4(clamp(finalRGB, 0.0, 1.0), mask * max(alpha, 0.08));
}