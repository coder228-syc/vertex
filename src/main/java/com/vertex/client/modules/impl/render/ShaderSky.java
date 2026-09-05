package com.vertex.client.modules.impl.render;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.vertex.client.modules.Module;
import com.vertex.client.modules.ModuleCategory;
import com.vertex.client.modules.ModuleInfo;
import com.vertex.client.modules.ModuleManager;
import com.vertex.client.modules.setting.BooleanSetting;
import com.vertex.client.modules.setting.ModeSetting;
import com.vertex.client.modules.setting.SliderSetting;
import com.vertex.client.render.util.RenderUtil;
import com.vertex.client.util.IMinecraft;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.function.Supplier;

@ModuleInfo(name = "ShaderSky", desc = "Sky shaders and atmospheric fog", type = ModuleCategory.Render, key = GLFW.GLFW_KEY_UNKNOWN)
public class ShaderSky extends Module {
    public static ShaderSky INSTANCE;

    public static final ShaderProgramKey CLOUDS_DEEP_SPACE_KEY = key("clouds_deep_space");
    public static final ShaderProgramKey CLOUDS_NEBULA_KEY = key("clouds_nebula");
    public static final ShaderProgramKey CLOUDS_PLASMA_KEY = key("clouds_plasma");
    public static final ShaderProgramKey SKY_DEEP_SPACE_KEY = key("sky_deep_space");
    public static final ShaderProgramKey SKY_NEBULA_KEY = key("sky_nebula");
    public static final ShaderProgramKey SKY_PLASMA_KEY = key("sky_plasma");
    public static final ShaderProgramKey DAWN_FOG_KEY = key("dawn_fog");

    public final BooleanSetting sky = new BooleanSetting("Sky", true);
    public final ModeSetting skyStyle = new ModeSetting(() -> sky.get(), "Sky Style", "Deep Space", "Deep Space", "Nebula", "Plasma");
    public final SliderSetting skySpeed = new SliderSetting("Sky Speed", 4.0, 0.1, 8.0, 0.1, () -> sky.get());
    public final SliderSetting skyIntensity = new SliderSetting("Sky Intensity", 3.0, 0.1, 5.0, 0.1, () -> sky.get());

    public final BooleanSetting atmo = new BooleanSetting("Atmosphere", true);
    public final ModeSetting atmoMode = new ModeSetting(() -> atmo.get(), "Atmo Mode", "Dusk", "Dawn", "Dusk", "Theme", "Night");
    public final SliderSetting atmoDensity = new SliderSetting("Atmo Density", 0.35, 0.05, 0.8, 0.05, () -> atmo.get());
    public final SliderSetting scatterHeight = new SliderSetting("Scatter Height", 76, 60, 120, 1, () -> atmo.get());
    public final SliderSetting godRays = new SliderSetting("God Rays", 0.75, 0, 1.0, 0.05, () -> atmo.get());
    public final SliderSetting softness = new SliderSetting("Softness", 0.6, 0, 1.0, 0.05, () -> atmo.get());
    public final SliderSetting sunGlow = new SliderSetting("Sun Glow", 0.85, 0, 1.5, 0.05, () -> atmo.get());
    public final BooleanSetting rainbow = new BooleanSetting("Rainbow", true, () -> atmo.get() && !atmoMode.is("Night"));
    public final SliderSetting rainbowBrightness = new SliderSetting("Rainbow Brightness", 1.0, 0, 1.0, 0.05, () -> atmo.get() && rainbow.get());
    public final SliderSetting rainbowSize = new SliderSetting("Rainbow Size", 42, 40, 64, 1, () -> atmo.get() && rainbow.get());
    public final SliderSetting stars = new SliderSetting("Night Stars", 0.8, 0, 1.0, 0.05, () -> atmo.get() && atmoMode.is("Night"));
    public final SliderSetting aurora = new SliderSetting("Night Aurora", 0.35, 0, 1.0, 0.05, () -> atmo.get() && atmoMode.is("Night"));
    public final ModeSetting debugView = new ModeSetting(() -> atmo.get(), "Debug View", "Off", "Off", "Scene", "Depth", "SkyMask", "Ray");

