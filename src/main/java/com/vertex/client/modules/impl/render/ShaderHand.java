package com.vertex.client.modules.impl.render;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.vertex.client.modules.Module;
import com.vertex.client.modules.ModuleManager;
import com.vertex.client.modules.ModuleCategory;
import com.vertex.client.modules.ModuleInfo;
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
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@ModuleInfo(name = "ShaderHand", desc = "Красивые шейдеры на руки", type = ModuleCategory.Render, key = GLFW.GLFW_KEY_UNKNOWN)
public class ShaderHand extends Module {
    public static ShaderHand INSTANCE;

    public static final ShaderProgramKey HANDS_MASK_DIFF_KEY = key("hands_mask_diff");
    public static final ShaderProgramKey HANDS_FILL_KEY = key("hands_fill");
    public static final ShaderProgramKey HANDS_SHADER_KEY = key("hands_shader");
    public static final ShaderProgramKey HANDS_GLOW_KEY = key("hands_glow");
    public static final ShaderProgramKey HANDS_OUTLINE_KEY = key("hands_outline");
    public static final ShaderProgramKey HANDS_KAWASE_DOWN_KEY = key("hands_kawase_down");
    public static final ShaderProgramKey HANDS_KAWASE_UP_KEY = key("hands_kawase_up");
    public static final ShaderProgramKey STYLE_NIGHT_KEY = key("hands_style_night");
    public static final ShaderProgramKey STYLE_CLASSIC_KEY = key("hands_style_classic");
    public static final ShaderProgramKey STYLE_CAUSTICS_KEY = key("hands_style_caustics");
    public static final ShaderProgramKey STYLE_PRISMATIC_KEY = key("hands_style_prismatic");
    public static final ShaderProgramKey STYLE_GLOSSY_KEY = key("hands_style_glossy");
    public static final ShaderProgramKey STYLE_DEEP_SPACE_KEY = key("hands_style_deep_space");
    public static final ShaderProgramKey STYLE_NEBULA_KEY = key("hands_style_nebula");

    private static final float[] SKY_COLOR_1 = {0.18F, 0.48F, 0.95F};
    private static final float[] SKY_COLOR_2 = {0.55F, 0.92F, 1.0F};

    public final ModeSetting mode = new ModeSetting("Mode", "Fill", "Fill", "Shader");
    public final SliderSetting shaderAlpha = new SliderSetting("Shader Opacity", 0.72, 0, 1.0, 0.05, () -> mode.is("Shader"));
    public final SliderSetting shaderSpeed = new SliderSetting("Shader Speed", 0.75, 0, 3.0, 0.05, () -> mode.is("Shader"));
    public final ModeSetting shaderStyle = new ModeSetting(() -> mode.is("Shader"), "Style", "Flow", "Flow", "Night", "Caustics", "Glossy", "Nebula");
    public final SliderSetting shaderIntensity = new SliderSetting("Shader Intensity", 1.5, 0.1, 3.0, 0.05, () -> mode.is("Shader"));
    public final BooleanSetting fillRainbow = new BooleanSetting("Rainbow", false, () -> mode.is("Fill"));
    public final SliderSetting rainbowSpeed = new SliderSetting("Rainbow Speed", 0.4, 0, 2.0, 0.05, () -> mode.is("Fill") && fillRainbow.get());
    public final SliderSetting rainbowScale = new SliderSetting("Rainbow Scale", 1.0, 0.2, 3.0, 0.1, () -> mode.is("Fill") && fillRainbow.get());
    public final SliderSetting fillAlpha = new SliderSetting("Fill Opacity", 0.8, 0, 1.0, 0.05, () -> mode.is("Fill"));
    public final BooleanSetting keepShading = new BooleanSetting("Keep Shading", true, () -> mode.is("Fill"));
    public final SliderSetting shadingStrength = new SliderSetting("Shading Strength", 0.3, 0, 1.0, 0.05, () -> mode.is("Fill") && keepShading.get());
    public final BooleanSetting outlineEnabled = new BooleanSetting("Outline", false);
    public final SliderSetting outlineWidth = new SliderSetting("Outline Width", 1.0, 0.5, 3.0, 0.5, () -> outlineEnabled.get());
    public final BooleanSetting glowEnabled = new BooleanSetting("Glow", true);
    public final ModeSetting glowColor = new ModeSetting(() -> glowEnabled.get(), "Glow Color", "Sky", "Sky", "Theme Color", "Item Color");
    public final SliderSetting glowRadius = new SliderSetting("Blur", 4, 0, 6, 1, () -> glowEnabled.get());
    public final BooleanSetting outerGlow = new BooleanSetting("Outer Glow", true, () -> glowEnabled.get());
    public final SliderSetting glowExposure = new SliderSetting("Brightness", 1.45, 0, 5.0, 0.1, () -> glowEnabled.get() && outerGlow.get());
    public final SliderSetting saturation = new SliderSetting("Saturation", 1.4, 0, 3.0, 0.1, () -> glowEnabled.get() || outlineEnabled.get());

