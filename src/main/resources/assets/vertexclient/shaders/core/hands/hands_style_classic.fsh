#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform vec4 Tint;
uniform vec4 Params;
uniform float alpha;

in vec2 TexCoord;
out vec4 OutColor;

vec3 classicColor(vec2 uv) {
    vec2 resolution = max(Params.xy, vec2(1.0));
    vec2 position = 0.2 * ((uv + uv) - resolution) / resolution.y;
    vec2 warped = vec2(0.0);
    vec2 original = position;
    vec4 color = vec4(1.0, 2.0, 3.0, 0.0);
    float amplitude = 0.5;
    float time = Params.z;

    for (float index = 1.0; index < 19.0; index += 1.0) {
        amplitude += 0.03;
        time += 1.0;
        vec2 value = cos(time - 7.0 * position * pow(amplitude, index)) - 5.0 * position;
        vec4 rotation = cos(index + time * 0.02 - vec4(0.0, 11.0, 33.0, 0.0));
        position = position * mat2(rotation.x, rotation.y, rotation.z, rotation.w);
        position += 0.005 * tanh(
            40.0 * dot(position, position) * cos(100.0 * position.yx + time)
        ) + 0.2 * amplitude * position
          + 0.003 * cos(time + 4.0 * exp(-0.01 * dot(color, color)));
        warped = position / (1.0 - 2.0 * dot(position, position));
        color += (1.0 + cos(vec4(0.0, 1.0, 3.0, 0.0) + time))
                / length((1.0 + index * dot(value, value))
                * sin(warped * 3.0 - 9.0 * position.yx + time));
    }
    color = 1.0 - sqrt(exp(-color * color * color / 200.0));
    color = pow(color, vec4(0.3));
    color -= dot(original - position, original - position) / 250.0;
    return clamp(color.rgb * Params.w, 0.0, 1.0) * Tint.rgb;
}

void main() {
    vec2 uv = TexCoord;
    float mask = texture(Sampler1, uv).a;
    if (mask < 0.01) discard;
    vec4 src = texture(Sampler0, uv);
    vec3 col = classicColor(uv);
    vec3 finalRGB = mix(src.rgb, col, alpha);
    OutColor = vec4(clamp(finalRGB, 0.0, 1.0), mask * max(alpha, 0.06));
}