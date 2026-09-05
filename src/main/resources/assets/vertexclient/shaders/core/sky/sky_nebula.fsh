#version 150

in vec2 TexCoord;
out vec4 OutColor;

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
#define DepthSampler Sampler0
#define CloudSampler Sampler1

uniform mat4 InvViewProjection;
uniform vec4 PrimaryColor;
uniform vec4 SecondaryColor;
uniform vec4 SkyParams;
uniform vec4 SkyTexel;

#moj_import <vertexclient:block_outline_common.glsl>
#moj_import <vertexclient:world_sky_common.glsl>

// Full-res composite: bilinear-upsampled nebula plus exact stars.
void main() {
    if (!isSkyDepth(texture(DepthSampler, TexCoord).r)) {
        discard;
    }
    vec3 direction = skyDirection(TexCoord);
    float time = SkyParams.x * SkyParams.z;
    vec3 color = texture(CloudSampler, TexCoord).rgb;
    color += vec3(0.85, 0.90, 1.0)
            * starGlow(direction, 240.0, 0.05, time);
    color += vec3(1.0, 0.96, 0.90)
            * starGlow(direction * 1.7 + 31.0, 130.0, 0.025, time * 1.3)
            * 1.4;
    color *= SkyParams.y;
    color = color / (1.0 + color);
    color = pow(color, vec3(0.85)) + ditherRgb(TexCoord);
    OutColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}