    private final Supplier<SimpleFramebuffer> beforeFB = Suppliers.memoize(() -> new SimpleFramebuffer(1, 1, true));
    private final Supplier<SimpleFramebuffer> afterFB = Suppliers.memoize(() -> new SimpleFramebuffer(1, 1, true));
    private final Supplier<SimpleFramebuffer> maskFB = Suppliers.memoize(() -> new SimpleFramebuffer(1, 1, false));
    private final List<SimpleFramebuffer> bloomBuffers = new ArrayList<>();

    private int width = 1, height = 1;
    private int configuredBeforeDepthTex = -1;
    private int configuredAfterDepthTex = -1;
    private int lastBloomTex = -1;
    private long shaderSessionStart = 0L;

    public ShaderHand() {
        INSTANCE = this;
        addSettings(mode, shaderAlpha, shaderSpeed, shaderStyle, shaderIntensity, fillRainbow, rainbowSpeed, rainbowScale, fillAlpha,
                keepShading, shadingStrength, outlineEnabled, outlineWidth, glowEnabled, glowColor,
                glowRadius, outerGlow, glowExposure, saturation);
    }

    @Override
    public void onEvent(Object event) {}

    @Override
    protected void onEnable() {
        shaderSessionStart = System.currentTimeMillis();
    }

    @Override
    protected void onDisable() {
        lastBloomTex = -1;
        configuredBeforeDepthTex = -1;
        configuredAfterDepthTex = -1;
    }

    private static ShaderProgramKey key(String path) {
        return new ShaderProgramKey(Identifier.of("vertexclient", "core/hands/" + path), VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY);
    }

    public static boolean isActive() {
        return INSTANCE != null && INSTANCE.isState();
    }

