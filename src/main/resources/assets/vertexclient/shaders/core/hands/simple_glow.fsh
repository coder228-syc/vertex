#version 150

uniform sampler2D Sampler0; // Blurred mask
uniform sampler2D Sampler1; // Original frame
uniform vec3 glowColor;
uniform float intensity;

in vec2 TexCoord;
out vec4 OutColor;

void main() {
    vec2 uv = TexCoord;
    
    // Get blurred mask and original
    vec4 blurredMask = texture(Sampler0, uv);
    vec4 original = texture(Sampler1, uv);
    
    // Apply glow to areas where mask exists
    float maskStrength = clamp(blurredMask.a * intensity, 0.0, 1.0);
    vec3 glow = glowColor * (0.6 + maskStrength * 0.4);
    
    // Fully cover the background where the hand is, soft edges from mask
    vec3 finalColor = mix(original.rgb, glow, maskStrength);
    
    OutColor = vec4(finalColor, original.a);
}
