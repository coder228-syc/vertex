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

// Full-res composite: bilinear-upsampled plasma plus exact stars.
void main() {
    if (!isSkyDepth(texture(DepthSampler, TexCoord).r)) {
        discard;
    }
    vec3 direction = skyDirection(TexCoord);
    float time = SkyParams.x * SkyParams.z;
    vec3 color = texture(CloudSampler, TexCoord).rgb;
    color += vec3(0.8, 0.9, 1.0)
            * starGlow(direction, 180.0, 0.025, time * 0.2)
            * 0.35;
    color *= SkyParams.y;
    color = color / (1.0 + color * 0.75);
    color = pow(color, vec3(0.88)) + ditherRgb(TexCoord);
    OutColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}