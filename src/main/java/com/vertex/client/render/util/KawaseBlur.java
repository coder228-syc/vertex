package com.vertex.client.render.util;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.RenderSystem;
import com.vertex.client.render.util.providers.ResourceProvider;
import com.vertex.client.render.shader.ShaderManager;
import com.vertex.client.util.IMinecraft;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.util.math.MatrixStack;

public class KawaseBlur implements IMinecraft {
    public static final KawaseBlur blur = new KawaseBlur();

    private final Supplier<SimpleFramebuffer> additional = Suppliers.memoize(() -> new SimpleFramebuffer(1, 1, false));
    private final Supplier<SimpleFramebuffer> blurred = Suppliers.memoize(() -> new SimpleFramebuffer(1, 1, false));

    public int getBlurredTexture() {
        return blurred.get().getColorAttachment();
    }

    public void updateBlur(float offset, int steps) {
        Framebuffer mainFramebuffer = mc.getFramebuffer();
        if (mainFramebuffer == null) {
            return;
        }
        SimpleFramebuffer fboAdditional = additional.get();
        SimpleFramebuffer fboBlurred = blurred.get();
        resize(fboAdditional, mainFramebuffer);
        resize(fboBlurred, mainFramebuffer);

        MatrixStack matrices = new MatrixStack();
        float width = mc.getWindow().getScaledWidth();
        float height = mc.getWindow().getScaledHeight();
        float invW = 1f / mainFramebuffer.textureWidth;
        float invH = 1f / mainFramebuffer.textureHeight;

        try {
            fboAdditional.beginWrite(true);
            if (!kawaseDown(matrices, mainFramebuffer.getColorAttachment(), offset, invW, invH, width, height)) {
                return;
            }

            SimpleFramebuffer[] buffers = {fboAdditional, fboBlurred};
            for (int i = 1; i < steps; ++i) {
                int step = i % 2;
                buffers[step].beginWrite(true);
                if (!kawaseDown(matrices, buffers[(step + 1) % 2].getColorAttachment(), offset, invW, invH, width, height)) {
                    return;
                }
            }

            for (int i = 0; i < steps; ++i) {
                int step = i % 2;
                buffers[(step + 1) % 2].beginWrite(true);
                if (!kawaseUp(matrices, buffers[step].getColorAttachment(), offset, invW, invH, width, height)) {
                    return;
                }
            }
        } finally {
            mainFramebuffer.beginWrite(false);
            RenderSystem.setShaderTexture(0, 0);
        }
    }

    private static void resize(SimpleFramebuffer target, Framebuffer source) {
        if (target.textureWidth != source.textureWidth || target.textureHeight != source.textureHeight) {
            target.resize(source.textureWidth, source.textureHeight);
        }
    }

    private static boolean kawaseDown(MatrixStack matrices, int sourceTexture, float offset, float invW, float invH, float width, float height) {
        RenderUtil.enableRender();
        ShaderProgram shader = RenderSystem.setShader(ResourceProvider.KAWASE_DOWN_SHADER_KEY);
        if (shader == null) {
            RenderUtil.disableRender();
            return false;
        }
        RenderSystem.setShaderTexture(0, sourceTexture);
        shader.getUniform("offset").set(offset);
        shader.getUniform("resolution").set(invW, invH);
        ShaderManager.vertexShader(matrices, 0, 0, width, height, -1);
        RenderUtil.disableRender();
        return true;
    }

    private static boolean kawaseUp(MatrixStack matrices, int sourceTexture, float offset, float invW, float invH, float width, float height) {
        RenderUtil.enableRender();
        ShaderProgram shader = RenderSystem.setShader(ResourceProvider.KAWASE_UP_SHADER_KEY);
        if (shader == null) {
            RenderUtil.disableRender();
            return false;
        }
        RenderSystem.setShaderTexture(0, sourceTexture);
        shader.getUniform("offset").set(offset);
        shader.getUniform("resolution").set(invW, invH);
        ShaderManager.vertexShader(matrices, 0, 0, width, height, -1);
        RenderUtil.disableRender();
        return true;
    }
}
