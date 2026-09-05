#version 150

in vec2 TexCoord;
out vec4 OutColor;

uniform mat4 InvViewProjection;
uniform vec4 PrimaryColor;
uniform vec4 SecondaryColor;
uniform vec4 SkyParams;
uniform vec4 SkyTexel;
uniform sampler2D Sampler0;
#define DepthSampler Sampler0

#moj_import <vertexclient:block_outline_common.glsl>
#moj_import <vertexclient:world_sky_common.glsl>

// Half-res pass: the smooth (band-limited) part of the deep space sky.
// Stars and tonemapping happen in the full-res composite.
void main() {
    if (!skyNearby(DepthSampler, TexCoord)) {
        OutColor = vec4(0.0);
        return;
    }
    vec3 direction = skyDirection(TexCoord);
    float time = SkyParams.x * SkyParams.z;
    vec3 bandNormal = normalize(vec3(0.30, 0.62, 0.72));
    float band = exp(-pow(dot(direction, bandNormal) * 2.2, 2.0));
    float haze = fbm(direction * 3.0 + vec3(time * 0.01, 0.0, 0.0));
    vec3 cloud = vec3(0.006, 0.008, 0.016)
            + mix(PrimaryColor.rgb, SecondaryColor.rgb, 0.5) * band * (0.10 + 0.28 * haze);
    OutColor = vec4(cloud, band);
}