    private final Supplier<SimpleFramebuffer> cloudsFB = Suppliers.memoize(() -> new SimpleFramebuffer(1, 1, false));
    private final Supplier<SimpleFramebuffer> sceneFB = Suppliers.memoize(() -> new SimpleFramebuffer(1, 1, false));
    private final Supplier<SimpleFramebuffer> depthFB = Suppliers.memoize(() -> new SimpleFramebuffer(1, 1, true));

    private final Matrix4f invViewProjection = new Matrix4f();
    private final Matrix4f inverseProjection = new Matrix4f();
    private final Matrix4f inverseView = new Matrix4f();
    private final Vector4f sunVector = new Vector4f();
    private final float[] palette = new float[18];

    private int width = 1, height = 1;
    private int configuredDepthTex = -1;

    public ShaderSky() {
        INSTANCE = this;
        addSettings(sky, skyStyle, skySpeed, skyIntensity,
                atmo, atmoMode, atmoDensity, scatterHeight, godRays, softness, sunGlow,
                rainbow, rainbowBrightness, rainbowSize, stars, aurora, debugView);
    }

    @Override
    public void onEvent(Object event) {}

    @Override
    protected void onDisable() {
        configuredDepthTex = -1;
    }

    private static ShaderProgramKey key(String path) {
        return new ShaderProgramKey(Identifier.of("vertexclient", "core/sky/" + path), VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY);
    }

    public static boolean isActive() {
        return INSTANCE != null && INSTANCE.isState();
    }