    public void captureBefore() {
        if (!isActive()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        Framebuffer main = mc.getFramebuffer();
        if (main == null) return;
        try {
            SimpleFramebuffer before = resize(beforeFB.get(), main);
            before.beginWrite(false);
            main.draw(before.textureWidth, before.textureHeight);
            before.endWrite();
            before.copyDepthFrom(main);
            main.beginWrite(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void captureAfter() {
        if (!isActive()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        Framebuffer main = mc.getFramebuffer();
        if (main == null) return;
        try {
            SimpleFramebuffer after = resize(afterFB.get(), main);
            after.beginWrite(false);
            main.draw(after.textureWidth, after.textureHeight);
            after.endWrite();
            after.copyDepthFrom(main);
            main.beginWrite(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void renderPipeline() {
        if (!isActive()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        Framebuffer main = mc.getFramebuffer();
        if (main == null) return;
        try {
            this.width = main.textureWidth;
            this.height = main.textureHeight;
            if (width <= 0 || height <= 0) return;

            SimpleFramebuffer before = resize(beforeFB.get(), main);
            SimpleFramebuffer after = resize(afterFB.get(), main);
            SimpleFramebuffer mask = resize(maskFB.get(), main);

            int beforeDepth = before.getDepthAttachment();
            int afterDepth = after.getDepthAttachment();
            if (beforeDepth != 0 && beforeDepth != configuredBeforeDepthTex) {
                configureDepthTexture(beforeDepth);
                configuredBeforeDepthTex = beforeDepth;
            }
            if (afterDepth != 0 && afterDepth != configuredAfterDepthTex) {
                configureDepthTexture(afterDepth);
                configuredAfterDepthTex = afterDepth;
            }

            drawMaskDiff(mask, before.getColorAttachment(), after.getColorAttachment(), beforeDepth, afterDepth);

            lastBloomTex = -1;
            if (mode.is("Fill")) {
                renderFillOverlay(after.getColorAttachment(), mask.getColorAttachment());
            } else if (mode.is("Shader")) {
                renderShaderOverlay(after.getColorAttachment(), mask.getColorAttachment());
            }

            if (glowEnabled.get() && outerGlow.get()) {
                int passes = Math.max(0, Math.min((int) Math.floor(glowRadius.get().intValue()), 12));
                if (passes > 0) {
                    int bloom = runBloomPasses(passes, mask.getColorAttachment());
                    lastBloomTex = bloom;
                    renderGlowComposite(bloom, mask.getColorAttachment(), after.getColorAttachment());
                }
            } else if (outlineEnabled.get()) {
                lastBloomTex = runBloomPasses(3, mask.getColorAttachment());
            }

            if (outlineEnabled.get()) {
                renderOutline(mask.getColorAttachment());
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

    private void drawMaskDiff(Framebuffer target, int beforeTex, int afterTex, int beforeDepth, int afterDepth) {
        RenderUtil.enableRender();
        ShaderProgram program = setProgram(HANDS_MASK_DIFF_KEY);
        if (program == null) { RenderUtil.disableRender(); return; }
        try {
            RenderSystem.disableDepthTest();
            RenderSystem.disableBlend();
            target.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            target.clear();
            target.beginWrite(false);
            bindTextures(beforeTex, afterTex, beforeDepth, afterDepth);
            drawQuad();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            resetTextures();
            RenderSystem.enableDepthTest();
            RenderUtil.disableRender();
            MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
        }
    }

    private void renderFillOverlay(int sceneTex, int maskTex) {
        float alpha = fillAlpha.get().floatValue();
        if (alpha <= 0.001F) return;
        RenderUtil.enableRender();
        ShaderProgram program = setProgram(HANDS_FILL_KEY);
        if (program == null) { RenderUtil.disableRender(); return; }
        try {
            MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            float[] accent = accentRgb();
            setUniform3(program, "fillColor", accent[0], accent[1], accent[2]);
            setUniform1(program, "fillAlpha", alpha);
            setUniform1(program, "shadingStrength", shadingStrength.get().floatValue());
            setUniformInt(program, "keepShading", keepShading.get() ? 1 : 0);
            setUniformInt(program, "rainbow", fillRainbow.get() ? 1 : 0);
            setUniform1(program, "rainbowTime", System.currentTimeMillis() % 100000L / 1000.0F);
            setUniform1(program, "rainbowSpeed", rainbowSpeed.get().floatValue());
            setUniform1(program, "rainbowScale", rainbowScale.get().floatValue());
            setUniform1(program, "screenH", height);
            bindTextures(sceneTex, maskTex);
            drawQuad();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            resetTextures();
            RenderUtil.disableRender();
            MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
        }
    }

    private void renderShaderOverlay(int sceneTex, int maskTex) {
        RenderUtil.enableRender();
        ShaderProgram program = setProgram(shaderPipeline());
        if (program == null) { RenderUtil.disableRender(); return; }
        try {
            MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            float[] accent = accentRgb();
            float time = (System.currentTimeMillis() - shaderSessionStart) / 1000.0F;
            setUniform2(program, "resolution", width, height);
            setUniform1(program, "time", time);
            setUniform1(program, "alpha", shaderAlpha.get().floatValue());
            setUniform1(program, "speed", shaderSpeed.get().floatValue());
            setUniform3(program, "primaryColor", accent[0], accent[1], accent[2]);
            setUniform3(program, "accentColor", accent[0], accent[1], accent[2]);
            setUniform4(program, "Tint", accent[0], accent[1], accent[2], 1.0F);
            setUniform4(program, "Params", width, height, time * shaderSpeed.get().floatValue(),
                    shaderIntensity.get().floatValue());
            bindTextures(sceneTex, maskTex);
            drawQuad();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            resetTextures();
            RenderUtil.disableRender();
            MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
        }
    }

    private ShaderProgramKey shaderPipeline() {
        return switch (shaderStyle.get()) {
            case "Night" -> STYLE_NIGHT_KEY;
            case "Classic" -> STYLE_CLASSIC_KEY;
            case "Caustics" -> STYLE_CAUSTICS_KEY;
            case "Prismatic" -> STYLE_PRISMATIC_KEY;
            case "Glossy" -> STYLE_GLOSSY_KEY;
            case "Deep Space" -> STYLE_DEEP_SPACE_KEY;
            case "Nebula" -> STYLE_NEBULA_KEY;
            default -> HANDS_SHADER_KEY;
        };
    }

    private void renderGlowComposite(int bloomTex, int maskTex, int sceneTex) {
        RenderUtil.enableRender();
        ShaderProgram program = setProgram(HANDS_GLOW_KEY);
        if (program == null) { RenderUtil.disableRender(); return; }
        try {
            MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            float[] c1 = resolveGlowColor1();
            float[] c2 = resolveGlowColor2();
            setUniform3(program, "glowColor1", c1[0], c1[1], c1[2]);
            setUniform3(program, "glowColor2", c2[0], c2[1], c2[2]);
            setUniform1(program, "exposure", glowExposure.get().floatValue());
            setUniform1(program, "saturation", saturation.get().floatValue());
            setUniformInt(program, "outerOnly", outerGlow.get() ? 1 : 0);
            setUniformInt(program, "colorMode", glowColorModeIndex());
            bindTextures(bloomTex, maskTex, sceneTex);
            drawQuad();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            resetTextures();
            RenderSystem.defaultBlendFunc();
            RenderUtil.disableRender();
            MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
        }
    }

    private void renderOutline(int maskTex) {
        RenderUtil.enableRender();
        ShaderProgram program = setProgram(HANDS_OUTLINE_KEY);
        if (program == null) { RenderUtil.disableRender(); return; }
        try {
            MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            boolean rainbow = fillRainbow.get() && mode.is("Fill");
            boolean hasBloom = lastBloomTex != -1;
            int colorMode = rainbow ? 1 : (hasBloom ? 2 : 0);
            float[] accent = accentRgba();
            setUniformInt(program, "colorMode", colorMode);
            setUniform1(program, "width", outlineWidth.get().floatValue());
            setUniform2(program, "texelSize", 1.0F / width, 1.0F / height);
            setUniform1(program, "alpha", accent[3]);
            setUniform1(program, "rainbowTime", System.currentTimeMillis() % 100000L / 1000.0F);
            setUniform1(program, "rainbowSpeed", rainbowSpeed.get().floatValue());
            setUniform1(program, "rainbowScale", rainbowScale.get().floatValue());
            setUniform1(program, "screenH", height);
            setUniform1(program, "saturation", saturation.get().floatValue());
            setUniform3(program, "solidColor", accent[0], accent[1], accent[2]);
            if (hasBloom) {
                bindTextures(maskTex, lastBloomTex);
            } else {
                bindTextures(maskTex, 0);
            }
            drawQuad();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            resetTextures();
            RenderUtil.disableRender();
            MinecraftClient.getInstance().getFramebuffer().beginWrite(true);
        }
    }

    private int glowColorModeIndex() {
        if (glowColor.is("Theme Color")) return 1;
        if (glowColor.is("Item Color")) return 2;
        return 0;
    }

    private float[] resolveGlowColor1() {
        if (glowColor.is("Sky")) {
            return SKY_COLOR_1;
        }
        return accentRgb();
    }

    private float[] resolveGlowColor2() {
        if (glowColor.is("Sky")) {
            return SKY_COLOR_2;
        }
        return accentRgb();
    }

    private float[] accentRgb() {
        int c = ModuleManager.STYLE_MANAGER.getFirstColor();
        return new float[]{(c >> 16 & 255) / 255.0F, (c >> 8 & 255) / 255.0F, (c & 255) / 255.0F};
    }

    private float[] accentRgba() {
        int c = ModuleManager.STYLE_MANAGER.getFirstColor();
        return new float[]{(c >> 16 & 255) / 255.0F, (c >> 8 & 255) / 255.0F, (c & 255) / 255.0F,
                (c >> 24 & 255) / 255.0F};
    }

    private int runBloomPasses(int passes, int maskTex) {
        int texture = maskTex;
        setupBloomBuffers(passes);
        for (int i = 0; i < passes; i++) {
            SimpleFramebuffer buffer = bloomBuffers.get(i);
            drawKawase(HANDS_KAWASE_DOWN_KEY, buffer, texture, i);
            texture = buffer.getColorAttachment();
        }
        for (int i = passes - 1; i >= 1; i--) {
            SimpleFramebuffer buffer = bloomBuffers.get(i - 1);
            drawKawase(HANDS_KAWASE_UP_KEY, buffer, texture, i);
            texture = buffer.getColorAttachment();
        }
        return texture;
    }

    private void drawKawase(ShaderProgramKey key, SimpleFramebuffer target, int sourceTexture, int pass) {
        RenderUtil.enableRender();
        ShaderProgram program = setProgram(key);
        if (program == null) { RenderUtil.disableRender(); return; }
        try {
            target.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            target.clear();
            target.beginWrite(true);
            RenderSystem.disableBlend();
            setUniform2(program, "uSize", target.textureWidth, target.textureHeight);
            setUniform2(program, "uOffset", 1.0F + pass, 1.0F + pass);
            setUniform2(program, "uHalfPixel", 0.5F / target.textureWidth, 0.5F / target.textureHeight);
            if (key == HANDS_KAWASE_UP_KEY) {
                setUniform3(program, "color", 1.0F, 1.0F, 1.0F);
            }
            bindTextures(sourceTexture);
            drawQuad();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            resetTextures();
            RenderUtil.disableRender();
        }
    }

    private void setupBloomBuffers(int count) {
        int width = this.width;
        int height = this.height;
        if (bloomBuffers.size() < count) {
            while (bloomBuffers.size() < count) {
                int i = bloomBuffers.size();
                SimpleFramebuffer buffer = new SimpleFramebuffer(Math.max(2, width >> (i + 1)),
                        Math.max(2, height >> (i + 1)), false);
                setLinearFiltering(buffer);
                bloomBuffers.add(buffer);
            }
        }
        for (int i = 0; i < count; i++) {
            int targetWidth = Math.max(2, width >> (i + 1));
            int targetHeight = Math.max(2, height >> (i + 1));
            SimpleFramebuffer buffer = bloomBuffers.get(i);
            if (buffer.textureWidth != targetWidth || buffer.textureHeight != targetHeight) {
                buffer.resize(targetWidth, targetHeight);
                setLinearFiltering(buffer);
            }
        }
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

    private static void setUniform1(ShaderProgram program, String name, float value) {
        try {
            GlUniform uniform = program.getUniform(name);
            if (uniform != null) uniform.set(value);
        } catch (Exception ignored) {}
    }

    private static void setUniform2(ShaderProgram program, String name, float x, float y) {
        try {
            GlUniform uniform = program.getUniform(name);
            if (uniform != null) uniform.set(x, y);
        } catch (Exception ignored) {}
    }

    private static void setUniform3(ShaderProgram program, String name, float x, float y, float z) {
        try {
            GlUniform uniform = program.getUniform(name);
            if (uniform != null) uniform.set(x, y, z);
        } catch (Exception ignored) {}
    }

    private static void setUniform4(ShaderProgram program, String name, float x, float y, float z, float w) {
        try {
            GlUniform uniform = program.getUniform(name);
            if (uniform != null) uniform.set(x, y, z, w);
        } catch (Exception ignored) {}
    }

    private static void setUniformInt(ShaderProgram program, String name, int value) {
        try {
            GlUniform uniform = program.getUniform(name);
            if (uniform != null) uniform.set(value);
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

    private static SimpleFramebuffer resize(SimpleFramebuffer target, Framebuffer main) {
        if (target.textureWidth != main.textureWidth || target.textureHeight != main.textureHeight) {
            target.resize(main.textureWidth, main.textureHeight);
        }
        return target;
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