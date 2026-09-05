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

// Full-res composite: bilinear-upsampled smooth layer plus exact stars.
void main() {
    if (!isSkyDepth(texture(DepthSampler, TexCoord).r)) {
        discard;
    }
    vec3 direction = skyDirection(TexCoord);
    float time = SkyParams.x * SkyParams.z;
    vec4 cloud = texture(CloudSampler, TexCoord);
    float stars = starGlow(direction, 300.0, 0.06, time)
            + starGlow(direction * 1.8 + 5.0, 160.0, 0.03, time * 1.3) * 1.3;
    vec3 color = cloud.rgb + vec3(0.90, 0.93, 1.0) * stars * (0.7 + 0.9 * cloud.a);
    color *= SkyParams.y;
    color = color / (1.0 + color * 0.8);
    color = pow(color, vec3(0.88)) + ditherRgb(TexCoord);
    OutColor = vec4(clamp(color, 0.0, 1.0), 1.0);
}