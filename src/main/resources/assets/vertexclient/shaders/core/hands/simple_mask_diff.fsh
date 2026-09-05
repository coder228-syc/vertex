#version 150

uniform sampler2D Sampler0; // Before frame
uniform sampler2D Sampler1; // After frame

in vec2 TexCoord;
out vec4 OutColor;

void main() {
    vec2 uv = TexCoord;
    
    // Get colors from both frames
    vec3 beforeColor = texture(Sampler0, uv).rgb;
    vec3 afterColor = texture(Sampler1, uv).rgb;
    
    // Calculate color difference
    float colorDiff = length(afterColor - beforeColor);
    
    // Create mask based on color difference
    float mask = smoothstep(0.02, 0.15, colorDiff);
    
    OutColor = vec4(mask, mask, mask, mask);
}
