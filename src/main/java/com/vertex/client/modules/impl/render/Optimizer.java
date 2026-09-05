package com.vertex.client.modules.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vertex.client.modules.Module;
import com.vertex.client.modules.ModuleCategory;
import com.vertex.client.modules.ModuleInfo;
import com.vertex.client.modules.setting.SliderSetting;
import com.vertex.client.render.util.KawaseBlur;
import com.vertex.client.render.util.RenderUtil;
import com.vertex.client.util.IMinecraft;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

@ModuleInfo(name = "Optimizer", desc = "Делает картинку мыльной — мягкий Kawase-блюр всего экрана", type = ModuleCategory.Render, key = GLFW.GLFW_KEY_UNKNOWN)
public class Optimizer extends Module {

    private final SliderSetting strength = new SliderSetting("Сила", 100, 0, 100, 1);
    private final SliderSetting radius = new SliderSetting("Радиус", 3, 1, 16, 1);
    private final SliderSetting steps = new SliderSetting("Шаги", 5, 1, 8, 1);

    public Optimizer() {
        addSettings(strength, radius, steps);
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> this.draw(ctx));
    }

    @Override
    public void onEvent(Object event) {
    }

    private void draw(DrawContext ctx) {
        if (!isState()) return;
        if (mc.world == null) return;

        float alpha = strength.get().floatValue() / 100.0F;
        if (alpha <= 0.01F) return;

        KawaseBlur.blur.updateBlur(radius.get().floatValue(), steps.get().intValue());
        drawBlurOverlay(ctx, alpha);
    }

    private void drawBlurOverlay(DrawContext ctx, float alpha) {
        float w = mc.getWindow().getScaledWidth();
        float h = mc.getWindow().getScaledHeight();

        Matrix4f matrix = ctx.getMatrices().peek().getPositionMatrix();
        RenderUtil.enableRender();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, KawaseBlur.blur.getBlurredTexture());
        BufferBuilder bufferBuilder = IMinecraft.tessellator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(matrix, 0.0F, h, 0.0F).color(1.0F, 1.0F, 1.0F, alpha).texture(0.0F, 1.0F);
        bufferBuilder.vertex(matrix, w, h, 0.0F).color(1.0F, 1.0F, 1.0F, alpha).texture(1.0F, 1.0F);
        bufferBuilder.vertex(matrix, w, 0.0F, 0.0F).color(1.0F, 1.0F, 1.0F, alpha).texture(1.0F, 0.0F);
        bufferBuilder.vertex(matrix, 0.0F, 0.0F, 0.0F).color(1.0F, 1.0F, 1.0F, alpha).texture(0.0F, 0.0F);
        RenderUtil.render3D.endBuilding(bufferBuilder);
        RenderUtil.disableRender();
    }
}