// Shared by the half-res sky cloud passes and the full-res composites.

vec3 skyDirection(vec2 coords) {
    vec2 ndc = coords * 2.0 - 1.0;
    // Both extreme clip-z values always lie inside the visible range, so the
    // reconstructed ray is correct regardless of the platform depth convention.
    vec4 nearPoint = InvViewProjection * vec4(ndc, 1.0, 1.0);
    vec4 farPoint = InvViewProjection * vec4(ndc, -1.0, 1.0);
    return normalize(
        farPoint.xyz / farPoint.w - nearPoint.xyz / nearPoint.w
    );
}

// True when a depth value belongs to the sky far plane.
// SkyParams.w selects the depth convention used by the current framebuffer:
//   0 -> standard depth range, far plane = 1.0  (classic fixed-function GL)
//   1 -> reversed depth, far plane = 0.0        (modern pipeline)
bool isSkyDepth(float depth) {
    if (SkyParams.w > 0.5) {
        return depth <= 0.000001;
    }
    return depth >= 0.999999;
}

// True when any depth texel within one half-res texel of `coords` is sky.
// The dilation guarantees the composite's bilinear upsample never blends
// in a cloud texel that skipped shading.
bool skyNearby(sampler2D depthSampler, vec2 coords) {
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            float depth = texture(depthSampler, coords + vec2(float(x), float(y)) * SkyTexel.xy * 2.0).r;
            if (isSkyDepth(depth)) {
                return true;
            }
        }
    }
    return false;
}