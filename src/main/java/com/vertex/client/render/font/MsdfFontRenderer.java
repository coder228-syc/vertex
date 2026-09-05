package com.vertex.client.render.font;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vertex.client.render.util.RenderUtil;
import com.vertex.client.util.IMinecraft;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;

public final class MsdfFontRenderer implements IMinecraft {

    private final int weight;
    private final float size;
    private final MsdfFont latin;
    private final MsdfFont cyrillic;

    MsdfFontRenderer(int weight, float size) {
        this.weight = weight;
        this.size = size;
        this.latin = MsdfFont.get("sf_pro", weight);
        this.cyrillic = MsdfFont.get("google_sans", weight);
    }

    public int weight() {
        return weight;
    }

    public float size() {
        return size;
    }

    public float getWidth(String text) {
        if (text == null || text.isEmpty()) return 0f;
        float penEm = 0f;
        int i = 0;
        int len = text.length();
        while (i < len) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            penEm += fontFor(cp).glyph(cp).advanceEm();
        }
        return penEm * size;
    }

    public float getHeight() {
        return (latin.ascenderEm() - latin.descenderEm()) * size;
    }

    public float ascender() {
        return latin.ascenderEm() * size;
    }

    public void drawLeftAligned(MatrixStack ms, String text, float x, float y, int color) {
        render(ms, text, x, y, color);
    }

    public void drawRightAligned(MatrixStack ms, String text, float x, float y, int color) {
        render(ms, text, x - getWidth(text), y, color);
    }

    public void centeredDraw(MatrixStack ms, String text, float x, float y, int color) {
        render(ms, text, x - getWidth(text) * 0.5f, y, color);
    }

    private MsdfFont fontFor(int codePoint) {
        return (codePoint >= 0x400 && codePoint <= 0x4FF) ? cyrillic : latin;
    }

    private void render(MatrixStack ms, String text, float x, float y, int color) {
        if (text == null || text.isEmpty()) return;

        float a = ((color >>> 24) & 0xFF) / 255f;
        float r = ((color >>> 16) & 0xFF) / 255f;
        float g = ((color >>> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        Map<Identifier, List<Quad>> byTexture = new Object2ObjectOpenHashMap<>();
        float baseline = y + latin.ascenderEm() * size;
        float penEm = 0f;
        int i = 0;
        int len = text.length();
        while (i < len) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            MsdfFont font = fontFor(cp);
            MsdfFont.Glyph glyph = font.glyph(cp);
            MsdfFont.Bounds plane = glyph.plane();
            if (plane != null) {
                float x0 = x + (penEm + plane.left()) * size;
                float x1 = x + (penEm + plane.right()) * size;
                float y0 = baseline - plane.top() * size;
                float y1 = baseline - plane.bottom() * size;
                byTexture.computeIfAbsent(font.textureId(), k -> new ObjectArrayList<>())
                        .add(new Quad(x0, y0, x1, y1, glyph.u0(), glyph.v0(), glyph.u1(), glyph.v1()));
            }
            penEm += glyph.advanceEm();
        }
        if (byTexture.isEmpty()) return;

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        RenderUtil.enableRender();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        Matrix4f matrix4f = ms.peek().getPositionMatrix();

        Tessellator tessellator = IMinecraft.tessellator();
        byTexture.forEach((texture, quads) -> {
            RenderSystem.setShaderTexture(0, texture);
            BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            for (Quad quad : quads) {
                bufferBuilder.vertex(matrix4f, quad.x0, quad.y1, 0).texture(quad.u0, quad.v1).color(r, g, b, a);
                bufferBuilder.vertex(matrix4f, quad.x1, quad.y1, 0).texture(quad.u1, quad.v1).color(r, g, b, a);
                bufferBuilder.vertex(matrix4f, quad.x1, quad.y0, 0).texture(quad.u1, quad.v0).color(r, g, b, a);
                bufferBuilder.vertex(matrix4f, quad.x0, quad.y0, 0).texture(quad.u0, quad.v0).color(r, g, b, a);
            }
            RenderUtil.render3D.endBuilding(bufferBuilder);
        });

        RenderUtil.disableRender();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private record Quad(float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1) {
    }
}