    public void render(Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Framebuffer main = mc.getFramebuffer();
        if (mc.world == null || main == null || projectionMatrix == null) return;
        try {
            this.width = main.textureWidth;
            this.height = main.textureHeight;
            if (width <= 0 || height <= 0) return;

            boolean skyOn = sky.get();
            boolean atmoOn = atmo.get();
            if (!skyOn && !atmoOn) return;

            SimpleFramebuffer depth = resizeTo(depthFB.get(), main);
            depth.beginWrite(false);
            depth.endWrite();
            depth.copyDepthFrom(main);
            int depthTex = depth.getDepthAttachment();
            if (depthTex != 0 && depthTex != configuredDepthTex) {
                configureDepthTexture(depthTex);
                configuredDepthTex = depthTex;
            }

            this.invViewProjection.set(new Matrix4f(projectionMatrix)).mul(positionMatrix).invert();

            if (skyOn) {
                renderSky(main, depthTex);
            }
            if (atmoOn) {
                renderAtmosphere(main, camera, positionMatrix, projectionMatrix, depthTex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                resetTextures();
                RenderSystem.defaultBlendFunc();
                MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
            } catch (Exception ignored) {}
        }
    }

    private void renderSky(Framebuffer main, int depthTex) {
        SimpleFramebuffer clouds = resizeClouds(cloudsFB.get());
        setLinearFiltering(clouds);

        RenderUtil.enableRender();
        ShaderProgram cloudsProgram = setProgram(cloudPipeline());
        if (cloudsProgram == null) { RenderUtil.disableRender(); return; }
        try {
            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();
            clouds.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            clouds.clear();
            clouds.beginWrite(true);
            bindTextures(depthTex);
            writeSkyUniforms(cloudsProgram, depthTex);
            drawQuad();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            resetTextures();
            RenderSystem.enableDepthTest();
            RenderUtil.disableRender();
        }

        RenderUtil.enableRender();
        ShaderProgram skyProgram = setProgram(skyPipeline());
        if (skyProgram == null) { RenderUtil.disableRender(); return; }
        try {
            main.beginWrite(true);
            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();
            bindTextures(depthTex, clouds.getColorAttachment());
            writeSkyUniforms(skyProgram, depthTex);
            drawQuad();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            resetTextures();
            RenderSystem.enableDepthTest();
            RenderUtil.disableRender();
        }
    }

    private void writeSkyUniforms(ShaderProgram program, int depthTex) {
        setMat4(program, "InvViewProjection", invViewProjection);
        float[] accent = accentRgb();
        setUniform4(program, "PrimaryColor", accent[0], accent[1], accent[2], 1.0F);
        setUniform4(program, "SecondaryColor", accent[0], accent[1], accent[2], 1.0F);
        setUniform4(program, "SkyParams", shaderTime(), skyIntensity.get().floatValue(), skySpeed.get().floatValue(), 0.0F);
        setUniform4(program, "SkyTexel", 1.0F / width, 1.0F / height, 0.0F, 0.0F);
    }

    private void renderAtmosphere(Framebuffer main, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix, int depthTex) {
        SimpleFramebuffer scene = resizeTo(sceneFB.get(), main);
        scene.beginWrite(false);
        main.draw(scene.textureWidth, scene.textureHeight);
        scene.endWrite();

        int mode = atmoModeIndex();
        MinecraftClient mc = MinecraftClient.getInstance();
        float tickDelta = mc.getRenderTickCounter().getTickDelta(false);
        float sunAngle = mc.world.getSkyAngle(tickDelta);
        float sinSun = -(float) Math.sin(sunAngle);
        float sunSide = sinSun >= 0.0F ? 1.0F : -1.0F;
        float sunPitch = sunPitch(mode);
        float sunDirY = sunSide * (float) Math.cos(sunPitch);
        float sunDirZ = (float) Math.sin(sunPitch);

        float sunScreenX = 0.5F;
        float sunScreenY = 0.5F;
        float sunScreenZ = 0.0F;
        sunVector.set(sunDirY, sunDirZ, 0.0F, 0.0F);
        positionMatrix.transform(sunVector);
        float sunForward = -sunVector.z;
        if (sunForward > 1.0E-4F) {
            sunVector.set(sunDirY * 1000.0F, sunDirZ * 1000.0F, sunForward * 1000.0F, 1.0F);
            projectionMatrix.transform(sunVector);
            if (sunVector.w > 1.0E-4F) {
                sunScreenX = sunVector.x / sunVector.w * 0.5F + 0.5F;
                sunScreenY = sunVector.y / sunVector.w * 0.5F + 0.5F;
                sunScreenZ = clamp01(sunForward * 4.0F);
            }
        }

        float rainbowDirX = -sunSide * (float) Math.cos(0.3F);
        float rainbowDirY = -(float) Math.sin(0.3F);

        fillPalette(mode, accentInt());

        inverseProjection.set(new Matrix4f(projectionMatrix)).invert();
        inverseView.set(new Matrix4f(positionMatrix)).invert();
        inverseView.m30((float) camera.getPos().x);
        inverseView.m31((float) camera.getPos().y);
        inverseView.m32((float) camera.getPos().z);

        RenderUtil.enableRender();
        ShaderProgram program = setProgram(DAWN_FOG_KEY);
        if (program == null) { RenderUtil.disableRender(); return; }
        try {
            main.beginWrite(true);
            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();
            writeAtmosphereUniforms(program, camera, tickDelta, sunDirY, sunDirZ,
                    sunScreenX, sunScreenY, sunScreenZ, rainbowDirX, rainbowDirY, mode);
            bindTextures(scene.getColorAttachment(), depthTex);
            drawQuad();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            resetTextures();
            RenderSystem.enableDepthTest();
            RenderUtil.disableRender();
        }
    }

    private void writeAtmosphereUniforms(ShaderProgram program, Camera camera, float tickDelta,
                                         float sunDirY, float sunDirZ,
                                         float sunScreenX, float sunScreenY, float sunScreenZ,
                                         float rainbowDirX, float rainbowDirY, int mode) {
        MinecraftClient mc = MinecraftClient.getInstance();
        float time = ((mc.world.getTime() % 100000L) + tickDelta) * 0.05F;
        float viewDistance = mc.options.getViewDistance().getValue() * 16.0F;
        float scatterHeightValue = clamp(scatterHeight.get().floatValue(), 60.0F, 120.0F);
        float density = clamp(atmoDensity.get().floatValue(), 0.05F, 0.8F);
        boolean nightMode = mode == 3;
        float rainbowV = !nightMode && rainbow.get() ? clamp01(rainbowBrightness.get().floatValue()) : 0.0F;
        float rainbowSizeV = clamp(rainbowSize.get().floatValue(), 40.0F, 64.0F);
        float godRaysV = clamp01(godRays.get().floatValue());
        float softnessV = clamp01(softness.get().floatValue());
        float starsV = nightMode ? clamp01(stars.get().floatValue()) : 0.0F;
        float auroraV = nightMode ? clamp01(aurora.get().floatValue()) : 0.0F;
        float moon = nightMode ? 1.0F : 0.0F;
        float sunGlowV = clamp(sunGlow.get().floatValue(), 0.0F, 1.5F);

        setMat4(program, "u_InverseProjectionMatrix", inverseProjection);
        setMat4(program, "u_InverseViewMatrix", inverseView);
        setUniform4(program, "u_CameraPosRaw", (float) camera.getPos().x, (float) camera.getPos().y, (float) camera.getPos().z, 0.0F);
        setUniform4(program, "u_TimeRaw", time, 0.0F, 0.0F, 0.0F);
        setUniform4(program, "u_ResolutionRaw", width, height, density, 0.0F);
        setUniform4(program, "u_SunDirectionRaw", sunDirY, sunDirZ, 0.0F, scatterHeightValue - 18.0F);
        setUniform4(program, "u_FogMaxRaw", scatterHeightValue, viewDistance, 0.0F, 0.0F);
        setUniform4(program, "u_PaletteZenithRaw", palette[0], palette[1], palette[2], 1.0F);
        setUniform4(program, "u_PaletteHorizonWarmRaw", palette[3], palette[4], palette[5], 1.0F);
        setUniform4(program, "u_PaletteHorizonCoolRaw", palette[6], palette[7], palette[8], 1.0F);
        setUniform4(program, "u_PaletteFogWarmRaw", palette[9], palette[10], palette[11], 1.0F);
        setUniform4(program, "u_PaletteFogCoolRaw", palette[12], palette[13], palette[14], 1.0F);
        setUniform4(program, "u_PaletteRayRaw", palette[15], palette[16], palette[17], 1.0F);
        setUniform4(program, "u_SunScreenRaw", sunScreenX, sunScreenY, sunScreenZ, rainbowV);
        setUniform4(program, "u_RainbowDirRaw", rainbowDirX, rainbowDirY, 0.0F, rainbowSizeV);
        setUniform4(program, "u_GodRaysRaw", godRaysV, softnessV, 0.0F, 0.0F);
        setUniform4(program, "u_StyleRaw", starsV, auroraV, moon, sunGlowV);
        setUniform4(program, "u_DebugRaw", debugIndex() == 1 ? 1.0F : 0.0F,
                debugIndex() == 2 ? 1.0F : 0.0F, debugIndex() == 3 ? 1.0F : 0.0F, debugIndex() == 4 ? 1.0F : 0.0F);
    }

    private int debugIndex() {
        if (debugView.is("Scene")) return 1;
        if (debugView.is("Depth")) return 2;
        if (debugView.is("SkyMask")) return 3;
        if (debugView.is("Ray")) return 4;
        return 0;
    }

    private void fillPalette(int mode, int dawnColor) {
        float dr = (dawnColor >> 16 & 255) / 255.0F;
        float dg = (dawnColor >> 8 & 255) / 255.0F;
        float db = (dawnColor & 255) / 255.0F;
        if (mode == 1) {
            float jr = 0.7882353F;
            float jg = 0.47058824F;
            float jb = 0.65882355F;
            mixOklab(palette, 0, 0.085F, 0.1F, 0.2F, jr, jg, jb, 0.3F);
            mixOklab(palette, 3, jr, jg, jb, 1.0F, 0.93F, 0.82F, 0.35F);
            mixOklab(palette, 6, 0.52F, 0.58F, 0.74F, jr, jg, jb, 0.28F);
            mixOklab(palette, 9, jr, jg, jb, 0.97F, 0.93F, 0.88F, 0.45F);
            mixOklab(palette, 12, 0.58F, 0.63F, 0.76F, jr, jg, jb, 0.35F);
            mixOklab(palette, 15, jr, jg, jb, 1.0F, 0.96F, 0.88F, 0.3F);
        } else if (mode == 2) {
            mixOklab(palette, 0, 0.16F, 0.19F, 0.38F, dr, dg, db, 0.14F);
            set3(palette, 3, clamp01(dr * 1.12F), clamp01(dg * 0.88F), clamp01(db * 0.62F));
            set3(palette, 6, 0.56F, 0.62F, 0.8F);
            mixOklab(palette, 9, dr, dg, db, 0.95F, 0.55F, 0.63F, 0.42F);
            set3(palette, 12, 0.6F, 0.67F, 0.82F);
            set3(palette, 15, clamp01(dr * 1.08F), clamp01(dg * 0.94F), clamp01(db * 0.72F));
        } else if (mode == 3) {
            set3(palette, 0, 0.045F, 0.055F, 0.135F);
            set3(palette, 3, 0.6F, 0.68F, 0.87F);
            set3(palette, 6, 0.095F, 0.125F, 0.25F);
            set3(palette, 9, 0.155F, 0.185F, 0.32F);
            set3(palette, 12, 0.115F, 0.145F, 0.275F);
            set3(palette, 15, 0.73F, 0.79F, 0.97F);
        } else {
            set3(palette, 0, 0.135F, 0.125F, 0.3F);
            set3(palette, 3, 0.89F, 0.46F, 0.55F);
            set3(palette, 6, 0.38F, 0.35F, 0.56F);
            set3(palette, 9, 0.8F, 0.52F, 0.62F);
            set3(palette, 12, 0.47F, 0.44F, 0.64F);
            set3(palette, 15, 0.92F, 0.56F, 0.72F);
        }
    }

    private static void set3(float[] floats, int offset, float r, float g, float b) {
        floats[offset] = r;
        floats[offset + 1] = g;
        floats[offset + 2] = b;
    }

    private static void mixOklab(float[] floats, int offset, float f, float g, float h, float j, float k, float l, float amount) {
        float t = clamp01(amount);
        float cbrtR1 = (float) Math.cbrt(0.41222146F * srgbToLinear(f) + 0.53633255F * srgbToLinear(g) + 0.051445995F * srgbToLinear(h));
        float cbrtG1 = (float) Math.cbrt(0.2119035F * srgbToLinear(f) + 0.6806995F * srgbToLinear(g) + 0.10739696F * srgbToLinear(h));
        float cbrtB1 = (float) Math.cbrt(0.08830246F * srgbToLinear(f) + 0.28171885F * srgbToLinear(g) + 0.6299787F * srgbToLinear(h));
        float cbrtR2 = (float) Math.cbrt(0.41222146F * srgbToLinear(j) + 0.53633255F * srgbToLinear(k) + 0.051445995F * srgbToLinear(l));
        float cbrtG2 = (float) Math.cbrt(0.2119035F * srgbToLinear(j) + 0.6806995F * srgbToLinear(k) + 0.10739696F * srgbToLinear(l));
        float cbrtB2 = (float) Math.cbrt(0.08830246F * srgbToLinear(j) + 0.28171885F * srgbToLinear(k) + 0.6299787F * srgbToLinear(l));
        float r = cbrtR1 + (cbrtR2 - cbrtR1) * t;
        float g2 = cbrtG1 + (cbrtG2 - cbrtG1) * t;
        float b2 = cbrtB1 + (cbrtB2 - cbrtB1) * t;
        float rr = r * r * r;
        float gg = g2 * g2 * g2;
        float bb = b2 * b2 * b2;
        floats[offset] = srgbFromLinear(4.0767417F * rr - 3.3077116F * gg + 0.23096994F * bb);
        floats[offset + 1] = srgbFromLinear(-1.268438F * rr + 2.6097574F * gg - 0.34131938F * bb);
        floats[offset + 2] = srgbFromLinear(-0.0041960864F * rr - 0.7034186F * gg + 1.7076147F * bb);
    }

    private static float srgbToLinear(float value) {
        return value <= 0.04045F ? value / 12.92F : (float) Math.pow((value + 0.055F) / 1.055F, 2.4);
    }

    private static float srgbFromLinear(float value) {
        value = clamp01(value);
        return value <= 0.0031308F ? value * 12.92F : (float) (1.055 * Math.pow(value, 0.4166666666666667) - 0.055);
    }

    private static float sunPitch(int mode) {
        return mode == 1 ? -0.045F : (mode == 2 ? 0.13F : (mode == 3 ? 0.17F : 0.11F));
    }

    private int atmoModeIndex() {
        if (atmoMode.is("Dawn")) return 0;
        if (atmoMode.is("Dusk")) return 1;
        if (atmoMode.is("Theme")) return 2;
        return 3;
    }

    private static float clamp01(float value) {
        return clamp(value, 0.0F, 1.0F);
    }

    private static float clamp(float value, float min, float max) {
        return !Float.isFinite(value) ? min : Math.max(min, Math.min(max, value));
    }

    private ShaderProgramKey cloudPipeline() {
        return switch (skyStyle.get()) {
            case "Nebula" -> CLOUDS_NEBULA_KEY;
            case "Plasma" -> CLOUDS_PLASMA_KEY;
            default -> CLOUDS_DEEP_SPACE_KEY;
        };
    }

    private ShaderProgramKey skyPipeline() {
        return switch (skyStyle.get()) {
            case "Nebula" -> SKY_NEBULA_KEY;
            case "Plasma" -> SKY_PLASMA_KEY;
            default -> SKY_DEEP_SPACE_KEY;
        };
    }

    private float shaderTime() {
        return System.currentTimeMillis() % 100000L / 1000.0F;
    }

    private float[] accentRgb() {
        int c = ModuleManager.STYLE_MANAGER.getFirstColor();
        return new float[]{(c >> 16 & 255) / 255.0F, (c >> 8 & 255) / 255.0F, (c & 255) / 255.0F};
    }

    private int accentInt() {
        return ModuleManager.STYLE_MANAGER.getFirstColor();
    }

    private static void setMat4(ShaderProgram program, String name, Matrix4f matrix) {
        try {
            GlUniform uniform = program.getUniform(name);
            if (uniform != null) uniform.set(matrix);
        } catch (Exception ignored) {}
    }

    private static void setUniform1(ShaderProgram program, String name, float value) {
        try {
            GlUniform uniform = program.getUniform(name);
            if (uniform != null) uniform.set(value);
        } catch (Exception ignored) {}
    }

    private static void setUniform4(ShaderProgram program, String name, float x, float y, float z, float w) {
        try {
            GlUniform uniform = program.getUniform(name);
            if (uniform != null) uniform.set(x, y, z, w);
        } catch (Exception ignored) {}
    }

    private static void bindTextures(int... textures) {
        for (int i = 0; i < textures.length; i++) {
            RenderSystem.setShaderTexture(i, textures[i]);
        }
    }

    private static void resetTextures() {
        for (int i = 0; i < 4; i++) RenderSystem.setShaderTexture(i, 0);
    }

    private void drawQuad() {
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(new Matrix4f(), ProjectionType.ORTHOGRAPHIC);
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.identity();
        try {
            Matrix4f identity = new Matrix4f();
            BufferBuilder buffer = IMinecraft.tessellator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            buffer.vertex(identity, -1, -1, 0).color(255, 255, 255, 255).texture(0, 0);
            buffer.vertex(identity, 1, -1, 0).color(255, 255, 255, 255).texture(1, 0);
            buffer.vertex(identity, 1, 1, 0).color(255, 255, 255, 255).texture(1, 1);
            buffer.vertex(identity, -1, 1, 0).color(255, 255, 255, 255).texture(0, 1);
            RenderUtil.render3D.endBuilding(buffer);
        } finally {
            modelView.popMatrix();
            RenderSystem.restoreProjectionMatrix();
        }
    }

    private static SimpleFramebuffer resizeTo(SimpleFramebuffer target, Framebuffer main) {
        if (target.textureWidth != main.textureWidth || target.textureHeight != main.textureHeight) {
            target.resize(main.textureWidth, main.textureHeight);
        }
        return target;
    }

    private static SimpleFramebuffer resizeClouds(SimpleFramebuffer target) {
        int targetWidth = Math.max(2, MinecraftClient.getInstance().getFramebuffer().textureWidth >> 1);
        int targetHeight = Math.max(2, MinecraftClient.getInstance().getFramebuffer().textureHeight >> 1);
        if (target.textureWidth != targetWidth || target.textureHeight != targetHeight) {
            target.resize(targetWidth, targetHeight);
        }
        return target;
    }

    private void setLinearFiltering(Framebuffer framebuffer) {
        RenderSystem.bindTexture(framebuffer.getColorAttachment());
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        RenderSystem.bindTexture(0);
    }

    private void configureDepthTexture(int depthTex) {
        RenderSystem.bindTexture(depthTex);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_MODE, 0);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        RenderSystem.bindTexture(0);
    }

    private static ShaderProgram setProgram(ShaderProgramKey key) {
        try {
            return RenderSystem.setShader(key);
        } catch (Exception e) {
            System.out.println("Failed to load shader: " + key.configId());
            e.printStackTrace();
            return null;
        }
    }
}