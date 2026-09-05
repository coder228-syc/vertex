package com.vertex.client.render.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vertex.client.util.IMinecraft;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public class RvRenderUtils implements IMinecraft {

    public static void drawRect(MatrixStack matrices, float x, float y, float width, float height, int color) {
        float x2 = x + width;
        float y2 = y + height;
        float a = (color >> 24 & 0xFF) / 255.0F;
        float r = (color >> 16 & 0xFF) / 255.0F;
        float g = (color >> 8 & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        if (a == 0.0F) a = 1.0F;

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderUtil.enableRender();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder bufferBuilder = IMinecraft.tessellator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x, y2, 0.0F).color(r, g, b, a);
        bufferBuilder.vertex(matrix, x2, y2, 0.0F).color(r, g, b, a);
        bufferBuilder.vertex(matrix, x2, y, 0.0F).color(r, g, b, a);
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(r, g, b, a);
        RenderUtil.render3D.endBuilding(bufferBuilder);
        RenderUtil.disableRender();
    }

    public static void drawTexture(MatrixStack matrices, Identifier texture, float x, float y, float width, float height) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderUtil.enableRender();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, texture);
        BufferBuilder bufferBuilder = IMinecraft.tessellator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(matrix, x, y + height, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F).texture(0.0F, 1.0F);
        bufferBuilder.vertex(matrix, x + width, y + height, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F).texture(1.0F, 1.0F);
        bufferBuilder.vertex(matrix, x + width, y, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F).texture(1.0F, 0.0F);
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F).texture(0.0F, 0.0F);
        RenderUtil.render3D.endBuilding(bufferBuilder);
        RenderUtil.disableRender();
    }

    public static void drawTextureUV(
            MatrixStack matrices, Identifier texture, float x, float y, float width, float height, float u1, float v1, float u2, float v2
    ) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderUtil.enableRender();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, texture);
        BufferBuilder bufferBuilder = IMinecraft.tessellator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(matrix, x, y + height, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F).texture(u1, v2);
        bufferBuilder.vertex(matrix, x + width, y + height, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F).texture(u2, v2);
        bufferBuilder.vertex(matrix, x + width, y, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F).texture(u2, v1);
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(1.0F, 1.0F, 1.0F, 1.0F).texture(u1, v1);
        RenderUtil.render3D.endBuilding(bufferBuilder);
        RenderUtil.disableRender();
    }

    public static void drawRoundedRect(MatrixStack matrices, float x, float y, float width, float height, float radius, int color) {
        drawSingleRoundedRect(matrices, x, y, width, height, radius, color);
    }

    public static void drawSingleRoundedRect(MatrixStack matrices, float x, float y, float width, float height, float radius, int color) {
        float a = (color >> 24 & 0xFF) / 255.0F;
        float r = (color >> 16 & 0xFF) / 255.0F;
        float g = (color >> 8 & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        if (a <= 0.001F) return;

        radius = Math.min(radius, Math.min(width / 2.0F, height / 2.0F));
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        BufferBuilder bufferBuilder;

        RenderUtil.enableRender();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        if (radius <= 0.5F) {
            bufferBuilder = IMinecraft.tessellator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(matrix, x, y + height, 0.0F).color(r, g, b, a);
            bufferBuilder.vertex(matrix, x + width, y + height, 0.0F).color(r, g, b, a);
            bufferBuilder.vertex(matrix, x + width, y, 0.0F).color(r, g, b, a);
            bufferBuilder.vertex(matrix, x, y, 0.0F).color(r, g, b, a);
            RenderUtil.render3D.endBuilding(bufferBuilder);
        } else {
            bufferBuilder = IMinecraft.tessellator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            bufferBuilder.vertex(matrix, x + radius, y + height, 0.0F).color(r, g, b, a);
            bufferBuilder.vertex(matrix, x + width - radius, y + height, 0.0F).color(r, g, b, a);
            bufferBuilder.vertex(matrix, x + width - radius, y, 0.0F).color(r, g, b, a);
            bufferBuilder.vertex(matrix, x + radius, y, 0.0F).color(r, g, b, a);
            bufferBuilder.vertex(matrix, x, y + height - radius, 0.0F).color(r, g, b, a);
            bufferBuilder.vertex(matrix, x + radius, y + height - radius, 0.0F).color(r, g, b, a);
            bufferBuilder.vertex(matrix, x + radius, y + radius, 0.0F).color(r, g, b, a);
            bufferBuilder.vertex(matrix, x, y + radius, 0.0F).color(r, g, b, a);
            bufferBuilder.vertex(matrix, x + width - radius, y + height - radius, 0.0F).color(r, g, b, a);
            bufferBuilder.vertex(matrix, x + width, y + height - radius, 0.0F).color(r, g, b, a);
            bufferBuilder.vertex(matrix, x + width, y + radius, 0.0F).color(r, g, b, a);
            bufferBuilder.vertex(matrix, x + width - radius, y + radius, 0.0F).color(r, g, b, a);
            RenderUtil.render3D.endBuilding(bufferBuilder);

            bufferBuilder = IMinecraft.tessellator().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
            drawCornerSector(bufferBuilder, matrix, x + radius, y + radius, radius, 180, 270, r, g, b, a);
            drawCornerSector(bufferBuilder, matrix, x + width - radius, y + radius, radius, 270, 360, r, g, b, a);
            drawCornerSector(bufferBuilder, matrix, x + width - radius, y + height - radius, radius, 0, 90, r, g, b, a);
            drawCornerSector(bufferBuilder, matrix, x + radius, y + height - radius, radius, 90, 180, r, g, b, a);
            RenderUtil.render3D.endBuilding(bufferBuilder);
        }

        RenderUtil.disableRender();
    }

    private static void drawCornerSector(
            BufferBuilder bufferBuilder, Matrix4f matrix, float cx, float cy, float radius, int startAngle, int endAngle, float r, float g, float b, float a
    ) {
        for (int i = startAngle; i < endAngle; i += 10) {
            float rad1 = (float) Math.toRadians(i);
            float rad2 = (float) Math.toRadians(Math.min(endAngle, i + 10));
            bufferBuilder.vertex(matrix, cx, cy, 0.0F).color(r, g, b, a);
            bufferBuilder.vertex(matrix, cx + (float) Math.cos(rad1) * radius, cy + (float) Math.sin(rad1) * radius, 0.0F).color(r, g, b, a);
            bufferBuilder.vertex(matrix, cx + (float) Math.cos(rad2) * radius, cy + (float) Math.sin(rad2) * radius, 0.0F).color(r, g, b, a);
        }
    }

    public static void drawCircle(MatrixStack matrices, float cx, float cy, float radius, int color) {
        float a = (color >> 24 & 0xFF) / 255.0F;
        float r = (color >> 16 & 0xFF) / 255.0F;
        float g = (color >> 8 & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        if (a == 0.0F) a = 1.0F;

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderUtil.enableRender();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder bufferBuilder = IMinecraft.tessellator().begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR);
        for (int i = 0; i < 360; i += 10) {
            float rad1 = (float) Math.toRadians(i);
            float rad2 = (float) Math.toRadians(i + 10);
            bufferBuilder.vertex(matrix, cx, cy, 0.0F).color(r, g, b, a);
            bufferBuilder.vertex(matrix, cx + (float) Math.cos(rad1) * radius, cy + (float) Math.sin(rad1) * radius, 0.0F).color(r, g, b, a);
            bufferBuilder.vertex(matrix, cx + (float) Math.cos(rad2) * radius, cy + (float) Math.sin(rad2) * radius, 0.0F).color(r, g, b, a);
        }
        RenderUtil.render3D.endBuilding(bufferBuilder);
        RenderUtil.disableRender();
    }
}